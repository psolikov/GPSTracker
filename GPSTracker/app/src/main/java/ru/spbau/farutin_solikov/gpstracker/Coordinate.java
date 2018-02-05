package ru.spbau.farutin_solikov.gpstracker;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class for storing map coordinates.
 */
public class Coordinate implements Parcelable {
	private final double lat;
	private final double lng;
	private final int id;
	
	Coordinate(double lat, double lng, int id) {
		// [minor] неконсистентно: `this.foo`; `foo`
		// почему то `x` то `lat`?
		// fixed
		this.lat = lat;
		this.lng = lng;
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
