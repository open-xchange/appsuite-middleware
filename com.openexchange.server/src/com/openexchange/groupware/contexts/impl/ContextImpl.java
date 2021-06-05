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

package com.openexchange.groupware.contexts.impl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link ContextImpl} - The implementation of {@link ContextExtended}.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class ContextImpl implements ContextExtended {

    private static final long serialVersionUID = 8570995404471786200L;

    private final int contextId;
    private String name;
    private String[] loginInfo;
    private int mailadmin = -1;
    private int filestoreId = -1;
    private String filestoreName;
    private String[] filestorageAuth;
    private long fileStorageQuota;
    private boolean enabled = true;
    private boolean updating = false;
    private boolean readOnly = false;
    private boolean updateNeeded = false;
    private final Map<String, List<String>> attributes;

    public ContextImpl(final int contextId) {
        super();
        this.contextId = contextId;
        attributes = new ConcurrentHashMap<String, List<String>>();
    }

    @Override
    public int getContextId() {
        return contextId;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof ContextImpl)) {
            return false;
        }
        return contextId == ((ContextImpl) obj).contextId;
    }

    @Override
    public int hashCode() {
        return contextId;
    }

    @Override
    public String toString() {
        return "ContextImpl cid: " + contextId;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getMailadmin() {
        return mailadmin;
    }

    @Override
    public long getFileStorageQuota() {
        return fileStorageQuota;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setMailadmin(final int mailadmin) {
        this.mailadmin = mailadmin;
    }

    public void setFileStorageQuota(final long fileStorageQuota) {
        this.fileStorageQuota = fileStorageQuota;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public int getFilestoreId() {
        return filestoreId;
    }

    public void setFilestoreId(final int filestoreId) {
        this.filestoreId = filestoreId;
    }

    @Override
    public String getFilestoreName() {
        return filestoreName;
    }

    public void setFilestoreName(final String filestoreName) {
        this.filestoreName = filestoreName;
    }

    public void setFilestoreAuth(final String[] filestoreAuth) {
        this.filestorageAuth = filestoreAuth;
    }

    @Override
    public String[] getFileStorageAuth() {
        return filestorageAuth.clone();
    }

    @Override
    public String[] getLoginInfo() {
        return loginInfo.clone();
    }

    public void setLoginInfo(final String[] loginInfo) {
        this.loginInfo = loginInfo.clone();
    }

    @Override
    public void setUpdating(final boolean updating) {
        this.updating = updating;
    }

    @Override
    public boolean isUpdating() {
        return updating;
    }

    @Override
    public boolean isUpdateNeeded() {
        return updateNeeded;
    }

    @Override
    public void setUpdateNeeded(boolean updateNeeded) {
        this.updateNeeded = updateNeeded;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;

    }

    @Override
    public Map<String, List<String>> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    /**
     * Adds given attribute.
     *
     * @param attrName The name
     * @param value The value
     */
    public void addAttribute(String attrName, String value) {
        List<String> list = attributes.get(attrName);
        if (list == null) {
            list = new LinkedList<String>();
            attributes.put(attrName, list);
        }
        if (!list.contains(value)) {
            list.add(value);
        }
    }

}
