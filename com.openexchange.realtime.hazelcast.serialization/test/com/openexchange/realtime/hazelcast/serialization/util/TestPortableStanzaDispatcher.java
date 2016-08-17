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

package com.openexchange.realtime.hazelcast.serialization.util;

import java.io.IOException;
import java.util.concurrent.Callable;
import com.hazelcast.nio.serialization.ClassDefinition;
import com.hazelcast.nio.serialization.ClassDefinitionBuilder;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.exception.OXException;
import com.openexchange.hazelcast.serialization.CustomPortable;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.hazelcast.serialization.packet.PortableID;
import com.openexchange.realtime.util.IDMap;

/**
 * {@link TestPortableStanzaDispatcher}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.2
 */
public class TestPortableStanzaDispatcher implements Callable<IDMap<OXException>>, CustomPortable {

    public static final int CLASS_ID = 1337;

    private static final String FIELD_IDS = "ids";

    private static final String FIELD_RETURN_EXCEPTION = "returnException";

    public static ClassDefinition CLASS_DEFINITION = null;

    static {
        CLASS_DEFINITION = new ClassDefinitionBuilder(FACTORY_ID, CLASS_ID)
        .addBooleanField(FIELD_RETURN_EXCEPTION)
        .addPortableArrayField(FIELD_IDS, PortableID.CLASS_DEFINITION)
        .build();
    }

    private PortableID[] ids;

    private boolean returnException;

    public TestPortableStanzaDispatcher() {
        this.ids = new PortableID[0];
        this.returnException = false;
    }

    public TestPortableStanzaDispatcher(boolean returnException, PortableID... ids) {
        this.ids = ids;
        this.returnException = returnException;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeBoolean(FIELD_RETURN_EXCEPTION, returnException);
        Portable[] portableIDs = new Portable[ids.length];
        System.arraycopy(ids, 0, portableIDs, 0, ids.length);
        writer.writePortableArray(FIELD_IDS, portableIDs);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        returnException = reader.readBoolean(FIELD_RETURN_EXCEPTION);
        Portable[] portableIDs = reader.readPortableArray(FIELD_IDS);
        PortableID[] tempIDs = new PortableID[portableIDs.length];
        System.arraycopy(portableIDs, 0, tempIDs, 0, portableIDs.length);
        ids=tempIDs;
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
    public IDMap<OXException> call() throws Exception {
        PortableIDToOXExceptionMap exceptionMap = new PortableIDToOXExceptionMap();
        if(returnException) {
            for (PortableID id : ids) {
                OXException rex = RealtimeExceptionCodes.UNEXPECTED_ERROR.create("Good day, sir!");
                exceptionMap.put(id, rex);
            }
        }
        return exceptionMap;
    }

}
