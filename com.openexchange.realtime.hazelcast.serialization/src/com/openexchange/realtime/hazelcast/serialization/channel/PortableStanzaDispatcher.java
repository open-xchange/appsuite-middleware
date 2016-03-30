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

package com.openexchange.realtime.hazelcast.serialization.channel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.exception.OXException;
import com.openexchange.hazelcast.serialization.CustomPortable;
import com.openexchange.realtime.cleanup.GlobalRealtimeCleanup;
import com.openexchange.realtime.dispatch.LocalMessageDispatcher;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.dispatch.Utils;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.hazelcast.serialization.osgi.Services;
import com.openexchange.realtime.hazelcast.serialization.packet.PortableID;
import com.openexchange.realtime.hazelcast.serialization.util.PortableIDToOXExceptionMap;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.util.IDMap;

/**
 * {@link PortableStanzaDispatcher}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.1
 */
public class PortableStanzaDispatcher implements Callable<IDMap<OXException>>, CustomPortable {

    private static final Logger LOG = LoggerFactory.getLogger(PortableStanzaDispatcher.class);

    private static final String FIELD_TARGETS = "targets";

    private static final String FIELD_STANZA = "stanza";

    public static int CLASS_ID = 14;

    private Stanza stanza;

    private final Set<ID> targets;

    /**
     * Initializes a new {@link PortableStanzaDispatcher}.
     *
     * @throws OXException
     */
    public PortableStanzaDispatcher() {
        this.stanza = null;
        this.targets = new HashSet<ID>();
    }

    /**
     * Initializes a new {@link PortableStanzaDispatcher}.
     *
     * @param stanza The stanza to dispatch
     * @throws OXException
     */
    public PortableStanzaDispatcher(Stanza stanza, Set<ID> targets) throws OXException {
        super();
        this.targets = targets;
        this.stanza = stanza;
        if (stanza != null) {
            stanza.transformPayloads("native");
        }
    }

    @Override
    public IDMap<OXException> call() throws Exception {
        stanza.trace("Received remote delivery. Dispatching locally");
        LocalMessageDispatcher dispatcher = Services.getService(LocalMessageDispatcher.class);
        Map<ID, OXException> exceptions = dispatcher.send(stanza, targets);
        /*
         * The Stanza was delivered to this node because the ResourceDirectory listed this node in the routing info. If the Resource isn't
         * available anylonger we remove it from the ResourceDirectory and try to send the Stanza again via the GlobalMessageDispatcher
         * service. This will succeed if the Channel can conjure the Resource.
         */
        if (Utils.shouldResend(exceptions, stanza)) {
            // Can't resend without incrementing but incrementing will mess up client sequences and further communication, so set to -1
            stanza.setSequenceNumber(-1);
            final GlobalRealtimeCleanup cleanup = Services.optService(GlobalRealtimeCleanup.class);
            final MessageDispatcher messageDispatcher = Services.optService(MessageDispatcher.class);
            if (cleanup == null || messageDispatcher == null) {
                LOG.error(
                    "Error while trying to resend.",
                    RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(cleanup == null ? GlobalRealtimeCleanup.class : MessageDispatcher.class));
            } else {
                cleanup.cleanForId(stanza.getTo());
                messageDispatcher.send(stanza);
                // remove the exception that triggered the resend.
                exceptions.remove(stanza.getTo());
            }
        }
        /*
         * We can't return a basic java.util.Map that wraps a com.hazelcast.nio.serialization.Portable as hazelcast won't be able to
         * deserialize the nested portables properly.
         */
        PortableIDToOXExceptionMap portableExceptions = new PortableIDToOXExceptionMap(exceptions);
        return portableExceptions;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        HashSet<PortableID> portableTargets = new HashSet<PortableID>(targets.size());
        for (ID targetID : targets) {
            portableTargets.add(new PortableID(targetID));
        }
        writer.writePortableArray(FIELD_TARGETS, portableTargets.toArray(new Portable[portableTargets.size()]));
        writer.writeByteArray(FIELD_STANZA, getBytes(stanza));
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        Portable[] portableTargets = reader.readPortableArray(FIELD_TARGETS);
        for (Portable portableTarget : portableTargets) {
            if (PortableID.class.isInstance(portableTarget)) {
                targets.add(PortableID.class.cast(portableTarget));
            } else {
                LOG.error("Expected a PortableID instead of {}", portableTarget);
            }
        }
        byte[] stanzaBytes = reader.readByteArray(FIELD_STANZA);
        try {
            stanza = getStanza(stanzaBytes);
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
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

    /**
     * Serialize a {@link Stanza} into a byte array
     * @param stanza The {@link Stanza} to be serialized
     * @return The serialized {@link Stanza} as byte array
     * @throws IOException If the {@link Stanza} can't be serialized
     */
    private static byte[] getBytes(Stanza stanza) throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(stanza);
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Deserialize a {@link Stanza} from a byte array representation. Needs access to all the classes that make up a Stanza via proper OSGI
     * imports.
     *
     * @param stanzaBytes The byte array representation of the Stanza
     * @return The deserialzed {@link Stanza}
     * @throws IOException If reading the byte array fails
     * @throws ClassNotFoundException If the OSGI imports are too restrictive and not all classes that make up a {@link Stanza} subclass are
     *             accessible
     */
    private static Stanza getStanza(byte[] stanzaBytes) throws IOException, ClassNotFoundException {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(stanzaBytes);
        final ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        return Stanza.class.cast(objectInputStream.readObject());
    }

}
