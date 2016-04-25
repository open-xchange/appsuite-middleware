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

package com.openexchange.user.copy.internal.folder.util;

import com.openexchange.groupware.container.FolderObject;

/**
 * A {@link FolderEqualsWrapper} contains a {@link FolderObject} and
 * adds {@link java.lang.Object#equals(Object)} and {@link java.lang.Object#hashCode()}
 * to it.
 * <br>
 * <br>
 * Two {@link FolderEqualsWrapper} are <em>equal</em> if {@link FolderEqualsWrapper#getObjectID()}
 * returns the same result for both objects.
 *
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class FolderEqualsWrapper implements Cloneable, Comparable<FolderEqualsWrapper> {

    private FolderObject folder;

    private String key;


    /**
     * Initializes a new {@link FolderEqualsWrapper}.
     * @param folder
     */
    public FolderEqualsWrapper(final FolderObject folder, final String key) {
        super();
        this.folder = folder;
        this.key = key;
    }

    public FolderObject getFolder() {
        return folder;
    }

    public int getParentFolderID() {
        return folder.getParentFolderID();
    }

    public int getObjectID() {
        return folder.getObjectID();
    }

    public void setObjectID(final int id) {
        folder.setObjectID(id);
    }

    public void setParentFolderID(final int id) {
        folder.setParentFolderID(id);
    }

    public void setCreator(final int id) {
        folder.setCreator(id);
    }

    public void setModifiedBy(final int id) {
        folder.setModifiedBy(id);
    }

    public void setKey(final String key) {
        this.key = key;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((folder == null) ? 0 : folder.getObjectID());
        result = result + (key == null ? 0 : key.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FolderEqualsWrapper other = (FolderEqualsWrapper) obj;
        if (folder == null) {
            if (other.folder != null) {
                return false;
            }
        } else if (!(folder.getObjectID() == other.getObjectID())) {
            return false;
        }
        if (key == null) {
            if (other.key != null) {
                return false;
            }
        } else {
            if (other.key == null) {
                return false;
            } else if (!key.equals(other.key)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public FolderEqualsWrapper clone() throws CloneNotSupportedException {
        final FolderEqualsWrapper clone = (FolderEqualsWrapper) super.clone();
        clone.folder = folder.clone();

        return clone;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Folder: " + folder.getObjectID() + ", Parent: " + folder.getParentFolderID();
    }

    @Override
    public int compareTo(FolderEqualsWrapper o) {
        if (this.getObjectID() == o.getObjectID()) {
            return 0;
        }

        if (this.getObjectID() < o.getObjectID()) {
            return -1;
        }

        return 1;
    }
}
