/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.share;

import java.util.Date;
import java.util.Map;

/**
 * {@link ShareTarget}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class ShareTarget {

    private int module;
    private String folder;
    private String item;
    private int ownedBy;
    private Date expiryDate;
    private Map<String, Object> meta;

    /**
     * Initializes a new, empty {@link ShareTarget}.
     */
    public ShareTarget() {
        super();
    }

    /**
     * Initializes a new {@link ShareTarget}, pointing to a folder of a specific groupware module.
     *
     * @param module The groupware module of the share's target folder
     * @param folder The identifier of the share's folder
     */
    public ShareTarget(int module, String folder) {
        this(module, folder, null);
    }

    /**
     * Initializes a new {@link ShareTarget}, pointing to an item located in a parent folder of a specific groupware module.
     *
     * @param module The groupware module of the share's target folder
     * @param folder The identifier of the share's folder
     * @param item The identifier of the share's item
     */
    public ShareTarget(int module, String folder, String item) {
        super();
        this.module = module;
        this.folder = folder;
        this.item = item;
    }

    /**
     * Gets the groupware module of the share's target folder.
     *
     * @return The module
     */
    public int getModule() {
        return module;
    }

    public void setModule(int module) {
        this.module = module;
    }

    /**
     * Gets the identifier of the share's folder.
     *
     * @return The folder ID
     */
    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public boolean isFolder() {
        return item == null;
    }

    /**
     * Gets the identifier of the share's item in case the share is not a folder share.
     *
     * @return The item ID, or <code>null</code> if the share references a folder
     */
    public String getItem() {
        return item;
    }

    /**
     * Sets the identifier of the share's item in case the share is not a folder share.
     *
     * @param item The item ID
     */
    public void setItem(String item) {
        this.item = item;
    }

    /**
     * Gets the identifier of the user that is considered as the owner of the share target, which is usually the user who created the
     * shared folder or item, but not necessarily the user who shared the target itself.
     *
     * @return The identifier of the user considered as the owner of the share target
     */
    public int getOwnedBy() {
        return ownedBy;
    }

    /**
     * Sets the identifier of the user that is considered as the owner of the share target, which is usually the user who created the
     * shared folder or item, but not necessarily the user who shared the target itself.
     *
     * @param ownedBy The identifier of the user considered as the owner of the share target
     */
    public void setOwnedBy(int ownedBy) {
        this.ownedBy = ownedBy;
    }

    /**
     * If defined, gets the date when this target expires, i.e. it should be no longer accessible.
     *
     * @return The expiry date of the share, or <code>null</code> if not defined
     */
    public Date getExpiryDate() {
        return expiryDate;
    }

    /**
     * Sets the date when this share target, i.e. it should be no longer accessible.
     *
     * @param expiryDate The expiry date of the target
     */
    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    /**
     * Gets a value indicating whether the target is considered as expired, i.e. an expiry date is set and is passed in the meantime.
     *
     * @return <code>true</code> if the share is expired, <code>false</code>, otherwise
     */
    public boolean isExpired() {
        return expiryDate != null && new Date().after(expiryDate);
    }

    /**
     * Gets arbitrary metadata in a map.
     *
     * @return The metadata
     */
    public Map<String, Object> getMeta() {
        return meta;
    }

    /**
     * Sets the metadata,
     *
     * @param meta The metadata to set
     */
    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }

    /**
     * Gets the relative path of this target to address it uniquely within an underlying share.
     *
     * @return The share-relative path to the target
     */
    public String getPath() {
        return String.format("%08x", hashCode());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((folder == null) ? 0 : folder.hashCode());
        result = prime * result + ((item == null) ? 0 : item.hashCode());
        result = prime * result + module;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ShareTarget)) {
            return false;
        }
        ShareTarget other = (ShareTarget) obj;
        if (folder == null) {
            if (other.folder != null) {
                return false;
            }
        } else if (!folder.equals(other.folder)) {
            return false;
        }
        if (item == null) {
            if (other.item != null) {
                return false;
            }
        } else if (!item.equals(other.item)) {
            return false;
        }
        if (module != other.module) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ShareTarget [module=" + module + ", folder=" + folder + (null != item ? (", item=" + item) : "") + "]";
    }

}
