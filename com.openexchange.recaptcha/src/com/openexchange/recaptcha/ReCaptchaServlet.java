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

package com.openexchange.recaptcha;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.DataServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ReCaptchaServlet}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class ReCaptchaServlet extends DataServlet {

    private static final long serialVersionUID = 2748741720778811479L;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ReCaptchaServlet.class);

    private static final String ACTION_HTML = "html";
    private static final String ACTION_KEY = "key";

    // -----------------------------------------------------------------------------------------------------

    private transient final ServiceLookup services;

    /**
     * Initializes a new {@link ReCaptchaServlet}.
     */
    public ReCaptchaServlet(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    protected boolean hasModulePermission(final ServerSession session) {
        return true;
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final Response response = new Response();
        try {
            final String action = parseMandatoryStringParameter(req, PARAMETER_ACTION);

            if (action.equalsIgnoreCase(ACTION_HTML)) {
                doGetHtml(response);
            } else if (action.equalsIgnoreCase(ACTION_KEY)) {
                doGetPublicKey(response);
            }
        } catch (OXException e) {
            LOG.error("", e);
            response.setException(e);
        }

        writeResponse(response, resp, getSessionObject(req));
    }

    private void doGetPublicKey(final Response response) {
        response.setData(services.getService(ConfigurationService.class).getProperty("publicKey"));
    }

    private void doGetHtml(final Response response) {
        response.setData(services.getService(ReCaptchaService.class).getHTML());
    }

}
