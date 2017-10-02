
# A app to control Raspberry Pi GPIO

from flask import Flask
from flask import request
import RPi.GPIO as GPIO
import time


app = Flask(__name__)



GPIO.setmode(GPIO.BCM)
 

BUZZ = 26
HIGH=1
LOW=0

GPIO.setwarnings(False)
GPIO.setup(BUZZ, GPIO.OUT)


GPIO.output(BUZZ, LOW)

time.sleep(3)

GPIO.output(BUZZ, HIGH)

isBuzz=False

@app.route('/', methods=['GET', 'POST'])
def home():
  return '''<h1>Home Page</h1>
  <p><a href="./buzz">Buzz Control</a>'''



@app.route('/buzz', methods=['GET'])
def buzz_form():
    global isBuzz
    if isBuzz==False:
        return '''<form action="/buzz" method="post">
       <p><button type="submit">Buzz</button></p>
       </form>
       <p><a href="../">Home</a>'''
    else:
        return '''<form action="/buzz" method="post">
       <p><button type="submit">UnBuzz</button></p>
       </form>
       <p><a href="../">Home</a>'''

@app.route('/buzz', methods=['POST'])
def buzz():
    global isBuzz
    isBuzz = not isBuzz
    if isBuzz==True:
        GPIO.output(BUZZ, LOW)
    	return '''<form action="/buzz" method="post">
       <p>Buzzing</p>
       <p><button type="submit">Unbuzz</button></p>
       </form>
       <p><a href="../">Home</a>'''
    else:
    	GPIO.output(BUZZ,HIGH)
    	return '''<form action="/buzz" method="post">
       <p>UnBuzzing</p>
       <p><button type="submit">Buzz</button></p>
       </form>
       <p><a href="../">Home</a>'''
    


if __name__ == '__main__':
  app.run(host='0.0.0.0',port=5000,debug=True)
