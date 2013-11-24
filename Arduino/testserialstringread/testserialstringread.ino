int bufferIndex = 0;

void setup()
{
  Serial.begin(9600);
}

void loop()
{
  char* line = readLine();
  if(line != NULL) {
    Serial.println(line); 
  }
}

char* readLine() {
  char buffer[255];
  int finished = 0;
  for(int i = 0; i < Serial.available(); i++ ) {
    char signal = Serial.read();
    buffer[bufferIndex] = signal;
    if(signal == '}') {
      buffer[bufferIndex + 1] = '\0';
      bufferIndex = 0; 
      finished = 1; 
    } else {
      bufferIndex++;
    }
  }
  if(finished) {
    return buffer;
  } else {
    return NULL;
  }  
}
