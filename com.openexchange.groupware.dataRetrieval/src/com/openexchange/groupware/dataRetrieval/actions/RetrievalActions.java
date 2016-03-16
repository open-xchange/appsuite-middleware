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

package com.openexchange.groupware.dataRetrieval.actions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.dataRetrieval.Constants;
import com.openexchange.groupware.dataRetrieval.DataProvider;
import com.openexchange.groupware.dataRetrieval.FileMetadata;
import com.openexchange.groupware.dataRetrieval.config.Configuration;
import com.openexchange.groupware.dataRetrieval.registry.DataProviderRegistry;
import com.openexchange.groupware.dataRetrieval.services.Services;
import com.openexchange.groupware.dataRetrieval.servlets.Paths;
import com.openexchange.session.RandomTokenContainer;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link RetrievalActions}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class RetrievalActions implements AJAXActionServiceFactory {

    private static final String REGISTER = "register";

    protected DataProviderRegistry registry;

    protected RandomTokenContainer<Map<String, Object>> paramMap;

    public RetrievalActions(final DataProviderRegistry registry, final RandomTokenContainer<Map<String, Object>> paramMap) {
        this.registry = registry;
        this.paramMap = paramMap;
    }

    @Override
    public AJAXActionService createActionService(final String action) throws OXException {
        if (!action.equals(REGISTER)) {
            throw AjaxExceptionCodes.UNKNOWN_ACTION.create( action);
        }
        return REGISTER_ACTION;
    }

    @Override
    public Collection<? extends AJAXActionService> getSupportedServices() {
        return java.util.Collections.singletonList(REGISTER_ACTION);
    }

    private final AJAXActionService REGISTER_ACTION = new AJAXActionService() {

        @Override
        public AJAXRequestResult perform(final AJAXRequestData requestData, final ServerSession session) throws OXException {
            final String id = requestData.getParameter("datasource");
            DataProvider provider = null;
            Object state = null;
            try {
                provider = registry.getProvider(id);
                state = provider.start();

                final Map<String, Object> parameters = new HashMap<String, Object>();

                final Iterator<String> parameterNames = requestData.getParameterNames();
                while (parameterNames.hasNext()) {
                    final String name = parameterNames.next();
                    parameters.put(name, requestData.getParameter(name));
                }

                final FileMetadata metadata = provider.retrieveMetadata(state, parameters, session);
                parameters.put(Constants.SESSION_KEY, session);
                parameters.put(Constants.CREATED, System.currentTimeMillis());
                final String token = paramMap.rememberForSession(session, parameters);

                return new AJAXRequestResult(toJSON(metadata, getURI(token, requestData)));
            } finally {
                if (provider != null && state != null) {
                    provider.close(state);
                }
            }
        }

        private String getURI(final String token, final AJAXRequestData request) {
            final Configuration configuration = Services.getConfiguration();
            return request.constructURL(configuration.getForcedProtocol() , Services.SERVICE_LOOKUP.getService(DispatcherPrefixService.class).getPrefix()+Paths.FILE_DELIVERY_PATH_APPENDIX, true, "token=" + token).toString();
        }

        private JSONObject toJSON(final FileMetadata metadata, final String uri) throws OXException {
            try {
                final JSONObject json = new JSONObject();
                if (metadata.getFilename() != null) {
                    json.put("filename", metadata.getFilename());
                }

                if (metadata.getType() != null) {
                    json.put("mimeType", metadata.getType());
                }

                if (metadata.getSize() > 0) {
                    json.put("length", metadata.getSize());
                }

                json.put("url", uri);
                return json;
            } catch (final JSONException e) {
                throw AjaxExceptionCodes.JSON_ERROR.create( e, e.getMessage());
            }
        }

    };

}
