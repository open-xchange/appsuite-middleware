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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfoDatabase;
import com.openexchange.groupware.infostore.InfostoreExceptionFactory;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.infostore.utils.MetadataSwitcher;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.groupware.tx.DBService;
import com.openexchange.groupware.infostore.Classes;
import com.openexchange.groupware.EnumComponent;

import static com.openexchange.tools.sql.DBUtils.getStatement;

@OXExceptionSource(
		classId = Classes.COM_OPENEXCHANGE_GROUPWARE_INFOSTORE_DATABASE_IMPL_INFODATABASEIMPL,
		component = EnumComponent.INFOSTORE
)
public class InfoDatabaseImpl  extends DBService implements InfoDatabase {
	
	private static final Log LOG = LogFactory.getLog(InfoDatabaseImpl.class);
	private static final InfostoreExceptionFactory EXCEPTIONS = new InfostoreExceptionFactory(InfoDatabaseImpl.class);
	
	private static final Metadata[] INFOSTORE_FIELDS = new Metadata[]{
		Metadata.ID_LITERAL,
		Metadata.FOLDER_ID_LITERAL,
		Metadata.VERSION_LITERAL,
		Metadata.COLOR_LABEL_LITERAL,
		Metadata.CREATION_DATE_LITERAL,
		Metadata.LAST_MODIFIED_LITERAL,
		Metadata.CREATED_BY_LITERAL,
		Metadata.MODIFIED_BY_LITERAL
	};
	
	private static final Set<Metadata> INFOSTORE_FIELDS_SET = Collections.unmodifiableSet(new HashSet<Metadata>(Arrays.asList(INFOSTORE_FIELDS)));
	
	private static final Metadata[] INFOSTORE_DOCUMENT_FIELDS = new Metadata[]{
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
		Metadata.VERSION_COMMENT_LITERAL
	};
	
	private static final Set<Metadata> INFOSTORE_DOCUMENT_FIELDS_SET = Collections.unmodifiableSet(new HashSet<Metadata>(Arrays.asList(INFOSTORE_DOCUMENT_FIELDS)));
	
	
	private static enum Table {
		INFOSTORE(INFOSTORE_FIELDS, INFOSTORE_FIELDS_SET,"infostore"), 
		INFOSTORE_DOCUMENT(INFOSTORE_DOCUMENT_FIELDS, INFOSTORE_DOCUMENT_FIELDS_SET, "infostore_document"),
		DEL_INFOSTORE(INFOSTORE_FIELDS, INFOSTORE_FIELDS_SET,"infostore"),
		DEL_INFOSTORE_DOCUMENT(INFOSTORE_DOCUMENT_FIELDS, INFOSTORE_DOCUMENT_FIELDS_SET, "infostore_document");
		
		
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
	
