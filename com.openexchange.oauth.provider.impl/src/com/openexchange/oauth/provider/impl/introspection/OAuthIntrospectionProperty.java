package com.openexchange.oauth.provider.impl.introspection;

import com.openexchange.authentication.NamePart;
import com.openexchange.config.lean.Property;

public enum OAuthIntrospectionProperty implements Property{
    
    /**
     * The token introspection endpoint.
     */
    ENDPOINT("endpoint", ""),
    
    /**
     * Enable basic authentication for introspection
     */
    BASIC_AUTH_ENABLED("basicAuthEnabled", Boolean.TRUE),
    
    /**
     * ID of the OAuth client.
     */
    CLIENT_ID("clientID", ""),
    
    /**
     * Secret of the OAuth client.
     */
    CLIENT_SECRET("clientSecret", ""),
        
    /**
     * Name of the claim that will be used to resolve a context.
     */
    CONTEXT_LOOKUP_CLAIM("contextLookupClaim", "sub"),

    /**
     * Gets the {@link NamePart} used for determining the context
     * of a user for which a token has been obtained. The part
     * is taken from the value of the according {@link LookupSource}.
     */
    CONTEXT_LOOKUP_NAME_PART("contextLookupNamePart", NamePart.DOMAIN.getConfigName()),

    /**
     * Name of the claim that will be used to resolve a user.
     */
    USER_LOOKUP_CLAIM("userLookupClaim", "sub"),

    /**
     * Gets the {@link NamePart} used for determining the user for
     * which a token has been obtained. The part is taken from
     * the value of the according {@link LookupSource}.
     */
    USER_LOOKUP_NAME_PART("userLookupNamePart", NamePart.LOCAL_PART.getConfigName());

    public static final String PREFIX = "com.openexchange.oauth.provider.introspection";
    private final String fqn;
    private final Object defaultValue;

    private OAuthIntrospectionProperty(String suffix, Object defaultValue) {
        this.fqn =  PREFIX + suffix;
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
