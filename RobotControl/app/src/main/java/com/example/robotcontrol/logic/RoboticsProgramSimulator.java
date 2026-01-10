package com.example.robotcontrol.logic;

import androidx.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;

public final class RoboticsProgramSimulator {

    private RoboticsProgramSimulator() {
    }

    public enum Direction {
        NORTH, EAST, SOUTH, WEST
    }

    public enum Move {
        LEFT, FORWARD, RIGHT
    }

    public static final class State {
        public int row;
        public int col;
        @NonNull
        public Direction direction;

        public State(int row, int col, @NonNull Direction direction) {
            this.row = row;
            this.col = col;
            this.direction = direction;
        }
    }

    public static final class Result {
        public final boolean crashed;
        public final boolean reachedGoal;
        public final int finalRow;
        public final int finalCol;
        @NonNull
        public final Direction finalDirection;

        public Result(boolean crashed, boolean reachedGoal, int finalRow, int finalCol, @NonNull Direction finalDirection) {
            this.crashed = crashed;
            this.reachedGoal = reachedGoal;
            this.finalRow = finalRow;
            this.finalCol = finalCol;
            this.finalDirection = finalDirection;
        }
    }

    public static Result simulate(
            int gridSize,
            int startRow,
            int startCol,
            int goalRow,
            int goalCol,
            @NonNull int[] obstacleIndices,
            @NonNull Move[] program
    ) {
        Set<Integer> obstacles = new HashSet<>();
        for (int idx : obstacleIndices) {
            obstacles.add(idx);
        }

        State s = new State(startRow, startCol, Direction.EAST);

        for (Move move : program) {
            if (s.row == goalRow && s.col == goalCol) {
                return new Result(false, true, s.row, s.col, s.direction);
            }

            boolean ok;
            if (move == Move.LEFT) {
                s.direction = turnLeft(s.direction);
                ok = true;
            } else if (move == Move.RIGHT) {
                s.direction = turnRight(s.direction);
                ok = true;
            } else {
                ok = moveForward(gridSize, obstacles, s);
            }

            if (!ok) {
                return new Result(true, false, s.row, s.col, s.direction);
            }
        }

        boolean reached = (s.row == goalRow && s.col == goalCol);
        return new Result(false, reached, s.row, s.col, s.direction);
    }

    private static boolean moveForward(int gridSize, @NonNull Set<Integer> obstacles, @NonNull State s) {
        int nextRow = s.row;
        int nextCol = s.col;
        switch (s.direction) {
            case NORTH:
                nextRow--;
                break;
            case SOUTH:
                nextRow++;
                break;
            case WEST:
                nextCol--;
                break;
            case EAST:
            default:
                nextCol++;
                break;
        }

        if (nextRow < 0 || nextRow >= gridSize || nextCol < 0 || nextCol >= gridSize) {
            return false;
        }

        int idx = nextRow * gridSize + nextCol;
        if (obstacles.contains(idx)) {
            return false;
        }

        s.row = nextRow;
        s.col = nextCol;
        return true;
    }

    private static Direction turnLeft(@NonNull Direction d) {
        switch (d) {
            case NORTH:
                return Direction.WEST;
            case WEST:
                return Direction.SOUTH;
            case SOUTH:
                return Direction.EAST;
            case EAST:
            default:
                return Direction.NORTH;
        }
    }

    private static Direction turnRight(@NonNull Direction d) {
        switch (d) {
            case NORTH:
                return Direction.EAST;
            case EAST:
                return Direction.SOUTH;
            case SOUTH:
                return Direction.WEST;
            case WEST:
            default:
                return Direction.NORTH;
        }
    }
}
