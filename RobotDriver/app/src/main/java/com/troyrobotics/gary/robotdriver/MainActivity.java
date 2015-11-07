package com.troyrobotics.gary.robotdriver;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;


public class MainActivity extends ActionBarActivity {

    private static final String forward = "65%";
    private static final String backward = "35%";

    private static  EditText iptext;
    private static  EditText porttext;

    private Connector conn;

    private HandlerThread ht;

    private Message m;
    private Button cnt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iptext = (EditText) findViewById(R.id.iptext);
        porttext = (EditText) findViewById(R.id.porttext);

        Button fb =(Button) findViewById(R.id.forwardbutton);
        Button bb =(Button) findViewById(R.id.backbutton);
        Button lb =(Button) findViewById(R.id.leftbutton);
        Button rb =(Button) findViewById(R.id.rightbutton);
        initButtonListeners(fb,bb,lb,rb);

        ht = new HandlerThread("myHandler");
        if (!ht.isAlive())
            ht.start();



        cnt = (Button) findViewById(R.id.connectbutton);
        cnt.setBackgroundColor(Color.RED);
        cnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 String tempip = iptext.getText().toString();
                 int tempport = getPort();

                m = Message.obtain(conn,MessageCode.MAKE_CONNECTION,tempport,0,tempip);
                conn.sendMessage(m);
            }
        });
        conn = new Connector(ht.getLooper(),this);
    }

    private int getPort()
    {
        try
        {
            return Integer.parseInt(porttext.getText().toString());
        }
        catch (NumberFormatException nfe)
        {
            return 8124;
        }
    }

    public void initButtonListeners(Button fb,Button bb,Button lb,Button rb)
    {
        fb.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        arduinoDrive(backward,forward);
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                        arduinoDrive("0","0");
                        return true; // if you want to handle the touch event
                }
                return false;
            }
        });

        bb.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        arduinoDrive(forward, backward);
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                        arduinoDrive("0","0");
                        return true; // if you want to handle the touch event
                }
                return false;
            }
        });

        lb.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        arduinoDrive(backward,backward);
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                        arduinoDrive("0","0");
                        return true; // if you want to handle the touch event
                }
                return false;
            }
        });

        rb.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        arduinoDrive(forward,forward);
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                        arduinoDrive("0","0");
                        return true; // if you want to handle the touch event
                }
                return false;
            }
        });
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


    private void arduinoDrive(String left, String right)//IMPORTANT: left and right are double values from 0 to 1
    {
        m = Message.obtain(conn,MessageCode.WRITE_TO_SERVER,left+","+right);
        conn.sendMessage(m);
    }

    public class Connector extends Handler {
        protected Socket socket;
        protected PrintWriter writer;
        private String myIp;
        private int myPort;
        private Message msg;
        private Activity myA;

        public Connector(Looper l,Activity activity){
            super(l);
            myA = activity;
        }

        @Override
        public void handleMessage(Message message)
        {
            super.handleMessage(message);
            msg = message;
            switch(msg.what)
            {
                case MessageCode.MAKE_CONNECTION:
                    makeConnection();
                    break;
                case MessageCode.WRITE_TO_SERVER:
                    writeToServer();
                    break;
                default:
                    break;
            }
        }

        private void makeConnection()
        {

            int port = msg.arg1;
            String ip = (String) msg.obj;
            showToast("Connecting to "+ip+":"+port);
            if (socket != null && writer != null)
            {
                showToast("Killing Previous Connection");
                killConnection();

            }
            try {

                socket = new Socket(ip, port);
                writer = new PrintWriter(socket.getOutputStream(),true);
                showToast("Connected");
            }
            catch (IOException e)
            {
                showToast(e.toString());
            }

        }

        private void writeToServer()
        {

            if (socket != null && writer != null)
            {
                String w = (String) msg.obj;
                showToast(w);
                writer.println(w);
            }
        }

        private void killConnection()
        {
            writer.close();
            writer = null;
            try
            {
                socket.close();
                socket = null;
                showToast("Killed");
            }
            catch (IOException e)
            {
                showToast("Socket would not die");
            }
        }

        private void changeButtonColor(final Color c)
        {

        }

        private void showToast(final String text)
        {
            myA.runOnUiThread(new Runnable() {
                public void run() {
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(MainActivity.this, text, duration);
                    toast.show();
                }
            });
        }
    }
}
