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

package com.openexchange.ajax.requesthandler.cache;

import java.sql.Connection;
import java.sql.SQLException;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageEventConstants;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.preview.PreviewExceptionCodes;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;


/**
 * {@link AbstractResourceCache}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public abstract class AbstractResourceCache implements ResourceCache, EventHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractResourceCache.class);

    @Override
    public boolean isEnabledFor(int contextId, int userId) throws OXException {
        final ConfigViewFactory factory = ServerServiceRegistry.getInstance().getService(ConfigViewFactory.class);
        final boolean defaultValue = true;
        if (null == factory) {
            return defaultValue;
        }
        final ConfigView configView = factory.getView(userId, contextId);
        final ComposedConfigProperty<Boolean> enabledProp = configView.property("com.openexchange.preview.cache.enabled", boolean.class);
        return enabledProp.isDefined() ? enabledProp.get().booleanValue() : defaultValue;
    }

    @Override
    public long[] getContextQuota(final int contextId) {
        long quota = -1L;
        long quotaPerDocument = -1L;

        // TODO: Check context-wise quota values
        final ConfigurationService confService = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
        if (null != confService) {
            String property = confService.getProperty("com.openexchange.preview.cache.quota", "10485760").trim();
            try {
                quota = Long.parseLong(property);
            } catch (final NumberFormatException e) {
                quota = -1L;
            }
            property = confService.getProperty("com.openexchange.preview.cache.quotaPerDocument", "524288").trim();
            try {
                quotaPerDocument = Long.parseLong(property);
            } catch (final NumberFormatException e) {
                quotaPerDocument = -1L;
            }
        }
        return new long[] { quota, quotaPerDocument };
    }

    @Override
    public void handleEvent(final Event event) {
        final String topic = event.getTopic();
        if (FileStorageEventConstants.UPDATE_TOPIC.equals(topic)) {
            try {
                final Session session = (Session) event.getProperty(FileStorageEventConstants.SESSION);
                final int userId = session.getUserId();
                final int contextId = session.getContextId();
                removeAlikes(event.getProperty(FileStorageEventConstants.E_TAG).toString(), userId, contextId);
            } catch (final OXException e) {
                LOG.warn("Couldn't remove cache entry.", e);
            }
        } else if (FileStorageEventConstants.DELETE_TOPIC.equals(topic)) {
            try {
                final Session session = (Session) event.getProperty(FileStorageEventConstants.SESSION);
                final int userId = session.getUserId();
                final int contextId = session.getContextId();
                removeAlikes(event.getProperty(FileStorageEventConstants.E_TAG).toString(), userId, contextId);
            } catch (final OXException e) {
                LOG.warn("Couldn't remove cache entry.", e);
            }
        }
    }

    protected ResourceCacheMetadataStore getMetadataStore() {
        return ResourceCacheMetadataStore.getInstance();
    }

    protected DatabaseService getDBService() throws OXException {
        final DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        if (dbService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
        }

        return dbService;
    }

    protected static ResourceCacheMetadata loadExistingEntry(ResourceCacheMetadataStore metadataStore, Connection con, int contextId, int userId, String id) throws OXException {
        ResourceCacheMetadata existingMetadata = null;
        try {
            existingMetadata = metadataStore.load(con, contextId, userId, id);
        } catch (SQLException e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        }

        return existingMetadata;
    }

    /**
     * Prepares specified file MIME type for being put into storage.
     *
     * @param fileType The file MIME type to prepare
     * @param maxLen The max. supported length
     * @return The prepared file MIME type or <code>null</code>
     */
    protected String prepareFileType(final String fileType, final int maxLen) {
        if (Strings.isEmpty(fileType)) {
            return null;
        }
        try {
            final String baseType = new ContentType(fileType.trim()).getBaseType();
            return baseType.length() > maxLen ? baseType.substring(0, maxLen) : baseType;
        } catch (final OXException e) {
            LOG.warn("Could not parse file type: " + fileType, e);
            return null;
        }
    }

}
