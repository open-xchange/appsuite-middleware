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

package com.openexchange.realtime.json.osgi;

import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.customizer.file.AdditionalFileField;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.conversion.simple.SimpleConverter;
import com.openexchange.conversion.simple.SimplePayloadConverter;
import com.openexchange.exception.OXException;
import com.openexchange.management.ManagementService;
import com.openexchange.realtime.Channel;
import com.openexchange.realtime.cleanup.GlobalRealtimeCleanup;
import com.openexchange.realtime.cleanup.RealtimeJanitor;
import com.openexchange.realtime.directory.ResourceDirectory;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.group.DistributedGroupManager;
import com.openexchange.realtime.handle.StanzaQueueService;
import com.openexchange.realtime.json.actions.RealtimeActions;
import com.openexchange.realtime.json.fields.ResourceIDField;
import com.openexchange.realtime.json.impl.JSONChannel;
import com.openexchange.realtime.json.impl.RTJSONHandler;
import com.openexchange.realtime.json.management.ManagementHouseKeeper;
import com.openexchange.realtime.json.payload.converter.IDToJSONConverter;
import com.openexchange.realtime.json.payload.converter.JSONToIDConverter;
import com.openexchange.realtime.json.payload.converter.JSONToRealtimeExceptionConverter;
import com.openexchange.realtime.json.payload.converter.JSONToStackTraceElementConverter;
import com.openexchange.realtime.json.payload.converter.JSONToThrowableConverter;
import com.openexchange.realtime.json.payload.converter.RealtimeExceptionToJSONConverter;
import com.openexchange.realtime.json.payload.converter.StackTraceElementToJSONConverter;
import com.openexchange.realtime.json.payload.converter.ThrowableToJSONConverter;
import com.openexchange.realtime.json.payload.converter.primitive.ByteToJSONConverter;
import com.openexchange.realtime.json.payload.converter.primitive.IntegerToJSONConverter;
import com.openexchange.realtime.json.payload.converter.primitive.JSONToByteConverter;
import com.openexchange.realtime.json.payload.converter.primitive.JSONToIntegerConverter;
import com.openexchange.realtime.json.payload.converter.primitive.JSONToStringConverter;
import com.openexchange.realtime.json.payload.converter.primitive.StringToJSONConverter;
import com.openexchange.realtime.json.presence.converter.JSONToPresenceStateConverter;
import com.openexchange.realtime.json.presence.converter.PresenceStateToJSONConverter;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.packet.PresenceState;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.payload.converter.PayloadTreeConverter;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;

public class RTJSONActivator extends AJAXModuleActivator {

