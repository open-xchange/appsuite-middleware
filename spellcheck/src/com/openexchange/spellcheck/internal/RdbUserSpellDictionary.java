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

package com.openexchange.spellcheck.internal;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.json.JSONArray;
import org.json.JSONException;

import com.openexchange.database.Database;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServerTimer;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.spellcheck.SpellCheckException;
import com.swabunga.spell.engine.SpellDictionary;

/**
 * {@link RdbUserSpellDictionary}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class RdbUserSpellDictionary implements SpellDictionary {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(RdbUserSpellDictionary.class);

	private static final class Key implements Serializable {

		private static final long serialVersionUID = -5495964300868695992L;

		/**
		 * Unique identifier of the context.
		 */
		private final int contextId;

		/**
		 * Object key of the cached object.
		 */
		private final int userId;

		/**
		 * Hash code of the context specific object.
		 */
		private final int hash;

		/**
		 * Initialization timestamp
		 */
		private final long timestamp;

		/**
		 * Initializes a new {@link Key}
		 * 
		 * @param userId
		 *            The user ID
		 * @param contextId
		 *            The context ID
		 */
		public Key(final int userId, final int contextId) {
			super();
			this.contextId = contextId;
			this.userId = userId;
			hash = userId ^ contextId;
			timestamp = System.currentTimeMillis();
		}

		public int getContextId() {
			return contextId;
		}

		public int getUserId() {
			return userId;
		}

		public long getTimestamp() {
			return timestamp;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			} else if (!(obj instanceof Key)) {
				return false;
			}
			final Key other = (Key) obj;
			return (contextId == other.contextId) && (userId == other.userId);
		}

		@Override
		public int hashCode() {
			return hash;
		}

		@Override
		public String toString() {
			return new StringBuilder(32).append("Key context=").append(contextId).append(" | user=").append(userId)
					.toString();
		}
	}

	private static final Object DUMMY_ENTRY = new Object();

	private static final Lock LOCK_DICT = new ReentrantLock();

	private static final Lock LOCK_LOCK = new ReentrantLock();

	private static final Map<Key, Map<String, Object>> userDicts = new ConcurrentHashMap<Key, Map<String, Object>>();

	private static final Map<Key, ReadWriteLock> locks = new ConcurrentHashMap<Key, ReadWriteLock>();

	private static TimerTask timerTask;

	private final Key key;

	private final Map<String, Object> words;

	private final AtomicBoolean modified;

	private final AtomicBoolean exists;

	/**
	 * Initializes a new {@link RdbUserSpellDictionary}
	 * 
	 * @param userId
	 *            The user ID
	 * @param ctx
	 *            The user's context
	 * @throws SpellCheckException
	 *             If initialization fails
	 */
	public RdbUserSpellDictionary(final int userId, final Context ctx) throws SpellCheckException {
		super();
		modified = new AtomicBoolean();
		key = new Key(userId, ctx.getContextId());
		words = getUserDict(key);
		exists = new AtomicBoolean(existsInDB());
		if (exists.get()) {
			readFromDB();
		}
	}

	/**
	 * Add a word (possibly) permanently to the dictionary.
	 * 
	 * @param word
	 *            The word to add to the dictionary
	 * @param save
	 *            <code>true</code> to save permanently to database storage;
	 *            otherwise <code>false</code>
	 */
	public void addWord(final String word, final boolean save) {
		if (_addWord(word) && save) {
			try {
				save();
			} catch (final SpellCheckException e) {
				LOG.error(e.getLocalizedMessage(), e);
			}
		}
	}

	private boolean _addWord(final String word) {
		final Lock readLock = getLock(key).readLock();
		readLock.lock();
		try {
			if (words.containsKey(word)) {
				return false;
			}
			modified.set(true);
			words.put(word, DUMMY_ENTRY);
			return true;
		} finally {
			readLock.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.swabunga.spell.engine.SpellDictionary#addWord(java.lang.String)
	 */
	public void addWord(final String word) {
		addWord(word, true);
	}

	private static final List<String> EMPTY_SUGGESTIONS = new ArrayList<String>(0);

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.swabunga.spell.engine.SpellDictionary#getSuggestions(java.lang.String,
	 *      int)
	 */
	public List<?> getSuggestions(final String sourceWord, final int scoreThreshold) {
		return EMPTY_SUGGESTIONS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.swabunga.spell.engine.SpellDictionary#getSuggestions(java.lang.String,
	 *      int, int[][])
	 */
	public List<?> getSuggestions(final String sourceWord, final int scoreThreshold, final int[][] matrix) {
		return EMPTY_SUGGESTIONS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.swabunga.spell.engine.SpellDictionary#isCorrect(java.lang.String)
	 */
	public boolean isCorrect(final String word) {
		return words.containsKey(word);
	}

	/**
	 * Removes a word from this dictionary
	 * 
	 * @param word
	 *            The word to remove
	 */
	private boolean _removeWord(final String word) {
		final Lock readLock = getLock(key).readLock();
		readLock.lock();
		try {
			if (!words.containsKey(word)) {
				return false;
			}
			words.remove(word);
			modified.set(true);
			return true;
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * Removes a word from this dictionary
	 * 
	 * @param word
	 *            The word to remove
	 * @param save
	 *            <code>true</code> to save removal permanently to database
	 *            storage; otherwise <code>false</code>
	 */
	public void removeWord(final String word, final boolean save) {
		if (_removeWord(word) && save) {
			try {
				save();
			} catch (final SpellCheckException e) {
				LOG.error(e.getLocalizedMessage(), e);
			}
		}
	}

	/**
	 * Checks if this user dictionary has been modified
	 * 
	 * @return <code>true</code> if modified; otherwise <code>false</code>
	 */
	public boolean isModified() {
		return modified.get();
	}

	/**
	 * Writes this dictionary's content to database storage
	 * 
	 * @throws SpellCheckException
	 *             If writing to database storage fails
	 */
	public void save() throws SpellCheckException {
		if (modified.compareAndSet(true, false)) {
			write2DB();
		}
	}

	/**
	 * Returns all words contained in this user dictionary
	 * 
	 * @return All words contained in this user dictionary
	 */
	public List<String> getWords() {
		return new ArrayList<String>(words.keySet());
	}

	private JSONArray getUserWords() {
		final Set<String> keySet = getUserDict(key).keySet();
		final JSONArray jsonArray = new JSONArray();
		for (String word : keySet) {
			jsonArray.put(word);
		}
		return jsonArray;
	}

	private static final String SQL_INSERT = "INSERT INTO spellcheck_user_dict VALUES (?, ?, ?)";

	private static final String SQL_UPDATE = "UPDATE spellcheck_user_dict SET words = ? WHERE cid = ? AND user = ?";

	private void write2DB() throws SpellCheckException {
		final Lock writeLock = getLock(key).writeLock();
		writeLock.lock();
		try {
			Connection writeCon = null;
			PreparedStatement stmt = null;
			try {
				writeCon = Database.get(key.contextId, true);
				if (exists.get()) {
					stmt = writeCon.prepareStatement(SQL_UPDATE);
					stmt.setString(1, getUserWords().toString());
					stmt.setInt(2, key.contextId);
					stmt.setInt(3, key.userId);
				} else {
					stmt = writeCon.prepareStatement(SQL_INSERT);
					stmt.setInt(1, key.contextId);
					stmt.setInt(2, key.userId);
					stmt.setString(3, getUserWords().toString());
				}
				stmt.executeUpdate();
			} catch (final DBPoolingException e) {
				throw new SpellCheckException(e);
			} catch (final SQLException e) {
				throw new SpellCheckException(SpellCheckException.Code.SQL_ERROR, e, e.getLocalizedMessage());
			} finally {
				closeDBStuff(null, stmt, writeCon, false, key.contextId);
			}
		} finally {
			writeLock.unlock();
		}
	}

	private static final String SQL_SELECT = "SELECT words FROM spellcheck_user_dict WHERE cid = ? AND user = ?";

	private boolean existsInDB() throws SpellCheckException {
		final Lock readLock = getLock(key).readLock();
		readLock.lock();
		try {
			Connection readCon = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				readCon = Database.get(key.contextId, false);
				stmt = readCon.prepareStatement(SQL_SELECT);
				stmt.setInt(1, key.contextId);
				stmt.setInt(2, key.userId);
				rs = stmt.executeQuery();
				return rs.next();
			} catch (final DBPoolingException e) {
				throw new SpellCheckException(e);
			} catch (final SQLException e) {
				throw new SpellCheckException(SpellCheckException.Code.SQL_ERROR, e, e.getLocalizedMessage());
			} finally {
				closeDBStuff(rs, stmt, readCon, true, key.contextId);
			}
		} finally {
			readLock.unlock();
		}
	}

	private void readFromDB() throws SpellCheckException {
		final Lock readLock = getLock(key).readLock();
		readLock.lock();
		try {
			Connection readCon = null;
			PreparedStatement stmt = null;
			ResultSet rs = null;
			try {
				readCon = Database.get(key.contextId, false);
				stmt = readCon.prepareStatement(SQL_SELECT);
				stmt.setInt(1, key.contextId);
				stmt.setInt(2, key.userId);
				rs = stmt.executeQuery();
				if (rs.next()) {
					exists.set(true);
					final JSONArray jsonArray = new JSONArray(rs.getString(1));
					final int len = jsonArray.length();
					for (int i = 0; i < len; i++) {
						words.put(jsonArray.getString(i), DUMMY_ENTRY);
					}
				}
			} catch (final DBPoolingException e) {
				throw new SpellCheckException(e);
			} catch (final SQLException e) {
				throw new SpellCheckException(SpellCheckException.Code.SQL_ERROR, e, e.getLocalizedMessage());
			} catch (final JSONException e) {
				throw new SpellCheckException(SpellCheckException.Code.INVALID_FORMAT, e, e.getLocalizedMessage());
			} finally {
				closeDBStuff(rs, stmt, readCon, true, key.contextId);
			}
		} finally {
			readLock.unlock();
		}
	}

	private static void closeDBStuff(final ResultSet rs, final Statement stmt, final Connection con,
			final boolean isReadCon, final int cid) {
		/*
		 * Close ResultSet
		 */
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				if (LOG.isErrorEnabled()) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
		/*
		 * Close Statement
		 */
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				if (LOG.isErrorEnabled()) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
		/*
		 * Close connection
		 */
		if (con != null) {
			Database.back(cid, !isReadCon, con);
		}
	}

	private static Map<String, Object> getUserDict(final Key key) {
		Map<String, Object> userDict = userDicts.get(key);
		if (userDict == null) {
			/*
			 * Create
			 */
			LOCK_DICT.lock();
			try {
				if ((userDict = userDicts.get(key)) == null) {
					userDict = new ConcurrentHashMap<String, Object>();
					userDicts.put(key, userDict);
				}
			} finally {
				LOCK_DICT.unlock();
			}
		} else {
			/*
			 * Update timestamp through re-put same value
			 */
			userDicts.put(key, userDict);
		}
		return userDict;
	}

	private static ReadWriteLock getLock(final Key key) {
		ReadWriteLock lock = locks.get(key);
		if (lock == null) {
			LOCK_LOCK.lock();
			try {
				if ((lock = locks.get(key)) == null) {
					lock = new ReentrantReadWriteLock();
					locks.put(key, lock);
				}
			} finally {
				LOCK_LOCK.unlock();
			}
		}
		return lock;
	}

	static void start() {
		ServerTimer.getTimer().schedule((timerTask = new TimerTask() {

			@Override
			public void run() {
				for (final Iterator<Key> iter = userDicts.keySet().iterator(); iter.hasNext();) {
					final Key key = iter.next();
					if ((System.currentTimeMillis() - key.timestamp) > 60000) {
						iter.remove();
					}
				}
			}
		}), 1000, 30000);
	}

	static void stop() {
		if (null != timerTask) {
			timerTask.cancel();
			ServerTimer.getTimer().purge();
		}
	}
}
