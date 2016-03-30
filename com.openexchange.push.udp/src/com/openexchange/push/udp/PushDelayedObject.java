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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
}
