package com.gc.mp4v2demo;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;


public class MP4UsbManager {
    private UsbDevice mDevice;

    private UsbManager mManager;

    private static final String ACTION_USB_PERMISSION = "com.gc.USB_PERMISSION";

    private static final String TAG = "MP4UsbManager";

    MP4UsbManager(Context context) {
        mManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        context.registerReceiver(usbReceiver, filter);
        // discover device
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {

                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

                    if (device != null) {
                        //setupDevice;
                        setupDevice(device);
                    }
                }

            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                // determine if connected device is a mass storage devuce
                if (device != null) {
                    mDevice = device;
                    discoverDevice(context);
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                Log.d(TAG, "USB device detached");

                // determine if connected device is a mass storage devuce
                if (device != null) {
                    Log.i(TAG, "detach " + String.valueOf(device));
                }
            }
        }
    };

    private void setupDevice(UsbDevice device) {
        Log.d(TAG, "setupDevice " +  String.valueOf(device));
    }

    private void discoverDevice(Context context) {

        if (mDevice != null && mManager.hasPermission(mDevice)) {
            Log.d(TAG, "received usb device via intent");
            // requesting permission is not needed in this case
            //setupDevice;
        } else {
            // first request permission from user to communicate with the
            // underlying

            // UsbDevice
            PendingIntent permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(
                    ACTION_USB_PERMISSION), 0);
            mManager.requestPermission(mDevice, permissionIntent);
        }
    }
}
