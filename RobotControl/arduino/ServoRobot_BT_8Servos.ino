// ServoRobot_BT_8Servos.ino
// Bluetooth Serial control for an 8-servo robot.
// Matches your Python/app commands:
//   a = forward (walk)
//   b = backward
//   s = stop / neutral
//   l = low position
//   w = walk position
//   t = test (sweep)
//
// Hardware option A (recommended for Arduino UNO/Nano + HC-05/HC-06):
// - HC-05 TX -> Arduino RX pin (SoftwareSerial RX)
// - HC-05 RX -> Arduino TX pin (SoftwareSerial TX) through a voltage divider (5V->3.3V)
// - HC-05 VCC/GND
//
// IMPORTANT:
// - Replace SERVO_PINS with your real 8 servo signal pins.
// - Replace the position arrays to match your robot geometry.

#include <Servo.h>

#if defined(ARDUINO_AVR_UNO) || defined(ARDUINO_AVR_NANO) || defined(ARDUINO_AVR_MEGA2560)
  #include <SoftwareSerial.h>
  // HC-05 default data baud is often 9600
  static const long BT_BAUD = 9600;
  static const uint8_t BT_RX_PIN = 10; // Arduino receives from HC-05 TX
  static const uint8_t BT_TX_PIN = 11; // Arduino sends to HC-05 RX
  SoftwareSerial BT(BT_RX_PIN, BT_TX_PIN);
  #define BT_PORT BT
  #define BT_SEPARATE_FROM_USB_SERIAL 1
#else
  // If you're on a board with multiple hardware UARTs, change this to the correct Serial port.
  // For example on Mega you could use Serial1.
  #define BT_PORT Serial
  static const long BT_BAUD = 9600;
  #define BT_SEPARATE_FROM_USB_SERIAL 0
#endif

static const uint8_t SERVO_COUNT = 8;

// TODO: set your 8 servo pins here
static const uint8_t SERVO_PINS[SERVO_COUNT] = {
  2, 3, 4, 5, 6, 7, 8, 9
};

Servo servos[SERVO_COUNT];

// Neutral / stop pose
static int posNeutral[SERVO_COUNT] = {90, 90, 90, 90, 90, 90, 90, 90};
// Low pose (darker/lower stance)
static int posLow[SERVO_COUNT]     = {50, 50, 130, 130, 50, 50, 130, 130};
// Walk pose (standing)
static int posWalk[SERVO_COUNT]    = {90, 80, 90, 100, 90, 80, 90, 100};
// Forward leg lift (left)
static int posForwardLeft[SERVO_COUNT] = {110, 60, 70, 100, 90, 80, 90, 100};
// Forward leg lift (right)
static int posForwardRight[SERVO_COUNT] = {90, 80, 90, 120, 70, 80, 90, 100};
// Backward leg lift (left)
static int posBackLeft[SERVO_COUNT] = {70, 100, 90, 100, 90, 80, 90, 100};
// Backward leg lift (right)
static int posBackRight[SERVO_COUNT] = {90, 80, 90, 80, 110, 80, 90, 100};

static const int STEP_DELAY_MS = 8;   // smooth motion
static const int STEP_SIZE = 1;       // degrees per step

static void logLine(const char* msg) {
  Serial.println(msg);
#if BT_SEPARATE_FROM_USB_SERIAL
  BT_PORT.println(msg);
#endif
}

static void logRxChar(char c) {
  Serial.print("[BT RX] ");
  Serial.println(c);
#if BT_SEPARATE_FROM_USB_SERIAL
  BT_PORT.print("[BT RX] ");
  BT_PORT.println(c);
#endif
}

void writePoseSmooth(const int target[SERVO_COUNT]) {
  bool moving = true;
  while (moving) {
    moving = false;
    for (uint8_t i = 0; i < SERVO_COUNT; i++) {
      int current = servos[i].read();
      int desired = target[i];
      if (current < desired) {
        servos[i].write(min(current + STEP_SIZE, desired));
        moving = true;
      } else if (current > desired) {
        servos[i].write(max(current - STEP_SIZE, desired));
        moving = true;
      }
    }
    delay(STEP_DELAY_MS);
  }
}

void poseNeutral() {
  writePoseSmooth(posNeutral);
}

void poseLow() {
  writePoseSmooth(posLow);
}

void poseWalk() {
  writePoseSmooth(posWalk);
}

// Enhanced forward gait with multiple steps for smooth animation
void doForwardStep() {
  // Perform 3 animated forward steps for continuous walking effect
  for (int step = 0; step < 3; step++) {
    // Left leg forward
    writePoseSmooth(posForwardLeft);
    delay(100);
    writePoseSmooth(posWalk);
    delay(100);
    
    // Right leg forward
    writePoseSmooth(posForwardRight);
    delay(100);
    writePoseSmooth(posWalk);
    delay(100);
  }
}

void doBackwardStep() {
  // Perform 3 animated backward steps for continuous walking effect
  for (int step = 0; step < 3; step++) {
    // Left leg backward
    writePoseSmooth(posBackLeft);
    delay(100);
    writePoseSmooth(posWalk);
    delay(100);
    
    // Right leg backward
    writePoseSmooth(posBackRight);
    delay(100);
    writePoseSmooth(posWalk);
    delay(100);
  }
}

