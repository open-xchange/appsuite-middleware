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

package com.openexchange.ajax.drive.action;

import java.util.Date;
import java.util.Map;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;

/**
 * {@link GetLinkResponse}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.8.0
 */
public class GetLinkResponse extends AbstractAJAXResponse {

    private String url;
    private boolean isNew;
    private String password;
    private Date expiryDate;
    private Map<String, Object> meta;

    /**
     * Initializes a new {@link GetLinkResponse}.
     *
     * @param response
     */
    protected GetLinkResponse(Response response) {
        super(response);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Gets the isNew
     *
     * @return The isNew
     */
    public boolean isNew() {
        return isNew;
    }

    /**
     * Sets the isNew
     *
     * @param isNew The isNew to set
     */
    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    /**
     * Gets the password
     *
     * @return The password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password
     *
     * @param password The password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the expiryDate
     *
     * @return The expiryDate
     */
    public Date getExpiryDate() {
        return expiryDate;
    }

    /**
     * Sets the expiryDate
     *
     * @param expiryDate The expiryDate to set
     */
    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    /**
     * Gets the meta
     *
     * @return The meta
     */
    public Map<String, Object> getMeta() {
        return meta;
    }

    /**
     * Sets the meta
     *
     * @param meta The meta to set
     */
    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }

}
