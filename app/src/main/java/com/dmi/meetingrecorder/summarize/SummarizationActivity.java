package com.dmi.meetingrecorder.summarize;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dmi.meetingrecorder.R;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.translate.Translate;
import com.google.api.services.translate.TranslateRequestInitializer;
import com.google.api.services.translate.model.TranslationsListResponse;
import com.squareup.okhttp.OkHttpClient;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

public class SummarizationActivity extends Activity {

    TextView summarizedTextView;
    TextToSpeech ttobj;
    boolean ttStatus = false;
    Button btnPlay;
    SummarizedTextModel summarizedTextModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summarization);
        summarizedTextView = (TextView) findViewById(R.id.summarizedTextView);
        btnPlay = (Button) findViewById(R.id.btnPlay);
        String text;
        if(getIntent().getExtras() != null) {
            text = getIntent().getExtras().getString("text", "");
        }


        text="Voice recognition software on computers requires that analog audio be converted into digital signals, known as analog-to-digital conversion. For a computer to decipher a signal, it must have a digital database, or vocabulary, of words or syllables, as well as a speedy means for comparing this data to signals. ";

//        if(isTranslateType){
//            String translatedText = getIntent().getExtras().getString("translatedText");
//            summarizedTextView.setText(Html.fromHtml(translatedText).toString());
//            setTitle("Translated Text");
//        }else {
//            summarizedTextModel = (SummarizedTextModel) getIntent().getSerializableExtra("summarizedText");

//            if (summarizedTextModel != null) {
//                String text = "";
//                for (String eachString : summarizedTextModel.getSentences()) {
//                    text = text + Html.fromHtml(eachString) + "  \r\n\r\n";
//                }
//                summarizedTextView.setText(text);
////            }
//        }

        ttobj=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                ttStatus = true;
            }
        }
        );
        ttobj.setLanguage(Locale.UK);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ttStatus) {
                            String text = summarizedTextView.getText().toString();
                            if (text != null && text.length() > 0) {
                                ttobj.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                            }
                } else {
                    Toast.makeText(SummarizationActivity.this, "Some issue in intializing voice setting. Please check your internet setting", Toast.LENGTH_SHORT).show();
                }
            }
        });

        (new SummarizeDocumentsTask(SummarizationActivity.this, text, 4)).execute();
    }


    @Override
    protected void onStop() {
        super.onStop();
        if(ttobj != null){
            try {
                ttobj.stop();
            }catch (Exception er)
            {
                er.printStackTrace();
            }
        }
    }



    protected class SummarizeDocumentsTask extends AsyncTask<Void, Integer, Integer> {

        private final Context mContext;
        String textToSummarize;
        SummarizedTextModel summarizedTextModel;
        ProgressDialog pd;
        int numSentence;

        public SummarizeDocumentsTask(Context c, String textToSummarize, int numSentence) {
            mContext = c;
            this.textToSummarize = textToSummarize;
            this.numSentence = numSentence;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            pd = new ProgressDialog(DocumentActivity.this);
//            pd.setMessage("loading");
//            pd.show();
            pd = new ProgressDialog(SummarizationActivity.this, R.style.MyDialog);
            pd.getWindow().setGravity(Gravity.CENTER);
            pd.setMessage("loading");
            pd.show();
            View viewD = getLayoutInflater().inflate(R.layout.custom_progress_dialog, null);
            pd.setContentView(viewD);
        }

        @Override
        protected void onPostExecute(Integer result) {
            pd.dismiss();

            pd.dismiss();
            if (summarizedTextModel != null) {
//                SummarizedTextModel summarizedTextModel = (SummarizedTextModel) getIntent().getSerializableExtra("summarizedText");
//                if (summarizedTextModel != null) {
                    String text = "";
                    for (String eachString : summarizedTextModel.getSentences()) {
                        text = text + Html.fromHtml(eachString) + "  \r\n\r\n";
                    }
                    summarizedTextView.setText(text);
            }
//                }
        }

        @Override
        protected Integer doInBackground(Void... params) {

            try {
                Retrofit retrofit = new Retrofit.Builder()
                        .client(getUnsafeOkHttpClient())
                        .baseUrl("https://textanalysis-text-summarization.p.mashape.com")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                // prepare call in Retrofit 2.0
                SummarizeAPI SummarizeAPIAPI = retrofit.create(SummarizeAPI.class);

                Call<SummarizedTextModel> summarizedTextModelTemp = SummarizeAPIAPI.getSummarizedText(textToSummarize, numSentence);
                summarizedTextModel = summarizedTextModelTemp.execute().body();
            } catch (Exception er) {
                er.printStackTrace();
            }

            return 1;
        }

    }

    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            OkHttpClient okHttpClient = new OkHttpClient();
            okHttpClient.setSslSocketFactory(sslSocketFactory);
            okHttpClient.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            okHttpClient.setConnectTimeout(30, TimeUnit.SECONDS);
            okHttpClient.setReadTimeout(30, TimeUnit.SECONDS);
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    }
