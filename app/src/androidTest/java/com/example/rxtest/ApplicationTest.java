package com.example.rxtest;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;
import android.util.Log;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.schedulers.Schedulers;

import java.util.concurrent.CountDownLatch;

@RunWith(AndroidJUnit4.class)
public class ApplicationTest extends InstrumentationTestCase {
    public ApplicationTest() {
        super();
    }

    private static final String TAG = ApplicationTest.class.getSimpleName();
    private Context targetContext;

    @Before
    public void setUp() {
        targetContext = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void connection() throws Exception {
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.google.com")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        GoogleService service = retrofit.create(GoogleService.class);

        final CountDownLatch latch = new CountDownLatch(1);

        service.top()
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.immediate())
                .retryWhen(new Retry(targetContext))
                .subscribe(
                        s -> Log.d(TAG, "onNext()"),
                        th -> {
                            Log.w(TAG, "onError()");
                            fail();
                            latch.countDown();
                        },
                        () -> {
                            Log.i(TAG, "onCompleted()");
                            latch.countDown();
                        }
                );
        latch.await();
    }
}