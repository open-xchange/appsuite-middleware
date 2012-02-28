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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.mq;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.jms.Topic;
import com.openexchange.exception.OXException;

/**
 * {@link MQService} - The generic Message Queue service.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface MQService {

    /**
     * Lookup in the registry for registered {@link ConnectionFactory}.
     * <p>
     * A {@link ConnectionFactory} is the main starting point to use message queue services in JMS-like manner:<br>
     * <img src="http://docs.oracle.com/javaee/1.4/tutorial/doc/images/jms-programmingModel.gif" alt="jms-model">
     * 
     * @param name The name of the {@link ConnectionFactory}
     * @return The look-up {@link ConnectionFactory} instance.
     */
    public ConnectionFactory lookupConnectionFactory(String name) throws OXException;

    /**
     * Lookup in the registry for registered {@link Queue}.
     * <p>
     * A queue follows the Point-to-Point Messaging Domain:<br>
     * <ul>
     * <li>Each message has only one consumer.</li>
     * <li>A sender and a receiver of a message have no timing dependencies. The receiver can fetch the message whether or not it was
     * running when the client sent the message.</li>
     * <li>The receiver acknowledges the successful processing of a message.</li>
     * </ul>
     * <img src="http://docs.oracle.com/javaee/1.3/jms/tutorial/1_3_1-fcs/doc/images/Fig2.2.gif" alt="p2p">
     * 
     * @param name The name of the queue
     * @return The look-up {@link Queue} instance.
     */
    public Queue lookupQueue(String name) throws OXException;

    /**
     * Lookup in the registry for registered {@link Topic}.
     * <p>
     * A topic follows the Publish/Subscribe Messaging Domain:<br>
     * <ul>
     * <li>Each message may have multiple consumers.</li>
     * <li>Publishers and subscribers have a timing dependency. A client that subscribes to a topic can consume only messages published
     * after the client has created a subscription, and the subscriber must continue to be active in order for it to consume messages.</li>
     * </ul>
     * <img src="http://docs.oracle.com/javaee/1.3/jms/tutorial/1_3_1-fcs/doc/images/Fig2.3.gif" alt="pub-sub">
     * 
     * @param name The name of the topic
     * @return The look-up {@link Topic} instance.
     */
    public Topic lookupTopic(String name) throws OXException;

}
