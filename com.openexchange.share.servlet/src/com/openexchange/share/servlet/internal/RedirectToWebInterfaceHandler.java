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

package com.openexchange.share.servlet.internal;

import java.io.IOException;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.config.ConfigurationService;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.modules.Module;
import com.openexchange.session.Session;
import com.openexchange.share.Share;
import com.openexchange.share.servlet.handler.ResolvedShare;
import com.openexchange.share.servlet.handler.ShareHandler;


/**
 * {@link ShareHandler}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class RedirectToWebInterfaceHandler implements ShareHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RedirectToWebInterfaceHandler.class);

    @Override
    public boolean keepSession() {
        return true;
    }

    @Override
    public boolean handles(ResolvedShare share) {
        return true;
    }

    @Override
    public void handle(ResolvedShare share) throws IOException {
        /*
         * construct & send redirect
         */
        String url = getRedirectURL(share.getSession(), share.getUser(), share.getShare(), share.getLoginConfig());
        LOG.info("Redirecting share {} to {}...", share.getShare().getToken(), url);
        share.getResponse().sendRedirect(url);
    }

    /**
     * Constructs the redirect URL pointing to the share in the web interface.
     *
     * @param session The session
     * @param user The user
     * @param share The share
     * @param loginConfig The login configuration to use
     * @return The redirect URL
     */
    private static String getRedirectURL(Session session, User user, Share share, LoginConfiguration loginConfig) {
        ConfigurationService configService = ShareServiceLookup.getService(ConfigurationService.class);
        String redirectLink;
        if (share.isFolder()) {
            redirectLink = configService.getProperty("com.openexchange.share.redirectLinkFolder",
                "/[uiwebpath]#session=[session]&store=[store]&user=[user]&user_id=[user_id]&language=[language]" +
                "&m=[module]&f=[folder]");
        } else {
            redirectLink = configService.getProperty("com.openexchange.share.redirectLinkItem",
                "/[uiwebpath]#session=[session]&store=[store]&user=[user]&user_id=[user_id]&language=[language]" +
                "&m=[module]&f=[folder]&i=[item]");
        }
        String uiWebPath = loginConfig.getUiWebPath();
//       uiWebPath = "/ox6/index.html";
        return redirectLink
            .replaceAll("\\[uiwebpath\\]", trimSlashes(uiWebPath))
            .replaceAll("\\[session\\]", session.getSessionID())
            .replaceAll("\\[user\\]", user.getMail())
            .replaceAll("\\[user_id\\]", String.valueOf(user.getId()))
            .replaceAll("\\[language\\]", String.valueOf(user.getLocale()))
            .replaceAll("\\[module\\]", Module.getForFolderConstant(share.getModule()).getName())
            .replaceAll("\\[folder\\]", share.getFolder())
            .replaceAll("\\[item\\]", share.getItem())
            .replaceAll("\\[store\\]", String.valueOf(loginConfig.isSessiondAutoLogin()))
        ;
    }

    private static String trimSlashes(String path) {
        if (null != path && 0 < path.length()) {
            if ('/' == path.charAt(0)) {
                path = path.substring(1);
            }
            if (0 < path.length() && '/' == path.charAt(path.length() - 1)) {
                path = path.substring(0, path.length() - 1);
            }
        }
        return path;
    }

}
