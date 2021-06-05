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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream.UnicodeExtraFieldPolicy;
import com.openexchange.filestore.FileStorage;

/**
 * {@link AppendingZippedFileStorageOutputStream} - The zipped output stream that appends data to a file storage location successively.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class AppendingZippedFileStorageOutputStream extends ZippedFileStorageOutputStream {

    private final Set<String> archiveEntryNames;
    private final AppendingFileStorageOutputStream out;
    private final ZipArchiveOutputStream zipOut;

    /**
     * Initializes a new {@link AppendingZippedFileStorageOutputStream}.
     *
     * @param fileStorage The file storage to write to
     * @param compressionLevel The compression level to use (default is {@link java.util.zip.Deflater#DEFAULT_COMPRESSION DEFAULT_COMPRESSION})
     * @param bufferSize The size for the in-memory buffer; if that buffer size is exceeded data will be flushed into file storage
     *                   (default is {@link AppendingFileStorageOutputStream#DEFAULT_IN_MEMORY_THRESHOLD})
     */
    AppendingZippedFileStorageOutputStream(FileStorage fileStorage, int compressionLevel, int bufferSize) {
        super();
        out = new AppendingFileStorageOutputStream(bufferSize, fileStorage);
        zipOut = initZipArchiveOutputStream(out, compressionLevel);
        archiveEntryNames = new HashSet<String>();
    }

    @Override
    public Optional<String> getOptionalFileStorageLocation() {
        return out.getFileStorageLocation();
    }

    @Override
    public String awaitFileStorageLocation() {
        return out.getFileStorageLocation().orElse(null);
    }

    @Override
    public long getBytesWritten() {
        return zipOut.getBytesWritten();
    }

    @Override
    public boolean isSeekable() {
        return zipOut.isSeekable();
    }

    @Override
    public void setEncoding(String encoding) {
        zipOut.setEncoding(encoding);
    }

    @Override
    public String getEncoding() {
        return zipOut.getEncoding();
    }

    @Override
    public void setUseLanguageEncodingFlag(boolean b) {
        zipOut.setUseLanguageEncodingFlag(b);
    }

    @Override
    public void setCreateUnicodeExtraFields(UnicodeExtraFieldPolicy b) {
        zipOut.setCreateUnicodeExtraFields(b);
    }

    @Override
    public void setFallbackToUTF8(boolean b) {
        zipOut.setFallbackToUTF8(b);
    }

    @Override
    public void finish() throws IOException {
        zipOut.finish();
        out.flush();
    }

    @Override
    public void closeArchiveEntry() throws IOException {
        zipOut.closeArchiveEntry();
    }

    @Override
    public void addRawArchiveEntry(ZipArchiveEntry entry, InputStream rawStream) throws IOException {
        if (false == archiveEntryNames.add(entry.getName())) {
            throw new java.util.zip.ZipException("duplicate entry");
        }
        zipOut.addRawArchiveEntry(entry, rawStream);
    }

    @Override
    public void putArchiveEntry(ArchiveEntry archiveEntry) throws IOException {
        if (false == archiveEntryNames.add(archiveEntry.getName())) {
            throw new java.util.zip.ZipException("duplicate entry");
        }
        zipOut.putArchiveEntry(archiveEntry);
    }

    @Override
    public void setComment(String comment) {
        zipOut.setComment(comment);
    }

    @Override
    public void setLevel(int level) {
        zipOut.setLevel(level);
    }

    @Override
    public void setMethod(int method) {
        zipOut.setMethod(method);
    }

    @Override
    public boolean canWriteEntryData(ArchiveEntry ae) {
        return zipOut.canWriteEntryData(ae);
    }

    @Override
    public ArchiveEntry createArchiveEntry(File inputFile, String entryName) throws IOException {
        return zipOut.createArchiveEntry(inputFile, entryName);
    }

    @Override
    public void write(byte[] b) throws IOException {
        zipOut.write(b);
    }

    @Override
    public void write(int b) throws IOException {
        zipOut.write(b);
    }

    @Override
    public void close() throws IOException {
        zipOut.flush();
        zipOut.close();
        out.flush();
        out.close();
        super.close();
    }

    @Override
    public void flush() throws IOException {
        super.flush();
    }

}
