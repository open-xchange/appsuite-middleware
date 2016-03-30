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

package com.openexchange.folderstorage.internal;

import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.folderstorage.AltNameAwareFolder;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExtension;
import com.openexchange.folderstorage.FolderField;
import com.openexchange.folderstorage.FolderProperty;
import com.openexchange.folderstorage.ParameterizedFolder;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.session.Session;

/**
 * {@link UserizedFolderImpl} - The {@link UserizedFolder} implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UserizedFolderImpl implements UserizedFolder {

    private static final long serialVersionUID = 5090343231211791986L;

    private final Session session;
    private final User user;
    private final Context context;

    private Folder folder;
    private Permission ownPermission;
    private Date lastModifiedUTC;
    private Date creationDateUTC;
    private Locale locale;
    private Boolean deefault;
    private Integer defaultType;
    private Type type;
    private Permission[] permissions;
    private String[] subfolderIds;
    private String parentId;
    private Date creationDate;
    private Date lastModified;
    private volatile Map<FolderField, FolderProperty> properties;
    private int[] totalAndUnread;
    private volatile ConcurrentMap<String, Object> parameters;
    private boolean altNames;

    /**
     * Initializes a new {@link UserizedFolderImpl} from specified folder.
     *
     * @param folder The underlying folder
     * @param session The associated session
     * @param user The associated user
     * @param context The associated context
     * @throws IllegalArgumentException If folder is <code>null</code>
     */
    public UserizedFolderImpl(final Folder folder, final Session session, final User user, final Context context) {
        super();
        if (null == folder) {
            throw new IllegalArgumentException("Folder is null.");
        }
        this.folder = folder;
        this.session = session;
        this.user = user;
        this.context = context;
    }

    /**
     * Sets the parameters reference.
     *
     * @param parameters The parameters to set
     */
    @Override
    public void setParameters(final ConcurrentMap<String, Object> parameters) {
        this.parameters = parameters;
    }

    /**
     * Gets the parameters reference.
     *
     * @return The parameters reference
     */
    @Override
    public ConcurrentMap<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return new StringBuilder(32).append("{ name=").append(folder.getName()).append(", id=").append(folder.getID()).append('}').toString();
    }

    @Override
    public Object clone() {
        try {
            final UserizedFolderImpl clone = (UserizedFolderImpl) super.clone();
            clone.folder = (Folder) clone.folder.clone();
            clone.ownPermission = ownPermission == null ? null : (Permission) ownPermission.clone();
            clone.lastModifiedUTC = null == lastModifiedUTC ? null : new Date(lastModifiedUTC.getTime());
            clone.creationDateUTC = null == creationDateUTC ? null : new Date(creationDateUTC.getTime());
            clone.locale = (Locale) (null == locale ? null : locale.clone());
            return clone;
        } catch (final CloneNotSupportedException e) {
            throw new InternalError(e.getMessage());
        }
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public Session getSession() {
        return session;
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
    public int getCreatedBy() {
        return folder.getCreatedBy();
    }

    @Override
    public Date getCreationDate() {
        return creationDate == null ? folder.getCreationDate() : creationDate;
    }

    @Override
    public Date getLastModified() {
        return lastModified == null ? folder.getLastModified() : lastModified;
    }

    @Override
    public int getModifiedBy() {
        return folder.getModifiedBy();
    }

    @Override
    public void setCreatedBy(final int createdBy) {
        folder.setCreatedBy(createdBy);
    }

    @Override
    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate == null ? null : new Date(creationDate.getTime());
    }

    @Override
    public void setLastModified(final Date lastModified) {
        this.lastModified = lastModified == null ? null : new Date(lastModified.getTime());
    }

    @Override
    public void setModifiedBy(final int modifiedBy) {
        folder.setModifiedBy(modifiedBy);
    }

    @Override
    public ContentType getContentType() {
        return folder.getContentType();
    }

    @Override
    public String getID() {
        return folder.getID();
    }

    @Override
    public String getLocalizedName(final Locale locale) {
        return folder.getLocalizedName(null == locale ? LocaleTools.DEFAULT_LOCALE : locale);
    }

    @Override
    public String getName() {
        return folder.getName();
    }

    @Override
    public String getParentID() {
        return null == parentId ? folder.getParentID() : parentId;
    }

    @Override
    public String getAccountID() {
        return folder.getAccountID();
    }

    @Override
    public void setAccountID(String accountId) {
        folder.setAccountID(accountId);
    }

    @Override
    public Permission[] getPermissions() {
        return null == permissions ? folder.getPermissions() : permissions;
    }

    @Override
    public String[] getSubfolderIDs() {
        return subfolderIds == null ? folder.getSubfolderIDs() : subfolderIds;
    }

    @Override
    public String getTreeID() {
        return folder.getTreeID();
    }

    @Override
    public Type getType() {
        return null == type ? folder.getType() : type;
    }

    @Override
    public boolean isCacheable() {
        return folder.isCacheable();
    }

    @Override
    public boolean isGlobalID() {
        return folder.isGlobalID();
    }

    @Override
    public boolean isSubscribed() {
        return folder.isSubscribed();
    }

    @Override
    public boolean hasSubscribedSubfolders() {
        return folder.hasSubscribedSubfolders();
    }

    @Override
    public boolean isVirtual() {
        return folder.isVirtual();
    }

    @Override
    public void setContentType(final ContentType contentType) {
        folder.setContentType(contentType);
    }

    @Override
    public void setID(final String id) {
        folder.setID(id);
    }

    @Override
    public void setName(final String name) {
        folder.setName(name);
    }

    @Override
    public void setParentID(final String parentId) {
        this.parentId = parentId;
    }

    @Override
    public void setPermissions(final Permission[] permissions) {
        this.permissions = permissions;
    }

    @Override
    public void setSubfolderIDs(final String[] subfolderIds) {
        this.subfolderIds = subfolderIds;
    }

    @Override
    public void setSubscribed(final boolean subscribed) {
        folder.setSubscribed(subscribed);
    }

    @Override
    public void setSubscribedSubfolders(final boolean subscribedSubfolders) {
        folder.setSubscribedSubfolders(subscribedSubfolders);
    }

    @Override
    public void setTreeID(final String id) {
        folder.setTreeID(id);
    }

    @Override
    public void setType(final Type type) {
        this.type = type;
    }

    @Override
    public Permission getOwnPermission() {
        return ownPermission;
    }

    @Override
    public void setOwnPermission(final Permission ownPermission) {
        this.ownPermission = ownPermission;
    }

    @Override
    public Date getLastModifiedUTC() {
        return lastModifiedUTC == null ? null : new Date(lastModifiedUTC.getTime());
    }

    @Override
    public void setLastModifiedUTC(final Date lastModifiedUTC) {
        this.lastModifiedUTC = lastModifiedUTC == null ? null : new Date(lastModifiedUTC.getTime());
    }

    @Override
    public Date getCreationDateUTC() {
        return creationDateUTC == null ? null : new Date(creationDateUTC.getTime());
    }

    @Override
    public void setCreationDateUTC(final Date creationDateUTC) {
        this.creationDateUTC = creationDateUTC == null ? null : new Date(creationDateUTC.getTime());
    }

    @Override
    public int getCapabilities() {
        return folder.getCapabilities();
    }

    @Override
    public int getDeleted() {
        return folder.getDeleted();
    }

    @Override
    public int getNew() {
        return folder.getNew();
    }

    @Override
    public String getSummary() {
        return folder.getSummary();
    }

    @Override
    public int getTotal() {
        if (null == totalAndUnread) {
            if (folder instanceof FolderExtension) {
                totalAndUnread = ((FolderExtension) folder).getTotalAndUnread(parameters);
                if (null != totalAndUnread) {
                    return totalAndUnread[0];
                }
            }
        } else {
            return totalAndUnread[0];
        }
        return folder.getTotal();
    }

    @Override
    public int getUnread() {
        if (null == totalAndUnread) {
            if (folder instanceof FolderExtension) {
                totalAndUnread = ((FolderExtension) folder).getTotalAndUnread(parameters);
                if (null != totalAndUnread) {
                    return totalAndUnread[1];
                }
            }
        } else {
            return totalAndUnread[1];
        }
        return folder.getUnread();
    }

    @Override
    public boolean isDefault() {
        return null == deefault ? folder.isDefault() : deefault.booleanValue();
    }

    @Override
    public void setCapabilities(final int capabilities) {
        folder.setCapabilities(capabilities);
    }

    @Override
    public void setDefault(final boolean deefault) {
        this.deefault = Boolean.valueOf(deefault);
    }

    @Override
    public void setDeleted(final int deleted) {
        folder.setDeleted(deleted);
    }

    @Override
    public void setDefaultType(final int defaultType) {
        this.defaultType = Integer.valueOf(defaultType);
    }

    @Override
    public int getDefaultType() {
        return null == defaultType ? folder.getDefaultType() : defaultType.intValue();
    }

    @Override
    public void setNew(final int nu) {
        folder.setNew(nu);
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
    public void setUnread(final int unread) {
        folder.setUnread(unread);
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public void setLocale(final Locale locale) {
        this.locale = locale;
    }

    @Override
    public String getNewID() {
        throw new UnsupportedOperationException("UserizedFolderImpl.getNewID()");
    }

    @Override
    public void setNewID(final String newId) {
        throw new UnsupportedOperationException("UserizedFolderImpl.setNewID()");
    }

    @Override
    public void setProperty(final FolderField name, final Object value) {
        // Nope...
    }

    @Override
    public Map<FolderField, FolderProperty> getProperties() {
        Map<FolderField, FolderProperty> map = this.properties;
        if (null == map) {
            synchronized (this) {
                map = this.properties;
                if (null == map) {
                    if (folder instanceof ParameterizedFolder) {
                        final ParameterizedFolder parameterizedFolder = (ParameterizedFolder) folder;
                        map = parameterizedFolder.getProperties();
                    } else {
                        map = Collections.emptyMap();
                    }
                    this.properties = map;
                }
            }
        }
        return map;
    }

    @Override
    public Map<String, Object> getMeta() {
        return folder.getMeta();
    }

    @Override
    public void setMeta(Map<String, Object> meta) {
        folder.setMeta(meta);
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
    public boolean supportsAltName() {
        return folder instanceof AltNameAwareFolder && ((AltNameAwareFolder) folder).supportsAltName();
    }

    @Override
    public String getLocalizedName(final Locale locale, final boolean altName) {
        if (folder instanceof AltNameAwareFolder) {
            return ((AltNameAwareFolder) folder).getLocalizedName(locale, altName);
        }
        return getLocalizedName(locale);
    }

    @Override
    public boolean isAltNames() {
        return altNames;
    }

    @Override
    public void setAltNames(final boolean altNames) {
        this.altNames = altNames;
    }

}
