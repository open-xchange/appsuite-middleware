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

package com.openexchange.realtime.hazelcast.channel;

import java.util.concurrent.atomic.AtomicReference;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;

/**
 * {@link HazelcastAccess} - Provides access to Hazelcast instance.
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class HazelcastAccess {

    private static final AtomicReference<HazelcastInstance> REFERENCE = new AtomicReference<HazelcastInstance>();

    /**
     * Sets the Hazelcast instance to given value/reference.
     * 
     * @param hazelcast The Hazelcast instance
     */
    public static void setHazelcastInstance(HazelcastInstance hazelcast) {
        REFERENCE.set(hazelcast);
    }

    /**
     * Gets the Hazelcast instance, throwing appropriate exceptions if it is not accessible.
     * 
     * @return The Hazelcast instance
     * @throws OXException if the HazelcastInstance can't be found
     */
    public static HazelcastInstance getHazelcastInstance() throws OXException {
        HazelcastInstance hazelcastInstance = optHazelcastInstance();
        if (null == hazelcastInstance || false == hazelcastInstance.getLifecycleService().isRunning()) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(HazelcastInstance.class.getName());
        }
        return hazelcastInstance;
    }

    /**
     * Gets the (possibly <code>null</code> or not running) Hazelcast instance.
     * 
     * @return The Hazelcast instance
     */
    public static HazelcastInstance optHazelcastInstance() {
        return REFERENCE.get();
    }

    /**
     * Get the local Hazelcast Member.
     * 
     * @return the local Hazelcast Member.
     * @throws OXException if the HazelcastInstance can't be found
     */
    public static Member getLocalMember() throws OXException {
        return getHazelcastInstance().getCluster().getLocalMember();
    }

    private HazelcastAccess() {
        // prevent instantiation
    }
}
