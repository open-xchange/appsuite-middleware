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

package com.openexchange.groupware.infostore.database.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.infostore.utils.MetadataSwitcher;

public class InfostoreQueryCatalog {
	
	private static final String SQL_CHUNK05 = " AND infostore.last_modified >= ";

	private static final String SQL_CHUNK04 = " FROM infostore JOIN infostore_document ON infostore.cid = ";

	private static final String SQL_CHUNK03 = " AND infostore_document.cid = ";

	private static final String SQL_CHUNK02 = " AND infostore.created_by = ";

	private static final String SQL_CHUNK01 = " AND infostore.version = infostore_document.version_number AND infostore.id = infostore_document.infostore_id WHERE infostore.folder_id = ";

	private static final String STR_SELECT = "SELECT ";
	
	private static final String STR_ORDER_BY = " ORDER BY ";

	private static final String STR_CID = "cid";

	public static final Metadata[] INFOSTORE_FIELDS = new Metadata[]{
		Metadata.ID_LITERAL,
		Metadata.FOLDER_ID_LITERAL,
		Metadata.VERSION_LITERAL,
		Metadata.COLOR_LABEL_LITERAL,
		Metadata.CREATION_DATE_LITERAL,
		Metadata.LAST_MODIFIED_LITERAL,
		Metadata.CREATED_BY_LITERAL,
		Metadata.MODIFIED_BY_LITERAL
	};
	
	public static final Set<Metadata> INFOSTORE_FIELDS_SET = Collections.unmodifiableSet(new HashSet<Metadata>(Arrays.asList(INFOSTORE_FIELDS)));
	
	public static final Metadata[] INFOSTORE_DOCUMENT_FIELDS = new Metadata[]{
		Metadata.ID_LITERAL,
		Metadata.VERSION_LITERAL,
		Metadata.CREATION_DATE_LITERAL,
		Metadata.LAST_MODIFIED_LITERAL,
		Metadata.CREATED_BY_LITERAL,
		Metadata.MODIFIED_BY_LITERAL,
		Metadata.TITLE_LITERAL,
		Metadata.URL_LITERAL,
		Metadata.DESCRIPTION_LITERAL,
		Metadata.CATEGORIES_LITERAL,
		Metadata.FILENAME_LITERAL,
		Metadata.FILE_SIZE_LITERAL,
		Metadata.FILE_MIMETYPE_LITERAL,
		Metadata.FILE_MD5SUM_LITERAL,
		Metadata.VERSION_COMMENT_LITERAL,
		Metadata.FILESTORE_LOCATION_LITERAL
	};
	
	public static final Set<Metadata> INFOSTORE_DOCUMENT_FIELDS_SET = Collections.unmodifiableSet(new HashSet<Metadata>(Arrays.asList(INFOSTORE_DOCUMENT_FIELDS)));
	
	
	public static enum Table {
		INFOSTORE(INFOSTORE_FIELDS, INFOSTORE_FIELDS_SET,"infostore"), 
		INFOSTORE_DOCUMENT(INFOSTORE_DOCUMENT_FIELDS, INFOSTORE_DOCUMENT_FIELDS_SET, "infostore_document"),
		DEL_INFOSTORE(INFOSTORE_FIELDS, INFOSTORE_FIELDS_SET,"del_infostore"),
		DEL_INFOSTORE_DOCUMENT(INFOSTORE_DOCUMENT_FIELDS, INFOSTORE_DOCUMENT_FIELDS_SET, "del_infostore_document");
		
		
		private String tablename;
		private Set<Metadata> fieldSet;
		private Metadata[] fields;

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
		
		public MetadataSwitcher getFieldSwitcher(){
			switch(this) {
			case INFOSTORE: case DEL_INFOSTORE : return new InfostoreColumnsSwitch();
			case INFOSTORE_DOCUMENT : case DEL_INFOSTORE_DOCUMENT : return new InfostoreDocumentColumnsSwitch();
			default: throw new IllegalArgumentException("Will not happen");
			}
		}
	}
	
