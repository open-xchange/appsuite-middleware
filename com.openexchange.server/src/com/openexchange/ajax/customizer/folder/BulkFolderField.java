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

package com.openexchange.ajax.customizer.folder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.folderstorage.Folder;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link BulkFolderField}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class BulkFolderField implements AdditionalFolderField {

    /** The default initial capacity - MUST be a power of two. */
    private static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16

    private static final Object NULL = new Object();

    private final AdditionalFolderField delegate;
    private final Map<Object, Object> values;

    /**
     * Initializes a new {@link BulkFolderField}.
     *
     * @param delegate The delegate field
     */
    public BulkFolderField(AdditionalFolderField delegate) {
        this(delegate, -1);
    }

    /**
     * Initializes a new {@link BulkFolderField}.
     *
     * @param delegate The delegate field
     * @param initialCapacity The initial cache capacity
     */
    public BulkFolderField(AdditionalFolderField delegate, int initialCapacity) {
        super();
        values = initialCapacity < 0 ? new HashMap<Object, Object>(DEFAULT_INITIAL_CAPACITY, 0.9F) : new HashMap<Object, Object>(initialCapacity, 0.9F);
        this.delegate = delegate;
    }

    @Override
    public int getColumnID() {
        return delegate.getColumnID();
    }

    @Override
    public String getColumnName() {
        return delegate.getColumnName();
    }

    @Override
    public Object getValue(Folder f, ServerSession session) {
        Object key = f.getID();
        Object value = null != key ? values.get(key) : null;
        if (null == value) {
            // Not yet contained in cache
            value = getValues(Collections.singletonList(f), session).get(0);
        }
        return NULL == value ? null : value;
    }

    @Override
    public List<Object> getValues(List<Folder> folders, ServerSession session) {
        if (values.isEmpty()) {
            return warmUp(folders, session);
        }

        List<Folder> fl = new ArrayList<Folder>(folders.size());
        for (Folder f : folders) {
            Object key = f.getID();
            if (null != key && !values.containsKey(key)) {
                fl.add(f);
            }
        }
        if (!fl.isEmpty()) {
            warmUp(fl, session);
        }
        List<Object> vals = new ArrayList<Object>(folders.size());
        for (Folder f : folders) {
            Object key = f.getID();
            Object value = null != key ? values.get(key) : null;
            if (null == value) {
                value = delegate.getValue(f, session);
            }
            vals.add(NULL == value ? null : value);
        }
        return vals;
    }

    @Override
    public Object renderJSON(AJAXRequestData requestData, Object value) {
        return delegate.renderJSON(requestData, value);
    }

    /**
     * Loads the values for specified folders and puts resulting values into cache.
     *
     * @param folders The folders
     * @param session The session
     */
    public List<Object> warmUp(List<Folder> folders, ServerSession session) {
        List<Object> vals = delegate.getValues(folders, session);
        int i = 0;
        for (Folder f : folders) {
            Object key = f.getID();
            Object value = vals.get(i++);
            if (null != key) {
                values.put(key, null == value ? NULL : value);
            }
        }
        return vals;
    }

}
