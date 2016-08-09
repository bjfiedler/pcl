/*********************************************************************
  This is an example for our nRF51822 based Bluefruit LE modules

  Pick one up today in the adafruit shop!

  Adafruit invests time and resources providing this open source code,
  please support Adafruit and open-source hardware by purchasing
  products from Adafruit!

  MIT license, check LICENSE for more information
  All text above, and the splash screen below must be included in
  any redistribution
*********************************************************************/

#include <Arduino.h>
#include <SPI.h>
#if not defined (_VARIANT_ARDUINO_DUE_X_) && not defined (_VARIANT_ARDUINO_ZERO_)
#include <SoftwareSerial.h>
#endif

#include "Adafruit_BLE.h"
#include "Adafruit_BluefruitLE_SPI.h"
#include "Adafruit_BluefruitLE_UART.h"

#include "BluefruitConfig.h"

/*=========================================================================
    APPLICATION SETTINGS

      FACTORYRESET_ENABLE       Perform a factory reset when running this sketch
     
                                Enabling this will put your Bluefruit LE module
                              in a 'known good' state and clear any config
                              data set in previous sketches or projects, so
                                running this at least once is a good idea.
     
                                When deploying your project, however, you will
                              want to disable factory reset by setting this
                              value to 0.  If you are making changes to your
                                Bluefruit LE device via AT commands, and those
                              changes aren't persisting across resets, this
                              is the reason why.  Factory reset will erase
                              the non-volatile memory where config data is
                              stored, setting it back to factory default
                              values.
         
                                Some sketches that require you to bond to a
                              central device (HID mouse, keyboard, etc.)
                              won't work at all with this feature enabled
                              since the factory reset will clear all of the
                              bonding data stored on the chip, meaning the
                              central device won't be able to reconnect.
    MINIMUM_FIRMWARE_VERSION  Minimum firmware version to have some new features
    MODE_LED_BEHAVIOUR        LED activity, valid options are
                              "DISABLE" or "MODE" or "BLEUART" or
                              "HWUART"  or "SPI"  or "MANUAL"
    -----------------------------------------------------------------------*/
#define FACTORYRESET_ENABLE         1
#define MINIMUM_FIRMWARE_VERSION    "0.6.6"
#define MODE_LED_BEHAVIOUR          "MODE"
/*=========================================================================*/

// Create the bluefruit object, either software serial...uncomment these lines
/*
  SoftwareSerial bluefruitSS = SoftwareSerial(BLUEFRUIT_SWUART_TXD_PIN, BLUEFRUIT_SWUART_RXD_PIN);

  Adafruit_BluefruitLE_UART ble(bluefruitSS, BLUEFRUIT_UART_MODE_PIN,
                      BLUEFRUIT_UART_CTS_PIN, BLUEFRUIT_UART_RTS_PIN);
*/

/* ...or hardware serial, which does not need the RTS/CTS pins. Uncomment this line */
// Adafruit_BluefruitLE_UART ble(BLUEFRUIT_HWSERIAL_NAME, BLUEFRUIT_UART_MODE_PIN);

/* ...hardware SPI, using SCK/MOSI/MISO hardware SPI pins and then user selected CS/IRQ/RST */
Adafruit_BluefruitLE_SPI ble(BLUEFRUIT_SPI_CS, BLUEFRUIT_SPI_IRQ, BLUEFRUIT_SPI_RST);

/* ...software SPI, using SCK/MOSI/MISO user-defined SPI pins and then user selected CS/IRQ/RST */
//Adafruit_BluefruitLE_SPI ble(BLUEFRUIT_SPI_SCK, BLUEFRUIT_SPI_MISO,
//                             BLUEFRUIT_SPI_MOSI, BLUEFRUIT_SPI_CS,
//                             BLUEFRUIT_SPI_IRQ, BLUEFRUIT_SPI_RST);


// A small helper
void error(const __FlashStringHelper*err) {
  Serial.println(err);
  while (1);
}

