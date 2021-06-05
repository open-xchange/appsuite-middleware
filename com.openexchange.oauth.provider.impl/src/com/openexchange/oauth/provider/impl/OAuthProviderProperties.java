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

package com.openexchange.oauth.provider.impl;

import com.openexchange.authentication.NamePart;
import com.openexchange.config.lean.Property;
import com.openexchange.oauth.provider.impl.introspection.OAuthIntrospectionProperty;

/**
 * {@link OAuthProviderProperties}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:sebastian.lutz@open-xchange.com">Sebastian Lutz</a>
 * @since v7.8.0
 */
public enum OAuthProviderProperties implements Property{

    /**
     * Basically enables the OAuth 2.0 provider.
     */
    ENABLED("enabled", Boolean.FALSE),

    /**
     * Configures the current provider mode ({@link OAuthProviderMode}).
     */
    MODE("mode", OAuthProviderMode.AUTH_SEVER.getProviderModeString()),

    /**
     * Specify how authorization codes shall be stored, to enable OAuth in multi-node environments.
     * Options are Hazelcast ('hz') or database ('db').
     */
    AUTHCODE_TYPE("authcode.type", "hz"),

    /**
     * The key to encrypt client secrets that are stored within the database.
     * A value must be set to enable the registration of OAuth 2.0 client applications.
     * It must be the same on every node. After the first client has been registered, the key must not be changed anymore.
     */
    ENCRYPTION_KEY("encryptionKey", ""),

    /**
     * A comma separated list of issuer names (JWT claim "iss") that tokens are accepted from.
     * If this property is empty, tokens are accepted from all issuers.
     */
    ALLOWED_ISSUER("allowedIssuer", ""),

    /**
     * Name of the claim that will be used to resolve a context.
     */
    CONTEXT_LOOKUP_CLAIM("contextLookupClaim", "sub"),

    /**
     * Gets the {@link NamePart} used for determining the context
     * of a user for which a token has been obtained. The part
     * is taken from the value of the according {@link OAuthIntrospectionProperty#CONTEXT_LOOKUP_CLAIM}.
     */
    CONTEXT_LOOKUP_NAME_PART("contextLookupNamePart", NamePart.DOMAIN.getConfigName()),

    /**
     * Name of the claim that will be used to resolve a user.
     */
    USER_LOOKUP_CLAIM("userLookupClaim", "sub"),

    /**
     * Gets the {@link NamePart} used for determining the user for
     * which a token has been obtained. The part is taken from
     * the value of the according {@link OAuthIntrospectionProperty#USER_LOOKUP_CLAIM}.
     */
    USER_LOOKUP_NAME_PART("userLookupNamePart", NamePart.LOCAL_PART.getConfigName()),

    ;

    private final String fqn;
    private final Object defaultValue;

    /**
     * Initializes a new {@link OAuthProviderProperties}.
     * @param suffix
     * @param defaultValue
     */
    private OAuthProviderProperties(String suffix, Object defaultValue) {
        this.fqn = "com.openexchange.oauth.provider." + suffix;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getFQPropertyName() {
        return fqn;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }
}