	private static String buildUpdateWithoutWhere(final String tablename, final Metadata[] metadata,final MetadataSwitcher columnNames, final String...additionalFields) {
		final StringBuilder builder = new StringBuilder();
		builder.append("UPDATE ").append(tablename).append(" SET ");
		for(final Metadata m : metadata) {
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
		return builder.toString();
	}
	
	private static String buildUpdateWithoutWhere(final Table t, final String...additionalFields) {
		return buildUpdateWithoutWhere(t.getTablename(), t.getFields(), t.getFieldSwitcher(), additionalFields);
	}
	
	private static final String INSERT_INFOSTORE = buildInsert(Table.INFOSTORE,"ctx");
	private static final String INSERT_INFOSTORE_DOCUMENT = buildInsert(Table.INFOSTORE_DOCUMENT,"ctx");
	private static final String INSERT_INFOSTORE_DOCUMENT_WITH_FILESTORE_LOC = buildInsert(Table.INFOSTORE_DOCUMENT,"ctx","filestore_location");
	
	
	private static final void fillStatement(final PreparedStatement stmt, final Metadata[] fields,final DocumentMetadata doc, final Object...additional) throws SQLException {
		final GetSwitch get = new GetSwitch(doc);
		int i = 1;
		for(final Metadata m : fields) {
			stmt.setObject(i++, m.doSwitch(get));
		}
		
		for(final Object o : additional) {
			stmt.setObject(i++, o);
		}
	}
	
	public InfoDatabaseImpl(){}
	
	public InfoDatabaseImpl(final DBProvider provider) {
		super(provider);
	}

	public void delete(final int[] ids, final Context ctx) throws OXException {
		final StringBuilder del = new StringBuilder("DELETE FROM infostore WHERE id IN (");
		final StringBuilder delVers = new StringBuilder("DELETE FROM infostore_document WHERE infostore_id IN (");
		for(final int id : ids) {
			del.append(id).append(',');
			delVers.append(id).append(',');
		}
		del.setLength(del.length()-1);
		delVers.setLength(delVers.length()-1);
		
		del.append(')');
		delVers.append(')');
		executeUpdate(del.toString(),new Metadata[0],null,ctx);
		executeUpdate(delVers.toString(),new Metadata[0],null,ctx);
		
	}

	public void insertDocument(final DocumentMetadata document, final Context ctx) throws OXException {
		executeUpdate(INSERT_INFOSTORE, Table.INFOSTORE.getFields(), document, ctx, Integer.valueOf(ctx.getContextId()));
	}
	
	public void insertVersion(final DocumentMetadata document, final Context ctx) throws OXException {
		executeUpdate(INSERT_INFOSTORE_DOCUMENT,Table.INFOSTORE.getFields(), document, ctx, Integer.valueOf(ctx.getContextId()));
	}
	
	public void insertVersion(final DocumentMetadata document, final Context ctx, final String filestoreLocation) throws OXException {
		executeUpdate(INSERT_INFOSTORE_DOCUMENT_WITH_FILESTORE_LOC,Table.INFOSTORE.getFields(), document, ctx, Integer.valueOf(ctx.getContextId()), filestoreLocation);
	}
	
	public void updateDocument(final DocumentMetadata document, final Metadata[] fields, final Context ctx) throws OXException {
		final StringBuilder update = new StringBuilder(buildUpdateWithoutWhere(Table.INFOSTORE));
		update.append(" WHERE id = ? and cid = ?");
		executeUpdate(update.toString(),Table.INFOSTORE.getFields(), document, ctx, Integer.valueOf(document.getId()), Integer.valueOf(ctx.getContextId()));
	}
	
	public void updateVersion(final DocumentMetadata document, final Metadata[] fields, final Context ctx) throws OXException {
		final StringBuilder update = new StringBuilder(buildUpdateWithoutWhere(Table.INFOSTORE_DOCUMENT));
		update.append(" WHERE id = ? and cid = ?");
		executeUpdate(update.toString(),Table.INFOSTORE_DOCUMENT.getFields(), document, ctx, Integer.valueOf(document.getId()), Integer.valueOf(ctx.getContextId()));
	}
	
	public void updateVersion(final DocumentMetadata document, final String filestoreLoc, final Metadata[] fields, final Context ctx) throws OXException {
		final StringBuilder update = new StringBuilder(buildUpdateWithoutWhere(Table.INFOSTORE_DOCUMENT,"filestore_location"));
		update.append(" WHERE id = ? and cid = ?");
		executeUpdate(update.toString(),Table.INFOSTORE_DOCUMENT.getFields(), document, ctx, filestoreLoc, Integer.valueOf(document.getId()), Integer.valueOf(ctx.getContextId()));
	}
	
	public DocumentMetadata[] findModifiedSince(final long mod, final long folderId, final Metadata[] fields, final Metadata orderBy, final boolean asc, final Context ctx) throws OXException {
		final FieldChooser chooser = new DocumentWins();
		final StringBuilder where = new StringBuilder(getFieldName(chooser, Metadata.LAST_MODIFIED_LITERAL));
		where.append(" > ?");
		return select(fields,where.toString(),orderBy,asc,chooser,ctx,Long.valueOf(mod));
	}
	
	public DocumentMetadata[] findByFolderId(final long folderId, final Metadata[] fields,final Context ctx) throws OXException {
		return findByFolderId(folderId, fields,null,false, ctx);
	}
	
	public DocumentMetadata[] findByFolderId(final long folderId, final Metadata[] fields,final Metadata orderBy, final boolean asc, final Context ctx) throws OXException {
		final FieldChooser chooser = new DocumentWins();
		final StringBuilder where = new StringBuilder(getFieldName(chooser, Metadata.FOLDER_ID_LITERAL));
		where.append(" = ?");
		return select(fields,where.toString(),orderBy,asc,chooser,ctx,Long.valueOf(folderId));
	}
	
	private DocumentMetadata[] select(final Metadata[] fields, final String where, final FieldChooser chooser, final Context ctx, final Object...queryArgs) throws OXException {
		return select(fields,where,(String)null,false,chooser,ctx,queryArgs);
	}
	
	private DocumentMetadata[] select(final Metadata[] fields, final String where, final Metadata orderBy, final boolean asc, final FieldChooser chooser, final Context ctx, final Object...queryArgs) throws OXException {
		
		return select(fields, where,getFieldName(chooser, orderBy) , asc, chooser, ctx, queryArgs);
	}
	
	private final String getFieldName(final FieldChooser chooser, final Metadata m) {
		final Table t = chooser.choose(m);
		return t.getTablename()+'.'+m.doSwitch(t.getFieldSwitcher());
	}

	@OXThrows(category=Category.CODE_ERROR, desc="An invalid SQL query was used sent to the SQL Server. This can only be fixed by R&D", exceptionId=1, msg="Invalid SQL query: %s")
	private DocumentMetadata[] select(final Metadata[] fields, final String where, final String orderBy, final boolean asc, final FieldChooser chooser, final Context ctx, final Object...queryArgs) throws OXException {
		final StringBuilder query = new StringBuilder();
		query.append("SELECT ");
		appendFields(fields, query, chooser);
		query.append("WHERE ").append(where);
		if(orderBy != null) {
			query.append(" ORDER BY ").append(orderBy);
			if(asc) {
				query.append(" ASC");
			} else {
				query.append(" DESC");
			}
		}
		
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		final List<DocumentMetadata> results = new ArrayList<DocumentMetadata>();
		try {
			con = getReadConnection(ctx);
			stmt = con.prepareStatement(query.toString());
			int i = 1;
			for(final Object queryArg : queryArgs) {
				stmt.setObject(i++, queryArg);
			}
			rs = stmt.executeQuery();
			while(rs.next()) {
				final DocumentMetadata m = new DocumentMetadataImpl();
				fillDocumentMetadata(rs, m, fields, chooser);
				results.add(m);
			}
		} catch (final SQLException e) {
			throw EXCEPTIONS.create(0,e,getStatement(stmt));
		} finally {
			close(stmt,rs);
			releaseReadConnection(ctx, con);
		}
		return results.toArray(new DocumentMetadata[results.size()]);
	}

	private void fillDocumentMetadata(final ResultSet rs, final DocumentMetadata m, final Metadata[] fields, final FieldChooser chooser) throws SQLException {
		final SetSwitch set = new SetSwitch(m);
		for(final Metadata field : fields) {
			final Table t = chooser.choose(field);
			final String fieldName = t.getTablename()+'.'+field.doSwitch(t.getFieldSwitcher());
			set.setValue(rs.getObject(fieldName));
		}
	}

	
	private void appendFields(final Metadata[] fields, final StringBuilder query, final FieldChooser chooser) {
		final Set<Table> tables = new HashSet<Table>();
		for(final Metadata m : fields) {
			final Table t = chooser.choose(m);
			tables.add(t);
			query.append(t.getTablename()).append('.').append(m.doSwitch(t.getFieldSwitcher())).append(',');
		}
		query.setLength(query.length()-1);
		query.append(" FROM ");
		if(tables.size()>1) {
			query.append(Table.INFOSTORE.getTablename()).append(" JOIN ").append(Table.INFOSTORE_DOCUMENT.getTablename()).append(" ON (")
			.append(Table.INFOSTORE.getTablename()).append('.').append(Metadata.ID_LITERAL.doSwitch(Table.INFOSTORE.getFieldSwitcher()))
			.append(" = ")
			.append(Table.INFOSTORE_DOCUMENT.getTablename()).append('.').append(Metadata.ID_LITERAL.doSwitch(Table.INFOSTORE_DOCUMENT.getFieldSwitcher()));
		} else {
			final Table t = tables.iterator().next();
			query.append(t.getTablename());
		}
	}

	@OXThrows(category=Category.CODE_ERROR, desc="An invalid SQL query was used sent to the SQL Server. This can only be fixed by R&D", exceptionId=0, msg="Invalid SQL query: %s")
	private final void executeUpdate(final String statement, final Metadata[] fields, final DocumentMetadata document, final Context ctx, final Object...additionals) throws OXException {
		Connection con = null;
		PreparedStatement stmt = null;
		try {
			con = getWriteConnection(ctx);
			stmt = con.prepareStatement(statement);
			fillStatement(stmt, fields ,document,additionals);
			stmt.executeUpdate();
		} catch (final SQLException e) {
			throw EXCEPTIONS.create(0,e,getStatement(stmt));
		} finally {
			close(stmt,null);
			releaseWriteConnection(ctx, con);
		}
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
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	private static interface FieldChooser {
		public Table choose(Metadata m);
	}
	
	private static class VersionWins implements FieldChooser {

		public Table choose(final Metadata m) {
			if(Table.INFOSTORE_DOCUMENT.getFieldSet().contains(m)) {
				return Table.INFOSTORE_DOCUMENT;
			}
			return Table.INFOSTORE;
		}
		
	}
	
	private static class DocumentWins implements FieldChooser {
		public Table choose(final Metadata m) {
			if(Table.INFOSTORE.getFieldSet().contains(m)) {
				return Table.INFOSTORE;
			}
			return Table.INFOSTORE_DOCUMENT;
		}
	}
	
}
