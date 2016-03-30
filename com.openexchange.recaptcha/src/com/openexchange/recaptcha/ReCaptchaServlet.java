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

package com.openexchange.recaptcha;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.DataServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.recaptcha.osgi.ReCaptchaServiceRegistry;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ReCaptchaServlet}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class ReCaptchaServlet extends DataServlet {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ReCaptchaServlet.class);

    private static final String ACTION_HTML = "html";

    private static final String ACTION_KEY = "key";

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
        } catch (final OXException e) {
            LOG.error("", e);
            response.setException(e);
        }

        writeResponse(response, resp, getSessionObject(req));
    }

    private void doGetPublicKey(final Response response) {
        response.setData(ReCaptchaServiceRegistry.getInstance().getService(ConfigurationService.class).getProperty("publicKey"));
    }

    private void doGetHtml(final Response response) {
        response.setData(ReCaptchaServiceRegistry.getInstance().getService(ReCaptchaService.class).getHTML());
    }

}
