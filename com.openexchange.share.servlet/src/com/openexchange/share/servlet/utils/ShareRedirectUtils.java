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

package com.openexchange.share.servlet.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.share.AuthenticationMode;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.GuestShare;
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

    private static final Pattern P_UIWEBPATH = Pattern.compile("[uiwebpath]", Pattern.LITERAL);
    private static final Pattern P_SESSION = Pattern.compile("[session]", Pattern.LITERAL);
    private static final Pattern P_USER = Pattern.compile("[user]", Pattern.LITERAL);
    private static final Pattern P_USER_ID = Pattern.compile("[user_id]", Pattern.LITERAL);
    private static final Pattern P_CONTEXT_ID = Pattern.compile("[context_id]", Pattern.LITERAL);
    private static final Pattern P_LANGUAGE = Pattern.compile("[language]", Pattern.LITERAL);
    private static final Pattern P_MODULE = Pattern.compile("[module]", Pattern.LITERAL);
    private static final Pattern P_FOLDER = Pattern.compile("[folder]", Pattern.LITERAL);
    private static final Pattern P_ITEM = Pattern.compile("[item]", Pattern.LITERAL);
    private static final Pattern P_STORE = Pattern.compile("[store]", Pattern.LITERAL);

    /**
     * Generates a redirect url targeting the login page.
     *
     * @param guestInfo - the guest info
     * @param target - target item or null if not defined
     * @return String with a redirect url
     * @throws OXException
     */
    public static String getLoginPageRedirectUrl(final GuestInfo guestInfo, final ShareTarget target, final LoginConfiguration loginConfiguration, String message, String messageType,
        String status) throws OXException {
        String loginPageLink = getLoginPageUrl(loginConfiguration);

        // Build URL
        StringBuilder url = new StringBuilder(loginPageLink);

        // Start fragment portion
        url.append('#');
        if (null != guestInfo) {
            url.append("share=").append(urlEncode(guestInfo.getBaseToken()));
            if (AuthenticationMode.ANONYMOUS_PASSWORD == guestInfo.getAuthentication()) {
                url.append('&').append("login_type=anonymous");
            } else {
                url.append('&').append("login_type=guest").append('&').append("login_name=").append(urlEncode(guestInfo.getEmailAddress()));
            }
        }
        if (null != target) {
            url.append('&').append("target=").append(urlEncode(target.getPath()));
        }
        if (!Strings.isEmpty(message) && !Strings.isEmpty(messageType) && !Strings.isEmpty(status)) {
            url.append("&message=").append(message).append("&message_type=").append(messageType).append("&status=").append(status);
        }
        return url.toString();
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
    public static String getWebSessionRedirectURL(Session session, User user, GuestShare share, LoginConfiguration loginConfig) {
        /*
         * prepare url
         */
        StringBuilder stringBuilder = new StringBuilder("[uiwebpath]#!&session=[session]&store=[store]&user=[user]&user_id=[user_id]&context_id=[context_id]");
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
        //        uiWebPath = "/ox6/index.html";
        redirectLink = P_UIWEBPATH.matcher(redirectLink).replaceAll(Matcher.quoteReplacement(ShareRedirectUtils.getLoginPageUrl(uiWebPath)));
        redirectLink = P_SESSION.matcher(redirectLink).replaceAll(Matcher.quoteReplacement(session.getSessionID()));
        redirectLink = P_USER.matcher(redirectLink).replaceAll(Matcher.quoteReplacement(user.getMail()));
        redirectLink = P_USER_ID.matcher(redirectLink).replaceAll(Integer.toString(user.getId()));
        redirectLink = P_CONTEXT_ID.matcher(redirectLink).replaceAll(String.valueOf(session.getContextId()));
        redirectLink = P_LANGUAGE.matcher(redirectLink).replaceAll(Matcher.quoteReplacement(String.valueOf(user.getLocale())));
        if (0 != module) {
            String name = ShareServiceLookup.getService(ModuleSupport.class).getShareModule(module);
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

    public static String getErrorRedirectUrl(String message, String status) {
        ConfigurationService configService = ShareServiceLookup.getService(ConfigurationService.class);
        String uiWebPath = configService.getProperty("com.openexchange.UIWebPath", "/appsuite");
        StringBuilder url = new StringBuilder(getLoginPageUrl(uiWebPath));
        url.append("#&login_type=guest");
        if (!Strings.isEmpty(message) && !Strings.isEmpty(status)) {
            url.append("&message=").append(message).append("&message_type=").append("ERROR").append("&status=").append(status);
        }
        return url.toString();
    }

    /**
     * Returns the url to the login page based on the given information.
     *
     * @param loginConfiguration - login configuration
     * @return String with the url to login
     */
    public static String getLoginPageUrl(final LoginConfiguration loginConfiguration) {
        String uiWebPath = loginConfiguration.getUiWebPath();
        return getLoginPageUrl(uiWebPath);
    }

    /**
     * Returns the url to the login page based on the given information.
     *
     * @param uiWebPath - Path to ui
     * @return String with the url to login
     */
    public static String getLoginPageUrl(String uiWebPath) {
        /*
         * get configured login link
         */
        String loginLink;
        {
            ConfigurationService configService = ShareServiceLookup.getService(ConfigurationService.class);
            loginLink = configService.getProperty("com.openexchange.share.loginLink", "/[uiwebpath]/ui");
        }
        /*
         * replace templates
         */
        loginLink = P_UIWEBPATH.matcher(loginLink).replaceAll(Matcher.quoteReplacement(trimSlashes(uiWebPath)));
        return loginLink;
    }

    /**
     * Translates a string into application/x-www-form-urlencoded format using encoding "ISO-8859-1".
     *
     * @param s - String to encode
     * @return "ISO-8859-1" encoded string
     */
    public static String urlEncode(final String s) {
        try {
            return URLEncoder.encode(s, "ISO-8859-1");
        } catch (final UnsupportedEncodingException e) {
            return s;
        }
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

    public static String translate(String toTranslate, Locale locale) throws OXException {
        TranslatorFactory factory = ShareServiceLookup.getService(TranslatorFactory.class);
        if (null == factory) {
            throw ServiceExceptionCode.absentService(TranslatorFactory.class);
        }
        Translator translator = factory.translatorFor(locale);
        return translator.translate(toTranslate);
    }

}
