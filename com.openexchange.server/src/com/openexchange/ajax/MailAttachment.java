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
import com.openexchange.sessiond.SessionExceptionCodes;
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
                if (null == session) {
                    // No such session
                    Tools.sendErrorPage(resp, HttpServletResponse.SC_NOT_FOUND, SessionExceptionCodes.SESSION_EXPIRED.create(token.getSessionId()).getDisplayMessage(Locale.US));
                    return;
                }
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
