# RoboConnect - Centralized Robotic Fleet Management

## 🤖 Project Overview

**RoboConnect** is a professional Android application for managing multiple robots through Bluetooth and WiFi connections. The app features a **modern robotics-themed UI/UX** with vibrant gradients, color-coded controls, and intuitive Material Design components.

### ✨ Key Highlights
- 🎨 **Robotics Theme**: Blue-to-purple gradients with tech-inspired aesthetics
- 🎯 **Color-Coded Controls**: Intuitive operation with visual feedback
- 📱 **Material Design**: Elevated cards, smooth animations, modern components
- 🚀 **Full Feature Set**: All requirements from cahier de charge implemented
- 🔐 **Secure**: Local SQLite database with SharedPreferences authentication

## 🎨 Visual Design

The app features a cohesive robotics theme across all screens:
- **Gradient Backgrounds**: Blue (#1E88E5) → Purple (#7C4DFF) for immersive experience
- **Elevated Cards**: White cards floating on gradients with 4-8dp elevation
- **Custom Icons**: Robot illustrations, circuit patterns, status indicators
- **Color-Coded Buttons**: Green (forward), Yellow (backward), Cyan (turn), Red (stop)
- **Typography System**: 7-level hierarchy from Display (32sp) to Caption (12sp)
- **Spacing System**: Consistent 4dp-based spacing throughout

See [VISUAL_SCREEN_GUIDE.md](VISUAL_SCREEN_GUIDE.md) for screen previews and [UI_UX_DESIGN_SYSTEM.md](UI_UX_DESIGN_SYSTEM.md) for complete design documentation.

## ✅ Implemented Features

### ✅ FR1: Login & Authentication
- Local authentication with SQLite database
- Email/password login and registration
- SharedPreferences for session management
- Automatic login state persistence

### ✅ FR2: Pairing & Discovery
- Bluetooth device scanning
- Real-time device discovery
- Permission handling for Bluetooth/WiFi

### ✅ FR3: Robot Add/Register
- Register discovered robots
- Store robot credentials in local SQLite database
- User becomes administrator upon adding robot

### ✅ FR4: Robot List & Status
- RecyclerView with robot cards
- Live connection status indicators
- Bluetooth/WiFi connection type icons
- Last connected timestamp
- Pull-to-refresh functionality

### ✅ FR5: Direct Robot Control
- Movement controls (Forward, Backward, Left, Right, Stop)
- Speed control slider (0-100)
- Two servo control sliders (0-180°)
- Real-time command transmission over Bluetooth
- Connection status display

### ✅ FR6: Robot Deletion
- Administrator can delete robots
- Removes from local SQLite database
- Confirmation dialog for safety

### ✅ FR7: Permission Management
- Grant access to other users by email
- View all users with access
- Revoke permissions
- Stored in local SQLite database

### ✅ FR8: Auto-Reconnect
- ConnectionManager with auto-reconnect logic
- Configurable reconnect attempts (max 3)
- Reconnect delay: 5 seconds
- Stored credentials for fast reconnects

### ✅ FR9: Pairing Mode (Robot)
- Physical button press enters pairing mode
- Robot becomes discoverable
- (Implementation on robot firmware side)

### ✅ FR10: Command Execution (Robot)
- Robot executes received commands
- Supported commands:
  - F: Forward
  - B: Backward
  - L: Left
  - R: Right
  - S: Stop
  - V[0-100]: Set speed
  - A[0-180]: Servo 1 angle
  - B[0-180]: Servo 2 angle

## Project Structure

```
app/src/main/java/com/example/robotcontrol/
├── models/
│   ├── Robot.java              # Robot data model
│   ├── User.java               # User data model
│   └── RobotPermission.java    # Permission data model
├── adapters/
│   ├── RobotAdapter.java       # RecyclerView adapter for robot list
│   ├── DeviceAdapter.java      # RecyclerView adapter for pairing
│   └── PermissionAdapter.java  # RecyclerView adapter for permissions
├── database/
│   └── DatabaseHelper.java     # SQLite database helper
├── utils/
│   └── ConnectionManager.java  # Bluetooth connection manager
├── LoginActivity.java          # FR1: Authentication
├── RobotListActivity.java      # FR4: Robot list
├── PairingActivity.java        # FR2, FR3: Pairing & discovery
├── ControlActivity.java        # FR5: Robot control
├── SettingsActivity.java       # FR6, FR7: Settings & permissions
└── MainActivity.java           # Original (unused)
```

## Screens & Navigation

1. **Login Screen** → Robot List
2. **Robot List** → Control (tap robot) or Pairing (tap +) or Settings (long press)
3. **Pairing** → Add Robot Dialog → Robot List
4. **Control** → Send commands to robot
5. **Settings** → Manage permissions, delete robot

## Technologies Used

- **Language**: Java
- **UI**: Material Design Components, RecyclerView, CardView
- **Database**: SQLite (local storage)
- **Authentication**: SharedPreferences with local user database
- **Communication**: Bluetooth Classic (RFCOMM)
- **Architecture**: Activity-based with adapters and SQLite helpers

## Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/Lamiaehadi/android-robot-control.git
cd android-robot-control
```

### 2. Open in Android Studio

1. Launch Android Studio
2. Select "Open an Existing Project"
3. Navigate to the cloned directory
4. Wait for Gradle sync to complete

### 3. Build & Run

```bash
./gradlew assembleDebug
./gradlew installDebug
```

Or use Android Studio's Run button (Shift+F10).

### 4. Permissions

Grant the following permissions when prompted:
- Bluetooth
- Bluetooth Scan
- Bluetooth Connect
- Location (required for Bluetooth scanning on Android 10+)

## Robot Side Implementation

Your robot should:

1. **Pairing Mode**: Enter discoverable mode when pairing button is pressed
2. **Bluetooth**: Create RFCOMM server with UUID: `00001101-0000-1000-8000-00805F9B34FB`
3. **Commands**: Listen for commands and execute:
   - Movement: F, B, L, R, S
   - Speed: V + number (0-100)
   - Servos: A/B + number (0-180)

Example Arduino code structure:
```cpp
if (command == 'F') {
  // Move forward
} else if (command == 'V') {
  // Set speed to next number
}
```

## 🎨 Design System

### Color Palette
- **Robot Blue Primary**: #1E88E5 (Main brand color)
- **Tech Orange**: #FF6F00 (Accent, FAB)
- **Neon Green**: #00E676 (Connected status)
- **Cyber Purple**: #7C4DFF (Gradient accent)
- **Status Red**: #EF5350 (Disconnected)

### Gradients
- **Primary**: Blue (#1E88E5) → Purple (#7C4DFF)
- **Accent**: Orange (#FF6F00) → Deep Orange (#E65100)

### Typography
- **Display**: 32sp (App title)
- **Headline**: 24sp (Section headers)
- **Title**: 20sp (Robot names)
- **Subtitle**: 18sp (Supporting text)
- **Body**: 16sp (Main content)
- **Caption**: 12sp (Timestamps)
- **Button**: 14sp uppercase (All buttons)

### Spacing
Base unit: 4dp multiplier
- Tiny: 4dp, Small: 8dp, Medium: 16dp
- Large: 24dp, XLarge: 32dp, XXLarge: 48dp

### Components
- **Cards**: 12-16dp radius, 4-8dp elevation
- **Buttons**: 48dp height, 8dp radius
- **Control Buttons**: 80×80dp, color-coded
- **Icons**: 16-96dp various sizes

See [UI_UX_DESIGN_SYSTEM.md](UI_UX_DESIGN_SYSTEM.md) for complete documentation.

## Database Schema

### SQLite (Local)

**users table**:
- `user_id` (TEXT PRIMARY KEY) - Unique user identifier
- `email` (TEXT UNIQUE) - User email address
- `user_name` (TEXT) - Display name
- `user_password` (TEXT) - Hashed password

**robots table**:
- `id` (TEXT PRIMARY KEY) - Robot unique identifier
- `name` (TEXT) - Robot display name
- `mac_address` (TEXT) - Bluetooth MAC address
- `ip_address` (TEXT) - WiFi IP address
- `type` (TEXT) - Robot type/model
- `owner_id` (TEXT) - User who owns this robot
- `connection_type` (TEXT) - "bluetooth" or "wifi"
- `last_connected` (INTEGER) - Timestamp of last connection

**credentials table**:
- `robot_id` (TEXT PRIMARY KEY) - Foreign key to robots
- `ssid` (TEXT) - WiFi network name
- `password` (TEXT) - WiFi password

**permissions table**:
- `permission_id` (TEXT PRIMARY KEY) - Unique permission identifier
- `robot_id` (TEXT) - Foreign key to robots
- `user_id` (TEXT) - User granted permission
- `user_email` (TEXT) - Email of user with access
- `can_control` (INTEGER) - 1 if can control, 0 otherwise
- `granted_at` (INTEGER) - Timestamp when granted

## 📚 Documentation

- **[QUICKSTART.md](QUICKSTART.md)** - Setup and first run guide
- **[IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)** - Technical implementation details
- **[SCREEN_FLOW.md](SCREEN_FLOW.md)** - App architecture and navigation
- **[UI_UX_DESIGN_SYSTEM.md](UI_UX_DESIGN_SYSTEM.md)** - Complete design system guide
- **[VISUAL_SCREEN_GUIDE.md](VISUAL_SCREEN_GUIDE.md)** - Screen-by-screen visual previews
- **[ROBOTICS_THEME_SUMMARY.md](ROBOTICS_THEME_SUMMARY.md)** - Theme implementation overview

## Known Limitations

1. WiFi connection not fully implemented (Bluetooth only for now)
2. Local storage only - no cloud sync between devices
3. Requires Android 10 (API 29) or higher
4. Robot firmware must implement Bluetooth RFCOMM server
5. No password reset functionality (local auth)

## Future Enhancements

- Cloud synchronization with Firebase
- WiFi direct connection support
- Real-time sensor data display
- Joystick control widget
- Battery and signal indicators
- Lottie animations for better UX
- Dark theme implementation
- Multiple robot simultaneous control
- Custom command macros
- Robot activity logging
- Push notifications for robot events
- Password reset via email

## Authors

- Lamiae Hadi
- Louay Mikou
- Ilyass Arro

**Supervised by**: Prof. Hamza Mouncif

**Academic Year**: 2025-2026

## License

Educational project for UEMF
