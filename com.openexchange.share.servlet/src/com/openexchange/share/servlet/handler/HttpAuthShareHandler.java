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

package com.openexchange.share.servlet.handler;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.AJAXUtility;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.ajax.login.ShareLoginConfiguration;
import com.openexchange.exception.OXException;
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
    protected abstract boolean handles(AccessShareRequest shareRequest, HttpServletRequest request, HttpServletResponse response) throws OXException;

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

    protected static boolean indicatesDownload(HttpServletRequest request) {
        return "download".equalsIgnoreCase(AJAXUtility.sanitizeParam(request.getParameter("delivery"))) ||
            isTrue(AJAXUtility.sanitizeParam(request.getParameter("dl")));
    }

    protected static boolean isTrue(String value) {
        return "1".equals(value) || "yes".equalsIgnoreCase(value) || Boolean.valueOf(value).booleanValue();
    }

}
