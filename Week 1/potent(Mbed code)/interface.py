import serial


def main():
    serdev = '/dev/ttyACM0'
    s = serial.Serial(serdev)
    while(1):
        s.write(raw_input("Stand?:"))
        try:
            print("Stand:", s.readline())
        except Exception, e:
            raise
    s.close()


if __name__ == '__main__':
    main()
