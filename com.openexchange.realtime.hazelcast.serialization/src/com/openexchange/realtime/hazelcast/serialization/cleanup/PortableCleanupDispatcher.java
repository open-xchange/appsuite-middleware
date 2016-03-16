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

package com.openexchange.realtime.hazelcast.serialization.cleanup;

import java.io.IOException;
import java.util.concurrent.Callable;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hazelcast.nio.serialization.ClassDefinition;
import com.hazelcast.nio.serialization.ClassDefinitionBuilder;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.hazelcast.serialization.AbstractCustomPortable;
import com.openexchange.osgi.ShutDownRuntimeException;
import com.openexchange.realtime.cleanup.LocalRealtimeCleanup;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.hazelcast.serialization.osgi.Services;
import com.openexchange.realtime.hazelcast.serialization.packet.PortableID;
import com.openexchange.realtime.packet.ID;

/**
 * {@link PortableCleanupDispatcher} - Issues a cleanup on the LocalRealtimeCleanup service.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.1
 */
public class PortableCleanupDispatcher extends AbstractCustomPortable implements Callable<Void> {

    private static final Logger LOG = LoggerFactory.getLogger(PortableCleanupDispatcher.class);

    public static final int CLASS_ID = 15;

    private static final String FIELD_ID = "id";

    public static ClassDefinition CLASS_DEFINITION = null;

    static {
        CLASS_DEFINITION = new ClassDefinitionBuilder(FACTORY_ID, CLASS_ID)
        .addPortableField(FIELD_ID, PortableID.CLASS_DEFINITION)
        .build();
    }

    private ID id;

    /**
     * Initializes a new {@link PortableCleanupDispatcher}.
     *
     * @param id The ID to clean up for.
     * @param cleanupScopes The scopes to clean up on the remote machines.
     */
    public PortableCleanupDispatcher(ID id) {
        Validate.notNull(id, "Mandatory parameter id is missing.");
        this.id = id;
    }

    /**
     * Initializes a new {@link PortableCleanupDispatcher}.
     */
    public PortableCleanupDispatcher() {
        super();
    }

    @Override
    public Void call() throws Exception {
        // Do the local cleanup via a simple service call
        LocalRealtimeCleanup localRealtimeCleanup;
        try {
            localRealtimeCleanup = Services.optService(LocalRealtimeCleanup.class);
            if (localRealtimeCleanup != null) {
                localRealtimeCleanup.cleanForId(id);
            } else {
                LOG.error("Error while trying to cleanup for ResponseChannel ID: {}", id, RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(LocalRealtimeCleanup.class.getName()));
            }
        } catch (ShutDownRuntimeException shutDown) {
            // Shutting down
            LOG.debug("Unable to start local cleanup for ResponseChannel ID {} due to shut-down.", id, shutDown);
        }
        return null;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writePortable(FIELD_ID, new PortableID(id));
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        //http://bugs.java.com/view_bug.do?bug_id=6302954
        id = reader.<PortableID>readPortable(FIELD_ID);
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

}
