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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.session.reservation.impl.portable;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.hazelcast.serialization.AbstractCustomPortable;
import com.openexchange.session.reservation.Reservation;

/**
 * {@link PortableReservation}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class PortableReservation extends AbstractCustomPortable {

    /** The unique portable class ID of the {@link PortableSession} */
    public static final int CLASS_ID = 23;

    public static final String PARAMETER_TOKEN = "token";
    public static final String PARAMETER_TIMEOUT = "timeout";
    public static final String PARAMETER_CREATION_STAMP = "creationStamp";
    public static final String PARAMETER_CONTEXT_ID = "contextId";
    public static final String PARAMETER_USER_ID = "userId";
    public static final String PARAMETER_STATE = "state";

    private int contextId;
    private int userId;
    private String token;
    private long timeout;
    private long creationStamp;
    private Map<String, String> state;

    /**
     * Initializes a new {@link PortableReservation}.
     */
    public PortableReservation() {
        super();
    }

    /**
     * Initializes a new {@link PortableReservation}.
     */
    public PortableReservation(Reservation reservation) {
        super();
        contextId = reservation.getContextId();
        userId = reservation.getUserId();
        token = reservation.getToken();
        timeout = reservation.getTimeoutMillis();
        creationStamp = reservation.getCreationStamp();
        state = reservation.getState();
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeInt(PARAMETER_CONTEXT_ID, contextId);
        writer.writeInt(PARAMETER_USER_ID, userId);
        writer.writeUTF(PARAMETER_TOKEN, token);
        writer.writeLong(PARAMETER_TIMEOUT, timeout);
        writer.writeLong(PARAMETER_CREATION_STAMP, creationStamp);
        writer.getRawDataOutput().writeObject(null == state ? new LinkedHashMap<String, String>(0) : state);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        contextId = reader.readInt(PARAMETER_CONTEXT_ID);
        userId = reader.readInt(PARAMETER_USER_ID);
        token = reader.readUTF(PARAMETER_TOKEN);
        timeout = reader.readLong(PARAMETER_TIMEOUT);
        creationStamp = reader.readLong(PARAMETER_CREATION_STAMP);
        Map<String, String> m = reader.getRawDataInput().readObject();
        state = m.isEmpty() ? null : m;
    }

    /**
     * Gets the contextId
     *
     * @return The contextId
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * Gets the userId
     *
     * @return The userId
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the token
     *
     * @return The token
     */
    public String getToken() {
        return token;
    }

    /**
     * Gets the timeout
     *
     * @return The timeout
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * Gets the creationStamp
     *
     * @return The creationStamp
     */
    public long getCreationStamp() {
        return creationStamp;
    }

    /**
     * Gets the state
     *
     * @return The state
     */
    public Map<String, String> getState() {
        return state;
    }

}
