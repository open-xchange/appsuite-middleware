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

package com.openexchange.groupware;

import java.io.Serializable;
import java.util.Date;

/**
 * {@link LinkEntityInfo}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public class LinkEntityInfo extends EntityInfo implements Serializable, Cloneable {

    private static final long serialVersionUID = 1907071021467621978L;

    private final String shareUrl;
    private final String password;
    private final Date expiryDate;
    private final boolean includeSubfolders;

    /**
     * Initializes a new {@link LinkEntityInfo}.
     * 
     * @param entityInfo The entity info to use for initialization
     * @param shareUrl The URL of the share link
     * @param password The optional password for the share link
     * @param expiryDate The optional expiration date for the share link
     * @param includeSubfolders A value indicating whether subfolders are included in the share or not
     */
    public LinkEntityInfo(EntityInfo entityInfo, String shareUrl, String password, Date expiryDate, boolean includeSubfolders) {
        super(entityInfo);
        this.shareUrl = shareUrl;
        this.password = password;
        this.expiryDate = expiryDate;
        this.includeSubfolders = includeSubfolders;
    }

    /**
     * Gets the URL of the share link.
     * 
     * @return The share URL
     */
    public String getShareUrl() {
        return shareUrl;
    }

    /**
     * Gets the optional password for the share link.
     * 
     * @return The password, or <code>null</code> if not set
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the optional expiration date for the share link.
     * 
     * @return The expiration date, or <code>null</code> if not set
     */
    public Date getExpiryDate() {
        return expiryDate;
    }

    /**
     * Gets a value indicating whether subfolders are included in the share or not.
     * 
     * @return <code>true</code> if subfolders are included, <code>false</code> otherwise
     */
    public boolean isIncludeSubfolders() {
        return includeSubfolders;
    }

    @Override
    public Object clone() {
        Date expiryDate = null != this.expiryDate ? new Date(this.expiryDate.getTime()) : null;
        return new LinkEntityInfo(this, shareUrl, password, expiryDate, includeSubfolders);
    }

}