	private static String buildInsert(final String tablename, final Metadata[] metadata,final MetadataSwitcher columnNames, final String...additionalFields) {
		final StringBuilder builder = new StringBuilder();
		builder.append("INSERT INTO ").append(tablename).append(" (");
		final StringBuilder questionMarks = new StringBuilder();
		
		for(final Metadata m : metadata) {
			final String col = (String) m.doSwitch(columnNames);
			if(col != null) {
				builder.append(col);
				builder.append(',');
				questionMarks.append("?,");
			}
		}
		
		for(final String s : additionalFields) {
			builder.append(s);
			builder.append(',');
			
			questionMarks.append("?,");
		}
		
		builder.setLength(builder.length()-1);
		questionMarks.setLength(questionMarks.length()-1);
		
		builder.append(") VALUES (").append(questionMarks.toString()).append(')');
		
		return builder.toString();
	}
	
	private static String buildInsert(final Table t, final String...additionalFields) {
		return buildInsert(t.getTablename(), t.getFields(), t.getFieldSwitcher(),additionalFields);
	}
	
	private static StringBuilder buildUpdateWithoutWhere(final String tablename, final Metadata[] metadata,final MetadataSwitcher columnNames, final String...additionalFields) {
		final StringBuilder builder = new StringBuilder();
		builder.append("UPDATE ").append(tablename).append(" SET ");
		for(final Metadata m : metadata) {
			//FIXME
			if(m == Metadata.VERSION_LITERAL && ( tablename.equals("infostore_document") || tablename.equals("del_infostore_document"))) {
				continue;
			}
			final String col = (String) m.doSwitch(columnNames);
			if(col != null) {
				builder.append(col);
				builder.append(" = ?,");
			}
		}
		
		for(final String s : additionalFields) {
			builder.append(s);
			builder.append(" = ?,");
		}
		
		builder.setLength(builder.length()-1);
		return builder;
	}
	
	/*private static StringBuilder buildUpdateWithoutWhere(final Table t, final String...additionalFields) {
		return buildUpdateWithoutWhere(t.getTablename(), t.getFields(), t.getFieldSwitcher(), additionalFields);
	}*/
	
	private static final String INSERT_INFOSTORE = buildInsert(Table.INFOSTORE,STR_CID);
	private static final String INSERT_INFOSTORE_DOCUMENT = buildInsert(Table.INFOSTORE_DOCUMENT,STR_CID);
	
	private static final String INSERT_DEL_INFOSTORE = buildInsert(Table.DEL_INFOSTORE,STR_CID);
	private static final String INSERT_DEL_INFOSTORE_DOCUMENT = buildInsert(Table.DEL_INFOSTORE_DOCUMENT,STR_CID);
	
	
	
	public String getDelete(final Table t, final List<DocumentMetadata> documents) {
		switch(t) {
		default: break;
		case INFOSTORE_DOCUMENT : case DEL_INFOSTORE_DOCUMENT: throw new IllegalArgumentException("getDelete is only applicable for the non version tables infostore and del_infostore");
		}
		final StringBuilder delete = new StringBuilder("DELETE FROM ").append(t.getTablename()).append(" WHERE ").append(Metadata.ID_LITERAL.doSwitch(t.getFieldSwitcher())).append(" IN (");
		for(final DocumentMetadata doc : documents) {
			delete.append(doc.getId()).append(',');
		}
		delete.setLength(delete.length()-1);
		delete.append(") AND cid = ?");
		return delete.toString();
	}
	
	public String getDocumentInsert(){
		return INSERT_INFOSTORE;
	}
	
	public String getDelDocumentInsert() {
		return INSERT_DEL_INFOSTORE;
	}

	
	public String getDocumentUpdate(final Metadata[] fields) {
		return buildUpdateWithoutWhere(Table.INFOSTORE.getTablename(), fields, Table.INFOSTORE.getFieldSwitcher()).append(" WHERE cid = ? and id = ? and last_modified <= ?").toString();
	}
	
	public Metadata[] getDocumentFields() {
		return Table.INFOSTORE.getFields();
	}
	
