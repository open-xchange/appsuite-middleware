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

package com.openexchange.mail.cache;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;

/**
 * {@link PooledMailAccess} - A simple wrapper for a {@link MailAccess} instance providing {@link Delayed} methods.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PooledMailAccess implements Delayed {

    /**
     * Gets the pooled value for specified mailAccess carrying given time-to-live milliseconds.
     *
     * @param mailAccess The mail access
     * @param ttlMillis The time-to-live milliseconds
     * @return The pooled value
     */
    public static PooledMailAccess valueFor(MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, long ttlMillis) {
        return new PooledMailAccess(mailAccess, ttlMillis);
    }

    private final long timeoutStamp;

    private final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess;

    /**
     * Initializes a new {@link PooledMailAccess}.
     *
     * @param mailAccess The mail access
     * @param ttlMillis The time-to-live milliseconds
     */
    private PooledMailAccess(MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess, long ttlMillis) {
        super();
        timeoutStamp = System.currentTimeMillis() + ttlMillis;
        this.mailAccess = mailAccess;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return TimeUnit.MILLISECONDS.equals(unit) ? (timeoutStamp - System.currentTimeMillis()) : unit.convert(
            timeoutStamp - System.currentTimeMillis(),
            TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        final long thisStamp = timeoutStamp;
        final long otherStamp = ((PooledMailAccess) o).timeoutStamp;
        return (thisStamp < otherStamp ? -1 : (thisStamp == otherStamp ? 0 : 1));
    }

    /**
     * Gets the mail access.
     *
     * @return The mail access
     */
    public MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> getMailAccess() {
        // stamp = System.currentTimeMillis();
        return mailAccess;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(32);
        builder.append("PooledMailAccess [timeoutStamp=").append(timeoutStamp).append(", ");
        if (mailAccess != null) {
            builder.append("mailAccess=").append(mailAccess);
        }
        builder.append(']');
        return builder.toString();
    }

}
