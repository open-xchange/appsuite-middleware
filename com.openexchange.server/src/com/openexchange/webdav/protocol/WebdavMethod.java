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

package com.openexchange.webdav.protocol;

/**
 * {@link WebdavMethod}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public enum WebdavMethod {

    GET(true),
    PUT(false),
    MKCOL(false),
    DELETE(false),
    HEAD(true),
    OPTIONS(true),
    TRACE(true),
    PROPPATCH(false),
    PROPFIND(true),
    MOVE(false),
    COPY(false),
    LOCK(false),
    UNLOCK(false),
    REPORT(true),
    ACL(false),
    MKCALENDAR(false),
    POST(false),
    ;

    private boolean readOnly;

    private WebdavMethod(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Gets a value indicating whether the supplied method is <i>read-only</i>, i.e. no changes will be performed on the server when
     * being executed or not.
     * 
     * @return <code>true</code> if the method is considered as <i>read-only</i>, <code>false</code>, otherwise
     */
    public boolean isReadOnly() {
        return readOnly;
    }

}

