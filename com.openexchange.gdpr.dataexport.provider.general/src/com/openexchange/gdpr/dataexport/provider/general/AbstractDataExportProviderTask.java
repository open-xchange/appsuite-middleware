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

import java.util.Iterator;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.gdpr.dataexport.DataExportAbortedException;
import com.openexchange.gdpr.dataexport.DataExportSink;
import com.openexchange.gdpr.dataexport.DataExportStatus;
import com.openexchange.gdpr.dataexport.DataExportStatusChecker;
import com.openexchange.gdpr.dataexport.DataExportTask;
import com.openexchange.gdpr.dataexport.Directory;
import com.openexchange.gdpr.dataexport.ExportResult;
import com.openexchange.gdpr.dataexport.Module;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;

/**
 * {@link AbstractDataExportProviderTask} - The abstract provider task performing the actual export of directories and items.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public abstract class AbstractDataExportProviderTask {

    /** The prefix indicating the virtual <i>shared</i> root (com.openexchange.groupware.container.FolderObject.SHARED_PREFIX) */
    protected static final String SHARED_PREFIX = "u:";

    /** The sink to output to */
    protected final DataExportSink sink;

    /** The optional save-point previously set by this provider */
    protected final Optional<JSONObject> savepoint;

    /** The data export task providing needed arguments */
    protected final DataExportTask task;

    /** The user's locale */
    protected final Locale locale;

    /** The service look-up */
    protected final ServiceLookup services;

    private final AtomicInteger count;
    private final AtomicBoolean pauseRequested;
    private final String moduleId;

    /**
     * Initializes a new {@link AbstractDataExportProviderTask}.
     *
     * @param moduleId The provider identifier
     * @param sink The sink to output to
     * @param savepoint The optional save-point previously set by this provider
     * @param task The data export task providing needed arguments
     * @param locale The locale
     * @param services The service look-up
     */
    protected AbstractDataExportProviderTask(String moduleId, DataExportSink sink, Optional<JSONObject> savepoint, DataExportTask task, Locale locale, ServiceLookup services) {
        super();
        this.moduleId = moduleId;
        this.sink = sink;
        this.savepoint = savepoint;
        this.task = task;
        this.locale = locale;
        this.services = services;
        pauseRequested = new AtomicBoolean(false);
        count = new AtomicInteger(0);
    }

    /**
     * The string constant consisting of disallowed characters for a ZIP entry
     * <ul>
     * <li>The system-dependent default name-separator character. On UNIX systems the value is <code>'/'</code>; on Microsoft Windows
     * systems it is <code>'\\'</code>.</li>
     * <li>The system-dependent path-separator character. This character is used to separate file names in a sequence of files given as a
     * <em>path list</em>. On UNIX systems, this character is <code>':'</code>; on Microsoft Windows systems it is <code>';'</code></li>
     * </ul>
     */
    private static final String DISALLOWED_CHARACTERS = "/:\\;";

    private static final char REPLACEMENT_CHARACTER = '_';

    /**
     * Sanitizes the name, which is supposed to be used for a ZIP entry.
     * <p>
     * This method ensures that neither default name-separator character (<code>'/'</code> or <code>'\\'</code>) nor path-separator
     * character (<code>':'</code> or <code>';'</code>) are contained in ZIP entry name.
     *
     * @param name The name for a ZIP entry
     * @return The sanitized name
     */
    protected static String sanitizeNameForZipEntry(String name) {
        if (Strings.isEmpty(name)) {
            return name;
        }

        int length = name.length();
        StringBuilder sb = null;
        for (int i = 0; i < length; i++) {
            char ch = name.charAt(i);
            if (DISALLOWED_CHARACTERS.indexOf(ch) >= 0) {
                if (sb == null) {
                    sb = new StringBuilder(length);
                    if (i > 0) {
                        sb.append(name, 0, i);
                    }
                }
                sb.append(REPLACEMENT_CHARACTER);
            } else {
                if (sb != null) {
                    sb.append(ch);
                }
            }
        }
        return null == sb ? name : sb.toString();
    }

    /**
     * Generates the full name prefix for given full name.
     *
     * @param fullName The full name
     * @return The full name prefix
     */
    protected static String generateFullNamePrefix(String fullName) {
        return Strings.isEmpty(fullName) ? "" : (new StringBuilder(fullName.length() + 3).append(fullName).append(" - ").toString());
    }

    /**
     * Increments and gets the number of items that have been exported.
     *
     * @return The item count
     */
    protected int incrementAndGetCount() {
        int itemNum;
        if ((itemNum = count.incrementAndGet()) <= 0) {
            return count.compareAndSet(itemNum, 1) ? 1 : count.incrementAndGet();
        }
        return itemNum;
    }

    /**
     * Gets the suitable module from associated data export task.
     *
     * @return The module
     * @throw IllegalStateException If there is no such module
     */
    protected Module getModule() {
        Module myModule = null;
        for (Iterator<Module> it = task.getArguments().getModules().iterator(); myModule == null && it.hasNext();) {
            Module module = it.next();
            if (moduleId.equals(module.getId())) {
                myModule = module;
            }
        }
        if (myModule == null) {
            throw new IllegalStateException("No such module: " + moduleId);
        }

        return myModule;
    }

    /**
     * Checks if pause has been requested.
     *
     * @return <code>true</code> if pause requested; otherwise <code>false</code>
     * @throws OXException If check cannot be performed
     * @throws DataExportAbortedException If task has been aborted
     */
    protected boolean isPauseRequested() throws OXException, DataExportAbortedException {
        boolean paused = pauseRequested.get();
        if (paused) {
            // Check if paused due to abortion
            checkAborted();
        }
        return paused;
    }

    /**
     * Pause is requested.
     */
    public void markPauseRequested() {
        pauseRequested.set(true);
    }

    /**
     * Checks if in-progress task has been aborted meanwhile.
     *
     * @throws OXException If check cannot be performed
     * @throws DataExportAbortedException If task has been aborted
     */
    protected void checkAborted() throws OXException, DataExportAbortedException {
        checkAborted(false);
    }

    /**
     * Checks if in-progress task has been aborted meanwhile.
     *
     * @param touch <code>true</code> to touch task's last-accessed time stamp in case it is not aborted; otherwise <code>false</code>
     * @throws OXException If check cannot be performed
     * @throws DataExportAbortedException If task has been aborted
     */
    protected void checkAborted(boolean touch) throws OXException, DataExportAbortedException {
        DataExportStatusChecker statusChecker = services.getOptionalService(DataExportStatusChecker.class);
        if (statusChecker != null) {
            Optional<DataExportStatus> optionalStatus = statusChecker.getDataExportStatus(task.getUserId(), task.getContextId());
            if (!optionalStatus.isPresent() || optionalStatus.get().isAborted()) {
                // Deleted or aborted
                throw new DataExportAbortedException();
            }
            if (touch) {
                statusChecker.touch(task.getUserId(), task.getContextId());
            }
        }
    }

    /**
     * Try to touch the last-accessed time stamp of in-progress task.
     *
     * @throws OXException If last-accessed time stamp cannot be updated
     */
    protected void tryTouch() throws OXException {
        DataExportStatusChecker statusChecker = services.getOptionalService(DataExportStatusChecker.class);
        if (statusChecker != null) {
            statusChecker.touch(task.getUserId(), task.getContextId());
        }
    }

    /**
     * Exports specified folder as hierarchy delimiter to data export sink.
     *
     * @param folder The folder to export
     * @param parentPath The path of the parent folder; e.g. <code>"INBOX/"</code>
     * @return The actual path if folder has been successfully exported; otherwise <code>null</code>
     * @throws OXException If export fails
     */
    protected String exportFolder(Folder folder, String parentPath) throws OXException {
        return sink.export(new Directory(parentPath, sanitizeNameForZipEntry(folder.getName())));
    }

    /**
     * Exports items.
     *
     * @return The export result
     * @throws OXException If export fails
     */
    public abstract ExportResult export() throws OXException;

}
