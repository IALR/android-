package com.example.robotcontrol.logic;

import androidx.annotation.NonNull;

public final class CircuitValidation {

    private CircuitValidation() {
    }

    public enum Part {
        NONE,
        BATTERY,
        SWITCH,
        RESISTOR,
        LED
    }

    /**
     * Validates a simple series circuit.
     *
     * Rules:
     * - exact order: Battery -> Switch -> Resistor -> LED
     * - the placed switch must be closed
     * - the placed LED must be forward polarity
     */
    public static boolean isValidAndOn(
            @NonNull Part[] slotParts,
            @NonNull boolean[] switchClosedBySlot,
            @NonNull boolean[] ledForwardBySlot
    ) {
        if (slotParts.length < 4 || switchClosedBySlot.length < 4 || ledForwardBySlot.length < 4) {
            return false;
        }

        if (slotParts[0] != Part.BATTERY) return false;
        if (slotParts[1] != Part.SWITCH) return false;
        if (slotParts[2] != Part.RESISTOR) return false;
        if (slotParts[3] != Part.LED) return false;

        if (!switchClosedBySlot[1]) return false;
        if (!ledForwardBySlot[3]) return false;

        return true;
    }
}