    private static final Logger LOG = LoggerFactory.getLogger(RTJSONActivator.class);
    private final AtomicBoolean isStopped = new AtomicBoolean(true);
    private RealtimeActions realtimeActions;
    private volatile RTJSONHandler handler;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {
            ConfigurationService.class, SessiondService.class, MessageDispatcher.class, SimpleConverter.class,
            ResourceDirectory.class, StanzaQueueService.class, PayloadTreeConverter.class, CapabilityService.class, TimerService.class,
            ThreadPoolService.class, ManagementService.class, GlobalRealtimeCleanup.class, DistributedGroupManager.class};
    }

    @Override
    protected void startBundle() throws Exception {
        JSONServiceRegistry.SERVICES.set(this);
        ManagementHouseKeeper managementHouseKeeper = ManagementHouseKeeper.getInstance();
        managementHouseKeeper.initialize(this);

        RTJSONHandler handler = new RTJSONHandler();
        this.handler = handler;
        registerService(Channel.class, new JSONChannel(handler));

        /*
         * Register the package specific payload converters. The SimpleConverterActivator listens for registrations of new
         * SimplePayloadConverters. When new SimplePayloadConverters are added they are wrapped in a PayloadConverterAdapter and registered
         * as ResultConverter service so they can be added to the DefaultConverter (as the DispatcherActivator is listening for new
         * ResultConverter services) which then can be used by the {@link PayloadElementTransformer} to convert them via the conversion
         * service offered by the {@link DefaultConverter}
         */
        registerService(SimplePayloadConverter.class, new ByteToJSONConverter());
        registerService(SimplePayloadConverter.class, new JSONToByteConverter());
        registerService(SimplePayloadConverter.class, new StringToJSONConverter());
        registerService(SimplePayloadConverter.class, new JSONToStringConverter());
        registerService(SimplePayloadConverter.class, new JSONToPresenceStateConverter());
        registerService(SimplePayloadConverter.class, new PresenceStateToJSONConverter());
        registerService(SimplePayloadConverter.class, new JSONToStackTraceElementConverter());
        registerService(SimplePayloadConverter.class, new StackTraceElementToJSONConverter());
        registerService(SimplePayloadConverter.class, new JSONToThrowableConverter());
        registerService(SimplePayloadConverter.class, new ThrowableToJSONConverter());
        registerService(SimplePayloadConverter.class, new JSONToRealtimeExceptionConverter());
        registerService(SimplePayloadConverter.class, new RealtimeExceptionToJSONConverter());
        registerService(SimplePayloadConverter.class, new JSONToIDConverter());
        registerService(SimplePayloadConverter.class, new IDToJSONConverter());
        registerService(SimplePayloadConverter.class, new JSONToIntegerConverter());
        registerService(SimplePayloadConverter.class, new IntegerToJSONConverter());

        // Add Transformers using Converters
        PayloadTreeConverter converter = getService(PayloadTreeConverter.class);
        converter.declarePreferredFormat(Presence.STATUS_PATH, PresenceState.class.getSimpleName());
        converter.declarePreferredFormat(Presence.MESSAGE_PATH, String.class.getSimpleName());
        converter.declarePreferredFormat(Presence.PRIORITY_PATH, Byte.class.getSimpleName());
        converter.declarePreferredFormat(Stanza.ERROR_PATH, RealtimeException.class.getSimpleName());
        realtimeActions = new RealtimeActions(this, handler.getStateManager(), handler.getProtocolHandler());
        registerModule(realtimeActions, "rt");

        getService(CapabilityService.class).declareCapability("rt");
        try {
            managementHouseKeeper.exposeManagementObjects();
        } catch (OXException oxe) {
            LOG.error("Failed to expose ManagementObjects", oxe);
        }
        /*
         * register an additional file field providing the realtime resource identifier
         */
        registerService(AdditionalFileField.class, new ResourceIDField());
        /*
         * Register all RealtimeJanitor services contained in this bundle
         */
        for(RealtimeJanitor realtimeJanitor : RealtimeJanitors.getInstance().getJanitors()) {
            registerService(RealtimeJanitor.class, realtimeJanitor, realtimeJanitor.getServiceProperties());
        }
        isStopped.set(false);
    }

    @Override
    public void stopBundle() throws Exception {
        if (isStopped.compareAndSet(false, true)) {
            ManagementHouseKeeper.getInstance().cleanup();
            RealtimeJanitors.getInstance().cleanup();
            RTJSONHandler handler = this.handler;
            if (null != handler) {
                handler.shutDownCleanupTimer();
                this.handler = null;
            }
            JSONServiceRegistry.SERVICES.set(null);
            super.stopBundle();
        }
    }

    @Override
    protected void handleAvailability(Class<?> clazz) {
        if (allAvailable()) {
            LOG.info("{} regained all needed services {}. Going to restart bundle.", this.getClass().getSimpleName(), clazz.getSimpleName());
            try {
                startBundle();
            } catch (Exception e) {
                LOG.error("Error while starting bundle.", e);
            }
        }
    }

    @Override
    protected void handleUnavailability(Class<?> clazz) {
        if (!isStopped.get()) {
            LOG.warn(
                "{} is handling unavailibility of needed service {}. Going to stop bundle.",
                this.getClass().getSimpleName(),
                clazz.getSimpleName());
            try {
                this.stopBundle();
            } catch (Exception e) {
                LOG.error("Error while stopping bundle.", e);
            }
        }
    }

}
