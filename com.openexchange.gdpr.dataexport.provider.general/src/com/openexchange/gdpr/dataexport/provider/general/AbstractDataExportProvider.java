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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.gdpr.dataexport.provider.general;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.gdpr.dataexport.DataExportProvider;
import com.openexchange.gdpr.dataexport.DataExportSink;
import com.openexchange.gdpr.dataexport.DataExportTask;
import com.openexchange.gdpr.dataexport.ExportResult;
import com.openexchange.gdpr.dataexport.PauseResult;
import com.openexchange.server.ServiceLookup;

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
