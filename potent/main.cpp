#include "mbed/mbed.h"
#include "HBridge.h"

#include <string>

HBridge hbridge(p18,p15,p22);
PwmOut led1(LED1);
PwmOut led2(LED2);
PwmOut led3(LED3);
PwmOut led4(LED4);
AnalogIn ain(p17);
Serial pc(USBTX, USBRX);

int main() {
    float speed;
    int pos = 5;
    int command_active = 1;
    int end_reached_counter = 0;
    int buf_pos = 0;
    char buf[256];
    char *pend;

    PwmOut *leds[4] = {&led1, &led2, &led3, &led4};

    float voltages[11] = {0.0, 0.008, 0.0325, 0.0975, 0.163, 0.2315, 0.3066,
         0.4529, 0.7941, 0.999, 1.1};

    pend = (char*)malloc(sizeof(char));
    hbridge.power(true);
    while (1) {
        int current_pos = 0;
        for (int i = 1; i < 11; i++) {
            if (ain < voltages[i]) {
                current_pos = i - 1;
                break;
            }
        }

        for (int i = 0; i < 4; i++) {
            if (current_pos/2 - i > 0) {
                *leds[i] = 1;
            } else if (current_pos/2.0 - i == 0.5) {
                *leds[i] = 0.1;
            } else {
                *leds[i] = 0;
            }
        }

        if (command_active) {
            if (ain.read() < voltages[pos] - 0.0005) {
                speed = 1.0;
                end_reached_counter = 0;

            } else if (ain.read() > voltages[pos] + 0.0005) {
                speed = -1.0;
                end_reached_counter = 0;

            } else if (end_reached_counter == 3) {
                command_active = 0;
                pc.printf("Reached position %d\n\r", pos);

            } else {
                speed = 0.0;
                end_reached_counter++;
            }
            hbridge.speed(speed);
        }

        if (pc.readable()) {
            char c = pc.getc();

            if (c != '\r') {
                buf[buf_pos] = c;
                buf_pos++;

                pc.putc(c);
            } else {

                buf[buf_pos] = '\0';
                buf_pos = 0;

                pos = (int) strtof(buf, &pend);

                if (pos > 10) {
                    pos = 10;
                }


                pc.printf("\rMoving to position %d\n\r", pos);
                command_active = 1;
            }
        }
    }
}
