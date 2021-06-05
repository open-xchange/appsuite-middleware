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

package com.openexchange.config.json.actions;

import static com.openexchange.tools.TimeZoneUtils.getTimeZone;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.AJAXUtility;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.config.json.ConfigAJAXRequest;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractConfigAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractConfigAction implements AJAXActionService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractConfigAction.class);

    protected static final String MODULE = "userconfig";

    private static final AJAXRequestResult RESULT_JSON_NULL = new AJAXRequestResult(JSONObject.NULL, "json");

    // ------------------------------------------------------------------------------------------------------------------

    /** The service look-up for tracked OSGi services */
    protected final ServiceLookup services;

    /**
     * Initializes a new {@link AbstractConfigAction}.
     */
    protected AbstractConfigAction(final ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Gets the service of specified type
     *
     * @param clazz The service's class
     * @return The service or <code>null</code> if absent
     */
    protected <S> S getService(Class<? extends S> clazz) {
        return services.getOptionalService(clazz);
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData requestData, final ServerSession session) throws OXException {
        try {
            final ConfigAJAXRequest ajaxRequest = new ConfigAJAXRequest(requestData, session);
            final String sTimeZone = requestData.getParameter(AJAXServlet.PARAMETER_TIMEZONE);
            if (null != sTimeZone) {
                ajaxRequest.setTimeZone(getTimeZone(sTimeZone));
            }
            return perform(ajaxRequest);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } catch (IllegalStateException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof OXException) {
                throw (OXException) cause;
            }
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Performs specified config request.
     *
     * @param req The config request
     * @return The result
     * @throws OXException If an error occurs
     * @throws JSONException If a JSON error occurs
     */
    protected abstract AJAXRequestResult perform(ConfigAJAXRequest req) throws OXException, JSONException;

    /**
     * Returns the URI part after path to the servlet.
     *
     * @param req the request that url should be parsed
     * @return the URI part after the path to your servlet.
     */
    protected static String getServletSpecificURI(final HttpServletRequest req) {
        String uri;
        try {
            final String characterEncoding = req.getCharacterEncoding();
            uri = AJAXUtility.decodeUrl(req.getRequestURI(), characterEncoding == null ? ServerConfig.getProperty(ServerConfig.Property.DefaultEncoding) : characterEncoding);
        } catch (RuntimeException e) {
            LOG.error("Unsupported encoding", e);
            uri = req.getRequestURI();
        }
        final String path = new StringBuilder(req.getContextPath()).append(req.getServletPath()).toString();
        final int pos = uri.indexOf(path);
        if (pos != -1) {
            uri = uri.substring(pos + path.length());
        }
        return uri;
    }

    /**
     * Gets the result filled with JSON <code>NULL</code>.
     *
     * @return The result with JSON <code>NULL</code>.
     */
    protected static AJAXRequestResult getJSONNullResult() {
        return RESULT_JSON_NULL;
    }
}
