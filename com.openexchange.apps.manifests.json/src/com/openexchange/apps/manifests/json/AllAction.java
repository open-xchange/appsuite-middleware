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

package com.openexchange.apps.manifests.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.capabilities.Capability;
import com.openexchange.capabilities.CapabilityFilter;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AllAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
@DispatcherNotes(noSession = true)
public class AllAction implements AJAXActionService {

    private final JSONArray manifests;
    private final ServiceLookup services;
    private final CapabilityFilter capabilityFilter;

    public AllAction(ServiceLookup services, JSONArray manifests, final CapabilityFilter capabilityFilter) {
        super();
        this.manifests = manifests;
        this.services = services;
        this.capabilityFilter = capabilityFilter;
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        return new AJAXRequestResult(getManifests(session, manifests, services, capabilityFilter), "json");
    }

    public static JSONArray getManifests(ServerSession session, JSONArray manifests, ServiceLookup services, final CapabilityFilter capabilityFilter) throws OXException {
        JSONArray result = new JSONArray();
        try {
            if (session.isAnonymous()) {
                // Deliver no apps and only plugins with the namespace 'signin'
                
                for (int i = 0, size = manifests.length(); i < size; i++) {
                    JSONObject definition = manifests.getJSONObject(i);
                    if (isSigninPlugin(definition)) {
                        result.put(new JSONObject(definition));
                    }
                }
                
            } else {
                Set<Capability> capabilities = services.getService(CapabilityService.class).getCapabilities(session.getUserId(), session.getContextId(), capabilityFilter);
                
                Map<String, Capability> capMap = new HashMap<String, Capability>(capabilities.size());
                for (Capability capability : capabilities) {
                    capMap.put(capability.getId(), capability);
                }

                for (int i = 0, size = manifests.length(); i < size; i++) {
                    JSONObject definition = manifests.getJSONObject(i);
                    if (hasCapability(capMap, definition)) {
                        result.put(new JSONObject(definition));
                    }
                }
            }
        } catch (JSONException x) {
            throw AjaxExceptionCodes.JSON_ERROR.create(x.getMessage(), x);
        }
        return result;
    }

    private static boolean isSigninPlugin(JSONObject definition) throws JSONException {
        if (!definition.has("namespace")) {
            return false;
        }

        return definition.getString("namespace").equalsIgnoreCase("signin");
    }

    private static boolean hasCapability(Map<String, Capability> capMap, JSONObject definition) throws JSONException {

        if (!definition.has("requires")) {
            return true;
        }
        Object requires = definition.get("requires");
        // This could be a string or an array
        List<String> capDef = new ArrayList<String>();

        if (JSONArray.class.isInstance(requires)) {
            JSONArray arr = (JSONArray) requires;
            for (int i = 0, size = arr.length(); i < size; i++) {
                capDef.add(arr.getString(i));
            }
        } else {
            capDef.add(requires.toString());
        }

        for (String c : capDef) {
            String[] split = Strings.splitByWhitespaces(c);
            String name = split[0];
            boolean inverse = false;
            if (name.charAt(0) == '!') {
                inverse = true;
                name = name.substring(1);
            }
            boolean needsBackend = false;
            if (split.length > 1) {
                final String word = split[1];
                needsBackend = "withbackend".equalsIgnoreCase(word) || "withbackendsupport".equalsIgnoreCase(word) || "backend".equals(word) || "backendsupport".equals(word);
            }

            Capability capability = capMap.get(name);

            if (inverse) {
                if (capability != null) {
                    return false;
                }
            } else {
                if (capability == null) {
                    return false;
                }
                if (needsBackend) {
                    if (!capability.isSupportedByBackend()) {
                        return false;
                    }
                }
            }

        }

        return true;
    }

}
