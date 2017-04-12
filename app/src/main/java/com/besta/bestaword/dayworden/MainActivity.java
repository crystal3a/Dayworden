package com.besta.bestaword.dayworden;

import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;



public class MainActivity extends AppCompatActivity {


    public interface TextToSpeechCallback {
        void onStart();
        void onCompleted();
        void onError();
    }



    private String mytag ="Dayworden";
    private TextView texttitle;
    private TextView textcontent;
    private Map<String, TextToSpeechCallback> mTtsCallbacks = new HashMap<>();
    private TextToSpeech mTextToSpeech;
    private int mTtsQueueMode = TextToSpeech.QUEUE_FLUSH;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        texttitle = (TextView) findViewById(R.id.texttitle);
        texttitle.setMovementMethod(new ScrollingMovementMethod());
        textcontent = (TextView) findViewById(R.id.textcontent);
        textcontent.setMovementMethod(new ScrollingMovementMethod());
        mContext= this.getApplicationContext();
        initTts(mContext);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(mytag, "onStart");

        if (isConnected()) {
            Log.d("NetworkConnection", "Network Connected.");
            //顯示每日一字
            display();
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

    private void display(){
        String url= getqueryurl();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(mytag, response.toString());
                        String res = jsonparser(response);
                        String titlestr = res.split("_____")[0].trim();
                        String contentstr = res.split("_____")[1].trim();
                        texttitle.setText(titlestr);
                        textcontent.setText(contentstr);
                        saytext(titlestr);

                        //"查無此字"
                        if(res=="")
                        {
                            //Speech.getInstance().setLocale(Locale.TRADITIONAL_CHINESE);
                            //Speech.getInstance().say("查無此字");
                        }
                        else
                        {
                            //DIC0001
                            //Speech.getInstance().setLocale(Locale.TRADITIONAL_CHINESE);
                            //res=StringFilter(res);
                            //Speech.getInstance().say(res);
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(mytag, "Error: {0}" , error.getMessage());
            }
        });
        Volley.newRequestQueue(getApplicationContext()).add(jsonObjReq);
    }

    private String getqueryurl() {
        //英文每日一字授權內容ID
        String ContentID = "DOW0017";
        //目前時間
        Date today = new Date();
        //設定日期格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        //進行轉換
        String dateString = sdf.format(today);
        QueryRequest qrreq = new QueryRequest();
        return qrreq.QueryJson(dateString,ContentID);
    }

    private void saytext(String result)
    {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        QueryRequest qrreq = new QueryRequest();
        String ContentID = "VOC0012";
        String url = qrreq.QueryHtml(result,ContentID);//VOC0012
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response) {
                Log.i("playsound", "onResponse");
                String [] resp = response.split("\\n");
                if(resp.length >0 )//length of array
                {
                    Log.d("play url",resp[0]);
                    playsound(resp[0]);


                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("saytext", "Error: " + error.getMessage());
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);



    }

    private void playsound(String url)
    {
        MediaPlayer mp = new MediaPlayer();
        try
        {
            mp.setDataSource(getApplicationContext(), Uri.parse(url));
            mp.prepare();
            mp.start();
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    Log.i("Completion Listener","onCompletion");
                    mp.stop();
                    mp.release();
                    String contentstr = StringFilter(textcontent.getText().toString());
                    Log.i(mytag,contentstr);
                    //say(contentstr);

                    say(contentstr, new TextToSpeechCallback() {
                        @Override
                        public void onStart() {
                            Log.i("speech", "speech started");
                        }

                        @Override
                        public void onCompleted() {
                            Log.i("speech", "speech completed");
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            finish();
                        }

                        @Override
                        public void onError() {
                            Log.i("speech", "speech error");
                        }
                    });
                }
            });
        }
        catch(Exception e)
        {
            Log.d("playsoundException",e.getMessage());
            //loadtextview(result);
        }
    }




    private String jsonparser(JSONObject response)
    {
        String qr_title="";
        String qr_result="";
        String qrword = "test";
        //qr_result = qrword + "\n 解釋 \n";
        Map<String, String> interpretation_map = new HashMap<>();
        try{
            Object entry_temp = response.getJSONObject("root").get("entry");
            Log.d("Entry",qrword+"取到entry");
            Object fedword_xml_temp;
            //JSONArray fedword_xml;
            if(entry_temp instanceof JSONArray){
                JSONArray entry = (JSONArray)entry_temp;
                fedword_xml_temp = entry.getJSONObject(0).get("fedword-xml"); //有複數entry只取第一個
            }
            else {
                JSONObject entry = (JSONObject)entry_temp;
                fedword_xml_temp = entry.get("fedword-xml");
            }
            Log.d("fedword-xml",qrword+"取到fedword-xml");
            if(fedword_xml_temp instanceof JSONArray){
                JSONArray fedword_xml = (JSONArray) fedword_xml_temp;
                for (int i = 0; i < fedword_xml.length(); i++){
                    JSONObject f = fedword_xml.getJSONObject(i);
                    String fedword_xml_att = f.getString("@att");
                    if(fedword_xml_att.indexOf("interpretation-ref-") != -1) {
                        String def_tmp = "";
                        Object interpretation_ref_temp = f.getJSONObject("row").get("row-value");
                        if(interpretation_ref_temp instanceof JSONObject){
                            def_tmp = (f.getJSONObject("row").getJSONObject("row-value").isNull("#text")) ?
                                    f.getJSONObject("row").getJSONObject("row-value").getString("#cdata-section") :
                                    f.getJSONObject("row").getJSONObject("row-value").getString("#text");
                            interpretation_map.put(fedword_xml_att, def_tmp);
                        } else{
                            JSONArray row = f.getJSONObject("row").getJSONArray("row-value");
                            for (int j = 0; j < row.length(); j++) {
                                if (row.getJSONObject(j).getString("@att").equals("def")) {
                                    def_tmp = (row.getJSONObject(j).isNull("#text")) ?
                                            row.getJSONObject(j).getString("#cdata-section") :
                                            row.getJSONObject(j).getString("#text");
                                    interpretation_map.put(fedword_xml_att, def_tmp);
                                }
                            }

                        }
                        Log.d("解釋內容", def_tmp);
                    }
                }
                for (int i = 0; i < fedword_xml.length(); i++){
                    JSONObject f = fedword_xml.getJSONObject(i);
                    String fedword_xml_att = f.getString("@att");

                    if(fedword_xml_att.equals("base-form")){
                        Object row_temp = f.get("row");
                        JSONObject row = (JSONObject) row_temp;
                        Object row_value_tmp = row.get("row-value");
                        JSONArray row_value = (JSONArray) row_value_tmp;
                        for (int k = 0; k < row_value.length(); k++){
                            JSONObject row_value_iter = row_value.getJSONObject(k);
                            String row_value_iter_att = row_value_iter.getString("@att");
                            if (row_value_iter_att.equals("entryword")){
                                qr_title = (row_value_iter.getString("#text") + "\n");
                                break;
                            }
                        }
                    }
                    else if(fedword_xml_att.equals("interpretation")){
                        Object row_temp = f.get("row");
                        if(row_temp instanceof JSONArray){
                            JSONArray row = (JSONArray) row_temp;
                            for (int j = 0;j < row.length(); j++){
                                JSONObject row_iter = row.getJSONObject(j);
                                Object row_value_tmp = row_iter.get("row-value");
                                if(row_value_tmp instanceof JSONObject){
                                    JSONObject row_value = (JSONObject) row_value_tmp;
                                    String interpretation_id = row_value.getString("#text");
                                    qr_result += (interpretation_map.get(interpretation_id) + "\n");
                                }
                                else{
                                    JSONArray row_value = (JSONArray) row_value_tmp;
                                    for (int k = 0; k < row_value.length(); k++){
                                        JSONObject row_value_iter = row_value.getJSONObject(k);
                                        String row_value_iter_att = row_value_iter.getString("@att");
                                        if (row_value_iter_att.equals("seg")){
                                            qr_result += (row_value_iter.getString("#text") + "\n");
                                        }
                                        else if(row_value_iter_att.equals("id")){
                                            String interpretation_id = row_value_iter.getString("#text");
                                            qr_result += (interpretation_map.get(interpretation_id)+ "\n");
                                        }
                                    }

                                }
                            }
                        }
                        else{
                            JSONObject row = (JSONObject) row_temp;
                            Object row_value_tmp = row.get("row-value");
                            if (row_value_tmp instanceof JSONObject){
                                JSONObject row_value = (JSONObject) row_value_tmp;
                                String interpretation_id = row_value.getString("#text");
                                qr_result += (interpretation_map.get(interpretation_id)+ "\n");
                            }
                            else{
                                JSONArray row_value = (JSONArray) row_value_tmp;
                                for(int k = 0; k < row_value.length();k++){
                                    JSONObject row_value_iter = row_value.getJSONObject(k);
                                    String row_value_iter_att = row_value_iter.getString("@att");
                                    if (row_value_iter_att.equals("seg")){
                                        qr_result += (row_value_iter.getString("#text") + "\n");
                                    }
                                    else if(row_value_iter_att.equals("id")){
                                        String interpretation_id = row_value_iter.getString("#text");
                                        qr_result += (interpretation_map.get(interpretation_id)+ "\n");
                                    }
                                }
                            }
                        }

                    }
                }
            }
            /*
            else{
                JSONObject fedword_xml = (JSONObject) fedword_xml_temp;
                qr_result += fedword_xml.getJSONObject("row").getJSONObject("row-value").getString("#text");
            }
            */


        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return qr_title + "_____" + qr_result;

    }


    public   static  String StringFilter(String   str) throws PatternSyntaxException {
        // 只允许字母和数字
        // String   regEx  =  "[^a-zA-Z0-9]";
        // 清除掉所有特殊字符
        String regEx="[`~!@#$%^&*()+=|{}':'\\[\\].<>/~！@#￥%……&*（）——+|{}【】‘：”“’a-zA-Z]";
        Pattern   p   =   Pattern.compile(regEx);
        Matcher m   =   p.matcher(str);
        return   m.replaceAll("").trim();
    }


    private void initTts(Context context) {
        if (mTextToSpeech == null) {
            mTextToSpeech = new TextToSpeech(context, mTttsInitListener);
            mTextToSpeech.setOnUtteranceProgressListener(mTtsProgressListener);
            mTextToSpeech.setLanguage(Locale.TRADITIONAL_CHINESE);
            //mTextToSpeech.setPitch(mTtsPitch);
            //mTextToSpeech.setSpeechRate(mTtsRate);
        }
    }


    private TextToSpeech.OnInitListener mTttsInitListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            switch (status) {
                case TextToSpeech.SUCCESS:
                    Log.i(mytag, "TextToSpeech engine successfully started");
                    break;

                case TextToSpeech.ERROR:
                    Log.e(mytag, "Error while initializing TextToSpeech engine!");
                    break;

                default:
                    Log.e(mytag, "Unknown TextToSpeech status: " + status);
                    break;
            }
        }
    };

    private UtteranceProgressListener mTtsProgressListener = new UtteranceProgressListener() {
        @Override
        public void onStart(final String utteranceId) {
            final TextToSpeechCallback callback = mTtsCallbacks.get(utteranceId);

            if (callback != null) {
                new Handler(mContext.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onStart();
                    }
                });
            }
        }

        @Override
        public void onDone(final String utteranceId) {
            final TextToSpeechCallback callback = mTtsCallbacks.get(utteranceId);

            if (callback != null) {
                new Handler(mContext.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onCompleted();
                        mTtsCallbacks.remove(utteranceId);
                    }
                });
            }
        }

        @Override
        public void onError(final String utteranceId) {
            final TextToSpeechCallback callback = mTtsCallbacks.get(utteranceId);

            if (callback != null) {
                new Handler(mContext.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError();
                        mTtsCallbacks.remove(utteranceId);
                    }
                });
            }
        }
    };


    /**
     * Uses text to speech to transform a written message into a sound.
     * @param message message to play
     */
    public void say(String message) {
        say(message, null);
    }

    /**
     * Uses text to speech to transform a written message into a sound.
     * @param message message to play
     * @param callback callback which will receive progress status of the operation
     */
    private void say(String message, TextToSpeechCallback callback) {

        String utteranceId = UUID.randomUUID().toString();

        if (callback != null) {
            mTtsCallbacks.put(utteranceId, callback);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTextToSpeech.speak(message, mTtsQueueMode, null, utteranceId);
        } else {
            HashMap<String, String> params = new HashMap<>();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
            mTextToSpeech.speak(message, mTtsQueueMode, params);
        }
    }

    /**
     * Stops text to speech.
     */
    private void stopTextToSpeech() {
        if (mTextToSpeech != null) {
            mTextToSpeech.stop();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d(mytag, "onPause");
        //stopTextToSpeech();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(mytag, "onStop");
        stopTextToSpeech();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(mytag, "onDestroy");
        stopTextToSpeech();
        //android.os.Process.killProcess(android.os.Process.myPid());
    }


}
