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

package com.openexchange.groupware.contexts;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * {@link SimContext}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class SimContext implements Context {

    private static final long serialVersionUID = -7118977669491491275L;
    
    private final int id;
    private final Map<String, List<String>> attributes = new HashMap<String, List<String>>();

    public SimContext(int id) {
        super();
        this.id = id;
    }

    @Override
    public int getContextId() {
        return id;
    }

    @Override
    public String[] getFileStorageAuth() {
        return null;
    }

    @Override
    public long getFileStorageQuota() {
        return 0;
    }

    @Override
    public int getFilestoreId() {
        return 0;
    }

    @Override
    public String getFilestoreName() {
        return null;
    }

    @Override
    public String[] getLoginInfo() {
        return null;
    }

    @Override
    public int getMailadmin() {
        return 0;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean isEnabled() {
        return false;
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
    public Map<String, List<String>> getAttributes() {
        return attributes;
    }

    public void setAttribute(String key, String value) {
        attributes.put(key, Collections.singletonList(value));
    }
}
