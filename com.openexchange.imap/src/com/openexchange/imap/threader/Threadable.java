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

package com.openexchange.imap.threader;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.imap.threadsort.MessageInfo;

/**
 * {@code Threadable} - An element within thread-sorted structure holding needed message information.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @see Threader
 */
public final class Threadable implements Cloneable, Serializable, Iterable<Threadable> {

    private static final long serialVersionUID = -680041493836177453L;

    /**
     * The empty <code>References</code>.
     */
    private static final String[] EMPTY_REFS = new String[0];

    /*-
     * -------------------- Members --------------------
     */

    Threadable next;
    Threadable kid;
    String fullName;
    String subject;
    private long date;
    String messageId;
    String inReplyTo;
    String[] refs;
    int messageNumber;
    long uid;
    private String subject2;
    private boolean hasRe;

    /**
     * Initializes a new dummy {@code Threadable}.
     */
    public Threadable() {
        super();
        subject = null; // this means "dummy".
        uid = -1L;
    }

    /**
     * Initializes a new {@code Threadable}.
     *
     * @param next The next element
     * @param subject The subject
     * @param id The identifier
     * @param references The referenced identifiers
     */
    public Threadable(Threadable next, String subject, String id, String[] references) {
        super();
        this.next = next;
        this.subject = subject;
        this.messageId = id;
        this.refs = references;
        uid = -1L;
    }

    @Override
    public Object clone() {
        try {
            final Threadable clone = (Threadable) super.clone();
            final Threadable next = clone.next;
            clone.next = (Threadable) (next == null ? null : next.clone());
            final Threadable kid = clone.kid;
            clone.kid = (Threadable) (kid == null ? null : kid.clone());
            return clone;
        } catch (CloneNotSupportedException e) {
            // Cannot occur
            throw new InternalError("Clone failed although Cloneable: " + e.getMessage());
        }
    }

    /**
     * Gets the appropriate {@code MessageInfo} for this {@code Threadable}.
     *
     * @return The appropriate {@code MessageInfo}
     */
    public MessageInfo toMessageInfo() {
        return new MessageInfo(messageNumber).setFullName(fullName);
    }

    /**
     * Gets the UID.
     *
     * @return The UID
     */
    public long getUid() {
        return uid;
    }

    @Override
    public String toString() {
        if (isDummy()) {
            return "[dummy]";
        }
        final StringBuilder builder = new StringBuilder(32);
        builder.append("Threadable [");
        if (fullName != null) {
            builder.append("fullName=").append(fullName).append(", ");
        }
        if (subject != null) {
            builder.append("subject=").append(subject).append(", ");
        }
        if (date > 0L) {
            builder.append("date=").append(new Date(date)).append(", ");
        }
        if (messageId != null) {
            builder.append("messageId=").append(messageId).append(", ");
        }
        if (inReplyTo != null) {
            builder.append("inReplyTo=").append(inReplyTo).append(", ");
        }
        if (refs != null) {
            builder.append("refs=").append(Arrays.toString(refs)).append(", ");
        }
        if (messageNumber > 0) {
            builder.append("messageNumber=").append(messageNumber);
        }
        if (uid > 0L) {
            builder.append(", uid=").append(uid).append(", ");
        }
        builder.append("hasRe=").append(hasRe).append("]");
        return builder.toString();
    }



    private static final Pattern PATTERN_SUBJECT = Pattern.compile(
        "^\\s*(Re|Sv|Vs|Aw|\u0391\u03A0|\u03A3\u03A7\u0395\u03A4|R|Rif|Res|Odp|Ynt)(?:\\[.*?\\]|\\(.*?\\))?:(?:\\s*)(.*)(?:\\s*)",
        Pattern.CASE_INSENSITIVE);

    private void simplifySubject() {
        if (com.openexchange.java.Strings.isEmpty(subject)) {
            subject2 = "";
            return;
        }

        // Try by RegEx
        {
            final Matcher m = PATTERN_SUBJECT.matcher(subject);
            if (m.matches()) {
                subject2 = m.group(2);
                return;
            }
        }

        // Start position
        int start = 0;
        final String subject = this.subject.trim();
        final int len = subject.length();
        if (len <= 2) {
            subject2 = subject;
            return;
        }

        boolean done = false;
        while (!done && (start < len)) {
            done = true;

            if (start < (len - 2) && (subject.charAt(start) == 'r' || subject.charAt(start) == 'R') && (subject.charAt(start + 1) == 'e' || subject.charAt(start + 1) == 'E')) {
                if (subject.charAt(start + 2) == ':') {
                    start += 3; // Skip over "Re:"
                    hasRe = true; // yes, we found it.
                    done = false; // keep going.
                } else if (start < (len - 2) && (subject.charAt(start + 2) == '[' || subject.charAt(start + 2) == '(')) {
                    int i = start + 3; // skip over "Re[" or "Re("

                    // Skip forward over digits after the "[" or "(".
                    while (i < len && subject.charAt(i) >= '0' && subject.charAt(i) <= '9') {
                        i++;
                    }

                    // Now ensure that the following thing is "]:" or "):"
                    // Only if it is do we alter `start'.
                    if (i < (len - 1) && (subject.charAt(i) == ']' || subject.charAt(i) == ')') && subject.charAt(i + 1) == ':') {
                        start = i + 2; // Skip over "]:"
                        hasRe = true; // yes, we found it.
                        done = false; // keep going.
                    }
                }
            }

            if (subject2 == "(no subject)") {
                subject2 = "";
            }
        }

        if (start == 0) {
            subject2 = subject;
        } else {
            subject2 = subject.substring(start).trim();
        }
    }

