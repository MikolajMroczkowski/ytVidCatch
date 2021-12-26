package com.example.ytvidcatch;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {

    Button downloadB;
    DownloadManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.downloadBtn).setEnabled(false);
        findViewById(R.id.downloadBtn).setOnClickListener(v -> {
            if (!CanDownload) {
                Toast toast = Toast.makeText(getApplicationContext(), "Brak możliwości Pobrania!", Toast.LENGTH_LONG);
                toast.show();
                return;
            }
            RadioGroup rg = (RadioGroup) findViewById(R.id.radioGroup);
            String fileType = "mp3";
            if (rg.getCheckedRadioButtonId() == R.id.video) {
                fileType = "mp4";
            }
            run(kod, fileType);

        });
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            handleSendText(intent);
        }
    }

    public String kod = "NULL";
    public Boolean CanDownload = false;

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

    void run(String code, String filetype) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(getApplicationContext(), "Przygotowanie...", Toast.LENGTH_LONG);
                toast.show();
            }
        });
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(
                10, TimeUnit.MINUTES).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.MINUTES).build();
        Request get = new Request.Builder()
                .url("https://e-buda.eu/ytdlapi/download.php?code=" + code + "&type=" + filetype)
                .build();

        client.newCall(get).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    ResponseBody responseBody = response.body();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast toast = Toast.makeText(getApplicationContext(), "Pobieranie..."+Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath(), Toast.LENGTH_LONG);
                            toast.show();
                        }
                    });
                    DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(responseBody.string()));
                    request.setTitle(code + "." + filetype)
                            .setDescription("File is downloading...")
                            .setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS, code + "." + filetype)
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    downloadManager.enqueue(request);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }

}
