package com.mti.diploma_v1;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class MainActivity  extends AppCompatActivity {


    public static String serial_in = "";
    public static String number_in = "";
    DatabaseHelper myDB;


    public EditText serial;
    public EditText number;



    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = (Button) findViewById(R.id.button);
        serial = (EditText) findViewById(R.id.editSerial);
        number = (EditText) findViewById(R.id.editNumber);
        FloatingActionButton btnScan = (FloatingActionButton) findViewById(R.id.scan);
        Button btnView = (Button) findViewById(R.id.btnView);
        myDB = new DatabaseHelper(this);


        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                startActivityForResult(intent, 1);
            }
        });


        btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                serial_in = serial.getText().toString();
                number_in = number.getText().toString();
                Log.d("КНОПКА", "кнопка нажата");


                if (serial_in.length()!=4){
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.Series_check, Toast.LENGTH_SHORT);
                    toast.show();
                }
                else if (number_in.length()!=6) {
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.Number_check, Toast.LENGTH_SHORT);
                    toast.show();
                }
                else {
                    try {

                        new SendData().execute();


                    } catch (Exception e) {
                        Toast toast = Toast.makeText(getApplicationContext(), R.string.Error +" " + e, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            }
        });

        btnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ViewListContents.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        String[] split = data.getStringExtra("passport").split(" ");
        String s = split[0] + split[1];
        String n = split[2];
        serial.setText(s);
        number.setText(n);
    }


    class SendData extends AsyncTask<Void, Void, Void> {

        String resultString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {

                String myURL = "http://vh228830.eurodir.ru/action.php";

                String parammetrs = "series=" + serial_in + "&number=" + number_in;
                byte[] data = null;
                InputStream is = null;


                try {
                    URL url = new URL(myURL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestProperty("Content-Length", "" + Integer.toString(parammetrs.getBytes().length));
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    Log.d("СЕТЬ", "подключение установлено");


                    // конвертируем передаваемую строку в UTF-8
                    data = parammetrs.getBytes("utf-8");


                    OutputStream os = conn.getOutputStream();


                    // передаем данные на сервер
                    os.write(data);
                    os.flush();
                    os.close();
                    data = null;
                    conn.connect();
                    int responseCode = conn.getResponseCode();


                    // передаем ответ сервер
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    if (responseCode == 200) {    // Если все ОК (ответ 200)
                        is = conn.getInputStream();

                        byte[] buffer = new byte[8192]; // размер буфера


                        // Далее так читаем ответ
                        int bytesRead;


                        while ((bytesRead = is.read(buffer)) != -1) {
                            baos.write(buffer, 0, bytesRead);
                        }


                        data = baos.toByteArray();
                        resultString = new String(data, "utf-8");  // сохраняем в переменную ответ сервера, у нас "OK"
                        Log.d("результат проверки", resultString);

                    } else {
                    }

                    conn.disconnect();

                } catch (MalformedURLException e) {

                    resultString = "MalformedURLException:" + e.getMessage();
                } catch (IOException e) {

                    resultString = "IOException:" + e.getMessage();
                } catch (Exception e) {

                    resultString = "Exception:" + e.getMessage();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Toast toast = Toast.makeText(getApplicationContext(), resultString.replace("'",""), Toast.LENGTH_LONG);
            toast.show();
            DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
            String date = df.format(Calendar.getInstance().getTime());
            AddData(date +"\n"+resultString.replace("'",""));

        }
    }

    public void AddData(String newEntry) {

        boolean insertData = myDB.addData(newEntry);
        if (insertData) {
            Toast.makeText(this, R.string.Saved, Toast.LENGTH_SHORT).show();


        } else {
            Toast.makeText(this, R.string.Saved_not, Toast.LENGTH_SHORT).show();
        }
    }
}


