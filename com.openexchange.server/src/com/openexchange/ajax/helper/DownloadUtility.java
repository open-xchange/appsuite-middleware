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

package com.openexchange.ajax.helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import com.openexchange.exception.OXException;
import com.openexchange.html.HtmlService;
import com.openexchange.java.CharsetDetector;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.StringAllocator;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.ImageTypeDetector;
import com.openexchange.tools.encoding.Helper;
import com.openexchange.tools.encoding.URLCoder;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link DownloadUtility} - Utility class for download.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DownloadUtility {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(DownloadUtility.class));

    /**
     * Initializes a new {@link DownloadUtility}.
     */
    private DownloadUtility() {
        super();
    }

    /**
     * Checks specified input stream intended for inline display for harmful data if its Content-Type indicates image content.
     *
     * @param inputStream The input stream
     * @param fileName The file name
     * @param contentTypeStr The content-type string
     * @param userAgent The user agent
     * @return The checked download providing input stream, content type, and content disposition to use
     * @throws OXException If checking download fails
     */
    public static CheckedDownload checkInlineDownload(final InputStream inputStream, final String fileName, final String contentTypeStr, final String userAgent) throws OXException {
        return checkInlineDownload(inputStream, fileName, contentTypeStr, null, userAgent);
    }

    /**
     * Checks specified input stream intended for inline display for harmful data if its Content-Type indicates image content.
     *
     * @param inputStream The input stream
     * @param fileName The file name
     * @param sContentType The <i>Content-Type</i> string
     * @param overridingDisposition Optionally overrides the <i>Content-Disposition</i> header
     * @param userAgent The <i>User-Agent</i>
     * @return The checked download providing input stream, content type, and content disposition to use
     * @throws OXException If checking download fails
     */
    public static CheckedDownload checkInlineDownload(final InputStream inputStream, final String fileName, final String sContentType, final String overridingDisposition, final String userAgent) throws OXException {
        return checkInlineDownload(inputStream, -1L, fileName, sContentType, overridingDisposition, userAgent);
    }


    private static final String MIME_APPL_OCTET = MimeTypes.MIME_APPL_OCTET;

    /**
     * Checks specified input stream intended for inline display for harmful data if its Content-Type indicates image content.
     *
     * @param inputStream The input stream
     * @param size The size of the passed stream
     * @param fileName The file name
     * @param sContentType The <i>Content-Type</i> string
     * @param overridingDisposition Optionally overrides the <i>Content-Disposition</i> header
     * @param userAgent The <i>User-Agent</i>
     * @return The checked download providing input stream, content type, and content disposition to use
     * @throws OXException If checking download fails
     */
    public static CheckedDownload checkInlineDownload(final InputStream inputStream, final long sizer, final String fileName, final String sContentType, final String overridingDisposition, final String userAgent) throws OXException {
        try {
            /*
             * We are supposed to let the client display the attachment. Therefore set attachment's Content-Type and inline disposition to let
             * the client decide if it's able to display.
             */
            final ContentType contentType = new ContentType(sContentType);
            if ((null != fileName) && contentType.startsWith(MIME_APPL_OCTET)) {
                /*
                 * Try to determine MIME type
                 */
                final String ct = MimeType2ExtMap.getContentType(fileName);
                final int pos = ct.indexOf('/');
                contentType.setPrimaryType(ct.substring(0, pos));
                contentType.setSubType(ct.substring(pos + 1));
            }
            String sContentDisposition = overridingDisposition;
            long sz = sizer;
            InputStream in = inputStream;
            // Some variables
            String fn = fileName;
            byte[] bytes;
            // Check by Content-Type and file name
            if (contentType.startsWithAny("text/htm", "text/xhtm", "text/xml")) {
                /*
                 * HTML content requested for download...
                 */
                if (null == sContentDisposition) {
                    sContentDisposition = "attachment";
                } else if (toLowerCase(sContentDisposition).startsWith("inline")) {
                    /*
                     * Sanitizing of HTML content needed
                     */
                    final ByteArrayOutputStream baos = Streams.stream2ByteArrayOutputStream(in);
                    final HtmlService htmlService = ServerServiceRegistry.getInstance().getService(HtmlService.class);
                    String cs = contentType.getCharsetParameter();
                    if (!CharsetDetector.isValid(cs)) {
                        cs = CharsetDetector.detectCharset(Streams.asInputStream(baos));
                        if ("US-ASCII".equalsIgnoreCase(cs)) {
                            cs = "ISO-8859-1";
                        }
                    }
                    String htmlContent = baos.toString(cs);
                    htmlContent = htmlService.sanitize(htmlContent, null, true, null, null);
                    final byte[] tmp = htmlContent.getBytes(Charsets.forName(cs));
                    sz = tmp.length;
                    in = Streams.newByteArrayInputStream(tmp);
                }
            } else if (contentType.startsWith("image/") || fileNameImpliesImage(fileName)) {
                /*
                 * Image content requested for download...
                 */
                final BrowserDetector browserDetector = new BrowserDetector(userAgent);
                final boolean msieOnWindows = (browserDetector.isMSIE() && browserDetector.isWindows());
                if (msieOnWindows && 8F > browserDetector.getBrowserVersion()) {
                    /*-
                     * Image content requested by Internet Explorer < v8
                     *
                     * Get first 256 bytes
                     */
                    byte[] sequence = new byte[256];
                    {
                        final int nRead = in.read(sequence, 0, sequence.length);
                        if (nRead < sequence.length) {
                            final byte[] tmp = sequence;
                            sequence = new byte[nRead];
                            System.arraycopy(tmp, 0, sequence, 0, nRead);
                        }
                    }
                    /*
                     * Check consistency of content-type, file extension and magic bytes
                     */
                    String preparedFileName = getSaveAsFileName(fileName, msieOnWindows, sContentType);
                    final String fileExtension = getFileExtension(fn);
                    if (null == fileExtension) {
                        /*
                         * Check for HTML since no corresponding file extension is known
                         */
                        if (HTMLDetector.containsHTMLTags(sequence)) {
                            return asAttachment(inputStream, preparedFileName, sz);
                        }
                    } else {
                        final Set<String> extensions = new HashSet<String>(MimeType2ExtMap.getFileExtensions(contentType.getBaseType()));
                        if (extensions.isEmpty() || (extensions.size() == 1 && extensions.contains("dat"))) {
                            /*
                             * Content type determined by file name extension is unknown
                             */
                            final String ct = MimeType2ExtMap.getContentType(fn);
                            if (MIME_APPL_OCTET.equals(ct)) {
                                /*
                                 * No content type known
                                 */
                                if (HTMLDetector.containsHTMLTags(sequence)) {
                                    return asAttachment(inputStream, preparedFileName, sz);
                                }
                            } else {
                                final int pos = ct.indexOf('/');
                                contentType.setPrimaryType(ct.substring(0, pos));
                                contentType.setSubType(ct.substring(pos + 1));
                            }
                        } else if (!extensions.contains(fileExtension)) {
                            /*
                             * File extension does not fit to MIME type. Reset file name.
                             */
                            fn = addFileExtension(fileExtension, extensions.iterator().next());
                            preparedFileName = getSaveAsFileName(fn, msieOnWindows, contentType.getBaseType());
                        }
                        final String detectedCT = ImageTypeDetector.getMimeType(sequence);
                        if (MIME_APPL_OCTET.equals(detectedCT)) {
                            /*
                             * Unknown magic bytes. Check for HTML.
                             */
                            if (HTMLDetector.containsHTMLTags(sequence)) {
                                return asAttachment(inputStream, preparedFileName, sz);
                            }
                        } else if (!contentType.isMimeType(detectedCT)) {
                            /*
                             * Image's magic bytes indicate another content type
                             */
                            contentType.setBaseType(detectedCT);
                        }
                    }
                    /*
                     * New combined input stream (with original size)
                     */
                    in = new CombinedInputStream(sequence, in);
                }
            } else if (fileNameImpliesHtml(fileName) && HTMLDetector.containsHTMLTags((bytes = Streams.stream2bytes(in)))) {
                /*
                 * HTML content requested for download...
                 */
                if (null == sContentDisposition) {
                    sContentDisposition = "attachment";
                } else if (toLowerCase(sContentDisposition).startsWith("inline")) {
                    /*
                     * Sanitizing of HTML content needed
                     */
                    final HtmlService htmlService = ServerServiceRegistry.getInstance().getService(HtmlService.class);
                    String cs = contentType.getCharsetParameter();
                    if (!CharsetDetector.isValid(cs)) {
                        cs = CharsetDetector.detectCharset(Streams.newByteArrayInputStream(bytes));
                        if ("US-ASCII".equalsIgnoreCase(cs)) {
                            cs = "ISO-8859-1";
                        }
                    }
                    String htmlContent = new String(bytes, Charsets.forName(cs));
                    htmlContent = htmlService.sanitize(htmlContent, null, true, null, null);
                    final byte[] tmp = htmlContent.getBytes(Charsets.forName(cs));
                    sz = tmp.length;
                    in = Streams.newByteArrayInputStream(tmp);
                }
            }
            /*
             * Create return value
             */
            final CheckedDownload retval;
            if (sContentDisposition == null) {
                final com.openexchange.java.StringAllocator builder = new com.openexchange.java.StringAllocator(32).append("attachment");
                appendFilenameParameter(fileName, contentType.isBaseType("application", "octet-stream") ? null : contentType.toString(), userAgent, builder);
                contentType.removeParameter("name");
                retval = new CheckedDownload(contentType.toString(), builder.toString(), in, sz);
            } else if (sContentDisposition.indexOf(';') < 0) {
                final com.openexchange.java.StringAllocator builder = new com.openexchange.java.StringAllocator(32).append(sContentDisposition);
                appendFilenameParameter(fileName, contentType.isBaseType("application", "octet-stream") ? null : contentType.toString(), userAgent, builder);
                contentType.removeParameter("name");
                retval = new CheckedDownload(contentType.toString(), builder.toString(), in, sz);
            } else {
                contentType.removeParameter("name");
                retval = new CheckedDownload(contentType.toString(), sContentDisposition, in, sz);
            }
            return retval;
        } catch (final UnsupportedEncodingException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    private static boolean fileNameImpliesHtml(final String fileName) {
        return null != fileName && MimeType2ExtMap.getContentType(fileName).startsWith("text/htm");
    }

    private static boolean fileNameImpliesImage(final String fileName) {
        return null != fileName && MimeType2ExtMap.getContentType(fileName).startsWith("image/");
    }

    /**
     * Appends the <tt>"filename"</tt> parameter to specified {@link StringBuilder} instance; e.g.
     *
     * <pre>
     * "attachment; filename="readme.txt"
     *            ^---------------------^
     * </pre>
     *
     * @param fileName The file name
     * @param userAgent The user agent identifier
     * @param appendTo The {@link StringBuilder} instance to append to
     */
    public static void appendFilenameParameter(final String fileName, final String userAgent, final StringBuilder appendTo) {
        appendFilenameParameter(fileName, null, userAgent, appendTo);
    }

    /**
     * Appends the <tt>"filename"</tt> parameter to specified {@link StringBuilder} instance; e.g.
     *
     * <pre>
     * "attachment; filename="readme.txt"
     *            ^---------------------^
     * </pre>
     *
     * @param fileName The file name
     * @param baseCT The base content type; e.g <tt>"application/octet-stream"</tt> or <tt>"text/plain"</tt>
     * @param userAgent The user agent identifier
     * @param appendTo The {@link StringBuilder} instance to append to
     */
    public static void appendFilenameParameter(final String fileName, final String baseCT, final String userAgent, final StringBuilder appendTo) {
        if (null == fileName) {
            appendTo.append("; filename=\"").append(DEFAULT_FILENAME).append('"');
            return;
        }
        String fn = fileName;
        if ((null != baseCT) && (null == getFileExtension(fn))) {
            if (baseCT.regionMatches(true, 0, MIME_TEXT_PLAIN, 0, MIME_TEXT_PLAIN.length()) && !fileName.toLowerCase(Locale.US).endsWith(".txt")) {
                fn += ".txt";
            } else if (baseCT.regionMatches(true, 0, MIME_TEXT_HTML, 0, MIME_TEXT_HTML.length()) && !fileName.toLowerCase(Locale.US).endsWith(".html")) {
                fn += ".html";
            }
        }
        fn = escapeBackslashAndQuote(fn);
        if (null != userAgent && new BrowserDetector(userAgent).isMSIE()) {
            // InternetExplorer
            appendTo.append("; filename=\"").append(Helper.encodeFilenameForIE(fn, Charsets.UTF_8)).append('"');
            return;
        }
        /*-
         * On socket layer characters are casted to byte values.
         *
         * See AJPv13Response.writeString():
         * sink.write((byte) chars[i]);
         *
         * Therefore ensure we have a one-character-per-byte charset, as it is with ISO-8859-1
         */
        String foo = new String(fn.getBytes(Charsets.UTF_8), Charsets.ISO_8859_1);
        final boolean isAndroid = (null != userAgent && toLowerCase(userAgent).indexOf("android") >= 0);
        if (isAndroid) {
            // myfile.dat => myfile.DAT
            final int pos = foo.lastIndexOf('.');
            if (pos >= 0) {
                foo = foo.substring(0, pos) + toUpperCase(foo.substring(pos));
            }
        } else {
            appendTo.append("; filename*=UTF-8''").append(URLCoder.encode(fn));
        }
        appendTo.append("; filename=\"").append(foo).append('"');
    }

    /**
     * Appends the <tt>"filename"</tt> parameter to specified {@link StringBuilder} instance; e.g.
     *
     * <pre>
     * "attachment; filename="readme.txt"
     *            ^---------------------^
     * </pre>
     *
     * @param fileName The file name
     * @param userAgent The user agent identifier
     * @param appendTo The {@link com.openexchange.java.StringAllocator} instance to append to
     */
    public static void appendFilenameParameter(final String fileName, final String userAgent, final com.openexchange.java.StringAllocator appendTo) {
        appendFilenameParameter(fileName, null, userAgent, appendTo);
    }

    /**
     * Appends the <tt>"filename"</tt> parameter to specified {@link StringBuilder} instance; e.g.
     *
     * <pre>
     * "attachment; filename="readme.txt"
     *            ^---------------------^
     * </pre>
     *
     * @param fileName The file name
     * @param baseCT The base content type; e.g <tt>"application/octet-stream"</tt> or <tt>"text/plain"</tt>
     * @param userAgent The user agent identifier
     * @param appendTo The {@link com.openexchange.java.StringAllocator} instance to append to
     */
    public static void appendFilenameParameter(final String fileName, final String baseCT, final String userAgent, final com.openexchange.java.StringAllocator appendTo) {
        if (null == fileName) {
            appendTo.append("; filename=\"").append(DEFAULT_FILENAME).append('"');
            return;
        }
        String fn = fileName;
        if ((null != baseCT) && (null == getFileExtension(fn))) {
            if (baseCT.regionMatches(true, 0, MIME_TEXT_PLAIN, 0, MIME_TEXT_PLAIN.length())) {
                fn += ".txt";
            } else if (baseCT.regionMatches(true, 0, MIME_TEXT_HTML, 0, MIME_TEXT_HTML.length())) {
                fn += ".html";
            }
        }
        fn = escapeBackslashAndQuote(fn);
        if (null != userAgent && new BrowserDetector(userAgent).isMSIE()) {
            // InternetExplorer
            appendTo.append("; filename=\"").append(Helper.encodeFilenameForIE(fn, Charsets.UTF_8)).append('"');
            return;
        }
        /*-
         * On socket layer characters are casted to byte values.
         *
         * See AJPv13Response.writeString():
         * sink.write((byte) chars[i]);
         *
         * Therefore ensure we have a one-character-per-byte charset, as it is with ISO-8859-1
         */
        String foo = new String(fn.getBytes(Charsets.UTF_8), Charsets.ISO_8859_1);
        final boolean isAndroid = (null != userAgent && toLowerCase(userAgent).indexOf("android") >= 0);
        if (isAndroid) {
            // myfile.dat => myfile.DAT
            final int pos = foo.lastIndexOf('.');
            if (pos >= 0) {
                foo = foo.substring(0, pos) + toUpperCase(foo.substring(pos));
            }
        } else {
            appendTo.append("; filename*=UTF-8''").append(URLCoder.encode(fn));
        }
        appendTo.append("; filename=\"").append(foo).append('"');
    }

    private static final Pattern PAT_BSLASH = Pattern.compile("\\\\");

    private static final Pattern PAT_QUOTE = Pattern.compile("\"");

    private static String escapeBackslashAndQuote(final String str) {
        return PAT_QUOTE.matcher(PAT_BSLASH.matcher(str).replaceAll("\\\\\\\\")).replaceAll("\\\\\\\"");
    }

    private static CheckedDownload asAttachment(final InputStream inputStream, final String preparedFileName, final long size) {
        /*
         * We are supposed to offer attachment for download. Therefore enforce application/octet-stream and attachment disposition.
         */
        return new CheckedDownload(MIME_APPL_OCTET, new com.openexchange.java.StringAllocator(64).append("attachment; filename=\"").append(
            preparedFileName).append('"').toString(), inputStream, size);
    }

    // private static final Pattern P = Pattern.compile("^[\\w\\d\\:\\/\\.]+(\\.\\w{3,4})$");

    /**
     * Checks if specified file name has a trailing file extension.
     *
     * @param fileName The file name
     * @return The extension (e.g. <code>".txt"</code>) or <code>null</code>
     */
    private static String getFileExtension(final String fileName) {
        if (null == fileName) {
            return null;
        }
        final int pos = fileName.lastIndexOf('.');
        if ((pos <= 0) || (pos >= fileName.length())) {
            return null;
        }
        return fileName.substring(pos);
    }

    private static String addFileExtension(final String fileName, final String ext) {
        if (null == fileName) {
            return null;
        }
        final int pos = fileName.indexOf('.');
        if (-1 == pos) {
            return new com.openexchange.java.StringAllocator(fileName).append('.').append(ext).toString();
        }
        return new com.openexchange.java.StringAllocator(fileName.substring(0, pos)).append('.').append(ext).toString();
    }

    private static final String DEFAULT_FILENAME = "file.dat";

    private static final String MIME_TEXT_PLAIN = "text/plain";

    private static final String MIME_TEXT_HTML = "text/htm";

    /**
     * Gets a safe form (as per RFC 2047) for specified file name.
     * <p>
     * {@link BrowserDetector} may be used to parse browser and/or platform identifier from <i>"user-agent"</i> header.
     *
     * @param fileName The file name
     * @param internetExplorer <code>true</code> if <i>"user-agent"</i> header indicates to be Internet Explorer on a Windows platform;
     *            otherwise <code>false</code>
     * @param baseCT The (optional) base content type
     * @return A safe form (as per RFC 2047) for specified file name
     * @see BrowserDetector
     */
    public static final String getSaveAsFileName(final String fileName, final boolean internetExplorer, final String baseCT) {
        if (null == fileName) {
            return DEFAULT_FILENAME;
        }
        final StringBuilder tmp = new StringBuilder(32);
        try {
            if (fileName.indexOf(' ') >= 0) {
                tmp.append(Helper.encodeFilename(fileName.replaceAll(" ", "_"), "UTF-8", internetExplorer));
            } else {
                tmp.append(Helper.encodeFilename(fileName, "UTF-8", internetExplorer));
            }
        } catch (final UnsupportedEncodingException e) {
            LOG.error(e.getMessage(), e);
            return fileName;
        }
        if ((null != baseCT) && (null == getFileExtension(fileName))) {
            if (baseCT.regionMatches(true, 0, MIME_TEXT_PLAIN, 0, MIME_TEXT_PLAIN.length()) && !fileName.toLowerCase(Locale.US).endsWith(".txt")) {
                tmp.append(".txt");
            } else if (baseCT.regionMatches(true, 0, MIME_TEXT_HTML, 0, MIME_TEXT_HTML.length()) && !fileName.toLowerCase(Locale.US).endsWith(".html")) {
                tmp.append(".html");
            }
        }
        return tmp.toString();
    }

    /**
     * {@link CheckedDownload} - Represents a checked download as a result of <tt>DownloadUtility.checkInlineDownload()</tt>.
     */
    public static final class CheckedDownload {

        private final String contentType;
        private final String contentDisposition;
        private final InputStream inputStream;
        private final long size;

        CheckedDownload(final String contentType, final String contentDisposition, final InputStream inputStream, final long size) {
            super();
            this.contentType = contentType;
            this.contentDisposition = contentDisposition;
            this.inputStream = inputStream;
            this.size = size;
        }

        /**
         * Gets the size
         *
         * @return The size
         */
        public long getSize() {
            return size;
        }

        /**
         * Gets the content type.
         *
         * @return The content type
         */
        public String getContentType() {
            return contentType;
        }

        /**
         * Gets the content disposition.
         *
         * @return The content disposition
         */
        public String getContentDisposition() {
            return contentDisposition;
        }

        /**
         * Gets the input stream.
         *
         * @return The input stream
         */
        public InputStream getInputStream() {
            return inputStream;
        }
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

    /** ASCII-wise to upper-case */
    private static String toUpperCase(final CharSequence chars) {
        if (null == chars) {
            return null;
        }
        final int length = chars.length();
        final StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            final char c = chars.charAt(i);
            builder.append((c >= 'a') && (c <= 'z') ? (char) (c & 0x5f) : c);
        }
        return builder.toString();
    }

}
