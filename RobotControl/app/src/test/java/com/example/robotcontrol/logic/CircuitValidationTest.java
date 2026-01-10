package com.example.robotcontrol.logic;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CircuitValidationTest {

    @Test
    public void isValidAndOn_requiresExactOrderAndToggles() {
        CircuitValidation.Part[] parts = new CircuitValidation.Part[]{
                CircuitValidation.Part.BATTERY,
                CircuitValidation.Part.SWITCH,
                CircuitValidation.Part.RESISTOR,
                CircuitValidation.Part.LED
        };

        boolean[] switchClosed = new boolean[]{false, true, false, false};
        boolean[] ledForward = new boolean[]{false, false, false, true};

        assertTrue(CircuitValidation.isValidAndOn(parts, switchClosed, ledForward));

        switchClosed[1] = false;
        assertFalse(CircuitValidation.isValidAndOn(parts, switchClosed, ledForward));

        switchClosed[1] = true;
        ledForward[3] = false;
        assertFalse(CircuitValidation.isValidAndOn(parts, switchClosed, ledForward));
    }
}
