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

package com.openexchange.ajax.customizer.folder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.groupware.container.FolderObject;
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
    public Object getValue(FolderObject f, ServerSession session) {
        Object key = f.getFullName();
        Object value = values.get(key == null ? Integer.valueOf(f.getObjectID()) : key);
        if (null == value) {
            // Not yet contained in cache
            value = getValues(Collections.singletonList(f), session).get(0);
        }
        return NULL == value ? null : value;
    }

    @Override
    public List<Object> getValues(List<FolderObject> folders, ServerSession session) {
        if (values.isEmpty()) {
            return warmUp(folders, session);
        }

        List<FolderObject> fl = new ArrayList<FolderObject>(folders.size());
        for (FolderObject f : folders) {
            Object key = f.getFullName();
            if (!values.containsKey(key == null ? Integer.valueOf(f.getObjectID()) : key)) {
                fl.add(f);
            }
        }
        if (!fl.isEmpty()) {
            warmUp(fl, session);
        }
        List<Object> vals = new ArrayList<Object>(folders.size());
        for (FolderObject f : folders) {
            Object key = f.getFullName();
            Object value = values.get(key == null ? Integer.valueOf(f.getObjectID()) : key);
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
    public List<Object> warmUp(List<FolderObject> folders, ServerSession session) {
        List<Object> vals = delegate.getValues(folders, session);
        int i = 0;
        for (FolderObject f : folders) {
            Object key = f.getFullName();
            Object value = vals.get(i++);
            values.put(key == null ? Integer.valueOf(f.getObjectID()) : key, null == value ? NULL : value);
        }
        return vals;
    }

}
