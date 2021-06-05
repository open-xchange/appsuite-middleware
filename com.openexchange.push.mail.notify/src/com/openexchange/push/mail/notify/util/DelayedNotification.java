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

package com.openexchange.push.mail.notify.util;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import com.openexchange.config.ConfigurationService;
import com.openexchange.push.mail.notify.osgi.Services;


/**
 * {@link DelayedNotification}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DelayedNotification implements Delayed {

    /**
     * The delay for pooled notifications.
     */
    private static volatile Long delayMillis;
    private static long delayMillis() {
        Long tmp = delayMillis;
        if (null == tmp) {
            synchronized (DelayedNotification.class) {
                tmp = delayMillis;
                if (null == tmp) {
                    long defaultDelayMillis = 5000L;
                    ConfigurationService service = Services.optService(ConfigurationService.class);
                    if (null == service) {
                        return defaultDelayMillis;
                    }

                    tmp = Long.valueOf(service.getIntProperty("com.openexchange.push.mail.notify.delay_millis", (int) defaultDelayMillis));
                    delayMillis = tmp;
                }
            }
        }
        return tmp.longValue();
    }

    // ----------------------------------------------------------------------------------------------------------------- //

    private final long stamp;
    private final boolean immediateDelivery;
    private final String mboxid;
    private final int hash;

    /**
     * Initializes a new {@link DelayedNotification}.
     */
    public DelayedNotification(final String mboxid, final boolean immediateDelivery) {
        super();
        stamp = System.currentTimeMillis();
        this.immediateDelivery = immediateDelivery;
        this.mboxid = mboxid;
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mboxid == null) ? 0 : mboxid.hashCode());
        hash = result;
    }


    /**
     * Gets the mboxid.
     *
     * @return The mboxid
     */
    public String getMboxid() {
        return mboxid;
    }

    @Override
    public int compareTo(final Delayed o) {
        final long thisStamp = stamp;
        final long otherStamp = ((DelayedNotification) o).stamp;
        return (thisStamp < otherStamp ? -1 : (thisStamp == otherStamp ? 0 : 1));
    }

    @Override
    public long getDelay(final TimeUnit unit) {
        return immediateDelivery ? 0L : unit.convert(delayMillis() - (System.currentTimeMillis() - stamp), TimeUnit.MILLISECONDS);
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
        if (!(obj instanceof DelayedNotification)) {
            return false;
        }
        DelayedNotification other = (DelayedNotification) obj;
        if (mboxid == null) {
            if (other.mboxid != null) {
                return false;
            }
        } else if (!mboxid.equals(other.mboxid)) {
            return false;
        }
        return true;
    }

}
