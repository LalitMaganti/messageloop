package com.tilal6991.messageloop;

import org.jetbrains.annotations.Nullable;

/**
 * Class which acts waits for events and dispatches to a handler in a non-blocking fashion. All
 * methods should be thread-safe.
 * <p>
 * Note to implementors: This class should be implemented as a self contained event loop which
 * waits for events on a thread and dispatches events to the handler when finished.
 */
public interface MessageLoop {

    /**
     * Initialises and starts the loop. All messages sent from this point will be sent to an
     * implementation specified message handler.
     *
     * @return whether the start was successful.
     */
    boolean start();

    /**
     * Checks if the thread which is calling this function is on the same thread (event loop)
     * as the message loop.
     *
     * @return whether the current thread and the event loop are on the same thread.
     */
    boolean isOnLoop();

    /**
     * Sends a message to the associated handler's [Handler.handle] method.
     *
     * @param type the type of message to send to the handler.
     * @param obj  an arbitrary object to pass as an extra parameter to the handler.
     *
     * @return whether the post was successful.
     */
    boolean post(int type, @Nullable Object obj);

    /**
     * Finishes the event loop and frees any held resources. Any messages currently on the loop
     * will be processed but no new events will be allowed.
     *
     * @return whether the shutdown was successful.
     */
    boolean shutdown();

    /**
     * Class which receives messages posted to it by a MessageLoop.
     */
    interface Handler {

        /**
         * Handles incoming messages.
         *
         * @param type an implementation specific type of the message.
         * @param obj  a generic parameter which complements the type of the message.
         */
        void handle(int type, @Nullable Object obj);
    }
}
