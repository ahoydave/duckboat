import datetime
timestamp = datetime.datetime.now().isoformat().split('.')[0].replace(':','')
filepath = "nest" + timestamp + ".jpg"

import decimal

import glob
filenames = glob.glob('nest_photos/*')

import boto3
s3_client = boto3.client('s3')
rek_client = boto3.client('rekognition')
table = boto3.resource('dynamodb').Table('SensorReadings')

for filepath in filenames:
    fne = filepath.split('/')[1]
    fn = fne.split('.')[0]
    ts = datetime.datetime.fromisoformat(fn[:-5] + 'T' + fn[-4:-2] + ':' + fn[-2:] + ':00').timestamp()

    response = s3_client.upload_file(filepath, "duck-nest-pics", fne)
    print("added " + filepath + " to s3")
    image_descrip = {
            'S3Object': {
                'Bucket': 'duck-nest-pics',
                'Name': fne
                }
            }
    response = rek_client.detect_labels(Image=image_descrip, MaxLabels=10)
    bird_count = len(list(filter(lambda x: x['Name'] == 'Bird', response['Labels'])))
    print("got bird count " + str(bird_count))
    item = {
            'deviceID': 'camera_rek_1',
            'timestamp': str(ts),
            'description': 'Rekognition labelling camera 1 as seeing a bird',
            'value' : bird_count
            }
    table.put_item(Item=item)
    print("reading sent")
