package com.example.memorableplaces;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
public class MainActivity extends AppCompatActivity {

    static ArrayList<String> myAddresses = new ArrayList<>();
    static ArrayList<LatLng> myPlaces = new ArrayList<>();
    static ArrayAdapter<String> arrayAdapter;
    static TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView listView = findViewById(R.id.listView);
        textView = findViewById(R.id.textView);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, myAddresses);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
                intent.putExtra("itemNumber",position);
                startActivity(intent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                myAddresses.remove(position);
                myPlaces.remove(position);
                arrayAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "Location removed", Toast.LENGTH_SHORT).show();
                if(myPlaces.size() <= 0){
                    textView.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });


    }

    public void addPlace(View view){
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        int arraySize = myAddresses.size();
        Log.i("mainActivity arraySize",String.valueOf(arraySize));
        startActivity(intent);
    }

}