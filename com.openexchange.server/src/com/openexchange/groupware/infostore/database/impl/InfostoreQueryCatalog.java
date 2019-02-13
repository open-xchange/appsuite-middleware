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

package com.openexchange.groupware.infostore.database.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.google.common.collect.ImmutableSet;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.infostore.utils.MetadataSwitcher;

public class InfostoreQueryCatalog {

    private static final InfostoreQueryCatalog INSTANCE = new InfostoreQueryCatalog();

    /**
     * Gets the query catalog instance.
     *
     * @return The instance.
     */
    public static InfostoreQueryCatalog getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link InfostoreQueryCatalog}.
     */
    private InfostoreQueryCatalog() {
        super();
    }

    private static final String SQL_CHUNK05 = " AND infostore.last_modified > ";

    private static final String SQL_CHUNK04 = " FROM infostore JOIN infostore_document ON infostore.cid = ";

    private static final String SQL_CHUNK03 = " AND infostore_document.cid = ";

    private static final String SQL_CHUNK02 = " AND infostore.created_by = ";

    private static final String SQL_CHUNK01 = " AND infostore.version = infostore_document.version_number AND infostore.id = infostore_document.infostore_id WHERE infostore.folder_id = ";

    private static final String STR_SELECT = "SELECT ";

    private static final String STR_ORDER_BY = " ORDER BY ";

    private static final String STR_LIMIT = " LIMIT ";

    private static final String STR_LAST_MODIFIED = "last_modified";

    private static final String STR_CID = "cid";

    public static final Metadata[] INFOSTORE_FIELDS = new Metadata[] {
        Metadata.ID_LITERAL, Metadata.FOLDER_ID_LITERAL, Metadata.VERSION_LITERAL, Metadata.COLOR_LABEL_LITERAL,
        Metadata.CREATION_DATE_LITERAL, Metadata.SEQUENCE_NUMBER_LITERAL, Metadata.CREATED_BY_LITERAL, Metadata.MODIFIED_BY_LITERAL,
        Metadata.LAST_MODIFIED_UTC_LITERAL, Metadata.ORIGIN_LITERAL };

    public static final Set<Metadata> INFOSTORE_FIELDS_SET = ImmutableSet.copyOf(INFOSTORE_FIELDS);

    public static final Metadata[] DEL_INFOSTORE_FIELDS = new Metadata[] {
        Metadata.ID_LITERAL, Metadata.FOLDER_ID_LITERAL, Metadata.VERSION_LITERAL,
        Metadata.CREATION_DATE_LITERAL, Metadata.SEQUENCE_NUMBER_LITERAL, Metadata.CREATED_BY_LITERAL, Metadata.MODIFIED_BY_LITERAL,
        Metadata.LAST_MODIFIED_UTC_LITERAL, Metadata.COLOR_LABEL_LITERAL, Metadata.ORIGIN_LITERAL };

    public static final Set<Metadata> DEL_INFOSTORE_FIELDS_SET = ImmutableSet.copyOf(DEL_INFOSTORE_FIELDS);

    public static final Metadata[] INFOSTORE_DOCUMENT_FIELDS = new Metadata[] {
        Metadata.ID_LITERAL, Metadata.VERSION_LITERAL, Metadata.CREATION_DATE_LITERAL, Metadata.LAST_MODIFIED_LITERAL,
        Metadata.CREATED_BY_LITERAL, Metadata.MODIFIED_BY_LITERAL, Metadata.TITLE_LITERAL, Metadata.URL_LITERAL,
        Metadata.DESCRIPTION_LITERAL, Metadata.CATEGORIES_LITERAL, Metadata.FILENAME_LITERAL, Metadata.FILE_SIZE_LITERAL,
        Metadata.FILE_MIMETYPE_LITERAL, Metadata.FILE_MD5SUM_LITERAL, Metadata.VERSION_COMMENT_LITERAL,
        Metadata.FILESTORE_LOCATION_LITERAL, Metadata.LAST_MODIFIED_UTC_LITERAL, Metadata.META_LITERAL,
        Metadata.CAPTURE_DATE_LITERAL, Metadata.GEOLOCATION_LITERAL, Metadata.WIDTH_LITERAL, Metadata.HEIGHT_LITERAL,
        Metadata.CAMERA_MODEL_LITERAL, Metadata.CAMERA_ISO_SPEED_LITERAL, Metadata.CAMERA_APERTURE_LITERAL,
        Metadata.CAMERA_EXPOSURE_TIME_LITERAL, Metadata.CAMERA_FOCAL_LENGTH_LITERAL, Metadata.MEDIA_META_LITERAL,
        Metadata.MEDIA_STATUS_LITERAL };

    public static final Set<Metadata> INFOSTORE_DOCUMENT_FIELDS_SET = ImmutableSet.copyOf(INFOSTORE_DOCUMENT_FIELDS);

    public static final Metadata[] DEL_INFOSTORE_DOCUMENT_FIELDS = new Metadata[] {
        Metadata.ID_LITERAL, Metadata.VERSION_LITERAL, Metadata.CREATION_DATE_LITERAL, Metadata.LAST_MODIFIED_LITERAL,
        Metadata.CREATED_BY_LITERAL, Metadata.MODIFIED_BY_LITERAL
    };

    public static final Set<Metadata> DEL_INFOSTORE_DOCUMENT_FIELDS_SET = ImmutableSet.copyOf(DEL_INFOSTORE_DOCUMENT_FIELDS);

    public static final Set<Metadata> IGNORE_ON_WRITE = ImmutableSet.of(Metadata.LAST_MODIFIED_UTC_LITERAL, Metadata.SEQUENCE_NUMBER_LITERAL, Metadata.MEDIA_DATE_LITERAL);

    /**
     * Filters non-writable fields from given field array
     *
     * @param fields The field array to filter
     * @return The possibly filtered field array
     */
    public Metadata[] filterWritable(Metadata[] fields) {
        List<Metadata> list = null;
        for (int i = 0; i < fields.length; i++) {
            Metadata field = fields[i];
            if (IGNORE_ON_WRITE.contains(field)) {
                // Current field must not be modified
                if (null == list) {
                    list = createListFor(fields, i);
                }
            } else {
                if (null != list) {
                    list.add(field);
                }
            }
        }
        return null == list ? fields : list.toArray(new Metadata[list.size()]);
    }

    /**
     * Creates a list carrying the elements from given field array until specified end index (exclusive).
     *
     * @param fields The field array
     * @param end The end index (exclusive)
     * @return The resulting list
     */
    private List<Metadata> createListFor(Metadata[] fields, int end) {
        List<Metadata> list = new ArrayList<Metadata>(fields.length);
        if (end > 0) {
            for (int j = 0; j < end; j++) {
                list.add(fields[j]);
            }
        }
        return list;
    }


    public static enum Table {
        INFOSTORE(INFOSTORE_FIELDS, INFOSTORE_FIELDS_SET, "infostore"),
        INFOSTORE_DOCUMENT(INFOSTORE_DOCUMENT_FIELDS, INFOSTORE_DOCUMENT_FIELDS_SET, "infostore_document"),
        DEL_INFOSTORE(DEL_INFOSTORE_FIELDS, DEL_INFOSTORE_FIELDS_SET, "del_infostore"),
        DEL_INFOSTORE_DOCUMENT(DEL_INFOSTORE_DOCUMENT_FIELDS, DEL_INFOSTORE_DOCUMENT_FIELDS_SET, "del_infostore_document");

        private final String tablename;

        private final Set<Metadata> fieldSet;

        private final Metadata[] fields;

        private Table(final Metadata[] fields, final Set<Metadata> fieldSet, final String tablename) {
            this.fields = fields;
            this.fieldSet = fieldSet;
            this.tablename = tablename;
        }

        public Metadata[] getFields() {
            return fields;
        }

        public Set<Metadata> getFieldSet() {
            return fieldSet;
        }

        public String getTablename() {
            return tablename;
        }

        public MetadataSwitcher getFieldSwitcher() {
            switch (this) {
                case INFOSTORE:
                    return InfostoreColumnsSwitch.getInstance();
                case DEL_INFOSTORE:
                    return DelInfostoreColumnsSwitch.getInstance();
                case INFOSTORE_DOCUMENT:
                    return InfostoreDocumentColumnsSwitch.getInstance();
                case DEL_INFOSTORE_DOCUMENT:
                    return DelInfostoreDocumentColumnsSwitch.getInstance();
                default:
                    throw new IllegalArgumentException("Will not happen");
            }
        }
    }

    private static String buildInsert(final String tablename, final Metadata[] metadata, final MetadataSwitcher columnNames, final String... additionalFields) {
        final StringBuilder builder = new StringBuilder(128);
        builder.append("INSERT INTO ").append(tablename).append(" (");
        final StringBuilder questionMarks = new StringBuilder(64);

        for (final Metadata m : metadata) {
            if (IGNORE_ON_WRITE.contains(m)) {
                continue;
            }
            final String col = (String) m.doSwitch(columnNames);
            if (col != null) {
                builder.append(col);
                builder.append(',');
                if (Metadata.GEOLOCATION_LITERAL == m) {
                    // See https://dev.mysql.com/doc/refman/8.0/en/populating-spatial-columns.html
                    questionMarks.append("GeomFromText(?),");
                } else {
                    questionMarks.append("?,");
                }
            }
        }

        for (final String s : additionalFields) {
            builder.append(s);
            builder.append(',');

            questionMarks.append("?,");
        }

        builder.setLength(builder.length() - 1);
        questionMarks.setLength(questionMarks.length() - 1);

        builder.append(") VALUES (").append(questionMarks).append(')');

        return builder.toString();
    }

    private static String buildInsert(final Table t, final String... additionalFields) {
        return buildInsert(t.getTablename(), t.getFields(), t.getFieldSwitcher(), additionalFields);
    }

    private static StringBuilder buildUpdateWithoutWhere(final Table table, final Metadata[] metadata, final MetadataSwitcher columnNames, final String... additionalFields) {
        final StringBuilder builder = new StringBuilder(128);
        builder.append("UPDATE ").append(table.getTablename()).append(" SET ");
        for (final Metadata m : metadata) {
            if (m == Metadata.VERSION_LITERAL && (table == Table.INFOSTORE_DOCUMENT || table == Table.DEL_INFOSTORE_DOCUMENT)) {
                continue;
            }
            if (IGNORE_ON_WRITE.contains(m)) {
                continue;
            }
            final String col = (String) m.doSwitch(columnNames);
            if (col != null) {
                builder.append(col);
                if (Metadata.GEOLOCATION_LITERAL == m) {
                    // See https://dev.mysql.com/doc/refman/8.0/en/populating-spatial-columns.html
                    builder.append(" = GeomFromText(?),");
                } else {
                    builder.append(" = ?,");
                }
            }
        }

        for (final String s : additionalFields) {
            builder.append(s);
            builder.append(" = ?,");
        }

        builder.setLength(builder.length() - 1);
        return builder;
    }

    private static final String INSERT_INFOSTORE = buildInsert(Table.INFOSTORE, STR_LAST_MODIFIED, STR_CID);

    private static final String INSERT_INFOSTORE_DOCUMENT = buildInsert(Table.INFOSTORE_DOCUMENT, STR_CID);

    private static final String INSERT_DEL_INFOSTORE = buildInsert(Table.DEL_INFOSTORE, STR_LAST_MODIFIED, STR_CID);

    public List<String> getDelete(final Table t, final List<DocumentMetadata> documents) {
        return getDelete(t, documents, true);
    }

    public List<String> getDelete(final Table t, final List<DocumentMetadata> documents, boolean includeVersions) {
        switch (t) {
            default:
                break;
            case INFOSTORE_DOCUMENT:
            case DEL_INFOSTORE_DOCUMENT:
                throw new IllegalArgumentException("getDelete is only applicable for the non version tables infostore and del_infostore");
        }
        final int size = documents.size();
        final List<String> l = new ArrayList<>(2);
        // Versions
        if (includeVersions) {
            final Table versionTable = Table.INFOSTORE.equals(t) ? Table.INFOSTORE_DOCUMENT : Table.DEL_INFOSTORE_DOCUMENT;
            final StringBuilder delete = new StringBuilder("DELETE FROM ").append(versionTable.getTablename()).append(" WHERE ").append(
                Metadata.ID_LITERAL.doSwitch(versionTable.getFieldSwitcher())).append(" IN (");
            delete.append(documents.get(0).getId());
            for (int i = 1; i < size; i++) {
                delete.append(',').append(documents.get(i).getId());
            }
            delete.append(") AND cid = ?");
            l.add(delete.toString());
        }
        // Documents
        final StringBuilder delete = new StringBuilder("DELETE FROM ").append(t.getTablename()).append(" WHERE ").append(
            Metadata.ID_LITERAL.doSwitch(t.getFieldSwitcher())).append(" IN (");
        delete.append(documents.get(0).getId());
        for (int i = 1; i < size; i++) {
            delete.append(',').append(documents.get(i).getId());
        }
        delete.append(") AND cid = ?");
        l.add(delete.toString());
        return l;
    }

    public List<String> getSingleDelete(final Table t) {
        switch (t) {
            default:
                break;
            case INFOSTORE_DOCUMENT:
            case DEL_INFOSTORE_DOCUMENT:
                throw new IllegalArgumentException("getDelete is only applicable for the non version tables infostore and del_infostore");
        }
        final List<String> l = new ArrayList<>(2);
        // Versions
        {
            final Table versionTable = Table.INFOSTORE.equals(t) ? Table.INFOSTORE_DOCUMENT : Table.DEL_INFOSTORE_DOCUMENT;
            final StringBuilder delete = new StringBuilder("DELETE FROM ").append(versionTable.getTablename()).append(" WHERE ").append(
                Metadata.ID_LITERAL.doSwitch(versionTable.getFieldSwitcher())).append("  = ? AND cid = ?");
            l.add(delete.toString());
        }
        // Document
        final StringBuilder delete = new StringBuilder("DELETE FROM ").append(t.getTablename()).append(" WHERE ").append(
            Metadata.ID_LITERAL.doSwitch(t.getFieldSwitcher())).append("  = ? AND cid = ?");
        l.add(delete.toString());
        return l;
    }

    /**
     * Constructs an REPLACE INTO statement useful to insert multiple entries into the del_* tables at once, overwriting already
     * existing entries if needed automatically.
     *
     * @param table The table to replace into
     * @param count The number of rows to prepare parameter placeholders for. For each row, the number of writable fields are prepared,
     *        additionally a paramter for the context ID is for each row.
     * @return The statement string
     */
    public String getReplace(Table table, int count, final String...additional) {
        if (1 > count) {
            throw new IllegalArgumentException("need at least one item to create statement");
        }
        Metadata[] fields = table.getFields();
        MetadataSwitcher switcher = table.getFieldSwitcher();
        StringBuilder questionMarksAllocator = new StringBuilder().append('(');
        StringBuilder allocator = new StringBuilder("REPLACE INTO ").append(table.getTablename()).append(" (");
        for (int i = 0; i < fields.length; i++) {
            if (IGNORE_ON_WRITE.contains(fields[i])) {
                continue;
            }

            Object sqlField = fields[i].doSwitch(switcher);
            if (sqlField != null) {
                if (Metadata.GEOLOCATION_LITERAL == fields[i]) {
                    // See https://dev.mysql.com/doc/refman/8.0/en/populating-spatial-columns.html
                    questionMarksAllocator.append("GeomFromText(?),");
                } else {
                    questionMarksAllocator.append("?,");
                }
                allocator.append(sqlField).append(',');
            }
        }
        boolean multipleAdditional = false;
        for (int i = 0; i < additional.length; i++) {
            if (multipleAdditional) {
                questionMarksAllocator.append(',');
            }
            questionMarksAllocator.append('?');
            multipleAdditional = true;
        }
        questionMarksAllocator.append(')');
        multipleAdditional = false;
        for (int i = 0; i < additional.length; i++) {
            if (multipleAdditional) {
                allocator.append(',');
            }
            allocator.append(additional[i]);
            multipleAdditional = true;
        }
        allocator.append(") VALUES ").append(questionMarksAllocator);
        for (int i = 1; i < count; i++) {
            allocator.append(',').append(questionMarksAllocator);
        }
        allocator.append(';');
        return allocator.toString();
    }


    public String getDocumentInsert() {
        return INSERT_INFOSTORE;
    }

    public String getDelDocumentInsert() {
        return INSERT_DEL_INFOSTORE;
    }

    public String getDocumentUpdate(final Metadata[] fields) {
        return buildUpdateWithoutWhere(Table.INFOSTORE, fields, Table.INFOSTORE.getFieldSwitcher(), STR_LAST_MODIFIED).append(
            " WHERE cid = ? and id = ? and last_modified <= ?").toString();
    }

    public String getNumberOfVersionsQueryForOneDocument() {
        // GROUP BY CLAUSE: ensure ONLY_FULL_GROUP_BY compatibility
        final Table table = Table.INFOSTORE_DOCUMENT;
        final String idColumn = (String) Metadata.ID_LITERAL.doSwitch(table.getFieldSwitcher());
        final StringBuilder builder = new StringBuilder(200);
        builder.append("SELECT COUNT(infostore_id) AS number_of_versions FROM infostore_document WHERE ").append(idColumn).append(" = ? ").append(
            "AND cid = ? GROUP BY infostore_id");
        return builder.toString();
    }

    /**
     * Gets a SQL statement to retrieve the number of versions for multiple documents.
     *
     * @param ids The ids of the documents to get the versions for
     * @return The statement
     */
    public String getNumberOfVersionsQueryForDocuments(int[] ids) {
        // GROUP BY CLAUSE: ensure ONLY_FULL_GROUP_BY compatibility
        if (null == ids || 0 == ids.length) {
            throw new IllegalArgumentException("ids");
        }
        StringBuilder stringBuilder = new StringBuilder("SELECT infostore_id,COUNT(infostore_id) ")
            .append("FROM infostore_document WHERE cid=? AND infostore_id");
        if (1 == ids.length) {
            stringBuilder.append("=?");
        } else {
            stringBuilder.append(" IN (?");
            for (int i = 1; i < ids.length; i++) {
                stringBuilder.append(",?");
            }
            stringBuilder.append(')');
        }
        stringBuilder.append(" GROUP BY infostore_id;");
        return stringBuilder.toString();
    }

    public Metadata[] getDocumentFields() {
        return Table.INFOSTORE.getFields();
    }

    public Metadata[] getDelDocumentFields() {
        return Table.DEL_INFOSTORE.getFields();
    }

    public Metadata[] getWritableDocumentFields() {
        final Metadata[] fields = getDocumentFields();
        return filterWritable(fields);
    }

    public Metadata[] getWritableDelDocumentFields() {
        final Metadata[] fields = getDelDocumentFields();
        return filterWritable(fields);
    }

    public Metadata[] filterForDocument(final Metadata[] modified) {
        final List<Metadata> m = new ArrayList<>();
        final Set<Metadata> knownFields = Table.INFOSTORE.getFieldSet();
        for (final Metadata metadata : modified) {
            if (knownFields.contains(metadata)) {
                m.add(metadata);
            }
        }
        return m.toArray(new Metadata[m.size()]);
    }

    public boolean updateDocument(final Metadata[] modifiedColumns) {
        final Set<Metadata> fields = Table.INFOSTORE.getFieldSet();
        for (final Metadata m : modifiedColumns) {
            if (fields.contains(m)) {
                return true;
            }
        }
        return false;
    }

    public String getVersionInsert() {
        return INSERT_INFOSTORE_DOCUMENT;
    }

    public String getVersionUpdate(final Metadata[] fields) {
        return buildUpdateWithoutWhere(Table.INFOSTORE_DOCUMENT, fields, Table.INFOSTORE_DOCUMENT.getFieldSwitcher()).append(
            " WHERE cid = ? and infostore_id = ? and version_number = ? ").toString();
    }

    public Metadata[] getVersionFields() {
        return Table.INFOSTORE_DOCUMENT.getFields();
    }

    public Metadata[] getDelVersionFields() {
        return Table.DEL_INFOSTORE_DOCUMENT.getFields();
    }

    public Metadata[] getWritableVersionFields() {
        final Metadata[] fields = getVersionFields();
        return filterWritable(fields);
    }

    public Metadata[] getWritableDelVersionFields() {
        final Metadata[] fields = getDelVersionFields();
        return filterWritable(fields);
    }

    public Metadata[] filterForVersion(final Metadata[] modified) {
        final List<Metadata> m = new ArrayList<>();
        final Set<Metadata> knownFields = Table.INFOSTORE_DOCUMENT.getFieldSet();
        for (final Metadata metadata : modified) {
            if (metadata == Metadata.VERSION_LITERAL) {
                continue;
            }
            if (knownFields.contains(metadata)) {
                m.add(metadata);
            }
        }
        return m.toArray(new Metadata[m.size()]);
    }

    public boolean updateVersion(final Metadata[] modifiedColumns) {
        final Set<Metadata> fields = Table.INFOSTORE_DOCUMENT.getFieldSet();
        for (final Metadata m : modifiedColumns) {
            if (fields.contains(m)) {
                return true;
            }
        }
        return false;
    }

    public String getVersionDelete(final Table t, final List<DocumentMetadata> documents) {
        switch (t) {
            default:
                break;
            case INFOSTORE:
            case DEL_INFOSTORE:
                throw new IllegalArgumentException(
                    "getVersionDelete is only applicable for the version tables infostore_document and del_infostore_document");
        }
        final StringBuilder delete = new StringBuilder("DELETE FROM ").append(t.getTablename()).append(" WHERE ( ");
        for (final DocumentMetadata doc : documents) {
            delete.append("( ").append(Metadata.ID_LITERAL.doSwitch(t.getFieldSwitcher())).append(" = ").append(doc.getId()).append(" AND ").append(
                Metadata.VERSION_LITERAL.doSwitch(t.getFieldSwitcher())).append(" = ").append(doc.getVersion()).append(" ) OR ");
        }
        delete.setLength(delete.length() - 6);
        delete.append(") ) AND cid = ?");
        return delete.toString();
    }

    public String getSingleVersionDelete(final Table t) {
        switch (t) {
            default:
                break;
            case INFOSTORE:
            case DEL_INFOSTORE:
                throw new IllegalArgumentException(
                    "getVersionDelete is only applicable for the version tables infostore_document and del_infostore_document");
        }
        final StringBuilder delete = new StringBuilder("DELETE FROM ").append(t.getTablename()).append(" WHERE ").append(
            Metadata.ID_LITERAL.doSwitch(t.getFieldSwitcher())).append(" = ? AND ").append(
                Metadata.VERSION_LITERAL.doSwitch(t.getFieldSwitcher())).append(" = ? AND cid = ?");
        return delete.toString();
    }

    public String getAllVersionsDelete(final Table t) {
        switch (t) {
            default:
                break;
            case INFOSTORE:
            case DEL_INFOSTORE:
                throw new IllegalArgumentException(
                    "getVersionDelete is only applicable for the version tables infostore_document and del_infostore_document");
        }
        final StringBuilder delete = new StringBuilder("DELETE FROM ").append(t.getTablename()).append(" WHERE ").append(
            Metadata.ID_LITERAL.doSwitch(t.getFieldSwitcher())).append(" = ? AND cid = ?");
        return delete.toString();
    }

    public FieldChooser getChooserForVersion(final int version) {
        if (version == InfostoreFacade.CURRENT_VERSION) {
            return new DocumentWins();
        }
        return new VersionWins();
    }

    public String getSelectDocument(final int id, final int version, final int ctx_id) {
        final StringBuilder builder = new StringBuilder(STR_SELECT).append(fields(Metadata.VALUES_ARRAY, getChooserForVersion(version))).append(
            SQL_CHUNK04).append(ctx_id).append(SQL_CHUNK03).append(ctx_id).append(" AND infostore.id = infostore_document.infostore_id ");
        if (version == InfostoreFacade.CURRENT_VERSION) {
            builder.append("AND infostore_document.version_number = infostore.version");
        }
        builder.append(" WHERE infostore.id = ").append(id);
        if (version != InfostoreFacade.CURRENT_VERSION) {
            builder.append(" AND infostore_document.version_number = ").append(version);
        }
        return builder.toString();
    }

    public String getListQuery(final int[] id, final Metadata[] metadata, final FieldChooser wins, final int contextId) {
        final StringBuilder builder = new StringBuilder(STR_SELECT).append(fields(metadata, wins)).append(SQL_CHUNK04).append(contextId).append(
            SQL_CHUNK03).append(contextId).append(
                " AND infostore.version = infostore_document.version_number AND infostore.id = infostore_document.infostore_id WHERE infostore.id IN (");
        for (final int i : id) {
            builder.append(i).append(',');
        }
        builder.setLength(builder.length() - 1);
        builder.append(')');
        if (1 < id.length) {
            /*
             * ensure exact list order
             */
            builder.append(" ORDER BY FIELD(infostore.id,").append(id[0]);
            for (int i = 1; i < id.length; i++) {
                builder.append(',').append(id[i]);
            }
            builder.append(')');
        }
        return builder.toString();
    }

    public String getDocumentsQuery(final long folderId, final Metadata[] metadata, final Metadata sort, final int order, int start, int end, final FieldChooser wins, final int contextId) {
        StringBuilder builder = new StringBuilder(STR_SELECT).append(fields(metadata, wins)).append(SQL_CHUNK04).append(contextId).append(
            SQL_CHUNK03).append(contextId).append(SQL_CHUNK01).append(folderId);

        if (sort != null) {
            builder.append(STR_ORDER_BY).append(fieldName(sort, wins, true)).append(' ').append(order(order));
        }

        if (start >= 0 && end > 0) {
            builder.append(STR_LIMIT);
            if (start > 0) {
                builder.append(start).append(',');
            }
            builder.append(end - start);
        }

        return builder.toString();
    }

    public String getDocumentsQuery(final long folderId, final int userId, final Metadata[] metadata, final Metadata sort, final int order, int start, int end, final FieldChooser wins, final int contextId) {
        StringBuilder builder = new StringBuilder(STR_SELECT).append(fields(metadata, wins)).append(SQL_CHUNK04).append(contextId).append(
            SQL_CHUNK03).append(contextId).append(SQL_CHUNK01).append(folderId).append(SQL_CHUNK02).append(userId);

        if (sort != null) {
            builder.append(STR_ORDER_BY).append(fieldName(sort, wins, true)).append(' ').append(order(order));
        }

        if (start >= 0 && end > 0) {
            builder.append(STR_LIMIT);
            if (start > 0) {
                builder.append(start).append(',');
            }
            builder.append(end - start);
        }

        return builder.toString();
    }

    public String getVersionsQuery(final int id, final Metadata[] metadata, final Metadata sort, final int order, final FieldChooser wins, final int contextId) {
        final StringBuilder builder = new StringBuilder(STR_SELECT).append(fields(metadata, wins)).append(SQL_CHUNK04).append(contextId).append(
            SQL_CHUNK03).append(contextId).append(" AND infostore.id = infostore_document.infostore_id ").append(" WHERE infostore.id = ").append(
                id);
        if (sort != null) {
            builder.append(STR_ORDER_BY).append(fieldName(sort, wins, true)).append(' ').append(order(order));
        }
        return builder.toString();
    }

    public String getNewDocumentsQuery(final long folderId, final long since, final Metadata[] metadata, final Metadata sort, final int order, final FieldChooser wins, final int contextId) {
        final StringBuilder builder = new StringBuilder(STR_SELECT).append(fields(metadata, wins)).append(SQL_CHUNK04).append(contextId).append(
            SQL_CHUNK03).append(contextId).append(SQL_CHUNK01).append(folderId).append(" AND infostore.creating_date >= ").append(since);
        if (sort != null) {
            builder.append(STR_ORDER_BY).append(fieldName(sort, wins, true)).append(' ').append(order(order));
        }
        return builder.toString();
    }

    public String getModifiedDocumentsQuery(final long folderId, final long since, final Metadata[] metadata, final Metadata sort, final int order, final FieldChooser wins, final int contextId) {
        final StringBuilder builder = new StringBuilder(STR_SELECT).append(fields(metadata, wins)).append(SQL_CHUNK04).append(contextId).append(
            SQL_CHUNK03).append(contextId).append(SQL_CHUNK01).append(folderId).append(SQL_CHUNK05).append(since);
        if (sort != null) {
            builder.append(STR_ORDER_BY).append(fieldName(sort, wins, true)).append(' ').append(order(order));
        }
        return builder.toString();
    }

    public String getDeletedDocumentsQuery(final long folderId, final long since, final Metadata sort, final int order, @SuppressWarnings("unused") final FieldChooser wins, final int contextId) {
        final StringBuilder builder = new StringBuilder("SELECT infostore.id, infostore.folder_id").append(
            " FROM del_infostore as infostore WHERE infostore.folder_id = ").append(folderId).append(" AND infostore.cid = ").append(
                contextId).append(SQL_CHUNK05).append(since);
        if (null != sort && Table.DEL_INFOSTORE.getFieldSet().contains(sort)) {
            builder.append(STR_ORDER_BY).append(sort.doSwitch(Table.DEL_INFOSTORE.getFieldSwitcher())).append(' ').append(order(order));
        }
        return builder.toString();
    }

    public String getNewDocumentsQuery(final long folderId, final int userId, final long since, final Metadata[] metadata, final Metadata sort, final int order, final FieldChooser wins, final int contextId) {
        final StringBuilder builder = new StringBuilder(STR_SELECT).append(fields(metadata, wins)).append(SQL_CHUNK04).append(contextId).append(
            SQL_CHUNK03).append(contextId).append(SQL_CHUNK01).append(folderId).append(" AND infostore.creating_date >= ").append(since).append(
                SQL_CHUNK02).append(userId);
        if (sort != null) {
            builder.append(STR_ORDER_BY).append(fieldName(sort, wins, true)).append(' ').append(order(order));
        }
        return builder.toString();
    }

    public String getModifiedDocumentsQuery(final long folderId, final int userId, final long since, final Metadata[] metadata, final Metadata sort, final int order, final FieldChooser wins, final int contextId) {
        final StringBuilder builder = new StringBuilder(STR_SELECT).append(fields(metadata, wins)).append(SQL_CHUNK04).append(contextId).append(
            SQL_CHUNK03).append(contextId).append(SQL_CHUNK01).append(folderId).append(SQL_CHUNK05).append(since).append(SQL_CHUNK02).append(
                userId);
        if (sort != null) {
            builder.append(STR_ORDER_BY).append(fieldName(sort, wins, true)).append(' ').append(order(order));
        }
        return builder.toString();
    }

    public String getDeletedDocumentsQuery(final long folderId, final int userId, final long since, final Metadata sort, final int order, final FieldChooser wins, final int contextId) {
        final StringBuilder builder = new StringBuilder("SELECT infostore.id, infostore.folder_id").append(
            " FROM del_infostore as infostore WHERE infostore.folder_id = ").append(folderId).append(" AND infostore.cid = ").append(
                contextId).append(SQL_CHUNK05).append(since).append(SQL_CHUNK02).append(userId);
        if (sort != null) {
            builder.append(STR_ORDER_BY).append(fieldName(sort, wins, true)).append(' ').append(order(order));
        }
        return builder.toString();
    }

    public String getCurrentFilenameQuery(final long folderId, final Metadata[] metadata, final DocumentWins wins, final int contextId) {
        final StringBuilder builder = new StringBuilder(STR_SELECT).append(fields(metadata, wins)).append(SQL_CHUNK04).append(contextId).append(
            SQL_CHUNK03).append(contextId).append(SQL_CHUNK01).append(folderId).append(" AND infostore_document.filename = ?");
        return builder.toString();
    }

    public String getAllVersionsQuery(final String where, final Metadata[] metadata, final VersionWins wins, final int contextId) {
        final StringBuilder builder = new StringBuilder(STR_SELECT).append(fields(metadata, wins)).append(SQL_CHUNK04).append(contextId).append(
            SQL_CHUNK03).append(contextId).append(" AND infostore.id = infostore_document.infostore_id").append(" WHERE ").append(where);
        return builder.toString();
    }

    public String getAllDocumentsQuery(final String where, final Metadata[] metadata, final DocumentWins wins, final int contextId) {
        final StringBuilder builder = new StringBuilder(STR_SELECT).append(fields(metadata, wins)).append(SQL_CHUNK04).append(contextId).append(
            SQL_CHUNK03).append(contextId).append(
                " AND infostore.version = infostore_document.version_number AND infostore.id = infostore_document.infostore_id WHERE ").append(
                    where);
        return builder.toString();
    }

    public String getFolderSequenceNumbersQuery(List<Long> folderIds, boolean versionsOnly, boolean deleted, int contextId) {
        StringBuilder allocator = new StringBuilder(STR_SELECT).append(Metadata.FOLDER_ID_LITERAL.getName()).append(",MAX(")
            .append(Metadata.SEQUENCE_NUMBER_LITERAL.getName())
            .append(") FROM ").append(deleted ? Table.DEL_INFOSTORE.getTablename() : Table.INFOSTORE.getTablename())
            .append(" WHERE ").append(STR_CID).append('=').append(contextId);
        if (versionsOnly) {
            allocator.append(" AND ").append(Metadata.VERSION_LITERAL.getName()).append(">0");
        }
        if (1 == folderIds.size()) {
            allocator.append(" AND ").append(Metadata.FOLDER_ID_LITERAL.getName()).append('=').append(folderIds.get(0));
        } else if (1 < folderIds.size()) {
            allocator.append(" AND ").append(Metadata.FOLDER_ID_LITERAL.getName()).append(" IN (").append(folderIds.get(0));
            for (int i = 1; i < folderIds.size(); i++) {
                allocator.append(',').append(folderIds.get(i));
            }
            allocator.append(')');
        }
        allocator.append(" GROUP BY ").append(Metadata.FOLDER_ID_LITERAL.getName()).append(';');
        return allocator.toString();
    }

    public String getSharedDocumentsForUserQuery(final int contextId, final int userId, final int[] groups, int leastPermission, final Metadata[] metadata, Metadata sort, int order, int start, int end, final DocumentWins wins) {
        final StringBuilder builder = new StringBuilder(STR_SELECT).append(fields(metadata, wins));
        builder.append(" FROM object_permission JOIN infostore ON object_permission.cid = ").append(contextId).append(" AND object_permission.module = 8 AND object_permission.cid = infostore.cid AND object_permission.folder_id = infostore.folder_id AND object_permission.object_id = infostore.id");
        builder.append(" JOIN infostore_document ON infostore.cid = infostore_document.cid AND infostore.version = infostore_document.version_number AND infostore.id = infostore_document.infostore_id");
        builder.append(" WHERE");
        appendEntityConstraint(builder, "object_permission", userId, groups);

        builder.append(" AND object_permission.bits >=").append(leastPermission);

        if (sort != null) {
            builder.append(STR_ORDER_BY).append(fieldName(sort, wins, true)).append(' ').append(order(order));
        }
        if (start >= 0 && end > 0) {
            builder.append(STR_LIMIT);
            if (start > 0) {
                builder.append(start).append(',');
            }
            builder.append(end - start);
        }

        return builder.toString();
    }

    public String getSharedDocumentsByUserQuery(int contextId, int userId, Metadata[] metadata, Metadata sort, int order, int start, int end, DocumentWins wins) {
        StringBuilder stringBuilder = new StringBuilder("SELECT DISTINCT ")
            .append(fields(metadata, wins))
            .append(" FROM object_permission JOIN infostore ON object_permission.cid = ").append(contextId)
            .append(" AND object_permission.module = 8 AND object_permission.cid = infostore.cid")
            .append(" AND object_permission.folder_id = infostore.folder_id AND object_permission.object_id = infostore.id")
            .append(" JOIN infostore_document ON infostore.cid = infostore_document.cid AND")
            .append(" infostore.version = infostore_document.version_number AND infostore.id = infostore_document.infostore_id")
            .append(" WHERE object_permission.created_by = ").append(userId)
            .append(" AND object_permission.permission_id != ").append(userId)
            ;
        if (sort != null) {
            stringBuilder.append(STR_ORDER_BY).append(fieldName(sort, wins, true)).append(' ').append(order(order));
        }
        if (start >= 0 && end > 0) {
            stringBuilder.append(STR_LIMIT);
            if (start > 0) {
                stringBuilder.append(start).append(',');
            }
            stringBuilder.append(end - start);
        }
        return stringBuilder.toString();
    }

    public String getSharedDocumentsSequenceNumbersQuery(final boolean versionsOnly, final boolean deleted, final int contextId, final int userId, final int[] groups) {
        final StringBuilder builder = new StringBuilder(STR_SELECT);
        if (deleted) {
            builder.append("MAX(p.last_modified) FROM del_object_permission");
        } else {
            builder.append("MAX(i.last_modified) FROM object_permission");
        }
        builder.append(" AS p JOIN infostore AS i ON p.cid = i.cid AND p.module = 8 AND p.folder_id = i.folder_id AND p.object_id = i.id");
        builder.append(" WHERE p.cid = ").append(contextId).append(" AND");
        appendEntityConstraint(builder, "p", userId, groups);
        if (versionsOnly) {
            builder.append(" AND i.version > 0");
        }
        return builder.toString();
    }

    public String getNewSharedDocumentsSince(final int contextId, final int userId, final int[] groups, final long since, final Metadata[] metadata, final Metadata sort, final int order, final FieldChooser wins) {
        final StringBuilder builder = new StringBuilder(STR_SELECT).append(fields(metadata, wins));
        builder.append(" FROM object_permission JOIN infostore ON object_permission.cid = ").append(contextId).append(" AND object_permission.module = 8 AND object_permission.cid = infostore.cid AND object_permission.folder_id = infostore.folder_id AND object_permission.object_id = infostore.id");
        builder.append(" JOIN infostore_document ON infostore.cid = infostore_document.cid AND infostore.version = infostore_document.version_number AND infostore.id = infostore_document.infostore_id");
        builder.append(" WHERE");
        appendEntityConstraint(builder, "object_permission", userId, groups);

        builder.append(" AND object_permission.last_modified > ").append(since);
        if (sort != null) {
            builder.append(STR_ORDER_BY).append(fieldName(sort, wins, true)).append(' ').append(order(order));
        }
        return builder.toString();
    }

    public String getModifiedSharedDocumentsSince(final int contextId, final int userId, final int[] groups, final long since, final Metadata[] metadata, final Metadata sort, final int order, final FieldChooser wins) {
        final StringBuilder builder = new StringBuilder(STR_SELECT).append(fields(metadata, wins));
        builder.append(" FROM object_permission JOIN infostore ON object_permission.cid = ").append(contextId).append(" AND object_permission.module = 8 AND object_permission.cid = infostore.cid AND object_permission.folder_id = infostore.folder_id AND object_permission.object_id = infostore.id");
        builder.append(" JOIN infostore_document ON infostore.cid = infostore_document.cid AND infostore.version = infostore_document.version_number AND infostore.id = infostore_document.infostore_id");
        builder.append(" WHERE");
        appendEntityConstraint(builder, "object_permission", userId, groups);

        builder.append(" AND infostore.last_modified > ").append(since);
        if (sort != null) {
            builder.append(STR_ORDER_BY).append(fieldName(sort, wins, true)).append(' ').append(order(order));
        }
        return builder.toString();
    }

    public String getDeletedSharedDocumentsSince(final int contextId, final int userId, final int[] groups, final long since, final Metadata[] metadata, final Metadata sort, final int order, final FieldChooser wins) {
        final StringBuilder builder = new StringBuilder(STR_SELECT).append(fields(metadata, wins));
        builder.append(" FROM del_object_permission AS object_permission JOIN infostore ON object_permission.cid = ").append(contextId).append(" AND object_permission.module = 8 AND object_permission.cid = infostore.cid AND object_permission.folder_id = infostore.folder_id AND object_permission.object_id = infostore.id");
        builder.append(" JOIN infostore_document ON infostore.cid = infostore_document.cid AND infostore.version = infostore_document.version_number AND infostore.id = infostore_document.infostore_id");
        builder.append(" WHERE");
        appendEntityConstraint(builder, "object_permission", userId, groups);

        builder.append(" AND object_permission.last_modified > ").append(since);
        if (sort != null) {
            builder.append(STR_ORDER_BY).append(fieldName(sort, wins, true)).append(' ').append(order(order));
        }
        return builder.toString();
    }

    public String getDocumentOriginQuery() {
        return "SELECT origin FROM infostore WHERE cid = ? AND id = ? AND folder_id = ? AND origin IS NOT NULL";
    }

    private static void appendEntityConstraint(StringBuilder builder, String tablePrefix, int userId, int[] groups) {
        if (groups != null && groups.length > 0) {
            builder.append(" ((").append(tablePrefix).append(".group_flag <> 1 AND ").append(tablePrefix).append(".permission_id = ").append(userId).append(") OR (").append(tablePrefix).append(".group_flag = 1 AND ").append(tablePrefix).append(".permission_id IN (");
            boolean first = true;
            for (int group : groups) {
                if (first) {
                    builder.append(group);
                    first = false;
                } else {
                    builder.append(", ").append(group);
                }
            }
            builder.append(")))");
        } else {
            builder.append(" (").append(tablePrefix).append(".group_flag <> 1 AND ").append(tablePrefix).append(".permission_id = ").append(userId).append(")");
        }
    }

    private String order(final int order) {
        return order == InfostoreFacade.DESC ? "DESC" : "ASC";
    }

    public String[] getFieldTuple(final Metadata field, final FieldChooser wins) {
        Table t = wins.choose(field);
        String col = (String) field.doSwitch(t.getFieldSwitcher());
        return col == null ? null : new String[] { t.getTablename(), col };
    }

    private String fieldName(Metadata field, FieldChooser wins, boolean forOrderByClause) {
        if (field == Metadata.CURRENT_VERSION_LITERAL) {
            return "(infostore.version = infostore_document.version_number) AS current_version";
        }

        if (field == Metadata.MEDIA_DATE_LITERAL) {
            if (forOrderByClause) {
                return "COALESCE(infostore_document.capture_date, infostore_document.last_modified)";
            }
            return "COALESCE(infostore_document.capture_date, infostore_document.last_modified) AS media_date";
        }

        if (field == Metadata.GEOLOCATION_LITERAL) {
            String[] tuple = getFieldTuple(field, wins);
            if (tuple == null) {
                return null;
            }
            String tableName = tuple[0];
            String col = tuple[1];
            return new StringBuilder("AsText(").append(tableName).append(".").append(col).append(") AS ").append(col).toString();
        }

        String[] tuple = getFieldTuple(field, wins);
        return tuple == null ? null : new StringBuilder(tuple[0]).append('.').append(tuple[1]).toString();
    }

    private String fields(Metadata[] metadata, FieldChooser wins) {
        StringBuilder builder = new StringBuilder(metadata.length << 2);
        for (Metadata m : metadata) {
            String col = fieldName(m, wins, false);
            if (col != null) {
                builder.append(col).append(',');
            }
        }
        builder.setLength(builder.length() - 1);
        return builder.toString();
    }

    public static final class InfostoreColumnsSwitch implements MetadataSwitcher {

        private static final InfostoreColumnsSwitch INST = new InfostoreColumnsSwitch();

        /**
         * Gets the switcher instance
         *
         * @return The instance
         */
        public static InfostoreColumnsSwitch getInstance() {
            return INST;
        }

        private InfostoreColumnsSwitch() {
            super();
        }

        @Override
        public Object categories() {
            return null;
        }

        @Override
        public Object colorLabel() {
            return "color_label";
        }

        @Override
        public Object content() {
            return null;
        }

        @Override
        public Object createdBy() {
            return "created_by";
        }

        @Override
        public Object creationDate() {
            return "creating_date";
        }

        @Override
        public Object meta() {
            return null;
        }

        @Override
        public Object currentVersion() {
            return null;
        }

        @Override
        public Object description() {
            return null;
        }

        @Override
        public Object fileMD5Sum() {
            return null;
        }

        @Override
        public Object fileMIMEType() {
            return null;
        }

        @Override
        public Object fileName() {
            return null;
        }

        @Override
        public Object fileSize() {
            return null;
        }

        @Override
        public Object folderId() {
            return "folder_id";
        }

        @Override
        public Object id() {
            return "id";
        }

        @Override
        public Object lastModified() {
            return null;
        }

        @Override
        public Object lockedUntil() {
            return null;
        }

        @Override
        public Object modifiedBy() {
            return "changed_by";
        }

        @Override
        public Object sequenceNumber() {
            return "last_modified";
        }

        @Override
        public Object title() {
            return null;
        }

        @Override
        public Object url() {
            return null;
        }

        @Override
        public Object version() {
            return "version";
        }

        @Override
        public Object versionComment() {
            return null;
        }

        @Override
        public Object filestoreLocation() {
            // Nothing to do
            return null;
        }

        @Override
        public Object lastModifiedUTC() {
            return lastModified();
        }

        @Override
        public Object numberOfVersions() {
            return null;
        }

        @Override
        public Object objectPermissions() {
            return null;
        }

        @Override
        public Object shareable() {
            return null;
        }

        @Override
        public Object origin() {
            return "origin";
        }

        @Override
        public Object captureDate() {
            return null;
        }

        @Override
        public Object geolocation() {
            return null;
        }

        @Override
        public Object width() {
            return null;
        }

        @Override
        public Object height() {
            return null;
        }

        @Override
        public Object cameraModel() {
            return null;
        }

        @Override
        public Object cameraIsoSpeed() {
            return null;
        }

        @Override
        public Object cameraAperture() {
            return null;
        }

        @Override
        public Object cameraExposureTime() {
            return null;
        }

        @Override
        public Object cameraFocalLength() {
            return null;
        }

        @Override
        public Object mediaMeta() {
            return null;
        }

        @Override
        public Object mediaStatus() {
            return null;
        }

        @Override
        public Object mediaDate() {
            return null;
        }

    }

    public static final class DelInfostoreColumnsSwitch implements MetadataSwitcher {

        private static final DelInfostoreColumnsSwitch INST = new DelInfostoreColumnsSwitch();

        /**
         * Gets the switcher instance
         *
         * @return The instance
         */
        public static DelInfostoreColumnsSwitch getInstance() {
            return INST;
        }

        private DelInfostoreColumnsSwitch() {
            super();
        }

        @Override
        public Object categories() {
            return null;
        }

        @Override
        public Object meta() {
            return null;
        }

        @Override
        public Object colorLabel() {
            return "color_label";
        }

        @Override
        public Object content() {
            return null;
        }

        @Override
        public Object createdBy() {
            return "created_by";
        }

        @Override
        public Object creationDate() {
            return "creating_date";
        }

        @Override
        public Object currentVersion() {
            return null;
        }

        @Override
        public Object description() {
            return null;
        }

        @Override
        public Object fileMD5Sum() {
            return null;
        }

        @Override
        public Object fileMIMEType() {
            return null;
        }

        @Override
        public Object fileName() {
            return null;
        }

        @Override
        public Object fileSize() {
            return null;
        }

        @Override
        public Object folderId() {
            return "folder_id";
        }

        @Override
        public Object id() {
            return "id";
        }

        @Override
        public Object lastModified() {
            return null;
        }

        @Override
        public Object lockedUntil() {
            return null;
        }

        @Override
        public Object modifiedBy() {
            return "changed_by";
        }

        @Override
        public Object sequenceNumber() {
            return "last_modified";
        }

        @Override
        public Object title() {
            return null;
        }

        @Override
        public Object url() {
            return null;
        }

        @Override
        public Object version() {
            return "version";
        }

        @Override
        public Object versionComment() {
            return null;
        }

        @Override
        public Object filestoreLocation() {
            // Nothing to do
            return null;
        }

        @Override
        public Object lastModifiedUTC() {
            return lastModified();
        }

        @Override
        public Object numberOfVersions() {
            return null;
        }

        @Override
        public Object objectPermissions() {
            return null;
        }

        @Override
        public Object shareable() {
            return null;
        }

        @Override
        public Object origin() {
            return "origin";
        }

        @Override
        public Object captureDate() {
            return null;
        }

        @Override
        public Object geolocation() {
            return null;
        }

        @Override
        public Object width() {
            return null;
        }

        @Override
        public Object height() {
            return null;
        }

        @Override
        public Object cameraModel() {
            return null;
        }

        @Override
        public Object cameraIsoSpeed() {
            return null;
        }

        @Override
        public Object cameraAperture() {
            return null;
        }

        @Override
        public Object cameraExposureTime() {
            return null;
        }

        @Override
        public Object cameraFocalLength() {
            return null;
        }

        @Override
        public Object mediaMeta() {
            return null;
        }

        @Override
        public Object mediaStatus() {
            return null;
        }

        @Override
        public Object mediaDate() {
            return null;
        }

    }

    public static final class InfostoreDocumentColumnsSwitch implements MetadataSwitcher {

        private static final InfostoreDocumentColumnsSwitch INST = new InfostoreDocumentColumnsSwitch();

        /**
         * Gets the switcher instance
         *
         * @return The instance
         */
        public static InfostoreDocumentColumnsSwitch getInstance() {
            return INST;
        }

        private InfostoreDocumentColumnsSwitch() {
            super();
        }

        @Override
        public Object categories() {
            return "categories";
        }

        @Override
        public Object meta() {
            return "meta";
        }

        @Override
        public Object colorLabel() {
            return null;
        }

        @Override
        public Object content() {
            return null;
        }

        @Override
        public Object createdBy() {
            return "created_by";
        }

        @Override
        public Object creationDate() {
            return "creating_date";
        }

        @Override
        public Object currentVersion() {
            return null;
        }

        @Override
        public Object description() {
            return "description";
        }

        @Override
        public Object fileMD5Sum() {
            return "file_md5sum";
        }

        @Override
        public Object fileMIMEType() {
            return "file_mimetype";
        }

        @Override
        public Object fileName() {
            return "filename";
        }

        @Override
        public Object fileSize() {
            return "file_size";
        }

        @Override
        public Object folderId() {
            return null;
        }

        @Override
        public Object id() {
            return "infostore_id";
        }

        @Override
        public Object lastModified() {
            return "last_modified";
        }

        @Override
        public Object lockedUntil() {
            return null;
        }

        @Override
        public Object modifiedBy() {
            return "changed_by";
        }

        @Override
        public Object sequenceNumber() {
            return null;
        }

        @Override
        public Object title() {
            return "title";
        }

        @Override
        public Object url() {
            return "url";
        }

        @Override
        public Object version() {
            return "version_number";
        }

        @Override
        public Object versionComment() {
            return "file_version_comment";
        }

        @Override
        public Object filestoreLocation() {
            return "file_store_location";
        }

        @Override
        public Object lastModifiedUTC() {
            return lastModified();
        }

        @Override
        public Object numberOfVersions() {
            return null;
        }

        @Override
        public Object objectPermissions() {
            return null;
        }

        @Override
        public Object shareable() {
            return null;
        }

        @Override
        public Object origin() {
            return "origin";
        }

        @Override
        public Object captureDate() {
            return "capture_date";
        }

        @Override
        public Object geolocation() {
            return "geolocation";
        }

        @Override
        public Object width() {
            return "width";
        }

        @Override
        public Object height() {
            return "height";
        }

        @Override
        public Object cameraModel() {
            return "camera_model";
        }

        @Override
        public Object cameraIsoSpeed() {
            return "camera_iso_speed";
        }

        @Override
        public Object cameraAperture() {
            return "camera_aperture";
        }

        @Override
        public Object cameraExposureTime() {
            return "camera_exposure_time";
        }

        @Override
        public Object cameraFocalLength() {
            return "camera_focal_length";
        }

        @Override
        public Object mediaMeta() {
            return "media_meta";
        }

        @Override
        public Object mediaStatus() {
            return "media_status";
        }

        @Override
        public Object mediaDate() {
            return null;
        }

    }

    public static final class DelInfostoreDocumentColumnsSwitch implements MetadataSwitcher {

        private static final DelInfostoreDocumentColumnsSwitch INST = new DelInfostoreDocumentColumnsSwitch();

        /**
         * Gets the switcher instance
         *
         * @return The instance
         */
        public static DelInfostoreDocumentColumnsSwitch getInstance() {
            return INST;
        }

        private DelInfostoreDocumentColumnsSwitch() {
            super();
        }

        @Override
        public Object categories() {
            return null;
        }

        @Override
        public Object meta() {
            return "meta";
        }

        @Override
        public Object colorLabel() {
            return null;
        }

        @Override
        public Object content() {
            return null;
        }

        @Override
        public Object createdBy() {
            return "created_by";
        }

        @Override
        public Object creationDate() {
            return "creating_date";
        }

        @Override
        public Object currentVersion() {
            return null;
        }

        @Override
        public Object description() {
            return null;
        }

        @Override
        public Object fileMD5Sum() {
            return null;
        }

        @Override
        public Object fileMIMEType() {
            return null;
        }

        @Override
        public Object fileName() {
            return null;
        }

        @Override
        public Object fileSize() {
            return null;
        }

        @Override
        public Object folderId() {
            return null;
        }

        @Override
        public Object id() {
            return "infostore_id";
        }

        @Override
        public Object lastModified() {
            return "last_modified";
        }

        @Override
        public Object lockedUntil() {
            return null;
        }

        @Override
        public Object modifiedBy() {
            return "changed_by";
        }

        @Override
        public Object sequenceNumber() {
            return null;
        }

        @Override
        public Object title() {
            return null;
        }

        @Override
        public Object url() {
            return null;
        }

        @Override
        public Object version() {
            return "version_number";
        }

        @Override
        public Object versionComment() {
            return null;
        }

        @Override
        public Object filestoreLocation() {
            return null;
        }

        @Override
        public Object lastModifiedUTC() {
            return lastModified();
        }

        @Override
        public Object numberOfVersions() {
            return null;
        }

        @Override
        public Object objectPermissions() {
            return null;
        }

        @Override
        public Object shareable() {
            return null;
        }

        @Override
        public Object origin() {
            return "origin";
        }

        @Override
        public Object captureDate() {
            return null;
        }

        @Override
        public Object geolocation() {
            return null;
        }

        @Override
        public Object width() {
            return null;
        }

        @Override
        public Object height() {
            return null;
        }

        @Override
        public Object cameraModel() {
            return null;
        }

        @Override
        public Object cameraIsoSpeed() {
            return null;
        }

        @Override
        public Object cameraAperture() {
            return null;
        }

        @Override
        public Object cameraExposureTime() {
            return null;
        }

        @Override
        public Object cameraFocalLength() {
            return null;
        }

        @Override
        public Object mediaMeta() {
            return null;
        }

        @Override
        public Object mediaStatus() {
            return null;
        }

        @Override
        public Object mediaDate() {
            return null;
        }

    }

    public static interface FieldChooser {
        public Table choose(Metadata m);
    }

    public static class VersionWins implements FieldChooser {

        @Override
        public Table choose(final Metadata m) {
            if (Table.INFOSTORE_DOCUMENT.getFieldSet().contains(m)) {
                return Table.INFOSTORE_DOCUMENT;
            }
            return Table.INFOSTORE;
        }

    }

    public static class DocumentWins implements FieldChooser {

        @Override
        public Table choose(final Metadata m) {
            if (Table.INFOSTORE.getFieldSet().contains(m)) {
                return Table.INFOSTORE;
            }
            return Table.INFOSTORE_DOCUMENT;
        }
    }
}