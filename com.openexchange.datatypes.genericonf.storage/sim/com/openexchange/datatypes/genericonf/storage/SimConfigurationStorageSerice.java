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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.datatypes.genericonf.storage;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.groupware.contexts.Context;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class SimConfigurationStorageSerice implements GenericConfigurationStorageService {

    private static int currentId = 0;

    public Map<Integer, Entry> entries = new HashMap<Integer, Entry>();

    public void delete(Context ctx, int id) throws GenericConfigStorageException {
        entries.remove(id);
    }

    public void delete(Connection con, Context ctx, int id) throws GenericConfigStorageException {
        entries.remove(id);
    }

    public void fill(Context ctx, int id, Map<String, Object> content, DynamicFormDescription form) throws GenericConfigStorageException {
        if (!entries.containsKey(id)) {
            return;
        }
        content.putAll(entries.get(id).content);
        for (FormElement formElement : entries.get(id).form.getFormElements()) {
            form.add(formElement);
        }
    }

    public void fill(Connection con, Context ctx, int id, Map<String, Object> content, DynamicFormDescription form) throws GenericConfigStorageException {
        if (!entries.containsKey(id)) {
            return;
        }
        content.putAll(entries.get(id).content);
        for (FormElement formElement : entries.get(id).form.getFormElements()) {
            form.add(formElement);
        }
    }

    public int save(Context ctx, Map<String, Object> content, DynamicFormDescription form) throws GenericConfigStorageException {
        int id = currentId++;
        Entry entry = new Entry(content, form);
        entries.put(id, entry);
        return id;
    }

    public int save(Connection con, Context ctx, Map<String, Object> content, DynamicFormDescription form) throws GenericConfigStorageException {
        int id = currentId++;
        Entry entry = new Entry(content, form);
        entries.put(id, entry);
        return id;
    }

    public void update(Context ctx, int id, Map<String, Object> content, DynamicFormDescription form) throws GenericConfigStorageException {
        Entry entry = new Entry(content, form);
        entries.put(id, entry);
    }

    public void update(Connection con, Context ctx, int id, Map<String, Object> content, DynamicFormDescription form) throws GenericConfigStorageException {
        Entry entry = new Entry(content, form);
        entries.put(id, entry);
    }

    private class Entry {

        public Map<String, Object> content;

        public DynamicFormDescription form;

        public Entry(Map<String, Object> content, DynamicFormDescription form) {
            this.content = content;
            this.form = form;
        }
    }
}
