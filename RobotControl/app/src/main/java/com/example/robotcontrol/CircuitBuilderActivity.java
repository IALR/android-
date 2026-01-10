package com.example.robotcontrol;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ClipData;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.HashSet;
import java.util.Set;

/**
 * Very simple Circuit Builder mini-game.
 * Drag Battery / Switch / Resistor / LED into 4 series slots.
 * The LED turns ON only if the circuit is correct.
 */
public class CircuitBuilderActivity extends AppCompatActivity {

    private enum Part {
        NONE,
        BATTERY,
        SWITCH,
        RESISTOR,
        LED
    }

    private static class SlotState {
        Part part = Part.NONE;
        boolean switchClosed = true; // only relevant if part == SWITCH
        boolean ledForward = true;   // only relevant if part == LED
    }

    private FrameLayout[] slots;
    private final SlotState[] slotStates = new SlotState[]{new SlotState(), new SlotState(), new SlotState(), new SlotState()};

    private TextView tvLedStatus;
    private TextView tvHint;
    private ImageView viewLedIndicator;

    private ObjectAnimator ledPulseAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppSettings.applyColorTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circuit_builder);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.circuit_builder_title));
        }

        slots = new FrameLayout[]{
                findViewById(R.id.slot1),
                findViewById(R.id.slot2),
                findViewById(R.id.slot3),
                findViewById(R.id.slot4)
        };

        tvLedStatus = findViewById(R.id.tvLedStatus);
        tvHint = findViewById(R.id.tvHint);
        viewLedIndicator = findViewById(R.id.viewLedIndicator);

        CardView partBattery = findViewById(R.id.partBattery);
        CardView partSwitch = findViewById(R.id.partSwitch);
        CardView partResistor = findViewById(R.id.partResistor);
        CardView partLed = findViewById(R.id.partLed);
        Button btnClear = findViewById(R.id.btnClearCircuit);

        setupDragSource(partBattery, Part.BATTERY);
        setupDragSource(partSwitch, Part.SWITCH);
        setupDragSource(partResistor, Part.RESISTOR);
        setupDragSource(partLed, Part.LED);

        for (int i = 0; i < slots.length; i++) {
            int slotIndex = i;
            slots[i].setOnDragListener((v, event) -> handleDrop(slotIndex, event));
            slots[i].setOnClickListener(v -> handleSlotTap(slotIndex));
        }

        btnClear.setOnClickListener(v -> clearCircuit());

        renderSlots();
        updateLedState();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setupDragSource(@NonNull View view, @NonNull Part part) {
        view.setOnLongClickListener(v -> {
            ClipData data = ClipData.newPlainText("part", part.name());
            View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
            v.startDragAndDrop(data, shadow, null, 0);
            return true;
        });
    }

    private boolean handleDrop(int slotIndex, @NonNull DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                return event.getClipDescription() != null;

            case DragEvent.ACTION_DROP:
                if (event.getClipData() == null || event.getClipData().getItemCount() == 0) return false;
                String text = String.valueOf(event.getClipData().getItemAt(0).getText());
                Part part;
                try {
                    part = Part.valueOf(text);
                } catch (Exception ignored) {
                    return false;
                }

                slotStates[slotIndex].part = part;
                // reset toggles when placing
                if (part == Part.SWITCH) slotStates[slotIndex].switchClosed = true;
                if (part == Part.LED) slotStates[slotIndex].ledForward = true;

                renderSlots();
                updateLedState();

                popSlot(slots[slotIndex]);
                return true;

            default:
                return true;
        }
    }

    private void handleSlotTap(int slotIndex) {
        SlotState state = slotStates[slotIndex];
        if (state.part == Part.SWITCH) {
            state.switchClosed = !state.switchClosed;
            renderSlots();
            updateLedState();
        } else if (state.part == Part.LED) {
            state.ledForward = !state.ledForward;
            renderSlots();
            updateLedState();
        }
    }

    private void clearCircuit() {
        for (SlotState s : slotStates) {
            s.part = Part.NONE;
            s.switchClosed = true;
            s.ledForward = true;
        }
        renderSlots();
        updateLedState();
    }

    private void renderSlots() {
        for (int i = 0; i < slots.length; i++) {
            FrameLayout slot = slots[i];
            slot.removeAllViews();

            LinearLayout container = new LinearLayout(this);
            container.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));
            container.setOrientation(LinearLayout.VERTICAL);
            container.setGravity(android.view.Gravity.CENTER);

            SlotState state = slotStates[i];

            ImageView icon = new ImageView(this);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(22), dp(22));
            icon.setLayoutParams(iconParams);
            int iconRes = getPartIconRes(state);
            if (iconRes != 0) {
                icon.setImageResource(iconRes);
                icon.setColorFilter(getColor(R.color.accent_primary));
                icon.setAlpha(0.95f);
                container.addView(icon);
            }

            TextView tv = new TextView(this);
            tv.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            tv.setGravity(android.view.Gravity.CENTER);
            tv.setTextColor(getColor(R.color.text_primary));
            tv.setTextSize(13f);
            tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.BOLD);
            tv.setText(getSlotLabel(state));
            if (iconRes != 0) {
                ((LinearLayout.LayoutParams) tv.getLayoutParams()).topMargin = dp(6);
            }
            container.addView(tv);

            slot.addView(container);
        }
    }

    private int getPartIconRes(@NonNull SlotState state) {
        switch (state.part) {
            case BATTERY:
                return R.drawable.ic_part_battery;
            case SWITCH:
                return R.drawable.ic_part_switch;
            case RESISTOR:
                return R.drawable.ic_part_resistor;
            case LED:
                return R.drawable.ic_part_led;
            case NONE:
            default:
                return 0;
        }
    }

    private int dp(int dp) {
        return Math.round(getResources().getDisplayMetrics().density * dp);
    }

    private String getSlotLabel(@NonNull SlotState state) {
        switch (state.part) {
            case BATTERY:
                return getString(R.string.circuit_builder_part_battery_short);
            case SWITCH:
                return state.switchClosed
                        ? getString(R.string.circuit_builder_switch_closed)
                        : getString(R.string.circuit_builder_switch_open);
            case RESISTOR:
                return getString(R.string.circuit_builder_part_resistor_short);
            case LED:
                return state.ledForward
                        ? getString(R.string.circuit_builder_led_forward)
                        : getString(R.string.circuit_builder_led_backward);
            case NONE:
            default:
                return getString(R.string.circuit_builder_slot_empty);
        }
    }

    private void updateLedState() {
        boolean on = isCircuitValidAndOn();
        tvLedStatus.setText(on ? getString(R.string.circuit_builder_led_on) : getString(R.string.circuit_builder_led_off));
        tvHint.setText(on ? getString(R.string.circuit_builder_hint_on) : getString(R.string.circuit_builder_hint));

        viewLedIndicator.setColorFilter(getColor(on ? R.color.accent_primary : R.color.text_secondary));
        viewLedIndicator.setAlpha(on ? 1.0f : 0.55f);

        if (on) {
            startLedPulse();
        } else {
            stopLedPulse();
        }
    }

    private void startLedPulse() {
        if (ledPulseAnimator != null && ledPulseAnimator.isRunning()) return;
        ledPulseAnimator = ObjectAnimator.ofFloat(viewLedIndicator, View.ALPHA, 0.65f, 1.0f);
        ledPulseAnimator.setDuration(500);
        ledPulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        ledPulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        ledPulseAnimator.start();
    }

    private void stopLedPulse() {
        if (ledPulseAnimator != null) {
            ledPulseAnimator.cancel();
            ledPulseAnimator = null;
        }
        if (viewLedIndicator != null) {
            viewLedIndicator.setAlpha(0.55f);
        }
    }

    private void popSlot(@NonNull View slot) {
        slot.animate()
                .scaleX(1.03f)
                .scaleY(1.03f)
                .setDuration(110)
                .withEndAction(() -> slot.animate().scaleX(1f).scaleY(1f).setDuration(110).start())
                .start();
    }

    @Override
    protected void onDestroy() {
        stopLedPulse();
        super.onDestroy();
    }

    private boolean isCircuitValidAndOn() {
        // Require exact order: Battery -> Switch -> Resistor -> LED
        if (slotStates[0].part != Part.BATTERY) return false;
        if (slotStates[1].part != Part.SWITCH) return false;
        if (slotStates[2].part != Part.RESISTOR) return false;
        if (slotStates[3].part != Part.LED) return false;

        // Must not have duplicates elsewhere (already implied by exact order)
        // Switch must be closed
        if (!slotStates[1].switchClosed) return false;

        // LED polarity must be correct (forward)
        if (!slotStates[3].ledForward) return false;

        // Resistor present means LED is protected
        return true;
    }
}
