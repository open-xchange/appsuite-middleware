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

package com.openexchange.ajax.zip;

import static com.openexchange.ajax.AJAXServlet.CONTENTTYPE_JSON;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.helper.DownloadUtility;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.ExceptionUtils;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link ZipUtility} - A utility class for outputting a ZIP archive.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class ZipUtility {

    /**
     * Initializes a new {@link ZipUtility}.
     */
    private ZipUtility() {
        super();
    }

    /**
     * Creates a ZIP archive using given <code>ZipEntryAdder</code> instance.
     *
     * @param adder Used for adding ZIP entries
     * @param zipFileName The file name of the ZIP archive
     * @param optCompressionLevel The optional compression level
     * @param requestData The AJAX request data
     * @return The appropriate result for the ZIP archive
     * @throws OXException If creating the ZIP archive fails
     */
    public static AJAXRequestResult createZipArchive(ZipEntryAdder adder, String zipFileName, int optCompressionLevel, AJAXRequestData requestData) throws OXException {
        // Check if it is possible to directly write to output stream
        if (setHttpResponseHeaders(zipFileName, requestData)) {
            try {
                // Create ZIP archive
                long bytesWritten = writeZipArchive(adder, new AJAXRequestDataZipArchiveOutputStreamProvider(requestData, optCompressionLevel));

                // Streamed
                AJAXRequestResult result = new AJAXRequestResult(AJAXRequestResult.DIRECT_OBJECT, "direct").setType(AJAXRequestResult.ResultType.DIRECT);
                if (bytesWritten != 0) {
                    result.setResponseProperty("X-Content-Size", Long.valueOf(bytesWritten));
                }
                return result;
            } catch (Exception e) {
                return rethrowOrsignalIOError(e, requestData);
            }
        }

        ThresholdFileHolder fileHolder = prepareThresholdFileHolder(zipFileName);
        try {
            // Create ZIP archive
            writeZipArchive(adder, fileHolder.asOutputStream(), optCompressionLevel);

            requestData.setFormat("file");
            AJAXRequestResult requestResult = new AJAXRequestResult(fileHolder, "file");
            fileHolder = null;
            return requestResult;
        } finally {
            Streams.close(fileHolder);
        }
    }

    private static AJAXRequestResult rethrowOrsignalIOError(Exception e, AJAXRequestData requestData) throws OXException {
        if (!requestData.isResponseCommitted()) {
            // Exception can be safely thrown to be orderly managed in dispatcher framework since HTTP response is not yet committed
            requestData.setResponseHeader("Content-Type", CONTENTTYPE_JSON);
            requestData.setResponseHeader("Content-Disposition", "inline");
            throw e instanceof OXException ? (OXException) e : OXException.general("Unexpected error while writing ZIP archive", e);
        }

        // HTTP response already committed. Artificially advertise I/O error to signal error to client.
        IOException ioe = ExceptionUtils.extractFrom(e, IOException.class);
        if (null == ioe) {
            ioe = new IOException(e.getMessage(), e);
        }
        return new AJAXRequestResult(ioe, "direct").setType(AJAXRequestResult.ResultType.DIRECT);
    }

    /**
     * Sets the HTTP response headers for the ZIP archive. Implicitly checks if direct output via given <code>AJAXRequestData</code>
     * instance is possible.
     *
     * @param zipFileName The file name of the ZIP archive
     * @param requestData The AJAX request data
     * @return <code>true</code> if headers are successfully written (and thus direct output is possible); otherwise <code>false</code>
     */
    public static boolean setHttpResponseHeaders(String zipFileName, AJAXRequestData requestData) {
        // Try to set Content-Type response header
        if (!requestData.setResponseHeader("Content-Type", "application/zip")) {
            return false;
        }

        // Set Content-Disposition response header
        final StringBuilder sb = new StringBuilder(512);
        sb.append("attachment");
        DownloadUtility.appendFilenameParameter(zipFileName, "application/zip", requestData.getUserAgent(), sb);
        requestData.setResponseHeader("Content-Disposition", sb.toString());
        return true;
    }

    /**
     * Creates an appropriate instance of <code>ThresholdFileHolder</code>, which carries the ZIP archive's data.
     *
     * @param zipFileName The file name of the ZIP archive
     * @return The prepared <code>ThresholdFileHolder</code> instance
     */
    public static ThresholdFileHolder prepareThresholdFileHolder(String zipFileName) {
        ThresholdFileHolder fileHolder = new ThresholdFileHolder();
        fileHolder.setDisposition("attachment");
        fileHolder.setName(zipFileName);
        fileHolder.setContentType("application/zip");
        fileHolder.setDelivery("download");
        return fileHolder;
    }

    /**
     * Writes the ZIP archive to given output stream using specified <code>ZipEntryAdder</code> instance.
     *
     * @param adder Used for adding ZIP entries
     * @param out The output stream to write to
     * @param optCompressionLevel The optional compression level
     * @return The number of bytes written to the ZIP archive
     * @throws OXException If writing the ZIP archive fails
     */
    public static long writeZipArchive(ZipEntryAdder adder, OutputStream out, int optCompressionLevel) throws OXException {
        return writeZipArchive(adder, new DefaultZipArchiveOutputStreamProvider(out, optCompressionLevel));
    }

    private static long writeZipArchive(ZipEntryAdder adder, InternalZipArchiveOutputStreamProvider zipOutProvider) throws OXException {
        try {
            // The buffer to use
            Buffer buffer = new Buffer();

            // The map for used names
            Map<String, Integer> fileNamesInArchive = new HashMap<String, Integer>();

            // Add ZIP entries
            adder.addZipEntries(zipOutProvider, buffer, fileNamesInArchive);

            // Return number of written bytes
            ZipArchiveOutputStream zipOutput = zipOutProvider.optZipArchiveOutputStream();
            return zipOutput == null ? 0L : zipOutput.getBytesWritten();
        } finally {
            // Complete the ZIP file
            Streams.close(zipOutProvider.optZipArchiveOutputStream());
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static abstract class InternalZipArchiveOutputStreamProvider implements ZipArchiveOutputStreamProvider {

        protected ZipArchiveOutputStream zipOutput;
        protected final int optCompressionLevel;

        protected InternalZipArchiveOutputStreamProvider(int optCompressionLevel) {
            super();
            this.optCompressionLevel = optCompressionLevel;
        }

        /**
         * Gets the raw reference to the ZIP archive's output stream.
         *
         * @return The ZIP archive's output stream or <code>null</code>
         */
        abstract ZipArchiveOutputStream optZipArchiveOutputStream();
    }

    private static class DefaultZipArchiveOutputStreamProvider extends InternalZipArchiveOutputStreamProvider {

        private final OutputStream out;

        DefaultZipArchiveOutputStreamProvider(OutputStream out, int optCompressionLevel) {
            super(optCompressionLevel);
            this.out = out;
        }

        @Override
        public ZipArchiveOutputStream getZipArchiveOutputStream() throws OXException {
            ZipArchiveOutputStream zipOutput = this.zipOutput;
            if (null == zipOutput) {
                // Initialize ZIP output stream
                zipOutput = createZipArchiveOutputStream(out, optCompressionLevel);
                this.zipOutput = zipOutput;
            }
            return zipOutput;
        }

        @Override
        ZipArchiveOutputStream optZipArchiveOutputStream() {
            return zipOutput;
        }
    }

    private static class AJAXRequestDataZipArchiveOutputStreamProvider extends InternalZipArchiveOutputStreamProvider {

        private final AJAXRequestData requestData;

        AJAXRequestDataZipArchiveOutputStreamProvider(AJAXRequestData requestData, int optCompressionLevel) {
            super(optCompressionLevel);
            this.requestData = requestData;
        }

        @Override
        public ZipArchiveOutputStream getZipArchiveOutputStream() throws OXException {
            ZipArchiveOutputStream zipOutput = this.zipOutput;
            if (null == zipOutput) {
                // Initialize ZIP output stream
                try {
                    zipOutput = createZipArchiveOutputStream(requestData.optOutputStream(), optCompressionLevel);
                    this.zipOutput = zipOutput;
                } catch (IOException e) {
                    throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
                }
            }
            return zipOutput;
        }

        @Override
        ZipArchiveOutputStream optZipArchiveOutputStream() {
            return zipOutput;
        }
    }

    static ZipArchiveOutputStream createZipArchiveOutputStream(OutputStream out, int optCompressionLevel) {
        ZipArchiveOutputStream zipOutput;
        zipOutput = new ZipArchiveOutputStream(out);
        zipOutput.setEncoding("UTF8");
        zipOutput.setUseLanguageEncodingFlag(true);
        if (optCompressionLevel > 0) {
            zipOutput.setLevel(optCompressionLevel);
        }
        return zipOutput;
    }

}
