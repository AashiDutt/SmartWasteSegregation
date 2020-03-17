
// Load WiFi Library
#include <ESP8266WiFi.h>

// Setup WiFI SSID & Password
const char* ssid = "YOUR_WIFI_SSID";
const char* password = "YOUR_WIFI_PASSWORD";

// Set web server port number to 80
WiFiServer server(80);

// Variable to store HTTP request
String header;

// Motor Variables
//String motor_1_State = "off";
//String motor_2_State = "off";
String ledState = "off";

// GPIO Pins Variables
const int led = LED_BUILTIN;

// Setup the GPIO's and start the Web Server
void setup() {
  Serial.begin(115200);
  
  // Initialize GPIO's
  pinMode(led, OUTPUT);
  
  // Set Pins to Low
  digitalWrite(led, LOW);

  // Connect to WiFi
  Serial.print("Connecting to ");
  Serial.println(ssid);
  WiFi.begin(ssid,password);
  while(WiFi.status() != WL_CONNECTED){
    delay(500);
    Serial.print(".");
  }

  // Print local IP address and start web server
  Serial.println("");
  Serial.println("WiFi connected.");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  server.begin();
}

// Look for clients, connect to them and exchange data
void loop() {
  // Listen for Incoming Clients
  WiFiClient client = server.available();
  
  // If no client found, keep searching
  if (!client) {
    return;
  }

  // Wait until the client sends some data
  Serial.println("New client connected.");
  
  // Wait till Client sends some data
  while(!client.available()){
    delay(1);
  }

  // Read data from Client
  String clientResponse = client.readStringUntil('\r');
  Serial.println(clientResponse);
  client.flush();

  // Control the Motors
  // Default Values
  int motor1ControlVal = 101;
  int motor2ControlVal = 102;
  String motorNum = "";

  // Control Motor-1
  if (clientResponse.indexOf("/motor/1/1") != -1){
    // Motor On
    motor1ControlVal = 1;
    motorNum = "1";
  }
  else if (clientResponse.indexOf("/motor/1/0") != -1) {
    // Motor Off
    motor1ControlVal = 0;
    motorNum = "1";
  }
  // Control Motor-2
  else if (clientResponse.indexOf("/motor/2/1") != -1) {
    // Motor On
    motor2ControlVal = 1;
    motorNum = "2";
  }
  else if (clientResponse.indexOf("/motor/2/0") != -1) {
    // Motor Off
    motor2ControlVal = 0;
    motorNum = "2";
  }
  else {
    Serial.println("Invalid Request");
    client.stop();
    return;
  }

  // Debug
  Serial.println("......Recieved Command.....");
  Serial.println(clientResponse.indexOf("/motor/1"));
  Serial.println("Motor-1 Value: ");
  Serial.println(motor1ControlVal);
  Serial.println("Motor-2 Value: ");
  Serial.println(motor2ControlVal);
  Serial.println("...........................");
  
  // Return the response to Client
  client.println("HTTP/1.1 200 OK");
  client.println("Content-Type: text/html");
  client.println("");
  client.print("Controlling Motor Number: ");
  client.print(motorNum);
}
