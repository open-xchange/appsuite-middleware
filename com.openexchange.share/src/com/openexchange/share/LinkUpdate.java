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

package com.openexchange.share;

import java.util.Date;

/**
 * {@link LinkUpdate}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class LinkUpdate {

    private Date expiryDate;
    private String password;
    private boolean includeSubfolders;
    private boolean containsExpiryDate;
    private boolean containsPassword;
    private boolean containsIncludeSubfolders;

    /**
     * Initializes a new {@link LinkUpdate}.
     */
    public LinkUpdate() {
        super();
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
        containsExpiryDate = true;
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
        containsPassword = true;
    }

    /**
     * Gets the includeSubfolders
     *
     * @return The includeSubfolders
     */
    public boolean isIncludeSubfolders() {
        return includeSubfolders;
    }

    /**
     * Sets the includeSubfolders
     *
     * @param includeSubfolders Whether sub-folders should be included or not
     */
    public void setIncludeSubfolders(boolean includeSubfolders) {
        this.includeSubfolders = includeSubfolders;
        containsIncludeSubfolders = true;
    }

    /**
     * Gets the containsExpiryDate
     *
     * @return The containsExpiryDate
     */
    public boolean containsExpiryDate() {
        return containsExpiryDate;
    }

    /**
     * Gets the containsPassword
     *
     * @return The containsPassword
     */
    public boolean containsPassword() {
        return containsPassword;
    }

    /**
     * Gets a value whether the include subfolders flag has been set or not.
     *
     * @return <code>true</code> if the include subfolders flag is set, <code>false</code>, otherwise
     */
    public boolean containsIncludeSubfolders() {
        return containsIncludeSubfolders;
    }

}
