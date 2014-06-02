#include "mbed.h"

DigitalOut led1(LED1);
DigitalOut led2(LED2);
DigitalOut led3(LED3);
DigitalOut led4(LED4);
AnalogIn ain(p18);

int main() {
    while (1){
        led1 = (ain > 0.2) ? 1 : 0;
        led2 = (ain > 0.4) ? 1 : 0;
        led3 = (ain > 0.6) ? 1 : 0;
        led4 = (ain > 0.8) ? 1 : 0;
    }
}
