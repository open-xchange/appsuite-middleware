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

package com.openexchange.subscribe.json;

import static com.openexchange.subscribe.json.MultipleHandlerTools.wrapThrowable;
import static com.openexchange.subscribe.json.SubscriptionJSONErrorMessages.MISSING_PARAMETER;
import static com.openexchange.subscribe.json.SubscriptionJSONErrorMessages.UNKNOWN_ACTION;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.i18n.I18nService;
import com.openexchange.i18n.I18nTranslator;
import com.openexchange.i18n.Translator;
import com.openexchange.multiple.MultipleHandler;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.SubscriptionSourceDiscoveryService;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link SubscriptionSourceMultipleHandler}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SubscriptionSourceMultipleHandler implements MultipleHandler {

    public static final int CLASS_ID = 1;
    public static final Set<String> ACTIONS_REQUIRING_BODY = Collections.emptySet();

    private final SubscriptionSourceDiscoveryService discoverer;

    public SubscriptionSourceMultipleHandler(final SubscriptionSourceDiscoveryService discoverer) {
        super();
        this.discoverer = discoverer;
    }

    @Override
    public void close() {
        // Nothing to close.
    }

    @Override
    public Date getTimestamp() {
        return null;
    }

    @Override
    public Collection<OXException> getWarnings() {
        return Collections.<OXException> emptySet();
    }

    @Override
    public JSONValue performRequest(final String action, final JSONObject request, final ServerSession session, final boolean secure) throws JSONException, OXException {
        try {
            if(null == action) {
                MISSING_PARAMETER.create("action");
                return null;
            } else if(action.equals("listSources") || action.equals("all")) {
                return listSources(request, session);
            } else if (action.equals("getSource") || action.equals("get")) {
                return getSource(request, session);
            } else {
                UNKNOWN_ACTION.create(action);
                return null;
            }
        } catch (final OXException x) {
            throw x;
        } catch (final JSONException x) {
            throw x;
        } catch (final Throwable t) {
            throw wrapThrowable(t);
        }
    }

    protected JSONValue listSources(final JSONObject req, final ServerSession session) throws OXException  {
        final int module = getModule(req);
        final List<SubscriptionSource> sources = getDiscovery(session).getSources(module);
        final String[] columns = getColumns(req);
        final JSONArray json = new SubscriptionSourceJSONWriter(createTranslator(session)).writeJSONArray(sources, columns);
        return json;
    }

    private Translator createTranslator(final ServerSession session) {
        final I18nService service = I18nServices.getInstance().getService(session.getUser().getLocale());
        return null == service ? Translator.EMPTY : new I18nTranslator(service);
    }

    private String[] getColumns(final JSONObject req) {
        final String columns = req.optString("columns");
        if(columns == null) {
            return new String[]{"id", "displayName", "module", "icon",  "formDescription"};
        }
        return columns.split("\\s*,\\s*");
    }

    protected JSONValue getSource(final JSONObject req, final ServerSession session) throws OXException, JSONException {
        final String identifier = req.getString("id");
        if(identifier == null) {
            MISSING_PARAMETER.create("id");
        }
        final SubscriptionSource source = getDiscovery(session).getSource(identifier);
        final JSONObject data = new SubscriptionSourceJSONWriter(createTranslator(session)).writeJSON(source);
        return data;
    }

    protected int getModule(final JSONObject req) {
        final String moduleAsString = req.optString("module");
        if(moduleAsString == null) {
            return -1;
        }
        if(moduleAsString.equals("contacts")) {
            return FolderObject.CONTACT;
        } else if (moduleAsString.equals("calendar")) {
            return FolderObject.CALENDAR;
        } else if (moduleAsString.equals("tasks")) {
            return FolderObject.TASK;
        } else if (moduleAsString.equals("infostore")) {
            return FolderObject.INFOSTORE;
        }
        return -1;
    }

    protected SubscriptionSourceDiscoveryService getDiscovery(final ServerSession session) throws OXException {
        return discoverer.filter(session.getUserId(), session.getContextId());
    }
}
