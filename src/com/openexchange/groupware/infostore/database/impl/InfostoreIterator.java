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
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreExceptionFactory;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.database.impl.InfostoreQueryCatalog.FieldChooser;
import com.openexchange.groupware.infostore.database.impl.InfostoreQueryCatalog.Table;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.groupware.infostore.Classes;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.AbstractOXException.Category;

@OXExceptionSource(
	classId = Classes.COM_OPENEXCHANGE_GROUPWARE_INFOSTORE_DATABASE_IMPL_INFOSTOREITERATOR,
	component = Component.INFOSTORE
)
public class InfostoreIterator implements SearchIterator {
	
	private static final InfostoreQueryCatalog QUERIES = InfostoreFacadeImpl.QUERIES;
	
	private static final Log LOG = LogFactory.getLog(InfostoreIterator.class);
	
	public static InfostoreIterator loadDocumentIterator(int id, int version, DBProvider provider, Context ctx) {
		String query = QUERIES.getSelectDocument(id, version, ctx.getContextId());
		return new InfostoreIterator(query, provider, ctx, Metadata.VALUES_ARRAY, QUERIES.getChooserForVersion(version));
	}
	
	public static InfostoreIterator list(int[] id, Metadata[] metadata, DBProvider provider, Context ctx) {
		String query = QUERIES.getListQuery(id,metadata,new InfostoreQueryCatalog.DocumentWins(),ctx.getContextId());
		return new InfostoreIterator(query, provider, ctx, metadata, new InfostoreQueryCatalog.DocumentWins());
	}
	
	public static InfostoreIterator documents(long folderId, Metadata[] metadata,Metadata sort, int order, DBProvider provider, Context ctx){
		String query = QUERIES.getDocumentsQuery(folderId, metadata, sort, order, new InfostoreQueryCatalog.DocumentWins(), ctx.getContextId());
		return new InfostoreIterator(query, provider, ctx, metadata, new InfostoreQueryCatalog.DocumentWins());
	}
	
	public static InfostoreIterator documentsByCreator(long folderId,int userId, Metadata[] metadata,Metadata sort, int order, DBProvider provider, Context ctx){
		String query = QUERIES.getDocumentsQuery(folderId,userId, metadata, sort, order, new InfostoreQueryCatalog.DocumentWins(), ctx.getContextId());
		return new InfostoreIterator(query, provider, ctx, metadata, new InfostoreQueryCatalog.DocumentWins());
	}
	
	public static InfostoreIterator versions(int id, Metadata[] metadata, Metadata sort, int order, DBProvider provider, Context ctx) {
		String query = QUERIES.getVersionsQuery(id, metadata, sort, order, new InfostoreQueryCatalog.VersionWins(), ctx.getContextId());
		return new InfostoreIterator(query, provider, ctx, metadata, new InfostoreQueryCatalog.VersionWins());
	}
	
	public static InfostoreIterator newDocuments(long folderId, Metadata[] metadata, Metadata sort, int order, long since, DBProvider provider, Context ctx) {
		String query = QUERIES.getNewDocumentsQuery(folderId,since, metadata, sort, order, new InfostoreQueryCatalog.DocumentWins(), ctx.getContextId());
		return new InfostoreIterator(query, provider, ctx, metadata, new InfostoreQueryCatalog.DocumentWins());
	}
	
	public static InfostoreIterator modifiedDocuments(long folderId, Metadata[] metadata, Metadata sort, int order, long since, DBProvider provider, Context ctx) {
		String query = QUERIES.getModifiedDocumentsQuery(folderId,since, metadata, sort, order, new InfostoreQueryCatalog.DocumentWins(), ctx.getContextId());
		return new InfostoreIterator(query, provider, ctx, metadata, new InfostoreQueryCatalog.DocumentWins());	
	}
	
	public static InfostoreIterator deletedDocuments(long folderId, Metadata sort, int order, long since, DBProvider provider, Context ctx) {
		String query = QUERIES.getDeletedDocumentsQuery(folderId,since, sort, order, new InfostoreQueryCatalog.DocumentWins(), ctx.getContextId());
		return new InfostoreIterator(query, provider, ctx, new Metadata[]{Metadata.ID_LITERAL}, new InfostoreQueryCatalog.DocumentWins());
	}
	
