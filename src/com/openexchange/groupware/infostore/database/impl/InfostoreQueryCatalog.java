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

		private Table(Metadata[] fields, Set<Metadata> fieldSet, String tablename) {
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
	
	private static String buildInsert(String tablename, Metadata[] metadata,MetadataSwitcher columnNames, String...additionalFields) {
		StringBuilder builder = new StringBuilder();
		builder.append("INSERT INTO ").append(tablename).append(" (");
		StringBuilder questionMarks = new StringBuilder();
		
		for(Metadata m : metadata) {
			String col = (String) m.doSwitch(columnNames);
			if(col != null) {
				builder.append(col);
				builder.append(",");
				questionMarks.append("?,");
			}
		}
		
		for(String s : additionalFields) {
			builder.append(s);
			builder.append(",");
			
			questionMarks.append("?,");
		}
		
		builder.setLength(builder.length()-1);
		questionMarks.setLength(questionMarks.length()-1);
		
		builder.append(") VALUES (").append(questionMarks.toString()).append(")");
		
		return builder.toString();
	}
	
	private static String buildInsert(Table t, String...additionalFields) {
		return buildInsert(t.getTablename(), t.getFields(), t.getFieldSwitcher(),additionalFields);
	}
	
	private static StringBuilder buildUpdateWithoutWhere(String tablename, Metadata[] metadata,MetadataSwitcher columnNames, String...additionalFields) {
		StringBuilder builder = new StringBuilder();
		builder.append("UPDATE ").append(tablename).append(" SET ");
		for(Metadata m : metadata) {
			//FIXME
			if(m == Metadata.VERSION_LITERAL && ( tablename.equals("infostore_document") || tablename.equals("del_infostore_document"))) {
				continue;
			}
			String col = (String) m.doSwitch(columnNames);
			if(col != null) {
				builder.append(col);
				builder.append(" = ?,");
			}
		}
		
		for(String s : additionalFields) {
			builder.append(s);
			builder.append(" = ?,");
		}
		
		builder.setLength(builder.length()-1);
		return builder;
	}
	
	private static StringBuilder buildUpdateWithoutWhere(Table t, String...additionalFields) {
		return buildUpdateWithoutWhere(t.getTablename(), t.getFields(), t.getFieldSwitcher(), additionalFields);
	}
	
	private static final String INSERT_INFOSTORE = buildInsert(Table.INFOSTORE,"cid");
	private static final String INSERT_INFOSTORE_DOCUMENT = buildInsert(Table.INFOSTORE_DOCUMENT,"cid");
	
	private static final String INSERT_DEL_INFOSTORE = buildInsert(Table.DEL_INFOSTORE,"cid");
	private static final String INSERT_DEL_INFOSTORE_DOCUMENT = buildInsert(Table.DEL_INFOSTORE_DOCUMENT,"cid");
	
	
	
	public String getDelete(Table t, List<DocumentMetadata> documents) {
		switch(t) {
		default: break;
		case INFOSTORE_DOCUMENT : case DEL_INFOSTORE_DOCUMENT: throw new IllegalArgumentException("getDelete is only applicable for the non version tables infostore and del_infostore");
		}
		StringBuilder delete = new StringBuilder("DELETE FROM ").append(t.getTablename()).append(" WHERE ").append(Metadata.ID_LITERAL.doSwitch(t.getFieldSwitcher())).append(" IN (");
		for(DocumentMetadata doc : documents) {
			delete.append(doc.getId()).append(",");
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

	
	public String getDocumentUpdate(Metadata[] fields) {
		return buildUpdateWithoutWhere(Table.INFOSTORE.getTablename(), fields, Table.INFOSTORE.getFieldSwitcher()).append(" WHERE cid = ? and id = ? and last_modified <= ?").toString();
	}
	
	public Metadata[] getDocumentFields() {
		return Table.INFOSTORE.getFields();
	}
	
	public Metadata[] filterForDocument(Metadata[] modified) {
		List<Metadata> m = new ArrayList<Metadata>();
		Set<Metadata> knownFields = Table.INFOSTORE.getFieldSet();
		for(Metadata metadata : modified) {
			if(knownFields.contains(metadata))
				m.add(metadata);
		}
		return m.toArray(new Metadata[m.size()]);
	}
	
	public boolean updateDocument(Metadata[] modifiedColumns) {
		Set<Metadata> fields = Table.INFOSTORE.getFieldSet();
		for(Metadata m : modifiedColumns) {
			if(fields.contains(m))
				return true;
		}
		return false;
	}
	
	public String getVersionInsert(){
		return INSERT_INFOSTORE_DOCUMENT;
	}
	
	public String getDelVersionInsert(){
		return INSERT_DEL_INFOSTORE_DOCUMENT;
	}
	
	public String getVersionUpdate(Metadata[] fields) {
		return buildUpdateWithoutWhere(Table.INFOSTORE_DOCUMENT.getTablename(), fields, Table.INFOSTORE_DOCUMENT.getFieldSwitcher()).append(" WHERE cid = ? and infostore_id = ? and version_number = ? and last_modified <= ?").toString();
	}
	
	public Metadata[] getVersionFields(){
		return Table.INFOSTORE_DOCUMENT.getFields();	
	}
	
	public Metadata[] filterForVersion(Metadata[] modified) {
		List<Metadata> m = new ArrayList<Metadata>();
		Set<Metadata> knownFields = Table.INFOSTORE_DOCUMENT.getFieldSet();
		for(Metadata metadata : modified) {
			if(metadata == Metadata.VERSION_LITERAL)
				continue;
			if(knownFields.contains(metadata))
				m.add(metadata);
		}
		return m.toArray(new Metadata[m.size()]);
	}
	
	public boolean updateVersion(Metadata[] modifiedColumns) {
		Set<Metadata> fields = Table.INFOSTORE_DOCUMENT.getFieldSet();
		for(Metadata m : modifiedColumns) {
			if(fields.contains(m))
				return true;
		}
		return false;
	}
	
	public String getVersionDelete(Table t, List<DocumentMetadata> documents) {
		switch(t) {
		default: break;
		case INFOSTORE : case DEL_INFOSTORE: throw new IllegalArgumentException("getVersionDelete is only applicable for the version tables infostore_document and del_infostore_document");
		}
		StringBuilder delete = new StringBuilder("DELETE FROM ").append(t.getTablename()).append(" WHERE ( ");
		for(DocumentMetadata doc : documents) {
			delete.append("( ").append(Metadata.ID_LITERAL.doSwitch(t.getFieldSwitcher())).append(" = ").append(doc.getId())
			.append(" AND ").append(Metadata.VERSION_LITERAL.doSwitch(t.getFieldSwitcher())).append(" = ").append(doc.getVersion())
			.append(" ) OR ");
		}
		delete.setLength(delete.length()-6);
		delete.append(") ) AND cid = ?");
		return delete.toString();
	}
	
	public FieldChooser getChooserForVersion(int version) {
		if(version == InfostoreFacade.CURRENT_VERSION)
			return new DocumentWins();
		return new VersionWins();
	}
	
	public String getSelectDocument(int id, int version, int ctx_id) {
		StringBuilder builder = new StringBuilder("SELECT ").append(fields(Metadata.VALUES_ARRAY, getChooserForVersion(version))).append(" FROM infostore JOIN infostore_document ON infostore.cid = ")
		.append(ctx_id)
		.append(" AND infostore_document.cid = ")
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

	public String getListQuery(int[] id, Metadata[] metadata, FieldChooser wins, int contextId) {
		StringBuilder builder = new StringBuilder("SELECT ").append(fields(metadata,wins))
		.append(" FROM infostore JOIN infostore_document ON infostore.cid = ")
		.append(contextId)
		.append(" AND infostore_document.cid = ")
		.append(contextId)
		.append(" AND infostore.version = infostore_document.version_number AND infostore.id = infostore_document.infostore_id WHERE infostore.id IN (");
		for(int i : id) { builder.append(i).append(","); }
		builder.setLength(builder.length()-1);
		builder.append(")");
		return builder.toString();
	}
	
	public String getDocumentsQuery(long folderId, Metadata[] metadata, Metadata sort, int order, FieldChooser wins, int contextId) {
		StringBuilder builder = new StringBuilder("SELECT ").append(fields(metadata,wins))
		.append(" FROM infostore JOIN infostore_document ON infostore.cid = ")
		.append(contextId)
		.append(" AND infostore_document.cid = ")
		.append(contextId)
		.append(" AND infostore.version = infostore_document.version_number AND infostore.id = infostore_document.infostore_id WHERE infostore.folder_id = ")
		.append(folderId);
		if(sort != null) {
			builder.append(" ORDER BY ").append(fieldName(sort,wins))
			.append(" ").append(order(order));
		}
		
		return builder.toString();
	}
	
	public String getDocumentsQuery(long folderId, int userId, Metadata[] metadata, Metadata sort, int order, FieldChooser wins, int contextId) {
		StringBuilder builder = new StringBuilder("SELECT ").append(fields(metadata,wins))
		.append(" FROM infostore JOIN infostore_document ON infostore.cid = ")
		.append(contextId)
		.append(" AND infostore_document.cid = ")
		.append(contextId)
		.append(" AND infostore.version = infostore_document.version_number AND infostore.id = infostore_document.infostore_id WHERE infostore.folder_id = ")
		.append(folderId)
		.append(" AND infostore.created_by = ").append(userId);
		if(sort != null) {
			builder.append(" ORDER BY ").append(fieldName(sort,wins))
			.append(" ").append(order(order));
		}
		return builder.toString();
	}

	public String getVersionsQuery(int id, Metadata[] metadata, Metadata sort, int order, FieldChooser wins, int contextId) {
		StringBuilder builder = new StringBuilder("SELECT ").append(fields(metadata,wins))
		.append(" FROM infostore JOIN infostore_document ON infostore.cid = ")
		.append(contextId)
		.append(" AND infostore_document.cid = ")
		.append(contextId)
		.append(" AND infostore.id = infostore_document.infostore_id ")
		.append(" WHERE infostore.id = ")
		.append(id);
		if(sort != null) {
			builder.append(" ORDER BY ").append(fieldName(sort,wins))
			.append(" ").append(order(order));
		}
		return builder.toString();
	}
	
	public String getNewDocumentsQuery(long folderId, long since, Metadata[] metadata, Metadata sort, int order, FieldChooser wins, int contextId) {
		StringBuilder builder = new StringBuilder("SELECT ").append(fields(metadata,wins))
		.append(" FROM infostore JOIN infostore_document ON infostore.cid = ")
		.append(contextId)
		.append(" AND infostore_document.cid = ")
		.append(contextId)
		.append(" AND infostore.version = infostore_document.version_number AND infostore.id = infostore_document.infostore_id WHERE infostore.folder_id = ")
		.append(folderId)
		.append(" AND infostore.creating_date >= ").append(since);
		if(sort != null) {
			builder.append(" ORDER BY ").append(fieldName(sort,wins))
			.append(" ").append(order(order));
		}
		return builder.toString();
	}

	public String getModifiedDocumentsQuery(long folderId, long since, Metadata[] metadata, Metadata sort, int order, FieldChooser wins, int contextId) {
		StringBuilder builder = new StringBuilder("SELECT ").append(fields(metadata,wins))
		.append(" FROM infostore JOIN infostore_document ON infostore.cid = ")
		.append(contextId)
		.append(" AND infostore_document.cid = ")
		.append(contextId)
		.append(" AND infostore.version = infostore_document.version_number AND infostore.id = infostore_document.infostore_id WHERE infostore.folder_id = ")
		.append(folderId)
		.append(" AND infostore.last_modified >= ").append(since);
		if(sort != null) {
			builder.append(" ORDER BY ").append(fieldName(sort,wins))
			.append(" ").append(order(order));
		}
		return builder.toString();
	}

	public String getDeletedDocumentsQuery(long folderId, long since, Metadata sort, int order, FieldChooser wins, int contextId) {
		StringBuilder builder = new StringBuilder("SELECT infostore.id")
		.append(" FROM del_infostore as infostore WHERE infostore.folder_id = ")
		.append(folderId)
		.append(" AND infostore.cid = ").append(contextId)
		.append(" AND infostore.last_modified >= ").append(since);
		if(sort != null) {
			builder.append(" ORDER BY ").append(fieldName(sort,wins))
			.append(" ").append(order(order));
		}
		return builder.toString();
	}
	
	public String getNewDocumentsQuery(long folderId,int userId, long since, Metadata[] metadata, Metadata sort, int order, FieldChooser wins, int contextId) {
		StringBuilder builder = new StringBuilder("SELECT ").append(fields(metadata,wins))
		.append(" FROM infostore JOIN infostore_document ON infostore.cid = ")
		.append(contextId)
		.append(" AND infostore_document.cid = ")
		.append(contextId)
		.append(" AND infostore.version = infostore_document.version_number AND infostore.id = infostore_document.infostore_id WHERE infostore.folder_id = ")
		.append(folderId)
		.append(" AND infostore.creating_date >= ").append(since)
		.append(" AND infostore.created_by = ").append(userId);
		if(sort != null) {
			builder.append(" ORDER BY ").append(fieldName(sort,wins))
			.append(" ").append(order(order));
		}
		return builder.toString();
	}

	public String getModifiedDocumentsQuery(long folderId,int userId, long since, Metadata[] metadata, Metadata sort, int order, FieldChooser wins, int contextId) {
		StringBuilder builder = new StringBuilder("SELECT ").append(fields(metadata,wins))
		.append(" FROM infostore JOIN infostore_document ON infostore.cid = ")
		.append(contextId)
		.append(" AND infostore_document.cid = ")
		.append(contextId)
		.append(" AND infostore.version = infostore_document.version_number AND infostore.id = infostore_document.infostore_id WHERE infostore.folder_id = ")
		.append(folderId)
		.append(" AND infostore.last_modified >= ").append(since)
		.append(" AND infostore.created_by = ").append(userId);
		if(sort != null) {
			builder.append(" ORDER BY ").append(fieldName(sort,wins))
			.append(" ").append(order(order));
		}
		return builder.toString();
	}

	public String getDeletedDocumentsQuery(long folderId,int userId, long since, Metadata sort, int order, FieldChooser wins, int contextId) {
		StringBuilder builder = new StringBuilder("SELECT infostore.id")
		.append(" FROM del_infostore as infostore WHERE infostore.folder_id = ")
		.append(folderId)
		.append(" AND infostore.cid = ").append(contextId)
		.append(" AND infostore.last_modified >= ").append(since)
		.append(" AND infostore.created_by = ").append(userId);
		if(sort != null) {
			builder.append(" ORDER BY ").append(fieldName(sort,wins))
			.append(" ").append(order(order));
		}
		return builder.toString();
	}
	
	public String getCurrentFilenameQuery(long folderId, Metadata[] metadata, DocumentWins wins, int contextId) {
		StringBuilder builder = new StringBuilder("SELECT ").append(fields(metadata,wins))
		.append(" FROM infostore JOIN infostore_document ON infostore.cid = ")
		.append(contextId)
		.append(" AND infostore_document.cid = ")
		.append(contextId)
		.append(" AND infostore.version = infostore_document.version_number AND infostore.id = infostore_document.infostore_id WHERE infostore.folder_id = ")
		.append(folderId)
		.append(" AND infostore_document.filename = ?");
		return builder.toString();
	}	

	public String getAllVersionsQuery(String where, Metadata[] metadata, VersionWins wins, int contextId) {
		StringBuilder builder = new StringBuilder("SELECT ").append(fields(metadata,wins))
		.append(" FROM infostore JOIN infostore_document ON infostore.cid = ")
		.append(contextId)
		.append(" AND infostore_document.cid = ")
		.append(contextId)
		.append(" AND infostore.id = infostore_document.infostore_id")
		.append(" WHERE ").append(where);
		return builder.toString();
	}

	public String getAllDocumentsQuery(String where, Metadata[] metadata, DocumentWins wins, int contextId) {
		StringBuilder builder = new StringBuilder("SELECT ").append(fields(metadata,wins))
		.append(" FROM infostore JOIN infostore_document ON infostore.cid = ")
		.append(contextId)
		.append(" AND infostore_document.cid = ")
		.append(contextId)
		.append(" AND infostore.version = infostore_document.version_number AND infostore.id = infostore_document.infostore_id WHERE ").append(where);
		return builder.toString();
	}
	
	private String order(int order) {
		if(order == InfostoreFacade.DESC)
			return "DESC";
		return "ASC";
	}

	private String fieldName(Metadata sort, FieldChooser wins) {
		if(sort == Metadata.CURRENT_VERSION_LITERAL) {
			return "(infostore.version = infostore_document.version_number) AS current_version";
		}
		Table t = wins.choose(sort);
		String col = (String) sort.doSwitch(t.getFieldSwitcher());
		if(col == null)
			return null;
		return new StringBuilder(t.getTablename()).append('.').append(col).toString();
	}

	private String fields(Metadata[] metadata, FieldChooser wins) {
		StringBuilder builder = new StringBuilder();
		for(Metadata m : metadata) {
			String col = fieldName(m,wins);
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

		public Table choose(Metadata m) {
			if(Table.INFOSTORE_DOCUMENT.getFieldSet().contains(m)) {
				return Table.INFOSTORE_DOCUMENT;
			}
			return Table.INFOSTORE;
		}
		
	}
	
	public static class DocumentWins implements FieldChooser {
		public Table choose(Metadata m) {
			if(Table.INFOSTORE.getFieldSet().contains(m)) {
				return Table.INFOSTORE;
			}
			return Table.INFOSTORE_DOCUMENT;
		}
	}

	
}
