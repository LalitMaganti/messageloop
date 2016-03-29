package com.tilal6991.messageloop;

import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class which implements the {@link MessageLoop} interface using a backing {@link HandlerThread}
 * and associated {@link android.os.Handler}.
 */
public class AndroidHandlerMessageLoop implements MessageLoop, Handler.Callback {

    private static final int PRE_START = 0;

    private static final int STARTED = 1;

    private static final int STOPPED = 2;

    private final HandlerThread mHandlerThread;

    private final AtomicInteger mState;

    private android.os.Handler mHandler;

    private Handler mMessageHandler;

    AndroidHandlerMessageLoop(@NotNull MessageLoop.Handler handler, HandlerThread handlerThread) {
        mMessageHandler = handler;
        mHandlerThread = handlerThread;

        mHandler = null;
        mState = new AtomicInteger(PRE_START);
    }

    public static MessageLoop create(MessageLoop.Handler handler) {
        return new AndroidHandlerMessageLoop(handler, new HandlerThread("co.fusionx.relay.MessageLoop"));
    }

    @Override
    public boolean start() {
        if (!mState.compareAndSet(PRE_START, STARTED)) {
            return false;
        }

        mHandlerThread.start();
        mHandler = new android.os.Handler(mHandlerThread.getLooper(), this);
        return true;
    }

    @Override
    public boolean isOnLoop() {
        Looper looper = mHandlerThread.getLooper();
        return looper != null && looper.equals(Looper.myLooper());
    }

    @Override
    public boolean post(int type, @Nullable Object obj) {
        return mState.compareAndSet(STARTED, STARTED) &&
                mHandler.sendMessage(mHandler.obtainMessage(type, obj));
    }

    @Override
    public boolean shutdown() {
        if (!mState.compareAndSet(STARTED, STOPPED)) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return mHandlerThread.quitSafely();
        } else {
            return mHandlerThread.quit();
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        mMessageHandler.handle(msg.what, msg.obj);
        return true;
    }
}
