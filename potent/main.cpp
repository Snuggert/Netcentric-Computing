#include "mbed/mbed.h"
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
    float speed;
    int stand = 5;
    char buf[256];
    char *pend;
    int i;
    char c;

    float voltages[11] = {0.0, 0.008, 0.0325, 0.0975, 0.163, 0.2315, 0.3066,
         0.4529, 0.7941, 0.999, 1.1};

    pend = (char*)malloc(sizeof(char));
    hbridge.power(true);
    while (1) {
        led1 = (ain > voltages[2]) ? 1 : 0;
        led2 = (ain > voltages[4]) ? 1 : 0;
        led3 = (ain > voltages[6]) ? 1 : 0;
        led4 = (ain > voltages[8]) ? 1 : 0;

        if (ain< voltages[stand] - 0.0005) {
            speed = 1.0;
        } else if (ain > voltages[stand] + 0.0005) {
            speed = -1.0;
        } else {
            speed = 0.0;
        }
        hbridge.speed(speed);

        if (pc.readable()) {
            for (i = 0; i < 255 && (c = pc.getc()) != '\r'; i++) {
                buf[i] = c;
                pc.putc(buf[i]);
            }
            buf[i] = '\0';
            stand = (int) strtof(buf, &pend);

            if (stand > 10) {
                stand = 10;
            }

            pc.printf("\n\rStand: %d\n\r", stand);
        }
    }
}
