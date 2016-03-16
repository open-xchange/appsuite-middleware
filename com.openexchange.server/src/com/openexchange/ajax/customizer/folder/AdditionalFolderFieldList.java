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

import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.ajax.customizer.AdditionalFieldsUtils;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.tools.session.ServerSession;

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
        LOG.warn("Collision in folder fields. Field '{}' : {} has already been taken. Ignoring second service.", field.getColumnName(), field.getColumnID());
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
        public Object getValue(final FolderObject folder, final ServerSession session) {
            return null;
        }

        @Override
        public Object renderJSON(AJAXRequestData requestData, final Object value) {
            return null;
        }

        @Override
        public List<Object> getValues(List<FolderObject> folder, ServerSession session) {
            return AdditionalFieldsUtils.bulk(this, folder, session);
        }

    }
}
