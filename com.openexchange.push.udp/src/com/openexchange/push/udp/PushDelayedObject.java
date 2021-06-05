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

package com.openexchange.push.udp;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * PushDelayedObject
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class PushDelayedObject implements Delayed {

    private final long delay;

    private final long finalTimeout;

    private final AbstractPushObject abstractPushObject;

    private long creationTime;

    public PushDelayedObject(final long delay, final AbstractPushObject abstractPushObject) {
        super();
        this.delay = delay;
        this.abstractPushObject = abstractPushObject;
        creationTime = System.currentTimeMillis();
        finalTimeout = creationTime + 5 * delay;
    }

    @Override
    public long getDelay(final TimeUnit timeUnit) {
        long currentTime = System.currentTimeMillis();
        long retval = Math.min((creationTime + delay) - currentTime, finalTimeout - currentTime);
        return timeUnit.convert(retval, TimeUnit.MILLISECONDS);
    }

    public AbstractPushObject getPushObject() {
        return abstractPushObject;
    }

    @Override
    public int compareTo(final Delayed other) {
        final long thisDelay = getDelay(TimeUnit.MICROSECONDS);
        final long otherDelay = other.getDelay(TimeUnit.MICROSECONDS);
        return (thisDelay < otherDelay ? -1 : (thisDelay == otherDelay ? 0 : 1));
    }

    public void updateTime() {
        creationTime = System.currentTimeMillis();
    }

    @Override
    public int hashCode() {
        final int prime = 83;
        int result = 1;
        result = prime * result + (int) (creationTime ^ (creationTime >>> 32));
        result = prime * result + (int) (delay ^ (delay >>> 32));
        result = prime * result + (int) (finalTimeout ^ (finalTimeout >>> 32));
        result = prime * result + ((abstractPushObject == null) ? 0 : abstractPushObject.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PushDelayedObject other = (PushDelayedObject) obj;
        if (creationTime != other.creationTime) {
            return false;
        }
        if (delay != other.delay) {
            return false;
        }
        if (finalTimeout != other.finalTimeout) {
            return false;
        }
        if (abstractPushObject == null) {
            if (other.abstractPushObject != null) {
                return false;
            }
        } else if (!abstractPushObject.equals(other.abstractPushObject)) {
            return false;
        }
        return true;
    }

}
