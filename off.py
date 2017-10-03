
import RPi.GPIO as GPIO

GPIO.setmode(GPIO.BCM)
 
PIN = 26


GPIO.setwarnings(False)
GPIO.setup(PIN, GPIO.OUT)
GPIO.setup(PIN, 1)
