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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.modules.Module;
import com.openexchange.login.LoginResult;
import com.openexchange.session.Session;
import com.openexchange.share.Share;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.servlet.internal.ShareLoginConfiguration;
import com.openexchange.share.servlet.utils.ShareServletUtils;
import com.openexchange.tools.servlet.http.Tools;


/**
 * {@link RedirectingShareHandler} - The basic share handler that redirects to Web Interface.
 * <p>
 * This share handler performs a login, establishing a dedicated session.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class RedirectingShareHandler extends AbstractShareHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RedirectingShareHandler.class);

    /**
     * Initializes a new {@link RedirectingShareHandler}.
     *
     * @param shareLoginConfiguration The login configuration for shares
     */
    public RedirectingShareHandler() {
        super();
    }

    @Override
    public int getRanking() {
        return 0;
    }

    /**
     * Gets a value indicating whether the guest's session should be kept alive, or if an implicit logout should be performed afterwards.
     *
     * @return <code>true</code> if the session should be kept, <code>false</code>, otherwise
     */
    protected boolean keepSession() {
        return true;
    }

    /**
     * Checks if this redirecting share handler fees responsible for passed share
     *
     * @param share The share
     * @param request The associated HTTP request
     * @param response The associated HTTP response
     * @return <code>true</code> if share can be handled; otherwise <code>false</code>
     * @throws OXException If check fails for any reason
     */
    protected boolean handles(Share share, HttpServletRequest request, HttpServletResponse response) throws OXException {
        return true;
    }

    @Override
    public boolean handle(Share share, HttpServletRequest request, HttpServletResponse response) throws OXException {
        if (false == handles(share, request, response)) {
            return false;
        }
        Session session = null;
        try {
            /*
             * get, authenticate and login as associated guest user
             */
            ShareLoginConfiguration shareLoginConfig = getShareLoginConfiguration();
            LoginConfiguration loginConfig = shareLoginConfig.getLoginConfig(share);
            LoginResult loginResult = ShareServletUtils.login(share, request, response, loginConfig, shareLoginConfig.isTransientShareSessions());
            if (null == loginResult) {
                return false;
            }
            session = loginResult.getSession();
            handleResolvedShare(new ResolvedShare(share, loginResult, loginConfig, request, response));
            return true;
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

    /**
     * Handles the given resolved share.
     *
     * @param resolvedShare The resolved share
     * @throws OXException If handling the resolved share fails
     * @throws IOException If an I/O error occurs
     */
    protected void handleResolvedShare(ResolvedShare resolvedShare) throws OXException, IOException {
        try {
            /*
             * prepare response
             */
            HttpServletRequest request = resolvedShare.getRequest();
            HttpServletResponse response = resolvedShare.getResponse();
            Tools.disableCaching(response);
            LoginServlet.addHeadersAndCookies(resolvedShare.getLoginResult(), response);
            LoginServlet.writeSecretCookie(request, response, resolvedShare.getSession(), resolvedShare.getSession().getHash(), request.isSecure(), request.getServerName(), resolvedShare.getLoginConfig());
            /*
             * construct & send redirect
             */
            String url = getRedirectURL(resolvedShare.getSession(), resolvedShare.getUser(), resolvedShare.getShare(), resolvedShare.getLoginConfig());
            LOG.info("Redirecting share {} to {}...", resolvedShare.getShare().getToken(), url);
            response.sendRedirect(url);
        } catch (RuntimeException e) {
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    // --------------------------------------------------------------------------------------------------------- //

    private static final Pattern P_UIWEBPATH = Pattern.compile("[uiwebpath]", Pattern.LITERAL);
    private static final Pattern P_SESSION = Pattern.compile("[session]", Pattern.LITERAL);
    private static final Pattern P_USER = Pattern.compile("[user]", Pattern.LITERAL);
    private static final Pattern P_USER_ID = Pattern.compile("[user_id]", Pattern.LITERAL);
    private static final Pattern P_LANGUAGE = Pattern.compile("[language]", Pattern.LITERAL);
    private static final Pattern P_MODULE = Pattern.compile("[module]", Pattern.LITERAL);
    private static final Pattern P_FOLDER = Pattern.compile("[folder]", Pattern.LITERAL);
    private static final Pattern P_ITEM = Pattern.compile("[item]", Pattern.LITERAL);
    private static final Pattern P_STORE = Pattern.compile("[store]", Pattern.LITERAL);

    /**
     * Constructs the redirect URL pointing to the share in the web interface.
     *
     * @param session The session
     * @param user The user
     * @param share The share
     * @param loginConfig The login configuration to use
     * @return The redirect URL
     */
    protected static String getRedirectURL(Session session, User user, Share share, LoginConfiguration loginConfig) {
        /*
         * prepare url
         */
        StringBuilder stringBuilder = new StringBuilder("/[uiwebpath]#session=[session]&store=[store]&user=[user]&user_id=[user_id]");
        int module = share.getCommonModule();
        String folder = share.getCommonFolder();
        String item = null != share.getTargets() && 1 == share.getTargets().size() ? share.getTargets().get(0).getItem() : null;
        if (0 != module) {
            stringBuilder.append("&m=[module]");
        }
        if (null != folder) {
            stringBuilder.append("&f=[folder]");
        }
        if (null != item) {
            stringBuilder.append("&i=[item]");
        }
        String redirectLink = stringBuilder.toString();
        /*
         * replace templates
         */
        String uiWebPath = loginConfig.getUiWebPath();
        uiWebPath = "/ox6/index.html";
        redirectLink = P_UIWEBPATH.matcher(redirectLink).replaceAll(Matcher.quoteReplacement(trimSlashes(uiWebPath)));
        redirectLink = P_SESSION.matcher(redirectLink).replaceAll(Matcher.quoteReplacement(session.getSessionID()));
        redirectLink = P_USER.matcher(redirectLink).replaceAll(Matcher.quoteReplacement(user.getMail()));
        redirectLink = P_USER_ID.matcher(redirectLink).replaceAll(Integer.toString(user.getId()));
        redirectLink = P_LANGUAGE.matcher(redirectLink).replaceAll(Matcher.quoteReplacement(String.valueOf(user.getLocale())));
        if (0 != module) {
            Module folderModule = Module.getForFolderConstant(module);
            String name = null != folderModule ? folderModule.getName() : String.valueOf(module);
            redirectLink = P_MODULE.matcher(redirectLink).replaceAll(Matcher.quoteReplacement(name));
        }
        if (null != folder) {
            redirectLink = P_FOLDER.matcher(redirectLink).replaceAll(Matcher.quoteReplacement(folder));
        }
        if (null != item) {
            redirectLink = P_ITEM.matcher(redirectLink).replaceAll(Matcher.quoteReplacement(item));
        }
        redirectLink = P_STORE.matcher(redirectLink).replaceAll(loginConfig.isSessiondAutoLogin() ? "true" : "false");
        return redirectLink;
    }

    private static String trimSlashes(String path) {
        String pazz = path;
        if (null != pazz && 0 < pazz.length()) {
            if ('/' == pazz.charAt(0)) {
                pazz = pazz.substring(1);
            }
            if (0 < pazz.length() && '/' == pazz.charAt(pazz.length() - 1)) {
                pazz = pazz.substring(0, pazz.length() - 1);
            }
        }
        return pazz;
    }

}
