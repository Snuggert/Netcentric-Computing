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

const int N_PREV_STATES = 12;

struct led_wrapper {
    DigitalOut *led;
    int prev_states[N_PREV_STATES];
};

int main() {
    float speed;
    int pos = 5;
    char buf[256];
    char *pend;

    led_wrapper leds[4] = {0};
    leds[0].led = &led1;
    leds[1].led = &led2;
    leds[2].led = &led3;
    leds[3].led = &led4;

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
            for (int j = N_PREV_STATES - 1; j > 0; j--) {
                leds[i].prev_states[j] = leds[i].prev_states[j - 1];
            }

            leds[i].prev_states[0] = *leds[i].led;
            if (current_pos/2 - i > 0) {
                *leds[i].led = 1;
            } else if (current_pos/2.0 - i == 0.5) {
                int new_state = !*leds[i].led;

                for (int j = 0; j < N_PREV_STATES; j++) {
                    if (leds[i].prev_states[j]) {
                        new_state = 0;
                        break;
                    }
                }

                *leds[i].led = new_state;

            } else {
                *leds[i].led = 0;
            }
        }

        if (ain.read() < voltages[pos] - 0.0005) {
            speed = 1.0;
        } else if (ain.read() > voltages[pos] + 0.0005) {
            speed = -1.0;
        } else {
            speed = 0.0;
        }
        hbridge.speed(speed);

        if (pc.readable()) {
            char c;
            int i;

            for (i = 0; i < 255 && (c = pc.getc()) != '\r'; i++) {
                buf[i] = c;
                pc.putc(buf[i]);
            }
            buf[i] = '\0';
            pos = (int) strtof(buf, &pend);

            if (pos > 10) {
                pos = 10;
            }

            pc.printf("\n\rPosition: %d\n\r", pos);
        }
    }
}
