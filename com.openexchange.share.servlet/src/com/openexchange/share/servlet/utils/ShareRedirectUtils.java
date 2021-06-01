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

package com.openexchange.share.servlet.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.groupware.ModuleSupport;
import com.openexchange.share.servlet.internal.ShareServiceLookup;
import com.openexchange.user.User;

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
    public static String getWebSessionRedirectURL(Session session, User user, ShareTarget target, LoginConfiguration loginConfig) throws OXException {
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
