# RoboConnect - Centralized Robotic Fleet Management

## 🤖 Project Overview

**RoboConnect** is a professional Android application for managing multiple robots through Bluetooth and WiFi connections. The app features a **modern robotics-themed UI/UX** with vibrant gradients, color-coded controls, and intuitive Material Design components.

### ✨ Key Highlights
- 🎨 **Robotics Theme**: Blue-to-purple gradients with tech-inspired aesthetics
- 🎯 **Color-Coded Controls**: Intuitive operation with visual feedback
- 📱 **Material Design**: Elevated cards, smooth animations, modern components
- 🚀 **Full Feature Set**: All requirements implemented
- 🔐 **Secure**: Local SQLite database with SharedPreferences authentication
- 📚 **Education Hub**: Learn topics, quizzes with results/review, mini-games (Path Planner & Circuit Builder)

## 🎨 Visual Design

The app features a cohesive robotics theme across all screens:
- **Gradient Backgrounds**: Blue (#1E88E5) → Purple (#7C4DFF) for immersive experience
- **Elevated Cards**: White cards floating on gradients with 4-8dp elevation
- **Custom Icons**: Robot illustrations, circuit patterns, status indicators
- **Color-Coded Buttons**: Green (forward), Yellow (backward), Cyan (turn), Red (stop)
- **Typography System**: 7-level hierarchy from Display (32sp) to Caption (12sp)
- **Spacing System**: Consistent 4dp-based spacing throughout
- **Micro-animations**: LED pulse effects, slot pop animations, card press feedback

## ✅ Implemented Features

### Core Robot Management
- **FR1: Login & Authentication** - Local authentication with SQLite, email/password login, session persistence
- **FR2: Pairing & Discovery** - Bluetooth device scanning with real-time discovery
- **FR3: Robot Add/Register** - Register discovered robots with local SQLite storage
- **FR4: Robot List & Status** - RecyclerView with cards, live status indicators, pull-to-refresh
- **FR5: Direct Robot Control** - Movement controls, speed slider, servo controls, real-time transmission
- **FR6: Robot Deletion** - Administrator-only deletion with confirmation
- **FR7: Permission Management** - Grant/revoke access by email, local storage
- **FR8: Auto-Reconnect** - ConnectionManager with configurable retry logic (max 3 attempts, 5s delay)
- **FR9: Pairing Mode** - Physical button triggers discoverable mode
- **FR10: Command Execution** - Support for movement, speed, servo commands

### Education Features
- **Learn Topics** - Organized robotics learning content
- **Quiz System** - Comprehensive quiz bank with scoring
- **Quiz Results Screen** - Score display with retry option
- **Quiz Review Flow** - Review answers after completion
- **Mini-Games**:
  - **Robot Path Planner**: Grid-based pathfinding game with obstacles
  - **Circuit Builder**: Visual circuit design with LED/switch validation
- **Progress Saving** - Best quiz scores, game completion flags stored in SharedPreferences
- **Game Achievements** - Level-based difficulty in Path Planner, complexity in Circuit Builder

## 📁 Project Structure

```
app/src/main/java/com/example/robotcontrol/
├── models/
│   ├── Robot.java              # Robot data model
│   ├── User.java               # User data model
│   └── RobotPermission.java    # Permission data model
├── adapters/
│   ├── RobotAdapter.java       # RecyclerView for robot list
│   ├── DeviceAdapter.java      # RecyclerView for pairing
│   └── PermissionAdapter.java  # RecyclerView for permissions
├── database/
│   └── DatabaseHelper.java     # SQLite database helper
├── utils/
│   ├── ConnectionManager.java  # Bluetooth connection manager
│   ├── EducationProgressStore.java # Progress persistence
│   ├── AppSettings.java        # Theme & app configuration
│   └── WiFiManagerHelper.java  # WiFi connection helper
├── logic/
│   ├── QuizScoring.java        # Quiz scoring logic (testable)
│   ├── CircuitValidation.java  # Circuit validation (testable)
│   └── RoboticsProgramSimulator.java # Path planner simulation
├── services/
│   └── AutoReconnectService.java # Background auto-reconnect
├── Activities/
│   ├── LoginActivity.java      # Authentication
│   ├── DashboardActivity.java  # Main dashboard with top 3 robots preview
│   ├── RobotListActivity.java  # Robot list view
│   ├── PairingActivity.java    # Bluetooth discovery & pairing
│   ├── ControlActivity.java    # Bluetooth robot control (8 servos)
│   ├── RobotControlActivity.java # WiFi robot control
│   ├── SettingsActivity.java   # Permissions & settings
│   ├── EducationHomeActivity.java # Education hub
│   ├── LearnActivity.java      # Learning content
│   ├── QuizActivity.java       # Quiz gameplay
│   ├── QuizResultActivity.java # Quiz results display
│   ├── QuizReviewActivity.java # Review answers
│   ├── RoboticsGameActivity.java # Path Planner game
│   └── CircuitBuilderActivity.java # Circuit Builder game
└── res/
    ├── layout/                 # XML layouts
    ├── drawable/               # Vector drawables & icons
    ├── values/                 # Colors, strings, dimensions
    ├── values-night/           # Dark theme resources
    └── xml/                    # Robot levels config, animations

app/src/test/java/com/example/robotcontrol/logic/
├── QuizScoringTest.java        # Unit tests for scoring
├── CircuitValidationTest.java  # Unit tests for validation
└── RoboticsProgramSimulatorTest.java # Unit tests for path planner
```

## 🎯 Screens & Navigation

1. **Login Screen** → Dashboard (authenticated)
2. **Dashboard** → Robot List, Education Hub, or tap preview card
3. **Robot List** → Control (Bluetooth) / WiFi Control
4. **Robot Control** → Movement & servo commands (Bluetooth with 8-servo support)
5. **WiFi Control** → ESP32 HTTP commands with servo sliders
6. **Education Hub** → Learn Topics, Quiz Bank, Mini-Games
7. **Learn** → Organized content sections
8. **Quiz** → Answer questions, submit, view results
9. **Quiz Results** → Score, retry, or review answers
10. **Quiz Review** → See all questions with correct answers highlighted
11. **Path Planner Game** → Navigate grid avoiding obstacles
12. **Circuit Builder Game** → Design circuits with visual validation

## 🛠️ Technologies Used

- **Language**: Java 11
- **UI Framework**: Material Design Components, ViewBinding
- **Lists/Grids**: RecyclerView, CardView, ViewPager2
- **Database**: SQLite (local storage via `DatabaseHelper`)
- **Authentication**: SharedPreferences + local user database
- **Communication**: 
  - Bluetooth Classic (RFCOMM, UUID: `00001101-0000-1000-8000-00805F9B34FB`)
  - WiFi HTTP (ESP32 with REST endpoints)
- **Cloud (Optional)**: Firebase Auth + Realtime Database (connection code present)
- **Testing**: JUnit 4 (unit tests for logic helpers)
- **Build System**: Gradle with Kotlin DSL (build.gradle.kts)
- **Minimum SDK**: 29 (Android 10)
- **Target SDK**: 36 (Android 15)

## 📋 Setup Instructions

### Prerequisites
- Android Studio (latest recommended)
- JDK 11+
- Android device or emulator (API 29+)

### 1. Clone the Repository

```bash
git clone https://github.com/Lamiaehadi/android-robot-control.git
cd android-robot-control
```

### 2. Open in Android Studio

1. Launch Android Studio
2. Select "Open an Existing Project"
3. Navigate to the cloned directory
4. Allow Gradle sync to complete (may take 1-2 minutes)

### 3. Build & Run

**Command line:**
```bash
./gradlew assembleDebug
./gradlew installDebug
```

**Android Studio:**
- Press `Shift + F10` (or Run → Run 'app')
- Select target device and run

### 4. Permissions

When prompted, grant:
- ✅ Bluetooth
- ✅ Bluetooth Scan
- ✅ Bluetooth Connect
- ✅ Location (required for Bluetooth scanning on Android 10+)
- ✅ Camera (optional, for future features)

### 5. Run Unit Tests

```bash
./gradlew testDebugUnitTest
```

Tests verify:
- Quiz scoring logic
- Circuit validation rules
- Path planner collision detection and goal reaching

## 🤖 Robot Firmware Implementation

### Bluetooth Protocol (ControlActivity.java)

Your robot firmware should implement an RFCOMM server:

**Commands received (single character, no newline)**:
- `a` - Move forward
- `b` - Move backward
- `l` - Turn left
- `w` - Turn right
- `s` - Stop
- `t` - Test/beep

**Servo commands**:
- Format: `p<index>:<angle>;` (example: `p3:120;`)
- Index: 1-8 (eight servo support)
- Angle: 0-180 degrees

**UUID**: `00001101-0000-1000-8000-00805F9B34FB`

### WiFi Protocol (RobotControlActivity.java + RobotController.java)

For ESP32 robots, implement HTTP endpoints:

**Configuration** (defaults):
- SSID: `Robot_AP`
- Password: `12345678`
- IP: `192.168.4.1`
- Port: `8888`

**Endpoints** (HTTP GET requests):
- `/forward` - Move forward
- `/backward` - Move backward
- `/left` - Turn left
- `/right` - Turn right
- `/stop` - Stop motors
- `/set?servo=<n>&angle=<0..180>` - Set servo angle

### Example Arduino/ESP32 Pseudocode

```cpp
// Bluetooth RFCOMM Server
void handleBluetoothCommand(char cmd) {
  switch(cmd) {
    case 'a': moveForward(); break;
    case 'b': moveBackward(); break;
    case 'l': turnLeft(); break;
    case 'w': turnRight(); break;
    case 's': stopMotors(); break;
    case 't': beep(); break;
  }
}

// Servo command: p<index>:<angle>;
void handleServoCommand(String cmd) {
  int index = cmd[1] - '0';
  int angle = atoi(cmd.substring(3).c_str());
  setServo(index, angle);
}

// WiFi HTTP Handler (ESP32)
void handleHTTP(String path) {
  if (path == "/forward") moveForward();
  else if (path == "/set?servo=3&angle=120") setServo(3, 120);
}
```

## 🎨 Design System

### Color Palette
| Color | Hex | Usage |
|-------|-----|-------|
| **Robot Blue** | #1E88E5 | Primary brand, gradients |
| **Cyber Purple** | #7C4DFF | Gradient accent, secondary |
| **Tech Orange** | #FF6F00 | FAB, accent elements |
| **Neon Green** | #00E676 | Connected status, success |
| **Status Red** | #EF5350 | Disconnected, errors |
| **White** | #FFFFFF | Cards, overlays |
| **Dark Gray** | #212121 | Text on light backgrounds |

### Gradients
- **Primary Gradient**: Blue (#1E88E5) → Purple (#7C4DFF)
- **Accent Gradient**: Orange (#FF6F00) → Deep Orange (#E65100)
- **Status Gradient**: Green (#00E676) → Teal (#00BCD4)

### Typography Scale
| Level | Size | Weight | Usage |
|-------|------|--------|-------|
| Display | 32sp | Bold | App title, splash screen |
| Headline | 24sp | Bold | Section headers |
| Title | 20sp | SemiBold | Robot names, card titles |
| Subtitle | 18sp | Medium | Supporting headings |
| Body | 16sp | Regular | Main content, descriptions |
| Small | 14sp | Regular | Status labels, hints |
| Caption | 12sp | Regular | Timestamps, secondary text |

### Spacing System (4dp base unit)
- Tiny: 4dp
- Small: 8dp
- Medium: 16dp
- Large: 24dp
- XLarge: 32dp
- XXLarge: 48dp

### Components
- **Cards**: 12-16dp border radius, 4-8dp elevation
- **Buttons**: 48dp height, 8dp border radius, 16dp horizontal padding
- **Control Buttons**: 80×80dp, color-coded (Green/Yellow/Cyan/Red)
- **FAB**: 56dp diameter, shadow depth 8dp
- **SeekBars**: 4dp height, blue tint (#1E88E5)
- **Icons**: 16dp (caption), 24dp (toolbar), 32dp (buttons), 96dp (hero images)

## 💾 Database Schema

### SQLite Tables

**users**
```sql
CREATE TABLE users (
  user_id TEXT PRIMARY KEY,
  email TEXT UNIQUE,
  user_name TEXT,
  user_password TEXT
);
```

**robots**
```sql
CREATE TABLE robots (
  id TEXT PRIMARY KEY,
  name TEXT,
  mac_address TEXT,
  ip_address TEXT,
  type TEXT,
  owner_id TEXT,
  connection_type TEXT,  -- "bluetooth" or "wifi"
  last_connected INTEGER,
  is_connected INTEGER
);
```

**credentials**
```sql
CREATE TABLE credentials (
  robot_id TEXT PRIMARY KEY,
  ssid TEXT,
  password TEXT
);
```

**permissions**
```sql
CREATE TABLE permissions (
  permission_id TEXT PRIMARY KEY,
  robot_id TEXT,
  user_id TEXT,
  user_email TEXT,
  can_control INTEGER,
  granted_at INTEGER
);
```

### SharedPreferences (EducationProgressStore.java)

- `quiz_best_score_<quiz_id>` - Best quiz score achieved
- `quiz_last_score_<quiz_id>` - Most recent quiz score
- `game_completed_<game_id>` - Boolean flag for game completion
- `user_session_token` - Current user session
- `app_theme` - Theme preference (light/dark)

## 🔧 Code Quality & Testing

### Testable Logic Helpers
- **QuizScoring.java**: Pure function for computing quiz scores
- **CircuitValidation.java**: Pure function for validating circuit configurations
- **RoboticsProgramSimulator.java**: Pure function for simulating path planner movements

### Test Coverage
- Quiz scoring with edge cases (0%, 50%, 100%)
- Circuit validation with various switch/LED combinations
- Path planner collision detection and goal reaching

### Resource-Driven Configuration
- Robot levels loaded from `values/robotics_levels.xml`
- Reduces hardcoded logic, improves maintainability

## 📚 Documentation

- **[README.md](README.md)** - This file
- **[QUICKSTART.md](QUICKSTART.md)** - Fast setup guide
- **[IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)** - Technical deep-dive
- **[SCREEN_FLOW.md](SCREEN_FLOW.md)** - Navigation architecture
- **[UI_UX_DESIGN_SYSTEM.md](UI_UX_DESIGN_SYSTEM.md)** - Complete design documentation
- **[VISUAL_SCREEN_GUIDE.md](VISUAL_SCREEN_GUIDE.md)** - Screen previews & layouts

## ⚠️ Known Limitations

1. WiFi connection requires manual SSID/password entry
2. Local storage only (no cloud sync between devices)
3. Requires Android 10 (API 29) or higher
4. Robot firmware must implement Bluetooth RFCOMM server
5. No password reset functionality (local auth only)
6. Path Planner levels hardcoded to 5×5 grid (configurable via XML)

## 🚀 Future Enhancements

- Cloud synchronization with Firebase Firestore
- WiFi direct P2P connection support
- Real-time sensor data dashboard (distance, temperature, etc.)
- Joystick/controller input widget
- Battery percentage display
- Signal strength indicators
- Advanced Lottie animations
- Dark theme implementation
- Multi-robot simultaneous control
- Custom command macros & scripting
- Activity/command logging
- Push notifications for robot events
- Biometric authentication (fingerprint)
- Voice control integration
- AR visualization of robot movements

## 👥 Authors

- **Lamiae Hadi**
- **Louay Mikou**
- **Ilyass Arro**

**Supervised by**: Prof. Hamza Mouncif

**Academic Year**: 2025-2026

## 📄 License

Educational project for UEMF (Université Euromed de Fès)

---

**Last Updated**: January 2026
