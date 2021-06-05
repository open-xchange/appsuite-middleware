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

package com.openexchange.api.client.common.calls.folders;

import com.openexchange.folderstorage.AbstractFolder;
import com.openexchange.folderstorage.SetterAwareFolder;

/**
 * {@link RemoteFolder} - A remote folder
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class RemoteFolder extends AbstractFolder implements SetterAwareFolder {

    private static final long serialVersionUID = -5400694241155511852L;

    protected String module;
    protected boolean global;

    private int ownRights;
    private boolean bOwnRights;

    private boolean hasSubfolders;
    private boolean bHasSubfolders;

    private boolean containsSubscribed;
    private boolean containsUsedForSync;
    private boolean containsSubscribedSubfolders;
    private ExtendedPermission[] extendedPermissions;
    private boolean containsExtendedPermissions;

    /**
     * Initializes a new {@link RemoteFolder}.
     *
     */
    public RemoteFolder() {}

    /**
     * Initializes a new {@link RemoteFolder}.
     *
     * @param module The module of the folder
     */
    public RemoteFolder(String module) {
        this.module = module;
    }

    @Override
    public boolean isGlobalID() {
        return global;
    }

    /**
     * Gets the global state
     *
     * @return The global state
     */
    public boolean isGlobal() {
        return global;
    }

    /**
     * Sets the global state
     *
     * @param global The global state to set
     */
    public void setGlobal(boolean global) {
        this.global = global;
    }

    /**
     * Gets the module
     *
     * @return The module
     */
    public String getModule() {
        return module;
    }

    /**
     * Sets the module
     *
     * @param module the module
     */
    public void setModule(String module) {
        this.module = module;
    }

    /**
     * Gets the user's own permissions
     *
     * @return The permissions
     */
    public int getOwnRights() {
        return ownRights;
    }

    /**
     * Sets the user's own permission
     *
     * @param ownRights The permissions
     */
    public void setOwnRights(int ownRights) {
        this.ownRights = ownRights;
        bOwnRights = true;
    }

    /**
     * Removes the ownRights
     */
    public void removeOwnRights() {
        ownRights = 0;
        bOwnRights = false;
    }

    /**
     * Indicates whether the folder has the ownRights field set
     *
     * @return True, if the folder has the ownRights field set, false otherwise
     */
    public boolean containsOwnRights() {
        return bOwnRights;
    }

    /**
     * Gets whether the folder has sub folders
     *
     * @return True if the folder has sub folders, false otherwise
     */
    public boolean hasSubfolders() {
       return hasSubfolders;
    }

    /**
     * Sets whether the folder has sub folders
     *
     * @param hasSubfolders Whether the folder has sub folders or not
     */
    public void setHasSubfolders(boolean hasSubfolders) {
       this.hasSubfolders = hasSubfolders;
       bHasSubfolders = true;
    }

    /**
     * Indicates whether the folder has the sub folders flag set
     *
     * @return true, if the folder has the sub folder flag set, false otherwise
     */
    public boolean containsHasSubfolders() {
       return bHasSubfolders;
    }

    /**
     * Removes the sub folders
     */
    public void removeHasSubfolders() {
        setSubfolderIDs(null);
        bHasSubfolders = false;
    }

    @Override
    public void setSubfolderIDs(String[] subfolderIds) {
        super.setSubfolderIDs(subfolderIds);
        setHasSubfolders(subfolderIds != null && subfolderIds.length > 0);
    }

    @Override
    public boolean containsSubscribed() {
        return containsSubscribed;
    }

    @Override
    public void setSubscribed(boolean subscribed) {
        super.setSubscribed(subscribed);
        containsSubscribed = true;
    }

    public void removeSubscribed() {
        super.setSubscribed(false);
        containsSubscribed = false;
    }

    public boolean containsSubscribedSubfolders() {
        return containsSubscribedSubfolders;
    }

    @Override
    public void setSubscribedSubfolders(boolean subscribedSubfolders) {
        super.setSubscribedSubfolders(subscribedSubfolders);
        containsSubscribedSubfolders = true;
    }

    public void removeSubscribedSubfolders() {
        super.setSubscribedSubfolders(false);
        containsSubscribedSubfolders = false;
    }

    @Override
    public boolean containsUsedForSync() {
        return containsUsedForSync;
    }

    public ExtendedPermission[] getExtendedPermissions() {
        return extendedPermissions;
    }

    public void setExtendedPermissions(ExtendedPermission[] extendedPermissions) {
        this.extendedPermissions = extendedPermissions;
        containsExtendedPermissions = true;
    }

    public void removeExtendedPermissions() {
        this.extendedPermissions = null;
        containsExtendedPermissions = false;
    }

    public boolean containsExtendedPermissions() {
        return containsExtendedPermissions;
    }

}
