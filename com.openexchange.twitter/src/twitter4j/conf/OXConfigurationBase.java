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

package twitter4j.conf;

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

    private final boolean httpUseSSL;

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
        httpUseSSL = true;
    }

    /**
     * Parses given {@link ConfigurationService}.
     *
     * @param configurationService The service
     */
    public void parseFrom(final ConfigurationService configurationService) {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TwitterConfiguration.class);
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
                    log.warn("Not a valid version string: {}. Using fallback \"2.2.3\"", property);
                    clientVersion = "2.2.3";
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
                } catch (NumberFormatException e) {
                    // NAN
                    log.warn("Connection timeout property is not a number: {}. Using fallback 0.",
                        property.trim());
                    httpProxyPort = 0;
                }
            }
        }
        {
            String property = configurationService.getProperty("com.openexchange.twitter.http.connectionTimeout");
            if (null != property && (property = property.trim()).length() > 0) {
                try {
                    httpConnectionTimeout = Integer.parseInt(property.trim());
                } catch (NumberFormatException e) {
                    // NAN
                    log.warn("Connection timeout property is not a number: {}. Using fallback 20000.",
                        property.trim());
                    httpConnectionTimeout = 20000;
                }
            }
        }
        {
            String property = configurationService.getProperty("com.openexchange.twitter.http.readTimeout");
            if (null != property && (property = property.trim()).length() > 0) {
                try {
                    httpReadTimeout = Integer.parseInt(property.trim());
                } catch (NumberFormatException e) {
                    // NAN
                    log.warn("Read timeout property is not a number: {}. Using fallback 120000.", property.trim());
                    httpReadTimeout = 120000;
                }
            }
        }
        {
            String property = configurationService.getProperty("com.openexchange.twitter.http.retryCount");
            if (null != property && (property = property.trim()).length() > 0) {
                try {
                    httpRetryCount = Integer.parseInt(property.trim());
                } catch (NumberFormatException e) {
                    // NAN
                    log.warn("Retry count property is not a number: {}. Using fallback 3.", property.trim());
                    httpRetryCount = 3;
                }
            }
        }
        {
            String property = configurationService.getProperty("com.openexchange.twitter.http.retryIntervalSecs");
            if (null != property && (property = property.trim()).length() > 0) {
                try {
                    httpRetryIntervalSecs = Integer.parseInt(property.trim());
                } catch (NumberFormatException e) {
                    // NAN
                    log.warn("Retry Interval Seconds property is not a number: {}. Using fallback 10.",
                        property.trim());
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