	public Metadata[] filterForDocument(final Metadata[] modified) {
		final List<Metadata> m = new ArrayList<Metadata>();
		final Set<Metadata> knownFields = Table.INFOSTORE.getFieldSet();
		for(final Metadata metadata : modified) {
			if(knownFields.contains(metadata)) {
				m.add(metadata);
			}
		}
		return m.toArray(new Metadata[m.size()]);
	}
	
	public boolean updateDocument(final Metadata[] modifiedColumns) {
		final Set<Metadata> fields = Table.INFOSTORE.getFieldSet();
		for(final Metadata m : modifiedColumns) {
			if(fields.contains(m)) {
				return true;
			}
		}
		return false;
	}
	
	public String getVersionInsert(){
		return INSERT_INFOSTORE_DOCUMENT;
	}
	
	public String getDelVersionInsert(){
		return INSERT_DEL_INFOSTORE_DOCUMENT;
	}
	
	public String getVersionUpdate(final Metadata[] fields) {
		return buildUpdateWithoutWhere(Table.INFOSTORE_DOCUMENT.getTablename(), fields, Table.INFOSTORE_DOCUMENT.getFieldSwitcher()).append(" WHERE cid = ? and infostore_id = ? and version_number = ? and last_modified <= ?").toString();
	}
	
	public Metadata[] getVersionFields(){
		return Table.INFOSTORE_DOCUMENT.getFields();	
	}
	
	public Metadata[] filterForVersion(final Metadata[] modified) {
		final List<Metadata> m = new ArrayList<Metadata>();
		final Set<Metadata> knownFields = Table.INFOSTORE_DOCUMENT.getFieldSet();
		for(final Metadata metadata : modified) {
			if(metadata == Metadata.VERSION_LITERAL) {
				continue;
			}
			if(knownFields.contains(metadata)) {
				m.add(metadata);
			}
		}
		return m.toArray(new Metadata[m.size()]);
	}
	
	public boolean updateVersion(final Metadata[] modifiedColumns) {
		final Set<Metadata> fields = Table.INFOSTORE_DOCUMENT.getFieldSet();
		for(final Metadata m : modifiedColumns) {
			if(fields.contains(m)) {
				return true;
			}
		}
		return false;
	}
	
	public String getVersionDelete(final Table t, final List<DocumentMetadata> documents) {
		switch(t) {
		default: break;
		case INFOSTORE : case DEL_INFOSTORE: throw new IllegalArgumentException("getVersionDelete is only applicable for the version tables infostore_document and del_infostore_document");
		}
		final StringBuilder delete = new StringBuilder("DELETE FROM ").append(t.getTablename()).append(" WHERE ( ");
		for(final DocumentMetadata doc : documents) {
			delete.append("( ").append(Metadata.ID_LITERAL.doSwitch(t.getFieldSwitcher())).append(" = ").append(doc.getId())
			.append(" AND ").append(Metadata.VERSION_LITERAL.doSwitch(t.getFieldSwitcher())).append(" = ").append(doc.getVersion())
			.append(" ) OR ");
		}
		delete.setLength(delete.length()-6);
		delete.append(") ) AND cid = ?");
		return delete.toString();
	}
	
	public FieldChooser getChooserForVersion(final int version) {
		if(version == InfostoreFacade.CURRENT_VERSION) {
			return new DocumentWins();
		}
		return new VersionWins();
	}
	
	public String getSelectDocument(final int id, final int version, final int ctx_id) {
		final StringBuilder builder = new StringBuilder(STR_SELECT).append(fields(Metadata.VALUES_ARRAY, getChooserForVersion(version))).append(SQL_CHUNK04)
		.append(ctx_id)
		.append(SQL_CHUNK03)
		.append(ctx_id)
		.append(" AND infostore.id = infostore_document.infostore_id ");
		if(version == InfostoreFacade.CURRENT_VERSION) {
			builder.append("AND infostore_document.version_number = infostore.version");
		}
		builder.append(" WHERE infostore.id = ")
		.append(id);
		if(version != InfostoreFacade.CURRENT_VERSION) {
			builder.append(" AND infostore_document.version_number = ")
			.append(version);
		}
		return builder.toString();
	}

