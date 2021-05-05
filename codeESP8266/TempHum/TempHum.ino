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

// Hong ngoai may lanh
const uint16_t kIrLed = 4;  // ESP8266 GPIO pin to use. Recommended: 4 (D2).
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

  
    dht.begin(); // Start dht
    WiFi.mode(WIFI_STA); // explicitly set mode, esp defaults to STA+AP



    
    // WiFi.mode(WiFi_STA); // it is a good practice to make sure your code sets wifi mode how you want it.

    //WiFiManager, Local intialization. Once its business is done, there is no need to keep it around
    WiFiManager wm;

    //reset settings - wipe credentials for testing
    //wm.resetSettings();

    // Automatically connect using saved credentials,
    // if connection fails, it starts an access point with the specified name ( "AutoConnectAP"),
    // if empty will auto generate SSID, if password is blank it will be anonymous AP (wm.autoConnect())
    // then goes into a blocking loop awaiting configuration and will return success result

    bool res;
    // res = wm.autoConnect(); // auto generated AP name from chipid
    // res = wm.autoConnect("AutoConnectAP"); // anonymous ap
    res = wm.autoConnect("AutoConnectAP","123456"); // password protected ap

    if(!res) {
        Serial.println("Failed to connect");
        // ESP.restart();
    } 
    else {
        //if you get here you have connected to the WiFi    
        Serial.println("connected...yeey :)");
    }

    Serial.println("\n");
    Serial.println("Connection established!");  
    Serial.print("IP address: ");
    Serial.println(WiFi.localIP());       // Gởi địa chỉ IP đến máy tinh

}

// the loop function runs over and over again forever
void loop() {
   // Wait a few seconds between measurements.
      delay(3000);

      
      HTTPClient http; 
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
      Serial.print(h);
      digitalWrite(LED_BUILTIN, LOW);   // Turn the LED on (Note that LOW is the voltage level
      
//convert value to String
      nhietdo = String(t);
      doam = String(h);
     
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
      // get data
      http.begin("http://ntd29011999.000webhostapp.com/getdata.php");              // Connect to host where MySQL databse is hosted
      http.addHeader("Content-Type", "application/x-www-form-urlencoded");            //Specify content-type header
 
      httpCode = http.GET();   // Send POST request to php file and store server response code in variable named httpCode
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
        float nd;
        JsonArray array = doc.as<JsonArray>();
        for(JsonVariant v : array) {
          JsonObject obj = v.as<JsonObject>();
          String sensor = obj["nhietdo"];
          Serial.println(sensor);
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
       if (t >= 23 && t <= 32){
        mota = "binh thuong";
       
        if(t <=27){
          if (value_off < 3) value_off = 3;
          if (value_off<7 && value_off >= 3){
          ac.off();
          #if SEND_DAIKIN
          ac.send();
          value_off++;
          if (value_off == 6)
            value_off = 0;
          #endif  // SEND_DAIKIN
          
          }
        }
      }
      else if (t > 32){
        
        mota = "nong";
        if (t > 35){
          if(value_off >= 3) value_off = 0;
          if(value_off < 3){
          
          Serial.println("Sending...");

          // Set up what we want to send. See ir_Daikin.cpp for all the options.
          ac.on();
          ac.setFan(5);
          ac.setMode(kDaikinCool);
          ac.setTemp(22);
          ac.setSwingVertical(false);
          ac.setSwingHorizontal(false);
        
          // Set the current time to 1:33PM (13:33)
          // Time works in minutes past midnight

          // Display what we are going to send.
          Serial.println(ac.toString());
        
          // Now send the IR signal.
        #if SEND_DAIKIN
          ac.send();
        #endif  // SEND_DAIKIN
        value_off++;
        }
        }
        else{
        if(value_off >= 3) value_off = 0;
        if(value_off < 3){
          
          Serial.println("Sending...");

          // Set up what we want to send. See ir_Daikin.cpp for all the options.
          ac.on();
          ac.setFan(5);
          ac.setMode(kDaikinCool);
          ac.setTemp(25);
          ac.setSwingVertical(false);
          ac.setSwingHorizontal(false);
        
          // Set the current time to 1:33PM (13:33)
          // Time works in minutes past midnight

          // Display what we are going to send.
          Serial.println(ac.toString());
        
          // Now send the IR signal.
        #if SEND_DAIKIN
          ac.send();
        #endif  // SEND_DAIKIN
        value_off++;
        }
        }
      }
      else {
        mota = "lanh";
        ac.off();
          #if SEND_DAIKIN
          ac.send();
          value_off++;
          if (value_off == 6)
            value_off = 0;
          #endif  // SEND_DAIKIN
          
      }
    

   
    Serial.println(value_off);
    // 90s run again     
    delay(300000);

}








  
