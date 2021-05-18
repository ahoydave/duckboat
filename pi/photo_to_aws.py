import datetime
timestamp = datetime.datetime.now().isoformat().split('.')[0].replace(':','')
filepath = "nest" + timestamp + ".jpg"

import picamera
with picamera.PiCamera(resolution=(640,480)) as camera:
  camera.vflip = True
  camera.hflip = True
  camera.capture(filepath)

import boto3
s3_client = boto3.client('s3')
response = s3_client.upload_file(filepath, "duck-nest-pics", filepath)

import os
os.remove(filepath)

rek_client = boto3.client('rekognition')
image_descrip = {
        'S3Object': {
            'Bucket': 'duck-nest-pics',
            'Name': filepath
            }
        }
response = rek_client.detect_labels(Image=image_descrip, MaxLabels=10)

def duck_there(label):
    return label['Name'] == 'Bird' and label['Confidence'] > 90

bird_count = len(list(filter(duck_there, response['Labels'])))

import time # not sure if I can just use datetime here?
table = boto3.resource('dynamodb').Table('SensorReadings')
item = {
        'deviceID': 'camera_rek_1',
        'timestamp': str(time.time()),
        'description': 'Rekognition labelling camera 1 as seeing a bird',
        'value' : bird_count
        }
table.put_item(Item=item)
