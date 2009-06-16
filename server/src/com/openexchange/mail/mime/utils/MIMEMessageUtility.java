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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.mail.mime.utils;

import static com.openexchange.mail.MailServletInterface.mailInterfaceMonitor;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParseException;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.image.ImageService;
import com.openexchange.image.internal.ImageData;
import com.openexchange.mail.MailException;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.HeaderName;
import com.openexchange.mail.mime.MIMETypes;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.PlainTextAddress;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;
import com.sun.mail.imap.protocol.BODYSTRUCTURE;

/**
 * {@link MIMEMessageUtility} - Utilities for MIME messages.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MIMEMessageUtility {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(MIMEMessageUtility.class);

    private static final Set<HeaderName> ENCODINGS;

    static {
        final Set<HeaderName> tmp = new HashSet<HeaderName>(4);
        tmp.add(HeaderName.valueOf("iso-8859-1"));
        tmp.add(HeaderName.valueOf("windows-1258"));
        tmp.add(HeaderName.valueOf("UTF-8"));
        tmp.add(HeaderName.valueOf("us-ascii"));
        ENCODINGS = java.util.Collections.unmodifiableSet(tmp);
    }

    /**
     * No instantiation
     */
    private MIMEMessageUtility() {
        super();
    }

    private static final Pattern PATTERN_EMBD_IMG = Pattern.compile(
        "(<img[^>]+src=\"?cid:)([^\"]+)(\"?[^>]*/?>)",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern PATTERN_EMBD_IMG_ALT = Pattern.compile(
        "(<img[^>]+src=\"?)([0-9a-z&&[^.\\s>\"]]+\\.[0-9a-z&&[^.\\s>\"]]+)(\"?[^>]*/?>)",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * Detects if given HTML content contains inlined images
     * <p>
     * Example:
     * 
     * <pre>
     * &lt;img src=&quot;cid:s345asd845@12drg&quot;&gt;
     * </pre>
     * 
     * @param htmlContent The HTML content
     * @return <code>true</code> if given HTML content contains inlined images; otherwise <code>false</code>
     */
    public static boolean hasEmbeddedImages(final String htmlContent) {
        return PATTERN_EMBD_IMG.matcher(htmlContent).find() || PATTERN_EMBD_IMG_ALT.matcher(htmlContent).find();
    }

    /**
     * Gathers all occurring content IDs in HTML content and returns them as a list
     * 
     * @param htmlContent The HTML content
     * @return an instance of <code>{@link List}</code> containing all occurring content IDs
     */
    public static List<String> getContentIDs(final String htmlContent) {
        final List<String> retval = new ArrayList<String>();
        Matcher m = PATTERN_EMBD_IMG.matcher(htmlContent);
        while (m.find()) {
            retval.add(m.group(2));
        }
        m = PATTERN_EMBD_IMG_ALT.matcher(htmlContent);
        while (m.find()) {
            retval.add(m.group(2));
        }
        return retval;
    }

    /**
     * Compares (case insensitive) the given values of message header "Content-ID". The leading/trailing characters '<code>&lt;</code>' and
     * ' <code>&gt;</code>' are ignored during comparison
     * 
     * @param contentId1 The first content ID
     * @param contentId2 The second content ID
     * @return <code>true</code> if both are equal; otherwise <code>false</code>
     */
    public static boolean equalsCID(final String contentId1, final String contentId2) {
        if (null != contentId1 && null != contentId2) {
            final String cid1 = contentId1.length() > 0 && contentId1.charAt(0) == '<' ? contentId1.substring(1, contentId1.length() - 1) : contentId1;
            final String cid2 = contentId2.length() > 0 && contentId2.charAt(0) == '<' ? contentId2.substring(1, contentId2.length() - 1) : contentId2;
            return cid1.equalsIgnoreCase(cid2);
        }
        return false;
    }

    public static final Pattern PATTERN_REF_IMG = Pattern.compile(
        "(<img[^>]*?)(src=\")([^\"]+?)((?:uid=|id=))([^\"&]+)(?:(&[^\"]+\")|(\"))([^>]*/?>)",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * Detects if given HTML content contains references to local image files
     * <ul>
     * <li>Example for an uploaded image file referenced within a composed mail:<br>
     * <code>
     * &nbsp;&nbsp;&lt;img&nbsp;src=&quot;/ajax/file?action=get&amp;session=abcdefg&amp;id=123dfr567zh&quot;&gt;
     * </code></li>
     * <li>Example for a stored image file referenced within a composed mail:<br>
     * <code>
     * &nbsp;&nbsp;&lt;img&nbsp;src=&quot;/ajax/image?uid=12gf356j7&quot;&gt;
     * </code></li>
     * </ul>
     * 
     * @param htmlContent The HTML content
     * @param session The user session
     * @return <code>true</code> if given HTML content contains references to local image files; otherwise <code>false</code>
     */
    public static boolean hasReferencedLocalImages(final String htmlContent, final Session session) {
        final Matcher m = PATTERN_REF_IMG.matcher(htmlContent);
        if (m.find()) {
            final ManagedFileManagement mfm = ServerServiceRegistry.getInstance().getService(ManagedFileManagement.class);
            do {
                final String uid = m.group(5);
                if ("uid=".equals(m.group(4))) {
                    // Touch image
                    touchImage(uid, session);
                } else {
                    if (!mfm.contains(uid)) {
                        touchImage(uid, session);
                    }
                }
            } while (m.find());
            return true;
        }
        return false;
    }

    private static void touchImage(final String uid, final Session session) {
        final ImageService imageService = ServerServiceRegistry.getInstance().getService(ImageService.class);
        ImageData imageData = imageService.getImageData(session, uid);
        if (imageData == null) {
            imageData = imageService.getImageData(session.getContextId(), uid);
        }
        if (imageData != null) {
            imageData.touch();
        }
    }

    /**
     * Determines specified part's real filename if any available.
     * 
     * @param part The part whose filename shall be determined
     * @return The part's real filename or <code>null</code> if none present
     */
    public static String getRealFilename(final MailPart part) {
        if (part.getFileName() != null) {
            return part.getFileName();
        }
        final String hdr = part.getFirstHeader(MessageHeaders.HDR_CONTENT_DISPOSITION);
        if (hdr == null) {
            return getContentTypeFilename(part);
        }
        try {
            final String retval = new ContentDisposition(hdr).getFilenameParameter();
            if (retval == null) {
                return getContentTypeFilename(part);
            }
            return retval;
        } catch (final MailException e) {
            return getContentTypeFilename(part);
        }
    }

    private static final String PARAM_NAME = "name";

    private static String getContentTypeFilename(final MailPart part) {
        if (part.containsContentType()) {
            return part.getContentType().getParameter(PARAM_NAME);
        }
        final String hdr = part.getFirstHeader(MessageHeaders.HDR_CONTENT_TYPE);
        if (hdr == null || hdr.length() == 0) {
            return null;
        }
        try {
            return new ContentType(hdr).getParameter(PARAM_NAME);
        } catch (final MailException e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }

    /*
     * Multipart subtype constants
     */
    private static final String MULTI_SUBTYPE_ALTERNATIVE = "ALTERNATIVE";

    // private static final String MULTI_SUBTYPE_MIXED = "MIXED";

    // private static final String MULTI_SUBTYPE_SIGNED = "SIGNED";

    /**
     * Checks if given multipart contains (file) attachments
     * 
     * @param mp The multipart to examine
     * @param subtype The multipart's subtype
     * @return <code>true</code> if given multipart contains (file) attachments; otherwise <code>false</code>
     * @throws MessagingException If a messaging error occurs
     * @throws MailException If a mail error occurs
     * @throws IOException If an I/O error occurs
     */
    public static boolean hasAttachments(final Multipart mp, final String subtype) throws MessagingException, MailException, IOException {
        if (MULTI_SUBTYPE_ALTERNATIVE.equalsIgnoreCase(subtype)) {
            if (mp.getCount() > 2) {
                return true;
            }
            return hasAttachments0(mp);
        }
        // TODO: Think about special check for multipart/signed
        /*
         * if (MULTI_SUBTYPE_SIGNED.equalsIgnoreCase(subtype)) { if (mp.getCount() > 2) { return true; } return hasAttachments0(mp); }
         */
        if (mp.getCount() > 1) {
            return true;
        }
        return hasAttachments0(mp);
    }

    private static boolean hasAttachments0(final Multipart mp) throws MessagingException, MailException, IOException {
        boolean found = false;
        final int count = mp.getCount();
        final ContentType ct = new ContentType();
        for (int i = 0; i < count && !found; i++) {
            final BodyPart part = mp.getBodyPart(i);
            final String[] tmp = part.getHeader(MessageHeaders.HDR_CONTENT_TYPE);
            if (tmp != null && tmp.length > 0) {
                ct.setContentType(MIMEMessageUtility.unfold(tmp[0]));
            } else {
                ct.setContentType(MIMETypes.MIME_DEFAULT);
            }
            if (ct.isMimeType(MIMETypes.MIME_MULTIPART_ALL)) {
                found |= hasAttachments((Multipart) part.getContent(), ct.getSubType());
            }
        }
        return found;
    }

    /**
     * Checks if given BODYSTRUCTURE item indicates to contain (file) attachments
     * 
     * @param bodystructure The BODYSTRUCTURE item
     * @return <code>true</code> if given BODYSTRUCTURE item indicates to contain (file) attachments; otherwise <code>false</code>
     */
    public static boolean hasAttachments(final BODYSTRUCTURE bodystructure) {
        if (bodystructure.isMulti()) {
            if (MULTI_SUBTYPE_ALTERNATIVE.equalsIgnoreCase(bodystructure.subtype)) {
                if (bodystructure.bodies.length > 2) {
                    return true;
                }
                return hasAttachments0(bodystructure);
            }
            // TODO: Think about special check for multipart/signed
            /*
             * if (MULTI_SUBTYPE_SIGNED.equalsIgnoreCase(bodystructure.subtype)) { if (bodystructure.bodies.length > 2) { return true; }
             * return hasAttachments0(bodystructure); }
             */
            if (bodystructure.bodies.length > 1) {
                return true;
            }
            return hasAttachments0(bodystructure);
        }
        return false;
    }

    private static boolean hasAttachments0(final BODYSTRUCTURE bodystructure) {
        boolean found = false;
        for (int i = 0; (i < bodystructure.bodies.length) && !found; i++) {
            found |= hasAttachments(bodystructure.bodies[i]);
        }
        return found;
    }

    private static final Pattern ENC_PATTERN = Pattern.compile("=\\?(\\S+?)\\?(\\S+?)\\?(.+?)\\?=");

    /**
     * Decodes a multi-mime-encoded header value using the algorithm specified in RFC 2047, Section 6.1.
     * <p>
     * If the charset-conversion fails for any sequence, an {@link UnsupportedEncodingException} is thrown.
     * <p>
     * If the String is not a RFC 2047 style encoded header, it is returned as-is
     * 
     * @param headerValue The possibly encoded header value
     * @return The possibly decoded header value
     */
    public static String decodeMultiEncodedHeader(final String headerValue) {
        if (headerValue == null) {
            return null;
        }
        final String hdrVal = MIMEMessageUtility.unfold(headerValue);
        final Matcher m = ENC_PATTERN.matcher(hdrVal);
        if (m.find()) {
            final StringBuilder sb = new StringBuilder(hdrVal.length());
            int lastMatch = 0;
            do {
                try {
                    sb.append(hdrVal.substring(lastMatch, m.start()));
                    sb.append(MimeUtility.decodeWord(m.group()));
                    lastMatch = m.end();
                } catch (final UnsupportedEncodingException e) {
                    LOG.error("Unsupported character-encoding in encoded-word: " + m.group(), e);
                    sb.append(m.group());
                    lastMatch = m.end();
                } catch (final ParseException e) {
                    return decodeMultiEncodedHeaderSafe(headerValue);
                }
            } while (m.find());
            sb.append(hdrVal.substring(lastMatch));
            return sb.toString();
        }
        return hdrVal;
    }

    /**
     * Decodes a multi-mime-encoded header value using the algorithm specified in RFC 2047, Section 6.1 in a safe manner.
     * 
     * @param headerValue The possibly encoded header value
     * @return The possibly decoded header value
     */
    private static String decodeMultiEncodedHeaderSafe(final String headerValue) {
        if (headerValue == null) {
            return null;
        }
        final String hdrVal = MIMEMessageUtility.unfold(headerValue);
        final Matcher m = ENC_PATTERN.matcher(hdrVal);
        if (m.find()) {
            final StringBuilder sb = new StringBuilder(hdrVal.length());
            StringBuilder tmp = null;
            int lastMatch = 0;
            do {
                try {
                    sb.append(hdrVal.substring(lastMatch, m.start()));
                    final String enc;
                    if ("Q".equals(m.group(2))) {
                        final String charset = m.group(1);
                        final String preparedEWord = prepareQEncodedValue(m.group(3), charset);
                        if (null == tmp) {
                            tmp = new StringBuilder(preparedEWord.length() + 16);
                        } else {
                            tmp.setLength(0);
                        }
                        tmp.append("=?").append(charset).append('?').append('Q').append('?').append(preparedEWord).append("?=");
                        enc = tmp.toString();
                    } else {
                        enc = m.group();
                    }
                    sb.append(MimeUtility.decodeWord(enc));
                    lastMatch = m.end();
                } catch (final UnsupportedEncodingException e) {
                    LOG.error("Unsupported character-encoding in encoded-word: " + m.group(), e);
                    sb.append(m.group());
                    lastMatch = m.end();
                } catch (final ParseException e) {
                    LOG.error("String is not an encoded-word as per RFC 2047: " + m.group(), e);
                    sb.append(m.group());
                    lastMatch = m.end();
                }
            } while (m.find());
            sb.append(hdrVal.substring(lastMatch));
            return sb.toString();
        }
        return hdrVal;
    }

    /**
     * Prepares specified encoded word, thus even corrupt headers are properly handled.<br>
     * Here is an example of such a corrupt header:
     * 
     * <pre>
     * =?windows-1258?Q?foo_bar@mail.foobar.com, _Foo_B=E4r_=28fb@somewhere,
     *  =@unspecified-domain,  =?windows-1258?Q?de=29@mail.foobar.com,
     *  _Jane_Doe@mail.foobar.com, ?=
     * </pre>
     * 
     * @param eword The possibly corrupt encoded word
     * @param charset The charset
     * @return The prepared encoded word which won't cause a {@link ParseException parse error} during decoding
     */
    private static String prepareQEncodedValue(final String eword, final String charset) {
        final int len = eword.length();
        int pos = eword.indexOf('=');
        if (pos == -1) {
            return eword;
        }
        final StringBuilder sb = new StringBuilder(len);
        int prev = 0;
        do {
            final char c1 = eword.charAt(pos + 1);
            final char c2 = eword.charAt(pos + 2);
            final int nextPos;
            if (isHex(c1) && isHex(c2)) {
                nextPos = pos + 3;
                sb.append(eword.substring(prev, nextPos));
            } else {
                nextPos = pos + 1;
                if (ENCODINGS.contains(HeaderName.valueOf(charset))) {
                    sb.append(eword.substring(prev, pos)).append("=3D");
                } else {
                    sb.append(eword.substring(prev, pos)).append(qencode('=', charset));
                }
            }
            prev = nextPos;
            pos = nextPos < len ? eword.indexOf('=', nextPos) : -1;
        } while (pos != -1);
        if (prev < len) {
            sb.append(eword.substring(prev));
        }
        return sb.toString();
    }

    private static boolean isHex(final char c) {
        return ('0' <= c && c <= '9') || ('A' <= c && c <= 'F') || ('a' <= c && c <= 'f');
    }

    private static String qencode(final char toEncode, final String charset) {
        if (!Charset.isSupported(charset)) {
            return String.valueOf(toEncode);
        }
        final StringBuilder retval = new StringBuilder(4);
        try {
            final byte[] bytes = String.valueOf(toEncode).getBytes(charset);
            for (int j = 0; j < bytes.length; j++) {
                retval.append('=').append(Integer.toHexString(bytes[j] & 0xFF).toUpperCase(Locale.ENGLISH));
            }
        } catch (final java.io.UnsupportedEncodingException e) {
            // Cannot occur
            LOG.error(e.getMessage(), e);
        }
        return retval.toString();
    }

    /**
     * Get the decoded filename associated with specified mail part.
     * <p>
     * Returns the value of the "filename" parameter from the "Content-Disposition" header field. If its not available, returns the value of
     * the "name" parameter from the "Content-Type" header field. Returns <code>null</code> if both are absent.
     * 
     * @param mailPart The mail part whose filename shall be returned
     * @return The mail part's decoded filename or <code>null</code>.
     */
    public static String getFileName(final MailPart mailPart) {
        // First look-up content-disposition
        String fileName = mailPart.getContentDisposition().getFilenameParameter();
        if (null == fileName) {
            // Then look-up content-type
            fileName = mailPart.getContentType().getNameParameter();
        }
        return decodeMultiEncodedHeader(fileName);
    }

    /**
     * Parse the given sequence of addresses into InternetAddress objects by invoking
     * <code>{@link InternetAddress#parse(String, boolean)}</code>. If <code>strict</code> is false, simple email addresses separated by
     * spaces are also allowed. If <code>strict</code> is true, many (but not all) of the RFC822 syntax rules are enforced. In particular,
     * even if <code>strict</code> is true, addresses composed of simple names (with no "@domain" part) are allowed. Such "illegal"
     * addresses are not uncommon in real messages.
     * <p>
     * Non-strict parsing is typically used when parsing a list of mail addresses entered by a human. Strict parsing is typically used when
     * parsing address headers in mail messages.
     * <p>
     * Additionally the personal parts are MIME encoded using default MIME charset.
     * 
     * @param addresslist - comma separated address strings
     * @param strict - <code>true</code> to enforce RFC822 syntax; otherwise <code>false</code>
     * @return array of <code>InternetAddress</code> objects
     * @throws AddressException - if parsing fails
     */
    public static InternetAddress[] parseAddressList(final String addresslist, final boolean strict) throws AddressException {
        final String al = replaceWithComma(unfold(addresslist));
        InternetAddress[] addrs = null;
        try {
            addrs = InternetAddress.parse(al, strict);
        } catch (final AddressException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(new StringBuilder(128).append("Internet addresses could not be properly parsed, ").append(
                    "using plain addresses' string representation instead.").toString(), e);
            }
            addrs = PlainTextAddress.getAddresses(al.split(" *, *"));
        }
        try {
            for (int i = 0; i < addrs.length; i++) {
                addrs[i].setPersonal(addrs[i].getPersonal(), MailProperties.getInstance().getDefaultMimeCharset());
            }
        } catch (final UnsupportedEncodingException e) {
            /*
             * Cannot occur since default charset is checked on global mail configuration initialization
             */
            LOG.error(e.getMessage(), e);
        }
        return addrs;
    }

    private static final Pattern PATTERN_REPLACE = Pattern.compile("([^\"]\\S+?)(\\s*)([;])(\\s*)");

    private static String replaceWithComma(final String addressList) {
        final Matcher m = PATTERN_REPLACE.matcher(addressList);
        if (m.find()) {
            final StringBuilder sb = new StringBuilder(addressList.length());
            int lastMatch = 0;
            do {
                sb.append(addressList.substring(lastMatch, m.start()));
                sb.append(m.group(1)).append(m.group(2)).append(',').append(m.group(4));
                lastMatch = m.end();
            } while (m.find());
            sb.append(addressList.substring(lastMatch));
            return sb.toString();
        }
        return addressList;
    }

    private static final Pattern PAT_QUOTED = Pattern.compile("(^\")([^\"]+?)(\"$)");

    private static final Pattern PAT_QUOTABLE_CHAR = Pattern.compile("[.,:;<>\"]");

    /**
     * Quotes given personal part of an Internet address according to RFC 822 syntax if needed; otherwise the personal is returned
     * unchanged.
     * <p>
     * This method guarantees that the resulting string can be used to build an Internet address according to RFC 822 syntax so that the
     * <code>{@link InternetAddress#parse(String)}</code> constructor won't throw an instance of <code>{@link AddressException}</code>.
     * 
     * <pre>
     * final String quotedPersonal = quotePersonal(&quot;Doe, Jane&quot;);
     * 
     * final String buildAddr = quotedPersonal + &quot; &lt;someone@somewhere.com&gt;&quot;;
     * System.out.println(buildAddr);
     * //Plain Address: &quot;=?UTF-8?Q?Doe=2C_Jan=C3=A9?=&quot; &lt;someone@somewhere.com&gt;
     * 
     * final InternetAddress ia = new InternetAddress(buildAddr);
     * System.out.println(ia.toUnicodeString());
     * //Unicode Address: &quot;Doe, Jane&quot; &lt;someone@somewhere.com&gt;
     * </pre>
     * 
     * @param personal The personal's string representation
     * @return The properly quoted personal for building an Internet address according to RFC 822 syntax
     */
    public static String quotePersonal(final String personal) {
        try {
            final String pers = MimeUtility.encodeWord(personal);
            if (PAT_QUOTED.matcher(pers).matches() ? false : PAT_QUOTABLE_CHAR.matcher(pers).find()) {
                /*
                 * Quote
                 */
                return new StringBuilder(pers.length() + 2).append('"').append(pers.replaceAll("\"", "\\\\\\\"")).append('"').toString();
            }
            return pers;
        } catch (final UnsupportedEncodingException e) {
            LOG.error("Unsupported encoding in a message detected and monitored: \"" + e.getMessage() + '"', e);
            mailInterfaceMonitor.addUnsupportedEncodingExceptions(e.getMessage());
            return personal;
        }
    }

    /**
     * Folds a string at linear whitespace so that each line is no longer than 76 characters, if possible. If there are more than 76
     * non-whitespace characters consecutively, the string is folded at the first whitespace after that sequence. The parameter
     * <tt>used</tt> indicates how many characters have been used in the current line; it is usually the length of the header name.
     * <p>
     * Note that line breaks in the string aren't escaped; they probably should be.
     * 
     * @param used The characters used in line so far
     * @param foldMe The string to fold
     * @return The folded string
     */
    public static String fold(final int used, final String foldMe) {
        int end;
        char c;
        /*
         * Strip trailing spaces and newlines
         */
        for (end = foldMe.length() - 1; end >= 0; end--) {
            c = foldMe.charAt(end);
            if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                break;
            }
        }
        String s;
        if (end != foldMe.length() - 1) {
            s = foldMe.substring(0, end + 1);
        } else {
            s = foldMe;
        }
        /*
         * Check if the string fits now
         */
        if (used + s.length() <= 76) {
            return s;
        }
        /*
         * Fold the string
         */
        final StringBuilder sb = new StringBuilder(s.length() + 4);
        char lastc = 0;
        int usedChars = used;
        while (usedChars + s.length() > 76) {
            int lastspace = -1;
            for (int i = 0; i < s.length(); i++) {
                if (lastspace != -1 && usedChars + i > 76) {
                    break;
                }
                c = s.charAt(i);
                if ((c == ' ' || c == '\t') && !(lastc == ' ' || lastc == '\t')) {
                    lastspace = i;
                }
                lastc = c;
            }
            if (lastspace == -1) {
                /*
                 * No space, use the whole thing
                 */
                sb.append(s);
                s = "";
                usedChars = 0;
                break;
            }
            sb.append(s.substring(0, lastspace));
            sb.append("\r\n");
            lastc = s.charAt(lastspace);
            sb.append(lastc);
            s = s.substring(lastspace + 1);
            usedChars = 1;
        }
        sb.append(s);
        return sb.toString();
    }

    /**
     * Unfolds a folded header. Any line breaks that aren't escaped and are followed by whitespace are removed.
     * 
     * @param headerLine The header line to unfold
     * @return The unfolded string
     */
    public static String unfold(final String headerLine) {
        if (null == headerLine) {
            return null;
        }
        StringBuilder sb = null;
        int i;
        /*-
         * Check folded encoded-words as per RFC 2047:
         * 
         * An 'encoded-word' may not be more than 75 characters long, including
         * 'charset', 'encoding', 'encoded-text', and delimiters.  If it is
         * desirable to encode more text than will fit in an 'encoded-word' of
         * 75 characters, multiple 'encoded-word's (separated by CRLF SPACE) may
         * be used.
         * 
         * In this case the SPACE character is not part of the header and should
         * be discarded.
         */
        String s = unfoldEncodedWords(headerLine);
        while ((i = s.indexOf('\r')) >= 0 || (i = s.indexOf('\n')) >= 0) {
            final int start = i;
            final int len = s.length();
            i++; // skip CR or NL
            if ((i < len) && (s.charAt(i - 1) == '\r') && (s.charAt(i) == '\n')) {
                i++; // skip LF
            }
            if (start == 0 || s.charAt(start - 1) != '\\') {
                char c;
                /*
                 * If next line starts with whitespace, skip all of it
                 */
                if ((i < len) && (((c = s.charAt(i)) == ' ') || (c == '\t'))) {
                    i++; // skip whitespace
                    while ((i < len) && (((c = s.charAt(i)) == ' ') || (c == '\t'))) {
                        i++;
                    }
                    if (sb == null) {
                        sb = new StringBuilder(s.length());
                    }
                    if (start != 0) {
                        sb.append(s.substring(0, start));
                        sb.append(' ');
                    }
                    s = s.substring(i);
                } else {
                    /*
                     * It's not a continuation line, just leave it in
                     */
                    if (sb == null) {
                        sb = new StringBuilder(s.length());
                    }
                    sb.append(s.substring(0, i));
                    s = s.substring(i);
                }
            } else {
                /*
                 * There's a backslash at "start - 1", strip it out, but leave in the line break
                 */
                if (sb == null) {
                    sb = new StringBuilder(s.length());
                }
                sb.append(s.substring(0, start - 1));
                sb.append(s.substring(start, i));
                s = s.substring(i);
            }
        }
        if (sb != null) {
            sb.append(s);
            return sb.toString();
        }
        return s;
    }

    private static final Pattern PAT_ENC_WORDS = Pattern.compile("(\\r?\\n(?:\\t| ))(=\\?\\S+?\\?\\S+?\\?.+?\\?=)");

    /**
     * Unfolds encoded-words as per RFC 2047. When unfolding a non-encoded-word the preceding space character should not be stripped out,
     * but should when unfolding encoded-words.
     * <p>
     * &quot;...<br>
     * An 'encoded-word' may not be more than 75 characters long, including 'charset', 'encoding', 'encoded-text', and delimiters. If it is
     * desirable to encode more text than will fit in an 'encoded-word' of 75 characters, multiple 'encoded-word's (separated by CRLF SPACE)
     * may be used.&quot;
     * <p>
     * 
     * <pre>
     * Subject: =?UTF-8?Q?Re:_Hardware_Kombatibilit=C3=A4t?=
     *  =?UTF-8?Q?sliste_f=C3=BCr_den_OXAE/OXSE4UCS?=
     * </pre>
     * 
     * Should be unfolded to:
     * 
     * <pre>
     * Subject: =?UTF-8?Q?Re:_Hardware_Kombatibilit=C3=A4t?==?UTF-8?Q?sliste_f=C3=BCr_den_OXAE/OXSE4UCS?=
     *                                                     &circ;&circ; SPACE removed
     * </pre>
     * 
     * @param encodedWords The possibly folded encoded-words
     * @return The unfolded encoded-words
     */
    private static String unfoldEncodedWords(final String encodedWords) {
        return PAT_ENC_WORDS.matcher(encodedWords).replaceAll("$2");
    }

    private static final int BUFSIZE = 8192; // 8K

    /**
     * Gets the matching header out of RFC 822 data input stream.
     * 
     * @param headerName The header name
     * @param inputStream The input stream
     * @param closeStream <code>true</code> to close the stream on finish; otherwise <code>false</code>
     * @return The value of first appeared matching header
     * @throws IOException If reading input stream fails
     */
    public static String extractHeader(final String headerName, final InputStream inputStream, final boolean closeStream) throws IOException {
        boolean close = closeStream;
        try {
            /*
             * Gather bytes until empty line, EOF or matching header found
             */
            final UnsynchronizedByteArrayOutputStream buffer = new UnsynchronizedByteArrayOutputStream(BUFSIZE);
            int start = 0;
            int i = -1;
            boolean firstColonFound = false;
            boolean found = false;
            while ((i = inputStream.read()) != -1) {
                int count = 0;
                while ((i == '\r') || (i == '\n')) {
                    if (!found) {
                        buffer.write(i);
                    }
                    if ((i == '\n')) {
                        if (found) {
                            i = inputStream.read();
                            if ((i != ' ') && (i != '\t')) {
                                /*
                                 * All read
                                 */
                                return new String(buffer.toByteArray(), "US-ASCII");

                            }
                            /*
                             * Write previously ignored CRLF
                             */
                            buffer.write('\r');
                            buffer.write('\n');
                            /*
                             * Continue collecting header value
                             */
                            buffer.write(i);
                        }
                        if (++count >= 2) {
                            /*
                             * End of headers
                             */
                            return null;
                        }
                        i = inputStream.read();
                        if ((i != ' ') && (i != '\t')) {
                            /*
                             * No header continuation; start of a new header
                             */
                            start = buffer.size();
                            firstColonFound = false;
                        }
                        buffer.write(i);
                    }
                    i = inputStream.read();
                }
                buffer.write(i);
                if (!firstColonFound && (i == ':')) {
                    /*
                     * Found the first delimiting colon in header line
                     */
                    firstColonFound = true;
                    if ((new String(buffer.toByteArray(start, buffer.size() - start - 1), "US-ASCII").equalsIgnoreCase(headerName))) {
                        /*
                         * Matching header
                         */
                        buffer.reset();
                        found = true;
                    }
                }
            }
            return null;
        } catch (final IOException e) {
            // Close on error
            close = true;
            throw e;
        } finally {
            if (close) {
                try {
                    inputStream.close();
                } catch (final IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }

}
