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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.ajax.requesthandler.converters.preview.cache.groupware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import com.openexchange.ajax.requesthandler.cache.ResourceCache;
import com.openexchange.ajax.requesthandler.converters.preview.cache.ResourceCacheMBeanImpl;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedExceptionCodes;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.ThreadRenamer;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link PreviewCacheDeleteListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class PreviewCacheDeleteListener implements DeleteListener {

    /**
     * Initializes a new {@link PreviewCacheDeleteListener} instance.
     */
    public PreviewCacheDeleteListener() {
        super();
    }

    @Override
    public void deletePerformed(final DeleteEvent event, final Connection readCon, final Connection writeCon) throws OXException {
        if (event.getType() == DeleteEvent.TYPE_USER) {
            deleteUserEntriesFromDB(event, writeCon);
        } else if (event.getType() == DeleteEvent.TYPE_CONTEXT) {
            final DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
            if (null == dbService) {
                deleteContextEntries(event.getContext().getContextId(), writeCon);
                return;
            }

            // Perform the two steps asynchronously
            final int contextId = event.getContext().getContextId();
            AbstractTask<Void> task = new AbstractTask<Void>() {

                @Override
                public void setThreadName(ThreadRenamer threadRenamer) {
                    threadRenamer.renamePrefix("PreviewCacheDeleteListener-");
                }

                @Override
                public Void call() throws OXException {
                    // Cleanse by instance
                    deleteContextEntries(contextId, null);

                    // Cleanse database content
                    Connection con = dbService.getWritable(contextId);
                    try {
                        deleteFromDB(contextId, writeCon);
                        return null;
                    } finally {
                        dbService.backWritable(contextId, con);
                    }
                }
            };
            ThreadPools.getThreadPool().submit(task, CallerRunsBehavior.<Void> getInstance());
        }
    }

    protected void deleteContextEntries(final int contextId, final Connection writeCon) throws OXException {
        // Cleanse by instance
        final ResourceCache resourceCache = ResourceCacheMBeanImpl.CACHE_REF.get();
        if (null != resourceCache) {
            try {
                resourceCache.clearFor(contextId);
            } catch (final Exception e) {
                final Logger logger = org.slf4j.LoggerFactory.getLogger(PreviewCacheDeleteListener.class);
                logger.warn("Failed to clean resource for deleted context {}", Integer.valueOf(contextId), e);
            }
        }

        if (null != writeCon) {
            deleteFromDB(contextId, writeCon);
        }
    }

    protected void deleteFromDB(final int contextId, final Connection writeCon) throws OXException {
        // DB cleansing
        PreparedStatement stmt = null;
        try {
            stmt = writeCon.prepareStatement("DELETE FROM preview WHERE cid = ?");
            stmt.setInt(1, contextId);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw DeleteFailedExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private void deleteUserEntriesFromDB(final DeleteEvent event, final Connection writeCon) throws OXException {
        final int contextId = event.getContext().getContextId();
        final int userId = event.getId();

        // Cleanse by instance
        final ResourceCache resourceCache = ResourceCacheMBeanImpl.CACHE_REF.get();
        if (null != resourceCache) {
            try {
                resourceCache.remove(userId, contextId);
            } catch (final Exception e) {
                final Logger logger = org.slf4j.LoggerFactory.getLogger(PreviewCacheDeleteListener.class);
                logger.warn("Failed to clean resource for deleted user {} in context {}", Integer.valueOf(userId), Integer.valueOf(contextId), e);
            }
        }

        // DB cleansing
        PreparedStatement stmt = null;
        try {
            stmt = writeCon.prepareStatement("DELETE FROM preview WHERE cid = ? AND user = ?");
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos, userId);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw DeleteFailedExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

}
