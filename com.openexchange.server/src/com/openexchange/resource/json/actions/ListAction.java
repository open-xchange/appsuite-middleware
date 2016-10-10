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

package com.openexchange.resource.json.actions;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.resource.Resource;
import com.openexchange.resource.internal.ResourceServiceImpl;
import com.openexchange.resource.json.ResourceAJAXRequest;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link ListAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.PUT, name = "list", description = "List resources", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module.")
}, requestBody = "An array with resources ids. ",
responseDescription = "An array of resource objects as described in Resource response.")
public final class ListAction extends AbstractResourceAction {

    private static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(ListAction.class);

    /**
     * Initializes a new {@link ListAction}.
     * @param services
     */
    public ListAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final ResourceAJAXRequest req) throws OXException, JSONException {
        UserStorage userStorage = null;

        JSONArray jsonArray = req.getData();
        int len = jsonArray.length();
        Date timestamp;
        List<Resource> resources = new LinkedList<Resource>();

        if (len > 0) {
            long lastModified = Long.MIN_VALUE;
            ServerSession session = req.getSession();

            for (int a = 0; a < len; a++) {
                final JSONObject jData = jsonArray.getJSONObject(a);
                final int id = DataParser.checkInt(jData, DataFields.ID);
                com.openexchange.resource.Resource r = null;

                try {
                    r = ResourceServiceImpl.getInstance().getResource(id, session.getContext());
                } catch (final OXException exc) {
                    LOG.debug("resource not found try to find id in user table", exc);
                }

                if (r == null) {
                    if (userStorage == null) {
                        userStorage = UserStorage.getInstance();
                    }

                    final User u = userStorage.getUser(id, session.getContext());

                    r = new com.openexchange.resource.Resource();
                    r.setIdentifier(u.getId());
                    r.setDisplayName(u.getDisplayName());
                    r.setLastModified(new Date(0));
                }

                if (lastModified < r.getLastModified().getTime()) {
                    lastModified = r.getLastModified().getTime();
                }

                resources.add(r);
            }
            timestamp = new Date(lastModified);
        } else {
            timestamp = new Date(0);
        }

        return new AJAXRequestResult(resources, timestamp, "resource");
    }

}
