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

package com.openexchange.realtime.hazelcast.serialization;

import java.io.IOException;
import java.util.Map.Entry;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.query.Predicate;
import com.openexchange.hazelcast.serialization.CustomPortable;
import com.openexchange.realtime.hazelcast.serialization.directory.PortableResource;
import com.openexchange.realtime.hazelcast.serialization.packet.PortableID;

/**
 * {@link PortableContextPredicate} - Predicate to lookup Resources based on the context identifier.
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.0
 */
public class PortableContextPredicate implements Predicate<PortableID, PortableResource>, CustomPortable {

    private static final long serialVersionUID = -8551722082184158155L;

    public static final int CLASS_ID = 13;

    private String contextId;

    private static final String CONTEXT_ID = "contextid";

    /**
     * Initializes a new {@link PortableContextPredicate}.
     * 
     * @param contextId The contextid to use when filtering/applying the predicate
     */
    public PortableContextPredicate(String contextId) {
        this.contextId = contextId.trim();
    }

    /**
     * Initializes a new {@link PortableContextPredicate}.
     */
    public PortableContextPredicate() {
        super();
    }

    @Override
    public boolean apply(Entry<PortableID, PortableResource> mapEntry) {
        PortableID id = mapEntry.getKey();
        return contextId.equals(id.getContext());
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeUTF(CONTEXT_ID, contextId);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        contextId = reader.readUTF(CONTEXT_ID);
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
