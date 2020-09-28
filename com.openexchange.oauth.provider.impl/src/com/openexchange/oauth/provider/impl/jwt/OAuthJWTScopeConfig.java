package com.openexchange.oauth.provider.impl.jwt;

import java.util.Collections;
import java.util.Map;
import com.openexchange.config.lean.DefaultProperty;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.config.lean.Property;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.impl.osgi.Services;

public class OAuthJWTScopeConfig {    
    private static final Property SCOPE_PREFIX = DefaultProperty.valueOf("com.openexchange.oauth.provider.scope.prefix", "com.openexchange.oauth.provider.scope.");
    
    /**
     * 
     * Prefix used to resolve external authorization 
     * server scopes to internal MW scopes. 
     *
     * @return The configured prefix or its default value
     * @throws OXException
     */
    public static String getScopePrefix() throws OXException {
        LeanConfigurationService service = Services.requireService(LeanConfigurationService.class);
        if (service == null) {
            return SCOPE_PREFIX.getDefaultValue().toString();
        }
        return service.getProperty(SCOPE_PREFIX);
    }
    
    /**
     * 
     * Considering the configured prefix, all internally configured scopes are determined and returned.
     *
     * @return Scopes matching the prefix
     * @throws OXException
     */
    public static Map<String, String> getInternalScopes() throws OXException {
        LeanConfigurationService service = Services.requireService(LeanConfigurationService.class);
        if (service == null) {
            return Collections.emptyMap();
        }
        String scopePrefix = service.getProperty(SCOPE_PREFIX);
        
        return service.getProperties((k,v) -> k.startsWith(scopePrefix));
    }
}
