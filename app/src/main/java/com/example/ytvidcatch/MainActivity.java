package com.example.ytvidcatch;

import android.app.DownloadManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yausername.ffmpeg.FFmpeg;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;
import com.yausername.youtubedl_android.YoutubeDLRequest;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    Button downloadB;
    DownloadManager manager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            YoutubeDL.getInstance().init(getApplicationContext());
            FFmpeg.getInstance().init(getApplication());
        } catch (YoutubeDLException e) {
            Log.e("TAG", "failed to initialize youtubedl-android", e);
        }
        findViewById(R.id.downloadBtn).setEnabled(false);
        findViewById(R.id.downloadBtn).setOnClickListener(v -> {
            if (!CanDownload) {
                Toast toast = Toast.makeText(getApplicationContext(), "Brak możliwości Pobrania!", Toast.LENGTH_LONG);
                toast.show();
                return;
            }
            TextView t = (TextView) findViewById(R.id.text);
            t.setText("Pobieram...");
            RadioGroup rg = (RadioGroup) findViewById(R.id.radioGroup);
            Boolean isAudio = true;
            if (rg.getCheckedRadioButtonId() == R.id.video) {
                isAudio = false;
            }
            Downloader d = new Downloader(isAudio,kod);
            d.execute();
        });
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            handleSendText(intent);
        }

    }
    @Override
    protected void onPause() {
        super.onPause();
        if(!CanDownload&!Downloading){
            finish();
        }
    }
    public String kod = "NULL";
    public Boolean CanDownload = false;
    public Boolean Downloading = false;
    void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        TextView t = (TextView) findViewById(R.id.text);
        if (sharedText != null) {
            if (sharedText.contains("https://youtu.be/") & sharedText.length() == 28) {
                CanDownload = true;
                kod = sharedText.substring(17);
                findViewById(R.id.downloadBtn).setEnabled(true);
                t.setText("Gotowe do pobrania!");
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Treść nie pochodzi z YT", Toast.LENGTH_LONG);
                toast.show();
                t.setText("Błąd Interpretacji!\nWprowadź treść z platformy youtube");
            }
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "Brak udostępnionej treści", Toast.LENGTH_LONG);
            toast.show();
        }
    }
    public class Downloader extends AsyncTask<Void, Void, Void> {
        Boolean isAudio;
        String code;
        public Downloader(Boolean isAudio,String code){
            this.isAudio = isAudio;
            this.code = code;
        }
        @Override
        protected Void doInBackground(Void... arg0) {
            Download(isAudio,code);
            return null;
        }

    }
    void Download(Boolean isAudio, String code){
        CanDownload = false;
        Downloading = true;
        File youtubeDLDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "youtubedl-android");
        YoutubeDLRequest request = new YoutubeDLRequest("https://www.youtube.com/watch?v="+code);
        if(isAudio){
            request.addOption("--extract-audio");
            request.addOption("--audio-format","mp3");
        }
        else{
            request.addOption("-f","bestvideo[ext=mp4]+bestaudio[ext=m4a]/mp4");
        }
        request.addOption("-o", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+ "/%(title)s.%(ext)s");
        try {
            YoutubeDL.getInstance().execute(request, (progress, etaInSeconds) -> {
                runOnUiThread(new Runnable() {
                    public void run(){
                        TextView t = (TextView) findViewById(R.id.text);
                        t.setText(String.valueOf(progress) + "% (ETA " + String.valueOf(etaInSeconds) + " seconds)");
                    }
                });
            });
            runOnUiThread(new Runnable() {
                public void run(){
                    TextView t = (TextView) findViewById(R.id.text);
                    t.setText("Gotowe!");
                }
            });
            Downloading= false;
        } catch (YoutubeDLException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
