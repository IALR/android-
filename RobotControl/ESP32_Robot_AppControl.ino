#include <WiFi.h>
#include <ESP32Servo.h>
#include <ArduinoJson.h>

// ===== AP Mode Configuration =====
const char* ap_ssid = "Robot_AP";
const char* ap_password = "12345678";
const IPAddress ap_ip(192, 168, 4, 1);
const IPAddress ap_subnet(255, 255, 255, 0);

// ===== Button setup =====
const int BUTTON_PIN = 34;
bool buttonPressed = false;
bool apStarted = false;
unsigned long lastButtonPressTime = 0;
unsigned long apStartTime = 0;
const unsigned long AP_TIMEOUT_MS = 300000; // 5 minutes
const unsigned long DEBOUNCE_TIME = 50;

// ===== Servo setup =====
Servo servo1, servo2, servo3;
const int servo1_pin = 25;
const int servo2_pin = 26;
const int servo3_pin = 27;
int servo1_pos = 90, servo2_pos = 90, servo3_pos = 90;

// ===== Motor setup =====
const int IN1 = 5;
const int IN2 = 17;
const int IN3 = 16;
const int IN4 = 4;

const uint16_t SERVER_PORT = 8888;
WiFiServer server(SERVER_PORT);

void setup() {
  Serial.begin(115200);
  delay(3000);
  Serial.println("\n\n=== ESP32 ROBOT - ANDROID APP CONTROL ===");
  
  // Configure button with internal pullup
  pinMode(BUTTON_PIN, INPUT_PULLUP);
  Serial.println("[OK] Button on GPIO 34");
  Serial.println("[OK] Press button to TOGGLE AP ON/OFF");

  // Servo setup
  servo1.attach(servo1_pin);
  servo2.attach(servo2_pin);
  servo3.attach(servo3_pin);
  servo1.write(90);
  servo2.write(90);
  servo3.write(90);
  Serial.println("[OK] Servos initialized");

  // Motor setup
  pinMode(IN1, OUTPUT);
  pinMode(IN2, OUTPUT);
  pinMode(IN3, OUTPUT);
  pinMode(IN4, OUTPUT);
  stopMotors();
  Serial.println("[OK] Motors initialized");

  // Turn off WiFi at startup
  WiFi.mode(WIFI_OFF);
  WiFi.disconnect(true);
  
  Serial.println("\n[READY] Press button to start AP");
  Serial.println("=== WAITING FOR BUTTON PRESS ===\n");
}

void stopMotors() {
  digitalWrite(IN1, LOW);
  digitalWrite(IN2, LOW);
  digitalWrite(IN3, LOW);
  digitalWrite(IN4, LOW);
}

void forward() {
  digitalWrite(IN1, LOW); 
  digitalWrite(IN2, HIGH);
  digitalWrite(IN3, HIGH); 
  digitalWrite(IN4, LOW);
}

void backward() {
  digitalWrite(IN1, HIGH); 
  digitalWrite(IN2, LOW);
  digitalWrite(IN3, LOW); 
  digitalWrite(IN4, HIGH);
}

void left() {
  digitalWrite(IN1, HIGH); 
  digitalWrite(IN2, LOW);
  digitalWrite(IN3, HIGH); 
  digitalWrite(IN4, LOW);
}

void right() {
  digitalWrite(IN1, LOW); 
  digitalWrite(IN2, HIGH);
  digitalWrite(IN3, LOW); 
  digitalWrite(IN4, HIGH);
}

void startAP() {
  if (apStarted) return;
  
  Serial.println("\n[AP] Starting Access Point...");
  Serial.print("[AP] SSID: ");
  Serial.println(ap_ssid);
  Serial.print("[AP] Password: ");
  Serial.println(ap_password);
  
  WiFi.mode(WIFI_OFF);
  delay(500);
  WiFi.mode(WIFI_AP);
  WiFi.softAPConfig(ap_ip, ap_ip, ap_subnet);
  
  boolean result = WiFi.softAP(ap_ssid, ap_password);
  
  if (result) {
    apStarted = true;
    apStartTime = millis();
    
    Serial.println("[AP] ✓ ACCESS POINT STARTED!");
    Serial.print("[AP] IP: ");
    Serial.println(WiFi.softAPIP());
    Serial.print("[Server] ✓ App server running on port ");
    Serial.println(SERVER_PORT);
    
    server.begin();
    Serial.println("=== APP CAN NOW CONNECT ===\n");
  } else {
    Serial.println("[AP] ✗ FAILED TO START!");
    apStarted = false;
  }
}

void stopAP() {
  if (!apStarted) return;
  
  Serial.println("\n[AP] Stopping Access Point...");
  stopMotors();
  server.stop();
  WiFi.softAPdisconnect(true);
  WiFi.mode(WIFI_OFF);
  apStarted = false;
  Serial.println("[AP] ✓ AP stopped. System offline.\n");
}

