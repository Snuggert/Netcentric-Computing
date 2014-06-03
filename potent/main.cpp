#include "mbed.h"
#include "HBridge.h"

#include <string>

HBridge hbridge(p18,p15,p22);
DigitalOut led1(LED1);
DigitalOut led2(LED2);
DigitalOut led3(LED3);
DigitalOut led4(LED4);
AnalogIn ain(p17);
Serial pc(USBTX, USBRX);

int main() {
    float speed, stand = 0.5;
    char buf[256];
    char *pend;
    int i;
    char c;

    pend = (char*)malloc(sizeof(char));
    hbridge.power(true);
    while (1){
        led1 = (ain > 0.2) ? 1 : 0;
        led2 = (ain > 0.4) ? 1 : 0;
        led3 = (ain > 0.6) ? 1 : 0;
        led4 = (ain > 0.8) ? 1 : 0;

        if(ain< stand - 0.05){
            speed = 1.0;
        }else if(ain > stand + 0.05){
            speed = -1.0;
        }else{
            speed = 0.0;
        }
        hbridge.speed(speed);

        if(pc.readable()){
            for (i = 0; i < 255 && (c = pc.getc()) != '\r'; i++) {
                buf[i] = c;
                pc.putc(buf[i]);
            }
            buf[i] = '\0';
            stand = strtof(buf, &pend);

            pc.printf("\n\rStand: %f\n\r", stand);
        }
    }
}
