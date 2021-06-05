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

package com.openexchange.share.subscription;

import static org.slf4j.LoggerFactory.getLogger;
import java.net.URI;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.session.Session;

/**
 * {@link XctxHostData}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public abstract class XctxHostData implements HostData {

    private final URI shareUri;
    private final Session guestSession;
    private final HostData guestHostData;

    /**
     * Initializes a new {@link XctxHostData}.
     * 
     * @param shareUri The share URI
     * @param guestSession The guest session, or <code>null</code> if not available
     */
    public XctxHostData(URI shareUri, Session guestSession) {
        super();
        this.shareUri = shareUri;
        this.guestSession = guestSession;
        this.guestHostData = null != guestSession ? (HostData) guestSession.getParameter(HostnameService.PARAM_HOST_DATA) : null;
    }

    /**
     * Gets the dispatcher prefix service.
     * 
     * @return The dispatcher prefix service
     */
    protected abstract DispatcherPrefixService getDispatcherPrefixService() throws OXException;

    @Override
    public String getHTTPSession() {
        return null != guestHostData ? guestHostData.getHTTPSession() : null;
    }

    @Override
    public String getRoute() {
        return null != guestHostData ? guestHostData.getRoute() : null;
    }

    @Override
    public String getHost() {
        return shareUri.getHost();
    }

    @Override
    public int getPort() {
        return shareUri.getPort();
    }

    @Override
    public boolean isSecure() {
        return null != shareUri.getScheme() && shareUri.getScheme().startsWith("https");
    }

    @Override
    public String getDispatcherPrefix() {
        String path = shareUri.getPath();
        if (null != path) {
            int idx = path.indexOf("/share");
            if (-1 != idx) {
                return path.substring(0, idx + 1);
            }
        }
        HostData guestHostData = null != guestSession ? (HostData) guestSession.getParameter(HostnameService.PARAM_HOST_DATA) : null;
        if (null != guestHostData) {
            return guestHostData.getDispatcherPrefix();
        }
        try {
            return getDispatcherPrefixService().getPrefix();
        } catch (OXException e) {
            getLogger(XctxHostData.class).warn("Error getting dispatcher prefix, falling back to \"/ajax/\".", e);
            return "/ajax/";
        }
    }

}
