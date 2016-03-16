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

package com.openexchange.realtime.json.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.RealtimeConfig;
import com.openexchange.realtime.cleanup.GlobalRealtimeCleanup;
import com.openexchange.realtime.directory.DefaultResource;
import com.openexchange.realtime.directory.Resource;
import com.openexchange.realtime.directory.ResourceDirectory;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.json.impl.StateManager;
import com.openexchange.realtime.json.osgi.JSONServiceRegistry;
import com.openexchange.realtime.json.protocol.NextSequence;
import com.openexchange.realtime.json.protocol.TracingDemand;
import com.openexchange.realtime.json.util.RTResultFormatter;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.util.IDMap;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link EnrolAction} - Enrols a realtime client when he contacts a backend node for the first time.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class EnrolAction extends RTAction {

    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(EnrolAction.class);

    private final StateManager stateManager;

    public EnrolAction(StateManager stateManager) {
        this.stateManager = stateManager;
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        final Map<String, Object> enrolActionResults = new HashMap<String, Object>();
        ID constructedId = constructID(requestData, session);
        LOG.debug("Enroling ID: {}", constructedId);
        List<JSONObject> stanzas = new ArrayList<JSONObject>();

        String userAtContext = constructedId.getUser() + "@" + constructedId.getContext();
        RealtimeConfig realtimeConfig = RealtimeConfig.getInstance();
        if(realtimeConfig.isTraceAllUsersEnabled() || realtimeConfig.getUsersToTrace().contains(userAtContext)) {
            JSONObject tracingDemandJSON = stanzaToJSON(new TracingDemand(constructedId, constructedId, true));
            stanzas.add(tracingDemandJSON);
        } else {
            JSONObject tracingDemandJSON = stanzaToJSON(new TracingDemand(constructedId, constructedId, false));
            stanzas.add(tracingDemandJSON);
        }

        NextSequence nextSequence = new NextSequence(constructedId, constructedId, 0);
        JSONObject nextSequenceJSON = stanzaToJSON(nextSequence);
        stanzas.add(nextSequenceJSON);

        enrolActionResults.put(STANZAS, stanzas);

        ResourceDirectory resourceDirectory = JSONServiceRegistry.getInstance().getService(ResourceDirectory.class);

        /*
         * The user might have been migrated from another node where he was active and created states on multiple nodes e.g.
         * StanzaSequenceGate of LocalMessageDispatchers during remote delivery of Stanzas. We have to clean those states or otherwise
         * StanzaSequenceGates will drop Stanzas due to the NextSequence Stanza that will be returned by this Action.
         */
        IDMap<Resource> idMap = resourceDirectory.get(constructedId);
        if (!idMap.isEmpty()) {
            GlobalRealtimeCleanup globalRealtimeCleanup = JSONServiceRegistry.getInstance().getService(GlobalRealtimeCleanup.class);
            globalRealtimeCleanup.cleanForId(constructedId);
        }

        /*
         * Create the new state after the potential cleanup
         */
        stateManager.retrieveState(constructedId);
        LOG.debug("Retrieved state for id {}", constructedId);

        try {
            resourceDirectory.set(constructedId, new DefaultResource());
        } catch (OXException e) {
            RealtimeException enrolException = RealtimeExceptionCodes.UNEXPECTED_ERROR.create(e.getMessage());
            LOG.error("", enrolException);
            enrolActionResults.put(ERROR, exceptionToJSON(enrolException, session));
        }
        LOG.debug(RTResultFormatter.format(enrolActionResults));
        return new AJAXRequestResult(enrolActionResults, "native");
    }

}
