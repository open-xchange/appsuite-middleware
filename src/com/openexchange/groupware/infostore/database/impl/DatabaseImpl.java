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

import static com.openexchange.tools.sql.DBUtils.getStatement;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api2.OXException;
import com.openexchange.event.EventClient;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.IDGenerator;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.UserConfiguration;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.ContextException;
import com.openexchange.groupware.delete.DeleteFailedException;
import com.openexchange.groupware.filestore.FilestoreException;
import com.openexchange.groupware.filestore.FilestoreStorage;
import com.openexchange.groupware.infostore.Classes;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreException;
import com.openexchange.groupware.infostore.InfostoreExceptionFactory;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.utils.DelUserFolderDiscoverer;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.infostore.webdav.EntityLockManager;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.DeltaImpl;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.results.TimedResultImpl;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.groupware.tx.DBService;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.groupware.tx.Undoable;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.file.FileStorage;
import com.openexchange.tools.file.FileStorageException;
import com.openexchange.tools.file.QuotaFileStorage;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.iterator.SearchIteratorException.SearchIteratorCode;
import com.openexchange.tools.oxfolder.OXFolderTools;

@OXExceptionSource(classId = Classes.COM_OPENEXCHANGE_GROUPWARE_INFOSTORE_DATABASE_IMPL_DATABASEIMPL, component = Component.INFOSTORE)
public class DatabaseImpl extends DBService {

	private static final Log LOG = LogFactory.getLog(DatabaseImpl.class);

	private static final InfostoreExceptionFactory EXCEPTIONS = new InfostoreExceptionFactory(
			DatabaseImpl.class);

	private final static int DOCUMENT_VERSION_NUMBER_WITHOUT_FILE = 0;

	protected static final int INFOSTORE_cid = 0;

	protected static final int INFOSTORE_id = 1;

	protected static final int INFOSTORE_folder_id = 2;

	protected static final int INFOSTORE_version = 3;

	protected static final int INFOSTORE_locked_until = 4;

	protected static final int INFOSTORE_creating_date = 5;

	protected static final int INFOSTORE_last_modified = 6;

	protected static final int INFOSTORE_created_by = 7;

	protected static final int INFOSTORE_changed_by = 8;

	protected static final int INFOSTORE_color_label = 9;

	protected static final int INFOSTORE_DOCUMENT_cid = 10;

	protected static final int INFOSTORE_DOCUMENT_infostore_id = 11;

	protected static final int INFOSTORE_DOCUMENT_version_number = 12;

	protected static final int INFOSTORE_DOCUMENT_creating_date = 13;

	protected static final int INFOSTORE_DOCUMENT_last_modified = 14;

	protected static final int INFOSTORE_DOCUMENT_created_by = 15;

	protected static final int INFOSTORE_DOCUMENT_changed_by = 16;

	protected static final int INFOSTORE_DOCUMENT_title = 17;

	protected static final int INFOSTORE_DOCUMENT_url = 18;

	protected static final int INFOSTORE_DOCUMENT_description = 19;

	protected static final int INFOSTORE_DOCUMENT_categories = 20;

	protected static final int INFOSTORE_DOCUMENT_filename = 21;

	protected static final int INFOSTORE_DOCUMENT_file_store_location = 22;

	protected static final int INFOSTORE_DOCUMENT_file_size = 23;

	protected static final int INFOSTORE_DOCUMENT_file_mimetype = 24;

	protected static final int INFOSTORE_DOCUMENT_file_md5sum = 25;

	protected static final int INFOSTORE_DOCUMENT_file_version_comment = 26;

	private static enum Fetch {
		PREFETCH, CLOSE_LATER, CLOSE_IMMEDIATELY
	}

	private static final Fetch FETCH = Fetch.PREFETCH;

	private FetchMode fetchMode;

	public DatabaseImpl() {
		this(null);
	}

	public DatabaseImpl(DBProvider provider) {
		super(provider);

		switch (FETCH) {
		case PREFETCH:
			fetchMode = new PrefetchMode();
			break;
		case CLOSE_LATER:
			fetchMode = new CloseLaterMode();
			break;
		case CLOSE_IMMEDIATELY:
			fetchMode = new CloseImmediatelyMode();
			break;
		default:
			fetchMode = new PrefetchMode();
		}
	}

	private static final String[] INFOSTORE_DATACOLUMNS = new String[] {
			"infostore.cid", "infostore.id", "infostore.folder_id",
			"infostore.version", "infostore.locked_until",
			"infostore.creating_date", "infostore.last_modified",
			"infostore.created_by", "infostore.changed_by",
			"infostore.color_label", "infostore_document.cid",
			"infostore_document.infostore_id",
			"infostore_document.version_number",
			"infostore_document.creating_date",
			"infostore_document.last_modified",
			"infostore_document.created_by", "infostore_document.changed_by",
			"infostore_document.title", "infostore_document.url",
			"infostore_document.description", "infostore_document.categories",
			"infostore_document.filename",
			"infostore_document.file_store_location",
			"infostore_document.file_size", "infostore_document.file_mimetype",
			"infostore_document.file_md5sum",
			"infostore_document.file_version_comment" };

	private ThreadLocal<List<String>> fileIdAddList = new ThreadLocal<List<String>>();

	private ThreadLocal<List<String>> fileIdRemoveList = new ThreadLocal<List<String>>();

	private ThreadLocal<Context> ctxHolder = new ThreadLocal<Context>();

	@OXThrows(category = Category.CODE_ERROR, desc = "A faulty SQL Query was sent to the SQL server. This can only be fixed in R&D", exceptionId = 0, msg = "Invalid SQL Query: %s")
	public boolean exists(int id, int version, Context ctx, User user,
			UserConfiguration userConfig) throws OXException {
		boolean retval = false;

		Connection con = getReadConnection(ctx);

		StringBuilder sql = new StringBuilder();
		if (version == InfostoreFacade.CURRENT_VERSION) {
			sql.append("SELECT id from infostore WHERE cid=? AND id=?");
		} else {
			sql
					.append("SELECT infostore_id FROM infostore_document WHERE cid=? AND infostore_id=? AND version_number=?");
		}
		PreparedStatement stmt = null;
		ResultSet result = null;
		try {
			stmt = con.prepareStatement(sql.toString());
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, id);
			if (version != InfostoreFacade.CURRENT_VERSION) {
				stmt.setInt(3, version);
			}
			result = stmt.executeQuery();
			if (result.next()) {
				retval = true;
			}
			result.close();
			stmt.close();
		} catch (SQLException e) {
			LOG.error("", e);
			throw EXCEPTIONS.create(0, e, getStatement(stmt));
		} finally {
			close(stmt, result);
			releaseReadConnection(ctx, con);
		}

