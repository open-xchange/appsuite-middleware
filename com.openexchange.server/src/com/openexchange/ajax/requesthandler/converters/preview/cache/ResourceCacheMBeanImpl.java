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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.ajax.requesthandler.converters.preview.cache;

import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.management.MBeanException;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.requesthandler.cache.ResourceCache;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;


/**
 * {@link ResourceCacheMBeanImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ResourceCacheMBeanImpl extends StandardMBean implements ResourceCacheMBean {

    /** The cache reference */
    public static final AtomicReference<ResourceCache> CACHE_REF = new AtomicReference<ResourceCache>();

    /**
     * Initializes a new {@link ResourceCacheMBeanImpl}.
     *
     * @throws NotCompliantMBeanException
     */
    public ResourceCacheMBeanImpl() throws NotCompliantMBeanException {
        super(ResourceCacheMBean.class);
    }

    @Override
    public void clear() throws MBeanException {
        final ResourceCache resourceCache = CACHE_REF.get();
        if (null != resourceCache) {
            final Logger logger = LoggerFactory.getLogger(ResourceCacheMBeanImpl.class);
            List<Integer> contextIds = null;
            try {
                contextIds = getContextIds();
            } catch (OXException e) {
                logger.error("", e);
                final String message = e.getMessage();
                throw new MBeanException(new Exception(message), message);
            }

            for (final Integer contextId : contextIds) {
                try {
                    resourceCache.clearFor(contextId.intValue());
                } catch (final OXException e) {
                    logger.error("", e);
                    final String message = e.getMessage();
                    throw new MBeanException(new Exception(message), message);
                } catch (final RuntimeException e) {
                    logger.error("", e);
                    final String message = e.getMessage();
                    throw new MBeanException(new Exception(message), message);
                }
            }
        }
    }

    /**
     * Gets available context identifier.
     *
     * @param optService The optional database service
     * @return The context identifiers
     * @throws OXException If identifiers cannot be loaded from configDB
     */
    private List<Integer> getContextIds() throws OXException {
        final ContextService contextService = ServerServiceRegistry.getInstance().getService(ContextService.class);
        if (null == contextService) {
            throw ServiceExceptionCode.absentService(ContextService.class);
        }
        return contextService.getAllContextIds();
    }

    @Override
    public void clearFor(final int contextId) throws MBeanException {
        final ResourceCache resourceCache = CACHE_REF.get();
        if (null != resourceCache) {
            try {
                resourceCache.clearFor(contextId);
            } catch (final Exception e) {
                LoggerFactory.getLogger(ResourceCacheMBeanImpl.class).error("", e);
                final String message = e.getMessage();
                throw new MBeanException(new Exception(message), message);
            }
        }
    }

    @Override
    public String sanitizeMimeTypesInDatabaseFor(int contextId, String invalids) throws MBeanException {
        final Logger logger = LoggerFactory.getLogger(ResourceCacheMBeanImpl.class);
        try {
            final DatabaseService databaseService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
            if (null == databaseService) {
                final String message = "Missing service: " + DatabaseService.class.getName();
                throw new MBeanException(new Exception(message), message);
            }

            final Set<String> invalidsSet = new HashSet<String>(Arrays.asList("application/force-download", "application/x-download", "application/$suffix"));
            if (!com.openexchange.java.Strings.isEmpty(invalids)) {
                for (final String invalid : invalids.split(" *, *")) {
                    invalidsSet.add(com.openexchange.java.Strings.toLowerCase(invalid.trim()));
                }
            }

            if (contextId >= 0) {
                return processContext(contextId, invalidsSet, databaseService);
            }

            // Process all available contexts
            final TIntSet contextIds;
            {
                Connection configDbCon = null;
                PreparedStatement stmt = null;
                ResultSet rs = null;
                try {
                    configDbCon = databaseService.getReadOnly();
                    stmt = configDbCon.prepareStatement("SELECT cid FROM context");
                    rs = stmt.executeQuery();

                    contextIds = new TIntHashSet();

                    while (rs.next()) {
                        contextIds.add(rs.getInt(1));
                    }
                } finally {
                    Databases.closeSQLStuff(rs, stmt);
                    rs = null;
                    stmt = null;
                    if (null != configDbCon) {
                        databaseService.backReadOnly(configDbCon);
                        configDbCon = null;
                    }
                }
            }

            if (contextIds.isEmpty()) {
                return "No contexts found";
            }

            final String sep = System.getProperty("line.separator");
            final StringBuilder responseBuilder = new StringBuilder(65536);
            final AtomicReference<Exception> errorRef = new AtomicReference<Exception>();
            contextIds.forEach(new TIntProcedure() {

                @Override
                public boolean execute(final int cid) {
                    if (responseBuilder.length() > 0) {
                        responseBuilder.append(sep);
                    }
                    try {
                        responseBuilder.append(processContext(cid, invalidsSet, databaseService));
                    } catch (final Exception e) {
                        logger.error("Context {} could not be processed", Integer.valueOf(cid), e);
                        responseBuilder.append("Context ").append(cid).append(" could not be processed: >>>").append(e.getMessage()).append("<<<");
                    }
                    return true;
                }
            });

            final Exception exc = errorRef.get();
            if (null != exc) {
                throw exc;
            }

            return responseBuilder.toString();
        } catch (final Exception e) {
            logger.error("", e);
            final String message = e.getMessage();
            throw new MBeanException(new Exception(message), message);
        }
    }

    String processContext(final int contextId, final Set<String> invalidsSet, final DatabaseService databaseService) throws OXException, SQLException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean afterReading = true;

        try {
            con = databaseService.getForUpdateTask(contextId);
            stmt = con.prepareStatement("SELECT infostore_id, version_number, file_mimetype, filename FROM infostore_document WHERE cid=?");
            stmt.setInt(1, contextId);
            rs = stmt.executeQuery();

            if (!rs.next()) {
                // No documents in specified context;
                return "Context " + contextId + " does not hold any documents.";
            }

            class Tuple {

                int id;
                int version;
                String mimeType;

                Tuple(String mimeType, ResultSet rs) throws SQLException {
                    super();
                    id = rs.getInt(1);
                    version = rs.getInt(2);
                    this.mimeType = mimeType;
                }
            }

            final String defaultMimeType = MimeTypes.MIME_APPL_OCTET;
            final List<Tuple> tuples = new LinkedList<Tuple>();
            do {
                String fileName = rs.getString(4);
                if (!com.openexchange.java.Strings.isEmpty(fileName)) {
                    String mimeType = rs.getString(3);
                    if (!com.openexchange.java.Strings.isEmpty(mimeType)) {
                        final String contentTypeByFileName = MimeType2ExtMap.getContentType(fileName);
                        if (invalidsSet.contains(com.openexchange.java.Strings.toLowerCase(mimeType)) || (!defaultMimeType.equals(contentTypeByFileName) && !equalPrimaryTypes(mimeType, contentTypeByFileName))) {
                            tuples.add(new Tuple(contentTypeByFileName, rs));
                        }
                    }
                }
            } while (rs.next());
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            if (tuples.isEmpty()) {
                return "No document with a broken/corrupt MIME type found in context " + contextId;
            }

            stmt = con.prepareStatement("UPDATE infostore_document SET file_mimetype=? WHERE cid=? AND infostore_id=? AND version_number=?");
            stmt.setInt(2, contextId);

            for (final Tuple tuple : tuples) {
                stmt.setString(1, tuple.mimeType);
                stmt.setInt(3, tuple.id);
                stmt.setInt(4, tuple.version);
                stmt.addBatch();
            }

            final int[] result = stmt.executeBatch();
            afterReading = false;

            return "Fixed " + Integer.toString(result.length) + (result.length == 1 ? " document" : " documents") + " with a broken/corrupt MIME type in context " + contextId;
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            if (null != con) {
                if (afterReading) {
                    databaseService.backForUpdateTaskAfterReading(contextId, con);
                } else {
                    databaseService.backForUpdateTask(contextId, con);
                }
            }
        }
    }

    private String getPrimaryType(final String contentType) {
        if (com.openexchange.java.Strings.isEmpty(contentType)) {
            return contentType;
        }
        final int pos = contentType.indexOf('/');
        return pos > 0 ? contentType.substring(0, pos) : contentType;
    }

    private boolean equalPrimaryTypes(final String contentType1, final String contentType2) {
        if (null == contentType1 || null == contentType2) {
            return false;
        }
        return com.openexchange.java.Strings.toLowerCase(getPrimaryType(contentType1)).startsWith(com.openexchange.java.Strings.toLowerCase(getPrimaryType(contentType2)));
    }

}
