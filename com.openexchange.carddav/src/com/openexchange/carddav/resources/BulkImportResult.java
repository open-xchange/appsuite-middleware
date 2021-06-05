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

package com.openexchange.carddav.resources;

import com.openexchange.exception.OXException;
import com.openexchange.webdav.protocol.WebdavPath;

/**
 * {@link BulkImportResult}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class BulkImportResult {

    private OXException error;
    private String uid;
    private WebdavPath href;

    /**
     * Initializes a new {@link BulkImportResult}.
     */
    public BulkImportResult() {
        super();
    }

    /**
     * Gets the error
     *
     * @return The error
     */
    public OXException getError() {
        return error;
    }

    /**
     * Sets the error
     *
     * @param error The error to set
     */
    public void setError(OXException error) {
        this.error = error;
    }

    /**
     * Gets the uid
     *
     * @return The uid
     */
    public String getUid() {
        return uid;
    }

    /**
     * Sets the uid
     *
     * @param uid The uid to set
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * Gets the href
     *
     * @return The href
     */
    public WebdavPath getHref() {
        return href;
    }

    /**
     * Sets the href
     *
     * @param href The href to set
     */
    public void setHref(WebdavPath href) {
        this.href = href;
    }

}
