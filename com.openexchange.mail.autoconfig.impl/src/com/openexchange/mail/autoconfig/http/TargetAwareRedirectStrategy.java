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

package com.openexchange.mail.autoconfig.http;

import static com.openexchange.mail.autoconfig.tools.Utils.OX_TARGET_ID;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.java.InetAddresses;

/**
 * {@link TargetAwareRedirectStrategy}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public class TargetAwareRedirectStrategy extends DefaultRedirectStrategy {
    
    public static final TargetAwareRedirectStrategy TARGET_STRATEGY_INSTANCE = new TargetAwareRedirectStrategy();
    

    private static final Logger LOGGER = LoggerFactory.getLogger(TargetAwareRedirectStrategy.class);

    private TargetAwareRedirectStrategy() {}

    @Override
    public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context) throws ProtocolException {
        /*
         * Get location and check if it is internal, if so check that internal requests are allowed
         */
        final Header locationHeader = response.getFirstHeader("location");
        final String location = locationHeader.getValue();
        try {
            URI locationURI = super.createLocationURI(location);
            InetAddress inetAddress = InetAddress.getByName(locationURI.getHost());
            if (false == InetAddresses.isInternalAddress(inetAddress) || isLocalAddressAllowed(context)) {
                /*
                 * Either not an internal address, or the original target was an internal address which is fine to send
                 * a request to. So keep on processing.
                 */
                return super.getRedirect(request, response, context);
            }
        } catch (UnknownHostException e) {
            throw new ProtocolException("Invalid redirect URI: " + location + ". Unknown host.", e);
        }
        throw new ProtocolException("Invalid redirect URI: " + location + ". No redirect to local address allowed.");
    }

    private boolean isLocalAddressAllowed(HttpContext httpContext) {
        if (null != httpContext.getAttribute(OX_TARGET_ID)) {
            Object attribute = httpContext.getAttribute(OX_TARGET_ID);
            if (false == URL.class.isAssignableFrom(attribute.getClass())) {
                return false;
            }
            URL url = (URL) attribute;

            try {
                InetAddress inetAddress = InetAddress.getByName(url.getHost());
                return InetAddresses.isInternalAddress(inetAddress);
            } catch (UnknownHostException e) {
                // IP address of that host could not be determined
                LOGGER.warn("Unknown host: {}. Skipping config server source for mail auto-config", url.getHost(), e);
            }
        }
        return false;
    }
}
