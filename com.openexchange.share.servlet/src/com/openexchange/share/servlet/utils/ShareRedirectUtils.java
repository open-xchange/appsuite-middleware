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

package com.openexchange.share.servlet.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.servlet.internal.ShareServiceLookup;

/**
 * Utility methods to handle share redirects.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class ShareRedirectUtils {

    private static final String S_UIWEBPATH = "[uiwebpath]";

    /**
     * Constructs the redirect URL pointing to the share in the web interface.
     *
     * @param session The session
     * @param user The user
     * @param target The share target within the share, or <code>null</code> if not addressed
     * @param loginConfig The login configuration to use
     * @return The redirect URL
     * @throws OXException If redirect URL cannot be generated
     */
    public static String getWebSessionRedirectURL(Session session, User user, ShareTarget target, LoginConfiguration loginConfig, String host) throws OXException {
        /*
         * evaluate link destination based on share or target
         */
        int module = -1;
        String folder = null;
        String item = null;
        if (null != target) {
            module = target.getModule();
            folder = target.getFolder();
            item = target.getItem();
        }
        /*
         * prepare url
         */
        StringBuilder stringBuilder = new StringBuilder(getLoginLink()).append("#!");
        stringBuilder.append("&session=").append(session.getSessionID());
        stringBuilder.append("&store=").append(loginConfig.isSessiondAutoLogin(host, session) ? true : false);
        stringBuilder.append("&user=").append(urlEncode(user.getMail()));
        stringBuilder.append("&user_id=").append(user.getId());
        stringBuilder.append("&context_id=").append(session.getContextId());
        if (module > 0) {
            ModuleSupport moduleSupport = ShareServiceLookup.getService(ModuleSupport.class);
            if (null == moduleSupport) {
                throw ServiceExceptionCode.absentService(ModuleSupport.class);
            }
            String name = moduleSupport.getShareModule(module);
            stringBuilder.append("&m=").append(urlEncode(name));
        }
        if (null != folder) {
            stringBuilder.append("&f=").append(urlEncode(folder));
        }
        if (null != item) {
            stringBuilder.append("&i=").append(urlEncode(item));
        }
        return stringBuilder.toString();
    }

    private static String urlEncode(String s) {
        if (null == s) {
            return s;
        }

        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }

    /**
     * Gets the relative path to the login page as defined by the <code>com.openexchange.share.loginLink</code> and
     * <code>com.openexchange.UIWebPath</code> configuration properties.
     *
     * @return The relative login link, e.g. <code>/appsuite/ui</code>
     */
    public static String getLoginLink() {
        ConfigurationService configService = ShareServiceLookup.getService(ConfigurationService.class);
        if (configService == null) {
            // If config service is not available fall back to defaults
            return "/appsuite/ui";
        }

        String loginLink = configService.getProperty("com.openexchange.share.loginLink", "/[uiwebpath]/ui");
        String uiWebPath = configService.getProperty("com.openexchange.UIWebPath", "/appsuite/");
        return Strings.replaceSequenceWith(loginLink, S_UIWEBPATH, trimSlashes(uiWebPath));
    }

    /**
     * Trims trailing and leading slashes from the supplied path.
     *
     * @param path The path
     * @return The trimmed path
     */
    public static String trimSlashes(String path) {
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
