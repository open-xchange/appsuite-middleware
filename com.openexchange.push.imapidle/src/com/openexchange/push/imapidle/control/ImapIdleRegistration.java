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

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import com.openexchange.push.imapidle.ImapIdlePushListener;
import com.sun.mail.imap.IMAPFolder;

/**
 * {@link ImapIdleRegistration}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
class ImapIdleRegistration implements Delayed {

    private final long stamp;
    private final ImapIdlePushListener pushListener;
    private final IMAPFolder imapFolder;
    private final int hash;

    /**
     * Initializes a new {@link ImapIdleRegistration}.
     */
    ImapIdleRegistration(ImapIdlePushListener pushListener,  IMAPFolder imapFolder, long elapseMillis) {
        super();
        stamp = System.currentTimeMillis() + elapseMillis;
        this.imapFolder = imapFolder;
        this.pushListener = pushListener;
        hash = 31 * 1 + ((pushListener == null) ? 0 : pushListener.hashCode());
    }

    /**
     * Initializes a new {@link ImapIdleRegistration}.
     */
    ImapIdleRegistration(ImapIdlePushListener pushListener) {
        super();
        stamp = 0L;
        imapFolder = null;
        this.pushListener = pushListener;
        hash = 31 * 1 + ((pushListener == null) ? 0 : pushListener.hashCode());
    }

    @Override
    public int compareTo(Delayed o) {
        long thisStamp = this.stamp;
        long otherStamp = ((ImapIdleRegistration) o).stamp;
        return (thisStamp < otherStamp ? -1 : (thisStamp == otherStamp ? 0 : 1));
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long toGo = stamp - System.currentTimeMillis();
        return unit.convert(toGo, TimeUnit.MILLISECONDS);
    }

    /**
     * Gets the push listener.
     *
     * @return The push listener
     */
    public ImapIdlePushListener getPushListener() {
        return pushListener;
    }

    /**
     * Gets the IMAP folder currently idle on.
     *
     * @return The IMAP folder
     */
    public IMAPFolder getImapFolder() {
        return imapFolder;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (null == obj) {
            return false;
        }
        if (!(obj instanceof ImapIdleRegistration)) {
            return obj.equals(pushListener);
        }
        ImapIdleRegistration other = (ImapIdleRegistration) obj;
        if (pushListener == null) {
            if (other.pushListener != null) {
                return false;
            }
        } else if (!pushListener.equals(other.pushListener)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ImapIdleRegistration [stamp=" + stamp + ", pushListener=" + pushListener + "]";
    }

}
