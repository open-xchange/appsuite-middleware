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

package com.openexchange.groupware.infostore.search.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.Classes;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreExceptionFactory;
import com.openexchange.groupware.infostore.SearchEngine;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.database.impl.InfostoreSecurityImpl;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.groupware.tx.DBService;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.iterator.SearchIteratorException.SearchIteratorCode;
import com.openexchange.tools.oxfolder.OXFolderTools;


/**
 * SearchEngineImpl
 * @author <a href="mailto:benjamin.otterbach@open-xchange.com">Benjamin Otterbach</a>
 */

@OXExceptionSource(
		classId=Classes.COM_OPENEXCHANGE_GROUPWARE_INFOSTORE_SEARCH_IMPL_SEARCHENGINEIMPL,
		component=EnumComponent.INFOSTORE
)
public class SearchEngineImpl extends DBService implements SearchEngine {	
	
	private static final Log LOG = LogFactory.getLog(SearchEngineImpl.class);
	private static final InfostoreExceptionFactory EXCEPTIONS = new InfostoreExceptionFactory(SearchEngineImpl.class);
	
	private final InfostoreSecurityImpl security = new InfostoreSecurityImpl();
	
	// private static final Log LOG = LogFactory.getLog(SearchEngineImpl.class);
	
	private static final String[] SEARCH_FIELDS = new String[] {
		"infostore_document.title",
		"infostore_document.url",
		"infostore_document.description",
		"infostore_document.categories",
		"infostore_document.filename",
		"infostore_document.file_version_comment"
	};

	public SearchEngineImpl() { 
		super(null);
	}
	
	public SearchEngineImpl(final DBProvider provider) {
		super(provider);
		security.setProvider(provider);
	}
	
	@Override
	public void setProvider(final DBProvider provider) {
		super.setProvider(provider);
		if(security!=null) {
			security.setProvider(provider);
		}
	}

