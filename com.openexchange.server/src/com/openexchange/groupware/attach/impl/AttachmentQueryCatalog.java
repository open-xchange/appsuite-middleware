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
        AttachmentField.FILE_ID_LITERAL
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
