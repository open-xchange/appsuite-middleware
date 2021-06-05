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

package com.openexchange.mail.watcher;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.config.MailProperties;


/**
 * {@link MailAccessDelayElement} - A simple wrapper for {@link MailAccess} that implements {@link Delayed} interface.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccessDelayElement implements Delayed {

    private static final int WATCHER_TIME = MailProperties.getInstance().getWatcherTime();

    /** The associated {@link MailAccess} instance */
    public final MailAccess<?, ?> mailAccess;

    /** The time stamp considered when to rate this element as expired/elapsed */
    public final long stamp;

    /**
     * Initializes a new {@link MailAccessDelayElement}.
     *
     * @param mailAccess The associated {@link MailAccess} instance
     * @param stamp The time stamp
     */
    public MailAccessDelayElement(MailAccess<?, ?> mailAccess, long stamp) {
        super();
        this.mailAccess = mailAccess;
        this.stamp = stamp;
    }

    @Override
    public int compareTo(Delayed o) {
        long thisStamp = stamp;
        long otherStamp = ((MailAccessDelayElement) o).stamp;
        return (thisStamp < otherStamp ? -1 : (thisStamp == otherStamp ? 0 : 1));
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return (unit.convert(WATCHER_TIME - (System.currentTimeMillis() - stamp), TimeUnit.MILLISECONDS));
    }

    @Override
    public int hashCode() {
        return mailAccess.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MailAccessDelayElement)) {
            return false;
        }
        final MailAccessDelayElement other = (MailAccessDelayElement) obj;
        if (mailAccess == null) {
            if (other.mailAccess != null) {
                return false;
            }
        } else if (!mailAccess.equals(other.mailAccess)) {
            return false;
        }
        return true;
    }

}