	public static InfostoreIterator newDocumentsByCreator(long folderId,int userId, Metadata[] metadata, Metadata sort, int order, long since, DBProvider provider, Context ctx) {
		String query = QUERIES.getNewDocumentsQuery(folderId,userId, since, metadata, sort, order, new InfostoreQueryCatalog.DocumentWins(), ctx.getContextId());
		return new InfostoreIterator(query, provider, ctx, metadata, new InfostoreQueryCatalog.DocumentWins());
	}
	
	public static InfostoreIterator modifiedDocumentsByCreator(long folderId,int userId, Metadata[] metadata, Metadata sort, int order, long since, DBProvider provider, Context ctx) {
		String query = QUERIES.getModifiedDocumentsQuery(folderId,userId, since, metadata, sort, order, new InfostoreQueryCatalog.DocumentWins(), ctx.getContextId());
		return new InfostoreIterator(query, provider, ctx, metadata, new InfostoreQueryCatalog.DocumentWins());	
	}
	
	public static InfostoreIterator deletedDocumentsByCreator(long folderId,int userId, Metadata sort, int order, long since, DBProvider provider, Context ctx) {
		String query = QUERIES.getDeletedDocumentsQuery(folderId,userId, since, sort, order, new InfostoreQueryCatalog.DocumentWins(), ctx.getContextId());
		return new InfostoreIterator(query, provider, ctx, new Metadata[]{Metadata.ID_LITERAL}, new InfostoreQueryCatalog.DocumentWins());
	}
	
	public static InfostoreIterator allDocumentsWhere(String where, Metadata[] metadata, DBProvider provider, Context ctx){
		String query = QUERIES.getAllDocumentsQuery(where,metadata,new InfostoreQueryCatalog.DocumentWins(), ctx.getContextId());
		return new InfostoreIterator(query, provider, ctx, metadata, new InfostoreQueryCatalog.DocumentWins());
	}
	
	public static InfostoreIterator allVersionsWhere(String where, Metadata[] metadata, DBProvider provider, Context ctx){
		String query = QUERIES.getAllVersionsQuery(where,metadata,new InfostoreQueryCatalog.VersionWins(), ctx.getContextId());
		return new InfostoreIterator(query, provider, ctx, metadata, new InfostoreQueryCatalog.VersionWins());	
	}
	
	public static InfostoreIterator documentsByFilename(long folderId, String filename, Metadata[] metadata, DBProvider provider, Context ctx){
		String query = QUERIES.getCurrentFilenameQuery(folderId,metadata,new InfostoreQueryCatalog.DocumentWins(), ctx.getContextId());
		return new InfostoreIterator(query, provider, ctx, metadata, new InfostoreQueryCatalog.DocumentWins(), filename);		
	}
	
	private static final InfostoreExceptionFactory EXCEPTIONS = new InfostoreExceptionFactory(InfostoreIterator.class);

	private Object[] args;
	private DBProvider provider;
	private String query;
	private boolean queried;
	private boolean initNext;
	private ResultSet rs;
	private boolean next;
	private AbstractOXException exception;

	private Context ctx;

	private Metadata[] fields;

	private FieldChooser chooser;

	
	protected InfostoreIterator(String query,DBProvider provider, Context ctx, Metadata[] fields, FieldChooser chooser, Object...args){
		this.query = query;
		this.provider = provider;
		this.args = args;
		this.ctx = ctx;
		this.fields = fields;
		this.chooser = chooser;
	}
	
	@OXThrows(
			category = Category.INTERNAL_ERROR,
			desc = "Can't close database connection",
			exceptionId = 2,
			msg = "Can't close database connection"
	)
	public void close() throws SearchIteratorException {
		if(rs == null)
			return;
		Connection con;
		try {
			con = rs.getStatement().getConnection();
			DBUtils.closeSQLStuff(rs, rs.getStatement());
			provider.releaseReadConnection(ctx, con);
			rs = null;
		} catch (SQLException e) {
			throw new SearchIteratorException(EXCEPTIONS.create(2));
		}
	}

