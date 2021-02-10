
package com.openexchange.rest.client.httpclient.internal;

import com.openexchange.annotation.NonNull;
import com.openexchange.rest.client.httpclient.AbstractHttpClientModifer;
import com.openexchange.rest.client.httpclient.WildcardHttpClientConfigProvider;

/**
 * 
 * {@link LeftoverClientConfigProvider} - Provider for all HTTP clients that don't have specific properties
 * or a specific configuration that must be applied.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
class LeftoverClientConfigProvider extends AbstractHttpClientModifer implements WildcardHttpClientConfigProvider {

    public LeftoverClientConfigProvider() {
        super(DEFAULT_UA);
    }

    @Override
    @NonNull
    public String getClientIdPattern() {
        return "*";
    }
    
    @Override
    @NonNull
    public String getGroupName() {
        /*
         * Fall back to generic HTTP client config with this
         */
        return "";
    }

}
