package chetan.example.rxandroiddemo;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.OnErrorThrowable;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button mButton;
    Button mSecondButton;
    Subscriber<ArrayList<Track>> mStringSubscriber;
    Subscriber<String> mSecondSubscriber;
    public static final String TAG = "RXANDROIDSAMPLES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButton = (Button) findViewById(R.id.clickButton);
        mSecondButton = (Button) findViewById(R.id.clickButtonSecond);
        mSecondButton.setOnClickListener(this);
        mButton.setOnClickListener(this);
        mStringSubscriber = new Subscriber<ArrayList<Track>>() {
            @Override
            public void onCompleted() {
                Log.d(TAG, "onCompleted()");
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError()", e);
            }

            @Override
            public void onNext(ArrayList<Track> trackArrayList) {
                for (int i = 0; i < trackArrayList.size(); i++) {

                    Log.d(TAG, "onNext(" + "track=>" + trackArrayList.get(i).getTrackName() + ")");
                }

            }
        };
        mSecondSubscriber = new Subscriber<String>() {
            @Override
            public void onCompleted() {
                Log.d(TAG, "onCompleted()");
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError()", e);
            }

            @Override
            public void onNext(String trackArrayList) {


                Log.d(TAG, "onNext(" + "track=>" + trackArrayList + ")");


            }
        };
    }


    Observable<String> getSecondObservable() {
        return Observable.defer(new Func0<Observable<String>>() {
            @Override
            public Observable<String> call() {
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(5));
                } catch (InterruptedException e) {
                    throw OnErrorThrowable.from(e);
                }
                return Observable.just(getBranchList());
            }
        });
    }

    private String getBranchList() {

        return ServerData();

    }

    static Observable<ArrayList<Track>> getObservable() {
        return Observable.defer(new Func0<Observable<ArrayList<Track>>>() {
            @Override
            public Observable<ArrayList<Track>> call() {
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(5));
                } catch (InterruptedException e) {
                    throw OnErrorThrowable.from(e);
                }
                return Observable.just(provideResult(), provideAnotherResult());
            }
        });
    }

    public void performActionOnButtonClick() {
        getObservable()
                // Run on a background thread
                .subscribeOn(Schedulers.newThread())
                // Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mStringSubscriber);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mStringSubscriber != null && mStringSubscriber.isUnsubscribed()) {
            mStringSubscriber.unsubscribe();
            Log.d(TAG, "Subscriber is Unsubscribe Sucessfully");
        }
    }

    public static ArrayList<Track> provideResult() {
        ArrayList<Track> trackArrayList = new ArrayList<Track>();
        Track track;
        for (int i = 0; i < 10; i++) {
            track = new Track("track" + i);
            trackArrayList.add(track);

        }
        return trackArrayList;
    }

    public static ArrayList<Track> provideAnotherResult() {
        ArrayList<Track> trackArrayList = new ArrayList<Track>();
        Track track;
        for (int i = 0; i < 10; i++) {
            track = new Track("Anothertrack" + i);
            trackArrayList.add(track);

        }
        return trackArrayList;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.clickButton:
                performActionOnButtonClick();
                break;
            case R.id.clickButtonSecond:
                performClickOnSecondButton();
                break;
        }

    }

    public void performClickOnSecondButton() {
        getSecondObservable().
                subscribeOn(Schedulers.newThread()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe(mSecondSubscriber);
    }

    public String ServerData() {
        String path = "http://drpatil.xerces.info/Get_Appointment_Branch.php";
        String response = "";
        try {
            URL url = new URL(path);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);


            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            //writer.write(getPostDataString(params));
            writer.flush();
            writer.close();
            os.close();
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                //Log.d("Output",br.toString());
                while ((line = br.readLine()) != null) {
                    response += line;
                    Log.d("output lines", line);
                }
            } else {
                response = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("Response", response);
        return response;
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

}
