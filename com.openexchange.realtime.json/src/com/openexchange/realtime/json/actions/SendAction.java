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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.json.impl.JSONProtocolHandler;
import com.openexchange.realtime.json.impl.StateEntry;
import com.openexchange.realtime.json.impl.StateManager;
import com.openexchange.realtime.packet.ID;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link SendAction}
 * <li> Send action examples:
 *   <ol>
 *     <li> Send an acknowledgement
 *       <pre>
 *       {
 *         "seq": [
 *           "0"
 *         ],
 *         "type": "ack"
 *       }
 *       </pre>
 *     <li> Send a ping into a room to verify that you didn't leve the room without a proper leave message
 *       <pre>
 *       [
 *         {
 *           "payloads": [
 *             {
 *               "data": 1,
 *               "namespace": "group",
 *               "element": "ping"
 *             }
 *           ],
 *           "to": "synthetic.china://room1",
 *           "element": "message"
 *         }
 *       ]
 *       </pre>
 *     <li> Generally send messages to the server, e.g. say something into a room
 *     <pre>
 *     {
 *       "payloads": [
 *         {
 *           "data": "say",
 *           "element": "action"
 *         },
 *         {
 *           "namespace": "china",
 *           "data": "Hello World",
 *           "element": "message"
 *         }
 *       ],
 *       "seq": 0,
 *       "element": "message",
 *       "to": "synthetic.china://room1"
 *     }
 *     </pre>
 *   </ol>
 * </li>
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SendAction extends RTAction  {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(SendAction.class);

    private final JSONProtocolHandler protocolHandler;
    private final StateManager stateManager;

    public SendAction(ServiceLookup services, StateManager stateManager, JSONProtocolHandler protocolHandler) {
        this.stateManager = stateManager;
        this.protocolHandler = protocolHandler;
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData request, ServerSession session) throws OXException {
        Object data = request.requireData();

        final List<JSONObject> objects;
        if (data instanceof JSONArray) {
            JSONArray array = (JSONArray) data;
            objects = new ArrayList<JSONObject>(array.length());
            for(int i = 0, length = array.length(); i < length; i++) {
                try {
                    objects.add(array.getJSONObject(i));
                } catch (JSONException e) {
                    throw new OXException(e);
                }
            }
        } else if (data instanceof JSONObject) {
            objects = Arrays.asList((JSONObject) data);
        } else {
            throw RealtimeExceptionCodes.UNEXPECTED_ERROR.create("Missing request body");
        }

        LOG.debug("Messages arrived in SendAction: " + objects.toString());
        ID id = constructID(request, session);

        StateEntry entry = stateManager.retrieveState(id);

        List<Long> acknowledgements = new ArrayList<Long>(objects.size());

        protocolHandler.handleIncomingMessages(id, session, entry, objects, acknowledgements);

        Map<String, Object> r = new HashMap<String, Object>();
        r.put("acknowledgements", acknowledgements);

        return new AJAXRequestResult(r, "native");
    }



}
