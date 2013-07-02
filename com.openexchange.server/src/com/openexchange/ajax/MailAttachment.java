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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.ajax;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.helper.BrowserDetector;
import com.openexchange.ajax.helper.DownloadUtility;
import com.openexchange.ajax.helper.DownloadUtility.CheckedDownload;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.exception.OXException;
import com.openexchange.html.HtmlService;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.StringAllocator;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.attachment.AttachmentToken;
import com.openexchange.mail.attachment.AttachmentTokenRegistry;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.utils.MessageUtility;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;

/**
 * {@link MailAttachment}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailAttachment extends AJAXServlet {

    private static final long serialVersionUID = -3109402774466180271L;

    private static final Log LOG = com.openexchange.log.Log.loggerFor(MailAttachment.class);

    private static final String MULTIPART_BOUNDARY = "MULTIPART_BYTERANGES";
    private static final int BUFLEN = 2048;
    private static final Pattern PATTERN_BYTE_RANGES = Pattern.compile("^bytes=\\d*-\\d*(,\\d*-\\d*)*$");

    /**
     * Initializes a new {@link MailAttachment}.
     */
    public MailAttachment() {
        super();
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        Tools.disableCaching(resp);
        /*
         * Get attachment
         */
        boolean outSelected = false;
        try {
            final String id = req.getParameter(PARAMETER_ID);
            if (null == id) {
                throw MailExceptionCode.MISSING_PARAM.create(PARAMETER_ID);
            }
            final boolean saveToDisk;
            {
                final String saveParam = req.getParameter("save");
                saveToDisk = ((saveParam == null || saveParam.length() == 0) ? false : ((Integer.parseInt(saveParam)) > 0));
            }
            final boolean filter;
            {
                final String filterParam = req.getParameter(PARAMETER_FILTER);
                filter = Boolean.parseBoolean(filterParam) || "1".equals(filterParam);
            }
            Tools.removeCachingHeader(resp);
            final AttachmentToken token = AttachmentTokenRegistry.getInstance().getToken(id);
            if (null == token) {
                throw MailExceptionCode.ATTACHMENT_EXPIRED.create();
            }
            /*-
             * Security check
             *
             * IP-Check appropriate for roaming mobile devices?
             */
            if (token.isCheckIp() && null != token.getClientIp() && !req.getRemoteAddr().equals(token.getClientIp())) {
                AttachmentTokenRegistry.getInstance().removeToken(id);
                throw MailExceptionCode.ATTACHMENT_EXPIRED.create();
            }
            /*
             * At least expect the same user agent as the one which created the attachment token
             */
            if (token.isOneTime() && null != token.getUserAgent()) {
                final String requestUserAgent = req.getHeader("user-agent");
                if (null == requestUserAgent) {
                    AttachmentTokenRegistry.getInstance().removeToken(id);
                    throw MailExceptionCode.ATTACHMENT_EXPIRED.create();
                }
                if (!new BrowserDetector(token.getUserAgent()).nearlyEquals(new BrowserDetector(requestUserAgent))) {
                    AttachmentTokenRegistry.getInstance().removeToken(id);
                    throw MailExceptionCode.ATTACHMENT_EXPIRED.create();
                }
            }
            /*
             * Write part to output stream
             */
            final MailPart mailPart = token.getAttachment();
            InputStream attachmentInputStream = null;
            ThresholdFileHolder tfh = null;
            try {
                long length = -1L;
                if (filter && !saveToDisk && mailPart.getContentType().isMimeType(MimeTypes.MIME_TEXT_HTM_ALL)) {
                    /*
                     * Apply filter
                     */
                    final ContentType contentType = mailPart.getContentType();
                    final String cs = contentType.containsCharsetParameter() ? contentType.getCharsetParameter() : MailProperties.getInstance().getDefaultMimeCharset();
                    String htmlContent = MessageUtility.readMailPart(mailPart, cs);
                    htmlContent = MessageUtility.simpleHtmlDuplicateRemoval(htmlContent);
                    final HtmlService htmlService = ServerServiceRegistry.getInstance().getService(HtmlService.class);
                    final byte[] bytes = sanitizeHtml(htmlContent, htmlService).getBytes(Charsets.forName(cs));
                    length = bytes.length;
                    attachmentInputStream = new UnsynchronizedByteArrayInputStream(bytes);
                } else {
                    attachmentInputStream = mailPart.getInputStream();
                    /*-
                     * Unfortunately, size indicated by mail part is not exact, therefore skip it.
                     *
                    length = mailPart.getSize();
                    if (length <= 0) {
                        tfh = new ThresholdFileHolder();
                        tfh.write(attachmentInputStream);
                        attachmentInputStream.close();
                        attachmentInputStream = tfh.getStream();
                        length = tfh.getLength();
                    }
                     *
                     */
                }
                /*
                 * Content-Length
                 */
                if (length > 0) {
                    resp.setHeader("Accept-Ranges", "bytes");
                    resp.setHeader("Content-Length", Long.toString(length));
                }
                /*
                 * Set Content-Type and Content-Disposition header
                 */
                final String fileName = mailPart.getFileName();
                final String userAgent = AJAXServlet.sanitizeParam(req.getHeader("user-agent"));
                final String contentType;
                if (saveToDisk) {
                    /*
                     * We are supposed to offer attachment for download. Therefore enforce application/octet-stream and attachment
                     * disposition.
                     */
                    final StringAllocator sb = new StringAllocator(32);
                    sb.append("attachment");
                    DownloadUtility.appendFilenameParameter(fileName, null, userAgent, sb);
                    resp.setHeader("Content-Disposition", sb.toString());
                    if (mailPart.containsContentType()) {
                        final ContentType ct = mailPart.getContentType();
                        ct.removeParameter("name");
                        contentType = ct.toString();
                    } else {
                        contentType = "application/octet-stream";
                    }
                    resp.setContentType(contentType);
                } else {
                    final CheckedDownload checkedDownload = DownloadUtility.checkInlineDownload(attachmentInputStream, fileName, mailPart.getContentType().toString(), userAgent);
                    contentType = checkedDownload.getContentType();
                    resp.setContentType(contentType);
                    resp.setHeader("Content-Disposition", checkedDownload.getContentDisposition());
                    attachmentInputStream = checkedDownload.getInputStream();
                }
                /*
                 * Reset response header values since we are going to directly write into servlet's output stream and then some browsers do
                 * not allow header "Pragma"
                 */
                Tools.removeCachingHeader(resp);
                try {
                    final ServletOutputStream outputStream = resp.getOutputStream();
                    outSelected = true;
                    final String sRange;
                    if (length > 0 && null != (sRange = req.getHeader("Range"))) {
                        // Taken from http://balusc.blogspot.co.uk/2009/02/fileservlet-supporting-resume-and.html
                        // Range header should match format "bytes=n-n,n-n,n-n...". If not, then return 416.
                        if (!PATTERN_BYTE_RANGES.matcher(sRange).matches()) {
                            resp.setHeader("Content-Range", "bytes */" + length); // Required in 416.
                            resp.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                            return;
                        }
                        // If-Range header should either match ETag or be greater then LastModified. If not,
                        // then return full file.
                        boolean full = false;
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
                                    start = length - end;
                                    end = length - 1;
                                } else if (end == -1 || end > length - 1) {
                                    end = length - 1;
                                }

                                // Check if Range is syntactically valid. If not, then return 416.
                                if (start > end) {
                                    resp.setHeader("Content-Range", "bytes */" + length); // Required in 416.
                                    resp.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                                    return;
                                }

                                // Add range.
                                ranges.add(new Range(start, end, length));
                            }
                        }
                        if (full || ranges.isEmpty()) {
                            // Return full file.
                            final Range r = new Range(0L, length - 1, length);
                            resp.setHeader("Content-Range", new StringAllocator("bytes ").append(r.start).append('-').append(r.end).append('/').append(r.total).toString());

                            // Copy full range.
                            copy(attachmentInputStream, outputStream, r.start, r.length);
                        } else if (ranges.size() == 1) {

                            // Return single part of file.
                            final Range r = ranges.get(0);
                            resp.setHeader("Content-Range", new StringAllocator("bytes ").append(r.start).append('-').append(r.end).append('/').append(r.total).toString());
                            resp.setHeader("Content-Length", Long.toString(r.length));
                            resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.

                            // Copy single part range.
                            copy(attachmentInputStream, outputStream, r.start, r.length);
                        } else {
                            // Return multiple parts of file.
                            final String boundary = MULTIPART_BOUNDARY;
                            resp.setContentType(new StringAllocator("multipart/byteranges; boundary=").append(boundary).toString());
                            resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.

                            // Copy multi part range.
                            for (final Range r : ranges) {
                                // Add multipart boundary and header fields for every range.
                                outputStream.println();
                                outputStream.println(new StringAllocator("--").append(boundary).toString());
                                outputStream.println(new StringAllocator("Content-Type: ").append(contentType).toString());
                                outputStream.println(new StringAllocator("Content-Range: bytes ").append(r.start).append('-').append(r.end).append('/').append(r.total).toString());

                                // Copy single part range of multi part range.
                                copy(attachmentInputStream, outputStream, r.start, r.length);
                            }

                            // End with multipart boundary.
                            outputStream.println();
                            outputStream.println(new StringAllocator("--").append(boundary).append("--").toString());
                        }
                    } else {
                        final int len = BUFLEN;
                        final byte[] buf = new byte[len];
                        for (int read; (read = attachmentInputStream.read(buf, 0, len)) > 0;) {
                            outputStream.write(buf, 0, read);
                        }
                    }
                    outputStream.flush();
                } catch (final java.net.SocketException e) {
                    final String lmsg = toLowerCase(e.getMessage());
                    if ("broken pipe".equals(lmsg) || "connection reset".equals(lmsg)) {
                        // Assume client-initiated connection closure
                        LOG.debug("Underlying (TCP) protocol communication aborted while trying to output file" + (isEmpty(fileName) ? "" : " " + fileName), e);
                    } else {
                        LOG.warn("Lost connection to client while trying to output file" + (isEmpty(fileName) ? "" : " " + fileName), e);
                    }
                } catch (final com.sun.mail.util.MessageRemovedIOException e) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Message not found.");
                } catch (final IOException e) {
                    if ("connection reset by peer".equals(toLowerCase(e.getMessage()))) {
                        /*-
                         * The client side has abruptly aborted the connection.
                         * That can have many causes which are not controllable by us.
                         *
                         * For instance, the peer doesn't understand what it received and therefore closes its socket.
                         * For the next write attempt by us, the peer's TCP stack will issue an RST,
                         * which results in this exception and message at the sender.
                         */
                        LOG.debug("Client dropped connection while trying to output file" + (isEmpty(fileName) ? "" : " " + fileName), e);
                    } else {
                        LOG.warn("Lost connection to client while trying to output file" + (isEmpty(fileName) ? "" : " " + fileName), e);
                    }
                }
            } finally {
                Streams.close(token, attachmentInputStream, tfh);
            }
        } catch (final OXException e) {
            callbackError(resp, outSelected, e);
        } catch (final Exception e) {
            final OXException exc = getWrappingOXException(e);
            LOG.error(exc.getMessage(), exc);
            callbackError(resp, outSelected, exc);
        }
    }

    private static boolean isMSIEOnWindows(final String userAgent) {
        final BrowserDetector browserDetector = new BrowserDetector(userAgent);
        return (browserDetector.isMSIE() && browserDetector.isWindows());
    }

    /**
     * Generates a wrapping {@link AbstractOXException} for specified exception.
     *
     * @param cause The exception to wrap
     * @return The wrapping {@link AbstractOXException}
     */
    protected static final OXException getWrappingOXException(final Exception cause) {
        if (LOG.isWarnEnabled()) {
            final StringBuilder warnBuilder = new StringBuilder(140);
            warnBuilder.append("An unexpected exception occurred, which is going to be wrapped for proper display.\n");
            warnBuilder.append("For safety reason its original content is display here.");
            LOG.warn(warnBuilder.toString(), cause);
        }
        return new OXException(cause);
    }

    private static void callbackError(final HttpServletResponse resp, final boolean outSelected, final OXException e) {
        try {
            resp.setContentType("text/html; charset=UTF-8");
            final Writer writer;
            if (outSelected) {
                /*
                 * Output stream has already been selected
                 */
                Tools.disableCaching(resp);
                writer =
                    new PrintWriter(new BufferedWriter(new OutputStreamWriter(resp.getOutputStream(), resp.getCharacterEncoding())), true);
            } else {
                writer = resp.getWriter();
            }
            resp.setHeader("Content-Disposition", null);
            final Response response = new Response();
            response.setException(e);
            writer.write(substituteJS(ResponseWriter.getJSON(response).toString(), "error"));
            writer.flush();
        } catch (final UnsupportedEncodingException uee) {
            uee.initCause(e);
            LOG.error(uee.getMessage(), uee);
        } catch (final IOException ioe) {
            ioe.initCause(e);
            LOG.error(ioe.getMessage(), ioe);
        } catch (final IllegalStateException ise) {
            ise.initCause(e);
            LOG.error(ise.getMessage(), ise);
        } catch (final JSONException je) {
            je.initCause(e);
            LOG.error(je.getMessage(), je);
        }
    }

    private static String sanitizeHtml(final String htmlContent, final HtmlService htmlService) {
        return htmlService.sanitize(htmlContent, null, false, null, null);
    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html; charset=UTF-8");
        res.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html; charset=UTF-8");
        res.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    /** Check for an empty string */
    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    /** ASCII-wise to lower-case */
    private static String toLowerCase(final CharSequence chars) {
        if (null == chars) {
            return null;
        }
        final int length = chars.length();
        final StringAllocator builder = new StringAllocator(length);
        for (int i = 0; i < length; i++) {
            final char c = chars.charAt(i);
            builder.append((c >= 'A') && (c <= 'Z') ? (char) (c ^ 0x20) : c);
        }
        return builder.toString();
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

    /**
     * Copy the given byte range of the given input to the given output.
     *
     * @param inputStream The input to copy the given range to the given output for.
     * @param output The output to copy the given range from the given input for.
     * @param start Start of the byte range.
     * @param length Length of the byte range.
     * @throws IOException If something fails at I/O level.
     */
    private void copy(final InputStream inputStream, final OutputStream output, final long start, final long length) throws IOException {
        // Write partial range.
        final InputStream input;
        if (!(inputStream instanceof BufferedInputStream) && !(inputStream instanceof ByteArrayInputStream)) {
            input = new BufferedInputStream(inputStream, 8192);
        } else {
            input = inputStream;
        }
        // Discard previous bytes
        for (int i = 0; i < start; i++) {
            if (input.read() < 0) {
                // Stream does not provide enough bytes
                throw new IOException("Start index " + start + " out of range. Got only " + i);
            }
            // Valid byte read... Continue.
        }
        long toRead = length;

        final byte[] buffer = new byte[BUFLEN];
        int read;
        while ((read = input.read(buffer)) > 0) {
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

}
