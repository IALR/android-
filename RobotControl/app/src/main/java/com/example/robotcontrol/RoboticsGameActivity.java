package com.example.robotcontrol;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.robotcontrol.adapters.RoboticsGridAdapter;
import com.example.robotcontrol.utils.EducationProgressStore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Simple robotics mini-game: a grid path planner.
 * Build a short program (Left/Forward/Right) to move the robot from S to G.
 */
public class RoboticsGameActivity extends AppCompatActivity {

    private static final int GRID_SIZE = 4;
    private static final int MAX_PROGRAM_STEPS = 18;
    private static final int STEP_DELAY_MS = 250;

    private RecyclerView rvGrid;
    private TextView tvProgram;
    private Button btnLeft;
    private Button btnForward;
    private Button btnRight;
    private Button btnRun;
    private Button btnReset;

    private final List<Character> cells = new ArrayList<>();
    private RoboticsGridAdapter adapter;

    private final List<Move> program = new ArrayList<>();

    private int startRow = 0;
    private int startCol = 0;
    private int goalRow = GRID_SIZE - 1;
    private int goalCol = GRID_SIZE - 1;

    private int robotRow = startRow;
    private int robotCol = startCol;

    private Direction direction = Direction.EAST;

    private final Set<Integer> obstacles = new HashSet<>();

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isRunning = false;

    private enum Direction {
        NORTH, EAST, SOUTH, WEST
    }

