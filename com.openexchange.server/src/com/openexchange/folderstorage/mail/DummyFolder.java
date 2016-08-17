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

package com.openexchange.folderstorage.mail;

import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.mail.contentType.MailContentType;
import com.openexchange.folderstorage.type.MailType;


/**
 * A folder that can be returned if the underlying mail store is not accessible.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
class DummyFolder implements Folder {

    private static final long serialVersionUID = -3174238234183553706L;

    private final String treeId;

    private final String id;

    private final String parentId;

    private final String name;

    private final int userId;

    private final String accountId;

    /**
     * Initializes a new {@link DummyFolder}.
     *
     * @param treeId The folder tree ID
     * @param id The folder ID
     * @param parentId The parent folder ID
     * @param accountId The folders account ID
     * @param name The folder name
     * @param userId The requesting users ID
     */
    DummyFolder(String treeId, String id, String parentId, String accountId, String name, int userId) {
        super();
        this.treeId = treeId;
        this.id = id;
        this.parentId = parentId;
        this.accountId = accountId;
        this.name = name;
        this.userId = userId;
    }

    @Override
    public int getCreatedBy() {
        return userId;
    }

    @Override
    public void setCreatedBy(int createdBy) {
    }

    @Override
    public int getModifiedBy() {
        return userId;
    }

    @Override
    public void setModifiedBy(int modifiedBy) {
    }

    @Override
    public Date getCreationDate() {
        return new Date();
    }

    @Override
    public void setCreationDate(Date creationDate) {
    }

    @Override
    public Date getLastModified() {
        return new Date();
    }

    @Override
    public void setLastModified(Date lastModified) {
    }

    @Override
    public boolean isCacheable() {
        return false;
    }

    @Override
    public boolean isVirtual() {
        return true;
    }

    @Override
    public boolean isGlobalID() {
        return false;
    }

    @Override
    public String getTreeID() {
        return treeId;
    }

    @Override
    public void setTreeID(String id) {
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public void setID(String id) {
    }

    @Override
    public String getNewID() {
        return null;
    }

    @Override
    public void setNewID(String newId) {
    }

    @Override
    public String getParentID() {
        return parentId;
    }

    @Override
    public void setParentID(String parentId) {
    }

    @Override
    public String getAccountID() {
        return accountId;
    }

    @Override
    public void setAccountID(String accountId) {
    }

    @Override
    public String[] getSubfolderIDs() {
        return new String[0];
    }

    @Override
    public void setSubfolderIDs(String[] subfolderIds) {
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
    }

    @Override
    public String getLocalizedName(Locale locale) {
        return name;
    }

    @Override
    public Permission[] getPermissions() {
        return null;
    }

    @Override
    public void setPermissions(Permission[] permissions) {
    }

    @Override
    public ContentType getContentType() {
        return MailContentType.getInstance();
    }

    @Override
    public void setContentType(ContentType contentType) {
    }

    @Override
    public Type getType() {
        return MailType.getInstance();
    }

    @Override
    public void setType(Type type) {
    }

    @Override
    public boolean isSubscribed() {
        return false;
    }

    @Override
    public void setSubscribed(boolean subscribed) {
    }

    @Override
    public boolean hasSubscribedSubfolders() {
        return false;
    }

    @Override
    public void setSubscribedSubfolders(boolean subscribedSubfolders) {

    }

    @Override
    public String getSummary() {
        return null;
    }

    @Override
    public void setSummary(String summary) {
    }

    @Override
    public int getTotal() {
        return 0;
    }

    @Override
    public void setTotal(int total) {
    }

    @Override
    public int getNew() {
        return 0;
    }

    @Override
    public void setNew(int nu) {
    }

    @Override
    public int getUnread() {
        return 0;
    }

    @Override
    public void setUnread(int unread) {
    }

    @Override
    public int getDeleted() {
        return 0;
    }

    @Override
    public void setDeleted(int deleted) {
    }

    @Override
    public boolean isDefault() {
        return false;
    }

    @Override
    public void setDefault(boolean deefault) {
    }

    @Override
    public int getDefaultType() {
        return 0;
    }

    @Override
    public void setDefaultType(int defaultType) {
    }

    @Override
    public int getCapabilities() {
        return -1;
    }

    @Override
    public void setCapabilities(int capabilities) {
    }

    @Override
    public int getBits() {
        return 0;
    }

    @Override
    public void setBits(int bits) {
    }

    @Override
    public void setMeta(Map<String, Object> meta) {
    }

    @Override
    public Map<String, Object> getMeta() {
        return null;
    }

    @Override
    public Set<String> getSupportedCapabilities() {
        return Collections.emptySet();
    }

    @Override
    public void setSupportedCapabilities(Set<String> capabilities) {
    }

    @Override
    public Object clone() {
        return new DummyFolder(treeId, id, parentId, accountId, name, userId);
    }

}
