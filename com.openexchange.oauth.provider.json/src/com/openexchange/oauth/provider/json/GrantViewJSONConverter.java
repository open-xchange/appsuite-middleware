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

package com.openexchange.oauth.provider.json;

import static com.openexchange.osgi.Tools.requireService;
import java.util.Iterator;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.oauth.provider.authorizationserver.client.Client;
import com.openexchange.oauth.provider.authorizationserver.client.Icon;
import com.openexchange.oauth.provider.authorizationserver.grant.GrantManagement;
import com.openexchange.oauth.provider.authorizationserver.grant.GrantView;
import com.openexchange.oauth.provider.resourceserver.scope.OAuthScopeProvider;
import com.openexchange.oauth.provider.resourceserver.scope.Scope;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link GrantViewJSONConverter}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class GrantViewJSONConverter {

    private static final Logger LOG = LoggerFactory.getLogger(GrantViewJSONConverter.class);

    private final ServerSession session;

    private final GrantManagement grantManagement;

    private final Translator translator;

    private final ManagedFileManagement managedFileManagement;

    public GrantViewJSONConverter(ServiceLookup services, ServerSession session) throws OXException {
        super();
        this.session = session;
        grantManagement = requireService(GrantManagement.class, services);
        translator = requireService(TranslatorFactory.class, services).translatorFor(session.getUser().getLocale());
        managedFileManagement = requireService(ManagedFileManagement.class, services);
    }

    public JSONArray convert(Iterator<GrantView> grants) throws OXException {
        try {
            JSONArray json = new JSONArray();
            while (grants.hasNext()) {
                json.put(convertGrantView(grants.next()));
            }
            return json;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private JSONObject convertGrantView(GrantView grant) throws JSONException, OXException {
        JSONObject json = new JSONObject();
        json.put("client", convertClient(grant.getClient()));
        json.put("scopes", convertScope(grant.getScope()));
        json.put("date", grant.getLatestGrantDate().getTime());
        return json;
    }

    private JSONObject convertScope(Scope scope) throws JSONException {
        JSONObject jScopes = new JSONObject();
        Set<String> scopeTokens = scope.get();
        for (String token : scopeTokens) {
            OAuthScopeProvider scopeProvider = grantManagement.getScopeProvider(token);
            String description;
            if (scopeProvider == null) {
                LOG.warn("No scope provider available for token {}", token);
                description = token;
            } else {
                description = translator.translate(scopeProvider.getDescription());
            }
            jScopes.put(token, description);
        }

        return jScopes;
    }

    private JSONObject convertClient(Client client) throws JSONException, OXException {
        JSONObject json = new JSONObject();
        json.put("id", client.getId());
        json.put("name", client.getName());
        json.put("description", client.getDescription());
        json.put("website", client.getWebsite());
        json.put("icon", buildIconURL(client.getIcon()));
        return json;
    }

    private String buildIconURL(Icon icon) throws OXException  {
        ManagedFile managedFile = managedFileManagement.createManagedFile(icon.getData(), true);
        managedFile.setContentType(icon.getMimeType());
        managedFile.setFileName(MimeType2ExtMap.getFileExtension(icon.getMimeType()));
        return managedFile.constructURL(session, false);
    }

}
