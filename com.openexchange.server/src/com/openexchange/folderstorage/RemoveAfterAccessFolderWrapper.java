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

package com.openexchange.folderstorage;

import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import com.openexchange.groupware.EntityInfo;

/**
 * {@link RemoveAfterAccessFolderWrapper} - Simple wrapper for {@link Folder} to enhance with {@link RemoveAfterAccessFolder} behavior.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RemoveAfterAccessFolderWrapper implements RemoveAfterAccessFolder {

    private static final long serialVersionUID = 5320754019338469187L;

    private final Folder folder;

    private final boolean loadSubfolders;

    private final int userId;

    private final int contextId;

    /**
     * Initializes a new {@link RemoveAfterAccessFolderWrapper}.
     */
    public RemoveAfterAccessFolderWrapper(final Folder folder, final boolean loadSubfolders, final int userId, final int contextId) {
        super();
        this.folder = folder;
        this.loadSubfolders = loadSubfolders;
        this.userId = userId;
        this.contextId = contextId;
    }

    @Override
    public int getContextId() {
        return contextId;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public boolean loadSubfolders() {
        return loadSubfolders;
    }

    @Override
    public int hashCode() {
        return folder.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return folder.equals(obj);
    }

    @Override
    public String toString() {
        return folder.toString();
    }

    @Override
    public int getCreatedBy() {
        return folder.getCreatedBy();
    }

    @Override
    public void setCreatedBy(final int createdBy) {
        folder.setCreatedBy(createdBy);
    }

    @Override
    public int getModifiedBy() {
        return folder.getModifiedBy();
    }

    @Override
    public void setModifiedBy(final int modifiedBy) {
        folder.setModifiedBy(modifiedBy);
    }

    @Override
    public Date getCreationDate() {
        return folder.getCreationDate();
    }

    @Override
    public void setCreationDate(final Date creationDate) {
        folder.setCreationDate(creationDate);
    }

    @Override
    public Date getLastModified() {
        return folder.getLastModified();
    }

    @Override
    public void setLastModified(final Date lastModified) {
        folder.setLastModified(lastModified);
    }

    @Override
    public String getID() {
        return folder.getID();
    }

    @Override
    public String getNewID() {
        return folder.getNewID();
    }

    @Override
    public String getAccountID() {
        return folder.getAccountID();
    }

    @Override
    public String getLocalizedName(final Locale locale) {
        return folder.getLocalizedName(locale);
    }

    @Override
    public String getName() {
        return folder.getName();
    }

    @Override
    public String getParentID() {
        return folder.getParentID();
    }

    @Override
    public Permission[] getPermissions() {
        return folder.getPermissions();
    }

    @Override
    public String[] getSubfolderIDs() {
        return folder.getSubfolderIDs();
    }

    @Override
    public String getTreeID() {
        return folder.getTreeID();
    }

    @Override
    public void setID(final String id) {
        folder.setID(id);
    }

    @Override
    public void setNewID(final String newId) {
        folder.setNewID(newId);
    }

    @Override
    public void setName(final String name) {
        folder.setName(name);
    }

    @Override
    public void setParentID(final String parentId) {
        folder.setParentID(parentId);
    }

    @Override
    public void setAccountID(String accountId) {
        folder.setAccountID(accountId);
    }

    @Override
    public void setPermissions(final Permission[] permissions) {
        folder.setPermissions(permissions);
    }

    @Override
    public void setSubfolderIDs(final String[] subfolderIds) {
        folder.setSubfolderIDs(subfolderIds);
    }

    @Override
    public void setTreeID(final String id) {
        folder.setTreeID(id);
    }

    @Override
    public boolean isSubscribed() {
        return folder.isSubscribed();
    }

    @Override
    public void setSubscribed(final boolean subscribed) {
        folder.setSubscribed(subscribed);
    }

    @Override
    public boolean hasSubscribedSubfolders() {
        return folder.hasSubscribedSubfolders();
    }

    @Override
    public void setSubscribedSubfolders(final boolean subscribedSubfolders) {
        folder.setSubscribedSubfolders(subscribedSubfolders);
    }

    @Override
    public boolean isVirtual() {
        return folder.isVirtual();
    }

    @Override
    public String getSummary() {
        return folder.getSummary();
    }

    @Override
    public void setSummary(final String summary) {
        folder.setSummary(summary);
    }

    @Override
    public void setTotal(final int total) {
        folder.setTotal(total);
    }

    @Override
    public int getNew() {
        return folder.getNew();
    }

    @Override
    public void setNew(final int nu) {
        folder.setNew(nu);
    }

    @Override
    public void setUnread(final int unread) {
        folder.setUnread(unread);
    }

    @Override
    public int getDeleted() {
        return folder.getDeleted();
    }

    @Override
    public void setDeleted(final int deleted) {
        folder.setDeleted(deleted);
    }

    @Override
    public boolean isDefault() {
        return folder.isDefault();
    }

    @Override
    public void setDefault(final boolean deefault) {
        folder.setDefault(deefault);
    }

    @Override
    public int getCapabilities() {
        return folder.getCapabilities();
    }

    @Override
    public void setCapabilities(final int capabilities) {
        folder.setCapabilities(capabilities);
    }

    @Override
    public int getBits() {
        return folder.getBits();
    }

    @Override
    public void setBits(final int bits) {
        folder.setBits(bits);
    }

    @Override
    public int getUnread() {
        return folder.getUnread();
    }

    @Override
    public int getTotal() {
        return folder.getTotal();
    }

    @Override
    public Object clone() {
        return folder.clone();
    }

    @Override
    public boolean isCacheable() {
        return folder.isCacheable();
    }

    @Override
    public ContentType getContentType() {
        return folder.getContentType();
    }

    @Override
    public int getDefaultType() {
        return folder.getDefaultType();
    }

    @Override
    public void setDefaultType(final int defaultType) {
        folder.setDefaultType(defaultType);
    }

    @Override
    public Type getType() {
        return folder.getType();
    }

    @Override
    public void setContentType(final ContentType contentType) {
        folder.setContentType(contentType);
    }

    @Override
    public void setType(final Type type) {
        folder.setType(type);
    }

    @Override
    public boolean isGlobalID() {
        return folder.isGlobalID();
    }

    @Override
    public void setMeta(Map<String, Object> meta) {
        folder.setMeta(meta);
    }

    @Override
    public Map<String, Object> getMeta() {
        return folder.getMeta();
    }

    @Override
    public Set<String> getSupportedCapabilities() {
        return folder.getSupportedCapabilities();
    }

    @Override
    public void setSupportedCapabilities(Set<String> capabilities) {
        folder.setSupportedCapabilities(capabilities);
    }

    @Override
    public FolderPath getOriginPath() {
        return folder.getOriginPath();
    }

    @Override
    public void setOriginPath(FolderPath originPath) {
        folder.setOriginPath(originPath);
    }

    @Override
    public EntityInfo getCreatedFrom() {
        return folder.getCreatedFrom();
    }

    @Override
    public void setCreatedFrom(EntityInfo createdFrom) {
        folder.setCreatedFrom(createdFrom);
    }

    @Override
    public EntityInfo getModifiedFrom() {
        return folder.getModifiedFrom();
    }

    @Override
    public void setModifiedFrom(EntityInfo modifiedFrom) {
        folder.setModifiedFrom(modifiedFrom);
    }

}
