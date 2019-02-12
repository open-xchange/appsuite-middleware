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

package com.openexchange.ajax.zip;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.helper.DownloadUtility;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
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
                long bytesWritten = writeZipArchive(adder, requestData.optOutputStream(), optCompressionLevel);

                // Streamed
                AJAXRequestResult result = new AJAXRequestResult(AJAXRequestResult.DIRECT_OBJECT, "direct").setType(AJAXRequestResult.ResultType.DIRECT);
                if (bytesWritten != 0) {
                    result.setResponseProperty("X-Content-Size", Long.valueOf(bytesWritten));
                }
                return result;
            } catch (final IOException e) {
                throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
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
        ZipArchiveOutputStream zipOutput = null;
        try {
            // Initialize ZIP output stream
            zipOutput = new ZipArchiveOutputStream(out);
            zipOutput.setEncoding("UTF-8");
            zipOutput.setUseLanguageEncodingFlag(true);
            if (optCompressionLevel > 0) {
                zipOutput.setLevel(optCompressionLevel);
            }

            // The buffer to use
            Buffer buffer = new Buffer();

            // The map for used names
            Map<String, Integer> fileNamesInArchive = new HashMap<String, Integer>();

            // Add ZIP entries
            adder.addZipEntries(zipOutput, buffer, fileNamesInArchive);

            // Return number of written bytes
            return zipOutput.getBytesWritten();
        } finally {
            // Complete the ZIP file
            Streams.close(zipOutput);
        }
    }

}
