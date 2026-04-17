package com.karifovas.gamerating.utils;

import java.util.*;
import java.util.function.Function;

public class TopologicalSortUtil {

    public static <T, K> List<T> sort(
            Collection<T> nodes,
            Function<T, K> nodeIdExtractor,
            Function<T, ? extends Iterable<K>> nodeDependenciesExtractor) {

        if (nodes == null || nodes.isEmpty()) {
            return List.of();
        }

        // Build a map of ID to node for quick lookups, validating uniqueness
        Map<K, T> nodeMap = new HashMap<>();
        for (T node : nodes) {
            K id = nodeIdExtractor.apply(node);
            if (nodeMap.containsKey(id)) {
                throw new IllegalArgumentException("Duplicate node ID detected: " + id);
            }
            nodeMap.put(id, node);
        }

        // Build adjacency list -
        Map<K, List<K>> adjacencyList = new HashMap<>();
        // How many unchecked nodes point to a node
        Map<K, Integer> nodeInwardDegree = new HashMap<>();

        // Initialize all nodes
        for (T node : nodes) {
            K id = nodeIdExtractor.apply(node);
            adjacencyList.putIfAbsent(id, new ArrayList<>());
            nodeInwardDegree.putIfAbsent(id, 0)
            ;
        }

        // Build the graph
        for (T node : nodes) {
            K id = nodeIdExtractor.apply(node);
            for (K depId : nodeDependenciesExtractor.apply(node)) {
                if (nodeMap.containsKey(depId)) {
                    adjacencyList.get(depId).add(id);
                    nodeInwardDegree.put(id, nodeInwardDegree.get(id) + 1);
                }
            }
        }

        // Kahn's algorithm
        Queue<K> queue = new LinkedList<>();
        for (K id : nodeInwardDegree.keySet()) {
            if (nodeInwardDegree.get(id) == 0) {
                queue.offer(id);
            }
        }

        List<T> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            K id = queue.poll();
            result.add(nodeMap.get(id));

            for (K neighbor : adjacencyList.get(id)) {
                nodeInwardDegree.put(neighbor, nodeInwardDegree.get(neighbor) - 1);
                if (nodeInwardDegree.get(neighbor) == 0) {
                    queue.offer(neighbor);
                }
            }
        }

        if (result.size() != nodes.size()) {
            throw new IllegalArgumentException("Cyclic dependency detected in the graph");
        }

        return result;
    }

    public static <T, K> Set<T> findDependents(
            T root,
            Collection<T> nodes,
            Function<T, K> identifierFunction,
            Function<T, ? extends Collection<T>> getDirectDependentsFn) {

        if (nodes == null || nodes.isEmpty()) {
            return Set.of();
        }

        Set<K> visited = new LinkedHashSet<>();
        Queue<T> queue = new ArrayDeque<>();
        Set<T> affected = new HashSet<>();

        queue.add(root);


        while (!queue.isEmpty()) {
            T current = queue.poll();
            var code = identifierFunction.apply(current);
            if (!visited.add(code)) {
                continue;
            }

            var dependents = getDirectDependentsFn.apply(current);

            affected.add(current);
            queue.addAll(dependents);
        }

        return affected;
    }
}
