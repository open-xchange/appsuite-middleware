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

package com.openexchange.mail.usersetting;

import static com.openexchange.tools.sql.DBUtils.closeResources;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;

import com.openexchange.cache.CacheKey;
import com.openexchange.cache.impl.Configuration;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.UserConfigurationException;
import com.openexchange.groupware.userconfiguration.UserConfigurationException.UserConfigurationCode;
import com.openexchange.mail.usersetting.UserSettingMail.Signature;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.DBPoolingException;

/**
 * CachingUserSettingMailStorage - this storage tries to use a cache for
 * instances of <code>{@link UserSettingMail}</code> and falls back to
 * database-based storage if any cache-related errors occur
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class CachingUserSettingMailStorage extends UserSettingMailStorage {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(CachingUserSettingMailStorage.class);

	private static final String CACHE_REGION_NAME = "UserSettingMail";

	private boolean useCache;

	private JCS cache;

	private Lock cacheWriteLock;

	/**
	 * Default constructor
	 */
	protected CachingUserSettingMailStorage() {
		super();
		try {
			cache = JCS.getInstance(CACHE_REGION_NAME);
			cacheWriteLock = new ReentrantLock();
			useCache = true;
		} catch (final CacheException e) {
			final UserConfigurationException ue = new UserConfigurationException(
					UserConfigurationCode.CACHE_INITIALIZATION_FAILED, e, CACHE_REGION_NAME);
			LOG.error(ue.getLocalizedMessage(), ue);
		}
	}

	private static final String SQL_LOAD = "SELECT bits, send_addr, reply_to_addr, msg_format, display_msg_headers, auto_linebreak, std_trash, std_sent, std_drafts, std_spam, "
			+ "upload_quota, upload_quota_per_file, confirmed_spam, confirmed_ham FROM user_setting_mail WHERE cid = ? AND user = ?";

	private static final String SQL_INSERT = "INSERT INTO user_setting_mail (cid, user, bits, send_addr, reply_to_addr, msg_format, display_msg_headers, auto_linebreak, std_trash, std_sent, std_drafts, std_spam, upload_quota, upload_quota_per_file, confirmed_spam, confirmed_ham) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	private static final String SQL_UPDATE = "UPDATE user_setting_mail SET bits = ?, send_addr = ?, reply_to_addr = ?, msg_format = ?, display_msg_headers = ?, auto_linebreak = ?, std_trash = ?, std_sent = ?, std_drafts = ?, std_spam = ?, upload_quota = ?, upload_quota_per_file = ?, confirmed_spam = ?, confirmed_ham = ? WHERE cid = ? AND user = ?";

	/**
	 * Saves given user's mail settings to database
	 * 
	 * @param usm
	 *            the user's mail settings to save
	 * @param user
	 *            the user ID
	 * @param ctx
	 *            the context
	 * @param writeConArg -
	 *            the writable connection; may be <code>null</code>
	 * @throws UserConfigurationException
	 *             if user's mail settings could not be saved
	 */
	@Override
	public void saveUserSettingMail(final UserSettingMail usm, final int user, final Context ctx,
			final Connection writeConArg) throws UserConfigurationException {
		try {
			Connection writeCon = writeConArg;
			boolean closeCon = false;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			boolean insert = false;
			Connection tmpCon = null;
			try {
				tmpCon = DBPool.pickup(ctx);
				stmt = tmpCon.prepareStatement(SQL_LOAD);
				stmt.setInt(1, ctx.getContextId());
				stmt.setInt(2, user);
				rs = stmt.executeQuery();
				insert = (!rs.next());
			} finally {
				closeResources(rs, stmt, tmpCon, true, ctx);
				rs = null;
				stmt = null;
				tmpCon = null;
			}
			try {
				if (writeCon == null) {
					writeCon = DBPool.pickupWriteable(ctx);
					closeCon = true;
				}
				if (insert) {
					stmt = getInsertStmt(usm, user, ctx, writeCon);
				} else {
					stmt = getUpdateStmt(usm, user, ctx, writeCon);
				}
				stmt.executeUpdate();
				saveSignatures(usm, user, ctx, writeCon);
			} finally {
				closeResources(rs, stmt, closeCon ? writeCon : null, false, ctx);
			}
			usm.setModifiedDuringSession(false);
			if (useCache) {
				/*
				 * Put clone into cache
				 */
				cacheWriteLock.lock();
				try {
					cache.put(new CacheKey(ctx, user), usm.clone());
				} catch (final CacheException e) {
					LOG.error("UserSettingMail could not be put into cache", e);
				} finally {
					cacheWriteLock.unlock();
				}
			}
		} catch (final SQLException e) {
			LOG.error(e.getMessage(), e);
			throw new UserConfigurationException(UserConfigurationCode.SQL_ERROR, e, new Object[0]);
		} catch (final DBPoolingException e) {
			LOG.error(e.getMessage(), e);
			throw new UserConfigurationException(UserConfigurationCode.DBPOOL_ERROR, e, new Object[0]);
		}
	}

	private static final String SQL_DELETE = "DELETE FROM user_setting_mail WHERE cid = ? AND user = ?";

	private static final String SQL_DELETE_SIGNATURES = "DELETE FROM user_setting_mail_signature WHERE cid = ? AND user = ?";

	/**
	 * Deletes the user's mail settings from database
	 * 
	 * @param user
	 *            the user ID
	 * @param ctx
	 *            the context
	 * @param writeConArg
	 *            the writable connection; may be <code>null</code>
	 * @throws UserConfigurationException -
	 *             if deletion fails
	 */
	@Override
	public void deleteUserSettingMail(final int user, final Context ctx, final Connection writeConArg)
			throws UserConfigurationException {
		try {
			Connection writeCon = writeConArg;
			boolean closeWriteCon = false;
			PreparedStatement stmt = null;
			try {
				if (writeCon == null) {
					writeCon = DBPool.pickupWriteable(ctx);
					closeWriteCon = true;
				}
				/*
				 * Delete signatures
				 */
				stmt = writeCon.prepareStatement(SQL_DELETE_SIGNATURES);
				stmt.setInt(1, ctx.getContextId());
				stmt.setInt(2, user);
				stmt.executeUpdate();
				stmt.close();
				/*
				 * Delete user setting
				 */
				stmt = writeCon.prepareStatement(SQL_DELETE);
				stmt.setInt(1, ctx.getContextId());
				stmt.setInt(2, user);
				stmt.executeUpdate();
				stmt.close();
				stmt = null;
				if (useCache) {
					/*
					 * Remove from cache
					 */
					cacheWriteLock.lock();
					try {
						cache.remove(new CacheKey(ctx, user));
					} catch (final CacheException e) {
						LOG.error("UserSettingMail could not be removed from cache", e);
					} finally {
						cacheWriteLock.unlock();
					}
				}
			} finally {
				closeResources(null, stmt, closeWriteCon ? writeCon : null, false, ctx);
				stmt = null;
			}
		} catch (final SQLException e) {
			LOG.error(e.getMessage(), e);
			throw new UserConfigurationException(UserConfigurationCode.SQL_ERROR, e, new Object[0]);
		} catch (final DBPoolingException e) {
			LOG.error(e.getMessage(), e);
			throw new UserConfigurationException(UserConfigurationCode.DBPOOL_ERROR, e, new Object[0]);
		}
	}

	/**
	 * Loads user's mail settings from database
	 * 
	 * @param usm
	 *            the user's mail settings to fill
	 * @param user
	 *            the user
	 * @param ctx
	 *            the context
	 * @param readConArg
	 *            the readable connection
	 * @throws UserConfigurationException
	 *             if loading fails
	 */
	@Override
	public UserSettingMail loadUserSettingMail(final int user, final Context ctx, final Connection readConArg)
			throws UserConfigurationException {
		try {
			UserSettingMail usm = useCache ? (UserSettingMail) cache.get(new CacheKey(ctx, user)) : null;
			if (null != usm) {
				return (UserSettingMail) usm.clone();
			}
			/*
			 * Fetch from database
			 */
			cacheWriteLock.lock();
			try {
				/*
				 * Still not in cache?
				 */
				if (null == (usm = useCache ? (UserSettingMail) cache.get(new CacheKey(ctx, user)) : null)) {
					usm = new UserSettingMail();
					Connection readCon = readConArg;
					boolean closeCon = false;
					PreparedStatement stmt = null;
					ResultSet rs = null;
					try {
						if (readCon == null) {
							readCon = DBPool.pickup(ctx);
							closeCon = true;
						}
						stmt = readCon.prepareStatement(SQL_LOAD);
						stmt.setInt(1, ctx.getContextId());
						stmt.setInt(2, user);
						rs = stmt.executeQuery();
						if (!rs.next()) {
							throw new UserConfigurationException(
									UserConfigurationException.UserConfigurationCode.MAIL_SETTING_NOT_FOUND, Integer
											.valueOf(user), Integer.valueOf(ctx.getContextId()));
						}
						usm.parseBits(rs.getInt(1));
						usm.setSendAddr(rs.getString(2));
						usm.setReplyToAddr(rs.getString(3));
						usm.setMsgFormat(rs.getInt(4));
						setDisplayMsgHeadersString(usm, rs.getString(5));
						usm.setAutoLinebreak(rs.getInt(6) >= 0 ? rs.getInt(6) : 0);
						usm.setStdTrashName(rs.getString(7));
						usm.setStdSentName(rs.getString(8));
						usm.setStdDraftsName(rs.getString(9));
						usm.setStdSpamName(rs.getString(10));
						usm.setUploadQuota(rs.getLong(11));
						usm.setUploadQuotaPerFile(rs.getLong(12));
						usm.setConfirmedSpam(rs.getString(13));
						usm.setConfirmedHam(rs.getString(14));
						loadSignatures(usm, user, ctx, readCon);
						usm.setModifiedDuringSession(false);
						if (useCache) {
							/*
							 * Put clone into cache
							 */
							try {
								cache.put(new CacheKey(ctx, user), usm.clone());
							} catch (final CacheException e) {
								LOG.error("UserSettingMail could not be put into cache", e);
							}
						}
					} finally {
						closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
					}
				}
				return usm;
			} finally {
				cacheWriteLock.unlock();
			}
		} catch (final SQLException e) {
			LOG.error(e.getMessage(), e);
			throw new UserConfigurationException(UserConfigurationCode.SQL_ERROR, e, new Object[0]);
		} catch (final DBPoolingException e) {
			LOG.error(e.getMessage(), e);
			throw new UserConfigurationException(UserConfigurationCode.DBPOOL_ERROR, e, new Object[0]);
		}
	}

	private static final String SQL_LOAD_SIGNATURES = "SELECT id, signature FROM user_setting_mail_signature WHERE cid = ? AND user = ?";

	private static void loadSignatures(final UserSettingMail usm, final int user, final Context ctx,
			final Connection readConArg) throws UserConfigurationException {
		try {
			Connection readCon = readConArg;
			boolean closeCon = false;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				if (readCon == null) {
					readCon = DBPool.pickup(ctx);
					closeCon = true;
				}
				stmt = readCon.prepareStatement(SQL_LOAD_SIGNATURES);
				stmt.setInt(1, ctx.getContextId());
				stmt.setInt(2, user);
				rs = stmt.executeQuery();
				if (rs.next()) {
					final Map<String, String> sigMap = new HashMap<String, String>();
					do {
						sigMap.put(rs.getString(1), rs.getString(2));
					} while (rs.next());
					final int size = sigMap.size();
					final Signature[] signatures = new Signature[size];
					final Iterator<Map.Entry<String, String>> iter = sigMap.entrySet().iterator();
					for (int i = 0; i < size; i++) {
						final Map.Entry<String, String> e = iter.next();
						signatures[i] = new Signature(e.getKey(), e.getValue());
					}
					usm.setSignatures(signatures);
				} else {
					usm.setSignatures(null);
				}
			} finally {
				closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
			}
		} catch (final SQLException e) {
			LOG.error(e.getMessage(), e);
			throw new UserConfigurationException(UserConfigurationCode.SQL_ERROR, e, new Object[0]);
		} catch (final DBPoolingException e) {
			LOG.error(e.getMessage(), e);
			throw new UserConfigurationException(UserConfigurationCode.DBPOOL_ERROR, e, new Object[0]);
		}
	}

	private static PreparedStatement getUpdateStmt(final UserSettingMail usm, final int user, final Context ctx,
			final Connection writeCon) throws SQLException {
		PreparedStatement stmt;
		stmt = writeCon.prepareStatement(SQL_UPDATE);
		stmt.setInt(1, usm.getBitsValue());
		stmt.setString(2, usm.getSendAddr() == null ? "" : usm.getSendAddr());
		stmt.setString(3, usm.getReplyToAddr() == null ? "" : usm.getReplyToAddr());
		stmt.setInt(4, usm.getMsgFormat());
		String s = getDisplayMsgHeadersString(usm);
		if (s == null) {
			stmt.setNull(5, Types.VARCHAR);
		} else {
			stmt.setString(5, s);
		}
		s = null;
		stmt.setInt(6, usm.getAutoLinebreak());
		stmt.setString(7, usm.getStdTrashName() == null ? UserSettingMail.STD_TRASH : usm.getStdTrashName());
		stmt.setString(8, usm.getStdSentName() == null ? UserSettingMail.STD_SENT : usm.getStdSentName());
		stmt.setString(9, usm.getStdDraftsName() == null ? UserSettingMail.STD_DRAFTS : usm.getStdDraftsName());
		stmt.setString(10, usm.getStdSpamName() == null ? UserSettingMail.STD_SPAM : usm.getStdSpamName());
		stmt.setLong(11, usm.getUploadQuota());
		stmt.setLong(12, usm.getUploadQuotaPerFile());
		stmt
				.setString(13, usm.getConfirmedSpam() == null ? UserSettingMail.STD_CONFIRMED_SPAM : usm
						.getConfirmedSpam());
		stmt.setString(14, usm.getConfirmedHam() == null ? UserSettingMail.STD_CONFIRMED_HAM : usm.getConfirmedHam());
		stmt.setInt(15, ctx.getContextId());
		stmt.setInt(16, user);
		return stmt;
	}

	private static PreparedStatement getInsertStmt(final UserSettingMail usm, final int user, final Context ctx,
			final Connection writeCon) throws SQLException {
		PreparedStatement stmt;
		stmt = writeCon.prepareStatement(SQL_INSERT);
		stmt.setInt(1, ctx.getContextId());
		stmt.setInt(2, user);
		stmt.setInt(3, usm.getBitsValue());
		stmt.setString(4, usm.getSendAddr() == null ? "" : usm.getSendAddr());
		stmt.setString(5, usm.getReplyToAddr() == null ? "" : usm.getReplyToAddr());
		stmt.setInt(6, usm.getMsgFormat());
		String s = getDisplayMsgHeadersString(usm);
		if (s == null) {
			stmt.setNull(7, Types.VARCHAR);
		} else {
			stmt.setString(7, s);
		}
		s = null;
		stmt.setInt(8, usm.getAutoLinebreak());
		stmt.setString(9, usm.getStdTrashName() == null ? UserSettingMail.STD_TRASH : usm.getStdTrashName());
		stmt.setString(10, usm.getStdSentName() == null ? UserSettingMail.STD_SENT : usm.getStdSentName());
		stmt.setString(11, usm.getStdDraftsName() == null ? UserSettingMail.STD_DRAFTS : usm.getStdDraftsName());
		stmt.setString(12, usm.getStdSpamName() == null ? UserSettingMail.STD_SPAM : usm.getStdSpamName());
		stmt.setLong(13, usm.getUploadQuota());
		stmt.setLong(14, usm.getUploadQuotaPerFile());
		stmt
				.setString(15, usm.getConfirmedSpam() == null ? UserSettingMail.STD_CONFIRMED_SPAM : usm
						.getConfirmedSpam());
		stmt.setString(16, usm.getConfirmedHam() == null ? UserSettingMail.STD_CONFIRMED_HAM : usm.getConfirmedHam());
		return stmt;
	}

	private static final String SQL_INSERT_SIGNATURE = "INSERT INTO user_setting_mail_signature (cid, user, id, signature) VALUES (?, ?, ?, ?)";

	private static boolean saveSignatures(final UserSettingMail usm, final int user, final Context ctx,
			final Connection writeConArg) throws UserConfigurationException {
		try {
			Connection writeCon = writeConArg;
			boolean closeCon = false;
			PreparedStatement stmt = null;
			try {
				if (writeCon == null) {
					writeCon = DBPool.pickupWriteable(ctx);
					closeCon = true;
				}
				/*
				 * Delete old
				 */
				stmt = writeCon.prepareStatement(SQL_DELETE_SIGNATURES);
				stmt.setInt(1, ctx.getContextId());
				stmt.setInt(2, user);
				stmt.executeUpdate();
				stmt.close();
				final Signature[] signatures = usm.getSignatures();
				if (signatures == null || signatures.length == 0) {
					return true;
				}
				/*
				 * Insert new
				 */
				stmt = writeCon.prepareStatement(SQL_INSERT_SIGNATURE);
				for (int i = 0; i < signatures.length; i++) {
					final Signature sig = signatures[i];
					stmt.setInt(1, ctx.getContextId());
					stmt.setInt(2, user);
					stmt.setString(3, sig.getId());
					stmt.setString(4, sig.getSignature());
					stmt.addBatch();
				}
				return (stmt.executeBatch().length > 0);
			} finally {
				closeResources(null, stmt, closeCon ? writeCon : null, false, ctx);
			}
		} catch (final SQLException e) {
			LOG.error(e.getMessage(), e);
			throw new UserConfigurationException(UserConfigurationCode.SQL_ERROR, e, new Object[0]);
		} catch (final DBPoolingException e) {
			LOG.error(e.getMessage(), e);
			throw new UserConfigurationException(UserConfigurationCode.DBPOOL_ERROR, e, new Object[0]);
		}
	}

	private static String getDisplayMsgHeadersString(final UserSettingMail usm) {
		final String[] displayMsgHeaders = usm.getDisplayMsgHeaders();
		if (displayMsgHeaders == null || displayMsgHeaders.length == 0) {
			return null;
		}
		final StringBuilder tmp = new StringBuilder(256);
		tmp.append(displayMsgHeaders[0]);
		for (int i = 1; i < displayMsgHeaders.length; i++) {
			tmp.append(',').append(displayMsgHeaders[i]);
		}
		return tmp.toString();
	}

	private static void setDisplayMsgHeadersString(final UserSettingMail usm, final String displayMsgHeadersStr) {
		if (displayMsgHeadersStr == null) {
			usm.setDisplayMsgHeaders(null);
			usm.setModifiedDuringSession(true);
			return;
		}
		usm.setDisplayMsgHeaders(displayMsgHeadersStr.split(" *, *"));
		usm.setModifiedDuringSession(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.usersetting.UserSettingMailStorage#clearStorage()
	 */
	@Override
	public void clearStorage() throws UserConfigurationException {
		if (useCache) {
			/*
			 * Put clone into cache
			 */
			cacheWriteLock.lock();
			try {
				cache.clear();
			} catch (final CacheException e) {
				LOG.error("UserSettingMail's cache could not be cleared", e);
			} finally {
				cacheWriteLock.unlock();
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.mail.usersetting.UserSettingMailStorage#removeUserSettingMail(int,
	 *      com.openexchange.groupware.contexts.Context)
	 */
	@Override
	public void removeUserSettingMail(final int user, final Context ctx) throws UserConfigurationException {
		if (useCache) {
			/*
			 * Put clone into cache
			 */
			cacheWriteLock.lock();
			try {
				cache.remove(new CacheKey(ctx, user));
			} catch (final CacheException e) {
				LOG.error("UserSettingMail could not be removed from cache", e);
			} finally {
				cacheWriteLock.unlock();
			}
		}
	}

	@Override
	public void shutdownStorage() {
		Configuration.getInstance().freeCache(CACHE_REGION_NAME);
	}

}
