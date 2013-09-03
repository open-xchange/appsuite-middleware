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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import org.osgi.framework.BundleContext;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.conversion.simple.SimpleConverter;
import com.openexchange.conversion.simple.SimplePayloadConverter;
import com.openexchange.realtime.Channel;
import com.openexchange.realtime.directory.ResourceDirectory;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.handle.StanzaQueueService;
import com.openexchange.realtime.json.JSONConfig;
import com.openexchange.realtime.json.actions.RealtimeActions;
import com.openexchange.realtime.json.impl.JSONChannel;
import com.openexchange.realtime.json.impl.RTJSONHandler;
import com.openexchange.realtime.json.payload.converter.JSONToRealtimeExceptionConverter;
import com.openexchange.realtime.json.payload.converter.JSONToStackTraceElementConverter;
import com.openexchange.realtime.json.payload.converter.JSONToThrowableConverter;
import com.openexchange.realtime.json.payload.converter.RealtimeExceptionToJSONConverter;
import com.openexchange.realtime.json.payload.converter.StackTraceElementToJSONConverter;
import com.openexchange.realtime.json.payload.converter.ThrowableToJSONConverter;
import com.openexchange.realtime.json.payload.converter.primitive.ByteToJSONConverter;
import com.openexchange.realtime.json.payload.converter.primitive.JSONToByteConverter;
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

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] {
            ConfigurationService.class, SessiondService.class, MessageDispatcher.class, SimpleConverter.class,
            ResourceDirectory.class, StanzaQueueService.class, PayloadTreeConverter.class, CapabilityService.class, TimerService.class, ThreadPoolService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        JSONServiceRegistry.SERVICES.set(this);
        JSONConfig atmosphereConfig = JSONConfig.getInstance();
        atmosphereConfig.start();

        RTJSONHandler handler = new RTJSONHandler();
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

        // Add Transformers using Converters
        PayloadTreeConverter converter = getService(PayloadTreeConverter.class);
        converter.declarePreferredFormat(Presence.STATUS_PATH, PresenceState.class.getSimpleName());
        converter.declarePreferredFormat(Presence.MESSAGE_PATH, String.class.getSimpleName());
        converter.declarePreferredFormat(Presence.PRIORITY_PATH, Byte.class.getSimpleName());
        converter.declarePreferredFormat(Stanza.ERROR_PATH, RealtimeException.class.getSimpleName());
        
        registerModule(new RealtimeActions(this, handler.getStateManager(), handler.getProtocolHandler(), handler.getProtocol()), "rt");

        getService(CapabilityService.class).declareCapability("rt");

    }

    @Override
    public void stop(BundleContext context) throws Exception {
        JSONServiceRegistry.SERVICES.set(null);
        super.stop(context);
    }

}
