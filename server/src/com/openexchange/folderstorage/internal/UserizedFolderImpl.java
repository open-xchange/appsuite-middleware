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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.folderstorage.internal;

import java.util.Date;
import java.util.Locale;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UserizedFolder;

/**
 * {@link UserizedFolderImpl} - The {@link UserizedFolder} implementation.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UserizedFolderImpl implements UserizedFolder {

    private static final long serialVersionUID = 5090343231211791986L;

    private Folder folder;

    private Permission ownPermission;

    /**
     * Initializes a new {@link UserizedFolderImpl}.
     */
    public UserizedFolderImpl(final Folder folder) {
        super();
        this.folder = folder;
    }

    @Override
    public Object clone() {
        try {
            final UserizedFolderImpl clone = (UserizedFolderImpl) super.clone();
            clone.folder = (Folder) clone.folder.clone();
            clone.ownPermission = (Permission) clone.ownPermission.clone();
            return clone;
        } catch (final CloneNotSupportedException e) {
            throw new InternalError(e.getMessage());
        }
    }

    public int getCreatedBy() {
        return folder.getCreatedBy();
    }

    public Date getCreationDate() {
        return folder.getCreationDate();
    }

    public Date getLastModified() {
        return folder.getLastModified();
    }

    public int getModifiedBy() {
        return folder.getModifiedBy();
    }

    public void setCreatedBy(final int createdBy) {
        folder.setCreatedBy(createdBy);
    }

    public void setCreationDate(final Date creationDate) {
        folder.setCreationDate(creationDate);
    }

    public void setLastModified(final Date lastModified) {
        folder.setLastModified(lastModified);
    }

    public void setModifiedBy(final int modifiedBy) {
        folder.setModifiedBy(modifiedBy);
    }

    public ContentType getContentType() {
        return folder.getContentType();
    }

    public String getID() {
        return folder.getID();
    }

    public String getLocalizedName(final Locale locale) {
        return folder.getLocalizedName(locale);
    }

    public String getName() {
        return folder.getName();
    }

    public String getParentID() {
        return folder.getParentID();
    }

    public Permission[] getPermissions() {
        return folder.getPermissions();
    }

    public String[] getSubfolderIDs() {
        return folder.getSubfolderIDs();
    }

    public String getTreeID() {
        return folder.getTreeID();
    }

    public Type getType() {
        return folder.getType();
    }

    public boolean isCacheable() {
        return folder.isCacheable();
    }

    public boolean isGlobalID() {
        return folder.isGlobalID();
    }

    public boolean isSubscribed() {
        return folder.isSubscribed();
    }

    public boolean isVirtual() {
        return folder.isVirtual();
    }

    public void setContentType(final ContentType contentType) {
        folder.setContentType(contentType);
    }

    public void setID(final String id) {
        folder.setID(id);
    }

    public void setName(final String name) {
        folder.setName(name);
    }

    public void setParentID(final String parentId) {
        folder.setParentID(parentId);
    }

    public void setPermissions(final Permission[] permissions) {
        folder.setPermissions(permissions);
    }

    public void setSubfolderIDs(final String[] subfolderIds) {
        folder.setSubfolderIDs(subfolderIds);
    }

    public void setSubscribed(final boolean subscribed) {
        folder.setSubscribed(subscribed);
    }

    public void setTreeID(final String id) {
        folder.setTreeID(id);
    }

    public void setType(final Type type) {
        folder.setType(type);
    }

    public Permission getOwnPermission() {
        return ownPermission;
    }

    public void setOwnPermission(final Permission ownPermission) {
        this.ownPermission = ownPermission;
    }

}
