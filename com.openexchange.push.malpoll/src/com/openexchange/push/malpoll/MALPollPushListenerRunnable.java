
package com.openexchange.push.malpoll;

import com.openexchange.push.PushException;

/**
 * Simple {@link Runnable} to trigger a listener's {@link MALPollPushListener#checkNewMail()} method.
 */
public final class MALPollPushListenerRunnable implements Runnable {

    private static final org.apache.commons.logging.Log LOG =
        org.apache.commons.logging.LogFactory.getLog(MALPollPushListenerRunnable.class);

    private final MALPollPushListener listener;

    public MALPollPushListenerRunnable(final MALPollPushListener listener) {
        super();
        this.listener = listener;
    }

    public void run() {
        try {
            listener.checkNewMail();
        } catch (final PushException e) {
            LOG.error(e.getMessage(), e);
        }
    }

}
