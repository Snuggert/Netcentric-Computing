import serial


def main():
    serdev = '/dev/ttyACM0'
    s = serial.Serial(serdev)
    s.write("hello")
    while(1):
        try:
            print s.readline()
        except Exception, e:
            raise
    s.close()


if __name__ == '__main__':
    main()
