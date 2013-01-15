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

package com.openexchange.realtime.atmosphere.presence.osgi;

import org.apache.commons.logging.Log;
import org.osgi.framework.ServiceReference;
import com.openexchange.conversion.simple.SimplePayloadConverter;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.realtime.MessageDispatcher;
import com.openexchange.realtime.atmosphere.payload.converter.primitive.ByteToJSONConverter;
import com.openexchange.realtime.atmosphere.payload.converter.primitive.JSONToByteConverter;
import com.openexchange.realtime.atmosphere.payload.converter.primitive.JSONToStringConverter;
import com.openexchange.realtime.atmosphere.payload.converter.primitive.StringToJSONConverter;
import com.openexchange.realtime.atmosphere.payload.transformer.AtmospherePayloadElementTransformer;
import com.openexchange.realtime.atmosphere.presence.converter.JSONToPresenceStateConverter;
import com.openexchange.realtime.atmosphere.presence.converter.PresenceStateToJSONConverter;
import com.openexchange.realtime.atmosphere.presence.handler.OXRTPresenceHandler;
import com.openexchange.realtime.atmosphere.stanza.StanzaHandler;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.packet.PresenceState;
import com.openexchange.realtime.presence.PresenceStatusService;
import com.openexchange.realtime.presence.subscribe.PresenceSubscriptionService;

/**
 * {@link AtmospherePresenceActivator} - Register the presence specific PayloadTransformers and Mappings from ElementPath to Class<?> and
 * add a new OXRTHandler that can handle incoming and outgoing Presence Stanzas.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class AtmospherePresenceActivator extends HousekeepingActivator {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(AtmospherePresenceActivator.class);
    private final OXRTPresenceHandler presenceHandler;

    /**
     * Initializes a new {@link AtmospherePresenceActivator}.
     */
    public AtmospherePresenceActivator() {
        presenceHandler = new OXRTPresenceHandler();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { PresenceSubscriptionService.class, PresenceStatusService.class, MessageDispatcher.class };
    }

    @Override
    protected void handleAvailability(Class<?> clazz) {
        //make sure the PresenceChangeListeners always have the proper service reference
        if(PresenceStatusService.class.equals(clazz)) {

        }
    }

    @Override
    protected void handleUnavailability(Class<?> clazz) {
      //make sure the PresenceChangeListeners always have the proper service reference
        if(PresenceStatusService.class.equals(clazz)) {

        }
    }

    @Override
    protected void startBundle() throws Exception {
        AtmospherePresenceServiceRegistry.SERVICES.set(this);

        track(PresenceStatusService.class, new SimpleRegistryListener<PresenceStatusService>() {

            @Override
            public void added(final ServiceReference<PresenceStatusService> ref, final PresenceStatusService presenceStatusService) {
//                extensions.addPayloadElementTransFormer(transformer);
//                register PresenceChangeListener with new presenceStatusService
            }

            @Override
            public void removed(final ServiceReference<PresenceStatusService> ref, final PresenceStatusService presenceStatusService) {
//                extensions.removePayloadElementTransformer(transformer);
//                remove PresenceChangeListener from old presenceStatusService
            }
        });

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

        // Add Transformers using Converters
        registerService(
            AtmospherePayloadElementTransformer.class,
            new AtmospherePayloadElementTransformer(PresenceState.class.getSimpleName(), Presence.STATUS_PATH));
        registerService(AtmospherePayloadElementTransformer.class, new AtmospherePayloadElementTransformer(
            String.class.getSimpleName(),
            Presence.MESSAGE_PATH));
        registerService(AtmospherePayloadElementTransformer.class, new AtmospherePayloadElementTransformer(
            Byte.class.getSimpleName(),
            Presence.PRIORITY_PATH));
        // registerService(AtmospherePayloadElementTransformer.class,
        // new AtmospherePayloadElementTransformer(Byte.class.getSimpleName(), Presence.ERROR_PATH));

        // Add Presence specific handler
        registerService(StanzaHandler.class, new OXRTPresenceHandler());
    }

    @Override
    protected void stopBundle() throws Exception {
        AtmospherePresenceServiceRegistry.getInstance().clearRegistry();
        unregisterServices();
    }

}
