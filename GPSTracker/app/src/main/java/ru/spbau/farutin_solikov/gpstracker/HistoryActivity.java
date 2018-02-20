package ru.spbau.farutin_solikov.gpstracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Activity with saved routes.
 */
public class HistoryActivity extends DrawerActivity {

    private static final String TAG = "HistoryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.content_history);

        final ListView historyList = findViewById(R.id.history_list);
        final List<Displacement> displacements;
        final ArrayList<String> routesNames = new ArrayList<>();

        try {
            displacements = new FetchDisplacements().execute().get();
            for (Displacement displacement : displacements) {
                routesNames.add(displacement.getName());
            }

            final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, routesNames);
            historyList.setAdapter(adapter);
            historyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Route route = null;
                    try {
                        route = new FetchRoutes().execute(displacements.get(position)).get();
                    } catch (InterruptedException e) {
                        Log.w(TAG, e.getMessage());
                    } catch (ExecutionException e) {
                        Log.w(TAG, e.getMessage());
                    }
                    ArrayList<Coordinate> coordinates = new ArrayList<>();
                    coordinates.addAll(route.getRoute());
                    System.err.println("ZHIII +" + coordinates.size());

                    Intent intent = new Intent(HistoryActivity.this, RouteActivity.class);
                    intent.putExtra(getString(R.string.extra_route_name), routesNames.get(position));
                    intent.putParcelableArrayListExtra(getString(R.string.extra_coordinates), coordinates);
                    startActivity(intent);
                }
            });

            historyList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this);

                    builder.setPositiveButton(getString(R.string.title_button_delete), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            String deleted = routesNames.remove(i);
                            adapter.notifyDataSetChanged();

                            if (routesNames.size() == 0) {
                                TextView emptyList = findViewById(R.id.empty_list);
                                emptyList.setVisibility(View.VISIBLE);
                            }

                            new DeleteRoute().execute(deleted);
                        }
                    });

                    builder.setNegativeButton(getString(R.string.title_button_cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });

                    builder.setTitle(getString(R.string.title_dialog_delete));
                    builder.create().show();

                    return true;
                }
            });

            if (routesNames.size() == 0) {
                TextView emptyList = findViewById(R.id.empty_list);
                emptyList.setVisibility(View.VISIBLE);
            }
        } catch (InterruptedException e) {
            Log.w(TAG, e.getMessage());
        } catch (ExecutionException e) {
            Log.w(TAG, e.getMessage());
        }

    }

    /**
     * Fetches all routes from the database in separate thread.
     */
    private static class FetchDisplacements extends AsyncTask<Void, Void, List<Displacement>> {

        @Override
        protected List<Displacement> doInBackground(Void... voids) {

            return Controller.fetchDisplacements();
        }
    }

    /**
     * Fetches all coordinates for given displacement in separate thread.
     */
    private static class FetchRoutes extends AsyncTask<Displacement, Void, Route> {

        @Override
        protected Route doInBackground(Displacement... displacements) {
            for (Displacement displacement : displacements) {
                return new Route(Controller.fetchCoordinates(displacement.getStart(), displacement.getStop()), displacement.getName());
            }

            return null;
        }
    }

    /**
     * Fetches all coordinates for given displacement in separate thread.
     */
    private static class DeleteRoute extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            for (String string : strings) {
                Controller.deleteDisplacement(string);
                return null;
            }

            return null;
        }
    }
}
