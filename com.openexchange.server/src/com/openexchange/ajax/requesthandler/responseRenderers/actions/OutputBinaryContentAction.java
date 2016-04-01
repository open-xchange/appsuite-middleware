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

package com.openexchange.ajax.requesthandler.responseRenderers.actions;

import static com.openexchange.java.Strings.isEmpty;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.fileholder.PushbackReadable;
import com.openexchange.ajax.fileholder.Readable;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.responseRenderers.FileResponseRenderer;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.tools.io.IOUtils;
import com.openexchange.tools.servlet.http.Tools;

/**
 * {@link OutputBinaryContentAction} writes the binary content to the OutPutStream of the response object
 *
 * Influence the following IDataWrapper attributes:
 * <ul>
 * <li>response
 * </ul>
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.0
 */
public class OutputBinaryContentAction implements IFileResponseRendererAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FileResponseRenderer.class);
    private static final int BUFLEN = 10240;
    private static final String MULTIPART_BOUNDARY = "MULTIPART_BYTERANGES";

    @Override
    public void call(IDataWrapper data) throws Exception {
        /*
         * Output binary content
         */
        try {
            final ServletOutputStream outputStream = data.getResponse().getOutputStream();
            final String sRange = data.getRequest().getHeader("Range");
            if ((data.getLength() > 0) && (null != sRange)) {
                // Taken from http://balusc.blogspot.co.uk/2009/02/fileservlet-supporting-resume-and.html
                // Range header should match format "bytes=n-n,n-n,n-n...". If not, then return 416.
                if (!Tools.isByteRangeHeader(sRange)) {
                    data.getResponse().setHeader("Content-Range", "bytes */" + data.getLength()); // Required in 416.
                    data.getResponse().sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                    return;
                }
                // If-Range header should either match ETag or be greater then LastModified. If not,
                // then return full file.
                boolean full = false;
                {
                    final String ifRange = data.getRequestData().getHeader("If-Range");
                    final String eTag = data.getResult().getHeader("ETag");
                    if (ifRange != null && !ifRange.equals(eTag)) {
                        try {
                            final long ifRangeTime = data.getRequest().getDateHeader("If-Range"); // Throws IAE if invalid.
                            if (ifRangeTime != -1 && ifRangeTime < data.getResult().getExpires()) {
                                full = true;
                            }
                        } catch (final IllegalArgumentException ignore) {
                            full = true;
                        }
                    }
                }
                // If any valid If-Range header, then process each part of byte range.
                final List<Range> ranges;
                if (full) {
                    ranges = Collections.emptyList();
                } else {
                    final String[] parts = Strings.splitByComma(sRange.substring(6));
                    ranges = new ArrayList<Range>(parts.length);
                    for (final String part : parts) {
                        // Assuming a file with length of 100, the following examples returns bytes at:
                        // 50-80 (50 to 80), 40- (40 to length=100), -20 (length-20=80 to length=100).
                        final int dashPos = part.indexOf('-');
                        long start = sublong(part, 0, dashPos);
                        long end = sublong(part, dashPos + 1, part.length());

                        if (start == -1) {
                            start = data.getLength() - end;
                            end = data.getLength() - 1;
                        } else if (end == -1 || end > data.getLength() - 1) {
                            end = data.getLength() - 1;
                        }

                        // Check if Range is syntactically valid. If not, then return 416.
                        if (start > end) {
                            data.getResponse().setHeader("Content-Range", "bytes */" + data.getLength()); // Required in 416.
                            data.getResponse().sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                            return;
                        }

                        // Add range.
                        ranges.add(new Range(start, end, data.getLength()));
                    }
                }
                if (full || ranges.isEmpty()) {
                    // Return full file.
                    final Range r = new Range(0L, data.getLength() - 1, data.getLength());
                    data.getResponse().setHeader("Content-Range", new StringBuilder("bytes ").append(r.start).append('-').append(r.end).append('/').append(r.total).toString());

                    // Copy full range.
                    copy(data.getDocumentData(), outputStream);
                } else if (ranges.size() == 1) {

                    // Return single part of file.
                    final Range r = ranges.get(0);
                    data.getResponse().setHeader("Content-Range", new StringBuilder("bytes ").append(r.start).append('-').append(r.end).append('/').append(r.total).toString());
                    data.getResponse().setHeader("Content-Length", Long.toString(r.length));
                    data.getResponse().setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.

                    // Copy single part range.
                    copy(data.getDocumentData(), outputStream, r.start, r.length);
                } else {
                    // Return multiple parts of file.
                    final String boundary = MULTIPART_BOUNDARY;
                    data.getResponse().setContentType(new StringBuilder("multipart/byteranges; boundary=").append(boundary).toString());
                    data.getResponse().setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.

                    // Copy multi part range.
                    for (final Range r : ranges) {
                        // Add multipart boundary and header fields for every range.
                        outputStream.println();
                        outputStream.println(new StringBuilder("--").append(boundary).toString());
                        outputStream.println(new StringBuilder("Content-Type: ").append(data.getContentType()).toString());
                        outputStream.println(new StringBuilder("Content-Range: bytes ").append(r.start).append('-').append(r.end).append('/').append(r.total).toString());

                        // Copy single part range of multi part range.
                        copy(data.getDocumentData(), outputStream, r.start, r.length);
                    }

                    // End with multipart boundary.
                    outputStream.println();
                    outputStream.println(new StringBuilder("--").append(boundary).append("--").toString());
                }
            } else {
                // Check for "off"/"len" parameters
                final int off = AJAXRequestDataTools.parseIntParameter(data.getRequest().getParameter("off"), -1);
                final int amount = AJAXRequestDataTools.parseIntParameter(data.getRequest().getParameter("len"), -1);
                if (off >= 0 && amount > 0) {
                    try {
                        data.getResponse().setHeader("Content-Length", Long.toString(amount));
                        copy(data.getDocumentData(), outputStream, off, amount);
                    } catch (final OffsetOutOfRangeIOException e) {
                        setHeaderSafe("Content-Range", "bytes */" + e.getAvailable(), data.getResponse()); // Required in 416.
                        data.getResponse().sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                        return;
                    }
                } else {
                    // Copy full range.
                    copy(data.getDocumentData(), outputStream);
                }
            }
            outputStream.flush();
        } catch (final java.net.SocketException e) {
            final String lmsg = com.openexchange.java.Strings.toLowerCase(e.getMessage());
            if ("broken pipe".equals(lmsg) || "connection reset".equals(lmsg)) {
                // Assume client-initiated connection closure
                LOG.debug("Underlying (TCP) protocol communication aborted while trying to output file{}", (isEmpty(data.getFileName()) ? "" : " " + data.getFileName()), e);
            } else {
                LOG.warn("Lost connection to client while trying to output file{}", (isEmpty(data.getFileName()) ? "" : " " + data.getFileName()), e);
            }
        } catch (final com.sun.mail.util.MessageRemovedIOException e) {
            sendErrorSafe(HttpServletResponse.SC_NOT_FOUND, "Message not found.", data.getResponse());
        } catch (final IOException e) {
            final String lcm = com.openexchange.java.Strings.toLowerCase(e.getMessage());
            if ("connection reset by peer".equals(lcm) || "broken pipe".equals(lcm)) {
                /*-
                 * The client side has abruptly aborted the connection.
                 * That can have many causes which are not controllable by us.
                 *
                 * For instance, the peer doesn't understand what it received and therefore closes its socket.
                 * For the next write attempt by us, the peer's TCP stack will issue an RST,
                 * which results in this exception and message at the sender.
                 */
                LOG.debug("Client dropped connection while trying to output file{}", (isEmpty(data.getFileName()) ? "" : " " + data.getFileName()), e);
            } else {
                LOG.warn("Lost connection to input or output end-point while trying to output file{}", (isEmpty(data.getFileName()) ? "" : " " + data.getFileName()), e);
            }
        }

    }

    /**
     * Returns a substring of the given string value from the given begin index to the given end index as a long.
     * <p>
     * If the substring is empty, then <code>-1</code> will be returned
     *
     * @param value The string value to return a substring as long for.
     * @param beginIndex The begin index of the substring to be returned as long.
     * @param endIndex The end index of the substring to be returned as long.
     * @return A substring of the given string value as long or <code>-1</code> if substring is empty.
     */
    private long sublong(final String value, final int beginIndex, final int endIndex) {
        final String substring = value.substring(beginIndex, endIndex);
        return (substring.length() > 0) ? Long.parseLong(substring) : -1;
    }

    private void setHeaderSafe(final String name, final String value, final HttpServletResponse resp) {
        try {
            resp.setHeader(name, value);
        } catch (final Exception e) {
            // Ignore
        }
    }

    private void sendErrorSafe(int sc, String msg, final HttpServletResponse resp) {
        try {
            resp.sendError(sc, msg);
        } catch (final Exception e) {
            // Ignore
        }
    }

    /**
     * Copies the given byte range of the given input to the given output.
     *
     * @param input The input to copy the given range to the given output for.
     * @param output The output to copy the given range from the given input for.
     * @param start Start of the byte range.
     * @param length Length of the byte range.
     * @throws IOException If something fails at I/O level.
     */
    private static void copy(IFileHolder.RandomAccess input, OutputStream output, long start, long length) throws IOException, OXException {
        if (input.length() == length) {
            // Write full range.
            copy(input, output);
        } else {
            // Write partial range.
            input.seek(start);   // ----> OffsetOutOfRangeIOException
            long toRead = length;

            // Check first byte
            @SuppressWarnings("resource") PushbackReadable readMe = new PushbackReadable(input);
            {
                byte[] bs = new byte[1];
                int first = readMe.read(bs);
                if (first <= 0) {
                    // Not enough bytes
                    throw new OffsetOutOfRangeIOException(start, input.length());
                }

                // Unread first byte
                readMe.unread(bs[0] & 0xff);
            }

            int buflen = BUFLEN;
            byte[] buffer = new byte[buflen];
            int read;

            while ((read = readMe.read(buffer, 0, buflen)) > 0) {
                if ((toRead -= read) > 0) {
                    output.write(buffer, 0, read);
                } else {
                    output.write(buffer, 0, (int) toRead + read);
                    break;
                }
            }
        }
    }

    /**
     * Copies the given input to the given output.
     *
     * @param input The input to copy to the given output.
     * @param output The output to copy from the given input.
     * @throws IOException If something fails at I/O level.
     */
    private static void copy(Readable input, OutputStream output) throws IOException, OXException {
        // Write full range.
        if (IFileHolder.InputStreamClosure.class.isInstance(input)) {
            InputStream stream = null;
            try {
                stream = ((IFileHolder.InputStreamClosure) input).newStream();
                IOUtils.transfer(stream, output);
            } finally {
                Streams.close(stream);
            }
        } else {
            int buflen = BUFLEN;
            byte[] buffer = new byte[buflen];
            int read;
            while ((read = input.read(buffer, 0, buflen)) > 0) {
                output.write(buffer, 0, read);
            }
        }
    }

    /**
     * Copies the given byte range of the given input to the given output.
     *
     * @param inputStream The input to copy the given range to the given output for.
     * @param output The output to copy the given range from the given input for.
     * @param start Start of the byte range.
     * @param length Length of the byte range.
     * @throws IOException If something fails at I/O level.
     * @throws OXException
     */
    private void copy(Readable inputStream, OutputStream output, long start, long length) throws IOException, OXException {
        if (inputStream instanceof IFileHolder.RandomAccess) {
            copy((IFileHolder.RandomAccess) inputStream, output, start, length);
            return;
        }

        // Write partial range.
        // Discard previous bytes
        byte[] buffer = new byte[1];
        for (int i = 0; i < start; i++) {
            if (inputStream.read(buffer, 0, 1) < 0) {
                // Stream does not provide enough bytes
                throw new OffsetOutOfRangeIOException(start, i);
            }
            // Valid byte read... Continue.
        }
        long toRead = length;

        int buflen = BUFLEN;
        buffer = new byte[buflen];
        int read;
        while ((read = inputStream.read(buffer, 0, buflen)) > 0) {
            if ((toRead -= read) > 0) {
                output.write(buffer, 0, read);
            } else {
                output.write(buffer, 0, (int) toRead + read);
                break;
            }
        }
    }

    private static final class Range {

        /** The begin position (inclusive) */
        final long start;

        /** The end position (inclusive) */
        final long end;

        /** The length */
        final long length;

        /** The total length */
        final long total;

        Range(final long start, final long end, final long total) {
            super();
            this.start = start;
            this.end = end;
            length = end - start + 1;
            this.total = total;
        }

    } // End of class Range

    private static final class OffsetOutOfRangeIOException extends IOException {

        private static final long serialVersionUID = 8094333124726048736L;

        private final long off;

        private final long available;

        /**
         * Initializes a new {@link OffsetOutOfRangeIOException}.
         */
        public OffsetOutOfRangeIOException(final long off, final long available) {
            super("Offset " + off + " out of range. Got only " + available);
            this.off = off;
            this.available = available;
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }

        /**
         * Gets the off
         *
         * @return The off
         */
        public long getOff() {
            return off;
        }

        /**
         * Gets the available
         *
         * @return The available
         */
        public long getAvailable() {
            return available;
        }

    } // End of class OffsetOutOfRangeIOException

}
