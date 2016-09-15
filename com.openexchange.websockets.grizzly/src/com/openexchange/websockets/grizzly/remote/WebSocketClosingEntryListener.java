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

package com.openexchange.websockets.grizzly.remote;

import static com.openexchange.java.Autoboxing.I;
import org.slf4j.Logger;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.MapEvent;
import com.openexchange.websockets.ConnectionId;
import com.openexchange.websockets.grizzly.GrizzlyWebSocketApplication;

/**
 * {@link WebSocketClosingEntryListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class WebSocketClosingEntryListener implements com.hazelcast.core.EntryListener<String, String> {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(WebSocketClosingEntryListener.class);

    private final GrizzlyWebSocketApplication app;

    /**
     * Initializes a new {@link WebSocketClosingEntryListener}.
     */
    public WebSocketClosingEntryListener(GrizzlyWebSocketApplication app) {
        super();
        this.app = app;
    }

    @Override
    public void entryEvicted(EntryEvent<String, String> event) {
        // Manually close associated Web Socket (if any available) to enforce re-establishing a new one
        MapKey key = MapKey.parseFrom(event.getKey());
        MapValue value = MapValue.parseFrom(event.getValue());
        if (app.closeWebSockets(key.getUserId(), key.getContextId(), ConnectionId.newInstance(value.getConnectionId()))) {
            LOG.info("Closed Web Socket ({}) due to entry eviction for user {} in context {}.", value.getConnectionId(), I(key.getUserId()), I(key.getContextId()));
        }
    }

    @Override
    public void entryAdded(EntryEvent<String, String> event) {
        // Nothing
    }

    @Override
    public void entryUpdated(EntryEvent<String, String> event) {
        // Nothing
    }

    @Override
    public void entryRemoved(EntryEvent<String, String> event) {
        // Nothing
    }

    @Override
    public void mapCleared(MapEvent event) {
        // Nothing
    }

    @Override
    public void mapEvicted(MapEvent event) {
        // Nothing
    }

}
