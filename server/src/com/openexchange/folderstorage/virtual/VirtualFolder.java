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

package com.openexchange.folderstorage.virtual;

import java.util.Date;
import java.util.Locale;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Type;

/**
 * {@link VirtualFolder} - A virtual folder.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class VirtualFolder implements Folder {

    private static final long serialVersionUID = 1076412172524386127L;

    private Folder realFolder;

    private Date lastModified;

    private int modifiedBy;

    private String treeId;

    private String name;

    private String parent;

    private Permission[] permissions;

    private String[] subfolders;

    private boolean subscribed;

    /**
     * Initializes a {@link VirtualFolder} with specified real folder.
     * 
     * @param source The real folder which is mapped by this virtual folder
     */
    public VirtualFolder(final Folder source) {
        super();
        this.realFolder = source;
    }

    @Override
    public Object clone() {
        try {
            final VirtualFolder clone = (VirtualFolder) super.clone();
            clone.realFolder = (Folder) (realFolder == null ? null : realFolder.clone());
            clone.lastModified = lastModified == null ? null : new Date(lastModified.getTime());
            if (permissions != null) {
                final Permission[] thisPermissions = this.permissions;
                final Permission[] clonePermissions = new Permission[thisPermissions.length];
                for (int i = 0; i < thisPermissions.length; i++) {
                    clonePermissions[i] = (Permission) thisPermissions[i].clone();
                }
                clone.permissions = clonePermissions;
            }
            if (subfolders != null) {
                final String[] thisSub = subfolders;
                final String[] cloneSub = new String[thisSub.length];
                for (int i = 0; i < cloneSub.length; i++) {
                    cloneSub[i] = thisSub[i];
                }
                clone.subfolders = cloneSub;
            }
            return clone;
        } catch (final CloneNotSupportedException e) {
            throw new InternalError(e.getMessage());
        }
    }

    public int getCreatedBy() {
        return realFolder.getCreatedBy();
    }

    public Date getCreationDate() {
        return realFolder.getCreationDate();
    }

    public Date getLastModified() {
        return lastModified == null ? null : new Date(lastModified.getTime());
    }

    public int getModifiedBy() {
        return modifiedBy;
    }

    public void setCreatedBy(final int createdBy) {
        // Nothing to do
    }

    public void setCreationDate(final Date creationDate) {
        // Nothing to do
    }

    public void setLastModified(final Date lastModified) {
        this.lastModified = lastModified == null ? null : new Date(lastModified.getTime());
    }

    public void setModifiedBy(final int modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public ContentType getContentType() {
        return realFolder.getContentType();
    }

    public String getID() {
        return realFolder.getID();
    }

    public String getLocalizedName(final Locale locale) {
        return name;
    }

    public String getName() {
        return name;
    }

    public String getParentID() {
        return parent;
    }

    public Permission[] getPermissions() {
        return permissions;
    }

    public String[] getSubfolderIDs() {
        return subfolders;
    }

    public String getTreeID() {
        return treeId;
    }

    public Type getType() {
        return realFolder.getType();
    }

    public void setContentType(final ContentType contentType) {
        // Nothing to do
    }

    public void setID(final String id) {
        // Nothing to do
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setParentID(final String parentId) {
        this.parent = parentId;
    }

    public void setPermissions(final Permission[] permissions) {
        this.permissions = permissions;
    }

    public void setSubfolderIDs(final String[] subfolderIds) {
        this.subfolders = subfolderIds;
    }

    public void setTreeID(final String id) {
        this.treeId = id;
    }

    public void setType(final Type type) {
        // Nothing to do
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public void setSubscribed(final boolean subscribed) {
        this.subscribed = subscribed;
    }

    public boolean isVirtual() {
        return true;
    }

    public boolean isGlobalID() {
        return realFolder.isGlobalID();
    }

    public boolean isCacheable() {
        return true;
    }

    public int getCapabilities() {
        return realFolder.getCapabilities();
    }

    public int getDeleted() {
        return realFolder.getDeleted();
    }

    public int getNew() {
        return realFolder.getNew();
    }

    public String getSummary() {
        return realFolder.getSummary();
    }

    public int getTotal() {
        return realFolder.getTotal();
    }

    public int getUnread() {
        return realFolder.getUnread();
    }

    public boolean isDefault() {
        return realFolder.isDefault();
    }

    public void setCapabilities(final int capabilities) {
        // Nothing to do
    }

    public void setDefault(final boolean deefault) {
        // Nothing to do
    }

    public void setDeleted(final int deleted) {
        // Nothing to do
    }

    public void setNew(final int nu) {
        // Nothing to do
    }

    public void setSummary(final String summary) {
        // Nothing to do
    }

    public void setTotal(final int total) {
        // Nothing to do
    }

    public void setUnread(final int unread) {
        // Nothing to do
    }

}
