
package com.openexchange.gdpr.dataexport.impl.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream.UnicodeExtraFieldPolicy;
import com.openexchange.filestore.FileStorage;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ZippedFileStorageOutputStream} - The abstract super class for zipped output streams into file storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public abstract class ZippedFileStorageOutputStream extends OutputStream {

    /**
     * Creates a new zipped output stream writing to given file storage.
     *
     * @param piped Whether to use a piped mechanism to transport bytes into file storage
     * @param fileStorage The file storage to write to
     * @param compressionLevel The compression level to use (default is {@link java.util.zip.Deflater#DEFAULT_COMPRESSION DEFAULT_COMPRESSION})
     * @param services The service look-up
     * @return The zipped output stream
     * @throws IOException If initialization fails
     */
    public static ZippedFileStorageOutputStream createDefaultZippedFileStorageOutputStream(FileStorage fileStorage, int compressionLevel) throws IOException {
        return createZippedFileStorageOutputStream(false, fileStorage, compressionLevel, AppendingFileStorageOutputStream.DEFAULT_IN_MEMORY_THRESHOLD, null);
    }

    /**
     * Creates a new zipped output stream writing to given file storage.
     *
     * @param piped Whether to use a piped mechanism to transport bytes into file storage
     * @param fileStorage The file storage to write to
     * @param compressionLevel The compression level to use (default is {@link java.util.zip.Deflater#DEFAULT_COMPRESSION DEFAULT_COMPRESSION})
     * @param bufferSize The size for the in-memory buffer; if that buffer size is exceeded data will be flushed into file storage
     *                   (default is {@link AppendingFileStorageOutputStream#DEFAULT_IN_MEMORY_THRESHOLD})
     * @param services The service look-up
     * @return The zipped output stream
     * @throws IOException If initialization fails
     */
    public static ZippedFileStorageOutputStream createZippedFileStorageOutputStream(boolean piped, FileStorage fileStorage, int compressionLevel, int bufferSize, ServiceLookup services) throws IOException {
        return piped ? new PipedZippedFileStorageOutputStream(fileStorage, compressionLevel, services) : new AppendingZippedFileStorageOutputStream(fileStorage, compressionLevel, bufferSize);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link ZippedFileStorageOutputStream}.
     */
    protected ZippedFileStorageOutputStream() {
        super();
    }

    // --------------------------------------------- Init streams---------------------------------------------------------------------------

    /**
     * Initializes an {@link ZipArchiveOutputStream Apache zipped output stream} using given output stream and compression level.
     *
     * @param out The output stream to zip to
     * @param compressionLevel The compression level to use (default is {@link java.util.zip.Deflater#DEFAULT_COMPRESSION DEFAULT_COMPRESSION})
     * @return
     */
    protected ZipArchiveOutputStream initZipArchiveOutputStream(OutputStream out, int compressionLevel) {
        ZipArchiveOutputStream zipOutput = new ZipArchiveOutputStream(out);
        zipOutput.setEncoding("UTF-8");
        zipOutput.setUseLanguageEncodingFlag(true);
        zipOutput.setLevel(compressionLevel);
        return zipOutput;
    }

    // --------------------------------------------- Public methods-------------------------------------------------------------------------

    /**
     * Gets the optional file storage location.
     *
     * @return The optional file storage location
     */
    public abstract Optional<String> getOptionalFileStorageLocation();

    /**
     * Gets the file storage location (uninterruptibly).
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * Note: Only call this method when this output stream has been finished/closed.
     * </div>
     *
     * @return The file storage location
     */
    public abstract String awaitFileStorageLocation();

    /**
     * Returns the current number of bytes written to this stream.
     *
     * @return the number of written bytes
     */
    public abstract long getBytesWritten();

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
    public abstract boolean isSeekable();

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
    public abstract void setEncoding(String encoding);

    /**
     * The encoding to use for filenames and the file comment.
     *
     * @return <code>null</code> if using the platform's default character encoding.
     */
    public abstract String getEncoding();

    /**
     * Whether to set the language encoding flag if the file name
     * encoding is UTF-8.
     *
     * <p>Defaults to true.</p>
     *
     * @param b whether to set the language encoding flag if the file
     *            name encoding is UTF-8
     */
    public abstract void setUseLanguageEncodingFlag(boolean b);

    /**
     * Whether to create Unicode Extra Fields.
     *
     * <p>Defaults to NEVER.</p>
     *
     * @param b whether to create Unicode Extra Fields.
     */
    public abstract void setCreateUnicodeExtraFields(UnicodeExtraFieldPolicy b);

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
    public abstract void setFallbackToUTF8(boolean b);

    /**
     * Finishes the addition of entries to this stream, without closing it.
     * Additional data can be written, if the format supports it.
     *
     * @throws IOException if the user forgets to close the entry.
     */
    public abstract void finish() throws IOException;

    /**
     * Closes the archive entry, writing any trailer information that may
     * be required.
     *
     * @throws IOException if an I/O error occurs
     */
    public abstract void closeArchiveEntry() throws IOException;

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
    public abstract void addRawArchiveEntry(ZipArchiveEntry entry, InputStream rawStream) throws IOException;

    /**
     * Writes the headers for an archive entry to the output stream.
     * The caller must then write the content to the stream and call
     * {@link #closeArchiveEntry()} to complete the process.
     *
     * @param entry describes the entry
     * @throws IOException if an I/O error occurs
     */
    public abstract void putArchiveEntry(ArchiveEntry archiveEntry) throws IOException;

    /**
     * Set the file comment.
     *
     * @param comment the comment
     */
    public abstract void setComment(String comment);

    /**
     * Sets the compression level for subsequent entries.
     *
     * <p>Default is Deflater.DEFAULT_COMPRESSION.</p>
     *
     * @param level the compression level.
     * @throws IllegalArgumentException if an invalid compression
     *             level is specified.
     */
    public abstract void setLevel(int level);

    /**
     * Sets the default compression method for subsequent entries.
     *
     * <p>Default is DEFLATED.</p>
     *
     * @param method an <code>int</code> from java.util.zip.ZipEntry
     */
    public abstract void setMethod(int method);

    /**
     * Whether this stream is able to write the given entry.
     *
     * <p>May return false if it is set up to use encryption or a
     * compression method that hasn't been implemented yet.</p>
     */
    public abstract boolean canWriteEntryData(ArchiveEntry ae);

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
    public abstract ArchiveEntry createArchiveEntry(File inputFile, String entryName) throws IOException;

}
