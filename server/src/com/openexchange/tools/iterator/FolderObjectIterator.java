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

package com.openexchange.tools.iterator;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import com.openexchange.api2.OXException;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.cache.impl.FolderCacheNotEnabledException;
import com.openexchange.caching.CacheException;
import com.openexchange.caching.ElementAttributes;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.tools.iterator.SearchIteratorException.SearchIteratorCode;
import com.openexchange.tools.oxfolder.OXFolderProperties;

/**
 * {@link FolderObjectIterator} - A {@link SearchIterator} especially for instances of {@link FolderObject}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FolderObjectIterator implements SearchIterator<FolderObject> {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(FolderObjectIterator.class);

    /**
     * The empty folder iterator
     */
    public static final FolderObjectIterator EMPTY_FOLDER_ITERATOR = new FolderObjectIterator() {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public FolderObject next() throws SearchIteratorException {
            return null;
        }

        @Override
        public void close() throws SearchIteratorException {
            // Nothing to close
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean hasSize() {
            return true;
        }

        @Override
        public boolean hasWarnings() {
            return false;
        }

        @Override
        public AbstractOXException[] getWarnings() {
            return null;
        }

        @Override
        public void addWarning(final AbstractOXException warning) {
            // Nothing to add
        }
    };

    private final Queue<FolderObject> prefetchQueue;

    private boolean isClosed;

    private final boolean closeCon;

    private final Set<Integer> folderIds;

    private FolderObject next;

    private Statement stmt;

    private ResultSet rs;

    private Connection readCon;

    private final Context ctx;

    private ElementAttributes attribs;

    private final boolean resideInCache;

    private final List<AbstractOXException> warnings;

    private static final String[] selectFields = {
        "fuid", "parent", "fname", "module", "type", "creating_date", "created_from", "changing_date", "changed_from", "permission_flag",
        "subfolder_flag", "default_flag" };

    /**
     * Gets all necessary fields in right order to be used in an SQL <i>SELECT</i> statement needed to create instances of
     * {@link FolderObject}.
     * 
     * @param tableAlias The table alias used throughout corresponding SQL <i>SELECT</i> statement or <code>null</code> if no alias used.
     * @return All necessary fields in right order to be used in an SQL <i>SELECT</i> statement
     */
    public static final String getFieldsForSQL(final String tableAlias) {
        final StringBuilder fields = new StringBuilder();
        final boolean useTableName = (tableAlias != null);
        if (useTableName) {
            fields.append(tableAlias);
            fields.append('.');
        }
        fields.append(selectFields[0]);
        for (int i = 1; i < selectFields.length; i++) {
            fields.append(", ");
            if (useTableName) {
                fields.append(tableAlias);
                fields.append('.');
            }
            fields.append(selectFields[i]);
        }
        return fields.toString();
    }

    /**
     * Initializes a new {@link FolderObjectIterator}
     */
    private FolderObjectIterator() {
        closeCon = false;
        resideInCache = false;
        ctx = null;
        prefetchQueue = null;
        folderIds = null;
        warnings = new ArrayList<AbstractOXException>(2);
    }

    /**
     * Initializes a new {@link FolderObjectIterator} from specified collection.
     * 
     * @param col The collection containing instances of {@link FolderObject}
     * @param resideInCache If objects shall reside in cache permanently or shall be removed according to cache policy
     * @throws SearchIteratorException If an unsupported collection is specified
     */
    public FolderObjectIterator(final Collection<FolderObject> col, final boolean resideInCache) throws SearchIteratorException {
        folderIds = null;
        warnings = new ArrayList<AbstractOXException>(2);
        rs = null;
        stmt = null;
        ctx = null;
        closeCon = false;
        this.resideInCache = resideInCache;
        if ((col == null) || col.isEmpty()) {
            next = null;
            prefetchQueue = null;
        } else {
            prefetchQueue = new LinkedList<FolderObject>(col);
            next = prefetchQueue.poll();
        }
    }

    /**
     * Initializes a new {@link FolderObjectIterator}
     * 
     * @param rs The result set providing selected folder data
     * @param stmt The fired statement (to release all resources on iterator end)
     * @param resideInCache If objects shall reside in cache permanently or shall be removed according to cache policy
     * @param ctx The context
     * @param readCon A connection holding at least read capability
     * @param closeCon Whether to close given connection or not
     * @throws SearchIteratorException If instantiation fails.
     */
    public FolderObjectIterator(final ResultSet rs, final Statement stmt, final boolean resideInCache, final Context ctx, final Connection readCon, final boolean closeCon) throws SearchIteratorException {
        if (OXFolderProperties.isEnableDBGrouping()) {
            folderIds = null;
        } else {
            folderIds = new HashSet<Integer>();
        }
        warnings = new ArrayList<AbstractOXException>(2);
        this.rs = rs;
        this.stmt = stmt;
        this.readCon = readCon;
        this.ctx = ctx;
        this.closeCon = closeCon;
        this.resideInCache = resideInCache;
        /*
         * Set next to first result set entry
         */
        final boolean prefetchEnabled = ServerConfig.getBoolean(Property.PrefetchEnabled);
        try {
            if (this.rs.next()) {
                next = createFolderObjectFromSelectedEntry();
            } else if (!prefetchEnabled) {
                closeResources();
            }
        } catch (final SQLException e) {
            throw new SearchIteratorException(SearchIteratorCode.SQL_ERROR, e, EnumComponent.FOLDER, e.getMessage());
        } catch (final DBPoolingException e) {
            throw new SearchIteratorException(SearchIteratorCode.DBPOOLING_ERROR, e, EnumComponent.FOLDER, e.getMessage());
        }
        if (prefetchEnabled) {
            prefetchQueue = new LinkedList<FolderObject>();
            /*
             * ResultSet prefetch is enabled. Fill iterator with whole ResultSet's content
             */
            try {
                while (this.rs.next()) {
                    FolderObject fo = createFolderObjectFromSelectedEntry();
                    while ((fo == null) && this.rs.next()) {
                        fo = createFolderObjectFromSelectedEntry();
                    }
                    if (fo != null) {
                        prefetchQueue.offer(fo);
                    }
                }
            } catch (final SQLException e) {
                throw new SearchIteratorException(SearchIteratorCode.SQL_ERROR, e, EnumComponent.FOLDER, e.getMessage());
            } catch (final DBPoolingException e) {
                throw new SearchIteratorException(SearchIteratorCode.DBPOOLING_ERROR, e, EnumComponent.FOLDER, e.getMessage());
            } finally {
                closeResources();
            }
        } else {
            prefetchQueue = null;
        }
    }

    private final ElementAttributes getEternalAttributes() throws FolderCacheNotEnabledException, CacheException, OXException {
        if (attribs == null) {
            attribs = FolderCacheManager.getInstance().getDefaultFolderObjectAttributes();
            attribs.setIdleTime(-1); // eternal
            attribs.setMaxLifeSeconds(-1); // eternal
            attribs.setIsEternal(true);
        }
        return attribs.copy();
    }

    /**
     * @return a <code>FolderObject</code> from current <code>ResultSet.next()</code> data
     */
    private final FolderObject createFolderObjectFromSelectedEntry() throws SQLException, DBPoolingException {
        // fname, fuid, module, type, creator
        final int folderId = rs.getInt(1);
        if (!OXFolderProperties.isEnableDBGrouping()) {
            if (folderIds.contains(Integer.valueOf(folderId))) {
                return null;
            }
            folderIds.add(Integer.valueOf(folderId));
        }
        /*
         * Look up cache
         */
        if (FolderCacheManager.isInitialized()) {
            try {
                final FolderObject fld = FolderCacheManager.getInstance().getFolderObject(folderId, ctx);
                if (fld != null) {
                    return fld;
                }
            } catch (final OXException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        /*
         * Not in cache; create from read data
         */
        final FolderObject fo = new FolderObject(rs.getString(3), folderId, rs.getInt(4), rs.getInt(5), rs.getInt(7));
        fo.setParentFolderID(rs.getInt(2)); // parent
        long tStmp = rs.getLong(6); // creating_date
        if (rs.wasNull()) {
            fo.setCreationDate(new Date());
        } else {
            fo.setCreationDate(new Date(tStmp));
        }
        fo.setCreatedBy(rs.getInt(7)); // created_from
        tStmp = rs.getLong(8); // changing_date
        if (rs.wasNull()) {
            fo.setLastModified(new Date());
        } else {
            fo.setLastModified(new Date(tStmp));
        }
        fo.setModifiedBy(rs.getInt(9)); // changed_from
        fo.setPermissionFlag(rs.getInt(10));
        int subfolder = rs.getInt(11);
        if (rs.wasNull()) {
            subfolder = 0;
        }
        fo.setSubfolderFlag(subfolder > 0);
        int defaultFolder = rs.getInt(12);
        if (rs.wasNull()) {
            defaultFolder = 0;
        }
        fo.setDefaultFolder(defaultFolder > 0);
        /*
         * Read & set permissions
         */
        fo.setPermissionsAsArray(FolderObject.getFolderPermissions(rs.getInt(1), ctx, readCon));
        /*
         * Determine if folder object should be put into cache or not
         */
        if (FolderCacheManager.isInitialized()) {
            try {
                if (resideInCache) {
                    FolderCacheManager.getInstance().putFolderObject(fo, ctx, false, getEternalAttributes());
                } else {
                    FolderCacheManager.getInstance().putFolderObject(fo, ctx, false, null);
                }
            } catch (final FolderCacheNotEnabledException e) {
                LOG.error(e.getMessage(), e);
            } catch (final OXException e) {
                LOG.error(e.getMessage(), e);
            } catch (final CacheException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return fo;
    }

    private final void closeResources() throws SearchIteratorException {
        SearchIteratorException error = null;
        /*
         * Close ResultSet
         */
        if (rs != null) {
            try {
                rs.close();
            } catch (final SQLException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getMessage(), e);
                }
                error = new SearchIteratorException(SearchIteratorCode.SQL_ERROR, e, EnumComponent.FOLDER, e.getMessage());
            }
            rs = null;
        }
        /*
         * Close Statement
         */
        if (stmt != null) {
            try {
                stmt.close();
            } catch (final SQLException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getMessage(), e);
                }
                if (error == null) {
                    error = new SearchIteratorException(SearchIteratorCode.SQL_ERROR, e, EnumComponent.FOLDER, e.getMessage());
                }
            }
            stmt = null;
        }
        /*
         * Close connection
         */
        if (closeCon && (readCon != null)) {
            DBPool.push(ctx, readCon);
            readCon = null;
        }
        if (error != null) {
            throw error;
        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.tools.iterator.SearchIterator#hasNext()
     */
    public boolean hasNext() {
        if (isClosed) {
            return false;
        }
        return next != null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.tools.iterator.SearchIterator#next()
     */
    public FolderObject next() throws SearchIteratorException {
        if (isClosed) {
            throw new SearchIteratorException(SearchIteratorCode.CLOSED, EnumComponent.FOLDER);
        }
        try {
            final FolderObject retval = next;
            next = null;
            if (prefetchQueue != null) {
                /*
                 * Select next from queue
                 */
                if (!prefetchQueue.isEmpty()) {
                    next = prefetchQueue.poll();
                    while ((next == null) && !prefetchQueue.isEmpty()) {
                        next = prefetchQueue.poll();
                    }
                }
            } else {
                /*
                 * Select next from underlying ResultSet
                 */
                if (rs.next()) {
                    next = createFolderObjectFromSelectedEntry();
                    while ((next == null) && rs.next()) {
                        next = createFolderObjectFromSelectedEntry();
                    }
                    if (next == null) {
                        close();
                    }
                } else {
                    close();
                }
            }
            return retval;
        } catch (final SQLException e) {
            throw new SearchIteratorException(SearchIteratorCode.SQL_ERROR, e, EnumComponent.FOLDER, e.getMessage());
        } catch (final DBPoolingException e) {
            throw new SearchIteratorException(SearchIteratorCode.DBPOOLING_ERROR, e, EnumComponent.FOLDER, e.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.tools.iterator.SearchIterator#close()
     */
    public void close() throws SearchIteratorException {
        if (isClosed) {
            return;
        }
        next = null;
        try {
            closeResources();
        } catch (final SearchIteratorException e) {
            LOG.error(e.getMessage(), e);
        }
        isClosed = true;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.tools.iterator.SearchIterator#size()
     */
    public int size() {
        if (prefetchQueue != null) {
            return prefetchQueue.size() + (next == null ? 0 : 1);
        }
        throw new UnsupportedOperationException("Method size() not implemented");
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.tools.iterator.SearchIterator#hasSize()
     */
    public boolean hasSize() {
        /*
         * Size can be predicted if prefetch queue is not null
         */
        return (prefetchQueue != null);
    }

    public void addWarning(final AbstractOXException warning) {
        warnings.add(warning);
    }

    public AbstractOXException[] getWarnings() {
        return warnings.isEmpty() ? null : warnings.toArray(new AbstractOXException[warnings.size()]);
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * Creates a <code>java.util.Queue</code> containing all iterator's elements. All resources are closed immediately.
     * 
     * @return iterator's content backed up by a <code>java.util.Queue</code>
     * @throws SearchIteratorException if any error occurs
     */
    public Queue<FolderObject> asQueue() throws SearchIteratorException {
        return asLinkedList();
    }

    /**
     * Creates a <code>java.util.List</code> containing all iterator's elements. All resources are closed immediately.
     * 
     * @return iterator's content backed up by a <code>java.util.List</code>
     * @throws SearchIteratorException if any error occurs
     */
    public List<FolderObject> asList() throws SearchIteratorException {
        return asLinkedList();
    }

    private LinkedList<FolderObject> asLinkedList() throws SearchIteratorException {
        final LinkedList<FolderObject> retval = new LinkedList<FolderObject>();
        if (isClosed) {
            return retval;
        }
        try {
            if (next == null) {
                return retval;
            }
            retval.add(next);
            if (prefetchQueue != null) {
                retval.addAll(prefetchQueue);
                return retval;
            }
            while (rs.next()) {
                FolderObject fo = createFolderObjectFromSelectedEntry();
                while ((fo == null) && rs.next()) {
                    fo = createFolderObjectFromSelectedEntry();
                }
                if (fo != null) {
                    retval.offer(fo);
                }
            }
            return retval;
        } catch (final DBPoolingException e) {
            throw new SearchIteratorException(SearchIteratorCode.DBPOOLING_ERROR, e, EnumComponent.FOLDER, e.getMessage());
        } catch (final SQLException e) {
            throw new SearchIteratorException(SearchIteratorCode.SQL_ERROR, e, EnumComponent.FOLDER, e.getMessage());
        } finally {
            next = null;
            try {
                closeResources();
            } catch (final SearchIteratorException e) {
                LOG.error(e.getMessage(), e);
            }
            isClosed = true;
        }
    }
}
