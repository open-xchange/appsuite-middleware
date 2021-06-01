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

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import com.openexchange.groupware.EntityInfo;

/**
 * {@link AbstractFolder} - An abstract folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractFolder implements Folder, Serializable {

    private static final long serialVersionUID = 11110622220507954L;

    protected int createdBy;

    protected int modifiedBy;

    protected Date creationDate;

    protected Date lastModified;

    protected String treeId;

    protected String id;

    protected String newId;

    protected String name;

    protected String parent;

    protected String accountId;

    protected Permission[] permissions;

    protected String[] subfolders;

    protected boolean subscribed;
    
    protected UsedForSync usedForSync;

    protected boolean subscribedSubfolders;

    protected ContentType contentType;

    protected Type type;

    protected String summary;

    protected int total;

    protected int nu;

    protected int unread;

    protected int deleted;

    protected int capabilities;

    protected int bits;

    protected boolean deefault;

    protected int defaultType;

    protected FolderPath originPath;

    protected Set<String> supportedCapabilities;

    protected EntityInfo createdFrom;

    protected EntityInfo modifiedFrom;

    protected Map<String, Object> meta = new HashMap<String, Object>();

    /**
     * Initializes an empty {@link AbstractFolder}.
     */
    protected AbstractFolder() {
        super();
        createdBy = -1;
        modifiedBy = -1;
        total = -1;
        nu = -1;
        unread = -1;
        deleted = -1;
        capabilities = -1;
        bits = -1;
        defaultType = 0;
    }

    @Override
    public String toString() {
        return new StringBuilder(32).append("{ name=").append(getName()).append(", id=").append(getID()).append('}').toString();
    }

    @Override
    public Object clone() {
        try {
            final AbstractFolder clone = (AbstractFolder) super.clone();
            if (creationDate != null) {
                clone.creationDate = new Date(creationDate.getTime());
            }
            if (lastModified != null) {
                clone.lastModified = new Date(lastModified.getTime());
            }
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
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getMeta() {
        return meta;
    }

    @Override
    public void setMeta(final Map<String, Object> meta) {
        this.meta = meta;
    }

    @Override
    public int getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(final int createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public int getModifiedBy() {
        return modifiedBy;
    }

    @Override
    public void setModifiedBy(final int modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @Override
    public Date getCreationDate() {
        return creationDate == null ? null : new Date(creationDate.getTime());
    }

    @Override
    public void setCreationDate(final Date creationDate) {
        this.creationDate = null == creationDate ? null : new Date(creationDate.getTime());
    }

    @Override
    public Date getLastModified() {
        return lastModified == null ? null : new Date(lastModified.getTime());
    }

    @Override
    public void setLastModified(final Date lastModified) {
        this.lastModified = null == lastModified ? null : new Date(lastModified.getTime());
    }

    @Override
    public ContentType getContentType() {
        return contentType;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public String getNewID() {
        return newId;
    }

    @Override
    public String getLocalizedName(final Locale locale) {
        return name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getParentID() {
        return parent;
    }

    @Override
    public String getAccountID() {
        return accountId;
    }

    @Override
    public void setAccountID(String accountId) {
        this.accountId = accountId;
    }

    @Override
    public Permission[] getPermissions() {
        return permissions;
    }

    @Override
    public String[] getSubfolderIDs() {
        return subfolders;
    }

    @Override
    public String getTreeID() {
        return treeId;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void setContentType(final ContentType contentType) {
        this.contentType = contentType;
    }

    @Override
    public void setID(final String id) {
        this.id = id;
    }

    @Override
    public void setNewID(final String newId) {
        this.newId = newId;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public void setParentID(final String parentId) {
        parent = parentId;
    }

    @Override
    public void setPermissions(final Permission[] permissions) {
        this.permissions = permissions;
    }

    @Override
    public void setSubfolderIDs(final String[] subfolderIds) {
        subfolders = subfolderIds;
    }

    @Override
    public void setTreeID(final String id) {
        treeId = id;
    }

    @Override
    public void setType(final Type type) {
        this.type = type;
    }

    @Override
    public boolean isSubscribed() {
        return subscribed;
    }

    @Override
    public void setSubscribed(final boolean subscribed) {
        this.subscribed = subscribed;
    }

    @Override
    public boolean hasSubscribedSubfolders() {
        return subscribedSubfolders;
    }

    @Override
    public void setSubscribedSubfolders(final boolean subscribedSubfolders) {
        this.subscribedSubfolders = subscribedSubfolders;
    }
    
    @Override
    public void setUsedForSync(UsedForSync usedForSync) {
        this.usedForSync = usedForSync;
    }
    
    @Override
    public UsedForSync getUsedForSync() {
        return usedForSync == null ? UsedForSync.DEFAULT : usedForSync;
    }

    @Override
    public boolean isVirtual() {
        return false;
    }

    @Override
    public boolean isCacheable() {
        return true;
    }

    @Override
    public String getSummary() {
        return summary;
    }

    @Override
    public void setSummary(final String summary) {
        this.summary = summary;
    }

    @Override
    public int getTotal() {
        return total;
    }

    @Override
    public void setTotal(final int total) {
        this.total = total;
    }

    @Override
    public int getNew() {
        return nu;
    }

    @Override
    public void setNew(final int nu) {
        this.nu = nu;
    }

    @Override
    public int getUnread() {
        return unread;
    }

    @Override
    public void setUnread(final int unread) {
        this.unread = unread;
    }

    @Override
    public int getDeleted() {
        return deleted;
    }

    @Override
    public void setDeleted(final int deleted) {
        this.deleted = deleted;
    }

    @Override
    public boolean isDefault() {
        return deefault;
    }

    @Override
    public void setDefault(final boolean deefault) {
        this.deefault = deefault;
    }

    @Override
    public int getDefaultType() {
        return defaultType;
    }

    @Override
    public void setDefaultType(final int defaultType) {
        this.defaultType = defaultType;
    }

    @Override
    public int getCapabilities() {
        return capabilities;
    }

    @Override
    public FolderPath getOriginPath() {
        return originPath;
    }

    @Override
    public void setOriginPath(FolderPath originPath) {
        this.originPath = originPath;
    }

    @Override
    public void setCapabilities(final int capabilities) {
        this.capabilities = capabilities;
    }

    @Override
    public int getBits() {
        return bits;
    }

    @Override
    public void setBits(final int bits) {
        this.bits = bits;
    }

    @Override
    public Set<String> getSupportedCapabilities() {
        return supportedCapabilities;
    }

    @Override
    public void setSupportedCapabilities(final Set<String> capabilities) {
        this.supportedCapabilities = capabilities;
    }

    /**
     * Adds specified capability identifiers.
     *
     * @param capabilities The capability identifiers to add
     */
    public void addSupportedCapabilities(final Set<String> capabilities) {
        if (null != capabilities && !capabilities.isEmpty()) {
            if (null == this.supportedCapabilities) {
                this.supportedCapabilities = new LinkedHashSet<String>(8);
            }
            for (String capability : capabilities) {
                this.supportedCapabilities.add(capability);
            }
        }
    }

    /**
     * Adds specified capability identifier.
     *
     * @param capability The capability identifier to add
     */
    public void addSupportedCapabilities(final String capability) {
        if (null == capability) {
            return;
        }
        if (null == this.supportedCapabilities) {
            this.supportedCapabilities = new LinkedHashSet<String>(8);
        }
        this.supportedCapabilities.add(capability);
    }

    @Override
    public EntityInfo getCreatedFrom() {
        return createdFrom;
    }

    @Override
    public void setCreatedFrom(EntityInfo createdFrom) {
        this.createdFrom = createdFrom;
    }

    @Override
    public EntityInfo getModifiedFrom() {
        return modifiedFrom;
    }

    @Override
    public void setModifiedFrom(EntityInfo modifiedFrom) {
        this.modifiedFrom = modifiedFrom;
    }

}
