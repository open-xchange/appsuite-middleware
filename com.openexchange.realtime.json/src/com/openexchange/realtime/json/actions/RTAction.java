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
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.conversion.simple.SimpleConverter;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.json.Utils;
import com.openexchange.realtime.json.impl.stanza.writer.StanzaWriter;
import com.openexchange.realtime.json.osgi.JSONServiceRegistry;
import com.openexchange.realtime.json.protocol.RTClientState;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link RTAction} - RTActions implement the functionality of the realtime http api. The response has the form:
 *
 * <pre>
 * data: {
 *   acks: [0,1]
 *   result:{}
 *   stanzas: [{stanza0},{stanza1},{stanza2}]
 *   error: {prefix: "thePrefix", code: theCode, ...}
 * }
 * </pre>
 *
 * where the not all data fields will be returned for every {@link RTAction}.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public abstract class RTAction implements AJAXActionService {

    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RTAction.class);

    public final static String ACKS = "acks";

    public final static String RESULT = "result";

    public final static String STANZAS = "stanzas";

    public final static String ERROR = "error";

    protected final StanzaWriter stanzaWriter = new StanzaWriter();

    protected ID constructID(AJAXRequestData request, ServerSession session) throws OXException {
        return Utils.constructID(request, session);
    }

    /**
     * Get buffered messages addressed to the client
     *
     * @param state The client state
     * @return The list of Stanzas that are addressed to the client, may be empty
     * @throws OXException
     */
    protected List<JSONObject> pollStanzas(RTClientState state) throws OXException {
        try {
            LOG.debug("Locking RTClientState for ID: {}", state.getId());
            state.lock();
            state.touch();
            List<Stanza> stanzasToSend = state.getStanzasToSend();
            List<JSONObject> stanzas = new ArrayList<JSONObject>(stanzasToSend.size());
            LOG.debug("Got {} Stanzas to send for client: {}", stanzasToSend.size(), state.getId());
            for (Stanza s : stanzasToSend) {
                stanzas.add(stanzaWriter.write(s));
            }
            return stanzas;
        } finally {
            // Increment TTL count even after failure as offending stanza might cause sending to fail. Incrementing will get rid of it.
            state.purge();
            LOG.debug("Unlocking RTClientState for ID: {}", state.getId());
            state.unlock();
        }
    }

    /**
     * Convert a Stanza from native to JSON representation
     *
     * @param stanza The stanza to convert
     * @return The converted Stanza
     * @throws OXException If the conversion fails
     */
    protected JSONObject stanzaToJSON(Stanza stanza) throws OXException {
        return stanzaWriter.write(stanza);
    }

    /**
     * Convert a RealtimeException from native to JSON representation
     *
     * @param ex The exception to convert
     * @param serverSession The ServerSession to use for the conversion process
     * @return The converted RealtimeException
     * @throws OXException If the conversion fails
     */
    protected JSONObject exceptionToJSON(RealtimeException ex, ServerSession serverSession) throws OXException {
        SimpleConverter simpleConverter = JSONServiceRegistry.getInstance().getService(SimpleConverter.class);
        Object converted = simpleConverter.convert(RealtimeException.class.getSimpleName(), "json", ex, serverSession);
        JSONObject exceptionAsJSON = (JSONObject) converted;
        return exceptionAsJSON;
    }

    /**
     * Get a result map containing the error.
     *
     * @param exception The exception to log
     * @param serverSession The ServerSession
     * @throws OXException If the exception could be converted to JSON
     */
    protected Map<String, Object> getErrorMap(RealtimeException exception, ServerSession serverSession) throws OXException {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put(ERROR, exceptionToJSON(exception, serverSession));
        return resultMap;
    }

    /**
     * Get a result map containing the error and remaining stanzas addressed to the client
     *
     * @param exception The exception to log
     * @param serverSession The ServerSession
     * @param clientState The state if the connected client
     * @throws OXException If the exception could be converted to JSON
     */
    protected Map<String, Object> getErrorAndStanzasMap(RealtimeException exception, ServerSession serverSession, RTClientState clientState) throws OXException {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put(ERROR, exceptionToJSON(exception, serverSession));
        resultMap.put(STANZAS, pollStanzas(clientState));
        return resultMap;
    }

}
