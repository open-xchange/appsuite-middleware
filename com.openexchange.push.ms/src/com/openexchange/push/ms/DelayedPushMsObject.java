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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.push.ms;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;

/**
 * Wrapper for PushMSObject if the Push should be delayed by a certain amount of time. Use case for this delaying wrapper: Unlike E-Mails
 * other PIM Objects shouldn't be pushed immediately because they can be changed within a short timeframe to adjust details or other objects
 * might be created in the same folder which would lead to yet another push event. Introduced to stay compatible with old {c.o}.push.udp
 * implementation.
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class DelayedPushMsObject implements Delayed {

    private static final long serialVersionUID = -2259438183887625194L;

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(DelayedPushMsObject.class));

    // How long should the Push of the wrapped PIM Object be delayed in milliseconds
    private final int delayDuration;

    // How long should the Push of the wrapped PIM Object be delayed in milliseconds
    private final long maxDelayDuration;

    // Determine when the object should be pushed by saving when it was last touched (either created or delayed)
    private long touchTime;
    
    // The wrapped object
    PushMsObject pushMsObject;

    /**
     * Initializes a new {@link DelayedPushMsObject} by wrapping the given PushMsObject.
     * @param pushMsObject the original PushMsObject
     * @param delayDuration how long should the push of the wrapped PushMsObject be delayed in milliseconds
     * @param maxDelayDuration the maximum time a PushMsObject can be delayed
     */
    public DelayedPushMsObject(PushMsObject pushMsObject, int delayDuration, int maxDelayDuration) {
        this.pushMsObject = pushMsObject;
        this.delayDuration = delayDuration;
        this.touchTime = System.currentTimeMillis();
        this.maxDelayDuration = touchTime + maxDelayDuration;
    }

    @Override
    public int compareTo(Delayed other) {
        long thisDelay = getDelay(TimeUnit.MICROSECONDS);
        long otherDelay = other.getDelay(TimeUnit.MICROSECONDS);
        return (thisDelay < otherDelay ? -1 : (thisDelay == otherDelay ? 0 : 1));
    }

    /*
     * The Delay has elapsed if
     * Either:  the delayDuration since last time this object was touched has elapsed  
     * Or:      the maxDelayDuration was reached
     */
    @Override
    public long getDelay(TimeUnit unit) {
        long currentTimeMillis = System.currentTimeMillis();
        long retval = Math.min((touchTime + delayDuration) - currentTimeMillis, maxDelayDuration - currentTimeMillis);
        return unit.convert(retval,TimeUnit.MILLISECONDS);
    }
    
    /**
     * Get the wrapped pushMsObject.
     * @return the wrapped pushMsObject
     */
    public PushMsObject getPushObject() {
        return pushMsObject;
    }

    /**
     * Refresh delay by touching the object.
     */
    public void touch() {
        this.touchTime = System.currentTimeMillis();
    }

}
