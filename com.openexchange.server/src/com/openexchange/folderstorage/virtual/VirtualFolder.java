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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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
import com.openexchange.i18n.tools.StringHelper;

/**
 * {@link VirtualFolder} - A virtual folder backed by a real folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class VirtualFolder implements Folder {

    private static final long serialVersionUID = -7353105231203614064L;

    private Folder realFolder;

    private Date lastModified;

    private int modifiedBy;

    private String treeId;

    private String name;

    private String localizedName;

    private String parent;

    private Permission[] permissions;

    private String[] subfolders;
    private boolean b_subfolders;

    private Boolean subscribed;

    private Boolean subscribedSubfolders;

    private String newId;

    /**
     * Initializes a {@link VirtualFolder} with specified real folder.
     *
     * @param source The real folder which is mapped by this virtual folder
     */
    public VirtualFolder(final Folder source) {
        super();
        realFolder = source;
        modifiedBy = -1;
    }

    @Override
    public String toString() {
        return new StringBuilder(32).append("{ name=").append(getName()).append(", id=").append(getID()).append('}').toString();
    }

    @Override
    public Object clone() {
        try {
            final VirtualFolder clone = (VirtualFolder) super.clone();
            clone.realFolder = (Folder) (realFolder == null ? null : realFolder.clone());
            clone.lastModified = cloneDate(lastModified);
            if (permissions != null) {
                final Permission[] thisPermissions = permissions;
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

    public String getNewID() {
        return newId;
    }

    public void setNewID(final String newId) {
        this.newId = newId;
    }

    public int getCreatedBy() {
        return realFolder.getCreatedBy();
    }

    public Date getCreationDate() {
        return realFolder.getCreationDate();
    }

    public Date getLastModified() {
        return lastModified == null ? realFolder.getLastModified() : cloneDate(lastModified);
    }

    public int getModifiedBy() {
        return -1 == modifiedBy ? realFolder.getModifiedBy() : modifiedBy;
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
        if (null == name) {
            return realFolder.getLocalizedName(locale);
        }
        if (null == localizedName) {
            localizedName = StringHelper.valueOf(locale).getString(name);
        }
        return localizedName;
    }

    public String getName() {
        return null == name ? realFolder.getName() : name;
    }

    public String getParentID() {
        return null == parent ? realFolder.getParentID() : parent;
    }

    /**
     * Gets either real folder's permissions or virtual folder's individual permissions (if set)
     *
     * <pre>
     * return permissions == null ? realFolder.getPermissions() : permissions;
     * </pre>
     *
     * @return The permissions for this virtual folder
     */
    public Permission[] getPermissions() {
        /*
         * If no permissions applied return real folder's permissions
         */
        return null == permissions ? realFolder.getPermissions() : permissions;
    }

    public String[] getSubfolderIDs() {
        return b_subfolders ? subfolders : realFolder.getSubfolderIDs();
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
        localizedName = null;
    }

    public void setParentID(final String parentId) {
        parent = parentId;
    }

    public void setPermissions(final Permission[] permissions) {
        this.permissions = permissions;
    }

    public void setSubfolderIDs(final String[] subfolderIds) {
        subfolders = subfolderIds;
        b_subfolders = true;
    }

    public void setTreeID(final String id) {
        treeId = id;
    }

    public void setType(final Type type) {
        // Nothing to do
    }

    public boolean isSubscribed() {
        return null == subscribed ? realFolder.isSubscribed() : subscribed.booleanValue();
    }

    public void setSubscribed(final boolean subscribed) {
        this.subscribed = Boolean.valueOf(subscribed);
    }

    public boolean hasSubscribedSubfolders() {
        return null == subscribedSubfolders ? realFolder.hasSubscribedSubfolders() : subscribedSubfolders.booleanValue();
        /*-
         *
        if (null == subscribedSubfolders) {
            return null == subfolders || subfolders.length > 0;
        }
        return subscribedSubfolders.booleanValue();
        */
    }

    public void setSubscribedSubfolders(final boolean subscribedSubfolders) {
        this.subscribedSubfolders = Boolean.valueOf(subscribedSubfolders);
    }

    public boolean isVirtual() {
        return true;
    }

    public boolean isGlobalID() {
        return realFolder.isGlobalID();
    }

    public boolean isCacheable() {
        return false;
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

    public int getDefaultType() {
        return realFolder.getDefaultType();
    }

    public void setDefaultType(final int defaultType) {
        // Nothing to do
    }

    public int getBits() {
        return realFolder.getBits();
    }

    public void setBits(final int bits) {
        // Nothing to do
    }

    private static Date cloneDate(final Date d) {
        if (null == d) {
            return null;
        }
        return new Date(d.getTime());
    }
}