	@OXThrows(
			category = Category.TRY_AGAIN,
			desc = "Could not fetch result from result set. Probably the database may be busy or not running. Please try again.",
			exceptionId = 0,
			msg = "Could not fetch result from result set. Probably the database may be busy or not running. Please try again."
	)
	public boolean hasNext() {
		if(!queried)
			query();
		if(exception != null)
			return true;
		if(initNext)
			try {
				next = rs.next();
				if(!next)
					close();
			} catch (SQLException e) {
				this.exception = EXCEPTIONS.create(0,e);
			} catch (SearchIteratorException e) {
				this.exception=e;
			}
		initNext = false;
		return next;
	}
	
	
	@OXThrows(
			category = Category.CODE_ERROR,
			desc = "An invalid query was sent to the database.",
			exceptionId = 1,
			msg = "Invalid SQL Query: %s"
	)
	private void query() {
		queried = true;
		initNext=true;
		Connection con = null;
		PreparedStatement stmt = null;
		try{
			con = provider.getReadConnection(ctx);
			stmt = con.prepareStatement(query);
			int i = 1;
			for(Object arg : args) {
				stmt.setObject(i++,arg);
			}
			if(LOG.isTraceEnabled()) {
				LOG.trace(stmt.toString());
			}
			//System.out.println(stmt.toString());
			rs = stmt.executeQuery();
		} catch (SQLException x) {
			if(stmt != null)
				DBUtils.closeSQLStuff(null, stmt);
			if(con != null)
				provider.releaseReadConnection(ctx, con);
			this.exception = EXCEPTIONS.create(1,x, DBUtils.getStatement(stmt,query));
		} catch (TransactionException e) {
			this.exception =e;
		}
	}

	public boolean hasSize() {
		return false;
	}

	public DocumentMetadata next() throws SearchIteratorException, OXException {
		hasNext();
		if(exception != null)
			throw new SearchIteratorException(exception);
		initNext = true;
		
		return getDocument();
	}

	@OXThrows(
			category = Category.TRY_AGAIN,
			desc = "Could not fetch result from result set. Probably the database may be busy or not running. Please try again.",
			exceptionId = 3,
			msg = "Could not fetch result from result set. Probably the database may be busy or not running. Please try again."
	)
	private DocumentMetadata getDocument() throws SearchIteratorException{
		DocumentMetadata dm = new DocumentMetadataImpl();
		SetSwitch set = new SetSwitch(dm);
SetValues:	for(Metadata m : fields) {
			if(m == Metadata.CURRENT_VERSION_LITERAL) {
				try {
					dm.setIsCurrentVersion(rs.getBoolean("current_version"));
					continue SetValues;
				} catch (SQLException e) {
					throw new SearchIteratorException(EXCEPTIONS.create(3,e));
				}
			}
			Table t = chooser.choose(m);
			String colName = (String) m.doSwitch(t.getFieldSwitcher());
			if(colName == null)
				continue;
			try {
				set.setValue(process(m, rs.getObject(new StringBuilder(t.getTablename()).append('.').append(colName).toString())));
			} catch (SQLException e) {
				throw new SearchIteratorException(EXCEPTIONS.create(3,e));
			}
			m.doSwitch(set);
		}
		return dm;
	}

	private Object process(Metadata m, Object object) {
		switch(m.getId()) {
		default : return object;
		case Metadata.LAST_MODIFIED : case Metadata.CREATION_DATE : return new Date((Long)object);
		case Metadata.MODIFIED_BY : case Metadata.CREATED_BY : case Metadata.VERSION : case Metadata.ID:case  Metadata.COLOR_LABEL:
			return ((Long)object).intValue();
		}
	}

	public int size() {
		throw new UnsupportedOperationException();
	}
	
	
	public List<DocumentMetadata> asList() throws SearchIteratorException, OXException{
		try {
			List<DocumentMetadata> result = new ArrayList<DocumentMetadata>();
			while(hasNext()) {
				result.add(next());
			}
			
			return result;	
		} finally {
			close();
		}
	}

}
