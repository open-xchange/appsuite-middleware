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

package com.openexchange.gdpr.dataexport.provider.general;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.json.JSONObject;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.exception.OXException;
import com.openexchange.gdpr.dataexport.DataExportProvider;
import com.openexchange.gdpr.dataexport.DataExportSink;
import com.openexchange.gdpr.dataexport.DataExportTask;
import com.openexchange.gdpr.dataexport.ExportResult;
import com.openexchange.gdpr.dataexport.PauseResult;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link AbstractDataExportProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public abstract class AbstractDataExportProvider<T extends AbstractDataExportProviderTask> implements DataExportProvider {

    /** The service look-up */
    protected final ServiceLookup services;

    private final ConcurrentMap<UUID, T> runningExports;

    /**
     * Initializes a new {@link AbstractDataExportProvider}.
     *
     * @param services The service look-up
     */
    protected AbstractDataExportProvider(ServiceLookup services) {
        super();
        this.services = services;
        runningExports = new ConcurrentHashMap<UUID, T>(16, 0.9F, 1);
    }

    /**
     * Gets the value for named boolean property.
     *
     * @param propertyName The property name
     * @param def The default value to assume
     * @param session The session providing user information
     * @return The boolean value
     * @throws OXException If property's boolean cannot be returned
     */
    protected boolean getBoolProperty(String propertyName, boolean def, Session session) throws OXException {
        ConfigViewFactory configViewFactory = services.getOptionalService(ConfigViewFactory.class);
        if (configViewFactory == null) {
            return def;
        }

        ConfigView view = configViewFactory.getView(session.getUserId(), session.getContextId());
        return ConfigViews.getDefinedBoolPropertyFrom(propertyName, def, view);
    }

    /**
     * Creates a new task to execute.
     *
     * @param processingId A unique identifier for provider's processing used to identify this invocation later on
     * @param sink The sink to output to
     * @param savepoint The optional save-point previously set by this provider
     * @param task The data export task providing needed arguments
     * @param locale The locale
     * @return The new task
     * @throws OXException If a new task cannot be created
     */
    protected abstract T createTask(UUID processingId, DataExportSink sink, Optional<JSONObject> savepoint, DataExportTask task, Locale locale) throws OXException;

    @Override
    public ExportResult export(UUID processingId, DataExportSink sink, Optional<JSONObject> savepoint, DataExportTask task, Locale locale) throws OXException, InterruptedException {
        T exportTask = createTask(processingId, sink, savepoint, task, locale);
        runningExports.put(processingId, exportTask);
        try {
            return exportTask.export();
        } finally {
            runningExports.remove(processingId);
        }
    }

    @Override
    public PauseResult pause(UUID processingId, DataExportSink sink, DataExportTask task) throws OXException {
        T exportTask = runningExports.get(processingId);
        if (exportTask != null) {
            exportTask.markPauseRequested();
        }
        return PauseResult.unpaused();
    }

}
