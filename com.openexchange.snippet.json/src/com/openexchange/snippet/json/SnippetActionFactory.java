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

package com.openexchange.snippet.json;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthModule;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.server.ServiceLookup;
import com.openexchange.snippet.SnippetService;
import com.openexchange.snippet.json.action.Method;
import com.openexchange.snippet.json.action.SnippetAction;

/**
 * {@link SnippetActionFactory}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SuppressWarnings("deprecation")
@OAuthModule
public class SnippetActionFactory implements AJAXActionServiceFactory {

    private final Map<String, SnippetAction> actions;

    /**
     * Initializes a new {@link SnippetActionFactory}.
     *
     * @param services The service look-up
     */
    public SnippetActionFactory(final ServiceLookup services, final ServiceListing<SnippetService> snippetServices) {
        super();
        actions = new ConcurrentHashMap<String, SnippetAction>(12, 0.9f, 1);
        addAction(new com.openexchange.snippet.json.action.AllAction(services, snippetServices, actions));
        addAction(new com.openexchange.snippet.json.action.GetAction(services, snippetServices, actions));
        addAction(new com.openexchange.snippet.json.action.ListAction(services, snippetServices, actions));
        addAction(new com.openexchange.snippet.json.action.DeleteAction(services, snippetServices, actions));
        addAction(new com.openexchange.snippet.json.action.NewAction(services, snippetServices, actions));
        addAction(new com.openexchange.snippet.json.action.ImportAction(services, snippetServices, actions));
        addAction(new com.openexchange.snippet.json.action.AttachAction(services, snippetServices, actions));
        addAction(new com.openexchange.snippet.json.action.DetachAction(services, snippetServices, actions));
        addAction(new com.openexchange.snippet.json.action.UpdateAction(services, snippetServices, actions));
        addAction(new com.openexchange.snippet.json.action.GetAttachmentAction(services, snippetServices, actions));
    }

    private void addAction(final SnippetAction snippetAction) {
        final List<Method> restMethods = snippetAction.getRESTMethods();
        if (null != restMethods && !restMethods.isEmpty()) {
            for (final Method method : restMethods) {
                actions.put(method.toString(), snippetAction);
            }
        }
        actions.put(snippetAction.getAction(), snippetAction);
    }

    @Override
    public AJAXActionService createActionService(final String action) throws OXException {
        return actions.get(action);
    }

}
