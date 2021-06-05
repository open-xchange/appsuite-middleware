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

package com.openexchange.subscribe.dav.gmx.de;

import java.net.URI;
import java.net.URISyntaxException;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.subscribe.dav.AbstractCardDAVSubscribeService;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link GmxDeSubscribeService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class GmxDeSubscribeService extends AbstractCardDAVSubscribeService {

    /**
     * Initializes a new {@link GmxDeSubscribeService}.
     *
     * @param services The {@link ServiceLookup}
     * @throws OXException
     */
    public GmxDeSubscribeService(ServiceLookup services) throws OXException {
        super(services);
    }

    @Override
    protected URI getBaseUrl(ServerSession session) {
        try {
            return new URI("https://carddav.gmx.net/CardDavProxy/carddav");
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected URI getUserPrincipal(ServerSession session) {
        try {
            return new URI("https://carddav.gmx.net/CardDavProxy/carddav/current-user-principal-uri");
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected int getChunkSize(ServerSession session) {
        return 25;
    }

    @Override
    protected String getDisplayName() {
        return "GMX.DE";
    }

    @Override
    protected String getId() {
        return "com.openexchange.subscribe.dav.gmx.de";
    }

}
