import MySQLdb


def connection():
    conn = MySQLdb.connect(host="localhost",
                           user="root",
                           passwd="GPSTracker-MySQL123",
                           db="gps")
    c = conn.cursor()

    return c, conn
