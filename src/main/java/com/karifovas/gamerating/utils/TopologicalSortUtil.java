package com.karifovas.gamerating.utils;

import java.util.*;
import java.util.function.Function;

public class TopologicalSortUtil {

    /**
     * Sorts nodes in topological order based on their dependencies.
     *
     * @param nodes the collection of nodes to sort
     * @param idFn  function to extract a unique ID from a node
     * @param depFn function to extract dependency IDs from a node
     * @param <T>   node type
     * @param <K>   ID type
     * @return a list of nodes in dependency order
     * @throws IllegalArgumentException if a cyclic dependency is detected
     */
    public static <T, K> List<T> sort(
            Collection<T> nodes,
            Function<T, K> idFn,
            Function<T, ? extends Iterable<K>> depFn) {

        if (nodes == null || nodes.isEmpty()) {
            return List.of();
        }

        // Build a map of ID to node for quick lookups
        Map<K, T> nodeMap = new HashMap<>();
        for (T node : nodes) {
            nodeMap.put(idFn.apply(node), node);
        }

        // Build adjacency list and in-degree count
        Map<K, List<K>> adjacencyList = new HashMap<>();
        Map<K, Integer> inDegree = new HashMap<>();

        // Initialize all nodes
        for (T node : nodes) {
            K id = idFn.apply(node);
            adjacencyList.putIfAbsent(id, new ArrayList<>());
            inDegree.putIfAbsent(id, 0);
        }

        // Build the graph
        for (T node : nodes) {
            K id = idFn.apply(node);
            for (K depId : depFn.apply(node)) {
                if (nodeMap.containsKey(depId)) {
                    adjacencyList.get(depId).add(id);
                    inDegree.put(id, inDegree.get(id) + 1);
                }
            }
        }

        // Kahn's algorithm
        Queue<K> queue = new LinkedList<>();
        for (K id : inDegree.keySet()) {
            if (inDegree.get(id) == 0) {
                queue.offer(id);
            }
        }

        List<T> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            K id = queue.poll();
            result.add(nodeMap.get(id));

            for (K neighbor : adjacencyList.get(id)) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.offer(neighbor);
                }
            }
        }

        if (result.size() != nodes.size()) {
            throw new IllegalArgumentException("Cyclic dependency detected in the graph");
        }

        return result;
    }

    /**
     * Finds all nodes that transitively depend on the given root node.
     * Uses BFS to traverse the dependency graph and collect all dependents.
     *
     * @param rootId The ID of the root node to find dependents for
     * @param nodes  The collection of all nodes
     * @param idFn   Function to extract ID from a node
     * @param depFn  Function to extract dependencies from a node
     * @return A set of all node IDs that transitively depend on the root node (including the root itself)
     */
    public static <T, K> Set<K> findDependents(
            K rootId,
            Collection<T> nodes,
            Function<T, K> idFn,
            Function<T, ? extends Iterable<K>> depFn) {

        if (nodes == null || nodes.isEmpty()) {
            return Set.of();
        }

        // Build reverse dependency map: for each dependency, track which nodes depend on it
        Map<K, List<K>> dependentsByCode = new HashMap<>();
        for (T node : nodes) {
            K nodeId = idFn.apply(node);
            for (K depId : depFn.apply(node)) {
                dependentsByCode.computeIfAbsent(depId, ignored -> new ArrayList<>())
                        .add(nodeId);
            }
        }

        Set<K> visited = new LinkedHashSet<>();
        Queue<K> queue = new ArrayDeque<>();
        queue.add(rootId);

        while (!queue.isEmpty()) {
            K current = queue.poll();
            if (!visited.add(current)) {
                continue;
            }
            queue.addAll(dependentsByCode.getOrDefault(current, List.of()));
        }

        return visited;
    }
}
