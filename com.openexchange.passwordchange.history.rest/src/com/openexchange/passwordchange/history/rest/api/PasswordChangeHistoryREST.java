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

package com.openexchange.passwordchange.history.rest.api;

import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.passwordchange.history.registry.PasswordChangeTrackerRegistry;
import com.openexchange.passwordchange.history.rest.osgi.Services;
import com.openexchange.passwordchange.history.tracker.PasswordChangeInfo;
import com.openexchange.passwordchange.history.tracker.PasswordChangeTracker;
import com.openexchange.rest.services.annotation.Role;
import com.openexchange.rest.services.annotation.RoleAllowed;

/**
 * {@link PasswordChangeHistoryREST}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
@Path("password/history/v1")
@RoleAllowed(Role.BASIC_AUTHENTICATED)
public class PasswordChangeHistoryREST {

    private static final String LIST = "/list";

    /**
     * Initializes a new {@link PasswordChangeHistoryREST}.
     */
    public PasswordChangeHistoryREST() {
        super();
    }

    @GET
    @Path(LIST)
    @Consumes(MediaType.TEXT_HTML)
    @Produces(MediaType.APPLICATION_JSON)
    public JSONObject listHistory(@QueryParam("limit") int limit, @QueryParam("contextID") int contextID, @QueryParam("userID") int userID) throws Exception {

        // Get services
        PasswordChangeTrackerRegistry registry = Services.getService(PasswordChangeTrackerRegistry.class, true);
        ConfigViewFactory config = Services.getService(ConfigViewFactory.class, true);
        ConfigView view = config.getView(userID, contextID);

        // Check configuration
        String symbolicName = view.get("com.openexchange.passwordchange.tracker", String.class);
        if (null == symbolicName || symbolicName.isEmpty()) {
            return new JSONObject("{No tracker for the requested user configured.}");
        }

        // Get tracker & data
        Map<String, PasswordChangeTracker> trackers = registry.getTrackers();
        PasswordChangeTracker pwdtracker = trackers.get(symbolicName);
        if (null == pwdtracker) {
            return new JSONObject("{No tracker for the requested user found.}");
        }
        List<PasswordChangeInfo> history = pwdtracker.listPasswordChanges(userID, contextID);
        int size = history.size();
        if (size > limit) {
            for (int i = limit; i < size; i++) {
                // Remove all that exceeds the limit
                history.remove(i);
            }
        }

        // Build response
        JSONObject json = new JSONObject();
        JSONArray entries = new JSONArray();
        int i = 0;
        for (PasswordChangeInfo info : history) {
            JSONObject data = new JSONObject();
            data.append("lastModified", info.lastModified().toString());
            data.append("modifiedBy", info.modifiedBy());
            data.append("modifyOrigin", info.modifyOrigin());
            entries.add(i++, data);
        }
        json.append("PasswordChangeHistroy", entries);
        return json;
    }

}
