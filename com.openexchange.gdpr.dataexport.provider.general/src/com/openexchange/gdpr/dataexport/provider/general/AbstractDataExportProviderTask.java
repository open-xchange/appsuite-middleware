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
     * character (<code>':'</code> or <code>';'</code>) are contained in ZP entry name.
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
     * @param path The path
     * @return <code>true</code> if folder has been successfully exported; otherwise <code>false</code>
     * @throws OXException If export fails
     */
    protected boolean exportFolder(Folder folder, String path) throws OXException {
        return sink.export(new Directory(path, sanitizeNameForZipEntry(folder.getName())));
    }

    /**
     * Exports items.
     *
     * @return The export result
     * @throws OXException If export fails
     */
    public abstract ExportResult export() throws OXException;

}