	public String getListQuery(final int[] id, final Metadata[] metadata, final FieldChooser wins, final int contextId) {
		final StringBuilder builder = new StringBuilder(STR_SELECT).append(fields(metadata,wins))
		.append(SQL_CHUNK04)
		.append(contextId)
		.append(SQL_CHUNK03)
		.append(contextId)
		.append(" AND infostore.version = infostore_document.version_number AND infostore.id = infostore_document.infostore_id WHERE infostore.id IN (");
		for(final int i : id) { builder.append(i).append(','); }
		builder.setLength(builder.length()-1);
		builder.append(')');
		return builder.toString();
	}
	
	public String getDocumentsQuery(final long folderId, final Metadata[] metadata, final Metadata sort, final int order, final FieldChooser wins, final int contextId) {
		final StringBuilder builder = new StringBuilder(STR_SELECT).append(fields(metadata,wins))
		.append(SQL_CHUNK04)
		.append(contextId)
		.append(SQL_CHUNK03)
		.append(contextId)
		.append(SQL_CHUNK01)
		.append(folderId);
		if(sort != null) {
			builder.append(STR_ORDER_BY).append(fieldName(sort,wins))
			.append(' ').append(order(order));
		}
		
		return builder.toString();
	}
	
	public String getDocumentsQuery(final long folderId, final int userId, final Metadata[] metadata, final Metadata sort, final int order, final FieldChooser wins, final int contextId) {
		final StringBuilder builder = new StringBuilder(STR_SELECT).append(fields(metadata,wins))
		.append(SQL_CHUNK04)
		.append(contextId)
		.append(SQL_CHUNK03)
		.append(contextId)
		.append(SQL_CHUNK01)
		.append(folderId)
		.append(SQL_CHUNK02).append(userId);
		if(sort != null) {
			builder.append(STR_ORDER_BY).append(fieldName(sort,wins))
			.append(' ').append(order(order));
		}
		return builder.toString();
	}

	public String getVersionsQuery(final int id, final Metadata[] metadata, final Metadata sort, final int order, final FieldChooser wins, final int contextId) {
		final StringBuilder builder = new StringBuilder(STR_SELECT).append(fields(metadata,wins))
		.append(SQL_CHUNK04)
		.append(contextId)
		.append(SQL_CHUNK03)
		.append(contextId)
		.append(" AND infostore.id = infostore_document.infostore_id ")
		.append(" WHERE infostore.id = ")
		.append(id);
		if(sort != null) {
			builder.append(STR_ORDER_BY).append(fieldName(sort,wins))
			.append(' ').append(order(order));
		}
		return builder.toString();
	}
	
	public String getNewDocumentsQuery(final long folderId, final long since, final Metadata[] metadata, final Metadata sort, final int order, final FieldChooser wins, final int contextId) {
		final StringBuilder builder = new StringBuilder(STR_SELECT).append(fields(metadata,wins))
		.append(SQL_CHUNK04)
		.append(contextId)
		.append(SQL_CHUNK03)
		.append(contextId)
		.append(SQL_CHUNK01)
		.append(folderId)
		.append(" AND infostore.creating_date >= ").append(since);
		if(sort != null) {
			builder.append(STR_ORDER_BY).append(fieldName(sort,wins))
			.append(' ').append(order(order));
		}
		return builder.toString();
	}

	public String getModifiedDocumentsQuery(final long folderId, final long since, final Metadata[] metadata, final Metadata sort, final int order, final FieldChooser wins, final int contextId) {
		final StringBuilder builder = new StringBuilder(STR_SELECT).append(fields(metadata,wins))
		.append(SQL_CHUNK04)
		.append(contextId)
		.append(SQL_CHUNK03)
		.append(contextId)
		.append(SQL_CHUNK01)
		.append(folderId)
		.append(SQL_CHUNK05).append(since);
		if(sort != null) {
			builder.append(STR_ORDER_BY).append(fieldName(sort,wins))
			.append(' ').append(order(order));
		}
		return builder.toString();
	}