	@OXThrowsMultiple(
			category={Category.CODE_ERROR, Category.TRY_AGAIN}, 
			
			desc={"Indicates a faulty SQL Query. Only R&D can fix this", "Thrown when a result cannot be prefetched. This indicates a problem with the DB Connection. Have a look at the underlying SQLException"}, 
			
			exceptionId={0,1}, 
			
			msg={"Incorrect SQL Query: %s", "Cannot pre-fetch results."}
	)
	public SearchIterator search(String query, final Metadata[] cols, final int folderId, final Metadata sortedBy, final int dir, final int start, final int end, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
		
		List<Integer> all = new ArrayList<Integer>();
        List<Integer> own = new ArrayList<Integer>();
        try {
            final int userId = user.getId();
            if (folderId == NOT_SET || folderId == NO_FOLDER) {
                final SearchIterator iter = OXFolderTools
                    .getAllVisibleFoldersIteratorOfModule(userId,
                        user.getGroups(), userConfig.getAccessibleModules(),
                        FolderObject.INFOSTORE, ctx);
                while (iter.hasNext()) {
                    final FolderObject folder = (FolderObject) iter.next();
                    final EffectivePermission perm = security.getFolderPermission(folder.getObjectID(), ctx, user, userConfig);
                    if (perm.canReadOwnObjects() && !perm.canReadAllObjects()) {
                        own.add(Integer.valueOf(folder.getObjectID()));
                    } else if (perm.canReadAllObjects()){
                        all.add(Integer.valueOf(folder.getObjectID()));
                    }
                }
            } else {
                final EffectivePermission perm = security.getFolderPermission(folderId, ctx, user, userConfig);
                if (perm.canReadOwnObjects() && !perm.canReadAllObjects()) {
                    own.add(Integer.valueOf(folderId));
                } else if (perm.canReadAllObjects()){
                    all.add(Integer.valueOf(folderId));
                } else {
                	return SearchIteratorAdapter.EMPTY_ITERATOR;
                }
            }
            all = Collections.unmodifiableList(all);
            own = Collections.unmodifiableList(own);
        } catch (final SearchIteratorException e) {
            throw new OXException(e);
        }
		
        if(all.isEmpty() && own.isEmpty()) {
        	return SearchIteratorAdapter.EMPTY_ITERATOR;
        }
        
		final StringBuilder SQL_QUERY = new StringBuilder();
		SQL_QUERY.append(getResultFieldsSelect(cols));
		SQL_QUERY.append(" FROM infostore JOIN infostore_document ON infostore_document.cid = infostore.cid AND infostore_document.infostore_id = infostore.id AND infostore_document.version_number = infostore.version WHERE infostore.cid = ")
		.append(ctx.getContextId());
		boolean needOr = false;
		
		if(!all.isEmpty()) {
			SQL_QUERY.append(" AND ((infostore.folder_id IN (").append(join(all)).append("))");
			needOr = true;
		}
		
		if(!own.isEmpty()) {
			if(needOr) {
				SQL_QUERY.append(" OR ");
			} else {
				SQL_QUERY.append(" AND (");
			}
			SQL_QUERY.append("(infostore.created_by = ").append(user.getId()).append(" AND infostore.folder_id in (").append(join(own)).append(")))");
 		} else {
 			SQL_QUERY.append(')');
 		}
		
		if(!query.equals("") && !query.equals("*") ) {
			
			query = query.replace('*', '%');
			query = query.replace('?', '_');
			query = query.replaceAll("'", "\\\\'");
	
			final StringBuffer SQL_QUERY_OBJECTS = new StringBuffer();
			for (final String currentField : SEARCH_FIELDS) {
				if (SQL_QUERY_OBJECTS.length() > 0) {
					SQL_QUERY_OBJECTS.append(" OR ");
				}
				
				if (query.indexOf('%') != -1) {
					SQL_QUERY_OBJECTS.append(currentField);
					SQL_QUERY_OBJECTS.append(" LIKE ('");
					SQL_QUERY_OBJECTS.append(query);
					SQL_QUERY_OBJECTS.append("')");
				} else {
					SQL_QUERY_OBJECTS.append(currentField);
					SQL_QUERY_OBJECTS.append(" LIKE ('%");
					SQL_QUERY_OBJECTS.append(query);
					SQL_QUERY_OBJECTS.append("%')");
				}
			}
			if (SQL_QUERY_OBJECTS.length() > 0) {
				SQL_QUERY.append(" AND (");
				SQL_QUERY.append(SQL_QUERY_OBJECTS);
				SQL_QUERY.append(") ");
			}
		}
		
		if (sortedBy != null && dir != NOT_SET) {
			final String[] orderColumn = switchMetadata2DBColumns(new Metadata[] { sortedBy });
			if ((orderColumn != null) && (orderColumn[0] != null)) {
				if (dir == DESC) {
					SQL_QUERY.append(" ORDER BY ");
					SQL_QUERY.append(orderColumn[0]);
					SQL_QUERY.append(" DESC");
				} else if (dir == ASC) {
					SQL_QUERY.append(" ORDER BY ");
					SQL_QUERY.append(orderColumn[0]);
					SQL_QUERY.append(" ASC");
				}				
			}
		}
		
		if ((start != NOT_SET) && (end != NOT_SET)) {
			if (end >= start) {
				SQL_QUERY.append(" LIMIT ");
				SQL_QUERY.append(start);
				SQL_QUERY.append(", ");
				SQL_QUERY.append(((end+1)-start));
			}
		} else {
			if (start != NOT_SET) {
				SQL_QUERY.append(" LIMIT ");
				SQL_QUERY.append(start);
				SQL_QUERY.append(",200");
			}
			if (end != NOT_SET) {
				SQL_QUERY.append(" LIMIT ");
				SQL_QUERY.append(end+1);
			}
		}
		
		final Connection con = getReadConnection(ctx);
		Statement stmt = null;
		try {
			stmt = con.createStatement();
			return new InfostoreSearchIterator(stmt.executeQuery(SQL_QUERY.toString()), this, cols, ctx, con, stmt);
		} catch (final SQLException e) {
			LOG.error("",e);
			throw EXCEPTIONS.create(0,e,SQL_QUERY.toString());
		} catch (final SearchIteratorException e) {
			LOG.error("",e);
			throw EXCEPTIONS.create(1,e);
		}
	}

	private String join(final List<Integer> all) {
		final StringBuffer joined = new StringBuffer();
		for(final Integer i : all) {
			joined.append(i.toString());
			joined.append(',');
		}
		joined.setLength(joined.length()-1);
		return joined.toString();
	}

