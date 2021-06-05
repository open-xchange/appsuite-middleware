/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.push.imapidle.control;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.DelayQueue;
import com.openexchange.push.imapidle.ImapIdlePushListener;
import com.sun.mail.imap.IMAPFolder;

/**
 * {@link ImapIdleControl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class ImapIdleControl {

    private final DelayQueue<ImapIdleRegistration> queue;

    /**
     * Initializes a new {@link ImapIdleControl}.
     */
    public ImapIdleControl() {
        super();
        queue = new DelayQueue<ImapIdleRegistration>();
    }

    /**
     * Adds the specified IMAP-IDLE push listener with given timeout.
     *
     * @param listener The listener to add
     * @param imapFolder The IMAP folder to idle on
     * @param timeoutMillis The timeout
     * @return <tt>true</tt>
     */
    public boolean add(ImapIdlePushListener listener, IMAPFolder imapFolder, long timeoutMillis) {
        return queue.offer(new ImapIdleRegistration(listener, imapFolder, timeoutMillis));
    }

    /**
     * Removes the specified IMAP-IDLE push listener.
     *
     * @param listener The listener to remove
     * @return <code>true</code> if such a listener was removed; otherwise <code>false</code>
     */
    public boolean remove(ImapIdlePushListener listener) {
        return queue.remove(new ImapIdleRegistration(listener));
    }

    /**
     * Removes expired push listeners from this control.
     *
     * @return The expired push listeners
     */
    List<ImapIdleRegistration> removeExpired() {
        List<ImapIdleRegistration> expirees = new LinkedList<ImapIdleRegistration>();
        queue.drainTo(expirees);
        return expirees;
    }

    /**
     * Awaits expired push listeners from this control.
     *
     * @return The expired push listeners
     * @throws InterruptedException If waiting thread is interrupted
     */
    List<ImapIdleRegistration> awaitExpired() throws InterruptedException {
        ImapIdleRegistration taken = queue.take();

        List<ImapIdleRegistration> expirees = new LinkedList<ImapIdleRegistration>();
        expirees.add(taken);

        queue.drainTo(expirees);
        return expirees;
    }

    /**
     * Awaits expired push listeners from this control uninterruptibly.
     *
     * @return The expired push listeners
     */
    List<ImapIdleRegistration> awaitExpiredUninterruptible() {
        boolean interrupted = false;
        try {
            ImapIdleRegistration taken = null;
            while (null == taken) {
                try {
                    taken = queue.take();
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }

            List<ImapIdleRegistration> expirees = new LinkedList<ImapIdleRegistration>();
            expirees.add(taken);

            queue.drainTo(expirees);
            return expirees;
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

}