void sendJsonResponse(WiFiClient &client, const char* command, bool success) {
  StaticJsonDocument<256> doc;
  doc["command"] = command;
  doc["status"] = success ? "ok" : "error";
  doc["servo1"] = servo1_pos;
  doc["servo2"] = servo2_pos;
  doc["servo3"] = servo3_pos;
  
  String json;
  serializeJson(doc, json);
  
  client.print("HTTP/1.1 200 OK\r\n");
  client.print("Content-Type: application/json\r\n");
  client.print("Content-Length: ");
  client.print(json.length());
  client.print("\r\n\r\n");
  client.print(json);
}

void loop() {
  // Button detection
  bool currentButtonState = !digitalRead(BUTTON_PIN);
  
  if (currentButtonState && !buttonPressed) {
    buttonPressed = true;
    lastButtonPressTime = millis();
    Serial.println("\n[Button] PRESSED!");
    
    if (apStarted) {
      stopAP();
    } else {
      startAP();
    }
  }
  
  if (!currentButtonState && buttonPressed) {
    if (millis() - lastButtonPressTime > DEBOUNCE_TIME) {
      buttonPressed = false;
    }
  }
  
  // Timeout check
  if (apStarted && millis() - apStartTime > AP_TIMEOUT_MS) {
    Serial.println("[Timeout] 5 minutes reached! Turning OFF AP.");
    stopAP();
  }
  
  // Handle app requests
  if (apStarted) {
    WiFiClient client = server.available();
    if (!client) return;

    String request = client.readStringUntil('\n');
    request.trim();

    // Read and discard remaining headers quickly (best-effort)
    // so the next request isn't affected.
    unsigned long headerStart = millis();
    while (client.connected() && millis() - headerStart < 50) {
      String h = client.readStringUntil('\n');
      h.trim();
      if (h.length() == 0) break; // end of headers
    }

    // Example: "GET /forward HTTP/1.1"
    String path = "";
    int sp1 = request.indexOf(' ');
    int sp2 = (sp1 >= 0) ? request.indexOf(' ', sp1 + 1) : -1;
    if (sp1 >= 0 && sp2 > sp1) {
      path = request.substring(sp1 + 1, sp2);
    }

    Serial.print("[Web] From ");
    Serial.print(client.remoteIP());
    Serial.print(" -> ");
    Serial.println(request);
    Serial.print("[Web] Path: ");
    Serial.println(path);

    bool handled = false;

    // ===== MOTOR CONTROL =====
    if (request.indexOf("/forward") != -1) {
      forward();
      Serial.println("[Cmd] forward");
      sendJsonResponse(client, "forward", true);
      handled = true;
    }
    else if (request.indexOf("/backward") != -1) {
      backward();
      Serial.println("[Cmd] backward");
      sendJsonResponse(client, "backward", true);
      handled = true;
    }
    else if (request.indexOf("/left") != -1) {
      left();
      Serial.println("[Cmd] left");
      sendJsonResponse(client, "left", true);
      handled = true;
    }
    else if (request.indexOf("/right") != -1) {
      right();
      Serial.println("[Cmd] right");
      sendJsonResponse(client, "right", true);
      handled = true;
    }
    else if (request.indexOf("/stop") != -1) {
      stopMotors();
      Serial.println("[Cmd] stop");
      sendJsonResponse(client, "stop", true);
      handled = true;
    }
    
    // ===== SERVO CONTROL =====
    else if (request.indexOf("/set") != -1) {
      int servoIndex = request.indexOf("servo=");
      int angleIndex = request.indexOf("angle=");
      
      if (servoIndex != -1 && angleIndex != -1) {
        int servoNum = request.substring(servoIndex + 6, request.indexOf('&')).toInt();
        int angle = request.substring(angleIndex + 6).toInt();
        
        if (angle >= 0 && angle <= 180) {
          Serial.printf("[Cmd] set servo=%d angle=%d\n", servoNum, angle);
          
          switch (servoNum) {
            case 1:
              servo1.write(angle);
              servo1_pos = angle;
              break;
            case 2:
              servo2.write(angle);
              servo2_pos = angle;
              break;
            case 3:
              servo3.write(angle);
              servo3_pos = angle;
              break;
          }
          sendJsonResponse(client, "servo", true);
          handled = true;
        }
      }
    }
    
    // ===== STATUS REQUEST =====
    else if (request.indexOf("/status") != -1) {
      Serial.println("[Cmd] status");
      sendJsonResponse(client, "status", true);
      handled = true;
    }

    // Default response
    if (!handled) {
      client.print("HTTP/1.1 404 Not Found\r\n");
      client.print("Content-Type: application/json\r\n\r\n");
      client.print("{\"status\":\"error\",\"message\":\"Unknown command\"}");
    }

    delay(10);
    client.stop();
  }
}
