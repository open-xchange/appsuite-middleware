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

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link BulkAdditionalFolderFieldsList}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class BulkAdditionalFolderFieldsList extends AdditionalFolderFieldList {

    private final AdditionalFolderFieldList delegate;

    private final TIntObjectMap<BulkFolderField> fieldMap = new TIntObjectHashMap<BulkFolderField>();
    private final Map<String, BulkFolderField> fieldMap2 = new HashMap<String, BulkFolderField>();
    private final Set<BulkFolderField> fields = new HashSet<BulkFolderField>();

    public BulkAdditionalFolderFieldsList(final AdditionalFolderFieldList fields) {
        this.delegate = fields;
    }

    @Override
    public void addField(final AdditionalFolderField field) {
        delegate.addField(field);
    }

    @Override
    public boolean equals(final Object obj) {
        return delegate.equals(obj);
    }

    @Override
    public AdditionalFolderField get(final int col) {
        final BulkFolderField bulkFolderField = fieldMap.get(col);
        if (bulkFolderField != null) {
            return bulkFolderField;
        }
        return cache(delegate.get(col));
    }

    @Override
    public AdditionalFolderField get(final String col) {
        final BulkFolderField bulkFolderField = fieldMap2.get(col);
        if (bulkFolderField != null) {
            return bulkFolderField;
        }
        return cache(delegate.get(col));
    }

    private AdditionalFolderField cache(final AdditionalFolderField additionalFolderField) {
        final BulkFolderField bff = new BulkFolderField(additionalFolderField);
        fieldMap.put(bff.getColumnID(), bff);
        fieldMap2.put(bff.getColumnName(), bff);
        fields.add(bff);
        return bff;
    }

    public void warmUp(final Collection<FolderObject> folders, final ServerSession session) {
        final List<FolderObject> folderObjects = new ArrayList<FolderObject>(folders);
        for(final BulkFolderField field : fields) {
            field.warmUp(folderObjects, session);
        }
    }

    @Override
    public int[] getKnownFields() {
        return delegate.getKnownFields();
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean knows(final int col) {
        return delegate.knows(col);
    }

    @Override
    public boolean knows(final String col) {
        return delegate.knows(col);
    }

    @Override
    public void remove(final int colId) {
        delegate.remove(colId);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }



}
