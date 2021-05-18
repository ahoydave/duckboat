#!/bin/bash

killall ffmpeg
sleep 1
/usr/bin/python3 /home/pi/duckboat/photo_to_aws.py
/home/pi/duckboat/picam-stream.sh
