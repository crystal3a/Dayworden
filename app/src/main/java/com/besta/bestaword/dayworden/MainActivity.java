package com.besta.bestaword.dayworden;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

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

        texttitle.setText("測試");
        textcontent.setText("測試內容");
    }
}
