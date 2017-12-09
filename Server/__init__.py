from flask import Flask, request, flash

from dbconnect import connection
from MySQLdb import escape_string as thwart

app = Flask(__name__)


@app.route('/')
def homepage():
    return "Light web application for subbmiting data from arduino."


@app.route('/gps/', methods=['GET'])
def gps_page():
    try:
        if request.method == "GET":
            user = request.args.get('user')
            password = request.args.get('password')
            db = request.args.get('db')
            lat = request.args.get('lat')
            lng = request.args.get('lng')
            if user == "your_login" and password == "your_password":
                try:
                    c, conn = connection()
                    dbase = db
                    c.execute("INSERT INTO " + dbase +
                              " (lat, lng) VALUES (%s, %s)", (thwart(lat), thwart(lng)))
                    conn.commit()
                    c.close()
                    conn.close()

                    gc.collect()
                    return "INSERTED"
                except Exception:
                    return "ACCESS GRANTED, BUT NOT CONNECTED"
            else:
                return "ACCESS DENIED"
        return "PLEASE, POST SOME GPS COORDINATES"
