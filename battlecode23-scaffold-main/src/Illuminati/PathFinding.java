package Illuminati;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
* Pathfinding
* This is a rudimentary A-star algorithm to help our bots navigate the abyss
*/
public class PathFinding {

    private static final int DIAGONAL_COST = 14;
    private static final int V_H_COST = 10;

    private int[][] grid;
    private List<Node> openList;
    private List<Node> closedList;
    private Node[][] nodes;
    private int gridRows;
    private int gridColumns;

    public PathFinding(int[][] grid) {
        this.grid = grid;
        this.gridRows = grid.length;
        this.gridColumns = grid[0].length;
        this.openList = new ArrayList<>();
        this.closedList = new ArrayList<>();
        this.nodes = new Node[gridRows][gridColumns];

        for (int i = 0; i < gridRows; i++) {
            for (int j = 0; j < gridColumns; j++) {
                nodes[i][j] = new Node(i, j);
            }
        }
    }

    public List<Node> findPath(int startX, int startY, int endX, int endY) {
        nodes[startX][startY].setCost(0);

        openList.add(nodes[startX][startY]);

        while (!openList.isEmpty()) {
            Node current = getLowestCostNode(openList);
            if (current.getX() == endX && current.getY() == endY) {
                return getPath(nodes[endX][endY]);
            }

            openList.remove(current);
            closedList.add(current);

            List<Node> neighbors = getNeighbors(current);
            for (Node neighbor : neighbors) {
                if (closedList.contains(neighbor)) {
                    continue;
                }

                int cost = current.getCost() + getCost(current, neighbor);
                if (cost < neighbor.getCost()) {
                    neighbor.setCost(cost);
                    neighbor.setParent(current);
                    if (!openList.contains(neighbor)) {
                        openList.add(neighbor);
                    }
                }
            }
        }

        return Collections.emptyList();
    }

    private List<Node> getNeighbors(Node node) {
        List<Node> neighbors = new ArrayList<>();

        int x = node.getX();
        int y = node.getY();

        // Check top
        if (x > 0) {
            if (grid[x - 1][y] <= 5) {
                neighbors.add(nodes[x - 1][y]);
            }
        }

        // Check bottom
        if (x < gridRows - 1) {
            if (grid[x + 1][y] <= 5) {
                neighbors.add(nodes[x + 1][y]);
            }
        }

        // Check left
        if (y > 0) {
            if (grid[x][y - 1] <= 5) {
                neighbors.add(nodes[x][y - 1]);
            }
        }

        // Check right
        if (y < gridColumns - 1) {
            if (grid[x][y + 1] <= 5) {
                neighbors.add(nodes[x][y + 1]);
            }
        }

        // Check top-left
        if (x > 0 && y > 0) {
            if (grid[x - 1][y - 1] <= 5) {
                neighbors.add(nodes[x - 1][y - 1]);
            }
        }

        // Check top-right
        if (x > 0 && y < gridColumns - 1) {
            if (grid[x - 1][y + 1] <= 5) {
                neighbors.add(nodes[x - 1][y + 1]);
            }
        }

        // Check bottom-left
        if (x < gridRows - 1 && y > 0) {
            if (grid[x + 1][y - 1] <= 5) {
                neighbors.add(nodes[x + 1][y - 1]);
            }
        }

        // Check bottom-right
        if (x < gridRows - 1 && y < gridColumns - 1) {
            if (grid[x + 1][y + 1] <= 5) {
                neighbors.add(nodes[x + 1][y + 1]);
            }
        }

        return neighbors;
    }

    private int getCost(Node current, Node neighbor) {
        int cost = V_H_COST;

        // Check if movement is diagonal
        if (current.getX() != neighbor.getX() && current.getY() != neighbor.getY()) {
            cost = DIAGONAL_COST;
        }

        return cost;
    }

    private List<Node> getPath(Node target) {
        List<Node> path = new ArrayList<>();

        path.add(target);
        Node parent = target.getParent();
        while (parent != null) {
            path.add(0, parent);
            parent = parent.getParent();
        }

        return path;
    }

    private Node getLowestCostNode(List<Node> list) {
        Node lowestCostNode = null;
        int lowestCost = Integer.MAX_VALUE;
        for (Node node : list) {
            int cost = node.getCost() + node.getHeuristicCost();
            if (cost < lowestCost) {
                lowestCost = cost;
                lowestCostNode = node;
            }
        }
        return lowestCostNode;
    }

    private static class Node {
        private int x;
        private int y;
        private int cost;
        private int heuristicCost;
        private Node parent;

        public Node(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getCost() {
            return cost;
        }

        public void setCost(int cost) {
            this.cost = cost;
        }

        public int getHeuristicCost() {
            return heuristicCost;
        }

        public void setHeuristicCost(int heuristicCost) {
            this.heuristicCost = heuristicCost;
        }

        public Node getParent() {
            return parent;
        }

        public void setParent(Node parent) {
            this.parent = parent;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "x=" + x +
                    ", y=" + y +
                    ", cost=" + cost +
                    ", heuristicCost=" + heuristicCost +
                    '}';
        }
    }
}

/*
    int[][] grid = {
            {0, 0, 0, 0, 0, 0},
            {0, 5, 5, 5, 5, 0},
            {0, 5, 0, 0, 5, 0},
            {0, 5, 0, 0, 5, 0},
            {0, 5, 5, 5, 5, 0},
            {0, 0, 0, 0, 0, 0}
    };

    AStarPathfinder pathfinder = new AStarPathfinder(grid);
    List<Node> path = pathfinder.findPath(0, 0, 5, 5);

for (Node node : path) {
        System.out.println(node);
        }
*/

