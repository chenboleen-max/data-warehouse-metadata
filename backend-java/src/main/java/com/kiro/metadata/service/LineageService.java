package com.kiro.metadata.service;

import com.kiro.metadata.dto.request.LineageCreateRequest;
import com.kiro.metadata.dto.response.ImpactReport;
import com.kiro.metadata.dto.response.LineageEdge;
import com.kiro.metadata.dto.response.LineageGraph;
import com.kiro.metadata.dto.response.LineageNode;
import com.kiro.metadata.entity.Lineage;
import com.kiro.metadata.entity.TableMetadata;
import com.kiro.metadata.entity.User;
import com.kiro.metadata.exception.CircularDependencyException;
import com.kiro.metadata.exception.DuplicateResourceException;
import com.kiro.metadata.exception.ResourceNotFoundException;
import com.kiro.metadata.repository.LineageRepository;
import com.kiro.metadata.repository.TableRepository;
import com.kiro.metadata.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 血缘关系服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LineageService {

    private final LineageRepository lineageRepository;
    private final TableRepository tableRepository;
    private final UserRepository userRepository;
    private final HistoryService historyService;

    /**
     * 创建血缘关系
     */
    @Transactional
    public Lineage createLineage(LineageCreateRequest request, String username) {
        log.info("Creating lineage: source={}, target={}, user={}", 
                 request.getSourceTableId(), request.getTargetTableId(), username);

        // 检查是否已存在
        if (lineageRepository.existsBySourceTableIdAndTargetTableId(
                request.getSourceTableId(), request.getTargetTableId())) {
            throw new DuplicateResourceException("血缘关系已存在");
        }

        // 获取表
        TableMetadata sourceTable = tableRepository.findById(request.getSourceTableId())
            .orElseThrow(() -> new ResourceNotFoundException("源表不存在: " + request.getSourceTableId()));
        TableMetadata targetTable = tableRepository.findById(request.getTargetTableId())
            .orElseThrow(() -> new ResourceNotFoundException("目标表不存在: " + request.getTargetTableId()));

        // 获取用户
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在: " + username));

        // 创建血缘关系
        Lineage lineage = new Lineage();
        lineage.setSourceTable(sourceTable);
        lineage.setTargetTable(targetTable);
        lineage.setLineageType(request.getLineageType());
        lineage.setTransformationLogic(request.getTransformationLogic());
        lineage.setCreatedBy(user);
        lineage.setCreatedAt(LocalDateTime.now());
        lineage.setUpdatedAt(LocalDateTime.now());

        Lineage saved = lineageRepository.save(lineage);
        log.info("Lineage created successfully: id={}", saved.getId());

        // 检查是否产生循环依赖
        List<String> cycle = detectCircularDependency(request.getTargetTableId());
        if (!cycle.isEmpty()) {
            log.warn("Circular dependency detected: {}", cycle);
            // 可以选择抛出异常或仅记录警告
            // throw new CircularDependencyException("检测到循环依赖: " + cycle);
        }

        // 记录变更历史
        historyService.recordChange("LINEAGE", saved.getId(), "CREATE", null, null, null, username);

        return saved;
    }

    /**
     * 根据 ID 获取血缘关系
     */
    public Lineage getLineageById(String lineageId) {
        log.debug("Getting lineage by id: {}", lineageId);
        return lineageRepository.findById(lineageId)
            .orElseThrow(() -> new ResourceNotFoundException("血缘关系不存在: " + lineageId));
    }

    /**
     * 删除血缘关系
     */
    @Transactional
    public void deleteLineage(String lineageId, String username) {
        log.info("Deleting lineage: id={}, user={}", lineageId, username);

        Lineage lineage = getLineageById(lineageId);

        // 记录变更历史
        historyService.recordChange("LINEAGE", lineageId, "DELETE", null, 
                                   lineage.getSourceTable().getId() + " -> " + lineage.getTargetTable().getId(), 
                                   null, username);

        lineageRepository.delete(lineage);
        log.info("Lineage deleted successfully: id={}", lineageId);
    }

    /**
     * 获取上游表列表
     */
    public List<TableMetadata> getUpstreamTables(String tableId) {
        log.debug("Getting upstream tables for: {}", tableId);
        List<Lineage> lineages = lineageRepository.findByTargetTableId(tableId);
        return lineages.stream()
            .map(Lineage::getSourceTable)
            .toList();
    }

    /**
     * 获取下游表列表
     */
    public List<TableMetadata> getDownstreamTables(String tableId) {
        log.debug("Getting downstream tables for: {}", tableId);
        List<Lineage> lineages = lineageRepository.findBySourceTableId(tableId);
        return lineages.stream()
            .map(Lineage::getTargetTable)
            .toList();
    }

    /**
     * 构建血缘关系图
     */
    public LineageGraph buildLineageGraph(String tableId, String direction, int maxDepth) {
        log.info("Building lineage graph: tableId={}, direction={}, maxDepth={}", tableId, direction, maxDepth);

        if (maxDepth < 1 || maxDepth > 5) {
            throw new IllegalArgumentException("深度必须在 1-5 之间");
        }

        TableMetadata rootTable = tableRepository.findById(tableId)
            .orElseThrow(() -> new ResourceNotFoundException("表不存在: " + tableId));

        Set<String> visited = new HashSet<>();
        Map<String, LineageNode> nodes = new HashMap<>();
        List<LineageEdge> edges = new ArrayList<>();

        // 添加根节点
        nodes.put(tableId, LineageNode.builder()
            .id(tableId)
            .name(rootTable.getDatabaseName() + "." + rootTable.getTableName())
            .depth(0)
            .type("root")
            .build());

        // 执行 DFS
        if ("upstream".equals(direction) || "both".equals(direction)) {
            dfsUpstream(tableId, 1, maxDepth, visited, nodes, edges);
        }

        if ("downstream".equals(direction) || "both".equals(direction)) {
            visited.clear(); // 重置访问标记
            dfsDownstream(tableId, 1, maxDepth, visited, nodes, edges);
        }

        LineageGraph graph = LineageGraph.builder()
            .nodes(new ArrayList<>(nodes.values()))
            .edges(edges)
            .build();

        log.info("Lineage graph built: nodes={}, edges={}", nodes.size(), edges.size());
        return graph;
    }

    /**
     * DFS 遍历上游
     */
    private void dfsUpstream(String currentId, int currentDepth, int maxDepth,
                            Set<String> visited, Map<String, LineageNode> nodes, List<LineageEdge> edges) {
        if (currentDepth > maxDepth || visited.contains(currentId)) {
            return;
        }

        visited.add(currentId);

        List<Lineage> lineages = lineageRepository.findByTargetTableId(currentId);
        for (Lineage lineage : lineages) {
            String sourceId = lineage.getSourceTable().getId();
            
            // 添加边
            edges.add(LineageEdge.builder()
                .source(sourceId)
                .target(currentId)
                .type(lineage.getLineageType().name())
                .build());

            // 添加节点（如果不存在）
            if (!nodes.containsKey(sourceId)) {
                TableMetadata sourceTable = lineage.getSourceTable();
                nodes.put(sourceId, LineageNode.builder()
                    .id(sourceId)
                    .name(sourceTable.getDatabaseName() + "." + sourceTable.getTableName())
                    .depth(currentDepth)
                    .type("upstream")
                    .build());

                // 递归遍历
                dfsUpstream(sourceId, currentDepth + 1, maxDepth, visited, nodes, edges);
            }
        }
    }

    /**
     * DFS 遍历下游
     */
    private void dfsDownstream(String currentId, int currentDepth, int maxDepth,
                              Set<String> visited, Map<String, LineageNode> nodes, List<LineageEdge> edges) {
        if (currentDepth > maxDepth || visited.contains(currentId)) {
            return;
        }

        visited.add(currentId);

        List<Lineage> lineages = lineageRepository.findBySourceTableId(currentId);
        for (Lineage lineage : lineages) {
            String targetId = lineage.getTargetTable().getId();
            
            // 添加边
            edges.add(LineageEdge.builder()
                .source(currentId)
                .target(targetId)
                .type(lineage.getLineageType().name())
                .build());

            // 添加节点（如果不存在）
            if (!nodes.containsKey(targetId)) {
                TableMetadata targetTable = lineage.getTargetTable();
                nodes.put(targetId, LineageNode.builder()
                    .id(targetId)
                    .name(targetTable.getDatabaseName() + "." + targetTable.getTableName())
                    .depth(currentDepth)
                    .type("downstream")
                    .build());

                // 递归遍历
                dfsDownstream(targetId, currentDepth + 1, maxDepth, visited, nodes, edges);
            }
        }
    }

    /**
     * 检测循环依赖
     */
    public List<String> detectCircularDependency(String tableId) {
        log.debug("Detecting circular dependency for table: {}", tableId);
        return dfsCycle(tableId, new ArrayList<>(), new HashSet<>())
            .orElse(Collections.emptyList());
    }

    /**
     * DFS 检测循环
     */
    private Optional<List<String>> dfsCycle(String currentId, List<String> path, Set<String> visited) {
        if (path.contains(currentId)) {
            // 找到循环，返回循环路径
            int cycleStart = path.indexOf(currentId);
            List<String> cycle = new ArrayList<>(path.subList(cycleStart, path.size()));
            cycle.add(currentId);
            return Optional.of(cycle);
        }

        if (visited.contains(currentId)) {
            return Optional.empty();
        }

        visited.add(currentId);
        List<String> newPath = new ArrayList<>(path);
        newPath.add(currentId);

        // 查询下游表
        List<Lineage> lineages = lineageRepository.findBySourceTableId(currentId);
        for (Lineage lineage : lineages) {
            Optional<List<String>> cycle = dfsCycle(
                lineage.getTargetTable().getId(),
                newPath,
                visited
            );
            if (cycle.isPresent()) {
                return cycle;
            }
        }

        return Optional.empty();
    }

    /**
     * 影响分析
     */
    public ImpactReport analyzeImpact(String tableId) {
        log.info("Analyzing impact for table: {}", tableId);

        TableMetadata table = tableRepository.findById(tableId)
            .orElseThrow(() -> new ResourceNotFoundException("表不存在: " + tableId));

        // 获取所有下游表（递归）
        Set<String> affectedTables = new HashSet<>();
        collectDownstreamTables(tableId, affectedTables, 5); // 最多 5 层

        List<TableMetadata> affectedTableList = affectedTables.stream()
            .map(id -> tableRepository.findById(id).orElse(null))
            .filter(Objects::nonNull)
            .toList();

        ImpactReport report = ImpactReport.builder()
            .tableId(tableId)
            .tableName(table.getDatabaseName() + "." + table.getTableName())
            .totalCount(affectedTables.size())
            .affectedTableNames(affectedTableList.stream()
                .map(t -> t.getDatabaseName() + "." + t.getTableName())
                .toList())
            .affectedTableIds(new ArrayList<>(affectedTables))
            .build();

        log.info("Impact analysis completed: affected tables={}", affectedTables.size());
        return report;
    }

    /**
     * 递归收集下游表
     */
    private void collectDownstreamTables(String tableId, Set<String> result, int maxDepth) {
        if (maxDepth <= 0) {
            return;
        }

        List<Lineage> lineages = lineageRepository.findBySourceTableId(tableId);
        for (Lineage lineage : lineages) {
            String targetId = lineage.getTargetTable().getId();
            if (!result.contains(targetId)) {
                result.add(targetId);
                collectDownstreamTables(targetId, result, maxDepth - 1);
            }
        }
    }

    /**
     * 获取两表之间的血缘路径
     */
    public List<List<String>> getLineagePath(String sourceId, String targetId) {
        log.info("Finding lineage path: source={}, target={}", sourceId, targetId);

        List<List<String>> allPaths = new ArrayList<>();
        List<String> currentPath = new ArrayList<>();
        currentPath.add(sourceId);

        findPaths(sourceId, targetId, currentPath, new HashSet<>(), allPaths, 5);

        log.info("Found {} paths between tables", allPaths.size());
        return allPaths;
    }

    /**
     * DFS 查找所有路径
     */
    private void findPaths(String currentId, String targetId, List<String> currentPath,
                          Set<String> visited, List<List<String>> allPaths, int maxDepth) {
        if (currentPath.size() > maxDepth) {
            return;
        }

        if (currentId.equals(targetId)) {
            allPaths.add(new ArrayList<>(currentPath));
            return;
        }

        visited.add(currentId);

        List<Lineage> lineages = lineageRepository.findBySourceTableId(currentId);
        for (Lineage lineage : lineages) {
            String nextId = lineage.getTargetTable().getId();
            if (!visited.contains(nextId)) {
                currentPath.add(nextId);
                findPaths(nextId, targetId, currentPath, visited, allPaths, maxDepth);
                currentPath.remove(currentPath.size() - 1);
            }
        }

        visited.remove(currentId);
    }
}
