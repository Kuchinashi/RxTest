package com.example.rxtest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.cookpad.android.rxt4a.subscriptions.AndroidSubscriptions;
import rx.Observable;
import rx.functions.Func1;

public class Retry implements Func1<Observable<? extends Throwable>, Observable<?>> {

    private static final String ACTION = "com.example.rxtest.ACTION";
    private static final String TAG = Retry.class.getSimpleName();
    private boolean firstRetry = true;
    private Context context;

    public Retry(Context context) {
        this.context = context;
    }

    @Override
    public Observable<?> call(Observable<? extends Throwable> observable) {
        Log.i(TAG, "call()");
        return observable.flatMap(o -> {
            Log.i(TAG, "flatMap()");
            if (firstRetry) {
                firstRetry = false;
                return waitingBroadcast();
            }
            return Observable.error(null);
        });
    }

    private Observable<String> waitingBroadcast() {
        Log.i(TAG, "waitingBroadcast()");
        return Observable.create(subscriber -> {
            final BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "onReceive()");
                    subscriber.onNext("next");
                }
            };
            final IntentFilter filter = new IntentFilter(ACTION);
            context.registerReceiver(receiver, filter);
            subscriber.add(AndroidSubscriptions.unsubscribeOnMainThread(
                    () -> context.unregisterReceiver(receiver)));

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent intent = new Intent(ACTION);
                context.sendBroadcast(intent);
            }, 3000);
        });
    }

}
