/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
import com.openexchange.i18n.I18nServiceRegistry;
import com.openexchange.i18n.I18nTranslator;
import com.openexchange.i18n.Translator;
import com.openexchange.multiple.MultipleHandler;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.SubscriptionSourceDiscoveryService;
import com.openexchange.subscribe.json.osgi.Services;
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
            if (null == action) {
                MISSING_PARAMETER.create("action");
                return null;
            } else if (action.equals("listSources") || action.equals("all")) {
                return listSources(request, session);
            } else if (action.equals("getSource") || action.equals("get")) {
                return getSource(request, session);
            } else {
                UNKNOWN_ACTION.create(action);
                return null;
            }
        } catch (OXException x) {
            throw x;
        } catch (JSONException x) {
            throw x;
        } catch (Throwable t) {
            throw wrapThrowable(t);
        }
    }

    protected JSONValue listSources(final JSONObject req, final ServerSession session) throws OXException {
        final int module = getModule(req);
        final List<SubscriptionSource> sources = getDiscovery(session).getSources(module);
        final String[] columns = getColumns(req);
        final JSONArray json = new SubscriptionSourceJSONWriter(createTranslator(session)).writeJSONArray(sources, columns);
        return json;
    }

    private Translator createTranslator(final ServerSession session) {
        I18nServiceRegistry registry = Services.getService(I18nServiceRegistry.class);
        if (registry == null) {
            return Translator.EMPTY;
        }
        I18nService service = registry.getI18nService(session.getUser().getLocale());
        return null == service ? Translator.EMPTY : new I18nTranslator(service);
    }

    private String[] getColumns(final JSONObject req) {
        final String columns = req.optString("columns");
        if (columns == null) {
            return new String[] { "id", "displayName", "module", "icon", "formDescription" };
        }
        return columns.split("\\s*,\\s*");
    }

    protected JSONValue getSource(final JSONObject req, final ServerSession session) throws OXException, JSONException {
        final String identifier = req.getString("id");
        if (identifier == null) {
            MISSING_PARAMETER.create("id");
        }
        final SubscriptionSource source = getDiscovery(session).getSource(identifier);
        final JSONObject data = new SubscriptionSourceJSONWriter(createTranslator(session)).writeJSON(source);
        return data;
    }

    protected int getModule(final JSONObject req) {
        final String moduleAsString = req.optString("module");
        if (moduleAsString == null) {
            return -1;
        }
        if (moduleAsString.equals("contacts")) {
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
