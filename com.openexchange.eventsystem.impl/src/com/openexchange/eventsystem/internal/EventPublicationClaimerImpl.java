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

package com.openexchange.eventsystem.internal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.eventsystem.Event;
import com.openexchange.eventsystem.EventConstants;
import com.openexchange.eventsystem.EventPublicationClaimer;
import com.openexchange.eventsystem.EventSystemExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.java.ConcurrentSet;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;


/**
 * {@link EventPublicationClaimerImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class EventPublicationClaimerImpl implements EventPublicationClaimer {

    private final ServiceLookup services;
    private final Set<Integer> contextIds;
    private final EventSystemServiceImpl serviceImpl;

    /**
     * Initializes a new {@link EventPublicationClaimerImpl}.
     */
    public EventPublicationClaimerImpl(final ServiceLookup services, final EventSystemServiceImpl serviceImpl) {
        super();
        this.services = services;
        contextIds = new ConcurrentSet<Integer>();
        this.serviceImpl = serviceImpl;
    }

    @Override
    public boolean claimEvent(final Event event, final String identifier) throws OXException {
        if (null == event) {
            return false;
        }
        final DatabaseService databaseService = services.getService(DatabaseService.class);
        if (null == databaseService) {
            throw ServiceExceptionCode.serviceUnavailable(DatabaseService.class);
        }
        final int contextId = getContextIdFrom(event);
        if (!contextIds.contains(Integer.valueOf(contextId))) { // contains() to avoid copy on add()
            contextIds.add(Integer.valueOf(contextId));
        }
        serviceImpl.startTimer();
        final Connection con = databaseService.getWritable(contextId);
        PreparedStatement stmt = null;
        boolean modifiedFlag = false;
        try {
            int pos = 1;
            if (Strings.isEmpty(identifier)) {
                stmt = con.prepareStatement("INSERT INTO eventSystemClaim (cid, uuid, lastModified) VALUES (?, ?, ?)");
                stmt.setInt(pos++, contextId);
                stmt.setBytes(pos++, UUIDs.toByteArray(event.getUuid()));
                stmt.setLong(pos++, System.currentTimeMillis());
            } else {
                stmt = con.prepareStatement("INSERT INTO eventSystemClaim (cid, uuid, id, lastModified) VALUES (?, ?, ?, ?)");
                stmt.setInt(pos++, contextId);
                stmt.setBytes(pos++, UUIDs.toByteArray(event.getUuid()));
                stmt.setString(pos++, identifier);
                stmt.setLong(pos++, System.currentTimeMillis());
            }
            // Try INSERT
            try {
                final boolean result = stmt.executeUpdate() > 0;
                if (result) {
                    modifiedFlag = true;
                }
                return result;
            } catch (final SQLException e) {
                return false;
            }
        } catch (final SQLException e) {
            throw EventSystemExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
            if (modifiedFlag) {
                databaseService.backWritable(contextId, con);
            } else {
                databaseService.backWritableAfterReading(contextId, con);
            }
        }
    }

    private int getContextIdFrom(final Event event) throws OXException {
        final Object val = event.getProperty(EventConstants.PROP_CONTEXT_ID);
        if (null == val) {
            throw EventSystemExceptionCodes.EVENT_NOT_CLAIMABLE.create();
        }
        if (val instanceof Integer) {
            return ((Integer) val).intValue();
        }
        try {
            return Integer.parseInt(val.toString().trim());
        } catch (final NumberFormatException e) {
            throw EventSystemExceptionCodes.EVENT_NOT_CLAIMABLE.create(e, new Object[0]);
        }
    }

    /**
     * Gets the touched context identifiers
     *
     * @return The context identifiers
     */
    public Set<Integer> getContextIds() {
        return contextIds;
    }

}
