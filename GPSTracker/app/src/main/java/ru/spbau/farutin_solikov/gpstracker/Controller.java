package ru.spbau.farutin_solikov.gpstracker;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Controller {
	private static final String url = "jdbc:mysql://146.185.144.144:3306/gps?autoReconnect=true&useSSL=false";
	private static final String user = "android";
	private static final String password = "GPSTracker-MySQL123";
	
	private static Connection con;
	private static Statement stmt = null;
	private static PreparedStatement preparedStatement = null;
	private static ResultSet rs;
	
	public static void startCoordinatesService(Context context) {
		CoordinatesService.enqueueWork(context, new Intent());
	}
	
	public static void stopCoordinatesService() {
		CoordinatesService.stop();
	}
		
	public static ArrayList<Coordinate> fetchCoordinates(int id) {
		ArrayList<Coordinate> coordinates = new ArrayList<>();
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			
			con = DriverManager.getConnection(url, user, password);
			String sql = "SELECT * FROM coordinates where id > ?";
			
			preparedStatement = con.prepareStatement(sql);
			preparedStatement.setInt(1, id);
			rs = preparedStatement.executeQuery();
			
			double lat;
			double lng;
			int coordinate_id;
			while (rs.next()) {
				lat = rs.getDouble("lat");
				lng = rs.getDouble("lng");
				coordinate_id = rs.getInt("id");
				coordinates.add(new Coordinate(lat, lng, coordinate_id));
				
				// demo only
				break;
			}
		} catch (SQLException | ClassNotFoundException sqlEx) {
			sqlEx.printStackTrace();
		} finally {
			try {
				con.close();
			} catch (SQLException ignored) {
			}
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException ignored) {
			}
			try {
				if (preparedStatement != null) {
					preparedStatement.close();
				}
			} catch (SQLException ignored) {
			}
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException ignored) {
			}
		}
		
		return coordinates;
	}
	
	public static void sendCoordinates(ArrayList<Coordinate> route, String name) {
		// send to the database
	}
	
	public static class Coordinate implements Parcelable {
		private double lat;
		private double lng;
		private int id;
		
		Coordinate(double x, double y, int id) {
			lat = x;
			lng = y;
			this.id = id;
		}
		
		public double getLat() {
			return lat;
		}
		
		public double getLng() {
			return lng;
		}
		
		public int getId() {
			return id;
		}
		
		@Override
		public int describeContents() {
			return 0;
		}
		
		@Override
		public void writeToParcel(Parcel parcel, int i) {
			parcel.writeDouble(lat);
			parcel.writeDouble(lng);
			parcel.writeInt(id);
		}
		
		public static final Parcelable.Creator<Coordinate> CREATOR
				= new Parcelable.Creator<Coordinate>() {
			public Coordinate createFromParcel(Parcel in) {
				return new Coordinate(in);
			}
			
			public Coordinate[] newArray(int size) {
				return new Coordinate[size];
			}
		};
		
		private Coordinate(Parcel in) {
			lat = in.readDouble();
			lng = in.readDouble();
			id = in.readInt();
		}
	}
}
