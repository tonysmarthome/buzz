
# A app to control Raspberry Pi GPIO

from flask import Flask
from flask import request




app = Flask(__name__)





@app.route('/', methods=['GET', 'POST'])
def home():
  return '''<h1>Home Page</h1>
  <p><a href="./light">Light Control</a>'''



@app.route('/light', methods=['GET'])
def light_form():
  return '''<form action="/light" method="post">
       <p>Red Light: <input type=checkbox name=light value="red" checked></p>
       <p>Green Light: <input type=checkbox name=light value="green" checked></p>
       <p>Yellow Light: <input type=checkbox name=light value="yellow" checked></p>
       <p><button type="submit">Submit</button></p>
       </form>
       <p><a href="../">Home</a>'''

@app.route('/light', methods=['POST'])
def light():
    l=request.form.getlist("light")


    s=''
    sg=''
    sr=''
    sy=''
    if 'green' in l:
    	sg='<p>Green Light is lighting!'
    if 'red' in l:
    	sr='<p>Red Light is lighting!'
    if 'yellow' in l:
    	sy='<p>Yellow Light is lighting!'

    if 'green' not in l:
    	sg='<p>Green Light is off!'
    if 'red' not in l:
    	sr='<p>Red Light is off!'
    if 'yellow' not in l:
    	sy='<p>Yellow Light is off!'
    s=sr+sg+sy
    return s


if __name__ == '__main__':
  app.run(host='0.0.0.0',port=5000,debug=True)