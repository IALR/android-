# 🚀 Quick Start Guide - RoboConnect

## Prerequisites

- Android Studio (latest version)
- Android device or emulator (API 29+)
- Firebase account (free)
- Robot with Bluetooth module

---

## Step 1: Firebase Setup (5 minutes)

### A. Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **"Add project"**
3. Name it: `RoboConnect` (or your preferred name)
4. Disable Google Analytics (optional)
5. Click **"Create project"**

### B. Add Android App

1. Click **"Add app"** → Select **Android**
2. Package name: `com.example.robotcontrol`
3. App nickname: `RoboConnect`
4. Click **"Register app"**
5. **Download `google-services.json`**
6. Place it in: `app/google-services.json` (replace existing)

### C. Enable Authentication

1. In Firebase Console → **Authentication**
2. Click **"Get started"**
3. Select **"Email/Password"**
4. Enable **Email/Password**
5. Click **"Save"**

### D. Enable Realtime Database

1. In Firebase Console → **Realtime Database**
2. Click **"Create Database"**
3. Select location: **US or nearest**
4. Start in **test mode** (for development)
5. Click **"Enable"**

**Security Rules** (for testing):
```json
{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null"
  }
}
```

---

## Step 2: Build & Install App (2 minutes)

### Option A: Android Studio

1. Open project in Android Studio
2. Wait for Gradle sync
3. Connect Android device or start emulator
4. Click **Run** button (green play icon)

### Option B: Command Line

```bash
cd RobotControl
./gradlew clean
./gradlew assembleDebug
./gradlew installDebug
```

---

## Step 3: First Run

### A. Grant Permissions

When app starts, grant these permissions:
- ✅ Bluetooth
- ✅ Bluetooth Scan
- ✅ Bluetooth Connect
- ✅ Location (required for BT scanning)

### B. Create Account

1. Open app → **Login screen**
2. Tap **"Register"**
3. Enter email: `test@example.com`
4. Enter password: `test123` (min 6 characters)
5. Tap **"Register"**
6. You're now logged in!

---

## Step 4: Add Your First Robot

### A. Prepare Robot

**Your robot should**:
- Have Bluetooth module (HC-05, HC-06, ESP32, etc.)
- Be in pairing mode (LED blinking)
- Be discoverable

**Example Arduino Setup**:
```cpp
#include <SoftwareSerial.h>
SoftwareSerial BT(10, 11); // RX, TX

void setup() {
  BT.begin(9600);
  Serial.begin(9600);
  Serial.println("Robot ready!");
}

void loop() {
  if (BT.available()) {
    char cmd = BT.read();
    
    switch(cmd) {
      case 'F': moveForward(); break;
      case 'B': moveBackward(); break;
      case 'L': turnLeft(); break;
      case 'R': turnRight(); break;
      case 'S': stop(); break;
      case 'V': setSpeed(BT.parseInt()); break;
      case 'A': servo1.write(BT.parseInt()); break;
      case 'B': servo2.write(BT.parseInt()); break;
    }
  }
}
```

### B. Pair in App

1. In **Robot List** → Tap **+** button
2. Tap **"Scan for Devices"**
3. Wait for your robot to appear
4. Tap on your robot
5. Enter name: `My Robot`
6. Enter type: `Mobile Robot`
7. Tap **"Add"**
8. Robot added! ✅

---

## Step 5: Control Your Robot

1. In **Robot List** → Tap on your robot card
2. Tap **"Connect"**
3. Wait for **"Connected"** status (green)
4. Use controls:
   - **Forward/Backward/Left/Right** buttons
   - **STOP** button (red)
   - **Speed** slider
   - **Servo 1/2** sliders

Commands are sent in real-time!

---

## Step 6: Share Robot (Optional)

### Grant Access to Another User

1. Long-press robot card → **Settings**
2. Tap **"Add Permission"**
3. Enter friend's email: `friend@example.com`
4. Tap **"Add"**
5. Friend can now see & control robot!

### Revoke Access

1. Settings → Find user in list
2. Tap **"Revoke"**
3. Confirm → Access removed

---

## Troubleshooting

### ❌ Can't build: "google-services plugin failed"

**Solution**: 
1. In `app/build.gradle.kts`, change:
   ```kotlin
   id("com.google.gms.google-services") version "4.4.0" apply false
   ```
   to:
   ```kotlin
   id("com.google.gms.google-services") version "4.4.0"
   ```
2. Sync Gradle
3. Rebuild

### ❌ Can't find robots during scan

**Causes**:
- Location permission not granted
- Bluetooth not enabled
- Robot not in pairing mode
- Robot already paired with another device

**Solutions**:
1. Enable Location in device settings
2. Enable Bluetooth
3. Unpair robot from system Bluetooth settings
4. Reset robot's Bluetooth module

### ❌ Can't connect to robot

**Check**:
- Robot is powered on
- Robot's Bluetooth is enabled
- No other app is connected to robot
- MAC address is correct in database

### ❌ Commands not working

**Check**:
- Connection status is "Connected" (green)
- Robot firmware is listening for commands
- Baud rate matches (usually 9600)
- Commands end with newline `\n`

### ❌ Firebase errors

**Check**:
1. `google-services.json` is in `app/` folder
2. Package name matches: `com.example.robotcontrol`
3. Authentication is enabled
4. Database is created
5. Rules allow read/write

---

## Testing Checklist

- [ ] App builds without errors
- [ ] Login/Register works
- [ ] Can scan for Bluetooth devices
- [ ] Can add robot
- [ ] Robot appears in list
- [ ] Can connect to robot
- [ ] Movement commands work
- [ ] Speed control works
- [ ] Servo controls work
- [ ] Can share robot with another user
- [ ] Shared user can see robot
- [ ] Can revoke permissions
- [ ] Can delete robot
- [ ] Logout works
- [ ] Auto-login works

---

## Next Steps

### Enhance App
- Add WiFi support
- Implement sensor data display
- Add robot logs/history
- Create custom command macros

### Enhance Robot
- Add distance sensors
- Add camera module
- Implement obstacle avoidance
- Add battery level reporting

### Deploy
- Change Firebase rules for production
- Add ProGuard rules for release build
- Sign APK for distribution
- Publish to Play Store (optional)

---

## Support

**Documentation**:
- [README.md](README.md) - Full documentation
- [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md) - Implementation details
- [RoboConnect.tex](RoboConnect.tex) - Specifications

**Common Issues**:
- Check Firebase Console for authentication errors
- Check Android Logcat for detailed errors
- Verify Bluetooth permissions are granted
- Ensure robot firmware matches command protocol

---

## Resources

- [Firebase Docs](https://firebase.google.com/docs)
- [Android Bluetooth Guide](https://developer.android.com/guide/topics/connectivity/bluetooth)
- [Material Design](https://material.io/design)
- [Arduino Bluetooth](https://www.arduino.cc/en/Reference/SoftwareSerial)

---

**Enjoy controlling your robots! 🤖**

If you encounter any issues, please check the troubleshooting section or contact the development team.

---

**RoboConnect** - Centralized Robotic Fleet Management
*UEMF 2025-2026*
