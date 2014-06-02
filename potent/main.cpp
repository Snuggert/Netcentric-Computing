#include "mbed.h"
#include "HBridge.h"

HBridge hbridge(p18,p15,p22);
DigitalOut led1(LED1);
DigitalOut led2(LED2);
DigitalOut led3(LED3);
DigitalOut led4(LED4);
AnalogIn ain(p17);

int main() {
	hbridge.speed(0.7);
    hbridge.power(true);
    while (1){
        led1 = (ain > 0.2) ? 1 : 0;
        led2 = (ain > 0.4) ? 1 : 0;
        led3 = (ain > 0.6) ? 1 : 0;
        led4 = (ain > 0.8) ? 1 : 0;
    }
}
