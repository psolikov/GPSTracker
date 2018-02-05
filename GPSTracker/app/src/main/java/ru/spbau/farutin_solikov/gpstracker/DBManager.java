package ru.spbau.farutin_solikov.gpstracker;

import android.util.Log;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for interaction with the database.
 */
public class DBManager {
	private static final String TAG = "DBManager";
	
	private static final String driver = "com.mysql.jdbc.Driver";
	private static final String url = "jdbc:mysql://146.185.144.144:3306/gps?autoReconnect=true&useSSL=false";
	// На всякий случай напишу, что так делать не надо, но для прототипа не критично.
	private static final String user = "android";
	private static final String password = "GPSTracker-MySQL123";

	/**
	 * Fetches new coordinates from the database.
	 *
	 * @param id id of the coordinate after which should take new coordinates
	 * @return new coordinates
	 */
	// Лучше возвращать List<T>, а ингда даже Collection<T>
	// (*) почему?
	//
	// Более общий тип, чтобы методы, использующие возвращаемое значение,
	// меньше зависели от реализации данного метода.
	public static List<Coordinate> fetchCoordinates(int id) {
		Connection con = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		ArrayList<Coordinate> coordinates = new ArrayList<>();

		try {
			Class.forName(driver);

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
		} catch (SQLException | ClassNotFoundException e) {
			// лучше логировать https://developer.android.com/reference/android/util/Log.html
			// fixed
			Log.w(TAG, e.getMessage());
		} finally {
			// было бы логичнее/правильнее закрывать ресурсы в обратном порядке
			// (*) почему?
			//
			// Ресурсы, созданные позже, могут зависеть от ресурсов, созданных раньше.
			try {
				if (rs != null) {
					rs.close();
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
				if (con != null) {
					con.close();
				}
			} catch (SQLException ignored) {
			}
		}
		
		return coordinates;
	}
	
	/**
	 * Deletes all rows from table.
	 */
	public static void clearTable() {
		try {
			Class.forName(driver);
			
			Connection con = DriverManager.getConnection(url, user, password);
			
			try {
				Statement stmt = con.createStatement();
				String query = "DELETE FROM coordinates";
				stmt.executeUpdate(query);
			} catch (SQLException s) {
				s.printStackTrace();
			}
			
			con.close();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Saves route to the database.
	 *
	 * @param route route to save
	 * @param name  route name
	 */
	public static void sendCoordinates(ArrayList<Coordinate> route, String name) {
		Connection con = null;
		Statement stmt = null;
		
		try {
			Class.forName(driver);
			
			con = DriverManager.getConnection(url, user, password);
			
			stmt = con.createStatement();
			DatabaseMetaData dbm = con.getMetaData();
			ResultSet rs = dbm.getTables(null, null, name, null);

			// странное решение с созданием отдельной таблицы для каждого route
			if (!rs.next()) {
				stmt = con.createStatement();
				String sql = "create table "
						+ name + " "
						+ "like coordinates";
				stmt.executeUpdate(sql);
			}
			
			for (Coordinate coordinate : route) {
				String sql = "INSERT INTO "
						+ name
						+ " (lat, lng) VALUES ("
						+ String.valueOf(coordinate.getLat()) + ", "
						+ String.valueOf(coordinate.getLat()) + ")";
				stmt.executeUpdate(sql);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null)
					con.close();
			} catch (SQLException ignored) {
			}
			try {
				if (con != null)
					con.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
	}
	
	/**
	 * Checks whether id is correct or not.
	 *
	 * @param deviceId input id
	 * @return true if id exists, false otherwise
	 */
	// возможно стоит переименовать, например в isExistingDeviceId или isValidDeviceId
	// fixed
	@SuppressWarnings("SameReturnValue")
	public static boolean isValidDeviceId(@SuppressWarnings("UnusedParameters") String deviceId) {
		// TODO: check id in the database
		return true;
	}
}
