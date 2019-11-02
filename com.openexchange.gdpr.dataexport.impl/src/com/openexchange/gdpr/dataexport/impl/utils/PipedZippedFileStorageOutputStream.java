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

package com.openexchange.gdpr.dataexport.impl.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream.UnicodeExtraFieldPolicy;
import org.slf4j.Logger;
import com.openexchange.filestore.FileStorage;
import com.openexchange.gdpr.dataexport.impl.DataExportUtility;
import com.openexchange.java.BlockingAtomicReference;
import com.openexchange.java.ExceptionAwarePipedInputStream;
import com.openexchange.java.ExceptionForwardingPipedOutputStream;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadRenamer;

/**
 * {@link PipedZippedFileStorageOutputStream} - An output stream that writes a ZIP archive to a file storage location.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class PipedZippedFileStorageOutputStream extends ZippedFileStorageOutputStream {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(PipedZippedFileStorageOutputStream.class);
    }

    private final ServiceLookup services;
    private final BlockingAtomicReference<String> fileStorageLocationReference;
    private final ExceptionForwardingPipedOutputStream out;
    private final ZipArchiveOutputStream zipOut;

    /**
     * Initializes a new {@link PipedZippedFileStorageOutputStream}.
     *
     * @param fileStorage The file storage to write to
     * @param compressionLevel The compression level to use (default is {@link java.util.zip.Deflater#DEFAULT_COMPRESSION DEFAULT_COMPRESSION})
     * @param services The service look-up
     * @throws IOException If initialization fails
     */
    PipedZippedFileStorageOutputStream(FileStorage fileStorage, int compressionLevel, ServiceLookup services) throws IOException {
        super(fileStorage);
        this.services = services;
        fileStorageLocationReference = new BlockingAtomicReference<String>();
        out = initOutputStream();
        zipOut = initZipArchiveOutputStream(out, compressionLevel);
    }

    /**
     * Gets the optional file storage location.
     *
     * @return The optional file storage location
     */
    @Override
    public Optional<String> getOptionalFileStorageLocation() {
        return Optional.ofNullable(fileStorageLocationReference.peek());
    }

    /**
     * Gets the file storage location (uninterruptibly).
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * Note: Only call this method when this output stream has been finished/closed.
     * </div>
     *
     * @return The file storage location
     */
    @Override
    public String awaitFileStorageLocation() {
        return fileStorageLocationReference.getUninterruptibly();
    }

    @Override
    public void write(byte[] b) throws IOException {
        try {
            zipOut.write(b);
        } catch (IOException e) {
            out.forwardException(e);
            throw e;
        }
    }

    @Override
    public void write(int b) throws IOException {
        try {
            zipOut.write(b);
        } catch (IOException e) {
            out.forwardException(e);
            throw e;
        }
    }

    /**
     * Returns the current number of bytes written to this stream.
     *
     * @return the number of written bytes
     */
    @Override
    public long getBytesWritten() {
        return zipOut.getBytesWritten();
    }

    /**
     * This method indicates whether this archive is writing to a
     * seekable stream (i.e., to a random access file).
     *
     * <p>For seekable streams, you don't need to calculate the CRC or
     * uncompressed size for {@link #STORED} entries before
     * invoking {@link #putArchiveEntry(ArchiveEntry)}.
     *
     * @return true if seekable
     */
    @Override
    public boolean isSeekable() {
        return zipOut.isSeekable();
    }

    /**
     * The encoding to use for filenames and the file comment.
     *
     * <p>For a list of possible values see <a
     * href="http://java.sun.com/j2se/1.5.0/docs/guide/intl/encoding.doc.html">http://java.sun.com/j2se/1.5.0/docs/guide/intl/encoding.doc.html</a>.
     * Defaults to UTF-8.</p>
     *
     * @param encoding the encoding to use for file names, use null
     *            for the platform's default encoding
     */
    @Override
    public void setEncoding(String encoding) {
        zipOut.setEncoding(encoding);
    }

    /**
     * The encoding to use for filenames and the file comment.
     *
     * @return <code>null</code> if using the platform's default character encoding.
     */
    @Override
    public String getEncoding() {
        return zipOut.getEncoding();
    }

    /**
     * Whether to set the language encoding flag if the file name
     * encoding is UTF-8.
     *
     * <p>Defaults to true.</p>
     *
     * @param b whether to set the language encoding flag if the file
     *            name encoding is UTF-8
     */
    @Override
    public void setUseLanguageEncodingFlag(boolean b) {
        zipOut.setUseLanguageEncodingFlag(b);
    }

    /**
     * Whether to create Unicode Extra Fields.
     *
     * <p>Defaults to NEVER.</p>
     *
     * @param b whether to create Unicode Extra Fields.
     */
    @Override
    public void setCreateUnicodeExtraFields(UnicodeExtraFieldPolicy b) {
        zipOut.setCreateUnicodeExtraFields(b);
    }

    /**
     * Whether to fall back to UTF and the language encoding flag if
     * the file name cannot be encoded using the specified encoding.
     *
     * <p>Defaults to false.</p>
     *
     * @param b whether to fall back to UTF and the language encoding
     *            flag if the file name cannot be encoded using the specified
     *            encoding.
     */
    @Override
    public void setFallbackToUTF8(boolean b) {
        zipOut.setFallbackToUTF8(b);
    }

    /**
     * Finishes the addition of entries to this stream, without closing it.
     * Additional data can be written, if the format supports it.
     *
     * @throws IOException if the user forgets to close the entry.
     */
    @Override
    public void finish() throws IOException {
        try {
            zipOut.finish();
        } catch (IOException e) {
            out.forwardException(e);
            throw e;
        }
    }

    /**
     * Closes the archive entry, writing any trailer information that may
     * be required.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void closeArchiveEntry() throws IOException {
        try {
            zipOut.closeArchiveEntry();
        } catch (IOException e) {
            out.forwardException(e);
            throw e;
        }
    }

    /**
     * Adds an archive entry with a raw input stream.
     *
     * If crc, size and compressed size are supplied on the entry, these values will be used as-is.
     * Zip64 status is re-established based on the settings in this stream, and the supplied value
     * is ignored.
     *
     * The entry is put and closed immediately.
     *
     * @param entry The archive entry to add
     * @param rawStream The raw input stream of a different entry. May be compressed/encrypted.
     * @throws IOException If copying fails
     */
    @Override
    public void addRawArchiveEntry(ZipArchiveEntry entry, InputStream rawStream) throws IOException {
        try {
            zipOut.addRawArchiveEntry(entry, rawStream);
        } catch (IOException e) {
            out.forwardException(e);
            throw e;
        }
    }

    /**
     * Writes the headers for an archive entry to the output stream.
     * The caller must then write the content to the stream and call
     * {@link #closeArchiveEntry()} to complete the process.
     *
     * @param entry describes the entry
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void putArchiveEntry(ArchiveEntry archiveEntry) throws IOException {
        try {
            zipOut.putArchiveEntry(archiveEntry);
        } catch (java.util.zip.ZipException e) {
            String message = e.getMessage();
            if (message == null || !message.startsWith("duplicate entry")) {
                out.forwardException(e);
            }
            throw e;
        } catch (IOException e) {
            out.forwardException(e);
            throw e;
        }
    }

    /**
     * Set the file comment.
     *
     * @param comment the comment
     */
    @Override
    public void setComment(String comment) {
        zipOut.setComment(comment);
    }

    /**
     * Sets the compression level for subsequent entries.
     *
     * <p>Default is Deflater.DEFAULT_COMPRESSION.</p>
     *
     * @param level the compression level.
     * @throws IllegalArgumentException if an invalid compression
     *             level is specified.
     */
    @Override
    public void setLevel(int level) {
        zipOut.setLevel(level);
    }

    /**
     * Sets the default compression method for subsequent entries.
     *
     * <p>Default is DEFLATED.</p>
     *
     * @param method an <code>int</code> from java.util.zip.ZipEntry
     */
    @Override
    public void setMethod(int method) {
        zipOut.setMethod(method);
    }

    /**
     * Whether this stream is able to write the given entry.
     *
     * <p>May return false if it is set up to use encryption or a
     * compression method that hasn't been implemented yet.</p>
     */
    @Override
    public boolean canWriteEntryData(ArchiveEntry ae) {
        return zipOut.canWriteEntryData(ae);
    }

    @Override
    public void write(byte[] b, int offset, int length) throws IOException {
        zipOut.write(b, offset, length);
    }

    @Override
    public void close() throws IOException {
        zipOut.close();
    }

    @Override
    public void flush() throws IOException {
        zipOut.flush();
    }

    /**
     * Creates a new zip entry taking some information from the given
     * file and using the provided name.
     *
     * <p>The name will be adjusted to end with a forward slash "/" if
     * the file is a directory.  If the file is not a directory a
     * potential trailing forward slash will be stripped from the
     * entry name.</p>
     *
     * <p>Must not be used if the stream has already been closed.</p>
     */
    @Override
    public ArchiveEntry createArchiveEntry(File inputFile, String entryName) throws IOException {
        try {
            return zipOut.createArchiveEntry(inputFile, entryName);
        } catch (IOException e) {
            out.forwardException(e);
            throw e;
        }
    }

    // --------------------------------------------- Init streams---------------------------------------------------------------------------

    private ExceptionForwardingPipedOutputStream initOutputStream() throws IOException {
        // Initialize pipes
        ExceptionAwarePipedInputStream in = new ExceptionAwarePipedInputStream(DataExportUtility.BUFFER_SIZE);
        ExceptionForwardingPipedOutputStream out = new ExceptionForwardingPipedOutputStream(in);

        // Create writer task
        FileStorage fileStorage= this.fileStorage;
        BlockingAtomicReference<String> fileStorageLocationReference = this.fileStorageLocationReference;
        AbstractTask<Void> fileStorageWriter = new DataExportFileStorageWriterTask(in, out, fileStorage, fileStorageLocationReference);

        // Submit/execute writer task
        ThreadPoolService threadPool = services.getOptionalService(ThreadPoolService.class);
        if (threadPool == null) {
            throw new IOException(ServiceExceptionCode.absentService(ThreadPoolService.class));
        }
        threadPool.submit(fileStorageWriter);

        // Return piped output stream, which is consumed by DataExportFileStorageWriterTask instance
        return out;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * {@link DataExportFileStorageWriterTask} - Writes the bytes received from given piped input stream into specified file storage.
     *
     * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
     * @since v7.10.3
     */
    private static final class DataExportFileStorageWriterTask extends AbstractTask<Void> {

        private final ExceptionAwarePipedInputStream in;
        private final ExceptionForwardingPipedOutputStream out;
        private final BlockingAtomicReference<String> fileStorageLocationReference;
        private final FileStorage fileStorage;

        /**
         * Initializes a new {@link DataExportFileStorageWriterTask}.
         *
         * @param in The piped input stream to read from
         * @param out The piped output stream feeding the bytes to piped input stream
         * @param fileStorage The file storage to write to
         * @param fileStorageLocationReference The reference for the resulting file storage location when writing to storage is completed
         */
        DataExportFileStorageWriterTask(ExceptionAwarePipedInputStream in, ExceptionForwardingPipedOutputStream out, FileStorage fileStorage, BlockingAtomicReference<String> fileStorageLocationReference) {
            super();
            this.in = in;
            this.out = out;
            this.fileStorageLocationReference = fileStorageLocationReference;
            this.fileStorage = fileStorage;
        }

        @Override
        public void setThreadName(ThreadRenamer threadRenamer) {
            threadRenamer.renamePrefix(DataExportFileStorageWriterTask.class.getSimpleName());
        }

        @Override
        public Void call() {
            try {
                String fileStorageLocation = fileStorage.saveNewFile(in);
                fileStorageLocationReference.set(fileStorageLocation);
            } catch (Exception e) {
                LoggerHolder.LOG.warn("Failed writing into file storage sink", e);
                out.setException(e);
            }
            return null;
        }
    }

}
