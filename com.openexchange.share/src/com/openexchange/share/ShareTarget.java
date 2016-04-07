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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

import java.io.Serializable;

/**
 * {@link ShareTarget}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class ShareTarget implements Cloneable, Serializable {

    private static final long serialVersionUID = 5128141075771075208L;

    private int module;
    private String folder;
    private String realFolder;
    private String item;

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
        this(module, folder, null, item);
    }

    /**
     * Initializes a new {@link ShareTarget}, pointing to an item located in a parent folder of a specific groupware module.
     *
     * @param module The groupware module of the share's target folder
     * @param folder The identifier of the share's folder
     * @param realFolder The identifier of the real folder (to load) in case it differs from <code>folder</code> parameter
     * @param item The identifier of the share's item
     */
    public ShareTarget(int module, String folder, String realFolder, String item) {
        super();
        this.module = module;
        this.folder = folder;
        this.realFolder = null == realFolder || (null != folder && folder.equals(realFolder)) ? null : realFolder;
        this.item = item;
    }

    /**
     * Initializes a new {@link ShareTarget}, taking over the values from the supplied share target.
     *
     * @param target The target to copy the properties from
     */
    public ShareTarget(ShareTarget target) {
        this(target.getModule(), target.getFolder(), target.getRealFolder(), target.getItem());
    }

    /**
     * Gets the groupware module of the share's target folder.
     *
     * @return The module
     */
    public int getModule() {
        return module;
    }

    /**
     * Sets the groupware module of the share's target folder.
     *
     * @param module The module
     */
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

    /**
     * Sets the identifier of the share's folder.
     *
     * @param folder The folder ID
     */
    public void setFolder(String folder) {
        this.folder = folder;
    }

    /**
     * Gets the identifier of the real folder (to load).
     *
     * @return The real folder or <code>null</code>
     */
    public String getRealFolder() {
        return realFolder;
    }

    /**
     * Sets the identifier of the real folder (to load).
     *
     * @param realFolder The real folder to set
     */
    public void setRealFolder(String realFolder) {
        this.realFolder = realFolder;
    }

    /**
     * Gets the identifier of the folder to load.
     *
     * @return The identifier of the folder to load
     */
    public String getFolderToLoad() {
        return null == realFolder ? folder : realFolder;
    }

    /**
     * Gets a value indicating whether this share target points to a folder, i.e. there is no item defined, or not.
     *
     * @return <code>true</code> if this target points to a folder, <code>false</code>, otherwise
     */
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

    @Override
    public ShareTarget clone() {
        return new ShareTarget(this);
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
        return "ShareTarget [module=" + module + ", folder=" + getFolder() + (null != getItem() ? (", item=" + getItem()) : "") + "]";
    }

}