void testSweep() {
  BT_PORT.println("TEST: sweeping all servos 0->180->0");
  for (int angle = 0; angle <= 180; angle += 2) {
    for (uint8_t i = 0; i < SERVO_COUNT; i++) servos[i].write(angle);
    delay(10);
  }
  for (int angle = 180; angle >= 0; angle -= 2) {
    for (uint8_t i = 0; i < SERVO_COUNT; i++) servos[i].write(angle);
    delay(10);
  }
  poseNeutral();
  BT_PORT.println("TEST: done");
}

void setup() {
  Serial.begin(115200);

#if defined(ARDUINO_AVR_UNO) || defined(ARDUINO_AVR_NANO)
  BT_PORT.begin(BT_BAUD);
#else
  BT_PORT.begin(BT_BAUD);
#endif

  for (uint8_t i = 0; i < SERVO_COUNT; i++) {
    servos[i].attach(SERVO_PINS[i]);
  }

  poseNeutral();

  logLine("READY: commands a b s l w t");
}

void handleCommand(char c) {
  switch (c) {
    case 'a':
      logLine("[CMD] a -> forward");
      BT_PORT.println("OK a (forward)");
      doForwardStep();
      break;

    case 'b':
      logLine("[CMD] b -> backward");
      BT_PORT.println("OK b (backward)");
      doBackwardStep();
      break;

    case 's':
      logLine("[CMD] s -> stop/neutral");
      BT_PORT.println("OK s (stop/neutral)");
      poseNeutral();
      break;

    case 'l':
      logLine("[CMD] l -> low");
      BT_PORT.println("OK l (low)");
      poseLow();
      break;

    case 'w':
      logLine("[CMD] w -> walk");
      BT_PORT.println("OK w (walk)");
      poseWalk();
      break;

    case 't':
      logLine("[CMD] t -> test");
      BT_PORT.println("OK t (test)");
      testSweep();
      break;

    default:
      // Ignore unknown characters but acknowledge for debugging
      logLine("[CMD] unknown");
      BT_PORT.print("ERR unknown: ");
      BT_PORT.println(c);
      break;
  }
}

static bool readServoSetCommand(int* outServoIndex, int* outAngle) {
  // Expected: p<servoIndex>:<angle>;
  // Example: p3:120;
  // We are called AFTER receiving 'p' and will read the rest until ';'.
  unsigned long start = millis();
  int servoIndex = -1;
  int angle = -1;
  bool seenColon = false;
  String digits;

  while (millis() - start < 400) {
    while (BT_PORT.available() > 0) {
      char ch = (char)BT_PORT.read();
      if (ch == '\r' || ch == '\n' || ch == ' ') continue;
      if (ch == ';') {
        // finalize
        if (!seenColon) return false;
        angle = digits.toInt();
        if (servoIndex < 1 || servoIndex > 8) return false;
        if (angle < 0 || angle > 180) return false;
        *outServoIndex = servoIndex;
        *outAngle = angle;
        return true;
      }
      if (!seenColon) {
        if (ch == ':') {
          servoIndex = digits.toInt();
          digits = "";
          seenColon = true;
        } else if (ch >= '0' && ch <= '9') {
          digits += ch;
        } else {
          return false;
        }
      } else {
        if (ch >= '0' && ch <= '9') {
          digits += ch;
        } else {
          return false;
        }
      }
    }
    delay(1);
  }
  return false;
}

static void setServoManual(int servoIndex, int angle) {
  if (servoIndex < 1 || servoIndex > 8) return;
  angle = constrain(angle, 0, 180);
  servos[servoIndex - 1].write(angle);

  Serial.print("[CMD] p -> servo ");
  Serial.print(servoIndex);
  Serial.print(" = ");
  Serial.println(angle);

#if BT_SEPARATE_FROM_USB_SERIAL
  BT_PORT.print("OK p");
  BT_PORT.print(servoIndex);
  BT_PORT.print(" ");
  BT_PORT.println(angle);
#else
  BT_PORT.print("OK p");
  BT_PORT.print(servoIndex);
  BT_PORT.print(" ");
  BT_PORT.println(angle);
#endif
}

void loop() {
  // Read Bluetooth serial one character at a time (same as your Python script)
  while (BT_PORT.available() > 0) {
    char c = (char)BT_PORT.read();
    if (c == '\r' || c == '\n' || c == ' ') continue;
    // normalize
    if (c >= 'A' && c <= 'Z') c = (char)(c - 'A' + 'a');
    logRxChar(c);

    // Manual servo set: p<index>:<angle>;
    if (c == 'p') {
      int servoIndex = -1;
      int angle = -1;
      if (readServoSetCommand(&servoIndex, &angle)) {
        setServoManual(servoIndex, angle);
      } else {
        logLine("[CMD] p -> invalid");
        BT_PORT.println("ERR p (invalid)");
      }
      continue;
    }

    handleCommand(c);
  }
}
