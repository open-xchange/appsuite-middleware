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

package com.openexchange.ms.internal;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;


/**
 * {@link HzDelayed}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HzDelayed<E> implements Delayed {

    /**
     * The delay for pooled messages.
     */
    private static final long MSEC_DELAY = 5000L;

    private final long stamp;
    private final boolean immediateDelivery;
    private final E data;

    /**
     * Initializes a new {@link HzDelayed}.
     */
    public HzDelayed(final E data, final boolean immediateDelivery) {
        super();
        stamp = System.currentTimeMillis();
        this.immediateDelivery = immediateDelivery;
        this.data = data;
    }
    
    /**
     * Gets the message data object.
     *
     * @return The message data object
     */
    public E getData() {
        return data;
    }

    @Override
    public int compareTo(final Delayed o) {
        final long thisStamp = stamp;
        final long otherStamp = ((HzDelayed<?>) o).stamp;
        return (thisStamp < otherStamp ? -1 : (thisStamp == otherStamp ? 0 : 1));
    }

    @Override
    public long getDelay(final TimeUnit unit) {
        return immediateDelivery ? 0L : unit.convert(MSEC_DELAY - (System.currentTimeMillis() - stamp), TimeUnit.MILLISECONDS);
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return data.equals(obj);
    }

}