		return retval;
	}

	@OXThrows(category = Category.CODE_ERROR, desc = "A faulty SQL Query was sent to the SQL server. This can only be fixed in R&D", exceptionId = 1, msg = "Invalid SQL Query: %s")
	public DocumentMetadata getDocumentMetadata(int id, int version,
			Context ctx, User user, UserConfiguration userConfig)
			throws OXException {
		DocumentMetadataImpl dmi = new DocumentMetadataImpl();
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet result = null;
		String versionString = null;
		if (version == InfostoreFacade.CURRENT_VERSION) {
			versionString = "infostore.version";
		} else {
			versionString = String.valueOf(version);
		}
		try {
			int[] columns = switchMetadata2DBColumns(
					(Metadata[]) Metadata.VALUES_ARRAY, false);
			con = getReadConnection(ctx);
			stmt = con
					.prepareStatement(getSQLSelectForInfostoreColumns(columns,
							false).toString()
							+ " FROM infostore JOIN infostore_document ON infostore.cid=? AND infostore.id=? AND infostore_document.infostore_id=infostore.id AND infostore_document.cid=? AND infostore_document.version_number = "
							+ versionString);
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, id);
			stmt.setInt(3, ctx.getContextId());
			result = stmt.executeQuery();
			if (result.next()) {
				dmi = fillDocumentMetadata(dmi = new DocumentMetadataImpl(),
						columns, result);
			}
			result.close();
			stmt.close();
		} catch (SQLException e) {
			LOG.error("", e);
			throw EXCEPTIONS.create(1, e, getStatement(stmt));
		} finally {
			close(stmt, result);
			if (con != null)
				releaseReadConnection(ctx, con);
		}

		if (dmi != null)
			dmi.setSequenceNumber(System.currentTimeMillis());

		return dmi;
	}

	public List<DocumentMetadata> getAllVersions(Context ctx,
			Metadata[] columns, String where) throws OXException {
		List<DocumentMetadata> result = new ArrayList<DocumentMetadata>();

		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			con = getReadConnection(ctx);
			stmt = con
					.prepareStatement(getSQLSelectForInfostoreColumns(
							switchMetadata2DBColumns(columns, true), false)
							.toString()
							+ " FROM infostore JOIN infostore_document ON infostore.cid=? AND infostore_document.infostore_id=infostore.id AND infostore_document.cid=? WHERE "
							+ where);
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, ctx.getContextId());
			rs = stmt.executeQuery();
			while (rs.next()) {
				DocumentMetadataImpl dm = new DocumentMetadataImpl();
				fillDocumentMetadata(dm,
						switchMetadata2DBColumns(columns, true), rs);
				result.add(dm);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			close(stmt, rs);
			releaseReadConnection(ctx, con);
		}

		return result;
	}

	public List<DocumentMetadata> getAllDocuments(Context ctx,
			Metadata[] columns, String where) throws OXException {
		List<DocumentMetadata> result = new ArrayList<DocumentMetadata>();

		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			con = getReadConnection(ctx);
			stmt = con
					.prepareStatement(getSQLSelectForInfostoreColumns(
							switchMetadata2DBColumns(columns, false), false)
							.toString()
							+ " FROM infostore JOIN infostore_document ON infostore.cid=? AND infostore_document.infostore_id=infostore.id AND infostore_document.cid=? AND infostore_document.version_number = infostore.version WHERE "
							+ where);
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, ctx.getContextId());
			rs = stmt.executeQuery();
			while (rs.next()) {
				DocumentMetadataImpl dm = new DocumentMetadataImpl();
				fillDocumentMetadata(dm,
						switchMetadata2DBColumns(columns, true), rs);
				result.add(dm);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			close(stmt, rs);
			releaseReadConnection(ctx, con);
		}

		return result;
	}

	@OXThrowsMultiple(category = { Category.CODE_ERROR,
			Category.SUBSYSTEM_OR_SERVICE_DOWN, Category.CODE_ERROR }, desc = {
			"A faulty SQL Query was sent to the SQL server. This can only be fixed in R&D",
			"This indicates a problem accessing the underlying filestorage. Look at the exceptions given as cause for this one.",
			"The context specific data about a filestorage could not be loaded. Look at the underlying exceptions for a hint." }, exceptionId = {
			2, 3, 4 }, msg = { "Invalid SQL Query: %s",
			"Could not access file store.", "Could not get file store location." })
	public InputStream getDocument(int id, int version, Context ctx, User user,
			UserConfiguration userConfig) throws OXException {
		InputStream retval = null;

		StringBuilder sql = new StringBuilder();
		if (version != -1) {
			sql
					.append("SELECT file_store_location FROM infostore_document WHERE cid=? AND infostore_id=? AND version_number=? AND file_store_location is not null");
		} else {
			sql
					.append("SELECT infostore_document.file_store_location from infostore_document JOIN infostore ON infostore.cid=? AND infostore.id=? AND infostore_document.cid=? AND infostore_document.infostore_id=? AND infostore_document.version_number=infostore.version AND file_store_location is not null");
		}
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet result = null;
		try {
			con = getReadConnection(ctx);

			stmt = con.prepareStatement(sql.toString());
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, id);
			if (version != -1) {
				stmt.setInt(3, version);
			} else {
				stmt.setInt(3, ctx.getContextId());
				stmt.setInt(4, id);
			}
			result = stmt.executeQuery();
			if (result.next()) {
				FileStorage fs = FileStorage.getInstance(FilestoreStorage.createURI(ctx), ctx, this
						.getProvider());
				retval = fs.getFile(result.getString(1));
				fs.close();
			}
		} catch (SQLException x) {
			throw EXCEPTIONS.create(2, x, getStatement(stmt));
		} catch (FileStorageException e) {
			throw new InfostoreException(e);
		} catch (FilestoreException e) {
			throw new InfostoreException(e);
		} finally {
			close(stmt, result);
			releaseReadConnection(ctx, con);
		}
		return retval;
	}

	@OXThrowsMultiple(category = { Category.CODE_ERROR,
			Category.CODE_ERROR }, desc = {
			"A faulty SQL Query was sent to the SQL server. This can only be fixed in R&D",
			"A faulty SQL Query was sent to the SQL server. This can only be fixed in R&D" }, exceptionId = {
			14, 15 }, msg = { "Invalid SQL Query: %s", "Invalid SQL Query: %s" })
	@Deprecated
	public Set<Integer> removeDocuments(Set<Integer> ids, long date,
			Context ctx, User user, UserConfiguration userConfig)
			throws OXException {
		Map<Integer, List<String>> filesAttachedToInfostoreObject = new HashMap<Integer, List<String>>();
		List<String> filesToDelete = new ArrayList<String>();
		Set<Integer> notDeletedDocuments = new HashSet<Integer>();

		if (ids.size() > 0) {
			StringBuilder getAllAttachedFileStoreLocationsSQL = new StringBuilder(
					"SELECT infostore_id, file_store_location FROM infostore_document WHERE infostore_id IN (");
			for (int current = 0; current < ids.size(); current++) {
				if (current > 0) {
					getAllAttachedFileStoreLocationsSQL.append(",");
				}
				getAllAttachedFileStoreLocationsSQL.append("?");
			}
			getAllAttachedFileStoreLocationsSQL
					.append(") AND cid=? AND file_store_location is not null ");

			Connection readCon = null;
			PreparedStatement stmt = null;
			ResultSet result = null;
			try {
				readCon = getReadConnection(ctx);
				stmt = readCon
						.prepareStatement(getAllAttachedFileStoreLocationsSQL
								.toString());
				Iterator<Integer> iter = ids.iterator();
				for (int current = 0; iter.hasNext(); current++) {
					stmt.setInt(current + 1, iter.next());
				}
				stmt.setInt(ids.size() + 1, ctx.getContextId());
				result = stmt.executeQuery();
				while (result.next()) {
					int currentInfostoreId = result.getInt(1);
					String currentInfostorePath = result.getString(2);
					List<String> currentIdList = new ArrayList<String>();
					if (filesAttachedToInfostoreObject
							.containsKey(currentInfostoreId)) {
						currentIdList = filesAttachedToInfostoreObject
								.get(currentInfostoreId);
					}
					currentIdList.add(currentInfostorePath);
					filesAttachedToInfostoreObject.put(currentInfostoreId,
							currentIdList);
				}
			} catch (SQLException e) {
				throw EXCEPTIONS.create(14, e, getStatement(stmt));
			} finally {
				close(stmt, result);
				releaseReadConnection(ctx, readCon);
			}
		}

		final StringBuilder deleteInfostoreDocumentVersions = new StringBuilder(
				"DELETE infostore_document.* " + "FROM infostore_document "
						+ "JOIN infostore ON "
						+ "infostore.id = infostore_document.infostore_id AND "
						+ "infostore_document.cid=? AND "
						+ "infostore.cid=? AND "
						+ "infostore_document.infostore_id=? AND "
						+ "infostore.last_modified <= ?");
		final StringBuilder deleteInfostoreDocument = new StringBuilder(
				"DELETE infostore.* " + "FROM infostore WHERE "
						+ "infostore.cid=? AND " + "infostore.id = ? AND "
						+ "infostore.last_modified <= ?");

		Connection writeCon = null;
		PreparedStatement stmt = null;
		for (int id : ids) {
			try {
				writeCon = getWriteConnection(ctx);
				copyRows(
						writeCon,
						"del_infostore",
						"SELECT cid, id, folder_id, version, color_label, creating_date, last_modified, created_by, changed_by FROM infostore WHERE cid = ? AND id = ?",
						ctx.getContextId(), id);
				stmt = writeCon
						.prepareStatement("UPDATE del_infostore SET last_modified=?, changed_by=? WHERE cid=? AND id=?");
				stmt.setLong(1, System.currentTimeMillis());
				stmt.setInt(2, user.getId());
				stmt.setInt(3, ctx.getContextId());
				stmt.setInt(4, id);
				stmt.execute();
				close(stmt, null);

				copyRows(
						writeCon,
						"del_infostore_document",
						"SELECT cid, infostore_id, version_number, creating_date, last_modified, created_by, changed_by, title, url, description, categories, filename, file_store_location, file_size, file_mimetype, file_md5sum, file_version_comment FROM infostore_document WHERE cid = ? AND infostore_id = ?",
						ctx.getContextId(), id);

				stmt = writeCon
						.prepareStatement("UPDATE del_infostore_document SET last_modified=?, changed_by=? WHERE cid=? AND infostore_id=?");
				stmt.setLong(1, System.currentTimeMillis());
				stmt.setInt(2, user.getId());
				stmt.setInt(3, ctx.getContextId());
				stmt.setInt(4, id);
				stmt.execute();
				close(stmt, null);

				stmt = writeCon
						.prepareStatement(deleteInfostoreDocumentVersions
								.toString());
				stmt.setInt(1, ctx.getContextId());
				stmt.setInt(2, ctx.getContextId());
				stmt.setInt(3, id);
				stmt.setLong(4, date);
				int status = stmt.executeUpdate();
				close(stmt, null);
				stmt = writeCon.prepareStatement(deleteInfostoreDocument
						.toString());
				stmt.setInt(1, ctx.getContextId());
				stmt.setInt(2, id);
				stmt.setLong(3, date);
				status += stmt.executeUpdate();
				close(stmt, null);
				if (status <= 0) {

					notDeletedDocuments.add(id);
					stmt = writeCon
							.prepareStatement("DELETE FROM del_infostore_document WHERE cid=? AND infostore_id=?");
					stmt.setInt(1, ctx.getContextId());
					stmt.setInt(2, id);
					stmt.execute();
					stmt.close();
					stmt = writeCon
							.prepareStatement("DELETE FROM del_infostore WHERE cid=? AND id=?");
					stmt.setInt(1, ctx.getContextId());
					stmt.setInt(2, id);
					stmt.execute();
				} else {
					if (filesAttachedToInfostoreObject.containsKey(id))
						filesToDelete.addAll(filesAttachedToInfostoreObject
								.get(id));
				}
			} catch (SQLException e) {
				LOG.error("", e);
				throw EXCEPTIONS.create(15, e, stmt.toString());
			} finally {
				close(stmt, null);
				releaseWriteConnection(ctx, writeCon);
			}
		}

		delFiles(filesToDelete, ctx);

		return notDeletedDocuments;
	}

	public int[] removeDocument(String identifier, Context ctx)
			throws OXException {
		return removeDocumentFromTable(identifier, ctx, "infostore");
	}

	public int[] removeDelDocument(String identifier, Context ctx)
			throws OXException {
		return removeDocumentFromTable(identifier, ctx, "del_infostore");
	}

	/**
	 * @param identifier
	 *            The file identifier
	 * @param ctx
	 *            The Context from which it should be deleted
	 * @param basetablename
	 *            The basename of the table e.g. del_infostore the fitting
	 *            database names (del_infostore_documents etc. are self-build
	 * @return The number of changed entries in the basetable in int[0] and the
	 *         number of deleted entries in int[1]
	 * @throws BackendException
	 */
	private int[] removeDocumentFromTable(String identifier, Context ctx,
			String basetablename) throws OXException {
		/*
		 * When deleting the fitting version line for the identifier we have to
		 * check the basetable, too. In the basetable we set the current version
		 * to the right value. This value is determined by the sorted set.
		 */
		String documentstable = basetablename.concat("_document");

		Connection writecon = getWriteConnection(ctx);

		int[] retval = new int[2];
		int version_nr = 0;
		int current_version = 0;
		int infostore_id = 0;

		final StringBuilder version_select = new StringBuilder(
				"SELECT version_number, infostore_id, version FROM "
						+ documentstable + " JOIN " + basetablename + " ON "
						+ basetablename + ".id = " + documentstable
						+ ".infostore_id " + " AND file_store_location=? AND "
						+ documentstable + ".cid=?");
		final StringBuilder query_all_versions = new StringBuilder(
				"SELECT version_number FROM " + documentstable
						+ " WHERE infostore_id=? AND cid=?");
		final StringBuilder deletefromdocumentstable = new StringBuilder(
				"DELETE FROM " + documentstable
						+ " WHERE cid=? AND file_store_location=?");
		final StringBuilder changeversioninbasetable = new StringBuilder(
				"UPDATE " + basetablename + " SET version=? "
						+ " WHERE id=? AND  " + basetablename + ".cid=?");

		try {
			PreparedStatement stmt = writecon.prepareStatement(version_select
					.toString());
			stmt.setString(1, identifier);
			stmt.setInt(2, ctx.getContextId());
			ResultSet result = stmt.executeQuery();
			if (result.next()) {
				version_nr = result.getInt(1);
				infostore_id = result.getInt(2);
				current_version = result.getInt(3);
			}
			result.close();
			stmt.close();

			// Now we have to check if the version number is the active one
			stmt = writecon.prepareStatement(query_all_versions.toString());
			stmt.setInt(1, infostore_id);
			stmt.setInt(2, ctx.getContextId());
			result = stmt.executeQuery();
			SortedSet<Integer> set = new TreeSet<Integer>();
			while (result.next()) {
				set.add(result.getInt(1));
			}
			set.remove(version_nr);

			if (version_nr == current_version) {
				stmt = writecon.prepareStatement(changeversioninbasetable
						.toString());
				stmt.setInt(1, set.last());
				stmt.setInt(2, infostore_id);
				stmt.setInt(3, ctx.getContextId());
				retval[0] = stmt.executeUpdate();
				stmt.close();
			}

			stmt = writecon.prepareStatement(deletefromdocumentstable
					.toString());
			stmt.setInt(1, ctx.getContextId());
			stmt.setString(2, identifier);
			retval[1] = stmt.executeUpdate();
			stmt.close();
		} catch (SQLException e) {
			throw new OXException("Error while removing documents from table.",
					e);
		} finally {
			releaseWriteConnection(ctx, writecon);
		}
		return retval;
	}

	public int modifyDocument(String oldidentifier, String newidentifier,
			String description, String mimetype, Context ctx)
			throws OXException {
		return modifyDocumentInTable(oldidentifier, newidentifier, description,
				mimetype, ctx, "infostore");
	}

	public int modifyDelDocument(String oldidentifier, String newidentifier,
			String description, String mimetype, Context ctx)
			throws OXException {
		return modifyDocumentInTable(oldidentifier, newidentifier, description,
				mimetype, ctx, "del_infostore");
	}

	private int modifyDocumentInTable(String oldidentifier,
			String newidentifier, String description, String mimetype,
			Context ctx, String basetablename) throws OXException {
		String documentstable = basetablename.concat("_document");
		int retval = -1;
		Connection writecon = getWriteConnection(ctx);

		final StringBuilder select_description = new StringBuilder(
				"SELECT description FROM " + documentstable
						+ " WHERE file_store_location=? AND cid=?");

		final StringBuilder updatedatabase = new StringBuilder(
				"UPDATE "
						+ documentstable
						+ " SET file_store_location=?,  description=?, file_mimetype=? "
						+ "WHERE file_store_location=? AND cid=?");
		try {
			String olddescription = null;
			PreparedStatement stmt = writecon
					.prepareStatement(select_description.toString());
			stmt.setString(1, oldidentifier);
			stmt.setInt(2, ctx.getContextId());
			ResultSet result = stmt.executeQuery();
			if (result.next()) {
				olddescription = result.getString(1);
			}
			stmt.close();
			if (olddescription == null) {
				olddescription = description;
			} else {
				olddescription = olddescription.concat(description);
			}

			stmt = writecon.prepareStatement(updatedatabase.toString());
			stmt.setString(1, newidentifier);
			stmt.setString(2, olddescription);
			stmt.setString(3, mimetype);
			stmt.setString(4, oldidentifier);
			stmt.setInt(5, ctx.getContextId());
			retval = stmt.executeUpdate();
			stmt.close();
		} catch (SQLException e) {
			throw new OXException(
					"Error while getting permissions for folder.", e);
		} finally {
			releaseWriteConnection(ctx, writecon);
		}
		return retval;
	}

	public int[] saveDocumentMetadata(String identifier,
			DocumentMetadata document, User user, Context ctx)
			throws OXException {
		int[] retval = new int[3];
		PreparedStatement stmt = null;
		Connection writeCon = null;
		try {
			int folder_id = OXFolderTools.getDefaultFolder(user.getId(),
					FolderObject.INFOSTORE, ctx);
			writeCon = getWriteConnection(ctx);

			int infostore_id = IDGenerator
					.getId(ctx, Types.INFOSTORE, writeCon);

			Date date = new Date(System.currentTimeMillis());

			stmt = writeCon
					.prepareStatement("INSERT INTO infostore (cid, id, folder_id, version, locked_until, color_label, creating_date, last_modified, created_by, changed_by) VALUES (?,?,?,?,?,?,?,?,?,?)");
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, infostore_id);
			stmt.setInt(3, folder_id);
			// Because we definitely have a file, we set version number to 1
			stmt.setInt(4, 1);
			stmt.setNull(5, java.sql.Types.INTEGER);
			stmt.setLong(6, document.getColorLabel());
			stmt.setLong(7, date.getTime());
			stmt.setLong(8, date.getTime());
			stmt.setInt(9, user.getId());
			stmt.setInt(10, user.getId());
			retval[0] = stmt.executeUpdate();
			stmt.close();

			stmt = writeCon
					.prepareStatement("INSERT INTO infostore_document (cid, infostore_id, version_number, creating_date, last_modified, created_by, changed_by, title, url, description, categories, filename) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, infostore_id);
			stmt.setInt(3, DOCUMENT_VERSION_NUMBER_WITHOUT_FILE);
			stmt.setLong(4, date.getTime());
			stmt.setLong(5, date.getTime());
			stmt.setInt(6, user.getId());
			stmt.setInt(7, user.getId());
			stmt.setString(8, document.getTitle());
			stmt.setString(9, document.getURL());
			stmt.setString(10, document.getDescription());
			stmt.setString(11, document.getCategories());
			stmt.setString(12, document.getFileName());
			retval[1] = stmt.executeUpdate();
			stmt.close();

			stmt = writeCon
					.prepareStatement("INSERT INTO infostore_document (cid, infostore_id, version_number, creating_date, last_modified, created_by, changed_by, title, url, description, categories, filename, file_store_location, file_size, file_mimetype, file_md5sum) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, infostore_id);
			stmt.setInt(3, 1);
			stmt.setLong(4, date.getTime());
			stmt.setLong(5, date.getTime());
			stmt.setInt(6, user.getId());
			stmt.setInt(7, user.getId());
			stmt.setString(8, document.getTitle());
			stmt.setString(9, document.getURL());
			stmt.setString(10, document.getDescription());
			stmt.setString(11, document.getCategories());
			stmt.setString(12, document.getFileName());
			stmt.setString(13, identifier);
			stmt.setString(14, String.valueOf(document.getFileSize()));
			stmt.setString(15, document.getFileMIMEType());
			stmt.setString(16, document.getFileMD5Sum());
			retval[2] = stmt.executeUpdate();
			document.setVersion(1);
			document.setId(infostore_id);
		} catch (SQLException e) {
			LOG.error("", e);
			throw new OXException(e);
		} catch (OXException e) {
			LOG.error("", e);
			throw new OXException(e);
		} finally {
			close(stmt, null);
			releaseWriteConnection(ctx, writeCon);
		}
		return retval;
	}

	@OXThrowsMultiple(category = { Category.CODE_ERROR,
			Category.CODE_ERROR }, desc = {
			"A faulty SQL Query was sent to the SQL server. This can only be fixed in R&D",
			"A faulty SQL Query was sent to the SQL server. This can only be fixed in R&D" }, exceptionId = {
			16, 17 }, msg = { "Invalid SQL Query: %s", "Invalid SQL Query: %s" })
	@Deprecated
	public List<Integer> removeVersion(int id, int[] versionId, Context ctx,
			User user, UserConfiguration userConfig) throws OXException {
		List<Integer> notDeletedVersions = new ArrayList<Integer>();

		Connection readCon = null;
		PreparedStatement stmt = null;
		ResultSet result = null;

		Date updated = new Date();
		int currentVersion = 0;
		try {
			List<String> filesToDelete = new ArrayList<String>();
			StringBuilder getAllAttachedFileStoreLocationsSQL = new StringBuilder(
					"SELECT file_store_location FROM infostore_document WHERE cid=? AND infostore_id=? AND (");
			for (int current = 0; current < versionId.length; current++) {
				if (current > 0) {
					getAllAttachedFileStoreLocationsSQL.append(" OR");
				}
				getAllAttachedFileStoreLocationsSQL.append(" version_number=?");
			}
			getAllAttachedFileStoreLocationsSQL
					.append(") AND file_store_location is not null");

			readCon = getReadConnection(ctx);
			stmt = readCon.prepareStatement(getAllAttachedFileStoreLocationsSQL
					.toString());
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, id);
			for (int current = 0; current < versionId.length; current++) {
				stmt.setInt(current + 3, versionId[current]);
			}
			result = stmt.executeQuery();
			while (result.next()) {
				filesToDelete.add(result.getString(1));
			}
			delFiles(filesToDelete, ctx);

			result.close();
			stmt.close();

			stmt = readCon
					.prepareStatement("SELECT version FROM infostore WHERE cid=? AND id=?");
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, id);
			result = stmt.executeQuery();
			if (result.next()) {
				currentVersion = result.getInt(1);
			}
		} catch (SQLException e) {
			throw EXCEPTIONS.create(16, e, getStatement(stmt));
		} finally {
			close(stmt, result);
			releaseReadConnection(ctx, readCon);
		}

		Connection writeCon = null;

		StringBuilder insertDelCopyInfostoreDocument = new StringBuilder(
				"INSERT INTO del_infostore_document ("
						+ "del_infostore_document.cid,"
						+ "del_infostore_document.infostore_id,"
						+ "del_infostore_document.version_number,"
						+ "del_infostore_document.creating_date,"
						+ "del_infostore_document.last_modified,"
						+ "del_infostore_document.created_by,"
						+ "del_infostore_document.changed_by,"
						+ "del_infostore_document.title,"
						+ "del_infostore_document.url,"
						+ "del_infostore_document.description,"
						+ "del_infostore_document.categories,"
						+ "del_infostore_document.filename,"
						+ "del_infostore_document.file_store_location,"
						+ "del_infostore_document.file_size,"
						+ "del_infostore_document.file_mimetype,"
						+ "del_infostore_document.file_md5sum,"
						+ "del_infostore_document.file_version_comment"
						+ ") SELECT "
						+ "infostore_document.cid,"
						+ "infostore_document.infostore_id,"
						+ "infostore_document.version_number,"
						+ "infostore_document.creating_date,"
						+ "infostore_document.last_modified,"
						+ "infostore_document.created_by,"
						+ "infostore_document.changed_by,"
						+ "infostore_document.title,"
						+ "infostore_document.url,"
						+ "infostore_document.description,"
						+ "infostore_document.categories,"
						+ "infostore_document.filename,"
						+ "infostore_document.file_store_location,"
						+ "infostore_document.file_size,"
						+ "infostore_document.file_mimetype,"
						+ "infostore_document.file_md5sum,"
						+ "infostore_document.file_version_comment"
						+ " FROM infostore_document WHERE cid=? AND infostore_id=? AND version_number=?");
		StringBuilder deleteVersionFromDocumentDB = new StringBuilder(
				"DELETE infostore_document.* FROM infostore_document JOIN infostore ON infostore.cid=? AND infostore_document.cid=? AND infostore.id=? AND infostore_document.infostore_id=? AND infostore_document.version_number=? AND infostore_document.version_number!=0 AND infostore.last_modified <= ?");
		StringBuilder rollback = new StringBuilder(
				"DELETE FROM del_infostore_document WHERE cid=? AND infostore_id=? AND version_number=?");

		try {
			boolean deleteCurrentVersion = false;
			writeCon = getWriteConnection(ctx);
			for (int current : versionId) {
				stmt = writeCon.prepareStatement(insertDelCopyInfostoreDocument
						.toString());
				stmt.setInt(1, ctx.getContextId());
				stmt.setInt(2, id);
				stmt.setInt(3, current);
				stmt.execute();
				stmt.close();
				stmt = writeCon.prepareStatement(deleteVersionFromDocumentDB
						.toString());
				stmt.setInt(1, ctx.getContextId());
				stmt.setInt(2, ctx.getContextId());
				stmt.setInt(3, id);
				stmt.setInt(4, id);
				stmt.setInt(5, current);
				stmt.setLong(6, updated.getTime());
				int deletedRows = stmt.executeUpdate();
				stmt.close();
				if (deletedRows <= 0) {
					stmt = writeCon.prepareStatement(rollback.toString());
					stmt.setInt(1, ctx.getContextId());
					stmt.setInt(2, id);
					stmt.setInt(3, current);
					stmt.execute();
					stmt.close();
					notDeletedVersions.add(current);
				} else if (current == currentVersion) {
					deleteCurrentVersion = true;
				}
			}

			if (notDeletedVersions.size() != versionId.length) {
				StringBuilder updateMainDocument = new StringBuilder();
				updateMainDocument
						.append("UPDATE infostore SET last_modified=?, changed_by=?");
				if (deleteCurrentVersion) {
					updateMainDocument
							.append(", version=(SELECT MAX(version_number) FROM infostore_document WHERE cid=? AND infostore_id=?)");
				}
				updateMainDocument.append(" WHERE cid=? AND id=?");

				stmt = writeCon.prepareStatement(updateMainDocument.toString());
				stmt.setLong(1, updated.getTime());
				stmt.setInt(2, user.getId());
				stmt.setInt(3, ctx.getContextId());
				stmt.setInt(4, id);
				if (deleteCurrentVersion) {
					stmt.setInt(5, ctx.getContextId());
					stmt.setInt(6, id);
				}
				stmt.execute();
				stmt.close();

				if (deleteCurrentVersion
						&& !notDeletedVersions.contains(currentVersion)) {
					// Copy title, url and description of former current version
					// into version 0
					stmt = writeCon
							.prepareStatement("SELECT title, url, description FROM del_infostore_document WHERE infostore_id = ? and version_number = ? and cid = ?");
					stmt.setInt(1, id);
					stmt.setInt(2, currentVersion);
					stmt.setInt(3, ctx.getContextId());
					result = stmt.executeQuery();
					if (!result.next())
						throw new SQLException(
								"Didn't find former current version");
					String title = result.getString(1);
					String url = result.getString(2);
					String description = result.getString(3);
					stmt.close();
					result.close();
					stmt = writeCon
							.prepareStatement("UPDATE infostore_document SET title = ?, url = ?, description = ? WHERE infostore_id = ? and cid = ? and version_number = 0");
					stmt.setString(1, title);
					stmt.setString(2, url);
					stmt.setString(3, description);
					stmt.setInt(4, id);
					stmt.setInt(5, ctx.getContextId());
					stmt.executeUpdate();
				}
			}
		} catch (SQLException e) {
			LOG.error("", e);
			throw EXCEPTIONS.create(17, e, stmt.toString());
		} finally {
			close(stmt, result);
			releaseWriteConnection(ctx, writeCon);
		}

		return notDeletedVersions;
	}

	@OXThrowsMultiple(category = { Category.CODE_ERROR,
			Category.TRY_AGAIN },

	desc = {
			"Indicates a faulty SQL Query. Only R&D can fix this",
			"Thrown when a result cannot be prefetched. This indicates a problem with the DB Connection. Have a look at the underlying SQLException" },

	exceptionId = { 18, 19 },

	msg = { "Incorrect SQL Query: %s", "Cannot pre-fetch results." })
	public TimedResult getDocuments(long folderId, Metadata[] columns,
			Metadata sort, int order, boolean onlyOwnObjects, Context ctx,
			User user, UserConfiguration userConfig) throws OXException {
		String onlyOwn = "";
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet result = null;
		try {
			if (onlyOwnObjects) {
				onlyOwn = " AND created_by=?";
			}

			int[] dbColumns = switchMetadata2DBColumns(columns, false);

			StringBuilder sql = new StringBuilder(
					getSQLSelectForInfostoreColumns(dbColumns, false));
			sql
					.append(" FROM infostore JOIN infostore_document ON infostore.cid=? AND infostore_document.cid=? AND infostore.id=infostore_document.infostore_id AND infostore_document.version_number=infostore.version AND infostore.folder_id=?");
			sql.append(onlyOwn);

			if (sort != null) {
				sql.append(" ORDER BY ");
				sql.append(INFOSTORE_DATACOLUMNS[switchMetadata2DBColumns(
						new Metadata[] { sort }, false)[0]]);
				if (order == InfostoreFacade.ASC) {
					sql.append(" ASC");
				} else if (order == InfostoreFacade.DESC) {
					sql.append(" DESC");
				}
			}
			con = getReadConnection(ctx);
			stmt = con.prepareStatement(sql.toString());
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, ctx.getContextId());
			stmt.setLong(3, folderId);
			if (onlyOwn.length() > 0) {
				stmt.setInt(4, user.getId());
			}
			result = stmt.executeQuery();

			return new TimedResultImpl(buildIterator(result, stmt, dbColumns,
					this, ctx, con, true), System.currentTimeMillis());
		} catch (SQLException e) {
			close(stmt, result);
			releaseReadConnection(ctx, con);
			throw EXCEPTIONS.create(18, e, getStatement(stmt));
		} catch (SearchIteratorException e) {
			close(stmt, result);
			releaseReadConnection(ctx, con);
			throw EXCEPTIONS.create(19, e);
		}
	}

	public SortedSet<String> getDocumentFileStoreLocationsperContext(Context ctx)
			throws OXException {
		SortedSet<String> _strReturnArray = new TreeSet<String>();
		Connection con = getReadConnection(ctx);
		StringBuilder SQL = new StringBuilder(
				"SELECT file_store_location from infostore_document where infostore_document.cid=? AND file_store_location is not null");
		try {
			final PreparedStatement stmt = con.prepareStatement(SQL.toString());
			stmt.setInt(1, ctx.getContextId());
			final ResultSet result = stmt.executeQuery();
			while (result.next()) {
				_strReturnArray.add(result.getString(1));
			}
			result.close();
			stmt.close();
		} catch (SQLException e) {
			LOG.error("", e);
			throw new OXException(e);
		}

		return _strReturnArray;
	}

	public SortedSet<String> getDelDocumentFileStoreLocationsperContext(
			Context ctx) throws OXException {
		SortedSet<String> _strReturnArray = new TreeSet<String>();
		Connection con = getReadConnection(ctx);
		StringBuilder SQL = new StringBuilder(
				"SELECT file_store_location from del_infostore_document where del_infostore_document.cid=? AND file_store_location is not null");
		try {
			final PreparedStatement stmt = con.prepareStatement(SQL.toString());
			stmt.setInt(1, ctx.getContextId());
			final ResultSet result = stmt.executeQuery();
			while (result.next()) {
				_strReturnArray.add(result.getString(1));
			}
			result.close();
			stmt.close();
		} catch (SQLException e) {
			LOG.error("", e);
			throw new OXException(e);
		}

		return _strReturnArray;
	}

	@OXThrowsMultiple(category = { Category.CODE_ERROR,
			Category.TRY_AGAIN },

	desc = {
			"Indicates a faulty SQL Query. Only R&D can fix this",
			"Thrown when a result cannot be prefetched. This indicates a problem with the DB Connection. Have a look at the underlying SQLException" },

	exceptionId = { 20, 21 },

	msg = { "Incorrect SQL Query: %s", "Cannot pre-fetch results." })
	public TimedResult getVersions(int id, Metadata[] columns, Metadata sort,
			int order, Context ctx, User user, UserConfiguration userConfig)
			throws OXException {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet result = null;
		try {
			int[] dbColumns = switchMetadata2DBColumns(columns, true);

			StringBuilder sql = new StringBuilder(
					getSQLSelectForInfostoreColumns(dbColumns, false)
							.toString());
			sql
					.append(" FROM infostore JOIN infostore_document ON infostore.cid=? AND infostore.id=? AND infostore_document.infostore_id=? AND infostore_document.cid=?");

			if (sort != null) {
				sql.append(" ORDER BY ");
				sql.append(INFOSTORE_DATACOLUMNS[switchMetadata2DBColumns(
						new Metadata[] { sort }, true)[0]]);
				if (order == InfostoreFacade.ASC) {
					sql.append(" ASC");
				} else if (order == InfostoreFacade.DESC) {
					sql.append(" DESC");
				}
			}
			con = getReadConnection(ctx);
			stmt = con.prepareStatement(sql.toString());
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, id);
			stmt.setInt(3, id);
			stmt.setInt(4, ctx.getContextId());
			result = stmt.executeQuery();

			return new TimedResultImpl(buildIterator(result, stmt, dbColumns,
					this, ctx, con, true), System.currentTimeMillis());
		} catch (SQLException e) {
			close(stmt, result);
			releaseReadConnection(ctx, con);
			throw EXCEPTIONS.create(20, e, getStatement(stmt));
		} catch (SearchIteratorException e) {
			close(stmt, result);
			releaseReadConnection(ctx, con);
			throw EXCEPTIONS.create(21, e);
		}
	}

	@OXThrowsMultiple(category = { Category.CODE_ERROR,
			Category.TRY_AGAIN },

	desc = {
			"Indicates a faulty SQL Query. Only R&D can fix this",
			"Thrown when a result cannot be prefetched. This indicates a problem with the DB Connection. Have a look at the underlying SQLException" },

	exceptionId = { 22, 23 },

	msg = { "Incorrect SQL Query: %s", "Cannot pre-fetch results." })
	public TimedResult getDocuments(int[] ids, Metadata[] columns, Context ctx,
			User user, UserConfiguration userConfig) throws OXException {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet result = null;
		try {
			con = getReadConnection(ctx);
			int[] dbColumns = switchMetadata2DBColumns(columns, false);
			StringBuilder SQL_JOIN = new StringBuilder(
					getSQLSelectForInfostoreColumns(dbColumns, false));
			SQL_JOIN
					.append(" FROM infostore JOIN infostore_document ON infostore.cid=? AND infostore_document.cid=? AND infostore.id=infostore_document.infostore_id AND infostore_document.version_number=infostore.version AND (");
			for (int i = 0; i < ids.length; i++) {
				SQL_JOIN.append("infostore.id=?");
				if (i < ids.length - 1) {
					SQL_JOIN.append(" OR ");
				}
			}
			SQL_JOIN.append(") ");

			stmt = con.prepareStatement(SQL_JOIN.toString());
			stmt.setInt(1, ctx.getContextId());
			stmt.setInt(2, ctx.getContextId());
			for (int i = 0; i < ids.length; i++) {
				stmt.setInt(i + 3, ids[i]);
			}
			result = stmt.executeQuery();

			return new TimedResultImpl(buildIterator(result, stmt, dbColumns,
					this, ctx, con, true), System.currentTimeMillis());
		} catch (SQLException e) {
			close(stmt, result);
			releaseReadConnection(ctx, con);
			throw EXCEPTIONS.create(22, e, getStatement(stmt));
		} catch (SearchIteratorException e) {
			close(stmt, result);
			releaseReadConnection(ctx, con);
			throw EXCEPTIONS.create(23, e);
		}
	}

	@OXThrowsMultiple(category = { Category.CODE_ERROR,
			Category.TRY_AGAIN },

	desc = {
			"Indicates a faulty SQL Query. Only R&D can fix this",
			"Thrown when a result cannot be prefetched. This indicates a problem with the DB Connection. Have a look at the underlying SQLException" },

	exceptionId = { 24, 25 },

	msg = { "Incorrect SQL Query.", "Cannot pre-fetch results." })
	public Delta getDelta(long folderId, long updateSince, Metadata[] columns,
			Metadata sort, int order, boolean onlyOwnObjects,
			boolean ignoreDeleted, Context ctx, User user,
			UserConfiguration userConfig) throws OXException {
		DeltaImpl retval = null;

		String onlyOwn = "";
		StringBuilder ORDER = new StringBuilder();
		if (sort != null) {
			ORDER.append(" ORDER BY ");
			ORDER.append(INFOSTORE_DATACOLUMNS[switchMetadata2DBColumns(
					new Metadata[] { sort }, false)[0]]);
			if (order == InfostoreFacade.ASC) {
				ORDER.append(" ASC");
			} else if (order == InfostoreFacade.DESC) {
				ORDER.append(" DESC");
			}
		}

		Connection con = null;
		PreparedStatement stmtNew = null;
		PreparedStatement stmtModified = null;
		PreparedStatement stmtDeleted = null;

		ResultSet resultNew = null;
		ResultSet resultModified = null;
		ResultSet resultDeleted = null;
		try {
			con = getReadConnection(ctx);
			if (onlyOwnObjects) {
				onlyOwn = " AND created_by=?";
			}

			int[] dbColumns = switchMetadata2DBColumns(columns, false);
			StringBuilder SELECT = new StringBuilder(
					getSQLSelectForInfostoreColumns(dbColumns, false));
			StringBuilder JOIN_NEW = new StringBuilder(
					" FROM infostore JOIN infostore_document ON infostore.cid=? AND infostore_document.cid=? AND infostore_document.infostore_id=infostore.id AND infostore_document.version_number = infostore.version AND infostore.folder_id=? AND infostore.creating_date>?").append(onlyOwn);
			StringBuilder JOIN_MODIFIED = new StringBuilder(
					" FROM infostore JOIN infostore_document ON infostore.cid=? AND infostore_document.cid=? AND infostore_document.infostore_id=infostore.id  AND infostore_document.version_number = infostore.version AND infostore.folder_id=? AND infostore.last_modified>? AND infostore.creating_date<infostore.last_modified"
							).append(onlyOwn);
			StringBuilder DELETE_SELECT = new StringBuilder(
					"SELECT id FROM del_infostore WHERE cid=? AND folder_id=? AND last_modified>?"
							).append(onlyOwn);

			stmtNew = con.prepareStatement(SELECT.toString()
					+ JOIN_NEW.toString() + ORDER.toString());
			stmtNew.setInt(1, ctx.getContextId());
			stmtNew.setInt(2, ctx.getContextId());
			stmtNew.setLong(3, folderId);
			stmtNew.setLong(4, updateSince);
			if (onlyOwn.length() > 0) {
				stmtNew.setInt(5, user.getId());
			}
			resultNew = stmtNew.executeQuery();

			stmtModified = con.prepareStatement(SELECT.toString()
					+ JOIN_MODIFIED.toString() + ORDER.toString());
			stmtModified.setInt(1, ctx.getContextId());
			stmtModified.setInt(2, ctx.getContextId());
			stmtModified.setLong(3, folderId);
			stmtModified.setLong(4, updateSince);
			if (onlyOwn.length() > 0) {
				stmtModified.setInt(5, user.getId());
			}
			resultModified = stmtModified.executeQuery();

			stmtDeleted = con.prepareStatement(DELETE_SELECT.toString()
					+ ORDER.toString());
			stmtDeleted.setInt(1, ctx.getContextId());
			stmtDeleted.setLong(2, folderId);
			stmtDeleted.setLong(3, updateSince);
			if (onlyOwn.length() > 0) {
				stmtDeleted.setInt(4, user.getId());
			}
			resultDeleted = stmtDeleted.executeQuery();

			SearchIterator isiNew = buildIterator(resultNew, stmtNew,
					dbColumns, this, ctx, con, false);
			SearchIterator isiModified = buildIterator(resultModified,
					stmtModified, dbColumns, this, ctx, con, false);
			SearchIterator isiDeleted = buildIterator(resultDeleted,
					stmtDeleted, new int[] { INFOSTORE_id }, this, ctx, con,
					false);

			retval = new DeltaImpl(isiNew, isiModified, isiDeleted, System
					.currentTimeMillis());
		} catch (SQLException e) {
			throw EXCEPTIONS.create(24, e);
		} catch (SearchIteratorException e) {
			throw EXCEPTIONS.create(25, e);
		} finally {
			if (FETCH.equals(Fetch.PREFETCH)) {
				close(stmtNew, resultNew);
				close(stmtModified, resultModified);
				close(stmtDeleted, resultDeleted);
				releaseReadConnection(ctx, con);
			}
		}

		return retval;
	}

	@OXThrows(category = Category.CODE_ERROR, desc = "A faulty SQL Query was sent to the SQL server. This can only be fixed in R&D", exceptionId = 26, msg = "Invalid SQL Query: %s")
	public int countDocuments(long folderId, boolean onlyOwnObjects,
			Context ctx, User user, UserConfiguration userConfig)
			throws OXException {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet result = null;

		try {
			con = getReadConnection(ctx);
			StringBuilder SQL = new StringBuilder(
					"SELECT count(id) from infostore where infostore.folder_id=?");
			if (onlyOwnObjects) {
				SQL.append(" AND infostore.created_by=?");
			}
			stmt = con.prepareStatement(SQL.toString());
			stmt.setLong(1, folderId);
			if (onlyOwnObjects) {
				stmt.setInt(2, user.getId());
			}
			result = stmt.executeQuery();
			if (result.next()) {
				return result.getInt(1);
			}
		} catch (SQLException e) {
			throw EXCEPTIONS.create(26, e, getStatement(stmt));
		} finally {
			close(stmt, result);
			releaseReadConnection(ctx, con);
		}
		return 0;
	}

	public int countDocumentsperContext(Context ctx) throws OXException {
		int retval = 0;

		Connection con = getReadConnection(ctx);
		try {
			StringBuilder SQL = new StringBuilder(
					"SELECT count(id) from infostore where infostore.cid=?");
			final PreparedStatement stmt = con.prepareStatement(SQL.toString());
			stmt.setInt(1, ctx.getContextId());
			final ResultSet result = stmt.executeQuery();
			if (result.next()) {
				retval = result.getInt(1);
			}
			result.close();
			stmt.close();
		} catch (Exception e) {
			LOG.error("", e);
			throw new OXException(
					"Error while getting permissions for folder.", e);
		} finally {
			releaseReadConnection(ctx, con);
		}

		return retval;
	}

	@OXThrows(category = Category.CODE_ERROR, desc = "A faulty SQL Query was sent to the SQL server. This can only be fixed in R&D", exceptionId = 27, msg = "Invalid SQL Query: %s")
	public boolean hasFolderForeignObjects(long folderId, Context ctx,
			User user, UserConfiguration userConfig) throws OXException {
		boolean retval = true;

		Connection con = getReadConnection(ctx);
		PreparedStatement stmt = null;
		ResultSet result = null;
		try {
			// TODO: Check if document is unlocked too!

			stmt = con
					.prepareStatement("SELECT id FROM infostore WHERE cid=? AND folder_id=? AND created_by!=?");
			stmt.setInt(1, ctx.getContextId());
			stmt.setLong(2, folderId);
			stmt.setInt(3, user.getId());
			result = stmt.executeQuery();
			if (result.next()) {
				retval = true;
			} else {
				retval = false;
			}

		} catch (SQLException e) {
			LOG.error("", e);
			throw EXCEPTIONS.create(27, e, getStatement(stmt));
		} finally {
			close(stmt, result);
			releaseReadConnection(ctx, con);
		}

		return retval;
	}

	@OXThrows(category = Category.CODE_ERROR, desc = "A faulty SQL Query was sent to the SQL server. This can only be fixed in R&D", exceptionId = 28, msg = "Invalid SQL Query: %s")
	public boolean isFolderEmpty(long folderId, Context ctx) throws OXException {
		boolean retval = false;

		Connection con = getReadConnection(ctx);
		PreparedStatement stmt = null;
		ResultSet result = null;
		try {
			stmt = con
					.prepareStatement("SELECT count(id) FROM infostore WHERE cid=? AND folder_id=?");
			stmt.setInt(1, ctx.getContextId());
			stmt.setLong(2, folderId);
			result = stmt.executeQuery();
			if (result.next() && result.getInt(1) <= 0) {
				retval = true;
			}
		} catch (SQLException e) {
			LOG.error("", e);
			throw EXCEPTIONS.create(28, e, getStatement(stmt));
		} finally {
			close(stmt, result);
			releaseReadConnection(ctx, con);
		}

		return retval;
	}

	private static final List<String> userFields = Arrays.asList("changed_by",
			"created_by");

	private static final List<String> tables = Arrays.asList("infostore",
			"infostore_document");

	@OXThrows(category = Category.CODE_ERROR, desc = "A faulty SQL Query was sent to the SQL server. This can only be fixed in R&D", exceptionId = 29, msg = "Invalid SQL Query: %s")
	
	public void removeUser(int id, Context ctx, SessionObject session, EntityLockManager locks) throws OXException {
		removePrivate(id,ctx,session);
		assignToAdmin(id,ctx,session);
		locks.transferLocks(ctx, id, ctx.getMailadmin());
	}

	private void removePrivate(int id, Context ctx, SessionObject session) throws OXException {
		try {
			List<FolderObject> foldersWithPrivateItems = new DelUserFolderDiscoverer(getProvider()).discoverFolders(id, ctx);
			if(foldersWithPrivateItems.size() == 0)
				return;
			StringBuffer where = new StringBuffer("infostore.folder_id in (");
			for(FolderObject folder : foldersWithPrivateItems){
				where.append(folder.getObjectID()).append(',');
			}
			where.setCharAt(where.length()-1, ')');
			where.append(" and infostore.cid = ").append(ctx.getContextId());
			
			SearchIterator iter = com.openexchange.groupware.infostore.database.impl.InfostoreIterator.allDocumentsWhere(where.toString(), Metadata.VALUES_ARRAY, getProvider(), ctx);
			where = new StringBuffer("infostore_document.cid = ");
			where.append(ctx.getContextId()).append(" and infostore_document.infostore_id in (");
			
			List<DocumentMetadata> documents = new ArrayList<DocumentMetadata>();
			while(iter.hasNext()) {
				DocumentMetadata metadata = (DocumentMetadata)iter.next();
				where.append(metadata.getId()).append(',');
				documents.add(metadata);
			}
			if(documents.size() == 0)
				return;
			
			where.setCharAt(where.length()-1, ')');
			
			List<DocumentMetadata> versions = new ArrayList<DocumentMetadata>();
			
			iter = com.openexchange.groupware.infostore.database.impl.InfostoreIterator.allVersionsWhere(where.toString(), Metadata.VALUES_ARRAY, getProvider(), ctx);
			
			while(iter.hasNext()) {
				DocumentMetadata metadata = (DocumentMetadata)iter.next();
				versions.add(metadata);
			}
			
			InfostoreQueryCatalog catalog = new InfostoreQueryCatalog();
			
			DeleteDocumentAction deleteDocumentAction = new DeleteDocumentAction();
			deleteDocumentAction.setProvider(getProvider());
			deleteDocumentAction.setContext(ctx);
			deleteDocumentAction.setDocuments(documents);
			deleteDocumentAction.setQueryCatalog(catalog);
			
			DeleteVersionAction deleteVersionAction = new DeleteVersionAction();
			deleteVersionAction.setProvider(getProvider());
			deleteVersionAction.setContext(ctx);
			deleteVersionAction.setDocuments(versions);
			deleteVersionAction.setQueryCatalog(catalog);
			
			deleteVersionAction.perform();
			try {
				deleteDocumentAction.perform();
			} catch (AbstractOXException e) {
				try {
					deleteVersionAction.undo();
					throw new InfostoreException(e);
				} catch (AbstractOXException e1) {
					LOG.fatal("Can't roll back deleting versions. Run the consistency tool.",e1);
				}
			}
						
			FileStorage fs = getFileStorage(ctx);

//			Remove the files. No rolling back from this point onward
			
			for(DocumentMetadata version : versions) {
				if(null != version.getFilestoreLocation())
					fs.deleteFile(version.getFilestoreLocation());
			}
			
			EventClient ec = new EventClient(session);

			for (DocumentMetadata m : documents) {
				try {
					ec.delete(m);
				} catch (Exception e) {
					LOG.error("", e);
				}
			}
			
		} catch (AbstractOXException x) {
			throw new InfostoreException(x);
		}
		
	}

	private void assignToAdmin(int id, Context ctx, SessionObject session) throws OXException {
		Connection writeCon = null;
		Statement stmt = null;
		StringBuilder query = null;
		try {
			writeCon = getWriteConnection(ctx);
			stmt = writeCon.createStatement();
			for (String table : tables) {
				for (String userField : userFields) {
					query = new StringBuilder("UPDATE ").append(table).append(
							" SET ").append(userField).append(" = ").append(
							ctx.getMailadmin()).append(" WHERE cid = ").append(
							ctx.getContextId()).append(" AND ").append(
							userField).append(" = ").append(id);
					stmt.executeUpdate(query.toString());
				}
			}
		} catch (SQLException e) {
			throw EXCEPTIONS.create(29, e, (query != null) ? query.toString()
					: "");
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					LOG.debug("", e);
				}
			releaseWriteConnection(ctx, writeCon);
		}
	}

	private StringBuffer getSQLSelectForInfostoreColumns(int[] columns,
			boolean deleteTable) {
		String delete = "";
		if (deleteTable) {
			delete = "del_";
		}
		StringBuffer select = new StringBuffer("SELECT ");
		for (int i = 0; i < columns.length; i++) {
			select.append(delete);
			select.append(INFOSTORE_DATACOLUMNS[columns[i]]);
			if (i < columns.length - 1) {
				select.append(", ");
			}
		}
		return (select);
	}

	private int[] switchMetadata2DBColumns(Metadata[] columns,
			boolean versionPriorityHigh) {
		int[] retval = new int[columns.length];
		for (int i = 0; i < columns.length; i++) {
			Metadata2DBSwitch: switch (columns[i].getId()) {
			default:
				break Metadata2DBSwitch;
			case Metadata.LAST_MODIFIED:
				if (versionPriorityHigh) {
					retval[i] = INFOSTORE_DOCUMENT_last_modified;
				} else {
					retval[i] = INFOSTORE_last_modified;
				}
				break Metadata2DBSwitch;
			case Metadata.CREATION_DATE:
				if (versionPriorityHigh) {
					retval[i] = INFOSTORE_DOCUMENT_creating_date;
				} else {
					retval[i] = INFOSTORE_creating_date;
				}
				break Metadata2DBSwitch;
			case Metadata.MODIFIED_BY:
				if (versionPriorityHigh) {
					retval[i] = INFOSTORE_DOCUMENT_changed_by;
				} else {
					retval[i] = INFOSTORE_changed_by;
				}
				break Metadata2DBSwitch;
			case Metadata.CREATED_BY:
				if (versionPriorityHigh) {
					retval[i] = INFOSTORE_DOCUMENT_created_by;
				} else {
					retval[i] = INFOSTORE_created_by;
				}
				break Metadata2DBSwitch;
			case Metadata.FOLDER_ID:
				retval[i] = INFOSTORE_folder_id;
				break Metadata2DBSwitch;
			case Metadata.VERSION:
				if (versionPriorityHigh) {
					retval[i] = INFOSTORE_DOCUMENT_version_number;
				} else {
					retval[i] = INFOSTORE_version;
				}
				break Metadata2DBSwitch;
			case Metadata.TITLE:
				retval[i] = INFOSTORE_DOCUMENT_title;
				break Metadata2DBSwitch;
			case Metadata.FILENAME:
				retval[i] = INFOSTORE_DOCUMENT_filename;
				break Metadata2DBSwitch;
			case Metadata.SEQUENCE_NUMBER:
				retval[i] = INFOSTORE_DOCUMENT_last_modified;
				break Metadata2DBSwitch;
			case Metadata.ID:
				retval[i] = INFOSTORE_id;
				break Metadata2DBSwitch;
			case Metadata.COLOR_LABEL:
				retval[i] = INFOSTORE_color_label;
				break Metadata2DBSwitch;
			case Metadata.FILE_SIZE:
				retval[i] = INFOSTORE_DOCUMENT_file_size;
				break Metadata2DBSwitch;
			case Metadata.FILE_MIMETYPE:
				retval[i] = INFOSTORE_DOCUMENT_file_mimetype;
				break Metadata2DBSwitch;
			case Metadata.DESCRIPTION:
				retval[i] = INFOSTORE_DOCUMENT_description;
				break Metadata2DBSwitch;
			case Metadata.LOCKED_UNTIL:
				retval[i] = INFOSTORE_locked_until;
				break Metadata2DBSwitch;
			case Metadata.URL:
				retval[i] = INFOSTORE_DOCUMENT_url;
				break Metadata2DBSwitch;
			case Metadata.CATEGORIES:
				retval[i] = INFOSTORE_DOCUMENT_categories;
				break Metadata2DBSwitch;
			case Metadata.FILE_MD5SUM:
				retval[i] = INFOSTORE_DOCUMENT_file_md5sum;
				break Metadata2DBSwitch;
			case Metadata.VERSION_COMMENT:
				retval[i] = INFOSTORE_DOCUMENT_file_version_comment;
				break Metadata2DBSwitch;
			case Metadata.CURRENT_VERSION:
				retval[i] = INFOSTORE_version;
				break Metadata2DBSwitch;
			case Metadata.FILESTORE_LOCATION:
				retval[i] = INFOSTORE_DOCUMENT_file_store_location;
				break Metadata2DBSwitch;

			}
		}
		return retval;
	}

	private final DocumentMetadataImpl fillDocumentMetadata(
			DocumentMetadataImpl dmi, int[] columns, ResultSet result)
			throws SQLException {
		int currentVersion = -1;
		int versionNumber = -1;
		for (int i = 0; i < columns.length; i++) {
			setObjectColumns: switch (columns[i]) {
			default:
				break setObjectColumns;
			case INFOSTORE_cid:
				break setObjectColumns;
			case INFOSTORE_id:
				dmi.setId(result.getInt(i + 1));
				break setObjectColumns;
			case INFOSTORE_folder_id:
				dmi.setFolderId(result.getInt(i + 1));
				break setObjectColumns;
			case INFOSTORE_version:
				currentVersion = result.getInt(i + 1);
				break setObjectColumns;
			case INFOSTORE_locked_until:
				dmi.setLockedUntil(new Date(result.getLong(i + 1)));
				if (result.wasNull())
					dmi.setLockedUntil(null);
				break setObjectColumns;
			case INFOSTORE_creating_date:
				dmi.setCreationDate(new Date(result.getLong(i + 1)));
				break setObjectColumns;
			case INFOSTORE_last_modified:
				dmi.setLastModified(new Date(result.getLong(i + 1)));
				break setObjectColumns;
			case INFOSTORE_created_by:
				dmi.setCreatedBy(result.getInt(i + 1));
				break setObjectColumns;
			case INFOSTORE_changed_by:
				dmi.setModifiedBy(result.getInt(i + 1));
				break setObjectColumns;
			case INFOSTORE_color_label:
				dmi.setColorLabel(result.getInt(i + 1));
				break setObjectColumns;
			case INFOSTORE_DOCUMENT_cid:
				break setObjectColumns;
			case INFOSTORE_DOCUMENT_infostore_id:
				break setObjectColumns;
			case INFOSTORE_DOCUMENT_version_number:
				versionNumber = result.getInt(i + 1);
				break setObjectColumns;
			case INFOSTORE_DOCUMENT_creating_date:
				dmi.setCreationDate(new Date(result.getLong(i + 1)));
				break setObjectColumns;
			case INFOSTORE_DOCUMENT_last_modified:
				dmi.setLastModified(new Date(result.getLong(i + 1)));
				break setObjectColumns;
			case INFOSTORE_DOCUMENT_created_by:
				dmi.setCreatedBy(result.getInt(i + 1));
				break setObjectColumns;
			case INFOSTORE_DOCUMENT_changed_by:
				dmi.setModifiedBy(result.getInt(i + 1));
				break setObjectColumns;
			case INFOSTORE_DOCUMENT_title:
				dmi.setTitle(result.getString(i + 1));
				break setObjectColumns;
			case INFOSTORE_DOCUMENT_url:
				dmi.setURL(result.getString(i + 1));
				break setObjectColumns;
			case INFOSTORE_DOCUMENT_description:
				dmi.setDescription(result.getString(i + 1));
				break setObjectColumns;
			case INFOSTORE_DOCUMENT_categories:
				dmi.setCategories(result.getString(i + 1));
				break setObjectColumns;
			case INFOSTORE_DOCUMENT_filename:
				dmi.setFileName(result.getString(i + 1));
				break setObjectColumns;
			case INFOSTORE_DOCUMENT_file_store_location:
				dmi.setFilestoreLocation(result.getString(i + 1));
				break setObjectColumns;
			case INFOSTORE_DOCUMENT_file_size:
				dmi.setFileSize(result.getInt(i + 1));
				break setObjectColumns;
			case INFOSTORE_DOCUMENT_file_mimetype:
				dmi.setFileMIMEType(result.getString(i + 1));
				break setObjectColumns;
			case INFOSTORE_DOCUMENT_file_md5sum:
				dmi.setFileMD5Sum(result.getString(i + 1));
				break setObjectColumns;
			case INFOSTORE_DOCUMENT_file_version_comment:
				dmi.setVersionComment(result.getString(i + 1));
				break setObjectColumns;
			}
		}
		if ((currentVersion != -1) && (versionNumber != -1)) {
			if (currentVersion == versionNumber) {
				dmi.setIsCurrentVersion(true);
			}
			dmi.setVersion(versionNumber);
		} else if (versionNumber != -1) {
			dmi.setVersion(versionNumber);
		} else if (currentVersion != -1) {
			dmi.setVersion(currentVersion);
		}
		return dmi;
	}

	private void addFile(String fileInFilespoolPath, Context ctx) {
		if (inTransaction()) {
			ctxHolder.set(ctx);
			fileIdAddList.get().add(fileInFilespoolPath);
		}
	}

	@OXThrowsMultiple(category = { Category.INTERNAL_ERROR,
			Category.SUBSYSTEM_OR_SERVICE_DOWN }, desc = {
			"A Context Exception occurred while trying to open the filestorage. Look at the Context Exception for further details",
			"An error occurred while removing the file from the file storage." }, exceptionId = {
			30, 31 }, msg = { "Cannot find file store location.",
			"Could not remove file. %s" })
	private void delFiles(List<String> filesToDelete, Context ctx)
			throws OXException {
		if (inTransaction()) {
			ctxHolder.set(ctx);
			fileIdRemoveList.get().addAll(filesToDelete);
		} else {
			for (String id : filesToDelete) {
				try {
					getFileStorage(ctx).deleteFile(id);
				} catch (FilestoreException e) {
					throw EXCEPTIONS.create(30, e);
				} catch (FileStorageException e) {
					throw new InfostoreException(e);
				}
			}
		}
	}

	protected FileStorage getFileStorage(Context ctx) throws FileStorageException,
			FilestoreException {
		return FileStorage.getInstance(FilestoreStorage.createURI(ctx), ctx, this.getProvider());
	}

	@Override
	public void startTransaction() throws TransactionException {
		fileIdRemoveList.set(new ArrayList<String>());
		fileIdAddList.set(new ArrayList<String>());
		ctxHolder.set(null);
		super.startTransaction();
	}

	@OXThrowsMultiple(category = { Category.INTERNAL_ERROR,
			Category.SUBSYSTEM_OR_SERVICE_DOWN }, desc = { "An error occurred while removing the file from the file storage." }, exceptionId = { 32 }, msg = { "Could not remove file. %s" })
	@Override
	public void commit() throws TransactionException {
		Context ctx = ctxHolder.get();
		for (String id : fileIdRemoveList.get()) {
			try {
				getFileStorage(ctx).deleteFile(id);
			} catch (FilestoreException e) {
				throw new TransactionException(e); // ErrorCode?
			} catch (FileStorageException e) {
				throw new TransactionException(e);
			}
		}
		super.commit();
	}

	@Override
	public void finish() throws TransactionException {
		fileIdRemoveList.set(null);
		fileIdAddList.set(null);
		ctxHolder.set(null);
		super.finish();
	}

	@OXThrowsMultiple(category = { Category.INTERNAL_ERROR,
			Category.SUBSYSTEM_OR_SERVICE_DOWN }, desc = { "An error occurred while removing the file from the file storage." }, exceptionId = { 33 }, msg = { "Could not remove file. %s" })
	@Override
	public void rollback() throws TransactionException {
		Context ctx = ctxHolder.get();
		for (String id : fileIdAddList.get()) {
			try {
				getFileStorage(ctx).deleteFile(id);
			} catch (FilestoreException e) {
				throw new TransactionException(e); // ErrorCode?
			} catch (FileStorageException e) {
				throw new TransactionException(e);
			}
		}
		super.rollback();
	}

	private int getId(Context context, Connection writeCon) throws SQLException {
		boolean autoCommit = writeCon.getAutoCommit();
		if (autoCommit)
			writeCon.setAutoCommit(false);
		try {
			return IDGenerator.getId(context, Types.INFOSTORE, writeCon);
		} finally {
			if (autoCommit) {
				writeCon.commit();
				writeCon.setAutoCommit(true);
			}
		}
	}

	private int getNextVersionNumberForInfostoreObject(int cid,
			int infostore_id, Connection con) throws SQLException {
		int retval = 0;

		PreparedStatement stmt = con
				.prepareStatement("SELECT MAX(version_number) FROM infostore_document WHERE cid=? AND infostore_id=?");
		stmt.setInt(1, cid);
		stmt.setInt(2, infostore_id);
		ResultSet result = stmt.executeQuery();
		if (result.next()) {
			retval = result.getInt(1);
		}
		result.close();
		stmt.close();

		stmt = con
				.prepareStatement("SELECT MAX(version_number) FROM del_infostore_document WHERE cid=? AND infostore_id=?");
		stmt.setInt(1, cid);
		stmt.setInt(2, infostore_id);
		result = stmt.executeQuery();
		if (result.next()) {
			int delVersion = result.getInt(1);
			if (delVersion > retval)
				retval = delVersion;
		}
		result.close();
		stmt.close();

		return retval + 1;
	}

	private void copyRows(Connection writeCon, String intoTable, String sql,
			Object... substitutes) throws SQLException {
		PreparedStatement readStatement = null;
		PreparedStatement stmt = null;

		ResultSet rs = null;

		try {
			StringBuilder queryBuilder = new StringBuilder("INSERT INTO ");
			queryBuilder.append(intoTable);
			queryBuilder.append("(");

			StringBuilder questionMarks = new StringBuilder();

			readStatement = writeCon.prepareStatement(sql);
			int i = 1;
			for (Object sub : substitutes)
				readStatement.setObject(i++, sub);

			rs = readStatement.executeQuery();

			ResultSetMetaData rsMeta = rs.getMetaData();
			int cCount = rsMeta.getColumnCount();
			for (int index = 1; index != cCount; index++) {
				queryBuilder.append(rsMeta.getColumnName(index));
				queryBuilder.append(",");
				questionMarks.append("?,");
			}

			queryBuilder.setLength(queryBuilder.length() - 1);
			questionMarks.setLength(questionMarks.length() - 1);

			queryBuilder.append(") VALUES (");
			queryBuilder.append(questionMarks.toString());
			queryBuilder.append(")");

			stmt = writeCon.prepareStatement(queryBuilder.toString());

			while (rs.next()) {
				for (int index = 1; index != cCount; index++) {
					stmt.setObject(index, rs.getObject(index));
				}
				// System.out.println("----------> "+stmt.toString());
				stmt.executeUpdate();
			}
		} finally {
			close(readStatement, rs);
			close(stmt, null);
		}

	}

	private SearchIterator buildIterator(ResultSet result,
			PreparedStatement stmt, int[] dbColumns, DatabaseImpl service,
			Context ctx, Connection con, boolean closeIfPossible)
			throws SearchIteratorException, SQLException {

		return fetchMode.buildIterator(result, stmt, dbColumns, service, ctx,
				con, closeIfPossible);

	}

	protected static interface FetchMode {
		public SearchIterator buildIterator(ResultSet result,
				PreparedStatement stmt, int[] dbColumns,
				DatabaseImpl DatabaseImpl2, Context ctx, Connection con,
				boolean closeIfPossible) throws SearchIteratorException,
				SQLException;
	}

	private class PrefetchMode implements FetchMode {

		public SearchIterator buildIterator(ResultSet result,
				PreparedStatement stmt, int[] dbColumns, DatabaseImpl impl,
				Context ctx, Connection con, boolean closeIfPossible)
				throws SearchIteratorException, SQLException {
			List<DocumentMetadata> resultList = new ArrayList<DocumentMetadata>();
			while (result.next()) {
				DocumentMetadataImpl dmi = new DocumentMetadataImpl();
				fillDocumentMetadata(dmi, dbColumns, result);
				resultList.add(dmi);
			}
			if (closeIfPossible) {
				close(stmt, result);
				releaseReadConnection(ctx, con);
			}
			return new SearchIteratorAdapter(resultList.iterator());
		}

	}

	private static class CloseLaterMode implements FetchMode {

		public SearchIterator buildIterator(ResultSet result,
				PreparedStatement stmt, int[] dbColumns, DatabaseImpl impl,
				Context ctx, Connection con, boolean closeIfPossible)
				throws SearchIteratorException, SQLException {
			return new InfostoreIterator(result, stmt, dbColumns, impl, ctx,
					con);
		}

	}

	private class CloseImmediatelyMode implements FetchMode {

		public SearchIterator buildIterator(ResultSet result,
				PreparedStatement stmt, int[] dbColumns, DatabaseImpl impl,
				Context ctx, Connection con, boolean closeIfPossible)
				throws SearchIteratorException, SQLException {
			if (closeIfPossible) {
				close(stmt, result);
				releaseReadConnection(ctx, con);
			}

			return new InfostoreIterator(result, dbColumns, impl);

		}

	}

	public static class InfostoreIterator implements SearchIterator {

		private Object next;

		private Statement stmt;

		private ResultSet rs;

		private final int[] columns;

		private DatabaseImpl d;

		private Context ctx;

		private Connection readCon;

		public InfostoreIterator(ResultSet rs, Statement stmt, int[] columns,
				DatabaseImpl d, Context ctx, Connection readCon)
				throws SearchIteratorException {
			this.rs = rs;
			this.stmt = stmt;
			this.columns = columns;
			this.d = d;
			this.ctx = ctx;
			this.readCon = readCon;

			try {
				if (rs.next()) {
					next = d.fillDocumentMetadata(new DocumentMetadataImpl(),
							columns, rs);
				} else {
					close();
				}
			} catch (SQLException e) {
				throw new SearchIteratorException(SearchIteratorCode.SQL_ERROR,
						e, Component.INFOSTORE);
			}
		}

		public InfostoreIterator(ResultSet rs, int[] columns, DatabaseImpl d) {
			this.rs = rs;
			this.columns = columns;
			this.d = d;
		}

		public boolean hasNext() {
			return next != null;
		}

		public int size() {
			throw new UnsupportedOperationException(
					"Mehtod size() not implemented");
		}

		public boolean hasSize() {
			return false;
		}

		public Object next() throws SearchIteratorException {
			try {
				Object retval = null;
				retval = next;
				if (rs.next()) {
					next = d.fillDocumentMetadata(new DocumentMetadataImpl(),
							columns, rs);
					NextObject: while (next == null) {
						if (rs.next()) {
							next = d.fillDocumentMetadata(
									new DocumentMetadataImpl(), columns, rs);
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
			} catch (SQLException exc) {
				throw new SearchIteratorException(SearchIteratorCode.SQL_ERROR,
						exc, Component.INFOSTORE);
			}
		}

		public void close() throws SearchIteratorException {
			next = null;
			try {
				if (rs != null) {
					rs.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				rs = null;
				stmt = null;
			} catch (SQLException e) {
				throw new SearchIteratorException(SearchIteratorCode.SQL_ERROR,
						e, Component.INFOSTORE);
			} finally {
				if (readCon != null)
					d.releaseReadConnection(ctx, readCon);
			}
		}
	}

	@OXThrows(
			category = Category.CODE_ERROR,
			desc = "An invalid SQL Query was sent to the server.",
			exceptionId = 34,
			msg = "Invalid SQL Query : %s"
	)
	public int getMaxActiveVersion(int id, Context context) throws OXException {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			con = getReadConnection(context);
			stmt = con
					.prepareStatement("SELECT max(version_number) FROM infostore_document WHERE cid = ? and infostore_id = ?");
			stmt.setInt(1, context.getContextId());
			stmt.setInt(2, id);
			rs = stmt.executeQuery();
			if (rs.next())
				return rs.getInt(1);
			else
				return -1;
		} catch (SQLException e) {
			throw EXCEPTIONS.create(34, e, getStatement(stmt));
		} finally {
			close(stmt, rs);
			if (con != null)
				releaseReadConnection(context, con);
		}
	}
}
