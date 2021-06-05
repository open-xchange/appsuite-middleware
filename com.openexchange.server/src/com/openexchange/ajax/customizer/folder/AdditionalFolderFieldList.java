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

import static com.openexchange.java.Autoboxing.I;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.ajax.customizer.AdditionalFieldsUtils;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.folderstorage.Folder;
import com.openexchange.tools.session.ServerSession;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * {@link AdditionalFolderFieldList}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AdditionalFolderFieldList {

	 // TODO: Track service ranking and allow fields to overwrite other fields.

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AdditionalFolderFieldList.class);

    private final TIntObjectHashMap<AdditionalFolderField> byColId = new TIntObjectHashMap<AdditionalFolderField>();
    private final Map<String, AdditionalFolderField> byName = new HashMap<String, AdditionalFolderField>();

    /**
     * Adds an additional folder field to this list.
     *
     * @param field The additional folder field
     */
    public synchronized void addField(final AdditionalFolderField field) {
        final int key = field.getColumnID();
        if (byColId.containsKey(key) || byName.containsKey(field.getColumnName())) {
            warnAboutCollision(field);
            return;
        }
        byColId.put(key, field);
        byName.put(field.getColumnName(), field);
    }

    private void warnAboutCollision(final AdditionalFolderField field) {
        LOG.warn("Collision in folder fields. Field '{}' : {} has already been taken. Ignoring second service.", field.getColumnName(), I(field.getColumnID()));
    }

    /**
     * Gets the additional folder field associated with specified column number.
     *
     * @param col The column number
     * @return The additional folder field associated with specified column number or a neutral <code>null</code> field
     */
    public AdditionalFolderField get(final int col) {
        final AdditionalFolderField additionalFolderField = byColId.get(col);
        return null == additionalFolderField ? new NullField(col) : additionalFolderField;
    }

    /**
     * Optionally gets the additional folder field associated with specified column number.
     *
     * @param col The column number
     * @return The additional folder field associated with specified column number or <code>null</code>
     */
    public AdditionalFolderField opt(final int col) {
        return byColId.get(col);
    }

    /**
     * Gets known fields.
     *
     * @return The known fields
     */
    public int[] getKnownFields() {
        return byColId.keys();
    }

    /**
     * Gets the additional folder field associated with specified column name.
     *
     * @param col The column name
     * @return The additional folder field associated with specified column name or a <code>null</code>
     */
    public AdditionalFolderField get(final String col) {
        return byName.get(col);
    }

    /**
     * Checks if an additional folder field is associated with specified column number.
     *
     * @param col The column number
     * @return <code>true</code> if an additional folder field is associated with specified column number; otherwise <code>false</code>
     */
    public boolean knows(final int col) {
        return byColId.containsKey(col);
    }

    /**
     * Checks if an additional folder field is associated with specified column name.
     *
     * @param col The column name
     * @return <code>true</code> if an additional folder field is associated with specified column name; otherwise <code>false</code>
     */
    public boolean knows(final String col) {
        return byName.containsKey(col);
    }

    /**
     * Removes the additional folder field associated with specified column number.
     *
     * @param colId The column number
     */
    public synchronized void remove(final int colId) {
        if (!knows(colId)) {
            return;
        }
        final AdditionalFolderField f = get(colId);
        byName.remove(f.getColumnName());
        byColId.remove(colId);
    }

    /**
     * A neutral <code>null</code> field implementation.
     */
    private static final class NullField implements AdditionalFolderField {

        private final int columnId;

        NullField(final int columnId) {
            super();
            this.columnId = columnId;
        }

        @Override
        public int getColumnID() {
            return columnId;
        }

        @Override
        public String getColumnName() {
            return null;
        }

        @Override
        public Object getValue(final Folder folder, final ServerSession session) {
            return null;
        }

        @Override
        public Object renderJSON(AJAXRequestData requestData, final Object value) {
            return null;
        }

        @Override
        public List<Object> getValues(List<Folder> folder, ServerSession session) {
            return AdditionalFieldsUtils.bulk(this, folder, session);
        }

    }
}
