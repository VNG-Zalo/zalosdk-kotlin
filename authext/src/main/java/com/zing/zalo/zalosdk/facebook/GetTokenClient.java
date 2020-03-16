package com.zing.zalo.zalosdk.facebook;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class GetTokenClient implements ServiceConnection {
    public static final int MESSAGE_GET_ACCESS_TOKEN_REQUEST = 0x10000;
    public static final int MESSAGE_GET_ACCESS_TOKEN_REPLY = 0x10001;

    public static final String EXTRA_APPLICATION_ID = "com.facebook.platform.extra.APPLICATION_ID";
    public static final String STATUS_ERROR_TYPE = "com.facebook.platform.status.ERROR_TYPE";

    private final Context context;
    private final Handler handler;
    private final String applicationId;
    private CompletedListener listener;
    private boolean running;
    private Messenger sender;

    public GetTokenClient(Context context, String applicationId) {
        Context applicationContext = context.getApplicationContext();

        this.context = (applicationContext != null) ? applicationContext : context;

        this.applicationId = applicationId;

        handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                GetTokenClient.this.handleMessage(message);
            }
        };
    }

    public void setCompletedListener(CompletedListener listener) {
        this.listener = listener;
    }

    protected Context getContext() {
        return context;
    }

    public boolean start() {
        if (running) {
            return false;
        }

        // Make sure that the service can handle the requested protocol version
        int availableVersion = NativeProtocol.getLatestAvailableProtocolVersionForService(NativeProtocol.PROTOCOL_VERSION_20121101);
        if (availableVersion == NativeProtocol.NO_PROTOCOL_AVAILABLE) {
            return false;
        }

        Intent intent = NativeProtocol.createPlatformServiceIntent(context);
        if (intent == null) {
            return false;
        } else {
            running = true;
            context.bindService(intent, this, Context.BIND_AUTO_CREATE);
            return true;
        }
    }

    public void cancel() {
        running = false;
    }

    public void onServiceConnected(ComponentName name, IBinder service) {
        sender = new Messenger(service);
        sendMessage();
    }

    public void onServiceDisconnected(ComponentName name) {
        sender = null;
        try {
            context.unbindService(this);
        } catch (IllegalArgumentException ex) {
            // Do nothing, the connection was already unbound
        }
        callback(null);
    }

    private void sendMessage() {
        Bundle data = new Bundle();
        data.putString(EXTRA_APPLICATION_ID, applicationId);

        populateRequestBundle(data);

        Message request = Message.obtain(null, MESSAGE_GET_ACCESS_TOKEN_REQUEST);
        request.arg1 = NativeProtocol.PROTOCOL_VERSION_20121101;
        request.setData(data);
        request.replyTo = new Messenger(handler);

        try {
            sender.send(request);
        } catch (RemoteException e) {
            callback(null);
        }
    }


    protected void populateRequestBundle(Bundle data) {
    }

    protected void handleMessage(Message message) {
        if (message.what == MESSAGE_GET_ACCESS_TOKEN_REPLY) {
            Bundle extras = message.getData();
            String errorType = extras.getString(STATUS_ERROR_TYPE);
            if (errorType != null) {
                callback(null);
            } else {
                callback(extras);
            }
            context.unbindService(this);
        }
    }

    private void callback(Bundle result) {
        if (!running) {
            return;
        }
        running = false;

        CompletedListener callback = listener;
        if (callback != null) {
            callback.completed(result);
        }
    }

    public interface CompletedListener {
        void completed(Bundle result);
    }
}
