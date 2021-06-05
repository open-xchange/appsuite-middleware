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

package com.openexchange.oauth.impl;

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.SSLHandshakeException;
import javax.xml.ws.handler.MessageContext.Scope;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Request;
import org.scribe.model.RequestTuner;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.exception.ExceptionUtils;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.net.ssl.exception.SSLExceptionCode;
import com.openexchange.oauth.API;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.OAuthConfigurationProperty;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.impl.services.Services;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link AbstractScribeAwareOAuthServiceMetaData}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public abstract class AbstractScribeAwareOAuthServiceMetaData extends AbstractOAuthServiceMetaData implements ScribeAware, OAuthIdentityAware, Reloadable {

    private static final String DEFAULT_CONTENT_TYPE = "application/json";
    protected static final String EMPTY_CONTENT_TYPE = " ";

    protected final ServiceLookup services;
    private final List<OAuthPropertyID> propertyNames;
    private static final String PROP_PREFIX = "com.openexchange.oauth";
    private final API api;

    /**
     * Initialises a new {@link AbstractScribeAwareOAuthServiceMetaData}.
     *
     * @param services the service lookup instance
     * @param api The {@link KnownApi}
     * @param scopes The {@link Scope}s
     */
    public AbstractScribeAwareOAuthServiceMetaData(final ServiceLookup services, API api, OAuthScope... scopes) {
        super(scopes);
        this.services = services;
        this.api = api;

        setId(api.getServiceId());
        setDisplayName(api.getDisplayName());

        // Common properties for all OAuthServiceMetaData implementations.
        propertyNames = new ArrayList<>();
        propertyNames.add(OAuthPropertyID.apiKey);
        propertyNames.add(OAuthPropertyID.apiSecret);

        // Add the extra properties (if any)
        propertyNames.addAll(getExtraPropertyNames());

        // Load configuration
        loadConfiguration();
    }

    /**
     * Load the configuration.
     */
    protected void loadConfiguration() {
        ConfigurationService configService = services.getService(ConfigurationService.class);
        if (null == configService) {
            throw new IllegalStateException("Missing configuration service");
        }
        reloadConfiguration(configService);
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        StringBuilder builder = new StringBuilder();
        for (OAuthPropertyID prop : propertyNames) {
            String propName = builder.append(PROP_PREFIX).append(".").append(getPropertyId()).append(".").append(prop).toString();
            String propValue = configService.getProperty(propName);
            if (Strings.isEmpty(propValue)) {
                throw new IllegalStateException("Missing following property in configuration: " + propName);
            }
            addOAuthProperty(prop, new OAuthConfigurationProperty(propName, propValue));
            builder.setLength(0);
        }

        // Basic URL encoding
        OAuthConfigurationProperty redirectUrl = getOAuthProperty(OAuthPropertyID.redirectUrl);
        if (redirectUrl != null) {
            String r = urlEncode(redirectUrl.getValue());
            addOAuthProperty(OAuthPropertyID.redirectUrl, new OAuthConfigurationProperty(redirectUrl.getName(), r));
        }
    }

    @Override
    public Interests getInterests() {
        return Reloadables.interestsForProperties(getConfigurationPropertyNames());
    }

    @Override
    public API getAPI() {
        return api;
    }

    @Override
    public String getUserIdentity(Session session, int accountId, String accessToken, String accessSecret) throws OXException {
        // Contact the OAuth provider and fetch the identity of the current logged in user
        OAuthService scribeService = new ServiceBuilder().provider(getScribeService()).apiKey(getAPIKey(session)).apiSecret(getAPISecret(session)).build();
        OAuthRequest request = new OAuthRequest(getIdentityHTTPMethod(), getIdentityURL(accessToken));
        request.addHeader("Content-Type", getContentType());
        scribeService.signRequest(new Token(accessToken, accessSecret), request);
        Response response = execute(request);

        int responseCode = response.getCode();
        String body = response.getBody();
        if (responseCode == 403) {
            throw OAuthExceptionCodes.OAUTH_ACCESS_TOKEN_INVALID.create(getId(), I(accountId), I(session.getUserId()), I(session.getContextId()));
        }
        if (responseCode >= 400 && responseCode <= 499) {
            throw OAuthExceptionCodes.DENIED_BY_PROVIDER.create(body);
        }

        final Matcher matcher = compileIdentityPattern(getIdentityFieldName()).matcher(body);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw OAuthExceptionCodes.CANNOT_GET_USER_IDENTITY.create(getId());
    }

    @Override
    public String getContentType() {
        return DEFAULT_CONTENT_TYPE;
    }

    /**
     * Compiles the pattern that will be used to extract the identity of the user
     * from the identity response.
     *
     * @param fieldName The field name that contains the identity of the user in the identity response
     * @return The compiled {@link Pattern}
     */
    private Pattern compileIdentityPattern(String fieldName) {
        return Pattern.compile("\"" + fieldName + "\":\\s*\"(\\S*?)\"");
    }

    /**
     * Executes specified request and returns its response.
     *
     * @param request The request
     * @return The response
     * @throws OXException If executing request fails
     */
    private Response execute(OAuthRequest request) throws OXException {
        try {
            return request.send(RequestTunerExtension.getInstance());
        } catch (org.scribe.exceptions.OAuthException e) {
            // Handle Scribe's org.scribe.exceptions.OAuthException (inherits from RuntimeException)
            if (ExceptionUtils.isEitherOf(e, SSLHandshakeException.class)) {
                List<Object> displayArgs = new ArrayList<>(2);
                displayArgs.add(SSLExceptionCode.extractArgument(e, "fingerprint"));
                displayArgs.add(getIdentityURL("** obfuscated **"));
                throw SSLExceptionCode.UNTRUSTED_CERTIFICATE.create(e, displayArgs.toArray(new Object[] {}));
            }

            Throwable cause = e.getCause();
            if (cause instanceof java.net.SocketTimeoutException) {
                // A socket timeout
                throw OAuthExceptionCodes.CONNECT_ERROR.create(cause, new Object[0]);
            }

            throw OAuthExceptionCodes.OAUTH_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Get the property identifier
     *
     * @return the property identifier
     */
    protected abstract String getPropertyId();

    /**
     * Get the extra property names
     *
     * @return A collection with extra property names
     */
    protected abstract Collection<OAuthPropertyID> getExtraPropertyNames();

    /**
     * {@link RequestTunerExtension}
     */
    private static final class RequestTunerExtension extends RequestTuner {

        private static final RequestTunerExtension INSTANCE = new RequestTunerExtension();

        /**
         * Returns the instance of the {@link RequestTuner}
         *
         * @return the instance of the {@link RequestTuner}
         */
        static final RequestTuner getInstance() {
            return INSTANCE;
        }

        /**
         * Initialises a new {@link AbstractScribeAwareOAuthServiceMetaData.RequestTunerExtension}.
         */
        public RequestTunerExtension() {
            super();
        }

        @Override
        public void tune(Request request) {
            request.setConnectTimeout(5, TimeUnit.SECONDS);
            request.setReadTimeout(30, TimeUnit.SECONDS);

            SSLSocketFactoryProvider factoryProvider = Services.optService(SSLSocketFactoryProvider.class);
            if (null != factoryProvider) {
                request.setSSLSocketFactory(factoryProvider.getDefault());
            }
        }
    }
}
