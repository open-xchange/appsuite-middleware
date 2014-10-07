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
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.modules.Module;
import com.openexchange.session.Session;
import com.openexchange.share.Share;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.servlet.internal.ShareLoginConfiguration;
import com.openexchange.share.servlet.internal.ShareServiceLookup;
import com.openexchange.tools.servlet.http.Tools;


/**
 * {@link LoginShareHandler} - The share handler that redirects to standard login page.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class LoginShareHandler extends AbstractShareHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LoginShareHandler.class);

    /**
     * Initializes a new {@link LoginShareHandler}.
     *
     * @param shareLoginConfiguration The login configuration for shares
     */
    public LoginShareHandler() {
        super();
    }

    @Override
    public int getRanking() {
        return 10;
    }

    /**
     * Checks if this redirecting share handler fees responsible for passed share
     *
     * @param request The associated HTTP request
     * @return <code>true</code> if share can be handled; otherwise <code>false</code>
     */
    protected boolean handles(HttpServletRequest request){
        String ua = request.getHeader("User-Agent");
        // Full list of browsers: http://www.useragentstring.com/pages/Browserlist/
        return null != ua && (ua.startsWith("Mozilla/") || ua.startsWith("Opera/"));
    }

    @Override
    public boolean handle(Share share, HttpServletRequest request, HttpServletResponse response) throws OXException {
        if (false == handles(request)) {
            // Not a Browser
            return false;
        }

        try {
            ShareLoginConfiguration shareLoginConfig = getShareLoginConfiguration();
            LoginConfiguration loginConfig = shareLoginConfig.getLoginConfig(share);

            int contextId = share.getContextID();
            int guestId = share.getGuest();

            // TODO: Jump to APp Suite UI login page

            return true;
        } catch (RuntimeException e) {
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
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
        ConfigurationService configService = ShareServiceLookup.getService(ConfigurationService.class);
        boolean isFolderShare = share.isFolder();

        String redirectLink;
        if (isFolderShare) {
            redirectLink = configService.getProperty("com.openexchange.share.loginLinkFolder",
                "/[uiwebpath]#store=[store]&user=[user]&user_id=[user_id]&language=[language]&m=[module]&f=[folder]");
        } else {
            redirectLink = configService.getProperty("com.openexchange.share.loginLinkItem",
                "/[uiwebpath]#store=[store]&user=[user]&user_id=[user_id]&language=[language]&m=[module]&f=[folder]&i=[item]");
        }

        {
            String uiWebPath = loginConfig.getUiWebPath(); // uiWebPath = "/ox6/index.html";
            redirectLink = P_UIWEBPATH.matcher(redirectLink).replaceAll(Matcher.quoteReplacement(trimSlashes(uiWebPath)));
        }
        redirectLink = P_USER.matcher(redirectLink).replaceAll(Matcher.quoteReplacement(user.getMail()));
        redirectLink = P_USER_ID.matcher(redirectLink).replaceAll(Integer.toString(user.getId()));
        redirectLink = P_LANGUAGE.matcher(redirectLink).replaceAll(Matcher.quoteReplacement(String.valueOf(user.getLocale())));
        redirectLink = P_MODULE.matcher(redirectLink).replaceAll(Matcher.quoteReplacement(Module.getForFolderConstant(share.getModule()).getName()));
        redirectLink = P_FOLDER.matcher(redirectLink).replaceAll(Matcher.quoteReplacement(share.getFolder()));
        if (false == isFolderShare) {
            redirectLink = P_ITEM.matcher(redirectLink).replaceAll(Matcher.quoteReplacement(share.getItem()));
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
