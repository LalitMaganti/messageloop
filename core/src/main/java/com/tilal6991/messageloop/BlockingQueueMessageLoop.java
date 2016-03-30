package com.tilal6991.messageloop;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Class which implements the {@link MessageLoop} interface using a backing {@link BlockingQueue}.
 */
public class BlockingQueueMessageLoop implements MessageLoop {

    private static final int PRE_START = 0;

    private static final int STARTED = 1;

    private static final int STOPPED = 2;

    private final MessageLoop.Handler mHandler;

    private final BlockingQueue<Message> mQueue;

    private final Object mLock;

    private final Thread mThread;

    private final int mMaxPoolSize;

    private Message pool;

    private int poolSize;

    private int mState;

    BlockingQueueMessageLoop(@Nonnull MessageLoop.Handler handler,
                             @Nonnull BlockingQueue<Message> queue,
                             @Nonnegative int maxPoolSize) {
        mHandler = handler;
        mQueue = queue;
        mMaxPoolSize = maxPoolSize;

        mLock = new Object();
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                startLooping();
            }
        });
        mState = PRE_START;
    }

    public static MessageLoop create(@Nonnull MessageLoop.Handler handler) {
        return new BlockingQueueMessageLoop(handler, new LinkedBlockingQueue<Message>(), 50);
    }

    @Override
    public boolean start() {
        synchronized (mLock) {
            if (mState == STARTED || mState == STOPPED) {
                return false;
            }
            mState = STARTED;
        }
        mThread.start();
        return true;
    }

    @Override
    public boolean isOnLoop() {
        return mThread.getId() == Thread.currentThread().getId();
    }

    @Override
    public boolean post(int type, @Nullable Object obj) {
        Message message;
        synchronized (mLock) {
            if (mState == PRE_START || mState == STOPPED) {
                return false;
            }
            message = obtain(type, obj);
            return mQueue.offer(message);
        }
    }

    @Override
    public boolean shutdown() {
        synchronized (mLock) {
            if (mState == PRE_START || mState == STOPPED) {
                return false;
            }
            mState = STOPPED;
        }
        mThread.interrupt();
        return true;
    }

    private void startLooping() {
        while (true) {
            // If message is null here something terrible must have happened.
            Message message;
            try {
                message = mState == STOPPED ? mQueue.poll() : mQueue.take();

                // The message can only be null if we are stopped and no new messages were
                // available to take. In that case, we have fulfilled the interface contract
                // and we can safely exit as we have processed all items in the queue.
                if (message == null) {
                    break;
                }
            } catch (InterruptedException e) {
                // Cleanly break out of the loop as an interrupt means the loop is done.
                break;
            }

            int type = message.type;
            Object obj = message.obj;

            synchronized (mLock) {
                release(message);
            }
            mHandler.handle(type, obj);
        }
    }

    private Message obtain(int type, @Nullable Object obj) {
        if (pool == null) {
            return new Message();
        }
        Message message = pool;
        pool = message.next;
        message.next = null;

        message.type = type;
        message.obj = obj;

        return message;
    }

    private void release(@Nonnull Message message) {
        if (poolSize < mMaxPoolSize) {
            message.reset();

            message.next = pool;
            pool = message;
            poolSize++;
        }
    }

    class Message {
        int type = -1;
        Object obj = null;
        Message next = null;

        void reset() {
            type = -1;
            obj = null;
        }
    }
}