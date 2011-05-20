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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.templating.json;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.openexchange.groupware.AbstractOXException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.multiple.MultipleHandler;
import com.openexchange.server.ServiceLookup;
import com.openexchange.templating.TemplateException;
import com.openexchange.templating.TemplateService;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link TemplateMultipleHandler}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class TemplateMultipleHandler implements MultipleHandler {

    private static ServiceLookup services = null;
    
    public static void setServices(ServiceLookup lookup) {
        services = lookup;
    }
    
    private static enum Action {
        names
    }
    
    private static enum Param {
        only
    }
    
    public void close() {

    }

    public Date getTimestamp() {
        return new Date();
    }

    public Collection<AbstractOXException> getWarnings() {
        return Collections.emptyList();
    }

    public Object performRequest(String action, JSONObject jsonObject, ServerSession session, boolean secure) throws AbstractOXException, JSONException {
        if(action.equalsIgnoreCase(Action.names.name())) {
            return names(jsonObject, session);
        } else {
            throw new AjaxException(AjaxException.Code.UnknownAction, action);
        }
    }

    private JSONArray names(JSONObject jsonObject, ServerSession session) throws JSONException, TemplateException {
        
        boolean onlyBasic = false;
        boolean onlyUser = false;
        String[] filter = null;
        
        if(jsonObject.has(Param.only.name())) {
            Set<String> only = new HashSet<String>(Arrays.asList(jsonObject.getString(Param.only.name()).split("\\s*,\\s*")));
            
            
            if(only.contains("basic")) {
                onlyBasic = true;
                only.remove("basic");
            } else if (only.contains("user")) {
                onlyUser = true;
                only.remove("user");
            }
            filter = only.toArray(new String[only.size()]);
        }
        
        TemplateService templates = services.getService(TemplateService.class);
        
        if(onlyBasic) {
            return ARRAY(templates.getBasicTemplateNames(filter));
        }
        
        if(onlyUser) {
            List<String> basicTemplateNames = templates.getBasicTemplateNames(filter);
            List<String> templateNames = templates.getTemplateNames(session, filter);
            templateNames.removeAll(basicTemplateNames);
            return ARRAY(templateNames);
        }
        
        return ARRAY(templates.getTemplateNames(session, filter));
    }

    private JSONArray ARRAY(List<String> templateNames) {
        JSONArray array = new JSONArray();
        for (String name : templateNames) {
            array.put(name);
        }
        return array;
    }

}
