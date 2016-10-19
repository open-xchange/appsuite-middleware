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

import java.util.Collections;
import java.util.List;
import com.openexchange.pns.Hit;
import com.openexchange.pns.KnownTopic;
import com.openexchange.pns.PushMatch;

/**
 * {@link WebSocketHit}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
final class WebSocketHit implements Hit {

    /** The topic for all */
    private static final String ALL = KnownTopic.ALL.getName();

    private final int userId;
    private final int contextId;
    private final String client;

    /**
     * Initializes a new {@link WebSocketHit}.
     *
     * @param client The Web Socket client
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    WebSocketHit(String client, int userId, int contextId) {
        super();
        this.userId = userId;
        this.client = client;
        this.contextId = contextId;
    }

    @Override
    public String getTransportId() {
        return WebSocketPushNotificationTransport.ID;
    }

    @Override
    public List<PushMatch> getMatches() {
        return Collections.singletonList(newMatch());
    }

    private PushMatch newMatch() {
        return new PushMatchImpl(userId, contextId, client, WebSocketPushNotificationTransport.ID, WebSocketPushNotificationTransport.createTokenFor(client, userId, contextId), ALL);
    }

    @Override
    public String getClient() {
        return client;
    }
}