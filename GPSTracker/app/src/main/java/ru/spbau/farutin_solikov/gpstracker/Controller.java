package ru.spbau.farutin_solikov.gpstracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static android.content.Context.MODE_PRIVATE;

/**
 * Class with supporting methods.
 */
public class Controller {
	/**
	 * SharedPreferences filename.
	 */
	public static final String PREF_FILE = "prefs";
	
	private static final String url = "jdbc:mysql://146.185.144.144:3306/gps?autoReconnect=true&useSSL=false";
	private static final String user = "android";
	
	private static final String password = "GPSTracker-MySQL123";
	private static Connection con;
	private static Statement stmt = null;
	private static PreparedStatement preparedStatement = null;
	
	private static ResultSet rs;
	
	/**
	 * Starts CoordinateService.
	 * @param context context to enqueue with
	 */
	public static void startCoordinatesService(Context context) {
		CoordinatesService.enqueueWork(context, new Intent());
	}
	
	/**
	 * Stops CoordinateService.
	 */
	public static void stopCoordinatesService() {
		CoordinatesService.stop();
	}
	
	/**
	 * Fetches new coordinates from the database.
	 * @param id id of the coordinate after which should take new coordinates
	 * @return new coordinates
	 */
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
				
				// TODO: remove, demo only
				break;
			}
		} catch (SQLException | ClassNotFoundException sqlEx) {
			sqlEx.printStackTrace();
		} finally {
			try {
				if (con != null) {
					con.close();
				}
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
	
	/**
	 * Saves route to the database.
	 * @param route route to save
	 * @param name route name
	 */
	public static void sendCoordinates(ArrayList<Coordinate> route, String name) {
		// TODO: send to the database
	}
	
	/**
	 * Checks whether id is correct or not.
	 * @param deviceId input id
	 * @return true if id has correct format and exists, false otherwise
	 */
	public static boolean checkDeviceId(String deviceId) {
		if (deviceId.length() % 5 != 4) {
			return false;
		}
		
		// TODO: check id in the database
		return true;
	}
	
	/**
	 * Checks whether user has already logged in or not.
	 * @param context context with SharedPreferences
	 * @return true if user has already logged in, false otherwise
	 */
	public static boolean userLoggedIn(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE, MODE_PRIVATE);
		return sharedPreferences.getString(context.getString(R.string.preference_user_id), "").length() > 0;
	}
	
	/**
	 * Checks whether notifications are turn on or not.
	 * @param context context with SharedPreferences
	 * @return true if notifications are turn on, false otherwise
	 * @return
	 */
	public static boolean notificationsOn(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE, MODE_PRIVATE);
		return sharedPreferences.getString(context.getString(R.string.preference_notifications_new_message), "true").equals("true");
	}
	
	/**
	 * Saves device id.
	 * @param context context with SharedPreferences
	 * @param deviceId id to save
	 */
	public static void saveUserDeviceId(Context context, String deviceId) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE, MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(context.getString(R.string.preference_user_id), deviceId);
		editor.apply();
	}
	
	/**
	 * Makes snapshot of map with route and builds intent with it to send.
	 * @param instance activity from which snapshot is sending
	 * @param map map to send
	 * @param routeName route name
	 */
	public static void sendSnapshot(final RouteActivity instance, GoogleMap map, final String routeName) {
		GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
			Bitmap bitmap;
			
			@Override
			public void onSnapshotReady(Bitmap snapshot) {
				try {
					bitmap = snapshot;
					
					File outputDir = new File(
							Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
							instance.getString(R.string.app_name));
					
					if (!outputDir.exists()) {
						outputDir.mkdir();
					}
					
					File outputFile = new File(outputDir, routeName + ".png");
					FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
					
					bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
					MediaScannerConnection.scanFile(instance,
							new String[] { outputFile.getPath() },
							new String[] { "image/png" },
							null);
					
					Uri attachment = Uri.fromFile(outputFile);
					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_SEND);
					intent.setType("image/*");
					intent.putExtra(Intent.EXTRA_STREAM, attachment);
					
					instance.startActivity(Intent.createChooser(intent, instance.getString(R.string.title_chooser)));
				} catch (IOException e) {
					Toast.makeText(instance, instance.getString(R.string.toast_route) + e.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		};
		
		map.snapshot(callback);
	}
	
	/**
	 * Class to receive coordinates broadcasted by CoordinatorService.
	 */
	private static class CoordinatesReceiver extends BroadcastReceiver {
		ArrayList<Coordinate> coordinates = null;
		
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle notificationData = intent.getExtras();
			coordinates = notificationData.getParcelableArrayList(context.getString(R.string.extra_coordinates));
		}
	}
	
	/**
	 * Once coordinates are received, notifies AlarmActivity.
	 */
	public static class AlarmCoordinatesReceiver extends CoordinatesReceiver {
		private AlarmActivity alarmActivityInstance = null;
		
		public AlarmCoordinatesReceiver(AlarmActivity instance) {
			alarmActivityInstance = instance;
		}
		
		@Override
		public void onReceive(Context context, Intent intent) {
			stopCoordinatesService();
			
			try {
				alarmActivityInstance.unregisterReceiver(this);
			} catch (IllegalArgumentException ignored) {
				// no API methods to tell if it is registered at the moment
			}
			
			super.onReceive(context, intent);
			alarmActivityInstance.positionChanged(coordinates.get(coordinates.size() - 1));
		}
	}
	
	/**
	 * Once coordinates are received, draws route in TrackerActivity.
	 */
	public static class TrackerCoordinatesReceiver extends CoordinatesReceiver {
		private TrackerActivity trackerActivityInstance = null;
		
		public TrackerCoordinatesReceiver(TrackerActivity instance) {
			trackerActivityInstance = instance;
		}
		
		@Override
		public void onReceive(Context context, Intent intent) {
			super.onReceive(context, intent);
			trackerActivityInstance.drawRoute(coordinates);
		}
	}
}
