package com.example.memorableplaces;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static ArrayList<String> myAddresses = new ArrayList<>();
    static ArrayList<String> myPlaces = new ArrayList<>();
    static ArrayAdapter<String> arrayAdapter;
    static TextView textView;
    static SharedPreferences savedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        savedData = this.getSharedPreferences("com.example.memorableplaces", Context.MODE_PRIVATE);
        ListView listView = findViewById(R.id.listView);
        textView = findViewById(R.id.textView);
//        try {
//            String addresses = savedData.getString("addresses", "empty");
//            String places = savedData.getString("places", "empty");
//            if(addresses.equals("empty") || places.equals("empty") ){
//                myAddresses = null;
//                myPlaces = null;
//            }
//            else{
//                myAddresses = (ArrayList<String>) ObjectSerializer.deserialize(addresses);
//                myPlaces = (ArrayList<String>) ObjectSerializer.deserialize(places);
//            }
//        }catch(Exception e){
//            e.printStackTrace();
//            Log.i("Warning","Deserialization failed!");
//        }
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, myAddresses);
        listView.setAdapter(arrayAdapter);

        //Click on a list item to show location on map
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
                intent.putExtra("itemNumber",position);
                startActivity(intent);
            }
        });

        //Long click an item to remove the element
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

    //floating add button shows all existing places
    public void addPlace(View view){
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        int arraySize = myAddresses.size();
        Log.i("mainActivity arraySize",String.valueOf(arraySize));
        startActivity(intent);
    }
}