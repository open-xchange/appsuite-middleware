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

package com.openexchange.service.messaging;

/**
 * {@link MessageHandler} - The listener for {@link Message messages}.
 * <p>
 * {@link MessageHandler} objects are registered with the messaging service registry and are notified with a {@link Message} object when a
 * message is sent or posted.
 * <p>
 * <code>MessageHandler</code> objects can inspect the received <code>Message</code> object to determine its topic and properties.
 * <p>
 * <code>MessageHandler</code> objects must be registered with a service property {@link MessagingServiceConstants#MESSAGE_TOPIC} whose
 * value is the list of topics in which the message handler is interested.
 * <p>
 * For example:
 *
 * <pre>
 * String[] topics = new String[] { &quot;com/isv/*&quot; };
 * Hashtable ht = new Hashtable();
 * ht.put(MessagingServiceConstants.MESSAGE_TOPIC, topics);
 * context.registerService(MessageHandler.class.getName(), this, ht);
 * </pre>
 *
 * Event Handler services can also be registered with an {@link MessagingServiceConstants#MESSAGE_FILTER} service property to further filter
 * the events. If the syntax of this filter is invalid, then the Event Handler must be ignored by the messaging service. The messaging
 * service should log a warning.
 *
 * @see Message
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.22
 */
public interface MessageHandler {

    /**
     * Called by the {@link MessagingService messaging service} to notify the listener of a message.
     *
     * @param message The message that occurred.
     */
    void handleMessage(Message message);

}
