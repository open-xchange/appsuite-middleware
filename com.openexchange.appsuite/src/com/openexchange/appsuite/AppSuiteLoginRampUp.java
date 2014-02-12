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

package com.openexchange.appsuite;

import java.util.Arrays;
import java.util.Collection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleActivator;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.Dispatcher;
import com.openexchange.exception.OXException;
import com.openexchange.login.LoginRampUpService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;
import static com.openexchange.ajax.requesthandler.AJAXRequestDataBuilder.request;

/**
 * {@link AppSuiteLoginRampUp}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AppSuiteLoginRampUp implements LoginRampUpService {

    private ServiceLookup services;

    /**
     * Initializes a new {@link AppSuiteLoginRampUp}.
     * @param activator
     */
    public AppSuiteLoginRampUp(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public boolean contributesTo(String client) {
        return client != null && client.equals("open-xchange-appsuite");
    }

    @Override
    public JSONObject getContribution(ServerSession session, AJAXRequestData loginRequest) throws OXException {
        JSONObject rampUp = new JSONObject();
        try {
            Dispatcher ox = services.getService(Dispatcher.class);
            AJAXRequestData manifestRequest = request().module("apps/manifests").action("config").format("json").hostname(loginRequest.getHostname()).build();
            try {
                rampUp.put("serverConfig", ox.perform(manifestRequest, null, session).getResultObject());
            } catch (OXException x) {
                // Omit result on error. Let the UI deal with this
            }
            
            
            try {
                JSONObject jslobs = new JSONObject();
                JSONArray lobs = (JSONArray) ox.perform(request()
                    .module("jslob")
                    .action("list")
                    .data(
                        new JSONArray(
                                Arrays.asList("io.ox/core", "io.ox/core/updates", "io.ox/mail", "io.ox/contacts", "io.ox/calendar", "io.ox/core/settingOptions", "io.ox/caldav", "io.ox/files", "io.ox/tours", "io.ox/mail/emoji", "io.ox/tasks")
                        ), "json"
                    ).format("json").build(), null, session).getResultObject();
                for(int i = 0, size = lobs.length(); i < size; i++) {
                    JSONObject lob = lobs.getJSONObject(i);
                    jslobs.put(lob.getString("id"), lob);
                }
                rampUp.put("jslobs", jslobs);
            } catch (OXException x) {
                // Omit result on error. Let the UI deal with this
            }
            
            
            JSONObject oauth = new JSONObject();
            rampUp.put("oauth", oauth);
            
            try {
                oauth.put("services", ox.perform(request().module("oauth/services").action("all").format("json").build(), null, session).getResultObject());                
            } catch (OXException x) {
                // Omit result on error. Let the UI deal with this
            }
            try {
                oauth.put("accounts", ox.perform(request().module("oauth/accounts").action("all").format("json").build(), null, session).getResultObject());                
            } catch (OXException x) {
            }
            
            try {
                rampUp.put("secretCheck", ox.perform(request().module("recovery/secret").action("check").format("json").build(), null, session).getResultObject());                
            } catch (OXException x) {
                // Omit result on error. Let the UI deal with this
            }
            
            try {
                rampUp.put("rootFolder", ox.perform(request().module("folders").action("get").params("id", "1", "tree", "1", "altNames", "true", "timezone", "UTC").format("json").build(), null, session).getResultObject());
            } catch (OXException x) {
                // Omit result on error. Let the UI deal with this
            }
            
            try {
                rampUp.put("user", ox.perform(request().module("user").action("get").params("timezone", "utc", "id", "" + session.getUserId()).format("json").build(), null, session).getResultObject());
            } catch (OXException x) {
                // Omit result on error. Let the UI deal with this
            }
            
            try {
                rampUp.put("accounts", ox.perform(request().module("account").action("all").format("json").params("columns", "1001,1002,1003,1004,1005,1006,1007,1008,1009,1010,1011,1012,1013,1014,1015,1016,1017,1018,1019,1020,1021,1022,1023,1024,1025,1026,1027,1028,1029,1030,1031,1032,1033,1034,1035,1036,1037,1038,1039,1040").build(), null, session).getResultObject());
            } catch (OXException x) {
                // Omit result on error. Let the UI deal with this
            }
            
        } catch (JSONException x) {
            // Omit result on error. Let the UI deal with this            
        }
        
        
        return rampUp;
    }


}
