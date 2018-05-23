package com.mti.diploma_v1;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;

public class ViewListContents extends AppCompatActivity {

    DatabaseHelper myDB;
    FloatingActionButton btnClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_list_contents);
        btnClear = (FloatingActionButton) findViewById(R.id.btnClear);
        myDB = new DatabaseHelper(this);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder adb=new AlertDialog.Builder(ViewListContents.this);
               adb.setTitle(R.string.Delete);
               adb.setMessage(R.string.Delete_all);
               adb.setNegativeButton(R.string.Cancel, null);
               adb.setPositiveButton(R.string.Ok, new AlertDialog.OnClickListener() {
                   public void onClick(DialogInterface dialog, int which) {
                       myDB.onDelete();
                       recreate();
            }});
       adb.show();
            }
        });

        ListView listView = (ListView) findViewById(R.id.listView);
        myDB = new DatabaseHelper(this);

        //populate an ArrayList<String> from the database and then view it
        ArrayList<String> theList = new ArrayList<>();
        Cursor data = myDB.getListContents();
        if(data.getCount() == 0){
            Toast.makeText(this, R.string.No_Content,Toast.LENGTH_LONG).show();
        }else{
            while(data.moveToNext()){
                theList.add(data.getString(1));
                ListAdapter listAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,theList);
                listView.setAdapter(listAdapter);
            }
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                final String value = adapterView.getItemAtPosition(position).toString().replaceAll("'", "");
                Log.d("нажат итем", "onItemClick: "+value);
                AlertDialog.Builder adb=new AlertDialog.Builder(ViewListContents.this);
                adb.setTitle(R.string.Send);
                adb.setMessage(R.string.Send_to);
                adb.setNegativeButton(R.string.Cancel, null);
                adb.setPositiveButton(R.string.Ok, new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        emailIntent.setType("text/plain");
                        emailIntent.putExtra(Intent.EXTRA_EMAIL  , new String[]{"destination@gmail.com"});
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Результат проверки паспорта");
                        emailIntent.putExtra(Intent.EXTRA_TEXT   , value);
                        startActivity(emailIntent);
                    }});
                adb.show();

            }
        });
    }

}