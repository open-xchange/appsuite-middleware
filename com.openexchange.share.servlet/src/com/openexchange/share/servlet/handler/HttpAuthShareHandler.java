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

package com.openexchange.share.servlet.handler;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.AJAXUtility;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.ajax.login.ShareLoginConfiguration;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptions;
import com.openexchange.login.LoginResult;
import com.openexchange.session.Session;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.servlet.auth.ShareLoginMethod;
import com.openexchange.share.servlet.utils.ShareServletUtils;


/**
 * {@link HttpAuthShareHandler}
 * <p>
 * This share handler asks for credentials via HTTP Authentication as needed and performs a login, establishing a dedicated session.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public abstract class HttpAuthShareHandler extends AbstractShareHandler {

    /**
     * Initializes a new {@link HttpAuthShareHandler}.
     */
    public HttpAuthShareHandler() {
        super();
    }

    /**
     * Gets a value indicating whether the guest's session should be kept alive, or if an implicit logout should be performed afterwards.
     *
     * @return <code>true</code> if the session should be kept, <code>false</code>, otherwise
     */
    protected abstract boolean keepSession();

    /**
     * Checks if this redirecting share handler fees responsible for passed guest and target
     *
     * @param shareRequest The share request
     * @param request The associated HTTP request
     * @param response The associated HTTP response
     * @return <code>true</code> if share can be handled; otherwise <code>false</code>
     * @throws OXException If check fails for any reason
     */
    protected abstract boolean handles(AccessShareRequest shareRequest, HttpServletRequest request, HttpServletResponse response);

    /**
     * Handles the given resolved share.
     *
     * @param resolvedShare The resolved share
     * @throws OXException If handling the resolved share fails
     * @throws IOException If an I/O error occurs
     */
    protected abstract void handleResolvedShare(ResolvedShare resolvedShare) throws OXException, IOException;

    @Override
    public ShareHandlerReply handle(AccessShareRequest shareRequest, HttpServletRequest request, HttpServletResponse response) throws OXException {
        if (false == handles(shareRequest, request, response)) {
            return ShareHandlerReply.NEUTRAL;
        }

        Session session = null;
        try {
            /*
             * get, authenticate and login as associated guest user
             */
            GuestInfo guest = shareRequest.getGuest();
            ShareLoginConfiguration shareLoginConfig = ShareServletUtils.getShareLoginConfiguration();
            LoginConfiguration loginConfig = shareLoginConfig.getLoginConfig(guest);
            ShareLoginMethod shareLoginMethod = getShareLoginMethod(shareRequest);
            LoginResult loginResult = ShareServletUtils.login(guest, request, response, loginConfig, shareLoginConfig.isTransientShareSessions(), shareLoginMethod);
            if (null == loginResult) {
                shareLoginMethod.sendUnauthorized(request, response);
                return ShareHandlerReply.DENY;
            }
            session = loginResult.getSession();

            handleResolvedShare(new ResolvedShare(shareRequest, loginResult, loginConfig, request, response));
            return ShareHandlerReply.ACCEPT;
        } catch (IOException e) {
            throw ShareExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (false == keepSession()) {
                ShareServletUtils.logout(session);
            }
        }
    }

    @Override
    public ShareHandlerReply handleNotFound(HttpServletRequest request, HttpServletResponse response, String status) throws IOException {
        if (false == handles(new AccessShareRequest(null, null, null, true), request, response)) {
            return ShareHandlerReply.NEUTRAL;
        }
        sendError(response, OXExceptions.notFound(""));
        return ShareHandlerReply.ACCEPT;
    }

    protected static void sendError(HttpServletResponse response, OXException e) throws IOException {
        if (null == response || response.isCommitted()) {
            org.slf4j.LoggerFactory.getLogger(HttpAuthShareHandler.class).debug("Unable to send error response after exception", e);
            return;
        }
        response.sendError(getStatusCode(e), e.getSoleMessage());
    }

    protected static boolean indicatesDownload(HttpServletRequest request) {
        return "download".equalsIgnoreCase(AJAXUtility.sanitizeParam(request.getParameter("delivery"))) ||
            isTrue(AJAXUtility.sanitizeParam(request.getParameter("dl")));
    }

    protected static boolean isTrue(String value) {
        return "1".equals(value) || "yes".equalsIgnoreCase(value) || Boolean.valueOf(value).booleanValue();
    }

    protected static int getStatusCode(OXException e) {
        if (e.isNotFound()) {
            return HttpServletResponse.SC_NOT_FOUND;
        }
        if (e.isNoPermission() || OXExceptionCode.CATEGORY_PERMISSION_DENIED.equals(e.getCategory())) {
            return HttpServletResponse.SC_FORBIDDEN;
        }
        if (OXExceptionCode.CATEGORY_SERVICE_DOWN.equals(e.getCategory())) {
            return HttpServletResponse.SC_SERVICE_UNAVAILABLE;
        }
        if (e.isConflict() || OXExceptionCode.CATEGORY_CONFLICT.equals(e.getCategory())) {
            return HttpServletResponse.SC_CONFLICT;
        }
        switch (e.getErrorCode()) {
            case "SHR-0002": // ShareExceptionCodes.UNKNOWN_SHARE
            case "SHR-0003": // ShareExceptionCodes.INVALID_LINK
            case "SHR-0015": // ShareExceptionCodes.UNKNOWN_GUEST
            case "SHR-0016": // ShareExceptionCodes.INVALID_TOKEN
            case "SHR-0023": // ShareExceptionCodes.INVALID_LINK_TARGET
                return HttpServletResponse.SC_NOT_FOUND;
            default:
                return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
    }

}
