/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.java;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * {@link AbstractCustomDelayed}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CustomDelayed<T> implements Delayed {

    private volatile long stamp;
    private final long delayDuration;
    private final long maxStamp;
    private final T element;

    /**
     * Initializes a new {@link CustomDelayed} with arbitrary delay durations by wrapping the given element.
     *
     * @param element The actual payload element
     * @param delayDuration The delay duration (in milliseconds) to use initially and as increment for each reset-operation
     * @param maxDelayDuration The the maximum delay duration (in milliseconds) to apply
     * @throws IllegalArgumentException If <code>delayDuration</code> is greater than <code>maxDelayDuration</code>
     */
    public CustomDelayed(T element, long delayDuration, long maxDelayDuration) {
        super();
        if (delayDuration > maxDelayDuration) {
            throw new IllegalArgumentException("delayDuration is greater than maxDelayDuration.");
        }
        this.element = element;
        this.delayDuration = delayDuration;
        long now = System.currentTimeMillis();
        stamp = now + delayDuration;
        maxStamp = now + maxDelayDuration;
    }

    /**
     * Initializes a new {@link CustomDelayed} with a fixed delay duration by wrapping the given element. Resetting this object has no
     * effect, i.e. the maximum delay duration is fixed.
     *
     * @param element The actual payload element
     * @param delayDuration The delay duration (in milliseconds) to use
     * @throws IllegalArgumentException If <code>delayDuration</code> is greater than <code>maxDelayDuration</code>
     */
    public CustomDelayed(T element, int delayDuration) {
        this(element, delayDuration, delayDuration);
    }

    @Override
    public int compareTo(final Delayed o) {
        long thisStamp = this.stamp;
        long otherStamp = ((CustomDelayed) o).stamp;
        return (thisStamp < otherStamp ? -1 : (thisStamp == otherStamp ? 0 : 1));
    }

    /*
     * The Delay has elapsed if Either: the delayDuration since last time this object was touched has elapsed Or: the maxDelayDuration was
     * reached
     */
    @Override
    public long getDelay(final TimeUnit unit) {
        long toGo = stamp - System.currentTimeMillis();
        return unit.convert(toGo, TimeUnit.MILLISECONDS);
    }

    /**
     * Get the wrapped pushMsObject.
     *
     * @return the wrapped pushMsObject
     */
    public T getElement() {
        return element;
    }

    /**
     * Resets the internal delay, up to the configured maximum delay duration.
     */
    public void reset() {
        final long stamp = System.currentTimeMillis() + delayDuration;
        // Stamp must not be greater than maxStamp
        this.stamp = stamp >= maxStamp ? maxStamp : stamp;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((element == null) ? 0 : element.hashCode());
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
        if (!(obj instanceof CustomDelayed)) {
            return false;
        }
        CustomDelayed other = (CustomDelayed) obj;
        if (element == null) {
            if (other.element != null) {
                return false;
            }
        } else if (!element.equals(other.element)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CustomDelayed [stamp=" + stamp + ", element=" + element + "]";
    }

}
