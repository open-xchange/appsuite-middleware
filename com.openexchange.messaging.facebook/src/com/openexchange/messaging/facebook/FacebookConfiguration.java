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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.messaging.facebook;

import java.text.MessageFormat;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import com.openexchange.config.ConfigurationService;

/**
 * {@link FacebookConfiguration}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public final class FacebookConfiguration {

    private static final FacebookConfiguration INSTANCE = new FacebookConfiguration();

    /**
     * Gets the {@link FacebookConfiguration instance}.
     *
     * @return The instance
     */
    public static FacebookConfiguration getInstance() {
        return INSTANCE;
    }

    /*-
     * ----------------------------------------------------------------------------------
     * --------------------------------- MEMBER SECTION ---------------------------------
     * ----------------------------------------------------------------------------------
     */

    private String loginPageBaseURL;

    private String actionOfLoginForm;

    private String apiKey;

    private String secretKey;

    private String apiVersion;

    private String nameOfUserField;

    private String nameOfPasswordField;

    private Pattern linkAfterLoginPattern;

    /**
     * Initializes a new {@link FacebookConfiguration}.
     */
    private FacebookConfiguration() {
        super();
        reset();
    }

    private void reset() {
        loginPageBaseURL = "http://www.facebook.com/login.php";
        actionOfLoginForm = "https://login.facebook.com/login.php?login_attempt=1";
        apiKey = FacebookConstants.KEY_API;
        secretKey = FacebookConstants.KEY_SECRET;
        apiVersion = "1.0";
        nameOfUserField = "email";
        nameOfPasswordField = "pass";
        linkAfterLoginPattern = Pattern.compile("(http://www.facebook.com/inbox/\\\\?ref=[a-z]*)");
    }

    /**
     * Configures this {@link FacebookConfiguration instance} using given {@link ConfigurationService configuration service}.
     *
     * @param configurationService The configuration service
     */
    public void configure(final ConfigurationService configurationService) {
        {
            loginPageBaseURL =
                configurationService.getProperty(
                    "com.openexchange.messaging.facebook.loginPageBaseURL",
                    "http://www.facebook.com/login.php").trim();
        }
        {
            actionOfLoginForm =
                configurationService.getProperty(
                    "com.openexchange.messaging.facebook.actionOfLoginForm",
                    "https://login.facebook.com/login.php?login_attempt=1");
        }
        {
            apiKey = configurationService.getProperty("com.openexchange.messaging.facebook.apiKey", FacebookConstants.KEY_API).trim();
        }
        {
            secretKey =
                configurationService.getProperty("com.openexchange.messaging.facebook.secretKey", FacebookConstants.KEY_SECRET).trim();
        }
        {
            apiVersion = configurationService.getProperty("com.openexchange.messaging.facebook.apiVersion", "1.0").trim();
        }
        {
            nameOfUserField = configurationService.getProperty("com.openexchange.messaging.facebook.nameOfUserField", "email").trim();
        }
        {
            nameOfPasswordField =
                configurationService.getProperty("com.openexchange.messaging.facebook.nameOfPasswordField", "pass").trim();
        }
        {
            String pattern =
                configurationService.getProperty(
                    "com.openexchange.messaging.facebook.linkAfterLogin",
                    "\"\\Qhttp://www.facebook.com/profile.php?ref=profile&id=\\E([0-9]+)\"").trim();
            /*
             * Remove surrounding quotes
             */
            if (pattern.length() > 1 && pattern.charAt(0) == '"') {
                pattern = pattern.substring(1);
            }
            if (pattern.length() > 1 && pattern.charAt(pattern.length() - 1) == '"') {
                pattern = pattern.substring(0, pattern.length() - 1);
            }
            /*
             * Compile pattern
             */
            try {
                linkAfterLoginPattern = Pattern.compile(pattern);
            } catch (final PatternSyntaxException e) {
                final String fallback = "(http://www.facebook.com/inbox/\\\\?ref=[a-z]*)";
                com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(FacebookConfiguration.class)).error(
                    MessageFormat.format(
                        "Illegal regular expression for property ''com.openexchange.messaging.facebook.linkAfterLogin'': \"{0}.\". Using fallback pattern: \"{1}\"",
                        pattern,
                        fallback));
                linkAfterLoginPattern = Pattern.compile(fallback);
            }
        }
    }

    /**
     * Drops this {@link FacebookConfiguration instance}.
     */
    public void drop() {
        reset();
    }

    /**
     * Gets the login page base URL; e.g. <i>http://www.facebook.com/login.php</i>.
     *
     * @return The login page base URL
     */
    public String getLoginPageBaseURL() {
        return loginPageBaseURL;
    }

    /**
     * Gets the API key.
     *
     * @return The API key
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Gets the secret key.
     *
     * @return The secret key
     */
    public String getSecretKey() {
        return secretKey;
    }

    /**
     * Gets the API version.
     *
     * @return The API version
     */
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * Gets the name of the user field.
     *
     * @return The name of the user field
     */
    public String getNameOfUserField() {
        return nameOfUserField;
    }

    /**
     * Gets the name of the password field.
     *
     * @return The name of the password field
     */
    public String getNameOfPasswordField() {
        return nameOfPasswordField;
    }

    /**
     * Gets the pattern to check the link of the expected page after a successful login to facebook.
     *
     * @return The pattern
     */
    public Pattern getLinkAfterLoginPattern() {
        return linkAfterLoginPattern;
    }

    /**
     * Gets the action of the login form; e.g. <i>https://login.facebook.com/login.php?login_attempt=1</i>.
     *
     * @return The action of the login form
     */
    public String getActionOfLoginForm() {
        return actionOfLoginForm;
    }

}
