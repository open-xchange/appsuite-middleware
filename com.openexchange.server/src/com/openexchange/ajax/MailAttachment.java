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

package com.openexchange.ajax;

import java.io.IOException;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.responseRenderers.FileResponseRenderer;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.attachment.AttachmentToken;
import com.openexchange.mail.attachment.AttachmentTokenService;
import com.openexchange.mail.json.MailActionFactory;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link MailAttachment}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailAttachment extends AJAXServlet {

    private static final long serialVersionUID = -3109402774466180271L;

    private static final String PARAMETER_MAILATTCHMENT = Mail.PARAMETER_MAILATTCHMENT;

    private final FileResponseRenderer renderer;

    /**
     * Initializes a new {@link MailAttachment}.
     */
    public MailAttachment() {
        super();
        this.renderer = new FileResponseRenderer();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Tools.disableCaching(resp);
        /*
         * Get attachment
         */
        try {
            String id = req.getParameter(PARAMETER_ID);
            if (null == id) {
                Tools.sendErrorPage(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing mandatory parameter");
                return;
            }

            // Check if client requests a range
            boolean hasRangeHeader = com.openexchange.tools.servlet.http.Tools.hasRangeHeader(req);

            // Look-up attachment by token identifier
            AttachmentTokenService service = ServerServiceRegistry.getInstance().getService(AttachmentTokenService.class, true);
            AttachmentToken token = service.getToken(id, hasRangeHeader);
            if (null == token) {
                // No such attachment
                Tools.sendErrorPage(resp, HttpServletResponse.SC_NOT_FOUND, MailExceptionCode.ATTACHMENT_EXPIRED.create().getDisplayMessage(Locale.US));
                return;
            }

            // IP check
            if (token.isCheckIp() && null != token.getClientIp() && !req.getRemoteAddr().equals(token.getClientIp())) {
                service.removeToken(id);
                Tools.sendErrorPage(resp, HttpServletResponse.SC_NOT_FOUND, MailExceptionCode.ATTACHMENT_EXPIRED.create().getDisplayMessage(Locale.US));
                return;
            }

            // At least expect the same user agent as the one which created the attachment token
//            if (token.isOneTime() && null != token.getUserAgent()) {
//                final String requestUserAgent = req.getHeader("user-agent");
//                if (null == requestUserAgent) {
//                    service.removeToken(id);
//                    Tools.sendErrorPage(resp, HttpServletResponse.SC_NOT_FOUND, MailExceptionCode.ATTACHMENT_EXPIRED.create().getDisplayMessage(Locale.US));
//                    return;
//                }
//                if (!BrowserDetector.detectorFor(token.getUserAgent()).nearlyEquals(BrowserDetector.detectorFor(requestUserAgent))) {
//                    service.removeToken(id);
//                    Tools.sendErrorPage(resp, HttpServletResponse.SC_NOT_FOUND, MailExceptionCode.ATTACHMENT_EXPIRED.create().getDisplayMessage(Locale.US));
//                    return;
//                }
//            }

            MailActionFactory actionFactory = MailActionFactory.getActionFactory();
            if (null == actionFactory) {
                Tools.sendErrorPage(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not orderly initialized");
                return;
            }
            AJAXActionService getAttachmentAction = actionFactory.createActionService("attachment");
            if (null == getAttachmentAction) {
                Tools.sendErrorPage(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not orderly initialized");
                return;
            }

            // Perform request
            try {
                SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
                Session session = sessiondService.getSession(token.getSessionId());
                ServerSession serverSession = ServerSessionAdapter.valueOf(session);
                DispatcherPrefixService dispatcherPrefixService = ServerServiceRegistry.getServize(DispatcherPrefixService.class, true);

                // Create request & yield result
                AJAXRequestData request = AJAXRequestDataTools.getInstance().parseRequest(req, false, false, serverSession, dispatcherPrefixService.getPrefix(), resp);
                request.setSession(serverSession);
                request.putParameter(PARAMETER_FOLDERID, token.getFolderPath());
                request.putParameter(PARAMETER_ID, token.getMailId());
                request.putParameter(PARAMETER_MAILATTCHMENT, token.getAttachmentId());

                AJAXRequestResult result = getAttachmentAction.perform(request, serverSession);

                renderer.write(request, result, req, resp);
            } catch (OXException e) {
                Tools.sendErrorPage(resp, HttpServletResponse.SC_NOT_FOUND, e.getDisplayMessage(Locale.US));
                return;
            }
        } catch (OXException e) {
            Tools.sendErrorPage(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getDisplayMessage(Locale.US));
            return;
        }
    }

}
