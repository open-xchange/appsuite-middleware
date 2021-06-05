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

package com.openexchange.admin.plugins;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.groupware.contexts.Context;


/**
 * {@link ContextAdapter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ContextAdapter implements Context {

    private static final long serialVersionUID = -7432344176700612294L;

    private final com.openexchange.admin.rmi.dataobjects.Context rmiContext;

    /**
     * Initializes a new {@link ContextAdapter}.
     */
    public ContextAdapter(final com.openexchange.admin.rmi.dataobjects.Context rmiContext) {
        super();
        this.rmiContext = rmiContext;
    }

    @Override
    public int getContextId() {
        final Integer id = rmiContext.getId();
        return null == id ? 0 : id.intValue();
    }

    @Override
    public String getName() {
        return rmiContext.getName();
    }

    @Override
    public String[] getLoginInfo() {
        final Set<String> loginMappings = rmiContext.getLoginMappings();
        return null == loginMappings ? null : loginMappings.toArray(new String[loginMappings.size()]);
    }

    @Override
    public int getMailadmin() {
        return -1;
    }

    @Override
    public String[] getFileStorageAuth() {
        return null;
    }

    @Override
    public long getFileStorageQuota() {
        return -1L;
    }

    @Override
    public int getFilestoreId() {
        final Integer filestoreId = rmiContext.getFilestoreId();
        return null == filestoreId ? 0 : filestoreId.intValue();
    }

    @Override
    public boolean isEnabled() {
        return rmiContext.getEnabled().booleanValue();
    }

    @Override
    public boolean isUpdating() {
        return false;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public String getFilestoreName() {
        return rmiContext.getFilestore_name();
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        return Collections.emptyMap();
    }

}
