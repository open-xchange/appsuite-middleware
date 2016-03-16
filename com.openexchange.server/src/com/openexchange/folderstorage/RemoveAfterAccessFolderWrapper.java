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

package com.openexchange.folderstorage;

import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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

}