    /**
     * Flushes formerly cached (simple) subject.
     */
    protected void flushSubjectCache() {
        subject2 = null;
    }

    /**
     * Returns each subsequent element in the set of messages of which this {@code Threadable} as the root. Order is unimportant.
     */
    public Enumeration<Threadable> allElements() {
        return new ThreadableEnumeration(this, true);
    }

    @Override
    public Iterator<Threadable> iterator() {
        return new IteratorImpl(this);
    }

    /**
     * Gets the number of this {@code Threadable}'s top elements.
     *
     * @return The number of top elements
     */
    public int tops() {
        int count = 1;
        Threadable t = this.next;
        while (t != null) {
            count++;
            t = t.next;
        }
        return count;
    }

    /**
     * Gets the size of this {@code Threadable}.
     *
     * @return The size
     */
    public int size() {
        int count = 0;
        for (Enumeration<Threadable> e = allElements(); e.hasMoreElements();) {
            final Threadable t = e.nextElement();
            if (!t.isDummy()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns an object identifying this message. Generally this will be a representation of the contents of the Message-ID header.
     */
    public String messageID() {
        return messageId;
    }

    /**
     * Returns the IDs of the set of messages referenced by this one. This list should be ordered from oldest-ancestor to youngest-ancestor.
     */
    public String[] messageReferences() {
        return refs == null ? EMPTY_REFS : refs;
    }

    /**
     * When no references are present, subjects will be used to thread together messages. This method should return a threadable subject:
     * two messages with the same simplifiedSubject will be considered to belong to the same thread. This string should not have `Re:' on
     * the front, and may have been simplified in whatever other ways seem appropriate.
     * <p>
     * This is a String of Unicode characters, and should have had any encodings (such as RFC 2047 charset encodings) removed first.
     * <p>
     * If you aren't interested in threading by subject at all, return null.
     */
    public String simplifiedSubject() {
        if (subject2 == null) {
            simplifySubject();
        }
        return subject2;
    }

    /**
     * Whether the original subject was one that appeared to be a reply (that is, had a `Re:' or some other indicator.) When threading by
     * subject, this property is used to tell whether two messages appear to be siblings, or in a parent/child relationship.
     */
    public boolean subjectIsReply() {
        if (subject2 == null) {
            simplifySubject();
        }
        return hasRe;
    }

    /**
     * Sets the full name
     *
     * @param fullName The full name to set
     * @return This threadable
     */
    public Threadable setFullName(String fullName) {
        this.fullName = fullName;
        return this;
    }

    /**
     * Gets the full name
     *
     * @return The full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Gets the kid
     *
     * @return The kid
     */
    public Threadable kid() {
        return kid;
    }

    /**
     * Gets the next
     *
     * @return The next
     */
    public Threadable next() {
        return next;
    }

    /**
     * When the proper thread order has been computed, these two methods will be called on each Threadable in the chain, to set up the
     * proper tree structure.
     */
    public void setNext(Object next) {
        this.next = (Threadable) next;
        flushSubjectCache();
    }

    /**
     * Sets the child.
     *
     * @param child The child
     */
    public void setChild(Threadable child) {
        this.kid = child;
        flushSubjectCache();
    }

    /**
     * Creates a dummy parent object.
     * <P>
     * With some set of messages, the only way to achieve proper threading is to introduce an element into the tree which represents
     * messages which are not present in the set: for example, when two messages share a common ancestor, but that ancestor is not in the
     * set. This method is used to make a placeholder for those sorts of ancestors. It should return an object which is also a IThreadable.
     * The setNext() and setChild() methods will be used on this placeholder, as either the object or the argument, just as for other
     * elements of the tree.
     */
    public Threadable makeDummy() {
        return new Threadable();
    }

    /**
     * This should return true of dummy messages, false otherwise. It is legal to pass dummy messages in with the list returned by
     * elements(); the isDummy() method is the mechanism by which they are noted and ignored.
     */
    public boolean isDummy() {
        return (subject == null);
    }

    private static final class ThreadableEnumeration implements Enumeration<Threadable> {

        private Threadable tail;
        private Enumeration<Threadable> kids;
        private final boolean recursive;

        protected ThreadableEnumeration(Threadable thread, boolean recursive) {
            super();
            this.recursive = recursive;
            if (recursive) {
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
                if (recursive && tail.kid != null) {
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
            return ((null != tail) || ((kids != null) && kids.hasMoreElements()));
        }
    }

    private static final class IteratorImpl implements Iterator<Threadable> {

        private final Enumeration<Threadable> e;
        private Threadable cur;

        protected IteratorImpl(Threadable t) {
            super();
            this.e = t.allElements();
        }

        @Override
        public boolean hasNext() {
            if (!e.hasMoreElements()) {
                return false;
            }
            do {
                cur = e.nextElement();
            } while (cur.isDummy() && e.hasMoreElements());
            return !cur.isDummy();
        }

        @Override
        public Threadable next() {
            if (null == cur || cur.isDummy()) {
                throw new NoSuchElementException();
            }
            final Threadable t = cur;
            cur = null;
            return t;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove() not supported");
        }
    }
}
