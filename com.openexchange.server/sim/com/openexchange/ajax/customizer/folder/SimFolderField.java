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

import java.util.List;
import com.openexchange.ajax.customizer.AdditionalFieldsUtils;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.folderstorage.Folder;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link SimFolderField}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class SimFolderField implements AdditionalFolderField {

    private int columnId;
    private String columnName;
    private Object value;
    private Object jsonValue;

    @Override
    public int getColumnID() {
        return columnId;
    }

    @Override
    public String getColumnName() {
        return columnName;
    }

    @Override
    public Object getValue(Folder folder, ServerSession session) {
        return value;
    }

    @Override
    public Object renderJSON(AJAXRequestData requestData, Object value) {
        return jsonValue;
    }

    public void setColumnId(int columnId) {
        this.columnId = columnId;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setJsonValue(Object jsonValue) {
        this.jsonValue = jsonValue;
    }

    @Override
    public List<Object> getValues(List<Folder> folder, ServerSession session) {
        return AdditionalFieldsUtils.bulk(this, folder, session);
    }

}