	public String getDeletedDocumentsQuery(final long folderId, final long since, final Metadata sort, final int order, final FieldChooser wins, final int contextId) {
		final StringBuilder builder = new StringBuilder("SELECT infostore.id")
		.append(" FROM del_infostore as infostore WHERE infostore.folder_id = ")
		.append(folderId)
		.append(" AND infostore.cid = ").append(contextId)
		.append(SQL_CHUNK05).append(since);
		if(sort != null) {
			builder.append(STR_ORDER_BY).append(fieldName(sort,wins))
			.append(' ').append(order(order));
		}
		return builder.toString();
	}
	
	public String getNewDocumentsQuery(final long folderId,final int userId, final long since, final Metadata[] metadata, final Metadata sort, final int order, final FieldChooser wins, final int contextId) {
		final StringBuilder builder = new StringBuilder(STR_SELECT).append(fields(metadata,wins))
		.append(SQL_CHUNK04)
		.append(contextId)
		.append(SQL_CHUNK03)
		.append(contextId)
		.append(SQL_CHUNK01)
		.append(folderId)
		.append(" AND infostore.creating_date >= ").append(since)
		.append(SQL_CHUNK02).append(userId);
		if(sort != null) {
			builder.append(STR_ORDER_BY).append(fieldName(sort,wins))
			.append(' ').append(order(order));
		}
		return builder.toString();
	}

	public String getModifiedDocumentsQuery(final long folderId,final int userId, final long since, final Metadata[] metadata, final Metadata sort, final int order, final FieldChooser wins, final int contextId) {
		final StringBuilder builder = new StringBuilder(STR_SELECT).append(fields(metadata,wins))
		.append(SQL_CHUNK04)
		.append(contextId)
		.append(SQL_CHUNK03)
		.append(contextId)
		.append(SQL_CHUNK01)
		.append(folderId)
		.append(SQL_CHUNK05).append(since)
		.append(SQL_CHUNK02).append(userId);
		if(sort != null) {
			builder.append(STR_ORDER_BY).append(fieldName(sort,wins))
			.append(' ').append(order(order));
		}
		return builder.toString();
	}

	public String getDeletedDocumentsQuery(final long folderId,final int userId, final long since, final Metadata sort, final int order, final FieldChooser wins, final int contextId) {
		final StringBuilder builder = new StringBuilder("SELECT infostore.id")
		.append(" FROM del_infostore as infostore WHERE infostore.folder_id = ")
		.append(folderId)
		.append(" AND infostore.cid = ").append(contextId)
		.append(SQL_CHUNK05).append(since)
		.append(SQL_CHUNK02).append(userId);
		if(sort != null) {
			builder.append(STR_ORDER_BY).append(fieldName(sort,wins))
			.append(' ').append(order(order));
		}
		return builder.toString();
	}
	
	public String getCurrentFilenameQuery(final long folderId, final Metadata[] metadata, final DocumentWins wins, final int contextId) {
		final StringBuilder builder = new StringBuilder(STR_SELECT).append(fields(metadata,wins))
		.append(SQL_CHUNK04)
		.append(contextId)
		.append(SQL_CHUNK03)
		.append(contextId)
		.append(SQL_CHUNK01)
		.append(folderId)
		.append(" AND infostore_document.filename = ?");
		return builder.toString();
	}	

	public String getAllVersionsQuery(final String where, final Metadata[] metadata, final VersionWins wins, final int contextId) {
		final StringBuilder builder = new StringBuilder(STR_SELECT).append(fields(metadata,wins))
		.append(SQL_CHUNK04)
		.append(contextId)
		.append(SQL_CHUNK03)
		.append(contextId)
		.append(" AND infostore.id = infostore_document.infostore_id")
		.append(" WHERE ").append(where);
		return builder.toString();
	}

	public String getAllDocumentsQuery(final String where, final Metadata[] metadata, final DocumentWins wins, final int contextId) {
		final StringBuilder builder = new StringBuilder(STR_SELECT).append(fields(metadata,wins))
		.append(SQL_CHUNK04)
		.append(contextId)
		.append(SQL_CHUNK03)
		.append(contextId)
		.append(" AND infostore.version = infostore_document.version_number AND infostore.id = infostore_document.infostore_id WHERE ").append(where);
		return builder.toString();
	}
	
