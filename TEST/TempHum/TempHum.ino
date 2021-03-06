/*
  ESP8266 Blink by Simon Peter
  Blink the blue LED on the ESP-01 module
  This example code is in the public domain

  The blue LED on the ESP-01 module is connected to GPIO1
  (which is also the TXD pin; so we cannot use Serial.print() at the same time)

  Note that this sketch uses LED_BUILTIN to find the pin with the internal LED
*/
#include "DHT.h"
#include <WiFiManager.h>
#include <ESP8266WiFi.h>                // Thư viện dùng để kết nối WiFi của ESP8266
#include <ESP8266HTTPClient.h>
#include <Arduino.h>
#include <IRremoteESP8266.h>
#include <IRsend.h>
#include <ir_Daikin.h>
#include <IRutils.h>
#include "BaseOTA.h"
#include <assert.h>
#include <ArduinoJson.h>


// DHT11 Nhiet do
#define DHTPIN 5 // what digital pin we're connected to
#define DHTTYPE DHT11 // DHT 11
// LED VA RELAY QUAT
#define RELAY1PIN 0
#define LEDPIN 2
// Hong ngoai may lanh
const uint16_t kIrLed = 4;  // ESP8266 GPIO pin to use
IRDaikinESP ac(kIrLed);  // Set the GPIO to be used to sending the message



// WIFI
//const char* ssid = "THANH TRUNG";         // Tên của mạng WiFi mà bạn muốn kết nối đến
//const char* password = "lalala12";// Mật khẩu của mạng WiFi

// value_off is value on or off on remote AC. 0 - 2 (send ON 3 times)  --- 3 - 5 (send OFF 3 times)
int value_off = 0;

String nhietdo, doam, mota, postData; // value for POSTDATA 

// SENSOR nhiet do
DHT dht(DHTPIN, DHTTYPE); // sensor temp, hum
// Json parse
StaticJsonDocument<1024> doc;


void setup() {
    ac.begin();  
    // baudrate
    Serial.begin(115200);
    // LED
    pinMode(LED_BUILTIN, OUTPUT);     // Initialize the LED_BUILTIN pin as an output
    pinMode(RELAY1PIN, OUTPUT);     // Initialize the RELAY for fan pin as an output
    pinMode(LEDPIN, OUTPUT);        // Initialize the LED pin as an output
  
    dht.begin(); // Start dht
  //WiFiManager
  //Local intialization. Once its business is done, there is no need to keep it around
  WiFiManager wifiManager;
  //reset settings - for testing
  //wifiManager.resetSettings();

  //sets timeout until configuration portal gets turned off
  //useful to make it all retry or go to sleep
  //in seconds
  wifiManager.setConfigPortalTimeout(180);
  
  //fetches ssid and pass and tries to connect
  //if it does not connect it starts an access point with the specified name
  //here  "AutoConnectAP"
  //and goes into a blocking loop awaiting configuration
  if(!wifiManager.autoConnect("AutoConnectAP")) {
    Serial.println("failed to connect and hit timeout");
    delay(3000);
    //reset and try again, or maybe put it to deep sleep
    ESP.restart();
    delay(5000);
  } 

  //if you get here you have connected to the WiFi
  Serial.println("connected...yeey :)");

    Serial.println("\n");
    Serial.println("Connection established!");  
    Serial.print("IP address: ");
    Serial.println(WiFi.localIP());       // Gởi địa chỉ IP đến máy tinh

}
void postDataTempHum(float h, float t){
      HTTPClient http; 
        //convert value to String
      nhietdo = String(t);
      doam = String(h);
     // POST nhiet do va do am
      postData = "nhietdo=" + nhietdo + "&doam=" + doam + "&mota=" + mota;
      // insert data
      http.begin("http://ntd29011999.000webhostapp.com/insertdata.php");              // Connect to host where MySQL databse is hosted
      http.addHeader("Content-Type", "application/x-www-form-urlencoded");            //Specify content-type header
      
      int httpCode = http.POST(postData);   // Send POST request to php file and store server response code in variable named httpCode
      Serial.println("Values are, nhietdo = " + nhietdo + " and doam = "+ doam + " and mota = " +mota );
      // if connection eatablished then do this
      if (httpCode == 200) { 
        Serial.println("Values uploaded successfully."); 
        Serial.println(httpCode); 
        String webpage = http.getString();    // Get html webpage output and store it in a string
        Serial.println(webpage + "\n");
      }

// if failed to connect then return and restart

      else { 
        Serial.println(httpCode); 
        Serial.println("Failed to upload values. \n"); 
        http.end(); 
        return; 
        }
      http.end();
}



void getDataDevice(String &battat, String &chedo, String &led){     
//getdataDevice
      HTTPClient http; 
      http.begin("http://ntd29011999.000webhostapp.com/getdataDevice.php");              // Connect to host where MySQL databse is hosted
      http.addHeader("Content-Type", "application/x-www-form-urlencoded");            //Specify content-type header
 
      int httpCode = http.GET();   // Send POST request to php file and store server response code in variable named httpCode
      // if connection eatablished then do this
      if (httpCode > 0) { 
        Serial.println("Values get successfully."); 
        Serial.println(httpCode); 
        String webpage = http.getString();    // Get html webpage output and store it in a string
        Serial.println(webpage + "\n");

 
        DeserializationError error = deserializeJson(doc, webpage);

  // Test if parsing succeeds.
        if (error) {
          Serial.print(F("deserializeJson() failed: "));
          Serial.println(error.f_str());
          return;
        }
      
        // Fetch values.
        JsonArray array = doc.as<JsonArray>();
        for(JsonVariant v : array) {
          JsonObject obj = v.as<JsonObject>();
          battat = obj["battat"].as<String>();
          chedo = obj["chedo"].as<String>();
          led = obj["led"].as<String>();
          Serial.println(battat);
        }
        doc.clear();
      }

// if failed to connect then return and restart
    else { 
        Serial.println(httpCode); 
        Serial.println("Failed to upload values. \n"); 
        http.end(); 
        return; 
        }
     http.end();
}

