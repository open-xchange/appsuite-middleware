
package com.openexchange.push.malpoll;

import com.openexchange.push.PushException;

/**
 * Simple {@link Runnable} to trigger a listener's {@link MALPollPushListener#checkNewMail()} method.
 */
public final class MALPollPushListenerRunnable implements Runnable {

    private final MALPollPushListener listener;

    private final org.apache.commons.logging.Log log;

    public MALPollPushListenerRunnable(final MALPollPushListener listener, final org.apache.commons.logging.Log log) {
        super();
        this.listener = listener;
        this.log = log;
    }

    public void run() {
        try {
            listener.checkNewMail();
        } catch (final PushException e) {
            log.error(e.getMessage(), e);
        }
    }

}
