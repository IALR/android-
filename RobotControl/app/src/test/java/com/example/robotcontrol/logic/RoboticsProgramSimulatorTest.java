package com.example.robotcontrol.logic;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RoboticsProgramSimulatorTest {

    @Test
    public void simulate_solutionReachesGoal() {
        int grid = 4;
        int[] obstacles = new int[]{};

        RoboticsProgramSimulator.Move[] program = new RoboticsProgramSimulator.Move[]{
                RoboticsProgramSimulator.Move.FORWARD,
                RoboticsProgramSimulator.Move.FORWARD,
                RoboticsProgramSimulator.Move.FORWARD,
                RoboticsProgramSimulator.Move.RIGHT,
                RoboticsProgramSimulator.Move.FORWARD,
                RoboticsProgramSimulator.Move.FORWARD,
                RoboticsProgramSimulator.Move.FORWARD
        };

        RoboticsProgramSimulator.Result r = RoboticsProgramSimulator.simulate(
                grid,
                0, 0,
                3, 3,
                obstacles,
                program
        );

        assertFalse(r.crashed);
        assertTrue(r.reachedGoal);
    }
}
