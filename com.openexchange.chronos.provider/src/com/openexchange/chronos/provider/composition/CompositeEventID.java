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

package com.openexchange.chronos.provider.composition;

import java.util.List;
import org.dmfs.rfc5545.DateTime;
import com.google.common.io.BaseEncoding;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.DefaultRecurrenceId;
import com.openexchange.java.Charsets;
import com.openexchange.tools.id.IDMangler;

/**
 * {@link CompositeEventID}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CompositeEventID extends CompositeID {

    /**
     * Parses a {@link CompositeEventID} from a mangled unique identifier string. The optional recurrence identifier may be supplied as
     * trailing date-time.
     *
     * @param uniqueID The composite identifier; e.g. <code>4://35/8687</code> or <code>4://8687/20170723</code>
     * @throws IllegalArgumentException If passed identifier can't be unmangled
     */
    public static CompositeEventID parse(String uniqueID) {
        List<String> unmangled = IDMangler.unmangle(decode(uniqueID));
        if (null == unmangled || 3 > unmangled.size()) {
            throw new IllegalArgumentException(uniqueID);
        }
        int accountId = Integer.parseInt(unmangled.get(0));
        String folderId = unmangled.get(1);
        String eventId = unmangled.get(2);
        RecurrenceId recurrenceId = 3 < unmangled.size() ? new DefaultRecurrenceId(DateTime.parse(unmangled.get(3))) : null;
        return new CompositeEventID(accountId, folderId, eventId, recurrenceId);
    }

    private final String folderId;
    private final String eventId;
    private final RecurrenceId recurrenceId;

    /**
     * Initializes a new {@link CompositeEventID}.
     *
     * @param accountId The account identifier
     * @param folderId The folder identifier
     * @param eventId The event identifier
     */
    public CompositeEventID(int accountId, String folderId, String eventId) {
        this(accountId, folderId, eventId, null);
    }

    /**
     * Initializes a new {@link CompositeEventID}.
     *
     * @param accountId The account identifier
     * @param folderId The folder identifier
     * @param eventId The event identifier
     * @param recurrenceId The optional recurrence identifier
     */
    public CompositeEventID(int accountId, String folderId, String eventId, RecurrenceId recurrenceId) {
        super(accountId);
        this.folderId = folderId;
        this.eventId = eventId;
        this.recurrenceId = recurrenceId;
    }

    /**
     * Initializes a new {@link CompositeEventID}.
     *
     * @param seriesMasterID The composite identifier of the series master event
     * @param recurrenceId The recurrence identifier
     */
    public CompositeEventID(CompositeEventID seriesMasterID, RecurrenceId recurrenceId) {
        this(seriesMasterID.getAccountId(), seriesMasterID.getFolderId(), seriesMasterID.getEventId(), recurrenceId);
    }

    /**
     * Gets the folder identifier.
     *
     * @return The folder identifier
     */
    public String getFolderId() {
        return folderId;
    }

    /**
     * Gets the event identifier.
     *
     * @return The event identifier
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Gets the recurrence identifier.
     *
     * @return The recurrence identifier, or <code>null</code> if not specified
     */
    public RecurrenceId getRecurrenceId() {
        return recurrenceId;
    }

    @Override
    public String toUniqueID() {
        String mangledId;
        if (null == recurrenceId) {
            mangledId = IDMangler.mangle(String.valueOf(accountId), folderId, eventId);
        } else {
            mangledId = IDMangler.mangle(String.valueOf(accountId), folderId, eventId, String.valueOf(recurrenceId.getValue()));
        }
        return encode(mangledId);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((eventId == null) ? 0 : eventId.hashCode());
        result = prime * result + ((folderId == null) ? 0 : folderId.hashCode());
        result = prime * result + ((recurrenceId == null) ? 0 : recurrenceId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        CompositeEventID other = (CompositeEventID) obj;
        if (eventId == null) {
            if (other.eventId != null)
                return false;
        } else if (!eventId.equals(other.eventId))
            return false;
        if (folderId == null) {
            if (other.folderId != null)
                return false;
        } else if (!folderId.equals(other.folderId))
            return false;
        if (recurrenceId == null) {
            if (other.recurrenceId != null)
                return false;
        } else if (!recurrenceId.equals(other.recurrenceId))
            return false;
        return true;
    }

    private static String encode(String id) {
        if (null == id) {
            return null;
        }
        id += "/PPPP";
        return BaseEncoding.base64Url().omitPadding().encode(id.getBytes(Charsets.UTF_8));
    }

    private static String decode(String encodedId) {
        if (null == encodedId) {
            return null;
        }
        while (0 != encodedId.length() % 4) {
            encodedId = encodedId + "P";
        }
        String decodedId = new String(BaseEncoding.base64Url().omitPadding().decode(encodedId), Charsets.UTF_8);
        int idx = decodedId.lastIndexOf('/');
        if (-1 != idx) {
            decodedId = decodedId.substring(0, idx);
        }
        return decodedId;
    }

}
