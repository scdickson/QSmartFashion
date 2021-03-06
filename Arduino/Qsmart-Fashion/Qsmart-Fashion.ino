/** @file Qsmart-Fashion.ino
 *  @brief Main arduino file for the Qsmart Fashion headband
 * 
 *  Reads input from the Arduino PulseSensor and DS18B20 Temperature
 *  sensor and sends the data over BLE in ASCII format.
 * 
 *  Uses code from the PulseSensor starter code as well as from the bildr
 *  tutorial on the DS18B20 sensor here: http://bildr.org/2011/07/ds18b20-arduino/
 * 
 *  @author Jenna MacCarley
 * 
 */

#include <SPI.h>
#include <Nordic_nRF8001.h>
#include <RBL_nRF8001.h>
#include <OneWire.h>

long Hxv[4]; // these arrays are used in the digital filter
long Hyv[4]; // H for highpass, L for lowpass
long Lxv[4];
long Lyv[4];

unsigned long readings; // used to help normalize the signal
unsigned long peakTime; // used to time the start of the heart pulse
unsigned long lastPeakTime = 0;// used to find the time between beats
volatile int Peak;     // used to locate the highest point in positive phase of heart beat waveform
int rate;              // used to help determine pulse rate
volatile int BPM;      // used to hold the pulse rate
int offset = 0;        // used to normalize the raw data
int sampleCounter;     // used to determine pulse timing
int beatCounter = 1;   // used to keep track of pulses
volatile int Signal;   // holds the incoming raw data
int NSignal;           // holds the normalized signal 
volatile int FSignal;  // holds result of the bandpass filter
volatile int HRV;      // holds the time between beats
volatile int Scale = 13;  // used to scale the result of the digital filter. range 12<>20 : high<>low amplification

boolean first = true; // reminds us to seed the filter on the first go
volatile boolean Pulse = false;  // becomes true when there is a heart pulse
volatile boolean B = false;     // becomes true when there is a heart pulse
volatile boolean QS = false;      // becomes true when pulse rate is determined. every 20 pulses

int pulsePin = 0;  // pulse sensor purple wire connected to analog pin 0

int DS18S20_Pin = 2; //DS18S20 Signal pin on digital 2

//Temperature chip i/o
OneWire ds(DS18S20_Pin);

void setup()
{
    // Set BLE advertising name
	ble_set_name("Jenna HB");
	
	pinMode(12,OUTPUT);  
	pinMode(10, OUTPUT);
	
	Serial.begin(115200);
	
	// this next bit will wind up in the library. it initializes Timer1 to throw an interrupt every 1mS.
	TCCR1A = 0x00; // DISABLE OUTPUTS AND BREAK PWM ON DIGITAL PINS 9 & 10
	TCCR1B = 0x11; // GO INTO 'PHASE AND FREQUENCY CORRECT' MODE, NO PRESCALER
	TCCR1C = 0x00; // DON'T FORCE COMPARE
	TIMSK1 = 0x01; // ENABLE OVERFLOW INTERRUPT (TOIE1)
	ICR1 = 8000;   // TRIGGER TIMER INTERRUPT EVERY 1mS  
	sei();         // MAKE SURE GLOBAL INTERRUPTS ARE ENABLED

  // Init. and start BLE library.
  ble_begin();
}

unsigned char buf[16] = {0};
unsigned char len = 0;
char bpm[32];
char tmp[32];
int bpm_scaled;

void loop()
{
  float temperature = getTemp();
  bpm_scaled = BPM;
  if ( ble_connected() )
  {
  	// Max BPM is 300
  	if(bpm_scaled > 300){
  		bpm[0] = '3';
  		bpm[1] = '0';
  		bpm[2] = '0';
  	}
  	else
    	itoa(bpm_scaled, bpm, 10);
    for(int i = 0; i < 3; i++)
    	ble_write(bpm[i]);
    ble_write(':');
    itoa((int)temperature, tmp, 10);
    for(int i = 0; i < 3; i++)
    	ble_write(tmp[i]);
  }

  ble_do_events();
  
  if ( ble_available() )
  {
    while ( ble_available() )
    {
      Serial.write(ble_read());
    }
    
    Serial.println();
  }
 
  Serial.print("S");          // S tells processing that the following string is sensor data
  Serial.println(Signal);     // Signal holds the latest raw Pulse Sensor signal
  if (B == true){             //  B is true when arduino finds the heart beat
    Serial.print("B");        // 'B' tells Processing the following string is HRV data (time between beats in mS)
    Serial.println(HRV);      //  HRV holds the time between this pulse and the last pulse in mS
    B = false;                // reseting the QS for next time
  }
  if (QS == true){            //  QS is true when arduino derives the heart rate by averaging HRV over 20 beats
    Serial.print("Q");        //  'QS' tells Processing that the following string is heart rate data
    Serial.println(BPM);      //  BPM holds the heart rate in beats per minute
    QS = false;               //  reset the B for next time
  }
  
  Serial.print("Temp is:");
  Serial.println(temperature);
  
  delay(3000);                    //  take a break for 3 seconds
}

