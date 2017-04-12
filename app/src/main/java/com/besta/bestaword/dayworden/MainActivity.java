package com.besta.bestaword.dayworden;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    private String mytag ="Dayworden";
    private TextView texttitle;
    private TextView textcontent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        texttitle = (TextView) findViewById(R.id.texttitle);
        texttitle.setMovementMethod(new ScrollingMovementMethod());
        textcontent = (TextView) findViewById(R.id.textcontent);
        textcontent.setMovementMethod(new ScrollingMovementMethod());

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(mytag, "onStart");

        if (isConnected()) {
            Log.d("NetworkConnection", "Network Connected.");
        }else{
            Log.d("NetworkConnection", "No network connection available.");
            //告訴使用者網路無法使用
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("無法使用")
                    .setTitle("請開啟網路連線功能")
                    .setCancelable(false)
                    .setPositiveButton("確定",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    finish(); // exit program
                                }
                            });
            dialog.show();
        }


    }

    private boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(mytag, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(mytag, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(mytag, "onDestroy");
        //android.os.Process.killProcess(android.os.Process.myPid());
    }


}
