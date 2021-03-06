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

import com.openexchange.dav.resources.DAVCollection;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;


/**
 * {@link SyncToken}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class SyncToken extends SingleXMLPropertyMixin {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SyncToken.class);

    private String value = null;
    private final DAVCollection collection;

    /**
     * Initializes a new {@link SyncToken}.
     *
     * @param collection The parent collection
     */
    public SyncToken(DAVCollection collection) {
        super(Protocol.DAV_NS.getURI(), "sync-token");
        this.collection = collection;
    }

    @Override
    protected String getValue() {
        if (null == value && null != collection) {
            try {
                value = collection.getSyncToken();
            } catch (WebdavProtocolException e) {
                LOG.error("error determining sync-token from collection", e);
            }
        }
        return value;
    }

}
