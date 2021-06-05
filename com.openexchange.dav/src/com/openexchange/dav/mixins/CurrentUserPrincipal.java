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

package com.openexchange.dav.mixins;

import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.dav.DAVFactory;
import com.openexchange.session.Session;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;

/**
 * {@link CurrentUserPrincipal}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CurrentUserPrincipal extends SingleXMLPropertyMixin {

    private static final String PROPERTY_NAME = "current-user-principal";
    private DAVFactory factory;

    /**
     * Initializes a new {@link CurrentUserPrincipal}.
     *
     * @param factory The factory
     */
    public CurrentUserPrincipal(DAVFactory factory) {
        super(Protocol.DAV_NS.getURI(), PROPERTY_NAME);
        this.factory = factory;
    }

    @Override
    protected String getValue() {
        Session session = factory.getSessionObject();
        if (null == session) {
            return "<D:href><D:unauthenticated/></D:href>";
        }
        return "<D:href>" + PrincipalURL.forUser(session.getUserId(), factory.getService(ConfigViewFactory.class)) + "</D:href>";
    }

}
