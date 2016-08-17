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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.hazelcast.nio.serialization.ClassDefinition;
import com.hazelcast.nio.serialization.ClassDefinitionBuilder;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.exception.OXException;
import com.openexchange.hazelcast.serialization.CustomPortable;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.util.IDMap;

/**
 * {@link PortableIDToOXExceptionMap} - A portable subclass of the {@link IDMap}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.2
 */
public class PortableIDToOXExceptionMap extends IDMap<OXException> implements CustomPortable {

    public static final int CLASS_ID = 22;

    private static final String MAP_ENTRIES = "map_entries";

    public static ClassDefinition CLASS_DEFINITION = null;

    static {
        CLASS_DEFINITION = new ClassDefinitionBuilder(FACTORY_ID, CLASS_ID)
        .addPortableArrayField(MAP_ENTRIES, PortableIDToOXExceptionMapEntry.CLASS_DEFINITION)
        .build();
    }

    /**
     * Initializes a new {@link PortableIDToOXExceptionMap}.
     */
    public PortableIDToOXExceptionMap() {
        super();
    }

    /**
     * Initializes a new {@link PortableIDToOXExceptionMap} based on an already existing {@link Map<ID, OXException>}
     * @param exceptions the already existing {@link Map<ID, OXException>}
     */
    public PortableIDToOXExceptionMap(Map<ID, OXException> exceptions) {
        super();
        delegate.putAll(exceptions);
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        if(entrySet().size() > 0) {
            List<PortableIDToOXExceptionMapEntry> entryList = new ArrayList<PortableIDToOXExceptionMapEntry>(entrySet().size());
            for(Entry<ID, OXException> entry : entrySet()) {
                entryList.add(new PortableIDToOXExceptionMapEntry(entry));
            }
            writer.writePortableArray(MAP_ENTRIES, entryList.toArray(new Portable[entryList.size()]));
        } else {
            writer.writePortableArray(MAP_ENTRIES, new Portable[0]);
        }

    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        delegate.clear();
        if(reader.getFieldNames().contains(MAP_ENTRIES)) {
            Portable[] map_entries = reader.readPortableArray(MAP_ENTRIES);
            if(map_entries != null && map_entries.length > 0) {
                for(int i = 0; i < map_entries.length; i++) {
                    PortableIDToOXExceptionMapEntry entry = PortableIDToOXExceptionMapEntry.class.cast(map_entries[i]);
                    delegate.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    @Override
    public int getFactoryId() {
        return FACTORY_ID;
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

}
