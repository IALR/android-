# RoboConnect (RobotControl)

Android app for connecting to and controlling robots over **Bluetooth (Classic SPP)** and **Wi‑Fi (ESP32 HTTP)**, plus an **Education** section with learning topics, quizzes, and mini-games.

## Features

- **Robot management**: add/track robots locally (and optional Firebase integration).
- **Bluetooth control (SPP)**:
  - Motor commands: `a` (forward), `b` (backward), `l` (left), `w` (right), `s` (stop), `t` (test)
  - Servo commands (8-servo robot arm): `p<index>:<angle>;` (example: `p3:120;`)
  - Uses UUID `00001101-0000-1000-8000-00805F9B34FB`
- **Wi‑Fi control (ESP32 HTTP)** (default settings):
  - SSID: `Robot_AP`
  - Password: `12345678`
  - Robot IP/port: `192.168.4.1:8888`
  - Endpoints:
    - Motors: `/forward`, `/backward`, `/left`, `/right`, `/stop`
    - Servos: `/set?servo=<n>&angle=<0..180>`
- **Education hub**:
  - Learn topics
  - Quiz bank + results screen + answer review
  - Mini-games: Robot Path Planner + Circuit Builder
  - Progress saving (best quiz score + game completion)

## Tech Stack

- Android (Java), ViewBinding, Material Components
- Local persistence: SQLite via `DatabaseHelper`
- Cloud (optional): Firebase Auth + Firebase Realtime Database
- Unit tests: JVM tests for logic helpers

## Project Structure

- Android app module: [app/](app/)
- ESP32/Arduino sketches:
  - [ESP32_Robot_AppControl.ino](ESP32_Robot_AppControl.ino)
  - [arduino/](arduino/)

## Requirements

- Android Studio (recommended)
- JDK 11 (project uses Java 11)
- Android device/emulator:
  - Min SDK: 29
  - Target/Compile SDK: 36

## Build & Run

1. Open this folder in Android Studio.
2. Let Gradle sync.
3. Run the `app` configuration on a device.

### Run unit tests

- `./gradlew testDebugUnitTest`

(On Windows PowerShell you can also use `gradlew.bat testDebugUnitTest`.)

## Permissions & Notes

- **Bluetooth (Android 12+)**: you must grant Bluetooth permissions (including `BLUETOOTH_CONNECT`) for scanning/connecting.
- **Wi‑Fi scanning**: on many Android versions, Wi‑Fi scan results require **Location** to be enabled and permission granted.
- **Wi‑Fi control** uses HTTP requests to the robot’s IP/port. The app attempts to bind traffic to the current Wi‑Fi network so commands route to the ESP32 AP.

## Robot Firmware Expectations

### Bluetooth protocol

- Motor control: sends single-character commands with no newline.
- Servo control: `p<index>:<angle>;` where `index` is 1..8 and `angle` is 0..180.

### Wi‑Fi protocol (ESP32)

The app sends HTTP GET requests to `http://<robot_ip>:<port>`:

- `/forward`, `/backward`, `/left`, `/right`, `/stop`
- `/set?servo=<n>&angle=<0..180>`

Defaults are `192.168.4.1:8888`.

## Troubleshooting

- **Wi‑Fi scan shows nothing**: enable Location and grant permission, then retry scan.
- **Wi‑Fi connect fails**: some phones show a system prompt; accept it, then try again.
- **Bluetooth connects but no movement**: verify your firmware maps `a/b/l/w/s` exactly, and that your module is Classic SPP (not BLE).

## License

Add a license if/when you’re ready.