	private String order(final int order) {
		if(order == InfostoreFacade.DESC) {
			return "DESC";
		}
		return "ASC";
	}

	private String fieldName(final Metadata sort, final FieldChooser wins) {
		if(sort == Metadata.CURRENT_VERSION_LITERAL) {
			return "(infostore.version = infostore_document.version_number) AS current_version";
		}
		final Table t = wins.choose(sort);
		final String col = (String) sort.doSwitch(t.getFieldSwitcher());
		if(col == null) {
			return null;
		}
		return new StringBuilder(t.getTablename()).append('.').append(col).toString();
	}

	private String fields(final Metadata[] metadata, final FieldChooser wins) {
		final StringBuilder builder = new StringBuilder();
		for(final Metadata m : metadata) {
			final String col = fieldName(m,wins);
			if(col != null) {
				builder.append(col).append(',');
			}
		}
		builder.setLength(builder.length()-1);
		return builder.toString();
	}


	private static final class InfostoreColumnsSwitch implements MetadataSwitcher{

		public Object categories() {
			return null;
		}

		public Object colorLabel() {
			return "color_label";
		}

		public Object content() {
			return null;
		}

		public Object createdBy() {
			return "created_by";
		}

		public Object creationDate() {
			return "creating_date";
		}

		public Object currentVersion() {
			return null;
		}

		public Object description() {
			return null;
		}

		public Object fileMD5Sum() {
			return null;
		}

		public Object fileMIMEType() {
			return null;
		}

		public Object fileName() {
			return null;
		}

		public Object fileSize() {
			return null;
		}

		public Object folderId() {
			return "folder_id";
		}

		public Object id() {
			return "id";
		}

		public Object lastModified() {
			return "last_modified";
		}

		public Object lockedUntil() {
			return null;
		}

		public Object modifiedBy() {
			return "changed_by";
		}

		public Object sequenceNumber() {
			return null;
		}

		public Object title() {
			return null;
		}

		public Object url() {
			return null;
		}

		public Object version() {
			return "version";
		}

		public Object versionComment() {
			return null;
		}

		public Object filestoreLocation() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
		
	private static final class InfostoreDocumentColumnsSwitch implements MetadataSwitcher{

		public Object categories() {
			return "categories";
		}

		public Object colorLabel() {
			return null;
		}

		public Object content() {
			return null;
		}

		public Object createdBy() {
			return "created_by";
		}

		public Object creationDate() {
			return "creating_date";
		}

		public Object currentVersion() {
			return null;
		}

		public Object description() {
			return "description";
		}

		public Object fileMD5Sum() {
			return "file_md5sum";
		}

		public Object fileMIMEType() {
			return "file_mimetype";
		}

		public Object fileName() {
			return "filename";
		}

		public Object fileSize() {
			return "file_size";
		}

		public Object folderId() {
			return null;
		}

		public Object id() {
			return "infostore_id";
		}

		public Object lastModified() {
			return "last_modified";
		}

		public Object lockedUntil() {
			return null;
		}

		public Object modifiedBy() {
			return "changed_by";
		}

		public Object sequenceNumber() {
			return null;
		}

		public Object title() {
			return "title";
		}

		public Object url() {
			return "url";
		}

		public Object version() {
			return "version_number";
		}

		public Object versionComment() {
			return "file_version_comment";
		}

		public Object filestoreLocation() {
			return "file_store_location";
		}
	}
	
	public static interface FieldChooser {
		public Table choose(Metadata m);
	}
	
	public static class VersionWins implements FieldChooser {

		public Table choose(final Metadata m) {
			if(Table.INFOSTORE_DOCUMENT.getFieldSet().contains(m)) {
				return Table.INFOSTORE_DOCUMENT;
			}
			return Table.INFOSTORE;
		}
		
	}
	
	public static class DocumentWins implements FieldChooser {
		public Table choose(final Metadata m) {
			if(Table.INFOSTORE.getFieldSet().contains(m)) {
				return Table.INFOSTORE;
			}
			return Table.INFOSTORE_DOCUMENT;
		}
	}

	
}
