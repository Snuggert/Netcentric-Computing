#include "mbed/mbed.h"
#include "AndroidAccessory.h"
#include "HBridge.h"

//device setup

PwmOut led1(LED1);
PwmOut led2(LED2);
PwmOut led3(LED3);
PwmOut led4(LED4);
HBridge hbridge(p18,p15,p22);

DigitalOut ind(p21);

#define OUTL 100
#define INBL 100

class AdkTerm :public AndroidAccessory
{
public:
    AdkTerm():AndroidAccessory(INBL,OUTL,
                                   "ARM",
                                   "mbed",
                                   "mbed Terminal",
                                   "0.1",
                                   "http://www.mbed.org",
                                   "0000000012345678"),
    pc(USBTX,USBRX),
    ain(p17),
    pos(5),
    command_active(1),
    end_reached_counter(0),
    buf_pos(0),
    leds({&led1, &led2, &led3, &led4}),
    voltages {0.0, 0.005, 0.0215, 0.06, 0.104, 0.149, 0.199, 0.2985, 0.513,
        0.76, 0.998},
    precission {0.0005, 0.0005, 0.00075, 0.0015, 0.0025, 0.0025, 0.003, 0.0035,
        0.0035, 0.004, 0.001}
    {};
    virtual int callbackRead(u8 *buff, int len);
    virtual void setupDevice();
    virtual void resetDevice();
    virtual int callbackWrite();



private:
    void serialIRQ();
    void move(char* buff);
    void onTick();
    void AttachTick();
    char buffer[OUTL];
    int bcount;
    Serial pc;
    Ticker tick;
    AnalogIn ain;
    Timeout n;
    bool settick;
    float speed;
    float direction;
    int pos;
    int command_active;
    int end_reached_counter;
    int buf_pos;
    int BUF_SIZE;
    char buf[BUF_SIZE];
    char *pend;

    PwmOut *leds[4];

    float voltages[11];
    float precission[11];

};



void AdkTerm::setupDevice()
{
    pc.baud(9600);
    pc.printf("Welcome to adkTerm (MbedSketch)\n\r");
    settick = false;
    pc.attach(this, &AdkTerm::serialIRQ, Serial::RxIrq);
    for (int i = 0; i<OUTL; i++) {
        buffer[i] = 0;
    }

    bcount = 0;
    //n.attach(this,&AdkTerm::AttachTick,5);
    //tick.attach(this,&AdkTerm::onTick,0.1);
    pend = (char*)malloc(sizeof(char));
    hbridge.power(true);

}

void AdkTerm::AttachTick()
{
    if(!settick)tick.attach(this,&AdkTerm::onTick,0.04);
    settick = true;
}

void AdkTerm::onTick()
{
    u8 wbuf[4];
    int current_pos = 0;
    float current_pos_f = 0.0;

    for (int i = 1; i < 11; i++) {
        if (ain < voltages[i] - precission[i] * 2) {
            current_pos = i - 1;
            current_pos_f = current_pos + (ain - voltages[i-1]) / (voltages[i] -
                voltages[i-1]);
            break;
        }
    }

    for (int i = 0; i < 4; i++) {
        if (current_pos/2 - i > 0) {
            *leds[i]= 1;
        } else if (current_pos/2.0 - i == 0.5) {
            *leds[i] = 0.1;
        } else {
            *leds[i] = 0;
        }
    }

    if (command_active) {
        if (ain.read() < voltages[pos] - precission[pos]) {
            if (direction == -1) {
                speed /= 2;
            }
            direction = 1;
            end_reached_counter = 0;

        } else if (ain.read() > voltages[pos] + precission[pos]) {
            if (direction == 1) {
                speed /= 2;
            }
            direction = -1;
            end_reached_counter = 0;

        } else if (end_reached_counter == 8) {
            command_active = 0;
            pc.printf("Reached position %d\n\r", pos);

        } else {
            direction = 0;
            end_reached_counter++;
        }
        hbridge.speed(speed * direction);
    }

    int stream = (int) (current_pos_f * 100);

    wbuf[0] = 'P';
    wbuf[1] = stream&0xFF;
    wbuf[2] = (stream>>8) & 0xFF;
    wbuf[3] = 0;

    this->write(wbuf, 3);
}

void AdkTerm::resetDevice()
{
    pc.printf("adkTerm reset\n\r");
    for (int i = 0; i<OUTL; i++) {
        buffer[i] = 0;
    }
    bcount = 0;
}

int AdkTerm::callbackRead(u8 *buf, int len)
{
    pc.printf("%i  %s\n\r\n\n\n",len,buf);
    move((char*)buf);
    for (int i = 0; i<INBL; i++) {
        buf[i] = 0;
    }

    AttachTick();

    return 0;
}

int AdkTerm::callbackWrite()
{

    ind = false;
    return 0;
}


void AdkTerm::serialIRQ()
{
    char c = pc.getc();

    if (c != '\r' && buf_pos < BUF_SIZE) {
        buffer[buf_pos] = c;
        buf_pos++;

        pc.putc(c);
    } else {

        buffer[buf_pos] = '\0';
        buf_pos = 0;

        move(buffer);
    }

}

void AdkTerm::move(char* buff) {
    pos = (int) strtof(buff, &pend);

    if (pos > 10) {
        pos = 10;
    }


    pc.printf("\rMoving to position %d\n\r", pos);
    command_active = 1;
    direction = 0;
    speed = 1;
}

AdkTerm AdkTerm;

int main()
{

    AdkTerm.setupDevice();
    printf("Android Development Kit: start\r\n");
    USBInit();
    while (1) {
        USBLoop();
    }
}
