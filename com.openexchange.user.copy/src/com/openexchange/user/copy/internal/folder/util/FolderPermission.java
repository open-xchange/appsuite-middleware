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

package com.openexchange.user.copy.internal.folder.util;


/**
 * {@link FolderPermission}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class FolderPermission {

    private int folderId;

    private int userId;

    private int fp;

    private int orp;

    private int owp;

    private int odp;

    private boolean adminFlag;

    private boolean groupFlag;

    private boolean system;

    /**
     * Initializes a new {@link FolderPermhassion}.
     */
    public FolderPermission() {
        super();
    }


    public int getFolderId() {
        return folderId;
    }


    public void setFolderId(final int folderId) {
        this.folderId = folderId;
    }


    public int getUserId() {
        return userId;
    }


    public void setUserId(final int userId) {
        this.userId = userId;
    }


    public int getFp() {
        return fp;
    }


    public void setFp(final int fp) {
        this.fp = fp;
    }


    public int getOrp() {
        return orp;
    }


    public void setOrp(final int orp) {
        this.orp = orp;
    }


    public int getOwp() {
        return owp;
    }


    public void setOwp(final int owp) {
        this.owp = owp;
    }


    public int getOdp() {
        return odp;
    }


    public void setOdp(final int odp) {
        this.odp = odp;
    }


    public boolean hasAdminFlag() {
        return adminFlag;
    }


    public void setAdminFlag(final boolean adminFlag) {
        this.adminFlag = adminFlag;
    }


    public boolean hasGroupFlag() {
        return groupFlag;
    }


    public void setGroupFlag(final boolean groupFlag) {
        this.groupFlag = groupFlag;
    }


    public boolean hasSystem() {
        return system;
    }


    public void setSystem(final boolean system) {
        this.system = system;
    }

}
