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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.pns.transport.websocket.internal;

import java.io.IOException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import org.slf4j.Logger;

/**
 * {@link ClientAssociatedEndpoint}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class ClientAssociatedEndpoint extends Endpoint {

    /** The logger constant */
    static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ClientAssociatedEndpoint.class);

    private final String client;
    private volatile Session session;

    /**
     * Initializes a new {@link ClientAssociatedEndpoint}.
     *
     * @param client The client identifier
     */
    public ClientAssociatedEndpoint(String client) {
        super();
        this.client = client;
    }

    @Override
    public void onOpen(final Session session, EndpointConfig config) {
        final String client = this.client;
        session.addMessageHandler(new MessageHandler.Whole<String>() {

            @Override
            public void onMessage(String message) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException ex) {
                    LOG.error("Error during Web Socket communication for client {}", client, ex);
                }
            }
        });
        this.session = session;
    }

    @Override
    public void onError(Session session, Throwable thr) {
        LOG.error("Error during Web Socket communication for client {}", client, thr);
    }

    /**
     * Sends the specified message
     *
     * @param message The message
     * @throws IOException If an I/O error occurs
     */
    public void sentMessage(String message) throws IOException {
        Session session = this.session;
        if (null == session) {
            throw new IllegalStateException("Web Socket not yet open");
        }

        session.getBasicRemote().sendText(message);
    }

}
