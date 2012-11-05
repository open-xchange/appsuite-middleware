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

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.realtime.atmosphere.osgi.service.AtmosphereExtensionService;
import com.openexchange.realtime.atmosphere.presence.handler.OXRTPresenceHandler;
import com.openexchange.realtime.atmosphere.presence.transformer.PresenceStateTransformer;
import com.openexchange.realtime.atmosphere.stanza.StanzaHandler;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.packet.PresenceState;
import com.openexchange.realtime.payload.transformer.PayloadElementTransformer;
import com.openexchange.realtime.presence.PresenceService;
import com.openexchange.realtime.presence.subscribe.PresenceSubscriptionService;

/**
 * {@link AtmospherePresenceActivator} - Register the presence specific PayloadTransformers and Mappings from ElementPath to Class<?> and
 * add a new OXRTHandler that can handle incoming and outgoing Presence Stanzas.
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class AtmospherePresenceActivator extends HousekeepingActivator {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(AtmospherePresenceActivator.class);

    private List<StanzaHandler> registeredHandlers;

    private List<PayloadElementTransformer> registeredTransformers;

    /**
     * Initializes a new {@link AtmospherePresenceActivator}.
     */
    public AtmospherePresenceActivator() {
        registeredHandlers = new ArrayList<StanzaHandler>();
        registeredTransformers = new ArrayList<PayloadElementTransformer>();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { PresenceSubscriptionService.class, PresenceService.class, AtmosphereExtensionService.class };
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
        AtmosphereExtensionService atmosphereRegistryService = AtmospherePresenceServiceRegistry.getInstance().getService(
            AtmosphereExtensionService.class,
            true);

        // Add Presence specific transformers and mappings
        atmosphereRegistryService.addPayloadElementTransFormer(new PresenceStateTransformer());
        atmosphereRegistryService.addElementPathMapping(Presence.PRESENCE_STATE_PATH, PresenceState.class);
        atmosphereRegistryService.addElementPathMapping(Presence.MESSAGE_PATH, String.class);
        atmosphereRegistryService.addElementPathMapping(Presence.PRIORITY_PATH, Byte.class);
        atmosphereRegistryService.addElementPathMapping(Presence.ERROR_PATH, OXException.class);

        // Add Presence specific handler
        atmosphereRegistryService.addStanzaHandler(new OXRTPresenceHandler());
    }

    @Override
    protected void stopBundle() throws Exception {
        AtmospherePresenceServiceRegistry.getInstance().clearRegistry();
        AtmosphereExtensionService atmosphereRegistryService = AtmospherePresenceServiceRegistry.getInstance().getService(
            AtmosphereExtensionService.class,
            true);
        for (StanzaHandler handler : registeredHandlers) {
            atmosphereRegistryService.removeStanzaHandler(handler);
        }
        for (PayloadElementTransformer transformer : registeredTransformers) {
            atmosphereRegistryService.removePayloadElementTransformer(transformer);
        }
        atmosphereRegistryService.removeElementpathMapping(Presence.PRESENCE_STATE_PATH);
        unregisterServices();
    }

}
