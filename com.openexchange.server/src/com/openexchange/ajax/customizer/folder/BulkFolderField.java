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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link BulkFolderField}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class BulkFolderField implements AdditionalFolderField {

    private AdditionalFolderField delegate;

    private Map<String, Object> values = new HashMap<String, Object>();

    public BulkFolderField(AdditionalFolderField delegate) {
        this.delegate = delegate;
    }

    public int getColumnID() {
        return delegate.getColumnID();
    }

    public String getColumnName() {
        return delegate.getColumnName();
    }

    public Object getValue(FolderObject f, ServerSession session) {
        getValues(Arrays.asList(f), session);
        
        String fn = f.getFullName();
        if (fn == null) {
            fn = "" + f.getObjectID();
        }
        return values.get(fn);
    }

    public List<Object> getValues(List<FolderObject> folder, ServerSession session) {
        List<Object> v = new ArrayList<Object>(folder.size());
        List<FolderObject> fo = new ArrayList<FolderObject>(folder.size());
        for (FolderObject f : folder) {
            String fn = f.getFullName();
            if (fn == null) {
                fn = "" + f.getObjectID();
            }
            Object object = values.get(fn);
            if (object == null) {
                fo.add(f);
            } else {
                v.add(object);
            }
        }
        if (fo.isEmpty()) {
            return v;
        }
        warmUp(fo, session);
        return getValues(folder, session);
    }

    public Object renderJSON(Object value) {
        return delegate.renderJSON(value);
    }

    public void warmUp(List<FolderObject> folderObjects, ServerSession session) {
        List<Object> v = delegate.getValues(folderObjects, session);
        int i = 0;
        for (FolderObject f : folderObjects) {
            Object object = v.get(i);
            String fn = f.getFullName();
            if (fn == null) {
                fn = "" + f.getObjectID();
            }
            values.put(fn, object);
            i++;
        }
    }

}
