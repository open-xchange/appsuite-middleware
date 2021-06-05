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

package com.openexchange.caldav.mixins;

import com.openexchange.caldav.CaldavProtocol;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.dav.mixins.PrincipalURL;
import com.openexchange.dav.resources.FolderCollection;
import com.openexchange.exception.OXException;
import com.openexchange.user.User;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;

/**
 * The {@link Organizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Organizer extends SingleXMLPropertyMixin {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Organizer.class);

    private final FolderCollection<?> collection;

    /**
     * Initializes a new {@link Organizer}.
     *
     * @param collection The collection to get the owner from.
     */
    public Organizer(FolderCollection<?> collection) {
        super(CaldavProtocol.CALENDARSERVER_NS.getURI(), "organizer");
        this.collection = collection;
    }

    @Override
    protected String getValue() {
        User user = null;
        if (null != collection) {
            try {
                user = collection.getOwner();
            } catch (OXException e) {
                LOG.warn("error determining owner from folder collection '{}'", collection.getFolder(), e);
            }
        }
        return null != user ? "<D:href>" + PrincipalURL.forUser(user.getId(), collection.getFactory().getService(ConfigViewFactory.class)) + "</D:href>": null;
    }

}
