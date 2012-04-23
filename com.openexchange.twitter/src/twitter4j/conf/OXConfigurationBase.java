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

package twitter4j.conf;

import java.text.MessageFormat;
import java.util.regex.Pattern;
import com.openexchange.config.ConfigurationService;
import com.openexchange.twitter.internal.TwitterConfiguration;

/**
 * {@link OXConfigurationBase}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OXConfigurationBase {

    private static final OXConfigurationBase INSTANCE = new OXConfigurationBase();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static OXConfigurationBase getInstance() {
        return INSTANCE;
    }

    private String clientVersion;

    private boolean httpUseSSL;

    private String httpProxyHost;

    private int httpProxyPort;

    private int httpConnectionTimeout;

    private int httpReadTimeout;

    private int httpRetryCount;

    private int httpRetryIntervalSecs;

    /**
     * Initializes a new {@link OXConfigurationBase}.
     */
    private OXConfigurationBase() {
        super();
    }

    /**
     * Parses given {@link ConfigurationService}.
     *
     * @param configurationService The service
     */
    public void parseFrom(final ConfigurationService configurationService) {
        final org.apache.commons.logging.Log log = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(TwitterConfiguration.class));
        {
            String property = configurationService.getProperty("com.openexchange.twitter.clientVersion");
            if (null == property) {
                clientVersion = "2.2.3";
            } else {
                property = property.trim();
                if (isValidVersionString(property)) {
                    clientVersion = property;
                } else {
                    // Not a valid version
                    log.warn(MessageFormat.format("Not a valid version string: {0}. Using fallback \"2.2.3\"", property));
                    clientVersion = "2.2.3";
                }
            }
        }
        {
            String property = configurationService.getProperty("com.openexchange.twitter.http.useSSL");
            if (null != property && (property = property.trim()).length() > 0) {
                if ("true".equalsIgnoreCase(property)) {
                    httpUseSSL = true;
                } else if ("false".equalsIgnoreCase(property)) {
                    httpUseSSL = false;
                } else {
                    // Not a valid boolean value
                    log.warn(MessageFormat.format("Not a valid flag whether to use SSL over HTTP: {0}. Using fallback \"false\"", property));
                    httpUseSSL = false;
                }
            }
        }
        {
            String property = configurationService.getProperty("com.openexchange.twitter.http.proxyHost");
            if (null != property && (property = property.trim()).length() > 0) {
                httpProxyHost = property.length() == 0 ? null : property;
            }
        }
        {
            String property = configurationService.getProperty("com.openexchange.twitter.http.proxyPort");
            if (null != property && (property = property.trim()).length() > 0) {
                try {
                    httpProxyPort = Integer.parseInt(property.trim());
                } catch (final NumberFormatException e) {
                    // NAN
                    log.warn(MessageFormat.format(
                        "Connection timeout property is not a number: {0}. Using fallback 0.",
                        property.trim()));
                    httpProxyPort = 0;
                }
            }
        }
        {
            String property = configurationService.getProperty("com.openexchange.twitter.http.connectionTimeout");
            if (null != property && (property = property.trim()).length() > 0) {
                try {
                    httpConnectionTimeout = Integer.parseInt(property.trim());
                } catch (final NumberFormatException e) {
                    // NAN
                    log.warn(MessageFormat.format(
                        "Connection timeout property is not a number: {0}. Using fallback 20000.",
                        property.trim()));
                    httpConnectionTimeout = 20000;
                }
            }
        }
        {
            String property = configurationService.getProperty("com.openexchange.twitter.http.readTimeout");
            if (null != property && (property = property.trim()).length() > 0) {
                try {
                    httpReadTimeout = Integer.parseInt(property.trim());
                } catch (final NumberFormatException e) {
                    // NAN
                    log.warn(MessageFormat.format("Read timeout property is not a number: {0}. Using fallback 120000.", property.trim()));
                    httpReadTimeout = 120000;
                }
            }
        }
        {
            String property = configurationService.getProperty("com.openexchange.twitter.http.retryCount");
            if (null != property && (property = property.trim()).length() > 0) {
                try {
                    httpRetryCount = Integer.parseInt(property.trim());
                } catch (final NumberFormatException e) {
                    // NAN
                    log.warn(MessageFormat.format("Retry count property is not a number: {0}. Using fallback 3.", property.trim()));
                    httpRetryCount = 3;
                }
            }
        }
        {
            String property = configurationService.getProperty("com.openexchange.twitter.http.retryIntervalSecs");
            if (null != property && (property = property.trim()).length() > 0) {
                try {
                    httpRetryIntervalSecs = Integer.parseInt(property.trim());
                } catch (final NumberFormatException e) {
                    // NAN
                    log.warn(MessageFormat.format(
                        "Retry Interval Seconds property is not a number: {0}. Using fallback 10.",
                        property.trim()));
                    httpRetryIntervalSecs = 10;
                }
            }
        }
    }

    /**
     * Generates a new configuration with parsed values applied.
     *
     * @return The new configuration with parsed values applied
     */
    public Configuration generateConfiguration() {
        final PropertyConfiguration configuration = (PropertyConfiguration) ConfigurationContext.getInstance();
        applyTo(configuration);
        return configuration;
    }

    /**
     * Applies parsed values to specified configuration.
     *
     * @param configuration The configuration to apply to
     */
    private void applyTo(final PropertyConfiguration configuration) {
        if (null != clientVersion) {
            configuration.setClientVersion(clientVersion);
        }
        configuration.setUseSSL(httpUseSSL);

        configuration.setHttpProxyHost(httpProxyHost);
        configuration.setHttpProxyPort(httpProxyPort);
        configuration.setHttpConnectionTimeout(httpConnectionTimeout);
        configuration.setHttpReadTimeout(httpReadTimeout);
        configuration.setHttpRetryCount(httpRetryCount);
        configuration.setHttpRetryIntervalSeconds(httpRetryIntervalSecs);
        configuration.setOAuthAccessToken(clientVersion);
    }

    /**
     * Generates a new configuration with parsed values applied.
     *
     * @return The new configuration with parsed values applied
     */
    public Configuration generateConfiguration(final String consumerKey, final String consumerSecret) {
        final PropertyConfiguration configuration = (PropertyConfiguration) ConfigurationContext.getInstance();
        applyTo(configuration);
        configuration.setOAuthConsumerKey(consumerKey);
        configuration.setOAuthConsumerSecret(consumerSecret);
        return configuration;
    }

    /**
     * Generates a new configuration with parsed values applied.
     *
     * @return The new configuration with parsed values applied
     */
    public Configuration generateConfiguration(final String consumerKey, final String consumerSecret, final String twitterToken, final String twitterTokenSecret) {
        final PropertyConfiguration configuration = (PropertyConfiguration) ConfigurationContext.getInstance();
        applyTo(configuration);
        configuration.setOAuthConsumerKey(consumerKey);
        configuration.setOAuthConsumerSecret(consumerSecret);
        configuration.setOAuthAccessToken(twitterToken);
        configuration.setOAuthAccessTokenSecret(twitterTokenSecret);
        return configuration;
    }

    private static boolean isValidVersionString(final String version) {
        return version != null && Pattern.compile("[\\d\\w]+([\\.,-][\\d\\w]+)*").matcher(version).matches();
    }
}
