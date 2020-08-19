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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.api.client.common.calls.folders;

import com.openexchange.folderstorage.AbstractFolder;

/**
 * {@link RemoteFolder} - A remote folder
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class RemoteFolder extends AbstractFolder {

    private static final long serialVersionUID = -5400694241155511852L;

    protected String module;
    protected boolean global;

    private int ownRights;
    private boolean bOwnRights;

    private boolean hasSubfolders;
    private boolean bHasSubfolders;

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
}
