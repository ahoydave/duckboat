# duckboat
Watching ducks on a boat making more ducks

This folder contains various scripts that run on a raspberry pi. And are used to monitor the ducks.

`photo_to_aws.py` - take a photo of the nest and then use aws rekognition image classifier to try to record whether the momma duck was on the nest. Result is saved to DynamoDB as a sensor reading

`picam-stream.sh` - stream video of the nest to twitch

`process_old_photos.py` - a once off batch processing of photos of the nest stored locally on the pi

`read_temp.py` - take a temperature reading and save it to DynamoDB

`take_photo.sh` - take a photo of the nest

`text_overlay` - part of an attempt to overlay text onto the video. The extra work re-encoding the video on the pi's processor was too slow for this to be viable (tried it on a pi zero)
