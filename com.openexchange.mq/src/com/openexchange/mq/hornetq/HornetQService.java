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

package com.openexchange.mq.hornetq;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.jms.Topic;
import org.hornetq.jms.server.embedded.EmbeddedJMS;
import com.openexchange.exception.OXException;
import com.openexchange.mq.MQExceptionCodes;
import com.openexchange.mq.MQService;


/**
 * {@link HornetQService} - The HornetQ Message Queue service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HornetQService implements MQService {

    private final EmbeddedJMS jmsServer;

    /**
     * Initializes a new {@link HornetQService}.
     */
    public HornetQService(final EmbeddedJMS jmsServer) {
        super();
        this.jmsServer = jmsServer;
    }

    /*-
     * ----------------------- Methods for JMS-like access to message queue -----------------------
     * 
     * Check with http://docs.oracle.com/javaee/1.3/jms/tutorial/1_3_1-fcs/doc/prog_model.html
     * 
     */

    @Override
    public ConnectionFactory lookupConnectionFactory(final String name) throws OXException {
        try {
            return (ConnectionFactory) jmsServer.lookup(name);
        } catch (final ClassCastException e) {
            throw MQExceptionCodes.CF_NOT_FOUND.create(e, name);
        } catch (final RuntimeException e) {
            throw MQExceptionCodes.CF_NOT_FOUND.create(e, name);
        }
    }

    @Override
    public Queue lookupQueue(final String name) throws OXException {
        try {
            return (Queue) jmsServer.lookup(name);
        } catch (final ClassCastException e) {
            throw MQExceptionCodes.QUEUE_NOT_FOUND.create(e, name);
        } catch (final RuntimeException e) {
            throw MQExceptionCodes.QUEUE_NOT_FOUND.create(e, name);
        }
    }

    @Override
    public Topic lookupTopic(final String name) throws OXException {
        try {
            return (Topic) jmsServer.lookup(name);
        } catch (final ClassCastException e) {
            throw MQExceptionCodes.TOPIC_NOT_FOUND.create(e, name);
        } catch (final RuntimeException e) {
            throw MQExceptionCodes.TOPIC_NOT_FOUND.create(e, name);
        }
    }

}
