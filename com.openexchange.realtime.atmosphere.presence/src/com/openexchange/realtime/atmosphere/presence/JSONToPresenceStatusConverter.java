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

package com.openexchange.realtime.atmosphere.presence;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.conversion.simple.SimpleConverter;
import com.openexchange.conversion.simple.SimplePayloadConverter;
import com.openexchange.conversion.simple.SimplePayloadConverter.Quality;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.example.presence.PresenceStatus;
import com.openexchange.realtime.example.presence.PresenceService.PresenceState;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link JSONToPresenceStatusConverter} - Convert incoming 
 * 
 * PresenceSubscriptionService and PresenceStatusService
 *  
 *  
 *  =INFO REPORT==== 2012-08-20 09:51:50 ===
D(<0.364.0>:ejabberd_receiver:320) : Received XML on stream = "<presence><show>away</show><status>test</status><c xmlns='http://jabber.org/protocol/caps' node='http://pidgin.im/' hash='sha-1' ver='AcN1/PEN8nq7AHD+9jpxMV4U6YM=' ext='voice-v1 camera-v1 video-v1'/><x xmlns='vcard-temp:x:update'><photo/></x></presence>"

=INFO REPORT==== 2012-08-20 09:51:50 ===
D(<0.365.0>:ejabberd_c2s:993) : presence_update({jid,"marens","marens.eu",
                                                 "OX1","marens","marens.eu",
                                                 "OX1"},
        {xmlelement,"presence",[],
                    [{xmlelement,"show",[],[{xmlcdata,<<"away">>}]},
                     {xmlelement,"status",[],[{xmlcdata,<<"test">>}]},
                     {xmlelement,"c",
                                 [{"xmlns","http://jabber.org/protocol/caps"},
                                  {"node","http://pidgin.im/"},
                                  {"hash","sha-1"},
                                  {"ver","AcN1/PEN8nq7AHD+9jpxMV4U6YM="},
                                  {"ext","voice-v1 camera-v1 video-v1"}],
                                 []},
                     {xmlelement,"x",
                                 [{"xmlns","vcard-temp:x:update"}],
                                 [{xmlelement,"photo",[],[]}]}]},
        {state,
            {socket_state,tls,{tlssock,#Port<0.1808>,#Port<0.1836>},<0.364.0>},
            ejabberd_socket,#Ref<0.0.0.9692>,false,"3920059724",
            {sasl_state,"jabber","marens.eu",[],
                #Fun<ejabberd_c2s.1.30334249>,#Fun<ejabberd_c2s.2.32721014>,
                #Fun<ejabberd_c2s.3.7052687>,cyrsasl_digest,
                {state,5,"3741804809","marens",[],
                    #Fun<ejabberd_c2s.1.30334249>,
                    #Fun<ejabberd_c2s.3.7052687>,ejabberd_auth_internal,
                    "marens.eu"}},
            c2s,c2s_shaper,false,true,false,true,
            [verify_none,{certfile,"/etc/ejabberd/ejabberd.pem"}],
            true,
            {jid,"marens","marens.eu","OX1","marens","marens.eu","OX1"},
            "marens","marens.eu","OX1",
            {{1345,447314,747479},<0.365.0>},
            {2,
             {{"maximegidding","gmail.com",[]},
              {{"marens","marens.eu",[]},nil,nil},
              nil}},
            {2,
             {{"maximegidding","gmail.com",[]},
              {{"marens","marens.eu",[]},nil,nil},
              nil}},
            {2,
             {{"marens","marens.eu",[]},
              nil,
              {{"maximegidding","gmail.com",[]},nil,nil}}},
            {0,nil},
            {xmlelement,"presence",[],
                [{xmlelement,"priority",[],[{xmlcdata,<<"1">>}]},
                 {xmlelement,"c",
                     [{"xmlns","http://jabber.org/protocol/caps"},
                      {"node","http://pidgin.im/"},
                      {"hash","sha-1"},
                      {"ver","AcN1/PEN8nq7AHD+9jpxMV4U6YM="},
                      {"ext","voice-v1 camera-v1 video-v1"}],
                     []},
                 {xmlelement,"x",
                     [{"xmlns","vcard-temp:x:update"}],
                     [{xmlelement,"photo",[],[]}]}]},
            undefined,
            {{2012,8,20},{7,22,13}},
            false,
            {userlist,none,[],false},
            c2s_tls,ejabberd_auth_internal,
            {{217,6,212,138},53912},
            []})


 *  
 *  
            {
              kind: 'presence'
              ns: 'presence',
              to: 'myuser@mycontext',
              data: {
                state: 'online'
                message: 'i am here',
                priority: 0
              }
            };
 * 
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class JSONToPresenceStatusConverter implements SimplePayloadConverter {

    @Override
    public String getInputFormat() {
        return "json";
    }

    @Override
    public String getOutputFormat() {
        return "presenceStatus";
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

    @Override
    public Object convert(Object data, ServerSession session, SimpleConverter converter) throws OXException {
        JSONObject object = (JSONObject) data;

        PresenceState state = null;

        try {
            String status = object.getString("state");
            for (PresenceState s : PresenceState.values()) {
                if (s.name().equalsIgnoreCase(status)) {
                    state = s;
                    break;
                }
            }
        } catch (JSONException e) {
            throw OXException.general(e.getMessage());
        }

        return new PresenceStatus(state, object.optString("message"));
    }

}
