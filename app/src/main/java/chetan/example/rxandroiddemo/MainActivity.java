package chetan.example.rxandroiddemo;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.OnErrorThrowable;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity
{

    Button mButton;
    Subscriber<String> mStringSubscriber;
    public static final String TAG = "RXANDROIDSAMPLES";
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButton = ( Button )findViewById( R.id.clickButton );
        mButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                performActionOnButtonClick();
            }
        });
        mStringSubscriber = new Subscriber<String>()
        {
            @Override public void onCompleted()
            {
                Log.d(TAG, "onCompleted()");
            }

            @Override public void onError(Throwable e)
            {
                Log.e(TAG, "onError()", e);
            }

            @Override public void onNext(String string)
            {
                Log.d(TAG, "onNext(" + string + ")");
            }
        };
    }


    static Observable<String> getObservable()
    {
        return Observable.defer(new Func0<Observable<String>>()
        {
            @Override public Observable<String> call()
            {
                try
                {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(5));
                }
                catch (InterruptedException e)
                {
                    throw OnErrorThrowable.from(e);
                }
                return Observable.just( provideResult());
            }
        });
    }
    public void performActionOnButtonClick()
    {
        getObservable()
                // Run on a background thread
                .subscribeOn(Schedulers.newThread())
                // Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mStringSubscriber);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if( mStringSubscriber != null && mStringSubscriber.isUnsubscribed() )
        {
            mStringSubscriber.unsubscribe();
            Log.d(TAG,"Subscriber is Unsubscribe Sucessfully");
        }
    }
    public String provideResult()
    {
        return  "THIS IS RESULT";
    }
}
