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

package com.openexchange.groupware.container;

import static com.openexchange.java.Autoboxing.i;

/**
 * DataObject
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */

public abstract class FolderChildObject extends DataObject {

    private static final long serialVersionUID = 9177795092994324456L;

    /**
     * The minimum identifier for a folder kept in database.
     */
    public static final int FOLDER_ID = 20;

    protected int parentFolderId;

    protected boolean b_parent_folder_id;

    // GET METHODS
    public int getParentFolderID() {
        return parentFolderId;
    }

    // SET METHODS
    public void setParentFolderID(final int parentFolderId) {
        this.parentFolderId = parentFolderId;
        b_parent_folder_id = true;
    }

    // REMOVE METHODS
    public void removeParentFolderID() {
        parentFolderId = 0;
        b_parent_folder_id = false;
    }

    // CONTAINS METHODS
    public boolean containsParentFolderID() {
        return b_parent_folder_id;
    }

    @Override
    public void reset() {
        super.reset();
        parentFolderId = 0;
        b_parent_folder_id = false;
    }

    @Override
    public void set(int field, Object value) {
        switch (field) {
        case FOLDER_ID:
            if (value instanceof Integer) {
                setParentFolderID(i((Integer) value));
            }
            if (value instanceof String) {
                setParentFolderID(Integer.parseInt((String) value));
            }
            break;
        default:
            super.set(field, value);
        }
    }

    @Override
    public Object get(int field) {
        switch (field) {
        case FOLDER_ID:
            return getParentFolderID();
        default:
            return super.get(field);
        }
    }

    @Override
    public boolean contains(int field) {
        switch (field) {
        case FOLDER_ID:
            return containsParentFolderID();
        default:
            return super.contains(field);
        }
    }

    @Override
    public void remove(int field) {
        switch (field) {
        case FOLDER_ID:
            removeParentFolderID();
            break;
        default:
            super.remove(field);
        }
    }
}
