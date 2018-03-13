package com.blundell.tut;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {

    private static final String GREEN_LED_PIN = "BCM19";

    private Gpio bus;
    private Handler ledToggleHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PeripheralManager service = PeripheralManager.getInstance();

        try {
            bus = service.openGpio(GREEN_LED_PIN);
        } catch (IOException e) {
            throw new IllegalStateException(GREEN_LED_PIN + " bus cannot be opened.", e);
        }

        try {
            bus.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
            bus.setActiveType(Gpio.ACTIVE_HIGH);
        } catch (IOException e) {
            throw new IllegalStateException(GREEN_LED_PIN + " bus cannot be configured.", e);
        }

        ledToggleHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    protected void onStart() {
        super.onStart();
        ledToggleHandler.post(toggleLed);
    }

    private final Runnable toggleLed = new Runnable() {
        @Override
        public void run() {
            boolean isOn;
            try {
                isOn = bus.getValue();
            } catch (IOException e) {
                throw new IllegalStateException(GREEN_LED_PIN + " cannot be read.", e);
            }
            try {
                if (isOn) {
                    bus.setValue(false);
                } else {
                    bus.setValue(true);
                }
            } catch (IOException e) {
                throw new IllegalStateException(GREEN_LED_PIN + " cannot be written.", e);
            }
            ledToggleHandler.postDelayed(this, TimeUnit.SECONDS.toMillis(1));
        }
    };

    @Override
    protected void onStop() {
        ledToggleHandler.removeCallbacks(toggleLed);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        try {
            bus.close();
        } catch (IOException e) {
            Log.e("TUT", GREEN_LED_PIN + " bus cannot be closed, you may experience errors on next launch.", e);
        }
        super.onDestroy();
    }
}