	public void index(final DocumentMetadata document, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException { }
	
	public void unIndex0r(final int id, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException { }
	
	private String[] switchMetadata2DBColumns(final Metadata[] columns) {
		final List<String> retval = new ArrayList<String>();
		for (final Metadata current : columns) {
			Metadata2DBSwitch : switch(current.getId()) {
			default : break Metadata2DBSwitch;
			case Metadata.LAST_MODIFIED : retval.add("infostore.last_modified"); break Metadata2DBSwitch;
			case Metadata.CREATION_DATE : retval.add("infostore.creating_date"); break Metadata2DBSwitch;
			case Metadata.MODIFIED_BY : retval.add("infostore.changed_by"); break Metadata2DBSwitch;
			case Metadata.FOLDER_ID : retval.add("infostore.folder_id"); break Metadata2DBSwitch;
			case Metadata.TITLE : retval.add("infostore_document.title"); break Metadata2DBSwitch;
			case Metadata.VERSION : retval.add("infostore.version"); break Metadata2DBSwitch;
			case Metadata.CONTENT : retval.add("infostore_document.description"); break Metadata2DBSwitch;
			case Metadata.FILENAME : retval.add("infostore_document.filename"); break Metadata2DBSwitch;
			case Metadata.SEQUENCE_NUMBER : retval.add("infostore.id"); break Metadata2DBSwitch;
			case Metadata.ID : retval.add("infostore.id"); break Metadata2DBSwitch;
			case Metadata.FILE_SIZE : retval.add("infostore_document.file_size"); break Metadata2DBSwitch;
			case Metadata.FILE_MIMETYPE : retval.add("infostore_document.file_mimetype"); break Metadata2DBSwitch;
			case Metadata.DESCRIPTION : retval.add("infostore_document.description"); break Metadata2DBSwitch;
			case Metadata.LOCKED_UNTIL : retval.add("infostore.locked_until"); break Metadata2DBSwitch;
			case Metadata.URL : retval.add("infostore_document.url"); break Metadata2DBSwitch;
			case Metadata.CREATED_BY : retval.add("infostore.created_by"); break Metadata2DBSwitch;
			case Metadata.CATEGORIES : retval.add("infostore_document.categories"); break Metadata2DBSwitch;
			case Metadata.FILE_MD5SUM : retval.add("infostore_document.file_md5sum"); break Metadata2DBSwitch;
			case Metadata.VERSION_COMMENT : retval.add("infostore_document.file_version_comment"); break Metadata2DBSwitch;
			case Metadata.COLOR_LABEL : retval.add("infostore.color_label"); break Metadata2DBSwitch;
			}
		}
		return(retval.toArray(new String[0]));
	}
	
	private String getResultFieldsSelect(final Metadata[] RESULT_FIELDS) {
		final String[] DB_RESULT_FIELDS = switchMetadata2DBColumns(RESULT_FIELDS);
		
		final StringBuilder selectFields = new StringBuilder();
		boolean id = false;
		for (String currentField : DB_RESULT_FIELDS) {
			if(currentField.equals("infostore.id")){
				currentField = "infostore.id";
				id = true;
			}
			selectFields.append(currentField);
			selectFields.append(", ");
		}
		if (!id) {
			selectFields.append("infostore.id,");
		}
		
		String retval = "";
		if (selectFields.length() > 0) {
			retval = "SELECT DISTINCT " + selectFields.toString();
			retval = retval.substring(0, retval.lastIndexOf(", "));
		}
		return retval;
	}
	
	public static class InfostoreSearchIterator implements SearchIterator {

		private Object next;
		private ResultSet rs ;
		private Metadata[] columns;
		private SearchEngineImpl s;
		private Context ctx;
		private Connection readCon;
		private Statement stmt;
		
		public InfostoreSearchIterator(final ResultSet rs, final SearchEngineImpl s, final Metadata[] columns, final Context ctx, final Connection readCon, final Statement stmt) throws SearchIteratorException {
			this.rs = rs;
			this.s = s;
			this.columns = columns;
			this.ctx = ctx;
			this.readCon = readCon;
			this.stmt = stmt;
			try {
				if (rs.next()) {
					next = fillDocumentMetadata(new DocumentMetadataImpl(), columns, rs);
				} else {
					close();
				}
			} catch (final Exception e) {
				throw new SearchIteratorException(SearchIteratorCode.SQL_ERROR,e,EnumComponent.INFOSTORE);
			}
		}
		
		public boolean hasNext() {
			return next != null;
		}

		public Object next() throws SearchIteratorException {
			try {
				Object retval = null;
				retval = next;
				if (rs.next()) {
					next = fillDocumentMetadata(new DocumentMetadataImpl(), columns, rs);
					NextObject: while (next == null) {
						if (rs.next()) {
							next = fillDocumentMetadata(new DocumentMetadataImpl(), columns, rs);
						} else {
							break NextObject;
						}
					}
					if (next == null) {
						close();
					}
				} else {
					close();
				}
				return retval;
			} catch (final Exception exc) {
				throw new SearchIteratorException(SearchIteratorCode.SQL_ERROR,exc,EnumComponent.INFOSTORE);
			}
		}

		public void close() throws SearchIteratorException {
			next = null;
			try {
				if (rs != null) {
					rs.close();
				}
				rs = null;
			} catch (final SQLException e) {
				LOG.debug("",e);
			}
			
			try {
				if( stmt != null ) {
					stmt.close();
				}
				stmt = null;
			} catch (final SQLException e) {
				LOG.debug("",e);
			}
			
			s.releaseReadConnection(ctx, readCon);
		}
		
		public int size() {
			throw new UnsupportedOperationException("Mehtod size() not implemented");
		}
		
		public boolean hasSize() {
			return false;
		}
		
		private DocumentMetadataImpl fillDocumentMetadata(final DocumentMetadataImpl retval, final Metadata[] columns, final ResultSet result) throws SQLException {
			for (int i = 0; i < columns.length; i++) {
				FillDocumentMetadata : switch(columns[i].getId()) {
				default : break FillDocumentMetadata;
				case Metadata.LAST_MODIFIED : retval.setLastModified(new Date(result.getLong(i+1))); break FillDocumentMetadata;
				case Metadata.CREATION_DATE : retval.setCreationDate(new Date(result.getLong(i+1))); break FillDocumentMetadata;
				case Metadata.MODIFIED_BY : retval.setModifiedBy(result.getInt(i+1)); break FillDocumentMetadata;
				case Metadata.FOLDER_ID : retval.setFolderId(result.getInt(i+1)); break FillDocumentMetadata;
				case Metadata.TITLE : retval.setTitle(result.getString(i+1)); break FillDocumentMetadata;
				case Metadata.VERSION : retval.setVersion(result.getInt(i+1)); break FillDocumentMetadata;
				case Metadata.CONTENT : retval.setDescription(result.getString(i+1)); break FillDocumentMetadata;
				case Metadata.FILENAME : retval.setFileName(result.getString(i+1)); break FillDocumentMetadata;
				case Metadata.SEQUENCE_NUMBER : retval.setId(result.getInt(i+1)); break FillDocumentMetadata;
				case Metadata.ID : retval.setId(result.getInt(i+1)); break FillDocumentMetadata;
				case Metadata.FILE_SIZE : retval.setFileSize(result.getInt(i+1)); break FillDocumentMetadata;
				case Metadata.FILE_MIMETYPE : retval.setFileMIMEType(result.getString(i+1)); break FillDocumentMetadata;
				case Metadata.DESCRIPTION : retval.setDescription(result.getString(i+1)); break FillDocumentMetadata;
				case Metadata.LOCKED_UNTIL : 
					retval.setLockedUntil(new Date(result.getLong(i+1)));
					if (result.wasNull()) {
						retval.setLockedUntil(null);
					}
					break FillDocumentMetadata;
				case Metadata.URL : retval.setURL(result.getString(i+1)); break FillDocumentMetadata;
				case Metadata.CREATED_BY : retval.setCreatedBy(result.getInt(i+1)); break FillDocumentMetadata;
				case Metadata.CATEGORIES : retval.setCategories(result.getString(i+1)); break FillDocumentMetadata;
				case Metadata.FILE_MD5SUM : retval.setFileMD5Sum(result.getString(i+1)); break FillDocumentMetadata;
				case Metadata.VERSION_COMMENT : retval.setVersionComment(result.getString(i+1)); break FillDocumentMetadata;
				case Metadata.COLOR_LABEL : retval.setColorLabel(result.getInt(i+1)); break FillDocumentMetadata;
				}
			}
			retval.setIsCurrentVersion(true);
			
			return retval;
		}

	}
	
}
