/*
 * Copyright 2007 AOL, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.openexchange.oauth.server;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.OAuthValidator;
import net.oauth.SimpleOAuthValidator;
import net.oauth.server.OAuthServlet;
import org.apache.commons.codec.digest.DigestUtils;
import com.openexchange.config.ConfigurationService;

/**
 * Utility methods for providers that store consumers, tokens and secrets in local cache (HashSet). Consumer key is used as the name, and
 * its credentials are stored in HashSet.
 * 
 * @author Praveen Alavilli
 */
public class SampleOAuthProvider {

    /**
     * The OAuth validator.
     */
    public static final OAuthValidator VALIDATOR = new SimpleOAuthValidator();

    private static final Map<String, OAuthConsumer> ALL_CONSUMERS = new ConcurrentHashMap<String, OAuthConsumer>(10);

    private static final Map<OAuthAccessor, OAuthAccessor> ALL_TOKENS = new ConcurrentHashMap<OAuthAccessor, OAuthAccessor>();

    private static Properties consumerProperties = null;

    public static synchronized void loadConsumers() throws IOException {
        Properties consumerProperties = SampleOAuthProvider.consumerProperties;
        if (null == consumerProperties) {
            consumerProperties = OAuthProviderServiceLookup.getService(ConfigurationService.class).getFile("oauth-provider.properties");
            SampleOAuthProvider.consumerProperties = consumerProperties;
        }

        // for each entry in the properties file create an OAuthConsumer
        for (final Map.Entry prop : consumerProperties.entrySet()) {
            final String consumer_key = (String) prop.getKey();
            // make sure it's key not additional properties
            if (!consumer_key.contains(".")) {
                final String consumer_secret = (String) prop.getValue();
                if (consumer_secret != null) {
                    final String consumer_description = consumerProperties.getProperty(consumer_key + ".description");
                    final String consumer_callback_url = consumerProperties.getProperty(consumer_key + ".callbackURL");
                    // Create OAuthConsumer w/ key and secret
                    final OAuthConsumer consumer = new OAuthConsumer(consumer_callback_url, consumer_key, consumer_secret, null);
                    consumer.setProperty("name", consumer_key);
                    consumer.setProperty("description", consumer_description);
                    ALL_CONSUMERS.put(consumer_key, consumer);
                }
            }
        }
    }

    public static synchronized OAuthConsumer getConsumer(final OAuthMessage requestMessage) throws IOException, OAuthProblemException {

        OAuthConsumer consumer = null;
        // try to load from local cache if not throw exception
        final String consumer_key = requestMessage.getConsumerKey();

        consumer = SampleOAuthProvider.ALL_CONSUMERS.get(consumer_key);

        if (consumer == null) {
            final OAuthProblemException problem = new OAuthProblemException("token_rejected");
            throw problem;
        }

        return consumer;
    }

    /**
     * Get the access token and token secret for the given oauth_token.
     */
    public static synchronized OAuthAccessor getAccessor(final OAuthMessage requestMessage) throws IOException, OAuthProblemException {

        // try to load from local cache if not throw exception
        final String consumer_token = requestMessage.getToken();
        OAuthAccessor accessor = null;
        for (final OAuthAccessor a : SampleOAuthProvider.ALL_TOKENS.keySet()) {
            if (a.requestToken != null) {
                if (a.requestToken.equals(consumer_token)) {
                    accessor = a;
                    break;
                }
            } else if (a.accessToken != null) {
                if (a.accessToken.equals(consumer_token)) {
                    accessor = a;
                    break;
                }
            }
        }

        if (accessor == null) {
            final OAuthProblemException problem = new OAuthProblemException("token_expired");
            throw problem;
        }

        return accessor;
    }

    /**
     * Set the access token
     */
    public static synchronized void markAsAuthorized(final OAuthAccessor accessor, final String userId) throws OAuthException {

        // first remove the accessor from cache
        ALL_TOKENS.remove(accessor);

        accessor.setProperty("user", userId);
        accessor.setProperty("authorized", Boolean.TRUE);

        // update token in local cache
        ALL_TOKENS.put(accessor, accessor);
    }

    /**
     * Generate a fresh request token and secret for a consumer.
     * 
     * @throws OAuthException
     */
    public static synchronized void generateRequestToken(final OAuthAccessor accessor) throws OAuthException {

        // generate oauth_token and oauth_secret
        final String consumer_key = (String) accessor.consumer.getProperty("name");
        // generate token and secret based on consumer_key

        // for now use md5 of name + current time as token
        final String token_data = consumer_key + System.nanoTime();
        final String token = DigestUtils.md5Hex(token_data);
        // for now use md5 of name + current time + token as secret
        final String secret_data = consumer_key + System.nanoTime() + token;
        final String secret = DigestUtils.md5Hex(secret_data);

        accessor.requestToken = token;
        accessor.tokenSecret = secret;
        accessor.accessToken = null;

        // add to the local cache
        ALL_TOKENS.put(accessor, accessor);

    }

    /**
     * Generate a fresh request token and secret for a consumer.
     * 
     * @throws OAuthException
     */
    public static synchronized void generateAccessToken(final OAuthAccessor accessor) throws OAuthException {

        // generate oauth_token and oauth_secret
        final String consumer_key = (String) accessor.consumer.getProperty("name");
        // generate token and secret based on consumer_key

        // for now use md5 of name + current time as token
        final String token_data = consumer_key + System.nanoTime();
        final String token = DigestUtils.md5Hex(token_data);
        // first remove the accessor from cache
        ALL_TOKENS.remove(accessor);

        accessor.requestToken = null;
        accessor.accessToken = token;

        // update token in local cache
        ALL_TOKENS.put(accessor, accessor);
    }

    public static void handleException(final Exception e, final HttpServletRequest request, final HttpServletResponse response, final boolean sendBody) throws IOException, ServletException {
        String realm = (request.isSecure()) ? "https://" : "http://";
        realm += request.getLocalName();
        OAuthServlet.handleException(response, e, realm, sendBody);
    }

}
