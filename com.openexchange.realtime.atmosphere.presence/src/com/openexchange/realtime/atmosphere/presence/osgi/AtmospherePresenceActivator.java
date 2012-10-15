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
import com.openexchange.conversion.simple.SimplePayloadConverter;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.realtime.atmosphere.OXRTConversionHandler;
import com.openexchange.realtime.atmosphere.presence.JSONToPresenceDataConverter;
import com.openexchange.realtime.atmosphere.presence.OXRTPresenceHandler;
import com.openexchange.realtime.atmosphere.presence.PresenceDataToJSONConverter;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.payload.PayloadElementTransformer;
import com.openexchange.realtime.presence.PresenceService;
import com.openexchange.realtime.presence.subscribe.PresenceSubscriptionService;

/**
 * {@link AtmospherePresenceActivator} - Register the presence specific payload converters as SimplePayloadConverters and add a new
 * OXRTHandler service that can handle incoming and outgoing Stanzas.
 * <ol>
 * <li>The <code>SimpleConverterActivator</code> listens for registrations of new <code>SimplePayloadConverters</code>.</li>
 * <li>When we register our presence specific <code>SimplePayloadConverters</code> the <code>SimpleconverterActivator</code> wraps them in a
 * <code>PayloadConverterAdapter</code> and registers them as <code>ResultConverter</code> services</li>
 * <li>The <code>DispatcherActivator</code> is listening for new <code>ResultConverter</code> services and adds them to the
 * <code>DefaultConverter</code></li>
 * <li>The presence <code>Payload</code> can then convert itself via the conversion service offered by the <code>DefaultConverter</code></li>
 * </ol>
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class AtmospherePresenceActivator extends HousekeepingActivator {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(AtmospherePresenceActivator.class);

    @Override
    protected Class<?>[] getNeededServices() {
         return new Class[] { PresenceSubscriptionService.class, PresenceService.class };
    }

    @Override
    protected void handleAvailability(Class<?> clazz) {
        Object service = getService(clazz);
        AtmospherePresenceServiceRegistry.getInstance().addService(clazz, service);
    }

    @Override
    protected void handleUnavailability(Class<?> clazz) {
        AtmospherePresenceServiceRegistry.getInstance().removeService(clazz);
    }

    @Override
    protected void startBundle() throws Exception {
        AtmospherePresenceServiceRegistry serviceRegistry = AtmospherePresenceServiceRegistry.getInstance();
        serviceRegistry.initialize(this, getNeededServices());

        /*
         * After adding the new SimplePayloadConverters that are able to convert from and to PresenceStatus we can register a new
         * OXRTConversionHandler for the PresenceStatus. All this ConversionHandler does is to tell the payload to convert itself into the
         * desired format.
         */
        registerService(SimplePayloadConverter.class, new PresenceDataToJSONConverter());
        registerService(SimplePayloadConverter.class, new JSONToPresenceDataConverter());
        registerService(PayloadElementTransformer.class, new OXRTPresenceHandler());
    }

    @Override
    protected void stopBundle() throws Exception {
        AtmospherePresenceServiceRegistry.getInstance().clearRegistry();
        unregisterServices();
    }

}
