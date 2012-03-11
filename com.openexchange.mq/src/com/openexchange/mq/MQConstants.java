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

package com.openexchange.mq;

/**
 * {@link MQConstants} - Provides useful Message Queue (MQ) constants.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface MQConstants {

    /**
     * The symbolic name of the Message Queue bundle.
     */
    public static final String BUNDLE_SYMBOLIC_NAME = "com.openexchange.mq";

    /**
     * The default name for registered {@link javax.jms.ConnectionFactory} instance.
     */
    public static final String NAME_CONNECTION_FACTORY = "ConnectionFactory";

    /**
     * The default prefix to lookup registered {@link javax.jms.Queue} instance.
     */
    public static final String PREFIX_QUEUE = "/queues/";

    /**
     * The default prefix to lookup registered {@link Tjavax.jms.opic} instance.
     */
    public static final String PREFIX_TOPIC = "/topics/";

    /**
     * The name for the general-purpose {@link javax.jms.Queue} instance.
     */
    public static final String NAME_QUEUE = "genericQueue";

    /**
     * The name for the general-purpose {@link Tjavax.jms.opic} instance.
     */
    public static final String NAME_TOPIC = "genericTopic";

    /**
     * The default port for socket-based acceptors/connectors.
     */
    public static final int MQ_LISTEN_PORT = 5445;

    /*
     * ------------------------------ Queue parameters --------------------------------
     */

    /**
     * The name of the selector parameter.
     * <p>
     * The selector defines what JMS message selector the predefined queue will have. Only messages that match the selector will be added to
     * the queue. This is an optional element with a default of <code>null</code> when omitted.
     * 
     * <pre>
     *  &lt;queue name="selectorQueue"&gt;
     *       &lt;entry name="/queue/selectorQueue"/&gt;
     *       <b>&lt;selector string="color='red'"/&gt;</b>
     *       &lt;durable>true&lt;/durable&gt;
     *  &lt;/queue&gt;
     * </pre>
     */
    public static final String QUEUE_PARAM_SELECTOR = "selector";

    /**
     * The name of the durable flag parameter.
     * <p>
     * The durable flag specifies whether the queue will be persisted. This again is optional and defaults to <code>false</code> if omitted.
     * 
     * <pre>
     *  &lt;queue name="selectorQueue"&gt;
     *       &lt;entry name="/queue/selectorQueue"/&gt;
     *       selector string="color='red'"/&gt;
     *       <b>&lt;durable>true&lt;/durable&gt;</b>
     *  &lt;/queue&gt;
     * </pre>
     */
    public static final String QUEUE_PARAM_DURABLE = "durable";

}
