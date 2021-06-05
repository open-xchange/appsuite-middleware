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

package com.openexchange.gdpr.dataexport.impl;

import static com.openexchange.gdpr.dataexport.impl.DataExportUtility.deleteQuietly;
import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.Deflater;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorageCodes;
import com.openexchange.gdpr.dataexport.DataExportDiagnosticsReport;
import com.openexchange.gdpr.dataexport.DataExportExceptionCode;
import com.openexchange.gdpr.dataexport.DataExportSavepoint;
import com.openexchange.gdpr.dataexport.DataExportSink;
import com.openexchange.gdpr.dataexport.DataExportStorageService;
import com.openexchange.gdpr.dataexport.DataExportTask;
import com.openexchange.gdpr.dataexport.Directory;
import com.openexchange.gdpr.dataexport.Item;
import com.openexchange.gdpr.dataexport.Message;
import com.openexchange.gdpr.dataexport.impl.utils.ZippedFileStorageOutputStream;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.server.ServiceLookup;

/**
 * {@link DataExportSinkImpl} - The export sink to which providers output their data and signal status changes.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DataExportSinkImpl implements DataExportSink {

    /** Simple class to delay initialization until needed */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DataExportSinkImpl.class);

    /**
     * The buffer size of 64K.
     */
    private static final int BUFFER_SIZE = DataExportUtility.BUFFER_SIZE;

    @SuppressWarnings("unused")
    private final ServiceLookup services;
    private final DataExportTask task;
    private final String moduleId;
    private final String pathPrefix;
    private final AtomicReference<String> optExistentFileStorageLocation;
    private final FileStorage fileStorage;
    private final AtomicReference<ZippedFileStorageOutputStream> zipOutReference;
    private final AtomicBoolean canceled;
    private final AtomicBoolean exportCalled;
    private final Optional<DataExportDiagnosticsReport> optionalReport;
    private final DataExportStorageService storageService;

    /**
     * Initializes a new {@link DataExportSinkImpl}.
     *
     * @param task The task associated with this sink
     * @param moduleId The module identifier
     * @param pathPrefix The path prefix
     * @param fileStorageLocation The optional file storage location
     * @param fileStorage The file storage to use as data sink
     * @param storageService The storage service
     * @param report The report to add messages to
     * @param services The service look-up
     */
    public DataExportSinkImpl(DataExportTask task, String moduleId, String pathPrefix, Optional<String> fileStorageLocation, FileStorage fileStorage, DataExportStorageService storageService, Optional<DataExportDiagnosticsReport> optionalReport, ServiceLookup services) {
        super();
        this.task = task;
        this.moduleId = moduleId;
        this.optExistentFileStorageLocation = new AtomicReference<String>(fileStorageLocation.orElse(null));
        this.storageService = storageService;
        if (Strings.isEmpty(pathPrefix)) {
            this.pathPrefix = "";
        } else {
            if (pathPrefix.endsWith("/")) {
                this.pathPrefix = pathPrefix;
            } else {
                this.pathPrefix = new StringBuffer(pathPrefix.length() + 1).append(pathPrefix).append('/').toString();
            }
        }
        this.fileStorage = fileStorage;
        this.optionalReport = optionalReport;
        this.services = services;
        zipOutReference = new AtomicReference<>(null);
        canceled = new AtomicBoolean(false);
        exportCalled = new AtomicBoolean(false);
    }

    private ZippedFileStorageOutputStream getZipOutputStream() throws OXException { // Only called when holding lock
        return getZipOutputStream(Optional.ofNullable(optExistentFileStorageLocation.get()));
    }

    private ZippedFileStorageOutputStream getZipOutputStream(Optional<String> optionalFileStorageLocation) throws OXException { // Only called when holding lock
        ZippedFileStorageOutputStream out = zipOutReference.get();
        if (out == null) {
            boolean error = true; // Pessimistic
            try {
                out = ZippedFileStorageOutputStream.createDefaultZippedFileStorageOutputStream(fileStorage, Deflater.NO_COMPRESSION);
                // Continue writing to ZIP archive?
                if (optionalFileStorageLocation.isPresent()) {
                    // Transfer existent archive to newly created zipped output stream
                    Optional<InputStream> optionalStream = getOptionalFileFromStorage(optionalFileStorageLocation.get());
                    if (optionalStream.isPresent()) {
                        InputStream in = null;
                        ZipArchiveInputStream zipIn = null;
                        try {
                            in = optionalStream.get();
                            zipIn = new ZipArchiveInputStream(in, "UTF8");
                            for (ZipArchiveEntry entry; (entry = zipIn.getNextZipEntry()) != null;) {
                                out.putArchiveEntry(entry);
                                IOUtils.copy(zipIn, out, BUFFER_SIZE);
                                out.closeArchiveEntry();
                                out.flush();
                            }
                        } finally {
                            Streams.close(zipIn, in);
                        }
                    }
                }
                zipOutReference.set(out);
                error = false; // Everything went fine
            } catch (IOException e) {
                throw DataExportExceptionCode.IO_ERROR.create(e, e.getMessage());
            } finally {
                if (error) {
                    Streams.close(out);
                }
            }
        }
        return out;
    }

    private Optional<InputStream> getOptionalFileFromStorage(String fileStorageLocation) throws OXException {
        try {
            return Optional.of(fileStorage.getFile(fileStorageLocation));
        } catch (OXException e) {
            if (FileStorageCodes.FILE_NOT_FOUND.equals(e)) {
                return Optional.empty();
            }
            throw e;
        }
    }

    /**
     * Marks this sink as canceled (if not done already).
     *
     * @return <code>true</code> if successfully marked as canceled; otherwise <code>false</code> if already marked as such
     */
    private boolean markAsCanceled() {
        return canceled.compareAndSet(false, true);
    }

    /**
     * Checks if any of the export() methods has been called so far.
     *
     * @return <code>true</code> if export was called; otherwise <code>false</code>
     */
    public boolean wasExportCalled() {
        return exportCalled.get();
    }

    @Override
    public synchronized String export(Directory directory) throws OXException {
        ZippedFileStorageOutputStream zipOutput = null;
        boolean success = false; // Pessimistic
        try {
            if (canceled.get()) {
                return null;
            }

            zipOutput = getZipOutputStream();
            String name = directory.getName();
            String pathPrefix = directory.getParentPath();
            if (pathPrefix.length() > 0 && pathPrefix.charAt(pathPrefix.length() - 1) != '/') {
                pathPrefix = new StringBuilder(pathPrefix).append('/').toString();
            }
            if (this.pathPrefix.length() > 0) {
                pathPrefix = this.pathPrefix + pathPrefix;
            }

            int num = 1;
            ZipArchiveEntry entry;
            String path;
            while (true) {
                try {
                    final String entryName;
                    {
                        final int pos = name.indexOf('.');
                        if (pos < 0) {
                            entryName = name + (num > 1 ? "_(" + num + ")" : "");
                        } else {
                            entryName = name.substring(0, pos) + (num > 1 ? "_(" + num + ")" : "") + name.substring(pos);
                        }
                    }

                    // Assumes the entry represents a directory if and only if the name ends with a forward slash "/".
                    path = pathPrefix + entryName + "/";
                    path = DataExportUtility.prepareEntryName(path, storageService.getConfig());
                    entry = new ZipArchiveEntry(path);
                    zipOutput.putArchiveEntry(entry);
                    break;
                } catch (java.util.zip.ZipException e) {
                    final String message = e.getMessage();
                    if (message == null || !message.startsWith("duplicate entry")) {
                        throw e;
                    }
                    num++;
                }
            }
            zipOutput.closeArchiveEntry();
            zipOutput.flush();

            exportCalled.set(true);
            success = true;
            if (this.pathPrefix.length() > 0) {
                path = path.substring(this.pathPrefix.length());
            }
            return path;
        } catch (IOException e) {
            throw DataExportExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw DataExportExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (Error e) {
            throw e;
        } finally {
            if (!success && zipOutput != null) {
                Optional<String> optionalLocation = zipOutput.getOptionalFileStorageLocation();
                if (optionalLocation.isPresent()) {
                    deleteQuietly(optionalLocation.get(), fileStorage);
                }
            }
        }
    }

    @Override
    public synchronized boolean export(InputStream in, Item item) throws OXException {
        ZippedFileStorageOutputStream zipOutput = null;
        boolean success = false; // Pessimistic
        InputStream data = in;
        try {
            if (canceled.get()) {
                return false;
            }

            zipOutput = getZipOutputStream();
            String name = item.getName();
            String pathPrefix = item.getPath() == null ? "" : item.getPath();
            if (pathPrefix.length() > 0 && pathPrefix.charAt(pathPrefix.length() - 1) != '/') {
                pathPrefix = new StringBuilder(pathPrefix).append('/').toString();
            }
            if (this.pathPrefix.length() > 0) {
                pathPrefix = this.pathPrefix + pathPrefix;
            }

            int num = 1;
            ZipArchiveEntry entry;
            while (true) {
                try {
                    final String entryName;
                    {
                        final int pos = name.indexOf('.');
                        if (pos < 0) {
                            entryName = name + (num > 1 ? "_(" + num + ")" : "");
                        } else {
                            entryName = name.substring(0, pos) + (num > 1 ? "_(" + num + ")" : "") + name.substring(pos);
                        }
                    }
                    entry = new ZipArchiveEntry(DataExportUtility.prepareEntryName(pathPrefix + entryName, storageService.getConfig()));
                    {
                        Optional<Date> date = item.getOptionalDate();
                        if (date.isPresent()) {
                            entry.setTime(date.get().getTime());
                        }
                    }
                    zipOutput.putArchiveEntry(entry);
                    break;
                } catch (java.util.zip.ZipException e) {
                    final String message = e.getMessage();
                    if (message == null || !message.startsWith("duplicate entry")) {
                        throw e;
                    }
                    num++;
                }
            }

            // Transfer bytes from the file to the ZIP file
            long size = 0;
            byte[] buf = new byte[BUFFER_SIZE];
            for (int read; (read = data.read(buf, 0, BUFFER_SIZE)) > 0;) {
                zipOutput.write(buf, 0, read);
                size += read;
            }
            entry.setSize(size);
            Streams.close(data);
            data = null;

            // Complete the entry
            zipOutput.closeArchiveEntry();
            zipOutput.flush();

            exportCalled.set(true);
            success = true;
            return true;
        } catch (IOException e) {
            throw DataExportExceptionCode.IO_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw DataExportExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (Error e) {
            throw e;
        } finally {
            Streams.close(data);
            if (!success && zipOutput != null) {
                Optional<String> optionalLocation = zipOutput.getOptionalFileStorageLocation();
                if (optionalLocation.isPresent()) {
                    deleteQuietly(optionalLocation.get(), fileStorage);
                }
            }
        }
    }

    @Override
    public boolean incrementFailCount() throws OXException {
        return storageService.incrementFailCount(task.getId(), moduleId, task.getUserId(), task.getContextId());
    }

    @Override
    public synchronized void setSavePoint(JSONObject jSavePoint) throws OXException {
        if (jSavePoint == null) {
            throw new IllegalArgumentException("Save-point must not be null");
        }

        // Complete currently "in progress" ZIP archive
        String currentFileStorageLocation = null;
        try {
            ZippedFileStorageOutputStream zipOutput = zipOutReference.getAndSet(null);
            if (zipOutput != null) {
                Streams.flush(zipOutput);
                Streams.close(zipOutput);

                // Acquire current file storage location
                currentFileStorageLocation = zipOutput.awaitFileStorageLocation();

                // Check whether previous file storage location needs to be dropped
                String existentFileStorageLocation = optExistentFileStorageLocation.getAndSet(null);
                if (existentFileStorageLocation != null) {
                    DataExportUtility.deleteQuietly(existentFileStorageLocation, fileStorage);
                }

                // Re-initialize stream
                optExistentFileStorageLocation.set(currentFileStorageLocation);
                getZipOutputStream();
            }

            // Write save-point to storage
            DataExportSavepoint savePoint = DataExportSavepoint.builder().withSavepoint(jSavePoint).withReport(optionalReport.orElse(null)).withFileStorageLocation(currentFileStorageLocation).build();
            storageService.setSavePoint(task.getId(), moduleId, savePoint, task.getUserId(), task.getContextId());

            // Avoid premature deletion
            currentFileStorageLocation = null;

            LOG.debug("Set save-point ``{}'' for \"{}\" data export {} of user {} in context {}", jSavePoint, moduleId, UUIDs.getUnformattedString(task.getId()), I(task.getUserId()), I(task.getContextId()));
        } finally {
            if (currentFileStorageLocation != null) {
                // Something went wrong
                DataExportUtility.deleteQuietly(currentFileStorageLocation, fileStorage);
            }
        }
    }

    @Override
    public synchronized Optional<String> finish() throws OXException {
        boolean canceled = markAsCanceled();
        if (!canceled) {
            // Already canceled
            return Optional.empty();
        }

        ZippedFileStorageOutputStream zipOutput = zipOutReference.getAndSet(null);
        if (zipOutput == null) {
            return Optional.empty();
        }

        // Complete the ZIP file
        Streams.flush(zipOutput);
        Streams.close(zipOutput);

        // Acquire new file storage location
        String newFileStorageLocation = zipOutput.awaitFileStorageLocation();

        // Check whether previous file storage location needs to be dropped
        String existentFileStorageLocation = optExistentFileStorageLocation.getAndSet(null);
        if (existentFileStorageLocation != null) {
            deleteQuietly(existentFileStorageLocation, fileStorage);
        }

        // Return new file storage location
        return Optional.ofNullable(newFileStorageLocation);
    }

    @Override
    public synchronized void revoke() throws OXException {
        boolean canceled = markAsCanceled();
        if (!canceled) {
            // Already canceled
            return;
        }

        ZippedFileStorageOutputStream zipOutput = zipOutReference.getAndSet(null);
        if (zipOutput != null) {
            // Complete the ZIP file
            Streams.close(zipOutput);

            String fileStorageLocation = zipOutput.awaitFileStorageLocation();
            deleteQuietly(fileStorageLocation, fileStorage);
        }
    }

    @Override
    public synchronized void addToReport(Message message) throws OXException {
        if (optionalReport.isPresent()) {
            DataExportDiagnosticsReport diagnosticsReport = optionalReport.get();
            if (diagnosticsReport.isConsiderPermissionDeniedErrors() || Message.Type.PERMISSION_DENIED != message.getType()) {
                diagnosticsReport.add(message);
            }
        }
    }

}
