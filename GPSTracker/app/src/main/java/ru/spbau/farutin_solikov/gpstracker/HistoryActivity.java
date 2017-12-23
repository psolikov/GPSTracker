package ru.spbau.farutin_solikov.gpstracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


import java.util.ArrayList;

public class HistoryActivity extends DrawerActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.content_history);
				
		final ListView historyList = findViewById(R.id.history_list);
		
		// get names from the database
		final ArrayList<String> routesNames = new ArrayList<>();
		routesNames.add("1");
		routesNames.add("2");
		routesNames.add("3");
		routesNames.add("4");
		routesNames.add("5");
		
		final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, routesNames);
		historyList.setAdapter(adapter);
		
		historyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// get coordinates from the database for this route
				ArrayList<Controller.Coordinate> route = new ArrayList<Controller.Coordinate>();
				route.add(new Controller.Coordinate(-33.887837, 151.151667, 0));
				route.add(new Controller.Coordinate(-33.888436, 151.151116, 0));
				route.add(new Controller.Coordinate(-33.888509, 151.150785, 0));
				route.add(new Controller.Coordinate(-33.888712, 151.150745, 0));
				route.add(new Controller.Coordinate(-33.88896, 151.150773, 0));
				route.add(new Controller.Coordinate(-33.889312, 151.149407, 0));
				route.add(new Controller.Coordinate(-33.889691, 151.14908, 0));
				route.add(new Controller.Coordinate(-33.890614, 151.149041, 0));
				
				
				Intent intent = new Intent(HistoryActivity.this, RouteActivity.class);
				intent.putExtra("ru.spbau.farutin_solikov.gpstracker.route_name", routesNames.get(position));
				intent.putParcelableArrayListExtra("ru.spbau.farutin_solikov.gpstracker.coordinates", route);
				startActivity(intent);
			}
		});
		
		historyList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
				AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this);
				
				builder.setPositiveButton("delete", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						routesNames.remove(i);
						adapter.notifyDataSetChanged();
						
						if (routesNames.size() == 0) {
							TextView emptyList = findViewById(R.id.empty_list);
							emptyList.setVisibility(View.VISIBLE);
						}
						
						// delete route in the database
					}
				});
				
				builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						
					}
				});
				
				builder.setTitle("Delete this route?");
				builder.create().show();
				
				return true;
			}
		});
		
		if (routesNames.size() == 0) {
			TextView emptyList = findViewById(R.id.empty_list);
			emptyList.setVisibility(View.VISIBLE);
		}
	}
	
}
