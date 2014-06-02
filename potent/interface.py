import serial


def main():
    serdev = '/dev/ttyACM0'
    s = serial.Serial(serdev)
    s.write("hello")
    s.close()


if __name__ == '__main__':
    main()
