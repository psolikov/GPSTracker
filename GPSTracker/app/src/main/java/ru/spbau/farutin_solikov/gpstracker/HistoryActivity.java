package ru.spbau.farutin_solikov.gpstracker;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class HistoryActivity extends DrawerActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.content_history);
	
		ListView historyList = findViewById(R.id.history_list);
		
		// get names from the database
		ArrayList<String> routesNames = new ArrayList<>();
		routesNames.add("1");
		routesNames.add("2");
		routesNames.add("3");
		routesNames.add("4");
		routesNames.add("5");
		
		ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, routesNames);
		historyList.setAdapter(adapter);
		
		historyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// get coordinates from the database for this route
				
			}
		});
	}
	
}
