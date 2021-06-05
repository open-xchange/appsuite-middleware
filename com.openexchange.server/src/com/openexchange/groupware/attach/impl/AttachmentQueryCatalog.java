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

package com.openexchange.groupware.attach.impl;

import java.util.List;
import com.google.common.base.Strings;
import com.openexchange.groupware.attach.AttachmentField;
import com.openexchange.groupware.attach.AttachmentMetadata;

public class AttachmentQueryCatalog {

    private static final AttachmentField[] DB_FIELDS = {
        AttachmentField.CREATED_BY_LITERAL,
        AttachmentField.CREATION_DATE_LITERAL,
        AttachmentField.FILE_MIMETYPE_LITERAL,
        AttachmentField.FILE_SIZE_LITERAL,
        AttachmentField.FILENAME_LITERAL,
        AttachmentField.ATTACHED_ID_LITERAL,
        AttachmentField.MODULE_ID_LITERAL,
        AttachmentField.RTF_FLAG_LITERAL,
        AttachmentField.ID_LITERAL,
        AttachmentField.COMMENT_LITERAL,
        AttachmentField.FILE_ID_LITERAL,
        AttachmentField.CHECKSUM_LITERAL
    };

    private static final String REMEMBER_DEL = "INSERT INTO del_attachment (id, del_date, cid, attached, module) VALUES (?,?,?,?,?)";

    private static final String INSERT;
    private static final String UPDATE;
    private static final String FIELDS;
    private static final String SELECT_BY_ID;
    private static final String SELECT_NEWEST_CREATION_DATE = "SELECT attached,MAX(creation_date) AS creation_date FROM prg_attachment WHERE cid=? AND module=? AND attached IN (";
    private static final String SELECT_FILE_ID = "SELECT file_id FROM prg_attachment WHERE id = ? AND cid = ? ";

    static {
        final StringBuilder updateBuffer = new StringBuilder("UPDATE prg_attachment SET ");
        final StringBuilder insertBuffer = new StringBuilder("INSERT INTO prg_attachment (");
        final StringBuilder questionMarks = new StringBuilder();
        final StringBuilder fieldsBuffer = new StringBuilder();
        final StringBuilder selectByIdBuffer = new StringBuilder("SELECT ");

        for(final AttachmentField field : DB_FIELDS) {
            fieldsBuffer.append(field.getName());
            fieldsBuffer.append(", ");

            questionMarks.append("?, ");
            updateBuffer.append(field);
            updateBuffer.append(" = ?, ");
        }
        updateBuffer.setLength(updateBuffer.length()-2);
        fieldsBuffer.append("cid");
        questionMarks.append('?');
        updateBuffer.append("WHERE cid = ? AND id = ?");

        insertBuffer.append(fieldsBuffer);
        insertBuffer.append(") VALUES ( ");
        insertBuffer.append(questionMarks);
        insertBuffer.append(')');


        INSERT = insertBuffer.toString();
        FIELDS = fieldsBuffer.toString();
        UPDATE = updateBuffer.toString();

        selectByIdBuffer.append(FIELDS);
        selectByIdBuffer.append(" FROM prg_attachment WHERE id = ? AND cid = ?");

        SELECT_BY_ID = selectByIdBuffer.toString();
    }

    public AttachmentField[] getFields() {
        return DB_FIELDS;
    }

    public String getInsert() {
        return INSERT;
    }

    public String getDelete(final String tablename, final List<AttachmentMetadata> attachments) {
        final StringBuilder builder = new StringBuilder("DELETE FROM ").append(tablename).append(" WHERE id IN (");
        for(final AttachmentMetadata m : attachments) {
            builder.append(m.getId()).append(',');
        }
        builder.deleteCharAt(builder.length()-1);
        builder.append(") and cid = ?");
        return builder.toString();
    }

    public String getInsertIntoDel(){
        return REMEMBER_DEL;
    }

    public String getUpdate() {
        return UPDATE;
    }

    public String getSelectFileId() {
        return SELECT_FILE_ID;
    }

    public void appendColumnList(final StringBuilder select, final AttachmentField[] columns) {
        appendColumnListWithPrefix(select, columns, null);
    }

    public void appendColumnListWithPrefix(final StringBuilder select, final AttachmentField[] columns, String prefix) {
        prefix = Strings.isNullOrEmpty(prefix) ? "" : prefix + ".";
        for(final AttachmentField field : columns ) {
            select.append(prefix).append(field);
            select.append(',');
        }
        select.setLength(select.length()-1);
    }

    public String getSelectById() {
        return SELECT_BY_ID;
    }

    public String getSelectNewestCreationDate() {
        return SELECT_NEWEST_CREATION_DATE;
    }

}