// the loop function runs over and over again forever
void loop() {
    // String for config mode off device
      String battat;
      String chedo;
      String led;
   // Wait a few seconds between measurements.
      
      getDataDevice(battat,chedo,led);
  
// Reading temperature or humidity takes about 250 milliseconds!
 // Sensor readings may also be up to 2 seconds 'old' (its a very slow sensor)
      float h = dht.readHumidity();
      float t = dht.readTemperature();
      Serial.println();
      if (isnan(h) || isnan(t)) 
                 {
                     Serial.println("Failed to read from DHT sensor!");
                      return;
                 }
      Serial.print("Temperature: ");
      Serial.print(t);
      Serial.print(" degrees Celcius, Humidity: ");
      Serial.println(h);

      // dieu khien AUTO
      if (chedo.equals("AUTO")){
         // DIEU KHIEN AUTO
         Serial.println("AUTO START");
         if (t >= 23 && t <= 32){
          mota = "binh thuong";
         
          if(t <=27){
            if (value_off < 3) value_off = 3;
            if (value_off<7 && value_off >= 3){
  //          ac.off();
  //          #if SEND_DAIKIN
  //          ac.send();
  //          value_off++;
  //          if (value_off == 6)
  //            value_off = 0;
  //          #endif  // SEND_DAIKIN
              digitalWrite(RELAY1PIN, HIGH);
              digitalWrite(LEDPIN, LOW);
              Serial.println("FAN: OFF\nLED: ON");
            
            }
           
          }
          else{
            digitalWrite(RELAY1PIN, HIGH);
              digitalWrite(LEDPIN, HIGH);
              Serial.println("FAN: OFF\nLED: OFF");
          }
        }
        else if (t > 32){
          
          mota = "nong";
          if (t > 35){
            if(value_off >= 3) value_off = 0;
            if(value_off < 3){
            
            Serial.println("Sending...");
            digitalWrite(RELAY1PIN, LOW);
            digitalWrite(LEDPIN, HIGH);
            Serial.println("FAN: ON\nLED: ON");
  //          // Set up what we want to send. See ir_Daikin.cpp for all the options.
  //          ac.on();
  //          ac.setFan(5);
  //          ac.setMode(kDaikinCool);
  //          ac.setTemp(22);
  //          ac.setSwingVertical(false);
  //          ac.setSwingHorizontal(false);
  //        
  //          // Set the current time to 1:33PM (13:33)
  //          // Time works in minutes past midnight
  //
  //          // Display what we are going to send.
  //          Serial.println(ac.toString());
  //        
  //          // Now send the IR signal.
  //        #if SEND_DAIKIN
  //          ac.send();
  //        #endif  // SEND_DAIKIN
          value_off++;
          }
          }
          else{
          if(value_off >= 3) value_off = 0;
          if(value_off < 3){
            
            Serial.println("Sending...");
            digitalWrite(RELAY1PIN, LOW);
            digitalWrite(LEDPIN, HIGH);
            Serial.println("FAN: ON\nLED: OFF");
  //          // Set up what we want to send. See ir_Daikin.cpp for all the options.
  //          ac.on();
  //          ac.setFan(5);
  //          ac.setMode(kDaikinCool);
  //          ac.setTemp(25);
  //          ac.setSwingVertical(false);
  //          ac.setSwingHorizontal(false);
  //        
  //          // Set the current time to 1:33PM (13:33)
  //          // Time works in minutes past midnight
  //
  //          // Display what we are going to send.
  //          Serial.println(ac.toString());
  //        
  //          // Now send the IR signal.
  //        #if SEND_DAIKIN
  //          ac.send();
  //        #endif  // SEND_DAIKIN
          value_off++;
          }
          }
        }
        else {
          mota = "lanh";
          digitalWrite(RELAY1PIN, HIGH);
            digitalWrite(LEDPIN, LOW);
            Serial.println("FAN: OFF\nLED: ON");
  //        ac.off();
  //          #if SEND_DAIKIN
  //          ac.send();
  //          value_off++;
  //          if (value_off == 6)
  //            value_off = 0;
  //          #endif  // SEND_DAIKIN
            
        }
      }
      //dieu khien MANUAL
      else if (chedo.equals("MANUAL")){
        Serial.println("MANUAL START");
        if (battat.equals("ON")){
          digitalWrite(RELAY1PIN, LOW);
          Serial.println("FAN: ON");
        }
        else if (battat.equals("OFF")){
          digitalWrite(RELAY1PIN, HIGH);
          Serial.println("FAN: OFF");
        }
        if (led.equals("ON")){
          digitalWrite(LEDPIN, LOW);
          Serial.println("LED: ON");
        }
        else if(led.equals("OFF")){
          digitalWrite(LEDPIN, HIGH);
          Serial.println("LED: OFF");
        }
      }
  



      postDataTempHum(h,t);
      

   
   
    Serial.println(value_off);
    // 90s run again     
    delay(300000);

}








  
