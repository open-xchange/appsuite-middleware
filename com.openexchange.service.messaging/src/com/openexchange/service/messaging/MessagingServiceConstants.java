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
 * {@link MessagingServiceConstants}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.22
 */
public final class MessagingServiceConstants {

    /**
     * Initializes a new {@link MessagingServiceConstants}.
     */
    private MessagingServiceConstants() {
        super();
    }

    /**
     * Service registration property (named <code>service.message.topics</code>) specifying the <code>Message</code> topics of interest to a
     * Message Handler service.<br>
     * <b>Note:</b> It is highly recommended to let the topic start with <b><tt>"remote/"</tt></b> to signal a remote message.
     * <p>
     * Message handlers SHOULD be registered with this property. The value of the property is a string or an array of strings that describe
     * the topics in which the handler is interested. An asterisk ('*') may be used as a trailing wildcard. Message Handlers which do not
     * have a value for this property must not receive messages. More precisely, the value of each string must conform to the following
     * grammar:
     *
     * <pre>
     *  topic-description := '*' | topic ( '/*' )?
     *  topic := token ( '/' token )*
     * </pre>
     *
     * @see Message
     */
    public static final String MESSAGE_TOPIC = "service.message.topics";

    /**
     * Service Registration property (named <code>service.message.filter</code>) specifying a filter to further select <code>Message</code>
     * s of interest to a Message Handler service.
     * <p>
     * Message handlers MAY be registered with this property. The value of this property is a string containing an LDAP-style filter
     * specification. Any of the event's properties may be used in the filter expression. Each event handler is notified for any event which
     * belongs to the topics in which the handler has expressed an interest. If the event handler is also registered with this service
     * property, then the properties of the event must also match the filter for the event to be delivered to the event handler.
     * <p>
     * If the filter syntax is invalid, then the Message Handler must be ignored and a warning should be logged.
     *
     * @see Message
     * @see org.osgi.framework.Filter
     */
    public static final String MESSAGE_FILTER = "service.message.filter";

    /**
     * The total length of message's body when serialized.
     */
    public static final String MESSAGE_LENGTH = "service.message.length";

}
