#include "mbed.h"
#include "HBridge.h"

HBridge hbridge(p18,p15,p22);
DigitalOut led1(LED1);
DigitalOut led2(LED2);
DigitalOut led3(LED3);
DigitalOut led4(LED4);
AnalogIn ain(p17);
Serial pc(USBTX, USBRX);

int main() {
    float speed;
    char buf[256];

    sp.baud(19200);

    hbridge.power(true);
    while (1){
        led1 = (ain > 0.2) ? 1 : 0;
        led2 = (ain > 0.4) ? 1 : 0;
        led3 = (ain > 0.6) ? 1 : 0;
        led4 = (ain > 0.8) ? 1 : 0;

        if(ain<0.45){
            speed = 1.0;
        }else if(ain > 0.55){
            speed = -1.0;
        }else{
            speed = 0.0;
        }
        hbridge.speed(speed);

        pc.printf("Serial serial serial killah\n");
        /*
        if (pc.readable()){
            led4 = 1;
            pc.gets(buf, 256);
            pc.printf("Received: %s\n", buf);
        }
        */
    }
}
