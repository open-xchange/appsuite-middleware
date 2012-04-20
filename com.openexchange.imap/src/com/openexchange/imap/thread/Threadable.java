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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.imap.thread;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import com.openexchange.imap.IMAPException;
import com.openexchange.imap.util.ImapUtility;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.sun.mail.iap.BadCommandException;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.BODY;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.IMAPResponse;
import com.sun.mail.imap.protocol.Item;
import com.sun.mail.imap.protocol.RFC822DATA;

/**
 * {@link Threadable}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Threadable {

    Threadable next;

    Threadable kid;

    String subject;

    private String author;

    private long date;

    String id;

    String[] refs;

    private int message_number;

    private String subject2;

    private boolean has_re;

    /**
     * Initializes a new {@link Threadable}.
     */
    public Threadable() {
        subject = null; // this means "dummy".
    }

    /**
     * Initializes a new {@link Threadable}.
     * 
     * @param next The next element
     * @param subject The subject
     * @param id The identifier
     * @param references The referenced identifiers
     */
    public Threadable(final Threadable next, final String subject, final String id, final String[] references) {
        this.next = next;
        this.subject = subject;
        this.id = id;
        this.refs = references;
    }

    @Override
    public String toString() {
        if (isDummy()) {
            return "[dummy]";
        }

        String s = "[ " + id + ": " + subject + " (";
        if (refs != null) {
            for (int i = 0; i < refs.length; i++) {
                s += " " + refs[i];
            }
        }
        if (date > 0) {
            s += " \"" + new Date(date) + "\"";
        }
        return s + " ) ]";
    }

    void simplifySubject() {

        int start = 0;
        final int L = subject.length();

        boolean done = false;
        while (!done) {
            done = true;

            // skip whitespace.
            while (subject.charAt(start) <= ' ') {
                start++;
            }

            if (start < (L - 2) && (subject.charAt(start) == 'r' || subject.charAt(start) == 'R') && (subject.charAt(start + 1) == 'e' || subject.charAt(start + 1) == 'e')) {
                if (subject.charAt(start + 2) == ':') {
                    start += 3; // Skip over "Re:"
                    has_re = true; // yes, we found it.
                    done = false; // keep going.
                    done = false;

                } else if (start < (L - 2) && (subject.charAt(start + 2) == '[' || subject.charAt(start + 2) == '(')) {
                    int i = start + 3; // skip over "Re[" or "Re("

                    // Skip forward over digits after the "[" or "(".
                    while (i < L && subject.charAt(i) >= '0' && subject.charAt(i) <= '9') {
                        i++;
                    }

                    // Now ensure that the following thing is "]:" or "):"
                    // Only if it is do we alter `start'.
                    if (i < (L - 1) && (subject.charAt(i) == ']' || subject.charAt(i) == ')') && subject.charAt(i + 1) == ':') {
                        start = i + 2; // Skip over "]:"
                        has_re = true; // yes, we found it.
                        done = false; // keep going.
                    }
                }
            }

            if (subject2 == "(no subject)") {
                subject2 = "";
            }
        }

        int end = L;
        // Strip trailing whitespace.
        while (end > start && subject.charAt(end - 1) < ' ') {
            end--;
        }

        if (start == 0 && end == L) {
            subject2 = subject;
        } else {
            subject2 = subject.substring(start, end);
        }
    }

    void flushSubjectCache() {
        subject2 = null;
    }

    /**
     * An enumeration for all nested elements.
     * 
     * @return The enumeration
     */
    public Enumeration<Threadable> allElements() {
        return new ThreadableEnumeration(this, true);
    }

    public String messageThreadID() {
        return id;
    }

    public String[] messageThreadReferences() {
        return refs;
    }

    public String simplifiedSubject() {
        if (subject2 == null) {
            simplifySubject();
        }
        return subject2;
    }

    public boolean subjectIsReply() {
        if (subject2 == null) {
            simplifySubject();
        }
        return has_re;
    }

    // Used by both IThreadable and ISortable
    public void setNext(final Object next) {
        this.next = (Threadable) next;
        flushSubjectCache();
    }

    // Used by both IThreadable and ISortable
    public void setChild(final Object kid) {
        this.kid = (Threadable) kid;
        flushSubjectCache();
    }

    /**
     * Create a dummy instance.
     * 
     * @return The dummy instance
     */
    public static Threadable makeDummy() {
        return new Threadable();
    }

    public boolean isDummy() {
        return (subject == null);
    }

    private static final class ThreadableEnumeration implements Enumeration<Threadable> {

        Threadable tail;

        Enumeration<Threadable> kids;

        boolean recursive_p;

        ThreadableEnumeration(final Threadable thread, final boolean recursive_p) {
            this.recursive_p = recursive_p;
            if (recursive_p) {
                tail = thread;
            } else {
                tail = thread.kid;
            }
        }

        @Override
        public Threadable nextElement() {
            if (kids != null) {
                // if `kids' is non-null, then we've already returned a node,
                // and we should now go to work on its children.
                final Threadable result = kids.nextElement();
                if (!kids.hasMoreElements()) {
                    kids = null;
                }
                return result;

            } else if (tail != null) {
                // Return `tail', but first note its children, if any.
                // We will descend into them the next time around.
                final Threadable result = tail;
                if (recursive_p && tail.kid != null) {
                    kids = new ThreadableEnumeration(tail.kid, true);
                }
                tail = tail.next;
                return result;

            } else {
                throw new NoSuchElementException();
            }
        }

        @Override
        public boolean hasMoreElements() {
            if (tail != null) {
                return true;
            } else if (kids != null && kids.hasMoreElements()) {
                return true;
            } else {
                return false;
            }
        }
    }

    private interface HeaderHandler {

        void handle(Header hdr, Threadable threadable) throws MessagingException;

    }

    static final Map<String, HeaderHandler> HANDLERS = new HashMap<String, HeaderHandler>() {

        {
            put(MessageHeaders.HDR_SUBJECT, new HeaderHandler() {

                @Override
                public void handle(final Header hdr, final Threadable threadable) throws MessagingException {
                    threadable.subject = MimeMessageUtility.decodeMultiEncodedHeader(MimeMessageUtility.checkNonAscii(hdr.getValue()));
                }
            });
            put(MessageHeaders.HDR_REFERENCES, new HeaderHandler() {

                private final Pattern split = Pattern.compile(" +");

                @Override
                public void handle(final Header hdr, final Threadable threadable) throws MessagingException {
                    threadable.refs = split.split(MimeMessageUtility.decodeMultiEncodedHeader(hdr.getValue()));
                }
            });
            put(MessageHeaders.HDR_MESSAGE_ID, new HeaderHandler() {

                @Override
                public void handle(final Header hdr, final Threadable threadable) throws MessagingException {
                    threadable.id = MimeMessageUtility.decodeMultiEncodedHeader(hdr.getValue());
                }
            });
        }
    };

    /**
     * Gets the threadables for given IMAP folder.
     * 
     * @param imapFolder The IMAP folders
     * @return The detected threadables
     * @throws MessagingException If an error occurs
     */
    @SuppressWarnings("unchecked")
    public static List<Threadable> getAllThreadablesFrom(final IMAPFolder imapFolder) throws MessagingException {
        final int messageCount = imapFolder.getMessageCount();
        if (messageCount <= 0) {
            /*
             * Empty folder...
             */
            return Collections.emptyList();
        }
        return (List<Threadable>) (imapFolder.doCommand(new IMAPFolder.ProtocolCommand() {

            @Override
            public Object doCommand(final IMAPProtocol protocol) throws ProtocolException {
                final StringBuilder command;
                final Response[] r;
                {
                    command = new StringBuilder(128).append("FETCH ").append(1 == messageCount ? "1" : "1:*").append(" (");
                    final boolean rev1 = protocol.isREV1();
                    if (rev1) {
                        command.append("BODY.PEEK[HEADER.FIELDS (");
                    } else {
                        command.append("RFC822.HEADER.LINES (");
                    }
                    command.append("Subject Message-Id Reference");
                    if (rev1) {
                        command.append(")]");
                    } else {
                        command.append(')');
                    }
                    command.append(')');
                    r = protocol.command(command.toString(), null);
                }
                final int len = r.length - 1;
                final Response response = r[len];
                if (response.isOK()) {
                    try {
                        final List<Threadable> threadables = new ArrayList<Threadable>();
                        for (int j = 0; j < len; j++) {
                            if ("FETCH".equals(((IMAPResponse) r[j]).getKey())) {
                                final InternetHeaders h;
                                {
                                    final InputStream headerStream;
                                    final BODY body = getItemOf(BODY.class, (FetchResponse) r[j]);
                                    if (null == body) {
                                        final RFC822DATA rfc822data = getItemOf(RFC822DATA.class, (FetchResponse) r[j]);
                                        headerStream = null == rfc822data ? null : rfc822data.getByteArrayInputStream();
                                    } else {
                                        headerStream = body.getByteArrayInputStream();
                                    }
                                    if (null == headerStream) {
                                        h = null;
                                    } else {
                                        h = new InternetHeaders();
                                        h.load(headerStream);
                                    }
                                }
                                if (h != null) {
                                    final Threadable t = new Threadable();
                                    for (final Enumeration<?> e = h.getAllHeaders(); e.hasMoreElements();) {
                                        final Header hdr = (Header) e.nextElement();
                                        final HeaderHandler headerHandler = HANDLERS.get(hdr.getName());
                                        if (null != headerHandler) {
                                            headerHandler.handle(hdr, t);
                                        }
                                    }
                                    r[j] = null;
                                    threadables.add(t);
                                }
                            }
                        }
                        protocol.notifyResponseHandlers(r);
                        return threadables;
                    } catch (final MessagingException e) {
                        throw new ProtocolException(e.getMessage(), e);
                    }
                } else if (response.isBAD()) {
                    if (ImapUtility.isInvalidMessageset(response)) {
                        return new long[0];
                    }
                    throw new BadCommandException(IMAPException.getFormattedMessage(
                        IMAPException.Code.PROTOCOL_ERROR,
                        command,
                        response.toString() + " (" + imapFolder.getStore().toString() + ")"));
                } else if (response.isNO()) {
                    throw new CommandFailedException(IMAPException.getFormattedMessage(
                        IMAPException.Code.PROTOCOL_ERROR,
                        command,
                        response.toString() + " (" + imapFolder.getStore().toString() + ")"));
                } else {
                    protocol.handleResult(response);
                }
                return null;
            }

        }));
    }

    /**
     * Gets the item associated with given class in specified <i>FETCH</i> response.
     * 
     * @param <I> The returned item's class
     * @param clazz The item class to look for
     * @param fetchResponse The <i>FETCH</i> response
     * @return The item associated with given class in specified <i>FETCH</i> response or <code>null</code>.
     * @see #getItemOf(Class, FetchResponse, String)
     */
    protected static <I extends Item> I getItemOf(final Class<? extends I> clazz, final FetchResponse fetchResponse) {
        final int len = fetchResponse.getItemCount();
        for (int i = 0; i < len; i++) {
            final Item item = fetchResponse.getItem(i);
            if (clazz.isInstance(item)) {
                return clazz.cast(item);
            }
        }
        return null;
    }
}
