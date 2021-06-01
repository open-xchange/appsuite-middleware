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

package com.openexchange.snippet.json.action;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.exception.OXException;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.snippet.DefaultSnippet;
import com.openexchange.snippet.SnippetService;
import com.openexchange.snippet.json.SnippetRequest;
import com.openexchange.snippet.utils.SnippetUtils;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link SnippetAction} - Abstract snippet action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class SnippetAction implements AJAXActionService {

    protected static final String MODULE = "userconfig";

    /**
     * Splits a char sequence by comma-separated (<code>','</code>) values.
     */
    protected static final Pattern SPLIT_CSV = Pattern.compile(" *, *");

    /**
     * Splits a char sequence by slash-separated (<code>'/'</code>) values.
     */
    protected static final Pattern SPLIT_PATH = Pattern.compile(Pattern.quote("/"));

    /**
     * The service look-up
     */
    protected final ServiceLookup services;

    /**
     * The service listing.
     */
    private final ServiceListing<SnippetService> snippetServices;

    /**
     * Registered actions.
     */
    protected final Map<String, SnippetAction> actions;

    /**
     * Initializes a new {@link SnippetAction}.
     *
     * @param services The service look-up
     */
    protected SnippetAction(final ServiceLookup services, final ServiceListing<SnippetService> snippetServices, final Map<String, SnippetAction> actions) {
        super();
        this.services = services;
        this.snippetServices = snippetServices;
        this.actions = actions;
    }

    /**
     * Tests for an empty string.
     */
    protected static boolean isEmpty(final String string) {
        return com.openexchange.java.Strings.isEmpty(string);
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData requestData, final ServerSession session) throws OXException {
        try {
            final String action = requestData.getParameter("action");
            if (null == action) {
                final Method method = Method.methodFor(requestData.getAction());
                if (null == method) {
                    throw AjaxExceptionCodes.BAD_REQUEST.create();
                }
                return performREST(new SnippetRequest(requestData, session), method);
            }
            return perform(new SnippetRequest(requestData, session));
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets the snippet service.
     *
     * @param serverSession The server session
     * @return The snippet service
     * @throws OXException If appropriate Snippet service cannot be returned
     */
    protected SnippetService getSnippetService(final ServerSession serverSession) throws OXException {
        final CapabilityService capabilityService = services.getOptionalService(CapabilityService.class);
        for (final SnippetService snippetService : snippetServices.getServiceList()) {
            final List<String> neededCapabilities = snippetService.neededCapabilities();
            if (null == capabilityService || (null == neededCapabilities || neededCapabilities.isEmpty())) {
                // Either no capabilities signaled or service is absent (thus not able to check)
                return snippetService;
            }
            final CapabilitySet capabilities = capabilityService.getCapabilities(serverSession);
            boolean contained = true;
            for (int i = neededCapabilities.size(); contained && i-- > 0;) {
                contained = capabilities.contains(neededCapabilities.get(i));
            }
            if (contained) {
                return snippetService;
            }
        }
        throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(SnippetService.class.getSimpleName());
    }

    /**
     * Performs given snippet request.
     *
     * @param snippetRequest The snippet request
     * @return The AJAX result
     * @throws OXException If performing request fails
     */
    protected abstract AJAXRequestResult perform(SnippetRequest snippetRequest) throws OXException, JSONException, IOException;

    /**
     * Performs given snippet request in REST style.
     *
     * @param snippetRequest The snippet request
     * @param method The REST method to perform
     * @return The AJAX result
     * @throws OXException If performing request fails for any reason
     * @throws JSONException If a JSON error occurs
     */
    @SuppressWarnings("unused")
    protected AJAXRequestResult performREST(final SnippetRequest snippetRequest, final Method method) throws OXException, JSONException, IOException {
        throw AjaxExceptionCodes.BAD_REQUEST.create();
    }

    /**
     * Gets the action identifier for this snippet action.
     *
     * @return The action identifier; e.g. <code>"get"</code>
     */
    public abstract String getAction();

    /**
     * Gets the REST method identifiers for this snippet action.
     *
     * @return The REST method identifiers or <code>null</code> (e.g. <code>"GET"</code>)
     */
    public List<Method> getRESTMethods() {
        return Collections.emptyList();
    }

    /**
     * @param snippet
     * @return
     * @throws OXException
     */
    protected String getContentSubType(DefaultSnippet snippet) throws OXException {
        if (snippet.getMisc() == null) {
            return "plain";
        }
        final String ct = SnippetUtils.parseContentTypeFromMisc(snippet.getMisc());
        return new ContentType(ct).getSubType();
    }

}
