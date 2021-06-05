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

import com.openexchange.dav.resources.FolderCollection;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;

/**
 * {@link CTag}
 *
 * getctag xmlns="http://calendarserver.org/ns/"
 *
 * The "calendar-ctag" is like a resource "etag"; it changes when anything in the calendar has changed. This allows the client to quickly
 * determine that it does not need to synchronize any changed events.
 * @see <a href="https://trac.calendarserver.org/browser/CalendarServer/trunk/doc/Extensions/caldav-ctag.txt">caldav-ctag-02</a>
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 *
 */
public class CTag extends SingleXMLPropertyMixin {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CTag.class);

    private final FolderCollection<?> collection;
    private String value = null;

    /**
     * Initializes a new {@link CTag}.
     *
     * @param collection The parent collection
     */
    public CTag(FolderCollection<?> collection) {
        super("http://calendarserver.org/ns/", "getctag");
        this.collection = collection;
    }

    @Override
    protected String getValue() {
        if (null == value && null != collection) {
            try {
                value = collection.getCTag();
            } catch (WebdavProtocolException e) {
                LOG.error("error determining ctag from collection", e);
            }
        }
        return value;
    }
}
