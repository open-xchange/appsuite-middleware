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

package com.openexchange.gdpr.dataexport.impl.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.zip.Deflater;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.gdpr.dataexport.DataExportDiagnosticsReport;
import com.openexchange.gdpr.dataexport.DataExportStorageService;
import com.openexchange.gdpr.dataexport.DataExportTask;
import com.openexchange.gdpr.dataexport.Message;
import com.openexchange.gdpr.dataexport.impl.DataExportStrings;
import com.openexchange.gdpr.dataexport.impl.DataExportUtility;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Streams;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ChunkedZippedOutputStream} - Writes result file as a ZIP archive in demanded chunks to file storage
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class ChunkedZippedOutputStream {

    /**
     * The buffer size of 64K.
     */
    private static final int BUFFER_SIZE = DataExportUtility.BUFFER_SIZE;

    private final DataExportStorageService storageService;
    private final DataExportTask task;
    @SuppressWarnings("unused")
    private final ServiceLookup services; // Currently not used
    private final FileStorage fileStorage;
    private final long maxFileSize;

    private boolean cleanedUp;
    private int currentChunkNumber;
    private long currentSize;
    private ZippedFileStorageOutputStream zipOutputStream;

    /**
     * Initializes a new {@link ChunkedZippedOutputStream}.
     *
     * @param task The task to create results for
     * @param fileStorage The file storage to write to
     * @param storageService The storage to use
     * @param services The service look-up
     */
    public ChunkedZippedOutputStream(DataExportTask task, FileStorage fileStorage, DataExportStorageService storageService, ServiceLookup services) {
        super();
        this.task = task;
        this.maxFileSize = task.getArguments().getMaxFileSize();
        this.fileStorage = fileStorage;
        this.storageService = storageService;
        this.services = services;
        cleanedUp = false;
    }

    /**
     * Cleans-up this instance's results.
     *
     * @throws OXException If an Open-Xchange error occurs
     */
    public synchronized void cleanUp() throws OXException {
        storageService.deleteResultFiles(task.getId(), task.getUserId(), task.getContextId());
        cleanedUp = true;
    }

    /**
     * Closes this instance & returns collected file storage locations.
     *
     * @throws IOException If an I/O error occurs
     * @throws OXException If an Open-Xchange error occurs
     */
    public synchronized void close() throws IOException, OXException {
        if (cleanedUp) {
            return;
        }

        if (zipOutputStream != null) {
            closeStream();
        }
    }

    /**
     * Adds the diagnostics report to this instance.
     *
     * @param report The messages of the diagnostics report
     * @param locale The locale
     * @throws IOException If an I/O error occurs
     */
    public synchronized void addDiagnostics(DataExportDiagnosticsReport report, Locale locale) throws IOException {
        if (report == null || report.isEmpty()) {
            return;
        }

        if (cleanedUp) {
            throw new IllegalStateException("Already cleaned-up");
        }

        if (zipOutputStream == null) {
            // Not yet initialized
            constructNewStream();
        }

        // Create archive entry
        StringHelper stringHelper = StringHelper.valueOf(locale);
        ZipArchiveEntry entry = new ZipArchiveEntry(DataExportUtility.prepareEntryName(stringHelper.getString(DataExportStrings.DIAGNOSTICS_FILE_PREFIX) + ".txt", storageService.getConfig()));
        zipOutputStream.putArchiveEntry(entry);

        StringBuilder messageBuilder = new StringBuilder(128);
        byte[] delimiter = "\r\n\r\n---------------------------------------\r\n\r\n".getBytes(StandardCharsets.UTF_8);
        long size = 0;
        Iterator<Message> it = report.iterator();

        byte[] msg = formatMessage(it.next(), messageBuilder, locale).getBytes(StandardCharsets.UTF_8);
        zipOutputStream.write(msg);
        size += msg.length;
        while (it.hasNext()) {
            zipOutputStream.write(delimiter);
            msg = formatMessage(it.next(), messageBuilder, locale).getBytes(StandardCharsets.UTF_8);
            zipOutputStream.write(msg);
            size += msg.length;
        }
        entry.setSize(size);

        // Complete the entry
        zipOutputStream.closeArchiveEntry();
        zipOutputStream.flush();
    }

    /**
     * Formats given message ready for being added to report.
     *
     * @param message The message
     * @param messageBuilder The string builder
     * @param locale The locale
     * @return The formatted string
     */
    private String formatMessage(Message message, StringBuilder messageBuilder, Locale locale) {
        messageBuilder.setLength(0);

        // Header
        messageBuilder.append(DateFormat.getDateInstance(DateFormat.LONG, locale).format(message.getTimeStamp()));
        messageBuilder.append(" at ");
        messageBuilder.append(DateFormat.getTimeInstance(DateFormat.SHORT, locale).format(message.getTimeStamp()));
        messageBuilder.append(" - ");
        messageBuilder.append(message.getModuleId());
        messageBuilder.append("\r\n");

        // Message
        messageBuilder.append(message.getMessage());
        messageBuilder.append("\r\n");

        return messageBuilder.toString();
    }

    /**
     * Adds the ZIP entries from specified ZIP input stream to this instance.
     *
     * @param zipIn The ZIP input stream to get ZIP entries from
     * @throws IOException If an I/O error occurs
     * @throws OXException If an Open-Xchange error occurs
     */
    public synchronized void addEntriesFrom(ZipArchiveInputStream zipIn) throws IOException, OXException {
        if (zipIn == null) {
            return;
        }

        if (cleanedUp) {
            throw new IllegalStateException("Already cleaned-up");
        }

        ThresholdFileHolder tmp = null;
        try {
            for (ZipArchiveEntry entry; (entry = zipIn.getNextZipEntry()) != null;) {
                long entrySize = entry.getCompressedSize();
                boolean useTmp = false;
                if (entrySize < 0) {
                    // Size unknown
                    tmp = createOrReset(tmp);
                    IOUtils.copy(zipIn, tmp.asOutputStream(), BUFFER_SIZE);
                    useTmp = true;
                    entrySize = tmp.getLength();
                }

                if (zipOutputStream == null) {
                    // Not yet initialized
                    constructNewStream();
                } else {
                    if ((maxFileSize > 0) && ((currentSize + entrySize) > maxFileSize)) {
                        // Max. file size would be exceeded
                        closeStream();
                        constructNewStream();
                    }
                }

                currentSize += entrySize;
                zipOutputStream.putArchiveEntry(entry);
                IOUtils.copy(useTmp && tmp != null ? tmp.getStream() : zipIn, zipOutputStream, BUFFER_SIZE);
                zipOutputStream.closeArchiveEntry();
                zipOutputStream.flush();
            }
        } finally {
            Streams.close(tmp);
        }
    }

    private static ThresholdFileHolder createOrReset(ThresholdFileHolder fileHolder) {
        if (fileHolder == null) {
            return new ThresholdFileHolder(false);
        }

        fileHolder.reset();
        return fileHolder;
    }

    private void closeStream() throws IOException, OXException {
        zipOutputStream.flush();
        zipOutputStream.finish();
        zipOutputStream.close();

        String fileStorageLocation = zipOutputStream.awaitFileStorageLocation();
        storageService.addResultFile(fileStorageLocation, ++currentChunkNumber, currentSize, task.getId(), task.getUserId(), task.getContextId());
    }

    private void constructNewStream() {
        zipOutputStream = ZippedFileStorageOutputStream.createDefaultZippedFileStorageOutputStream(fileStorage, Deflater.DEFAULT_COMPRESSION);
        currentSize = 0;
    }

}
