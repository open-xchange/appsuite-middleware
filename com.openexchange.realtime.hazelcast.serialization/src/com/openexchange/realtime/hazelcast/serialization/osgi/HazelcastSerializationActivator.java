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

package com.openexchange.realtime.hazelcast.serialization.osgi;

import org.osgi.framework.ServiceReference;
import com.openexchange.hazelcast.serialization.CustomPortableFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.realtime.cleanup.GlobalRealtimeCleanup;
import com.openexchange.realtime.cleanup.LocalRealtimeCleanup;
import com.openexchange.realtime.dispatch.LocalMessageDispatcher;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.hazelcast.serialization.PortableContextPredicateFactory;
import com.openexchange.realtime.hazelcast.serialization.channel.PortableStanzaDispatcherFactory;
import com.openexchange.realtime.hazelcast.serialization.cleanup.PortableCleanupDispatcherFactory;
import com.openexchange.realtime.hazelcast.serialization.cleanup.PortableCleanupStatusFactory;
import com.openexchange.realtime.hazelcast.serialization.directory.PortableMemberPredicateFactory;
import com.openexchange.realtime.hazelcast.serialization.directory.PortableResourceFactory;
import com.openexchange.realtime.hazelcast.serialization.directory.PortableRoutingInfoFactory;
import com.openexchange.realtime.hazelcast.serialization.group.PortableNotInternalPredicateFactory;
import com.openexchange.realtime.hazelcast.serialization.group.PortableSelectorChoiceFactory;
import com.openexchange.realtime.hazelcast.serialization.packet.PortableIDFactory;
import com.openexchange.realtime.hazelcast.serialization.packet.PortablePresenceFactory;
import com.openexchange.realtime.hazelcast.serialization.util.PortableIDToOXExceptionMapEntryFactory;
import com.openexchange.realtime.hazelcast.serialization.util.PortableIDToOXExceptionMapFactory;

/**
 * {@link HazelcastSerializationActivator}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.6.1
 */
public class HazelcastSerializationActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return null;
    }

    @Override
    protected void startBundle() throws Exception {

        Services.setServiceLookup(this);

        /*
         * The PortableStanzaDispatcher needs:
         * - LocalMessageDispatcher:  For its default operation of delivering stanzas locally on the target node, see
         *                            PortableStanzaDispatcher
         * - LocalRealtimeCleanup:    For its default operation of initiating a local cleanup on the target node, see
         *                            PortableCleanupDispatcher
         * - GlobalRealtimeCleanup:   In case the addressed recipient can't be found. A cleanup has to be triggered before trying to resend
         *                            , see PortableStanzaDispatcher
         * - GlobalMessageDispatcher: If local delivery fails and delivery has to be retried globally after removing the Resource that was
         *                            listed as local to this node from the ResourceDirectory, see PortableStanzaDispatcher
         */
        track(LocalMessageDispatcher.class, new SimpleRegistryListener<LocalMessageDispatcher>() {

            @Override
            public void added(ServiceReference<LocalMessageDispatcher> ref, LocalMessageDispatcher service) {
                addService(LocalMessageDispatcher.class, service);
            }

            @Override
            public void removed(ServiceReference<LocalMessageDispatcher> ref, LocalMessageDispatcher service) {
                removeService(LocalMessageDispatcher.class);
            }
        });

        track(MessageDispatcher.class, new SimpleRegistryListener<MessageDispatcher>() {

            @Override
            public void added(ServiceReference<MessageDispatcher> ref, MessageDispatcher service) {
                addService(MessageDispatcher.class, service);
            }

            @Override
            public void removed(ServiceReference<MessageDispatcher> ref, MessageDispatcher service) {
                removeService(LocalMessageDispatcher.class);
            }
        });

        track(GlobalRealtimeCleanup.class, new SimpleRegistryListener<GlobalRealtimeCleanup>() {

            @Override
            public void added(ServiceReference<GlobalRealtimeCleanup> ref, GlobalRealtimeCleanup service) {
                addService(GlobalRealtimeCleanup.class, service);
            }

            @Override
            public void removed(ServiceReference<GlobalRealtimeCleanup> ref, GlobalRealtimeCleanup service) {
                removeService(GlobalRealtimeCleanup.class);
            }
        });

        track(LocalRealtimeCleanup.class, new SimpleRegistryListener<LocalRealtimeCleanup>() {

            @Override
            public void added(ServiceReference<LocalRealtimeCleanup> ref, LocalRealtimeCleanup service) {
                addService(LocalRealtimeCleanup.class, service);
            }

            @Override
            public void removed(ServiceReference<LocalRealtimeCleanup> ref, LocalRealtimeCleanup service) {
                removeService(LocalRealtimeCleanup.class);
            }
        });

        registerService(CustomPortableFactory.class, new PortableContextPredicateFactory());
        registerService(CustomPortableFactory.class, new PortableIDFactory());
        registerService(CustomPortableFactory.class, new PortableMemberPredicateFactory());
        registerService(CustomPortableFactory.class, new PortableNotInternalPredicateFactory());
        registerService(CustomPortableFactory.class, new PortablePresenceFactory());
        registerService(CustomPortableFactory.class, new PortableResourceFactory());
        registerService(CustomPortableFactory.class, new PortableRoutingInfoFactory());
        registerService(CustomPortableFactory.class, new PortableSelectorChoiceFactory());
        registerService(CustomPortableFactory.class, new PortableStanzaDispatcherFactory());
        registerService(CustomPortableFactory.class, new PortableCleanupDispatcherFactory());
        registerService(CustomPortableFactory.class, new PortableCleanupStatusFactory());
        registerService(CustomPortableFactory.class, new PortableIDToOXExceptionMapFactory());
        registerService(CustomPortableFactory.class, new PortableIDToOXExceptionMapEntryFactory());
    }

    @Override
    protected void stopBundle() throws Exception {
        Services.setServiceLookup(null);
        super.stopBundle();
    }

}
