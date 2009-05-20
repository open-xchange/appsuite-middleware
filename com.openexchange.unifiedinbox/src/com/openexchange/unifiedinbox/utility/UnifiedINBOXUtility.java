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

package com.openexchange.unifiedinbox.utility;

import static com.openexchange.mail.MailPath.SEPERATOR;
import static com.openexchange.mail.utils.MailFolderUtility.prepareFullname;
import static com.openexchange.mail.utils.MailFolderUtility.prepareMailFolderParam;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.unifiedinbox.UnifiedINBOXAccess;
import com.openexchange.unifiedinbox.UnifiedINBOXException;

/**
 * {@link UnifiedINBOXUtility} - Utility methods for Unified INBOX.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnifiedINBOXUtility {

    /**
     * Initializes a new {@link UnifiedINBOXUtility}.
     */
    private UnifiedINBOXUtility() {
        super();
    }

    /**
     * Parses specified Unified INBOX mail IDs.
     * 
     * @param mailIDs The Unified INBOX mail IDs to parse
     * @return A map grouping referenced accounts and referenced fullnames and IDs.
     * @throws MailException If parsing mail IDs fails
     */
    public static Map<Integer, Map<String, List<String>>> parseMailIDs(final String[] mailIDs) throws MailException {
        final Map<Integer, Map<String, List<String>>> map = new HashMap<Integer, Map<String, List<String>>>(mailIDs.length);
        // Start parsing
        final MailPath mailPath = new MailPath();
        for (final String mailID : mailIDs) {
            mailPath.setMailIdentifierString(mailID);

            final Integer key = Integer.valueOf(mailPath.getAccountId());
            Map<String, List<String>> folderUIDMap = map.get(key);
            if (null == folderUIDMap) {
                folderUIDMap = new HashMap<String, List<String>>(mailIDs.length / 2);
                map.put(key, folderUIDMap);
            }
            final String folder = mailPath.getFolder();
            List<String> uids = folderUIDMap.get(folder);
            if (null == uids) {
                uids = new ArrayList<String>();
                folderUIDMap.put(folder, uids);
            }
            uids.add(mailPath.getMailID());
        }
        return map;
    }

    /**
     * Generates a nested folder's fullname.
     * 
     * @param uiAccountId The Unified INBOX's account ID
     * @param uiFullname The Unified INBOX's fullname
     * @param nestedAccountId The nested account's ID
     * @param nestedFullname The nested folder's fullname
     * @return The generated nested folder's fullname.
     */
    public static String generateNestedFullname(final int uiAccountId, final String uiFullname, final int nestedAccountId, final String nestedFullname) {
        return new StringBuilder(32).append(prepareFullname(uiAccountId, uiFullname)).append(SEPERATOR).append(
            prepareFullname(nestedAccountId, nestedFullname)).toString();
    }

    /**
     * Parses nested fullname.
     * <p>
     * <code>"INBOX/default3/INBOX"</code> =&gt; <code>"default3/INBOX"</code>
     * 
     * @param nestedFullname The nested fullname to parse
     * @return The parsed nested fullname argument
     * @throws UnifiedINBOXException If specified nested fullname is invalid
     */
    public static FullnameArgument parseNestedFullname(final String nestedFullname) throws UnifiedINBOXException {
        // INBOX/default0/INBOX
        if (!startsWithKnownFullname(nestedFullname)) {
            throw new UnifiedINBOXException(
                UnifiedINBOXException.Code.FOLDER_NOT_FOUND,
                prepareMailFolderParam(nestedFullname).getFullname());
        }
        // Cut off starting known fullname and its separator character
        final String fn = nestedFullname.substring(nestedFullname.indexOf(SEPERATOR) + 1);
        return prepareMailFolderParam(fn);
    }

    private static boolean startsWithKnownFullname(final String fullname) {
        for (final Iterator<String> iter = UnifiedINBOXAccess.KNOWN_FOLDERS.iterator(); iter.hasNext();) {
            final String knownFullname = iter.next();
            if (fullname.startsWith(knownFullname)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the account's fullname.
     * 
     * @param mailAccess The mail access to desired account
     * @param fullname The fullname to look-up
     * @return The account's fullname
     * @throws MailException If fullname look-up fails
     */
    public static String determineAccountFullname(final MailAccess<?, ?> mailAccess, final String fullname) throws MailException {
        if (UnifiedINBOXAccess.INBOX.equals(fullname)) {
            return UnifiedINBOXAccess.INBOX;
        }
        if (UnifiedINBOXAccess.DRAFTS.equals(fullname)) {
            return mailAccess.getFolderStorage().getDraftsFolder();
        }
        if (UnifiedINBOXAccess.SENT.equals(fullname)) {
            return mailAccess.getFolderStorage().getSentFolder();
        }
        if (UnifiedINBOXAccess.SPAM.equals(fullname)) {
            return mailAccess.getFolderStorage().getSpamFolder();
        }
        if (UnifiedINBOXAccess.TRASH.equals(fullname)) {
            return mailAccess.getFolderStorage().getTrashFolder();
        }
        throw new UnifiedINBOXException(UnifiedINBOXException.Code.UNKNOWN_DEFAULT_FOLDER_INDEX, fullname);
    }

    /**
     * Prints specified {@link Throwable}'s stack trace to given string builder.
     * 
     * @param t The {@link Throwable} instance
     * @param builder The string builder to append to
     */
    public static void appendStackTrace2StringBuilder(final Throwable t, final StringBuilder builder) {
        t.printStackTrace(new java.io.PrintWriter(new StringWriter(builder)));
    }

    private static class StringWriter extends Writer {

        private final StringBuilder buf;

        /**
         * Create a new string writer, using the default initial string-buffer size.
         */
        public StringWriter() {
            buf = new StringBuilder();
            lock = buf;
        }

        /**
         * Create a new string writer, using the specified initial string-buffer size.
         * 
         * @param initialSize an int specifying the initial size of the buffer.
         */
        public StringWriter(final int initialSize) {
            if (initialSize < 0) {
                throw new IllegalArgumentException("Negative buffer size");
            }
            buf = new StringBuilder(initialSize);
            lock = buf;
        }

        /**
         * Create a new string writer, using the specified string builder.
         * 
         * @param buf The string builder to use
         */
        public StringWriter(final StringBuilder buf) {
            if (null == buf) {
                throw new IllegalArgumentException("Buffer is null");
            }
            this.buf = buf;
            lock = this.buf;
        }

        /**
         * Write a single character.
         */
        @Override
        public void write(final int c) {
            buf.append((char) c);
        }

        /**
         * Write a portion of an array of characters.
         * 
         * @param cbuf Array of characters
         * @param off Offset from which to start writing characters
         * @param len Number of characters to write
         */
        @Override
        public void write(final char cbuf[], final int off, final int len) {
            if ((off < 0) || (off > cbuf.length) || (len < 0) || ((off + len) > cbuf.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return;
            }
            buf.append(cbuf, off, len);
        }

        /**
         * Write a string.
         */
        @Override
        public void write(final String str) {
            buf.append(str);
        }

        /**
         * Write a portion of a string.
         * 
         * @param str String to be written
         * @param off Offset from which to start writing characters
         * @param len Number of characters to write
         */
        @Override
        public void write(final String str, final int off, final int len) {
            buf.append(str.substring(off, off + len));
        }

        /**
         * Appends the specified character sequence to this writer.
         * <p>
         * An invocation of this method of the form <tt>out.append(csq)</tt> behaves in exactly the same way as the invocation
         * 
         * <pre>
         * out.write(csq.toString())
         * </pre>
         * <p>
         * Depending on the specification of <tt>toString</tt> for the character sequence <tt>csq</tt>, the entire sequence may not be
         * appended. For instance, invoking the <tt>toString</tt> method of a character buffer will return a subsequence whose content
         * depends upon the buffer's position and limit.
         * 
         * @param csq The character sequence to append. If <tt>csq</tt> is <tt>null</tt>, then the four characters <tt>"null"</tt> are
         *            appended to this writer.
         * @return This writer
         * @since 1.5
         */
        @Override
        public StringWriter append(final CharSequence csq) {
            if (csq == null) {
                write("null");
            } else {
                write(csq.toString());
            }
            return this;
        }

        /**
         * Appends a subsequence of the specified character sequence to this writer.
         * <p>
         * An invocation of this method of the form <tt>out.append(csq, start,
         * end)</tt> when <tt>csq</tt> is not <tt>null</tt>, behaves in exactly the same way as the invocation
         * 
         * <pre>
         * out.write(csq.subSequence(start, end).toString())
         * </pre>
         * 
         * @param csq The character sequence from which a subsequence will be appended. If <tt>csq</tt> is <tt>null</tt>, then characters
         *            will be appended as if <tt>csq</tt> contained the four characters <tt>"null"</tt>.
         * @param start The index of the first character in the subsequence
         * @param end The index of the character following the last character in the subsequence
         * @return This writer
         * @throws IndexOutOfBoundsException If <tt>start</tt> or <tt>end</tt> are negative, <tt>start</tt> is greater than <tt>end</tt>, or
         *             <tt>end</tt> is greater than <tt>csq.length()</tt>
         * @since 1.5
         */
        @Override
        public StringWriter append(final CharSequence csq, final int start, final int end) {
            final CharSequence cs = (csq == null ? "null" : csq);
            write(cs.subSequence(start, end).toString());
            return this;
        }

        /**
         * Appends the specified character to this writer.
         * <p>
         * An invocation of this method of the form <tt>out.append(c)</tt> behaves in exactly the same way as the invocation
         * 
         * <pre>
         * out.write(c)
         * </pre>
         * 
         * @param c The 16-bit character to append
         * @return This writer
         * @since 1.5
         */
        @Override
        public StringWriter append(final char c) {
            write(c);
            return this;
        }

        /**
         * Return the buffer's current value as a string.
         */
        @Override
        public String toString() {
            return buf.toString();
        }

        /**
         * Return the string buffer itself.
         * 
         * @return StringBuffer holding the current buffer value.
         */
        public StringBuilder getBuffer() {
            return buf;
        }

        /**
         * Flush the stream.
         */
        @Override
        public void flush() {
            // Nothing to do
        }

        /**
         * Closing a <tt>StringWriter</tt> has no effect. The methods in this class can be called after the stream has been closed without
         * generating an <tt>IOException</tt>.
         */
        @Override
        public void close() throws IOException {
            // Nothing to do
        }
    } // End of StringWriter
}
