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
        this(module, folder, (String) null);
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
