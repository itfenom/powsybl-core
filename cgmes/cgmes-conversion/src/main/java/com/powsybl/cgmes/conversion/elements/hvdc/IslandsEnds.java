/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements.hvdc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class IslandsEnds {

    Set<IslandEnd> islandsEndsNodes;

    // The island includes dcTopologicalNodes and first acTopologicalNode
    IslandsEnds() {
        islandsEndsNodes = new HashSet<>();
    }

    void add(Adjacency adjacency, List<String> islandNodes) {
        if (islandNodes.isEmpty()) {
            return;
        }
        System.err.printf("IslandNodes ---> %s %n", islandNodes.toString());
        // energinet
        //if (!islandNodes.contains("_b7750093-2c47-4202-b3a2-2080d76a1b6c")) {
        //if (!islandNodes.contains("_e4228fdf-3944-4ce1-a784-8d4506cd1ba5")) {
        //if (!islandNodes.contains("_d255c52a-32e0-4d44-8efc-dd953bb677d6")) {
        if (!islandNodes.contains("_48950a5a-1366-4171-ba80-a1ab3d341e86")) {
        // eirgridsoni
        //if (!islandNodes.contains("_b5b275aa-86c2-465e-8b69-a58c98e8c2ea")) {
        //if (!islandNodes.contains("_652873ea-33cd-4397-82be-c3cc12f4ee45")) {
        //if (!islandNodes.contains("_5f39589f-6d45-4afe-a9cb-b9849e3af100")) {
            return;
        }
        Set<String> visitedTopologicalNodes = new HashSet<>();

        String topologicalNodeEnd1 = islandNodes.get(0);
        List<String> adjacentTopologicalNodeEnd1 = computeAdjacentTopologicalNodes(topologicalNodeEnd1,
            adjacency, visitedTopologicalNodes);
        String commonTopologicalNodeEnd1 = computeCommontopologicalNode(adjacency, adjacentTopologicalNodeEnd1);

        String topologicalNodeEnd2 = getTopologicalNodeOtherEnd(islandNodes, visitedTopologicalNodes);
        if (topologicalNodeEnd2 == null) {
            return;
        }
        List<String> adjacentTopologicalNodeEnd2 = computeAdjacentTopologicalNodes(topologicalNodeEnd2,
            adjacency, visitedTopologicalNodes);
        String commonTopologicalNodeEnd2 = computeCommontopologicalNode(adjacency, adjacentTopologicalNodeEnd2);

        IslandEnd islandEnd = new IslandEnd(commonTopologicalNodeEnd1, adjacentTopologicalNodeEnd1,
            commonTopologicalNodeEnd2,
            adjacentTopologicalNodeEnd2);

        islandsEndsNodes.add(islandEnd);
    }

    private String getTopologicalNodeOtherEnd(List<String> islandNodes, Set<String> visitedTopologicalNodes) {
        return islandNodes.stream()
            .filter(n -> !visitedTopologicalNodes.contains(n))
            .findFirst()
            .orElse(null);
    }

    private List<String> computeAdjacentTopologicalNodes(String topologicalNodeId,
        Adjacency adjacency, Set<String> visitedTopologicalNodes) {

        List<String> adjacentTopologicalNodes = new ArrayList<>();
        adjacentTopologicalNodes.add(topologicalNodeId);
        visitedTopologicalNodes.add(topologicalNodeId);

        int k = 0;
        while (k < adjacentTopologicalNodes.size()) {
            String topologicalNode = adjacentTopologicalNodes.get(k);
            if (adjacency.adjacency.containsKey(topologicalNode)) {
                adjacency.adjacency.get(topologicalNode).forEach(adjacent -> {
                    if (Adjacency.isDcLineSegment(adjacent.type)) {
                        return;
                    }
                    if (visitedTopologicalNodes.contains(adjacent.topologicalNode)) {
                        return;
                    }
                    adjacentTopologicalNodes.add(adjacent.topologicalNode);
                    visitedTopologicalNodes.add(adjacent.topologicalNode);
                });
            }
            k++;
        }
        return adjacentTopologicalNodes;
    }

    private String computeCommontopologicalNode(Adjacency adjacency, List<String> topologicalNodeEnd) {
        Optional<String> n = topologicalNodeEnd.stream()
            .max((n1, n2) -> Integer.compare(computeAdjacents(adjacency, n1), computeAdjacents(adjacency, n2)));

        if (n.isPresent()) {
            return n.get();
        }
        return null;
    }

    private int computeAdjacents(Adjacency adjacency, String topologicalNode) {
        return (int) adjacency.adjacency.get(topologicalNode).stream()
            .filter(adj -> Adjacency.isAcDcConverter(adj.type))
            .count();
    }

    void print() {
        LOG.info("IslandsEnds");
        islandsEndsNodes.forEach(islandEnd -> islandEnd.print());
    }

    static class IslandEnd {
        String commonTopologicalNode1;
        List<String> topologicalNodes1;

        String commonTopologicalNode2;
        List<String> topologicalNodes2;

        IslandEnd(String commonTopologicalNode1, List<String> topologicalNodes1, String commonTopologicalNode2,
            List<String> topologicalNodes2) {
            this.commonTopologicalNode1 = commonTopologicalNode1;
            this.topologicalNodes1 = topologicalNodes1;
            this.commonTopologicalNode2 = commonTopologicalNode2;
            this.topologicalNodes2 = topologicalNodes2;
        }

        void print() {
            LOG.info("    commonTopologicalNode1: {}", this.commonTopologicalNode1);
            LOG.info("    topologicalNodes1: {}", this.topologicalNodes1);
            LOG.info("    commonTopologicalNode2: {}", this.commonTopologicalNode2);
            LOG.info("    topologicalNodes2: {}", this.topologicalNodes2);
            LOG.info("---");
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(IslandsEnds.class);
}
