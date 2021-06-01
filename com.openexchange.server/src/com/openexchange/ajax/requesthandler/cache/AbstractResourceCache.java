/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.requesthandler.cache;

import static com.openexchange.ajax.requesthandler.cache.ResourceCacheProperties.DOCUMENT_QUOTA;
import static com.openexchange.ajax.requesthandler.cache.ResourceCacheProperties.ENABLED;
import static com.openexchange.ajax.requesthandler.cache.ResourceCacheProperties.GLOBAL_QUOTA;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
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
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.timer.TimerService;


/**
 * {@link AbstractResourceCache}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public abstract class AbstractResourceCache implements ResourceCache, EventHandler, Reloadable {

    protected static final int MAX_FILE_TYPE_LENGTH = 255;

    protected static final int MAX_FILE_NAME_LENGTH = 767;

    private static final Logger LOG = LoggerFactory.getLogger(AbstractResourceCache.class);

    private final AtomicLong globalQuota = new AtomicLong(-1L);

    private final AtomicLong documentQuota = new AtomicLong(-1L);

    private final ServiceLookup serviceLookup;

    protected AbstractResourceCache(final ServiceLookup serviceLookup) {
        super();
        this.serviceLookup = serviceLookup;
        initQuotas(serviceLookup.getService(ConfigurationService.class));
    }

    @Override
    public boolean isEnabledFor(int contextId, int userId) throws OXException {
        final ConfigViewFactory factory = ServerServiceRegistry.getInstance().getService(ConfigViewFactory.class);
        final boolean defaultValue = true;
        if (null == factory) {
            return defaultValue;
        }
        final ConfigView configView = factory.getView(userId, contextId);
        final ComposedConfigProperty<Boolean> enabledProp = configView.property(ENABLED, boolean.class);
        return enabledProp.isDefined() ? enabledProp.get().booleanValue() : defaultValue;
    }

    @Override
    public void handleEvent(final Event event) {
        final String topic = event.getTopic();
        if (FileStorageEventConstants.UPDATE_TOPIC.equals(topic)) {
            try {
                final Session session = (Session) event.getProperty(FileStorageEventConstants.SESSION);
                final int contextId = session.getContextId();
                removeAlikes(event.getProperty(FileStorageEventConstants.E_TAG).toString(), 0, contextId);
            } catch (OXException e) {
                LOG.warn("Couldn't remove cache entry.", e);
            }
        } else if (FileStorageEventConstants.DELETE_TOPIC.equals(topic)) {
            try {
                final Session session = (Session) event.getProperty(FileStorageEventConstants.SESSION);
                final int contextId = session.getContextId();
                removeAlikes(event.getProperty(FileStorageEventConstants.E_TAG).toString(), 0, contextId);
            } catch (OXException e) {
                LOG.warn("Couldn't remove cache entry.", e);
            }
        }
    }

    //                                                               \\
    // ===================== Reloadable ===================== \\
    //

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        initQuotas(configService);
    }

    @Override
    public Interests getInterests() {
        return Reloadables.interestsForProperties(GLOBAL_QUOTA, DOCUMENT_QUOTA);
    }

    private void initQuotas(ConfigurationService configService) {
        String property = configService.getProperty(GLOBAL_QUOTA, "10485760").trim();
        try {
            globalQuota.set(Long.parseLong(property));
        } catch (NumberFormatException e) {
            globalQuota.set(-1L);
        }

        property = configService.getProperty(DOCUMENT_QUOTA, "524288").trim();
        try {
            documentQuota.set(Long.parseLong(property));
        } catch (NumberFormatException e) {
            documentQuota.set(-1L);
        }
    }

    //                                                               \\
    // ===================== protected members ===================== \\
    //                                                               \\

    protected ResourceCacheMetadataStore getMetadataStore() {
        return ResourceCacheMetadataStore.getInstance();
    }

    protected DatabaseService getDBService() throws OXException {
        final DatabaseService dbService = serviceLookup.getService(DatabaseService.class);
        if (dbService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
        }

        return dbService;
    }

    protected ConfigurationService getConfigurationService() throws OXException {
        final ConfigurationService configService = serviceLookup.getService(ConfigurationService.class);
        if (configService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ConfigurationService.class.getName());
        }

        return configService;
    }

    protected TimerService optTimerService() {
        return serviceLookup.getService(TimerService.class);
    }

    protected long getGlobalQuota(int userId, int contextId) throws OXException {
        ConfigViewFactory viewFactory = ServerServiceRegistry.getInstance().getService(ConfigViewFactory.class);
        if (null == viewFactory) {
            return globalQuota.get();
        }

        ConfigView view = viewFactory.getView(userId, contextId);
        ComposedConfigProperty<Long> property = view.property(GLOBAL_QUOTA, Long.class);
        if (!property.isDefined()) {
            return globalQuota.get();
        }
        return property.get().longValue();
    }

    protected long getDocumentQuota(int userId, int contextId) throws OXException {
        ConfigViewFactory viewFactory = ServerServiceRegistry.getInstance().getService(ConfigViewFactory.class);
        if (null == viewFactory) {
            return documentQuota.get();
        }

        ConfigView view = viewFactory.getView(userId, contextId);
        ComposedConfigProperty<Long> property = view.property(DOCUMENT_QUOTA, Long.class);
        if (!property.isDefined()) {
            return documentQuota.get();
        }
        return property.get().longValue();
    }

    /**
     * Prepares specified file MIME type for being put into storage.
     *
     * @param fileType The file MIME type to prepare
     * @return The prepared file MIME type or <code>null</code>
     */
    protected String prepareFileType(final String fileType) {
        if (Strings.isEmpty(fileType)) {
            return null;
        }
        try {
            final String baseType = new ContentType(fileType.trim()).getBaseType();
            return baseType.length() > MAX_FILE_TYPE_LENGTH ? baseType.substring(0, MAX_FILE_TYPE_LENGTH) : baseType;
        } catch (OXException e) {
            LOG.warn("Could not parse file type: {}", fileType, e);
            return null;
        }
    }

    /**
     * Prepares specified file name for being put into storage.
     *
     * @param fileType The file name to prepare
     * @return The prepared file name or <code>null</code>
     */
    protected String prepareFileName(final String fileName) {
        if (Strings.isEmpty(fileName)) {
            return null;
        }

        if (fileName.length() <= MAX_FILE_NAME_LENGTH) {
            return fileName;
        }

        return fileName.substring(0, MAX_FILE_NAME_LENGTH);
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

    protected static boolean entryExists(ResourceCacheMetadataStore metadataStore, Connection con, int contextId, int userId, String id) throws OXException {
        try {
            return metadataStore.exists(con, contextId, userId, id);
        } catch (SQLException e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        }
    }

    protected static ResourceCacheMetadata loadExistingEntryForUpdate(ResourceCacheMetadataStore metadataStore, Connection con, int contextId, int userId, String id) throws OXException {
        ResourceCacheMetadata existingMetadata = null;
        try {
            existingMetadata = metadataStore.loadForUpdate(con, contextId, userId, id);
        } catch (SQLException e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        }

        return existingMetadata;
    }

}
