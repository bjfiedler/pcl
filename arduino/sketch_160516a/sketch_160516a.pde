import processing.serial.*;


Serial myport;
PrintWriter output;
void setup(){
  printArray(Serial.list());
  myport = new Serial(this,Serial.list()[0],115200);
  myport.clear();
  output  = createWriter("log_" + year()+month()+day()+'_'+hour()+minute()+second()+".txt");
  

}

void draw(){
  
  while (myport.available() > 0){
    String data = myport.readStringUntil('\n');

    if (data != null){
      output.print(millis());
      output.print(',');
      output.print(data);
      output.flush();
      println(data);      
      if (data.contains("OK")){
        myport.write("AT+BLEGETRSSI\n");
      }
    }
  }
  
}