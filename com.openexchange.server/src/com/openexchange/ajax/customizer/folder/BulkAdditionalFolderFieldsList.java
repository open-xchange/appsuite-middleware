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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.folderstorage.Folder;
import com.openexchange.tools.session.ServerSession;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;


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

    public void warmUp(final Collection<Folder> folders, final ServerSession session) {
        final List<Folder> folderObjects = new ArrayList<Folder>(folders);
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
