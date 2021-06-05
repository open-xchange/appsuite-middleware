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

package com.openexchange.rest.client.httpclient.internal;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import org.apache.http.ProtocolException;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import com.openexchange.java.InetAddresses;

/**
 * {@link DenyLocalRedirectStrategy}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a> - Moved with 7.10.4
 * @since v7.6.1
 */
public class DenyLocalRedirectStrategy extends DefaultRedirectStrategy {

    public static final DenyLocalRedirectStrategy DENY_LOCAL_INSTANCE = new DenyLocalRedirectStrategy();

    private DenyLocalRedirectStrategy() {
        super();
    }

    @Override
    protected URI createLocationURI(String location) throws ProtocolException {
        try {
            URI locationURI = super.createLocationURI(location);
            InetAddress inetAddress = InetAddress.getByName(locationURI.getHost());
            if (InetAddresses.isInternalAddress(inetAddress)) {
                throw new ProtocolException("Invalid redirect URI: " + location + ". No redirect to local address allowed.");
            }
            return locationURI;
        } catch (UnknownHostException e) {
            throw new ProtocolException("Invalid redirect URI: " + location + ". Unknown host.", e);
        }
    }
}