    private enum Move {
        LEFT, FORWARD, RIGHT
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppSettings.applyColorTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robotics_game);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.robotics_game_title));
        }

        rvGrid = findViewById(R.id.rvGrid);
        tvProgram = findViewById(R.id.tvProgram);
        btnLeft = findViewById(R.id.btnLeft);
        btnForward = findViewById(R.id.btnForward);
        btnRight = findViewById(R.id.btnRight);
        btnRun = findViewById(R.id.btnRun);
        btnReset = findViewById(R.id.btnReset);

        rvGrid.setLayoutManager(new GridLayoutManager(this, GRID_SIZE));
        initObstacles();
        initCells();

        adapter = new RoboticsGridAdapter(cells);
        rvGrid.setAdapter(adapter);

        updateProgramText();

        btnLeft.setOnClickListener(v -> addMove(Move.LEFT));
        btnForward.setOnClickListener(v -> addMove(Move.FORWARD));
        btnRight.setOnClickListener(v -> addMove(Move.RIGHT));
        btnReset.setOnClickListener(v -> resetGame());
        btnRun.setOnClickListener(v -> runProgram());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    private void initObstacles() {
        // Easy mode: no obstacles.
        obstacles.clear();
    }

    private void addObstacle(int row, int col) {
        obstacles.add(index(row, col));
    }

    private void initCells() {
        cells.clear();
        for (int i = 0; i < GRID_SIZE * GRID_SIZE; i++) {
            cells.add(RoboticsGridAdapter.CELL_EMPTY);
        }
        robotRow = startRow;
        robotCol = startCol;
        direction = Direction.EAST;
        renderGrid();
    }

    private void renderGrid() {
        for (int i = 0; i < GRID_SIZE * GRID_SIZE; i++) {
            cells.set(i, RoboticsGridAdapter.CELL_EMPTY);
        }

        // Obstacles
        for (Integer idx : obstacles) {
            if (idx >= 0 && idx < cells.size()) {
                cells.set(idx, RoboticsGridAdapter.CELL_OBSTACLE);
            }
        }

        // Start and goal
        cells.set(index(startRow, startCol), RoboticsGridAdapter.CELL_START);
        cells.set(index(goalRow, goalCol), RoboticsGridAdapter.CELL_GOAL);

        // Robot (overrides start symbol while on it)
        cells.set(index(robotRow, robotCol), RoboticsGridAdapter.CELL_ROBOT);

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void addMove(@NonNull Move move) {
        if (isRunning) return;
        if (program.size() >= MAX_PROGRAM_STEPS) {
            Toast.makeText(this, getString(R.string.robotics_game_too_long, MAX_PROGRAM_STEPS), Toast.LENGTH_SHORT).show();
            return;
        }
        program.add(move);
        updateProgramText();
    }

    private void updateProgramText() {
        if (program.isEmpty()) {
            tvProgram.setText(getString(R.string.robotics_game_program_empty));
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.robotics_game_program_prefix)).append(" ");
        for (int i = 0; i < program.size(); i++) {
            Move m = program.get(i);
            if (m == Move.LEFT) sb.append("L");
            else if (m == Move.RIGHT) sb.append("R");
            else sb.append("F");
            if (i < program.size() - 1) sb.append(" ");
        }
        tvProgram.setText(sb.toString());
    }

    private void setControlsEnabled(boolean enabled) {
        btnLeft.setEnabled(enabled);
        btnForward.setEnabled(enabled);
        btnRight.setEnabled(enabled);
        btnRun.setEnabled(enabled);
        btnReset.setEnabled(enabled);
    }

    private void resetGame() {
        if (isRunning) {
            handler.removeCallbacksAndMessages(null);
            isRunning = false;
        }
        program.clear();
        initCells();
        updateProgramText();
        setControlsEnabled(true);
    }

    private void runProgram() {
        if (isRunning) return;

        if (program.isEmpty()) {
            Toast.makeText(this, getString(R.string.robotics_game_add_moves), Toast.LENGTH_SHORT).show();
            return;
        }

        isRunning = true;
        setControlsEnabled(false);

        executeStep(0);
    }

    private void executeStep(int stepIndex) {
        if (!isRunning) return;

        if (isAtGoal()) {
            finishSuccess();
            return;
        }

        if (stepIndex >= program.size()) {
            isRunning = false;
            setControlsEnabled(true);
            Toast.makeText(this, getString(R.string.robotics_game_finished_not_goal), Toast.LENGTH_SHORT).show();
            return;
        }

        Move move = program.get(stepIndex);
        boolean ok;

        if (move == Move.LEFT) {
            direction = turnLeft(direction);
            ok = true;
        } else if (move == Move.RIGHT) {
            direction = turnRight(direction);
            ok = true;
        } else {
            ok = moveForward();
        }

        renderGrid();

        if (!ok) {
            isRunning = false;
            setControlsEnabled(true);
            Toast.makeText(this, getString(R.string.robotics_game_crash), Toast.LENGTH_SHORT).show();
            return;
        }

        handler.postDelayed(() -> executeStep(stepIndex + 1), STEP_DELAY_MS);
    }

    private void finishSuccess() {
        isRunning = false;
        setControlsEnabled(true);

        EducationProgressStore.markRoboticsGameCompleted(this);

        if (rvGrid != null) {
            rvGrid.animate()
                    .scaleX(1.03f)
                    .scaleY(1.03f)
                    .setDuration(140)
                    .withEndAction(() -> rvGrid.animate().scaleX(1f).scaleY(1f).setDuration(140).start())
                    .start();
        }
        Toast.makeText(this, getString(R.string.robotics_game_success), Toast.LENGTH_LONG).show();
    }

    private boolean isAtGoal() {
        return robotRow == goalRow && robotCol == goalCol;
    }

    private boolean moveForward() {
        int nextRow = robotRow;
        int nextCol = robotCol;
        switch (direction) {
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

        if (nextRow < 0 || nextRow >= GRID_SIZE || nextCol < 0 || nextCol >= GRID_SIZE) {
            return false;
        }

        int idx = index(nextRow, nextCol);
        if (obstacles.contains(idx)) {
            return false;
        }

        robotRow = nextRow;
        robotCol = nextCol;
        return true;
    }

    private Direction turnLeft(Direction d) {
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

    private Direction turnRight(Direction d) {
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

    private int index(int row, int col) {
        return row * GRID_SIZE + col;
    }
}
