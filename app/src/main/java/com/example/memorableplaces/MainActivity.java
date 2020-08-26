package com.example.memorableplaces;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.DialogInterface;
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

    static ArrayList<String> myAddresses;
    static ArrayList<String> myPlaces;
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

        try { //decode saved data and save to array lists
            String addresses = savedData.getString("addresses", "empty");
            String places = savedData.getString("places", "empty");
            if(addresses.equals("empty") || places.equals("empty") ){
                myAddresses = new ArrayList<>();
                myPlaces  = new ArrayList<>();
            }
            else{
                myAddresses = (ArrayList<String>) ObjectSerializer.deserialize(addresses);
                myPlaces = (ArrayList<String>) ObjectSerializer.deserialize(places);
                if(myPlaces.size()==0 && myAddresses.size()==0) {
                    textView.setVisibility(View.VISIBLE);
                }else{
                    textView.setVisibility(View.GONE);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            Log.i("Warning","Deserialization failed!");
        }

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

                final int pos = position;

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE: //Yes button clicked
                                myAddresses.remove(pos);
                                myPlaces.remove(pos);
                                arrayAdapter.notifyDataSetChanged();

                                try {
                                    //encode all data and save to shared preferences
                                    String encodedString = ObjectSerializer.serialize(MainActivity.myAddresses);
                                    MainActivity.savedData.edit().putString("addresses", encodedString).apply();
                                    encodedString = ObjectSerializer.serialize(MainActivity.myPlaces);
                                    MainActivity.savedData.edit().putString("places", encodedString).apply();
                                }catch(Exception e){
                                    e.printStackTrace();
                                    Log.i("Warning","Serialization failed!");
                                }

                                Toast.makeText(MainActivity.this, "Location removed", Toast.LENGTH_SHORT).show();
                                if(myPlaces.size() <= 0){
                                    textView.setVisibility(View.VISIBLE);
                                }
                                break;

                            case DialogInterface.BUTTON_NEGATIVE: //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setMessage("Remove this location?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

                return true;
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