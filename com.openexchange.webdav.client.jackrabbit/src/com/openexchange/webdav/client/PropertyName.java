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

package com.openexchange.webdav.client;

import javax.xml.namespace.QName;

/**
 * {@link PropertyName}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.4
 */
public enum PropertyName {
    ;

    private static final String NS_DAV = "DAV:";
    private static final String NS_PREFIX_DAV = "D";

    public static final QName DAV_CREATIONDATE = new QName(NS_DAV, "creationdate", NS_PREFIX_DAV);
    public static final QName DAV_DISPLAYNAME = new QName(NS_DAV, "displayname", NS_PREFIX_DAV);
    public static final QName DAV_GETCONTENTLANGUAGE = new QName(NS_DAV, "getcontentlanguage", NS_PREFIX_DAV);
    public static final QName DAV_GETCONTENTLENGTH = new QName(NS_DAV, "getcontentlength", NS_PREFIX_DAV);
    public static final QName DAV_GETCONTENTTYPE = new QName(NS_DAV, "getcontenttype", NS_PREFIX_DAV);
    public static final QName DAV_GETETAG = new QName(NS_DAV, "getetag", NS_PREFIX_DAV);
    public static final QName DAV_GETLASTMODIFIED = new QName(NS_DAV, "getlastmodified", NS_PREFIX_DAV);
    public static final QName DAV_RESOURCETYPE = new QName(NS_DAV, "resourcetype", NS_PREFIX_DAV);
    public static final QName DAV_LOCKDISCOVERY = new QName(NS_DAV, "lockdiscovery", NS_PREFIX_DAV);
    public static final QName DAV_SOURCE = new QName(NS_DAV, "source", NS_PREFIX_DAV);
    public static final QName DAV_SUPPORTEDLOCK = new QName(NS_DAV, "supportedlock", NS_PREFIX_DAV);

    public static final QName DAV_QUOTA_AVAILABLE_BYTES = new QName(NS_DAV, "quota-available-bytes", NS_PREFIX_DAV);
    public static final QName DAV_QUOTA_USED_BYTES = new QName(NS_DAV, "quota-used-bytes", NS_PREFIX_DAV);

}
