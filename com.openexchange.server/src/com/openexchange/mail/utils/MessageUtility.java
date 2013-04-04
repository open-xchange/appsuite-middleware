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

package com.openexchange.mail.utils;

import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.activation.DataContentHandler;
import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.HeaderTokenizer;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.java.CharsetDetector;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.StringAllocator;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.datasource.DataContentHandlerDataSource;
import com.openexchange.mail.mime.datasource.StreamDataSource.InputStreamProvider;

/**
 * {@link MessageUtility} - Provides various helper methods for message processing.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MessageUtility {

    private static final String STR_EMPTY = "";

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(MessageUtility.class));

    private static final boolean DEBUG = LOG.isDebugEnabled();

    private static abstract class AbstractInputStreamProvider implements InputStreamProvider {

        protected AbstractInputStreamProvider() {
            super();
        }

        @Override
        public String getName() {
            return null;
        }

    }

    /**
     * No instantiation.
     */
    private MessageUtility() {
        super();
    }

    /**
     * Gets a valid charset-encoding for specified textual part; meaning its content type matches <code>text/&#42;</code>.
     * 
     * @param p The part to detect a charset for
     * @param ct The part's content type
     * @return A valid charset-encoding for specified textual part.
     * @throws OXException If part's input stream cannot be obtained
     */
    public static String checkCharset(final MailPart p, final ContentType ct) throws OXException {
        String cs = ct.getCharsetParameter();
        if (!CharsetDetector.isValid(cs)) {
            com.openexchange.java.StringAllocator sb = null;
            if (cs != null) {
                sb = new com.openexchange.java.StringAllocator(64).append("Illegal or unsupported encoding: \"").append(cs).append("\".");
                mailInterfaceMonitor.addUnsupportedEncodingExceptions(cs);
            }
            cs = CharsetDetector.detectCharset(p.getInputStream());
            if (null != sb && LOG.isWarnEnabled()) {
                sb.append(" Using auto-detected encoding: \"").append(cs).append('"');
                LOG.warn(sb.toString());
            }
        }
        return cs;
    }

    /**
     * Gets a valid charset-encoding for specified textual part; meaning its content type matches <code>text/&#42;</code>.
     * 
     * @param p The part to detect a charset for
     * @param ct The part's content type
     * @return A valid charset-encoding for specified textual part.
     */
    public static String checkCharset(final Part p, final ContentType ct) {
        String cs = ct.getCharsetParameter();
        if (!CharsetDetector.isValid(cs)) {
            com.openexchange.java.StringAllocator sb = null;
            if (cs != null) {
                sb = new com.openexchange.java.StringAllocator(64).append("Illegal or unsupported encoding: \"").append(cs).append("\".");
                mailInterfaceMonitor.addUnsupportedEncodingExceptions(cs);
            }
            cs = CharsetDetector.detectCharset(getPartInputStream(p));
            if (null != sb && LOG.isWarnEnabled()) {
                sb.append(" Using auto-detected encoding: \"").append(cs).append('"');
                LOG.warn(sb.toString());
            }
        }
        return cs;
    }

    /**
     * Gets the input stream of specified part.
     * 
     * @param p The part whose input stream shall be returned
     * @return The part's input stream.
     */
    public static InputStream getPartInputStream(final Part p) {
        InputStream tmp = null;
        try {
            tmp = p.getInputStream();
            tmp.read();
            return p.getInputStream();
        } catch (final IOException e) {
            return getPartRawInputStream(p);
        } catch (final MessagingException e) {
            return getPartRawInputStream(p);
        } finally {
            Streams.close(tmp);
        }
    }

    private static InputStream getPartRawInputStream(final Part p) {
        /*
         * Try to get raw input stream
         */
        if (p instanceof MimeBodyPart) {
            try {
                return ((MimeBodyPart) p).getRawInputStream();
            } catch (final MessagingException e1) {
                return null;
            }
        }
        if (p instanceof MimeMessage) {
            try {
                return ((MimeMessage) p).getRawInputStream();
            } catch (final MessagingException e1) {
                return null;
            }
        }
        /*
         * Neither a MimeBodyPart nor a MimeMessage
         */
        return null;
    }

    /**
     * Reads the string out of MIME part's input stream. On first try the input stream retrieved by
     * <code>javax.mail.Part.getInputStream()</code> is used. If an I/O error occurs (<code>java.io.IOException</code>) then the next try is
     * with part's raw input stream. If everything fails an empty string is returned.
     * 
     * @param p The <code>javax.mail.Part</code> object
     * @param ct The part's content type
     * @return The string read from part's input stream or the empty string "" if everything failed
     * @throws MessagingException If an error occurs in part's getter methods
     */
    public static String readMimePart(final Part p, final ContentType ct) throws MessagingException {
        /*
         * Use specified charset if available else use default one
         */
        String charset = ct.getCharsetParameter();
        if (null == charset) {
            charset = ServerConfig.getProperty(ServerConfig.Property.DefaultEncoding);
        }
        return readMimePart(p, charset);
    }

    /**
     * Reads the string out of MIME part's input stream. On first try the input stream retrieved by
     * <code>javax.mail.Part.getInputStream()</code> is used. If an I/O error occurs (<code>java.io.IOException</code>) then the next try is
     * with part's raw input stream. If everything fails an empty string is returned.
     * 
     * @param p The <code>javax.mail.Part</code> object
     * @param charset The charset
     * @return The string read from part's input stream or the empty string "" if everything failed
     * @throws MessagingException If an error occurs in part's getter methods
     */
    public static String readMimePart(final Part p, final String charset) throws MessagingException {
        try {
            final InputStreamProvider streamProvider = new AbstractInputStreamProvider() {

                @Override
                public InputStream getInputStream() throws IOException {
                    try {
                        return p.getInputStream();
                    } catch (final MessagingException e) {
                        throw new IOException(e.getMessage(), e);
                    }
                }
            };
            return readStream(streamProvider, charset);
        } catch (final IOException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof MessagingException) {
                throw (MessagingException) cause;
            }
            /*
             * Try to get data from raw input stream
             */
            final InputStreamProvider streamProvider;
            if (p instanceof MimeBodyPart) {
                streamProvider = new AbstractInputStreamProvider() {

                    @Override
                    public InputStream getInputStream() throws IOException {
                        try {
                            return ((MimeBodyPart) p).getRawInputStream();
                        } catch (final MessagingException e) {
                            throw new IOException(e.getMessage(), e);
                        }
                    }
                };
            } else if (p instanceof MimeMessage) {
                streamProvider = new AbstractInputStreamProvider() {

                    @Override
                    public InputStream getInputStream() throws IOException {
                        try {
                            return ((MimeMessage) p).getRawInputStream();
                        } catch (final MessagingException e) {
                            throw new IOException(e.getMessage(), e);
                        }
                    }
                };
            } else {
                /*
                 * Neither a MimeBodyPart nor a MimeMessage
                 */
                return STR_EMPTY;
            }
            try {
                return readStream(streamProvider, charset);
            } catch (final IOException e1) {
                LOG.error(e1.getMessage(), e1);
                return STR_EMPTY;
            }
        }
    }

    /**
     * Reads the stream content from given mail part.
     * 
     * @param mailPart The mail part
     * @param charset The charset encoding used to generate a {@link String} object from raw bytes
     * @return the <code>String</code> read from mail part's stream
     * @throws IOException
     */
    public static String readMailPart(final MailPart mailPart, final String charset) throws IOException, OXException {
        final InputStreamProvider streamProvider = new AbstractInputStreamProvider() {

            @Override
            public InputStream getInputStream() throws IOException {
                try {
                    return mailPart.getInputStream();
                } catch (final OXException e) {
                    throw new IOException(e.getMessage(), e);
                }
            }
        };
        try {
            return readStream(streamProvider, charset);
        } catch (final IOException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof OXException) {
                throw (OXException) cause;
            }
            throw e;
        }
    }

    /**
     * Reads a string from given input stream using direct buffering.
     * 
     * @param bytes The bytes to read
     * @param charset The charset
     * @return The <code>String</code> read from input stream
     * @throws IOException If an I/O error occurs
     */
    public static String readBytes(final byte[] bytes, final String charset) throws IOException {
        final InputStreamProvider streamProvider = new AbstractInputStreamProvider() {

            @Override
            public InputStream getInputStream() throws IOException {
                return Streams.newByteArrayInputStream(bytes);
            }
        };
        return readStream(streamProvider, charset);
    }

    private static final int BUFSIZE = 8192; // 8K

    /**
     * The unknown character: <code>'&#65533;'</code>
     */
    public static final char UNKNOWN = '\ufffd';

    /**
     * Reads a string from given input stream using direct buffering.
     * 
     * @param streamProvider The input stream provider
     * @param charset The charset
     * @return The <code>String</code> read from input stream
     * @throws IOException If an I/O error occurs
     */
    public static String readStream(final InputStreamProvider streamProvider, final String charset) throws IOException {
        if (null == streamProvider) {
            return STR_EMPTY;
        }
        if (isBig5(charset)) {
            /*
             * Special treatment for possible BIG5 encoded stream
             */
            return readBig5Bytes(getBytesFrom(streamProvider.getInputStream()));
        }
        if ("GB18030".equalsIgnoreCase(charset)) {
            /*
             * Special treatment for possible GB18030 encoded stream
             */
            return readGB18030Bytes(getBytesFrom(streamProvider.getInputStream()));
        }
        if (isGB2312(charset)) {
            /*
             * Special treatment for possible GB2312 encoded stream
             */
            final byte[] bytes = getBytesFrom(streamProvider.getInputStream());
            if (bytes.length == 0) {
                return STR_EMPTY;
            }
            String retval = new String(bytes, "GB2312");
            if (retval.indexOf(UNKNOWN) < 0) {
                return retval;
            }
            retval = new String(bytes, "GB18030");
            if (retval.indexOf(UNKNOWN) < 0) {
                return retval;
            }
            /*
             * Detect the charset
             */
            final String detectedCharset = CharsetDetector.detectCharset(Streams.newByteArrayInputStream(bytes));
            if (DEBUG) {
                LOG.debug("Mapped \"GB2312\" charset to \"" + detectedCharset + "\".");
            }
            if (isBig5(detectedCharset)) {
                return readBig5Bytes(bytes);
            }
            return new String(bytes, detectedCharset);
        }
        final String retval = readStream0(streamProvider.getInputStream(), charset);
        if (true || retval.indexOf(UNKNOWN) < 0) {
            return retval;
        }
        final byte[] bytes = getBytesFrom(streamProvider.getInputStream());
        final String detectedCharset = CharsetDetector.detectCharset(Streams.newByteArrayInputStream(bytes));
        if (DEBUG) {
            LOG.debug("Mapped \"" + charset + "\" charset to \"" + detectedCharset + "\".");
        }
        return new String(bytes, detectedCharset);
    }

    /**
     * Reads a string from given input stream using direct buffering.
     * 
     * @param inStream The input stream
     * @param charset The charset
     * @return The <code>String</code> read from input stream
     * @throws IOException If an I/O error occurs
     */
    public static String readStream(final InputStream inStream, final String charset) throws IOException {
        if (null == inStream) {
            return STR_EMPTY;
        }
        if (isBig5(charset)) {
            /*
             * Special treatment for possible BIG5 encoded stream
             */
            return readBig5Bytes(getBytesFrom(inStream));
        }
        if ("GB18030".equalsIgnoreCase(charset)) {
            /*
             * Special treatment for possible GB18030 encoded stream
             */
            return readGB18030Bytes(getBytesFrom(inStream));
        }
        if (isGB2312(charset)) {
            /*
             * Special treatment for possible GB2312 encoded stream
             */
            final byte[] bytes = getBytesFrom(inStream);
            if (bytes.length == 0) {
                return STR_EMPTY;
            }
            String retval = new String(bytes, Charsets.forName("GB2312"));
            if (retval.indexOf(UNKNOWN) < 0) {
                return retval;
            }
            retval = new String(bytes, Charsets.forName("GB18030"));
            if (retval.indexOf(UNKNOWN) < 0) {
                return retval;
            }
            /*
             * Detect the charset
             */
            final String detectedCharset = CharsetDetector.detectCharset(Streams.newByteArrayInputStream(bytes));
            if (DEBUG) {
                LOG.debug("Mapped \"GB2312\" charset to \"" + detectedCharset + "\".");
            }
            if (isBig5(detectedCharset)) {
                return readBig5Bytes(bytes);
            }
            return new String(bytes, Charsets.forName(detectedCharset));
        }
        return readStream0(inStream, charset);
    }

    private static String readGB18030Bytes(final byte[] bytes) throws Error {
        if (bytes.length == 0) {
            return STR_EMPTY;
        }
        final String retval = new String(bytes, Charsets.forName("GB18030"));
        if (retval.indexOf(UNKNOWN) < 0) {
            return retval;
        }
        /*
         * Detect the charset
         */
        final String detectedCharset = CharsetDetector.detectCharset(Streams.newByteArrayInputStream(bytes));
        if (DEBUG) {
            LOG.debug("Mapped \"GB18030\" charset to \"" + detectedCharset + "\".");
        }
        if (isBig5(detectedCharset)) {
            return readBig5Bytes(bytes);
        }
        return new String(bytes, Charsets.forName(detectedCharset));
    }

    private static String readBig5Bytes(final byte[] bytes) throws Error {
        if (bytes.length == 0) {
            return STR_EMPTY;
        }
        final String retval = new String(bytes, Charsets.forName("big5"));
        if (retval.indexOf(UNKNOWN) < 0) {
            return retval;
        }
        /*
         * Expect the charset to be Big5-HKSCS
         */
        try {
            return new String(bytes, Charsets.forName("Big5-HKSCS"));
        } catch (final Error error) {
            // Huh..?
            final Throwable cause = error.getCause();
            if ((cause instanceof java.io.CharConversionException) || (cause instanceof java.nio.charset.CharacterCodingException)) {
                /*
                 * Retry with auto-detected charset
                 */
                return new String(bytes, Charsets.forName(CharsetDetector.detectCharset(Streams.newByteArrayInputStream(bytes))));
            }
            throw error;
        }
    }

    private static String readStream0(final InputStream inStream, final String charset) throws IOException {
        if (null == inStream) {
            return STR_EMPTY;
        }
        try {
            final ByteArrayOutputStream tmp = Streams.newByteArrayOutputStream(BUFSIZE << 1);
            final byte[] buf = new byte[BUFSIZE];
            for (int read; (read = inStream.read(buf, 0, BUFSIZE)) > 0;) {
                tmp.write(buf, 0, read);
            }
            try {
                return tmp.toString(charset);
            } catch (final UnsupportedCharsetException e) {
                LOG.error("Unsupported encoding in a message detected and monitored: \"" + charset + '"', e);
                mailInterfaceMonitor.addUnsupportedEncodingExceptions(charset);
                final byte[] bytes = tmp.toByteArray();
                return new String(bytes, Charsets.forName(detectCharset(bytes)));
            } catch (final Error error) {
                final Throwable cause = error.getCause();
                if ((cause instanceof java.io.CharConversionException) || (cause instanceof java.nio.charset.CharacterCodingException)) {
                    final byte[] bytes = tmp.toByteArray();
                    return new String(bytes, Charsets.forName(detectCharset(bytes)));
                }
                throw error;
            }
        } catch (final IOException e) {
            if ("No content".equals(e.getMessage())) {
                /*-
                 * Special JavaMail I/O error to indicate no content available from IMAP server.
                 * Return the empty string in this case.
                 */
                return STR_EMPTY;
            }
            throw e;
        } finally {
            Streams.close(inStream);
        }
    }

    private static String detectCharset(final byte[] bytes) {
        String charset = CharsetDetector.detectCharset(Streams.newByteArrayInputStream(bytes));
        if ("US-ASCII".equalsIgnoreCase(charset)) {
            charset = "ISO-8859-1";
        }
        return charset;
    }

    private static final Pattern PATTERN_BIG5 = Pattern.compile("[-_]+");

    private static final String BIG5 = "big5";

    private static final String BIGFIVE = "bigfive";

    /**
     * Checks if specified charset name can be considered as BIG5.
     * 
     * @param charset The charset name to check
     * @return <code>true</code> if charset name can be considered as BIG5; otherwise <code>false</code>
     */
    public static boolean isBig5(final String charset) {
        if (null == charset) {
            return false;
        }
        final String lc = charset.toLowerCase(Locale.US);
        if (!lc.startsWith("big", 0)) {
            return false;
        }
        final String wo = PATTERN_BIG5.matcher(lc).replaceAll("");
        return BIG5.equals(wo) || BIGFIVE.equals(wo);
    }

    private static final String GB2312 = "gb2312";

    /**
     * Checks if specified charset name can be considered as GB2312.
     * 
     * @param charset The charset name to check
     * @return <code>true</code> if charset name can be considered as GB2312; otherwise <code>false</code>
     */
    public static boolean isGB2312(final String charset) {
        if (null == charset) {
            return false;
        }
        return GB2312.equals(charset.toLowerCase(Locale.US));
    }

    /**
     * Gets the byte content from specified input stream.
     * 
     * @param in The input stream to get the byte content from
     * @return The byte content
     * @throws IOException If an I/O error occurs
     */
    public static byte[] getBytesFrom(final InputStream in) throws IOException {
        if (null == in) {
            return new byte[0];
        }
        try {
            final byte[] buf = new byte[BUFSIZE];
            int len = 0;
            if ((len = in.read(buf, 0, buf.length)) <= 0) {
                return new byte[0];
            }
            final ByteArrayOutputStream out = Streams.newByteArrayOutputStream(BUFSIZE);
            do {
                out.write(buf, 0, len);
            } while ((len = in.read(buf, 0, buf.length)) > 0);
            return out.toByteArray();
        } catch (final IOException e) {
            if ("No content".equals(e.getMessage())) {
                /*-
                 * Special JavaMail I/O error to indicate no content available from IMAP server.
                 * Return an empty array in this case.
                 */
                return new byte[0];
            }
            throw e;
        } finally {
            Streams.close(in);
        }
    }

    /**
     * Check if specified bytes contain ascii-only content.
     * 
     * @param bytes The bytes to check
     * @return <code>true</code> if bytes are ascii-only; otherwise <code>false</code>
     */
    public static boolean isAscii(final byte[] bytes) {
        if (null == bytes || 0 == bytes.length) {
            return true;
        }
        final int len = bytes.length;
        boolean isAscci = true;
        for (int i = 0; (i < len) && isAscci; i++) {
            isAscci = bytes[i] >= 0;
        }
        return isAscci;
    }

    private static final int THRESHOLD = 25;

    /**
     * Detects possible duplicate &lt;html&gt; tags and removes all but last.
     * 
     * @param html The HTML content
     * @return The HTML content with duplicate &lt;html&gt; tags removed
     */
    public static String simpleHtmlDuplicateRemoval(final String html) {
        if (isEmpty(html)) {
            return html;
        }
        final String lc = html.toLowerCase();
        final String sub = "<html>";
        {
            int count = 0;
            int idx = 0;
            final int subLen = sub.length();
            while ((idx = lc.indexOf(sub, idx)) >= 0) {
                count++;
                idx += subLen;
            }
            if (count <= THRESHOLD) {
                return html;
            }
        }
        // Threshold exceeded
        int pos = lc.lastIndexOf(sub);
        if (pos > 0) {
            pos = lc.lastIndexOf(sub, pos - 1);
            if (pos >= 0) {
                return html.substring(pos + 6);
            }
        }
        return html;
    }

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

    private static final com.sun.mail.handlers.multipart_mixed DCH_MULTIPART = new com.sun.mail.handlers.multipart_mixed();
    private static final com.sun.mail.handlers.message_rfc822 DCH_MESSAGE = new com.sun.mail.handlers.message_rfc822();
    private static final com.sun.mail.handlers.text_plain DCH_TEXT_PLAIN = new com.sun.mail.handlers.text_plain();
    private static final com.sun.mail.handlers.text_html DCH_TEXT_HTML = new com.sun.mail.handlers.text_html();
    private static final com.sun.mail.handlers.text_xml DCH_TEXT_XML = new com.sun.mail.handlers.text_xml();

    private static DataContentHandler dchFor(final String subtype) {
        if (subtype.startsWith("plain")) {
            return DCH_TEXT_PLAIN;
        } else if (subtype.startsWith("htm")) {
            return DCH_TEXT_HTML;
        } else if (subtype.startsWith("xml")) {
            return DCH_TEXT_XML;
        }
        return null;
    }

    /**
     * Sets the message content
     * 
     * @param message The message to set
     * @param part The part
     * @throws MessagingException If setting content fails
     */
    public static void setContent(final Message message, final Part part) throws MessagingException {
        if (null == message || null == part) {
            return;
        }
        part.setDataHandler(new DataHandler(new DataContentHandlerDataSource(message, "message/rfc822", DCH_MESSAGE)));
    }

    /**
     * Sets the multipart content
     * 
     * @param multipart The multipart to set
     * @param part The part
     * @throws MessagingException If setting content fails
     */
    public static void setContent(final Multipart multipart, final Part part) throws MessagingException {
        if (null == multipart || null == part) {
            return;
        }
        part.setDataHandler(new DataHandler(new DataContentHandlerDataSource(multipart, multipart.getContentType(), DCH_MULTIPART)));
    }

    /**
     * Sets the multipart content
     * 
     * @param multipart The multipart to set
     * @param contentType The content type
     * @param part The part
     * @throws MessagingException If setting content fails
     */
    public static void setContent(final Multipart multipart, final String contentType, final Part part) throws MessagingException {
        if (null == multipart || null == part) {
            return;
        }
        part.setDataHandler(new DataHandler(new DataContentHandlerDataSource(multipart, contentType, DCH_MULTIPART)));
    }

    /**
     * Convenience method that sets the given String as this part's content, with a primary MIME type of "text/plain; charset=us-ascii".
     * 
     * @param text The text content to set
     * @param charset The charset to use for the text
     * @exception MessagingException If an error occurs
     */
    public static void setText(final String text, final Part part) throws MessagingException {
        setText(text, null, null, part);
    }

    /**
     * Convenience method that sets the given String as this part's content, with a primary MIME type of "text/plain".
     * <p>
     * The given Unicode string will be charset-encoded using the specified charset. The charset is also used to set the "charset"
     * parameter.
     * 
     * @param text The text content to set
     * @param charset The charset to use for the text
     * @exception MessagingException If an error occurs
     */
    public static void setText(final String text, final String charset, final Part part) throws MessagingException {
        setText(text, charset, null, part);
    }

    /**
     * Convenience method that sets the given String as this part's content, with a primary MIME type of "text" and the specified MIME
     * subtype.
     * <p>
     * The given Unicode string will be charset-encoded using the specified charset. The charset is also used to set the "charset"
     * parameter.
     * 
     * @param text The text content to set
     * @param charset The charset to use for the text
     * @param subtype The MIME subtype to use (e.g., "html")
     * @exception MessagingException If an error occurs
     */
    public static void setText(final String text, final String charset, final String subtype, final Part part) throws MessagingException {
        if (null == text || null == part) {
            return;
        }
        final String st = null == subtype ? "plain" : subtype;
        final String objectMimeType =
            new StringAllocator(32).append("text/").append(st).append("; charset=").append(
                MimeUtility.quote(null == charset ? "us-ascii" : charset, HeaderTokenizer.MIME)).toString();
        final DataContentHandlerDataSource ds = new DataContentHandlerDataSource(text, objectMimeType, dchFor(toLowerCase(st)));
        part.setDataHandler(new DataHandler(ds));
        part.setHeader(MessageHeaders.HDR_CONTENT_TYPE, objectMimeType);
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

}
