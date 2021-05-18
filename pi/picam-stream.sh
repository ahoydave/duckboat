#!/bin/bash

# =================================================================
# Stream configuration file for Raspberry Pi Camera
#
# @author Russell Feldhausen (russfeldh@gmail.com)
# @version 2019-06-05
#
# This set of commands should allow you to stream video from your 
# Raspberry Pi Camera to Twitch and Youtube (and possibly other
# RTMP endpoints) with decent quality and performance.
#
# You may need to install raspivid and/or ffmpeg to use this script.
# 
# This was tested and built on Raspbian 9 installed using Noobs
# =================================================================


# Set width and height of output video
WIDTH=640
HEIGHT=480

# Set output framerate
FRAMERATE=15

# Set keyframe spacing (must be double the framerate)
KEYFRAME=30

# Set bitrate (Twitch recommends 3500000)
# Can add this with -b $BITRATE for raspivid and -b:v $BITRATE for ffmpeg
BITRATE=350000
MAX_BIT_RATE=100000
RECORD_BITRATE=500000

# Set stream URL
URL=rtmp://lhr04.contribute.live-video.net/app

# Set stream key
KEY=live_510705574_TL8RhAFhsRzn6SvnXHnvVrIjuFric5

# The text overlay file
TEXT_FILE=~/duckboat/text_overlay

# -vf drawtext="textfile=$TEXT_FILE"
# -i anullsrc 
# Command
# raspivid -vf -n -t 0 -w $WIDTH -h $HEIGHT -b $BITRATE -fps $FRAMERATE -b $RECORD_BITRATE -g $KEYFRAME -o - | ffmpeg -f lavfi -i anullsrc -c:a aac -r $FRAMERATE -i - -g $KEYFRAME -strict experimental -threads 4 -vcodec copy -map 0:a -map 1:v -preset ultrafast -b:v $BITRATE -f flv "${URL}/${KEY}"
raspivid -vf -n -t 0 -w $WIDTH -h $HEIGHT -b $BITRATE -fps $FRAMERATE -b $RECORD_BITRATE -g $KEYFRAME -o - | ffmpeg -r $FRAMERATE -i - -g $KEYFRAME -strict experimental -threads 4 -codec:v copy -preset ultrafast -b:v $BITRATE -f flv "${URL}/${KEY}"

# =================================================================
# Full Documentation of Command Options
# 
# +++ raspivid +++
# -n = no preview window
# -t = time to capture (0 to disable, which allows streaming)
# -w = video width
# -h = video height
# -fps = output framerate (max 30 for 1080p, 60 for 720p)
# -b = bitrate
# -g = keyframe rate (refresh period)
# -o - = output to stdout (allows piping to ffmpeg)
#
# +++ ffmpeg +++
# -f lavfi = use lavfi filter (see note below)
# -i anullsrc = grab blank input (see note below)
# -c:a aac = set audio codec to aac
# -r = output framerate (should match raspivid framerate)
# -i - = read input from stdin (piped from ffmpeg)
# -g = keyframe rate (refresh period)
# -strict experimental = allow nonstandard things
# -threads 4 = set number of encoding threads to 4 (# of cores)
# -vcodec copy = use video as-is (do not re-encode video)
# -map 0:a = use the audio from input 0 (see note below)
# -map 1:v = use the video from input 1 (raspivid)
# -b:v = bitrate
# -preset ultrafast = use the ultrafast encoding preset
# -f flv = set output format to flv for streaming
# "${URL}/{KEY}" = specify RTMP URL as output for stream
#
# ** NOTE **
# According to some information online, YouTube will reject a live
# stream without an audio channel. So, in the ffmpeg command above
# a blank audio channel is included. It was not required for Twitch
# in my testing. 
# =================================================================