/** @brief returns the temperature from one DS18S20 in DEG Celsius */ 
float getTemp(){

 byte data[12];
 byte addr[8];

 if ( !ds.search(addr)) {
   //no more sensors on chain, reset search
   ds.reset_search();
   return -1000;
 }

 if ( OneWire::crc8( addr, 7) != addr[7]) {
   Serial.println("CRC is not valid!");
   return -1000;
 }

 if ( addr[0] != 0x10 && addr[0] != 0x28) {
   Serial.print("Device is not recognized");
   return -1000;
 }

 ds.reset();
 ds.select(addr);
 ds.write(0x44,1); // start conversion, with parasite power on at the end

 byte present = ds.reset();
 ds.select(addr);  
 ds.write(0xBE); // Read Scratchpad

 
 for (int i = 0; i < 9; i++) { // we need 9 bytes
  data[i] = ds.read();
 }
 
 ds.reset_search();
 
 byte MSB = data[1];
 byte LSB = data[0];

 float tempRead = ((MSB << 8) | LSB); //using two's compliment
 float TemperatureSum = tempRead / 16;
 
 return TemperatureSum;
 
}


/** @brief Timer interrupt service routine */
ISR(TIMER1_OVF_vect){
// Timer 1 makes sure that we take a reading every milisecond
Signal = analogRead(pulsePin);

// First normailize the waveform around 0
readings += Signal; // take a running total
sampleCounter++;     // we do this every milisecond. this timer is used as a clock
if ((sampleCounter %300) == 0){   // adjust as needed
  offset = readings / 300;        // average the running total
  readings = 0;                   // reset running total
}
NSignal = Signal - offset;        // normalizing here

// IF IT'S THE FIRST TIME THROUGH THE SKETCH, SEED THE FILTER WITH CURRENT DATA
if (first = true){
  for (int i=0; i<4; i++){
    Lxv[i] = Lyv[i] = NSignal <<10;  // seed the lowpass filter
    Hxv[i] = Hyv[i] = NSignal <<10;  // seed the highpass filter
  }
first = false;      // only seed once please
}
// THIS IS THE BANDPAS FILTER. GENERATED AT www-users.cs.york.ac.uk/~fisher/mkfilter/trad.html
//  BUTTERWORTH LOWPASS ORDER = 3; SAMPLERATE = 1mS; CORNER = 5Hz
    Lxv[0] = Lxv[1]; Lxv[1] = Lxv[2]; Lxv[2] = Lxv[3];
    Lxv[3] = NSignal<<10;    // insert the normalized data into the lowpass filter
    Lyv[0] = Lyv[1]; Lyv[1] = Lyv[2]; Lyv[2] = Lyv[3];
    Lyv[3] = (Lxv[0] + Lxv[3]) + 3 * (Lxv[1] + Lxv[2])
          + (3846 * Lyv[0]) + (-11781 * Lyv[1]) + (12031 * Lyv[2]);
//  Butterworth; Highpass; Order = 3; Sample Rate = 1mS; Corner = .8Hz
    Hxv[0] = Hxv[1]; Hxv[1] = Hxv[2]; Hxv[2] = Hxv[3];
    Hxv[3] = Lyv[3] / 4116; // insert lowpass result into highpass filter
    Hyv[0] = Hyv[1]; Hyv[1] = Hyv[2]; Hyv[2] = Hyv[3];
    Hyv[3] = (Hxv[3]-Hxv[0]) + 3 * (Hxv[1] - Hxv[2])
          + (8110 * Hyv[0]) + (-12206 * Hyv[1]) + (12031 * Hyv[2]);
FSignal = Hyv[3] >> Scale;  // result of highpass shift-scaled

//PLAY AROUND WITH THE SHIFT VALUE TO SCALE THE OUTPUT ~12 <> ~20 = High <> Low Amplification.

if (FSignal >= Peak && Pulse == false){  // heart beat causes ADC readings to surge down in value.  
  Peak = FSignal;                        // finding the moment when the downward pulse starts
  peakTime = sampleCounter;              // recodrd the time to derive HRV. 
}
//  NOW IT'S TIME TO LOOK FOR THE HEART BEAT
if ((sampleCounter %20) == 0){// only look for the beat every 20mS. This clears out alot of high frequency noise.
  if (FSignal < 0 && Pulse == false){  // signal surges down in value every time there is a pulse
     Pulse = true;                     // Pulse will stay true as long as pulse signal < 0
     analogWrite(12,255);            // pin 13 will stay high as long as pulse signal < 0  
     analogWrite(10,255); 
     HRV = peakTime - lastPeakTime;    // measure time between beats
     lastPeakTime = peakTime;          // keep track of time for next pulse
     B = true;                         // set the Quantified Self flag when HRV gets updated. NOT cleared inside this ISR     
     rate += HRV;                      // add to the running total of HRV used to determine heart rate
     beatCounter++;                     // beatCounter times when to calculate bpm by averaging the beat time values
     if (beatCounter == 10){            // derive heart rate every 10 beats. adjust as needed
       rate /= beatCounter;             // averaging time between beats
       BPM = 60000/rate;                // how many beats can fit into a minute?
       beatCounter = 0;                 // reset counter
       rate = 0;                        // reset running total
       QS = true;                       // set Beat flag when BPM gets updated. NOT cleared inside this ISR
     }
  }
  if (FSignal > 0 && Pulse == true){    // when the values are going up, it's the time between beats
    int val = ((BPM) > 255) ? 255 : (BPM);
    int redVal = 0;
    int blueVal = 0;
    if(val > 160){ //RED
    	redVal = 0;
    	blueVal = 255;
    }else if(val > 90 && val <= 160){ //PINK
    	redVal = 253;
    	blueVal = 0;
    }
    else{ //BLUE
    	redVal = 255;
    	blueVal = 0;
    }
    analogWrite(12, blueVal);               // blue 60,180
    analogWrite(10, redVal);
    Pulse = false;                      // reset these variables so we can do it again!
    Peak = 0;                           // 
  }
}

}// end isr