/**************************************************************************/
/*!
    @brief  Sets up the HW an the BLE module (this function is called
            automatically on startup)
*/
/**************************************************************************/
void setup(void)
{
  pinMode(5, OUTPUT);
  pinMode(6, OUTPUT);
  pinMode(9, OUTPUT);
  pinMode(10, OUTPUT);
  pinMode(11, OUTPUT);
  pinMode(12, OUTPUT);
  pinMode(13, OUTPUT);
  digitalWrite(5, LOW);
  digitalWrite(6, LOW);
  digitalWrite(9, LOW);
  digitalWrite(10, LOW);
  digitalWrite(11, LOW);
  digitalWrite(12, LOW);
  digitalWrite(13, LOW);
#define BALLPIN 21
  pinMode(BALLPIN, INPUT);

  //  while (!Serial);  // required for Flora & Micro
  delay(500);

  Serial.begin(115200);
  Serial.println(F("Adafruit Bluefruit Command <-> Data Mode Example"));
  Serial.println(F("------------------------------------------------"));

  /* Initialise the module */
  Serial.print(F("Initialising the Bluefruit LE module: "));

  if ( !ble.begin(VERBOSE_MODE) )
  {
    error(F("Couldn't find Bluefruit, make sure it's in CoMmanD mode & check wiring?"));
  }
  Serial.println( F("OK!") );

  if ( FACTORYRESET_ENABLE )
  {
    /* Perform a factory reset to make sure everything is in a known state */
    Serial.println(F("Performing a factory reset: "));
    if ( ! ble.factoryReset() ) {
      error(F("Couldn't factory reset"));
    }
  }

  /* Disable command echo from Bluefruit */
  ble.echo(false);

  Serial.println("Requesting Bluefruit info:");
  /* Print Bluefruit information */
  ble.info();

  Serial.println(F("Please use Adafruit Bluefruit LE app to connect in UART mode"));
  Serial.println(F("Then Enter characters to send to Bluefruit"));
  Serial.println();

  ble.verbose(false);  // debug info is a little annoying after this point!

  /* Wait for connection */
  while (! ble.isConnected()) {
    delay(500);
  }

  Serial.println(F("******************************"));

  // LED Activity command is only supported from 0.6.6
  if ( ble.isVersionAtLeast(MINIMUM_FIRMWARE_VERSION) )
  {
    // Change Mode LED Activity
    Serial.println(F("Change LED activity to " MODE_LED_BEHAVIOUR));
    ble.sendCommandCheckOK("AT+HWModeLED=" MODE_LED_BEHAVIOUR);
  }

  // Set module to DATA mode
  Serial.println( F("Switching to DATA mode!") );
  ble.setMode(BLUEFRUIT_MODE_DATA);

  Serial.println(F("******************************"));

}
#include"AchtungHintermann.raw.h"
#include"Aus.raw.h"
#include"DreiSekunden.raw.h"
#include"HerzlichWillkommen.raw.h"
#include"Trillerpfeife.raw.h"
#include"StilleHalbeSekunde.raw.h"
unsigned long timeForNextSample = 0;
unsigned long currentTime = 0;
unsigned long timeForNextFlash = 0;
const unsigned char* sounds[] = {0, AchtungHintermann, Aus, DreiSekunden, HerzlichWillkommen, Trillerpfeife, StilleHalbeSekunde};
const uint16_t soundLength[] = {0, sizeof(AchtungHintermann), sizeof(Aus), sizeof(DreiSekunden), sizeof(HerzlichWillkommen), sizeof(Trillerpfeife), sizeof(StilleHalbeSekunde)};
uint8_t currentSound = 0;
uint16_t currentSample = 0;
uint8_t flashState = HIGH;
bool flash = false;

uint8_t lastBallState = 0, currentBallState = 0;
unsigned long ballChangeTime = 0;
#define LOSGELASSEN 1
#define FESTGEHALTEN 0
/**************************************************************************/
/*!
    @brief  Constantly poll for new command or response data
*/
/**************************************************************************/
void loop(void)
{
  currentTime = micros();

  if (currentTime >= timeForNextSample) {
    if (currentSound) {
      if (currentSample < soundLength[currentSound]) {

        analogWrite(10, sounds[currentSound][currentSample++]);
      }
      else {
        currentSound = 0;
      }
    }
    timeForNextSample += 125;
    currentBallState = digitalRead(BALLPIN);
    if (currentBallState != lastBallState) {
      lastBallState = currentBallState;
      ballChangeTime = currentTime;
//      ble.write(currentBallState?'f':'F');
      if (currentBallState == LOSGELASSEN) {
        flash = false;
        digitalWrite(3, LOW);
      }
    }
    if (lastBallState == FESTGEHALTEN && (currentTime - ballChangeTime) > 3000000) {
      flash = true;
    }

  }

  else if (!currentSound)
    // Echo received data
    //  while ( ble.available() )
    if ( ble.available() )
    {
      int c = ble.read();

      Serial.print((char)c);
      switch (c) {
        case 'R': digitalWrite(6, HIGH); break;
        case 'G': digitalWrite(5, HIGH); break;
        case 'B': digitalWrite(9, HIGH); break;
        case 'r': digitalWrite(6, LOW); break;
        case 'g': digitalWrite(5, LOW); break;
        case 'b': digitalWrite(9, LOW); break;


        case 'J': digitalWrite(11, HIGH); break;
        case 'K': digitalWrite(12, HIGH); break;
        case 'L': digitalWrite(13, HIGH); break;
        case 'j': digitalWrite(11, LOW); break;
        case 'k': digitalWrite(12, LOW); break;
        case 'l': digitalWrite(13, LOW); break;
        case '1': currentSample = 0; currentSound = 1; timeForNextSample = micros() + 125; break;
        case '2': currentSample = 0; currentSound = 2; timeForNextSample = micros() + 125; break;
        case '3': currentSample = 0; currentSound = 3; timeForNextSample = micros() + 125; break;
        case '4': currentSample = 0; currentSound = 4; timeForNextSample = micros() + 125; break;
        case 'p': currentSample = 0; currentSound = 6; timeForNextSample = micros() + 125; break;
        case '5':
        case 't':
        case 'T': currentSample = 0; currentSound = 5; timeForNextSample = micros() + 125; break;
        case 'F': flash = true; timeForNextFlash = currentTime; break;
        case 'f': flash = false; break;


      }
    }
  if (currentTime >= timeForNextFlash) {
    if (flash || flashState) {
      flashState = !flashState;
      digitalWrite(6, flashState);
      timeForNextFlash += 300000;
    }
  }

}
