#coding:utf-8

from picamera.array import PiRGBArray
from picamera import PiCamera

import httplib, urllib
import requests
import RPi.GPIO as gpio
import time
import pandas as pd
import cv2
import os

gpio.setmode(gpio.BCM)
trig = 18
echo = 15
gpio.setup(trig, gpio.OUT)
gpio.setup(echo, gpio.IN)
address = ''
temperature = 0

def handle_time():

    try:
        while True:
            time_list = pd.read_csv("05712c7b058474e8f0e652e7256d781d/20160419T154644.log.csv")
            time_list.shape
            time_list

            start_list =[] # C1
            running_list =[] #C2
            for t in time_list.iterrows() :
                start_list.append(t[1]['c_1'])
                running_list.append(t[1]['c_2'])

            start_hour = int(start_list[0].split(' ')[1].split(':')[0])
            start_minit = int(start_list[0].split(' ')[1].split(':')[1])
            start_second = int(start_list[0].split(' ')[1].split(':')[2])

            start_second_total = 60*60*start_hour + 60*start_minit + start_second
            end_second_total = start_second_total + running_list[len(running_list)-1]

            current = time.localtime()
            current_hour = current.tm_hour
            current_minit = current.tm_min
            current_second = current.tm_sec

            current_second_total = current_hour*60*60 + current_minit*60 + current_second

            time_second_total = current_second_total - end_second_total
            time_hour = time_second_total/60/60
            time_minit = (time_second_total - time_hour*60*60)/60
            time_second = time_second_total - time_minit*60 - time_hour*60*60

            print "empty car during" + str(time_hour) + ":" + str(time_minit) + ":" + str(time_second)

            if time_second_total > 300 or time_second_total < 300:
                print time_second_total
                return
            
    except KeyboardInterrupt :
        print "False"
        gpio.cleanup()

def handle_address():
    global address
    
    time_list = pd.read_csv("05712c7b058474e8f0e652e7256d781d/20160419T154644.log.csv")
    time_list.shape
    time_list

    address_list = []
    for t in time_list.iterrows() :
        address_list.append(t[1]['c_15'])

    arr = len(address_list) - 10
    address = address_list[arr]
    while address == 'nan':
        arr = int(arr)-1
        addresss = address_list[int(arr)]

    if address == '북구' or address == '동구':
        address = '서초구'

def handle_video():
    os.system('sudo modprobe bcm2835-v4l2')
    cap = cv2.VideoCapture(0)
    cap.set(3, 1024)
    cap.set(4, 768)
    
    ret, frame = cap.read()
    if not ret:
        print('Not Found Devices')
        
    cv2.imwrite('detect_img.png', frame)
    print "Capture Picam"
    
    cap.release()
    cv2.destroyAllWindows()

def post():

    files = {'photo' : open('/home/pi/workspace/detect_img.png', 'rb')}
    requests.post("http://52.78.88.51:8080/SaveUsServer/insert.do", files=files)
    print "\nSend image"

def post2() :
    global address 
    params = urllib.urlencode( {'address' : address })
    
    headers = {"Content-type" :"application/x-www-form-urlencoded"}
    conn = httplib.HTTPConnection("52.78.88.51:8080")
    conn.request("POST", "/SaveUsServer/dataInsert.do", params, headers)
    response = conn.getresponse()
    data = response.read()
    print data
    print address
    conn.close()

def handle_ultra():
    
    MAX_COUNT = 2
    
    count = 0    
    distance_com = 0
    try :
        while True :
            gpio.output(trig, False)
            time.sleep(0.5)

            gpio.output(trig, True)
            time.sleep(0.00001)
            gpio.output(trig, False)

            while gpio.input(echo) == 0 :
                pulse_start = time.time()

            while gpio.input(echo) == 1 :
                pulse_end = time.time()

            pulse_duration = pulse_end - pulse_start
            distance = pulse_duration * 17000
            distance = round(distance, 2)

            if count < MAX_COUNT :
                distance_dif = distance - distance_com
                distance_com = distance

                print "Distance : ", distance, "cm"
                print "Distance_dif : ", distance_dif, "cm"

            else :
                break

            if distance_dif > 5 or distance_dif < -5:
                count += 1
                print count
                if count == MAX_COUNT :                            
                    handle_video()
                    

    except KeyboardInterrupt :
        print "False"
        gpio.cleanup()
    

    
if __name__ == '__main__':
    handle_time()
    handle_ultra()
    handle_address()
    post()
    post2()




