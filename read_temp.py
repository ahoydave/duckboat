import RPi.GPIO as GPIO
import time
import glob
import os
import boto3

GPIO.setmode(GPIO.BCM)
GPIO.setup(4, GPIO.IN, pull_up_down=GPIO.PUD_UP)
GPIO.setup(23, GPIO.IN, pull_up_down=GPIO.PUD_UP)
#os.system('modprobe w1-gpio')
#os.system('modprobe w1-therm')

def get_reading(filename):
    f = open(filename, 'r')
    data = f.read()
    f.close()
    (discard, sep, reading) = data.partition(' t=')
    return reading.strip()

def get_filenames():
    dirs = glob.glob('/sys/bus/w1/devices/28*')
    add_slave_str = lambda s : s + '/w1_slave'
    return map(add_slave_str, dirs)

def save_readings(readings):
    table = boto3.resource('dynamodb').Table('SensorReadings')
    for [deviceId, reading] in readings:
        item = {
                 'deviceID': deviceId,
                 'timestamp': str(time.time()),
                 'description': 'Temp in 1/1000 deg C +- .5 deg',
                 'value': reading
               }
        print('Adding item ' + str(item))
        table.put_item(Item=item)

filenames = get_filenames()
print(str(time.time()) + '\n')
readings = map(lambda filename : [filename, get_reading(filename)], filenames)
print(readings)
save_readings(readings)
