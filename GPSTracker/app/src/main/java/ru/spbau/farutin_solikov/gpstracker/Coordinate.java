package ru.spbau.farutin_solikov.gpstracker;

import android.os.Parcel;
import android.os.Parcelable;

public class Coordinate implements Parcelable {
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
	
	public static final Creator<Coordinate> CREATOR
			= new Creator<Coordinate>() {
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
