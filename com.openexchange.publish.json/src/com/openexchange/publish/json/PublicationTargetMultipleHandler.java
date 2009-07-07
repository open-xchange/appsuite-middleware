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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.publish.json;

import static com.openexchange.publish.json.PublicationJSONErrorMessage.MISSING_PARAMETER;
import static com.openexchange.publish.json.PublicationJSONErrorMessage.UNKNOWN_ACTION;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.multiple.MultipleHandler;
import com.openexchange.publish.PublicationException;
import com.openexchange.publish.PublicationTarget;
import com.openexchange.publish.PublicationTargetDiscoveryService;
import com.openexchange.tools.session.ServerSession;
import static com.openexchange.publish.json.MultipleHandlerTools.*;


/**
 * {@link PublicationTargetMultipleHandler}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class PublicationTargetMultipleHandler implements MultipleHandler {

    public static final Set<String> ACTIONS_REQUIRING_BODY = Collections.emptySet();
    private PublicationTargetDiscoveryService discoverer = null;
    private PublicationTargetWriter writer = new PublicationTargetWriter();

    public PublicationTargetMultipleHandler(PublicationTargetDiscoveryService discoverer) {
        this.discoverer = discoverer;
    }
    
    
    public void close() {

    }

    public Date getTimestamp() {
        return null;
    }

    public JSONValue performRequest(String action, JSONObject request, ServerSession session) throws AbstractOXException, JSONException {
        try {
            if (null == action) {
                throw MISSING_PARAMETER.create("action");
            } else if (action.equals("listTargets") || action.equals("all")) {
                return listTargets(request, session);
            } else if (action.equals("getTarget") || action.equals("get")) {
                return getTarget(request, session);
            } else {
                throw UNKNOWN_ACTION.create(action);
            }
        } catch (AbstractOXException x) {
            throw x;
        } catch (JSONException x) {
            throw x;
        } catch (Throwable t) {
            throw wrapThrowable(t);
        }
    }

    private JSONValue getTarget(JSONObject request, ServerSession session) throws PublicationJSONException, PublicationException, JSONException {
        String identifier = request.optString("id");
        if (identifier == null) {
            throw MISSING_PARAMETER.create("id");
        }
        PublicationTarget target = discoverer.getTarget(identifier);
        JSONObject data = writer.write(target);
        return data;
    }

    private JSONValue listTargets(JSONObject request, ServerSession session) throws JSONException, PublicationJSONException, PublicationException {
        Collection<PublicationTarget> targets = discoverer.listTargets();
        String[] columns = getColumns(request);
        JSONArray json = writer.writeJSONArray(targets, columns);
        return json;
    }
    
    private String[] getColumns(JSONObject req) {
        String columns = req.optString("columns");
        if (columns == null) {
            return new String[] { "id", "displayName", "module", "icon", "formDescription" };
        }
        return columns.split("\\s*,\\s*");
    }

}
