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

package com.openexchange.ms.internal.portable;

import java.util.List;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.hazelcast.core.ITopic;
import com.hazelcast.nio.serialization.Portable;
import com.openexchange.ms.MessageListener;
import com.openexchange.ms.internal.AbstractHzTopic;

/**
 * {@link PortableHzTopic}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class PortableHzTopic<P extends Portable> extends AbstractHzTopic<P> {

    private final ITopic<PortableMessage<P>> hzTopic;

    /**
     * Initializes a new {@link PortableHzTopic}.
     *
     * @param name The topic's name
     * @param hz The hazelcast instance
     */
    public PortableHzTopic(String name, HazelcastInstance hz) {
        super(name, hz);
        this.hzTopic = hz.getTopic(name);
    }

    @Override
    protected String registerListener(MessageListener<P> listener, String senderID) {
        try {
            return hzTopic.addMessageListener(new PortableHzMessageListener<P>(listener, senderID));
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    protected boolean unregisterListener(String registrationID) {
        try {
            return hzTopic.removeMessageListener(registrationID);
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    protected void publish(String senderId, P message) {
        try {
            hzTopic.publish(new PortableMessage<P>(senderId, message));
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    protected void publish(String senderId, List<P> messages) {
        try {
            hzTopic.publish(new PortableMessage<P>(senderId, messages));
        } catch (HazelcastInstanceNotActiveException e) {
            throw handleNotActiveException(e);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (null != hzTopic) {
            hzTopic.destroy();
        }
    }

}
