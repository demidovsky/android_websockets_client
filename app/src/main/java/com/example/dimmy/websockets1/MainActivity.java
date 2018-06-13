package com.example.dimmy.websockets1;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import org.apache.commons.io.IOUtils;

public class MainActivity extends AppCompatActivity {
    private Socket socket;
    private String HOST = "http://10.0.2.2:3000/"; // replace with production host

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pingSocket();
                Snackbar.make(view, "Sent to socket", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });

        checkInternetConnection();
        checkServerConnection();
        initSocket();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initSocket() {
        Log.i("test", "init");

        IO.Options opts = new IO.Options();
        opts.forceNew = true;
        opts.query = "deviceID=test-client";
        opts.path = "/socket";
        try {
            socket = IO.socket(HOST, opts);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                socket.emit("search", "init");
            }

        }).on("event", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
            }

        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
            }

        });
        socket.connect();


    }


    private void pingSocket() {
        Log.i("test", "ping");
        socket.emit("search", "ping");
    }


    private void executeReq(URL urlObject) throws IOException {
        HttpURLConnection conn = null;

        conn = (HttpURLConnection) urlObject.openConnection();
        conn.setReadTimeout(100000); //Milliseconds
        conn.setConnectTimeout(150000); //Milliseconds
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();

        InputStream inputStream = conn.getInputStream();
        String text = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());

        Log.i("Response:", text);
    }


    private void checkInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (null == ni)
            Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show();
        else {
            Toast.makeText(this, "Internet connection is alive", Toast.LENGTH_LONG).show();
        }
    }

    private void checkServerConnection() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    executeReq(new URL(HOST));
                    Log.i("test", "Server is alive");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i("test", "Server not answering");
                }
            }
        }).start();
    }
}
