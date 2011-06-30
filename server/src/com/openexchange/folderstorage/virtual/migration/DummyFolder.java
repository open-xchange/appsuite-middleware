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

package com.openexchange.folderstorage.virtual.migration;

import java.util.Date;
import java.util.Locale;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Type;

/**
 * Helper class to create a folder.
 */
final class DummyFolder implements Folder {

    private static final long serialVersionUID = 8179196440833088118L;

    private Date lastModified;

    private int modifiedBy;

    private String treeId;

    private String id;

    private String name;

    private String parent;

    private Permission[] permissions;

    private boolean subscribed;

    public DummyFolder() {
        super();
        modifiedBy = -1;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new InternalError(e.getMessage());
        }
    }

    public int getCapabilities() {
        throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.getCapabilities()");
    }

    public ContentType getContentType() {
        throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.getContentType()");
    }

    public int getCreatedBy() {
        throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.getCreatedBy()");
    }

    public java.util.Date getCreationDate() {
        throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.getCreationDate()");
    }

    public int getDeleted() {
        throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.getDeleted()");
    }

    public String getID() {
        return id;
    }

    public java.util.Date getLastModified() {
        return lastModified;
    }

    public String getLocalizedName(final Locale locale) {
        return name;
    }

    public int getModifiedBy() {
        return modifiedBy;
    }

    public String getName() {
        return name;
    }

    public int getNew() {
        throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.getNew()");
    }

    public String getParentID() {
        return parent;
    }

    public Permission[] getPermissions() {
        return permissions;
    }

    public String[] getSubfolderIDs() {
        throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.getSubfolderIDs()");
    }

    public String getSummary() {
        throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.getSummary()");
    }

    public int getTotal() {
        throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.getTotal()");
    }

    public String getTreeID() {
        return treeId;
    }

    public Type getType() {
        throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.getType()");
    }

    public int getUnread() {
        throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.getUnread()");
    }

    public boolean isCacheable() {
        throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.isCacheable()");
    }

    public boolean isDefault() {
        throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.isDefault()");
    }

    public boolean isGlobalID() {
        throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.isGlobalID()");
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public boolean isVirtual() {
        return true;
    }

    public void setCapabilities(final int capabilities) {
        throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.setCapabilities()");
    }

    public void setContentType(final ContentType contentType) {
        throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.setContentType()");
    }

    public void setCreatedBy(final int createdBy) {
        throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.setCreatedBy()");
    }

    public void setCreationDate(final java.util.Date creationDate) {
        throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.setCreationDate()");
    }

    public void setDefault(final boolean deefault) {
        throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.setDefault()");
    }

    public void setDeleted(final int deleted) {
        throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.setDeleted()");
    }

    public void setID(final String id) {
        this.id = id;
    }

    public void setLastModified(final Date lastModified) {
        this.lastModified = lastModified;
    }

    public void setModifiedBy(final int modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setNew(final int nu) {
        throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.setNew()");
    }

    public void setParentID(final String parentId) {
        parent = parentId;
    }

    public void setPermissions(final Permission[] permissions) {
        this.permissions = permissions;
    }

    public void setSubfolderIDs(final String[] subfolderIds) {
        throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.setSubfolderIDs()");
    }

    public void setSubscribed(final boolean subscribed) {
        this.subscribed = subscribed;
    }

    public void setSummary(final String summary) {
        throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.setSummary()");
    }

    public void setTotal(final int total) {
        throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.setTotal()");
    }

    public void setTreeID(final String id) {
        treeId = id;
    }

    public void setType(final Type type) {
        throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.setType()");
    }

    public void setUnread(final int unread) {
        throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.setUnread()");
    }

    public int getDefaultType() {
        throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.getDefaultInfo()");
    }

    public void setDefaultType(final int defaultType) {
        throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.setDefaultInfo()");
    }

    public int getBits() {
        throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.getBits()");
    }

    public void setBits(final int bits) {
        throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.setBits()");
    }

    public String getNewID() {
        throw new UnsupportedOperationException("DummyFolder.getNewID()");
    }

    public void setNewID(final String newId) {
        throw new UnsupportedOperationException("DummyFolder.setNewID()");
    }

    public boolean hasSubscribedSubfolders() {
        throw new UnsupportedOperationException("DummyFolder.hasSubscribedSubfolders()");
    }

    public void setSubscribedSubfolders(final boolean subscribedSubfolders) {
        throw new UnsupportedOperationException("DummyFolder.setSubscribedSubfolders()");
    }

}
