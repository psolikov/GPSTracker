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

import static android.content.Context.MODE_PRIVATE;

/**
 * Class with supporting methods.
 */
public class Controller {
    /**
     * SharedPreferences filename.
     */
    public static final String PREF_FILE = "prefs";
    
    /**
     * Starts CoordinateService.
     *
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
     *
     * @param id id of the coordinate after which should take new coordinates
     * @return new coordinates
     */
    public static ArrayList<Coordinate> fetchCoordinates(int id) {
        return DBManager.fetchCoordinates(id);
    }

    /**
     * Deletes all rows from table.
     */
    public static void clearTable() {
        DBManager.clearTable();
    }

    /**
     * Saves route to the database.
     *
     * @param route route to save
     * @param name  route name
     */
    public static void sendCoordinates(ArrayList<Coordinate> route, String name) {
        DBManager.sendCoordinates(route, name);
    }

    /**
     * Checks whether id is correct or not.
     *
     * @param deviceId input id
     * @return true if id has correct format and exists, false otherwise
     */
    public static boolean checkDeviceId(String deviceId) {
        return deviceId.length() % 5 == 4 && DBManager.checkDeviceId(deviceId);
    }

    /**
     * Checks whether user has already logged in or not.
     *
     * @param context context with SharedPreferences
     * @return true if user has already logged in, false otherwise
     */
    public static boolean userLoggedIn(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE, MODE_PRIVATE);
        return sharedPreferences.getString(context.getString(R.string.preference_user_id), "").length() > 0;
    }

    /**
     * Checks whether notifications are turn on or not.
     *
     * @param context context with SharedPreferences
     * @return true if notifications are turn on, false otherwise
     */
    public static boolean notificationsOn(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE, MODE_PRIVATE);
        return sharedPreferences.getString(context.getString(R.string.preference_notifications_new_message), "true").equals("true");
    }

    /**
     * Saves device id.
     *
     * @param context  context with SharedPreferences
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
     *
     * @param instance  activity from which snapshot is sending
     * @param map       map to send
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
                            new String[]{outputFile.getPath()},
                            new String[]{"image/png"},
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
     * Class to receive coordinates broadcast by CoordinatorService.
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
