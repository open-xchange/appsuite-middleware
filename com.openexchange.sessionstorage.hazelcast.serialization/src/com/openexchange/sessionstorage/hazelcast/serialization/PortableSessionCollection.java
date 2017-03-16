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

package com.openexchange.sessionstorage.hazelcast.serialization;

import java.io.IOException;
import com.hazelcast.nio.serialization.ClassDefinition;
import com.hazelcast.nio.serialization.ClassDefinitionBuilder;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.hazelcast.serialization.CustomPortable;
import com.openexchange.session.Session;

/**
 * {@link PortableSessionCollection} - The portable representation for {@link Session} type.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class PortableSessionCollection implements CustomPortable {

    /** The unique portable class ID of the {@link PortableSessionCollection} */
    public static final int CLASS_ID = 403;

    /** The class definition for PortableCacheEvent */
    public static ClassDefinition CLASS_DEFINITION = new ClassDefinitionBuilder(FACTORY_ID, CLASS_ID)
        .addPortableArrayField("sessions", PortableSession.CLASS_DEFINITION)
        .build();

    // -------------------------------------------------------------------------------------------------

    private PortableSession[] sessions;

    /**
     * Initializes a new {@link PortableSessionCollection}.
     */
    public PortableSessionCollection() {
        super();
        sessions = new PortableSession[0];
    }

    /**
     * Initializes a new {@link PortableSessionCollection}.
     *
     * @param session The underlying session
     */
    public PortableSessionCollection(PortableSession[] sessions) {
        super();
        this.sessions = sessions;
    }

    /**
     * Gets the sessions
     *
     * @return The sessions
     */
    public PortableSession[] getSessions() {
        return sessions;
    }

    @Override
    public int getFactoryId() {
        return FACTORY_ID;
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writePortableArray("sessions", sessions);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        Portable[] portables = reader.readPortableArray("sessions");
        sessions = new PortableSession[portables.length];
        for (int i = 0; i < portables.length; i++) {
            sessions[i] = (PortableSession) portables[i];
        }
    }

}
