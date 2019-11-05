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

package com.openexchange.gdpr.dataexport.impl.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
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
        zipOut.addRawArchiveEntry(entry, rawStream);
    }

    @Override
    public void putArchiveEntry(ArchiveEntry archiveEntry) throws IOException {
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
