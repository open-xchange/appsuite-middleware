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

package com.openexchange.realtime.json.actions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.directory.DefaultResource;
import com.openexchange.realtime.directory.ResourceDirectory;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.json.impl.StateManager;
import com.openexchange.realtime.json.osgi.JSONServiceRegistry;
import com.openexchange.realtime.json.protocol.NextSequence;
import com.openexchange.realtime.packet.ID;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link EnrolAction} - Enrols a realtime client when he contacts a backend node for the first time.
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class EnrolAction extends RTAction {

    private final static Log LOG = com.openexchange.log.Log.loggerFor(EnrolAction.class);

    private final StateManager stateManager;

    public EnrolAction(StateManager stateManager) {
        this.stateManager = stateManager;
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        final Map<String, Object> enrolActionResults = new HashMap<String, Object>();
        ID constructedId = constructID(requestData, session);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Enroling ID: " + constructedId);
        }
        stateManager.retrieveState(constructedId);
        NextSequence nextSequence = new NextSequence(constructedId, constructedId, 0);
        JSONObject answerJSON = stanzaToJSON(nextSequence);
        enrolActionResults.put(STANZAS, Collections.singletonList(answerJSON));

        ResourceDirectory resourceDirectory = JSONServiceRegistry.getInstance().getService(ResourceDirectory.class);
        try {
            resourceDirectory.set(constructedId, new DefaultResource());
        } catch (OXException e) {
            RealtimeException enrolException = RealtimeExceptionCodes.UNEXPECTED_ERROR.create(e.getMessage());
            LOG.error(enrolException.getMessage(), enrolException);
            enrolActionResults.put(ERROR, exceptionToJSON(enrolException, session));
        }

        return new AJAXRequestResult(enrolActionResults, "native");
    }

}
