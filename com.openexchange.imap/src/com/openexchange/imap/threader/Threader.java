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

package com.openexchange.imap.threader;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link Threader} - This is an implementation of a message threading algorithm, as originally devised by Zamie Zawinski. See <a
 * href="http://www.jwz.org/doc/threading.html">http://www.jwz.org/doc/threading.html</a> for details. For his Java implementation, see <a
 * href="http://lxr.mozilla.org/mozilla/source/grendel/sources/grendel/view/Threader.java">http://lxr.mozilla.org/mozilla/source/grendel
 * /sources/grendel/view/Threader.java</a>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Threader {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Threader.class);

    private ThreadContainer rootNode; // has kids, and no next
    private Map<String, ThreadContainer> idMap; // maps message IDs to ThreadContainers
    private int bogusIdCount; // tick of how many dup IDs we've seen
    private boolean insistOnRe;

    /**
     * Initializes a new {@link Threader}.
     */
    public Threader() {
        super();
        bogusIdCount = 0;
        insistOnRe = true;
    }

    /**
     * Sets the <code>insistOnRe</code> flag. Default is <code>true</code>.
     * <p>
     * If disabled, those messages are grouped to threads which have an equal subject, regardless if one is the response (has "Re: " prefix)
     * of the other.
     * <p>
     * By default exactly that case is prevented:
     *
     * <pre>
     * // - If that container is a non-dummy, and that message's subject begins
     * // with &quot;Re:&quot;, but *this* message's subject does *not*, then make that
     * // be a child of this one -- they were misordered. (This happens
     * // somewhat implicitly, since if there are two messages, one with Re:
     * // and one without, the one without will be in the hash table,
     * // regardless of the order in which they were seen.)
     * //
     * // - Otherwise, make a new dummy container and make both messages be a
     * // child of it. This catches the both-are-replies and neither-are-
     * // replies cases, and makes them be siblings instead of asserting a
     * // hierarchical relationship which might not be true.
     *
     * </pre>
     *
     * @param insistOnRe The <code>insistOnRe</code> flag to set
     * @return This threader with new behavior applied
     */
    public Threader setInsistOnRe(boolean insistOnRe) {
        this.insistOnRe = insistOnRe;
        return this;
    }

    /**
     * Threads the set of messages indicated by <tt>threadableRoot</tt>.
     * <p>
     * The <tt>Threadable</tt> returned is the new first element of the root set.
     *
     * @param threadableRoot The start of the list.
     */
    public Threadable thread(final Threadable threadableRoot) {
        if (threadableRoot == null) {
            return null;
        }

        idMap = new HashMap<String, ThreadContainer>();

        for (final Enumeration<Threadable> e = threadableRoot.allElements(); e.hasMoreElements();) {
            final Threadable t = e.nextElement();
            if (!t.isDummy()) {
                buildContainer(t);
            }
        }

        rootNode = findRootSet();
        idMap = null;

        pruneEmptyContainers(rootNode);

        // We do this so to avoid flipping the input order each time through.
        rootNode.reverseChildren();

        gatherSubjects();

        if (rootNode.next != null) {
            throw new IllegalStateException("root node has a next?" + rootNode);
        }

        for (ThreadContainer r = rootNode.child; r != null; r = r.next) {
            // If this direct child of the root node has no threadable in it,
            // manufacture a dummy container to bind its children together.
            // Note that these dummies can only ever occur as elements of
            // the root set.
            if (r.threadable == null) {
                r.threadable = r.child.threadable.makeDummy();
            }
        }

        final Threadable result = (rootNode.child == null ? null : rootNode.child.threadable);

        // Flush the tree structure of each element of the root set down into
        // their underlying threadables.
        rootNode.flush();
        rootNode = null;

        return result;
    }

    /**
     * <code>buildContainer()</code> does three things:
     * <ul>
     * <li>It walks the tree of {@code Threadable}s, and wraps each in a {@code ThreadContainer} object.</li>
     * <li>It indexes each {@code ThreadContainer} object in the id_table, under the message ID of the contained {@code Threadable}.</li>
     * <li>For each of the {@code Threadable}'s references, it ensures that there is a {@code ThreadContainer} in the table (an empty one,
     * if necessary.)</li>
     * </ul>
     *
     * @param threadable The {@code Threadable} instance to build container for
     */
    private void buildContainer(final Threadable threadable) {
        String id = threadable.messageID();
        ThreadContainer container = idMap.get(id);

        if (container != null) {
            // There is already a ThreadContainer in the table for this ID.
            // Under normal circumstances, there will be no IThreadable in it
            // (since it was a forward reference from a References field.)
            //
            // If there is already a threadable in it, then that means there
            // are two IThreadables with the same ID. Generate a new ID for
            // this one, sigh... This ID is only used to cause the two entries
            // in the hash table to not stomp each other.
            //
            if (container.threadable != null) {
                id = "<Bogus-id:" + (bogusIdCount++) + ">";
                container = null;
            } else {
                container.threadable = threadable;
            }
        }

        // Create a ThreadContainer for this IThreadable, and index it in
        // the hash table.
        //
        if (container == null) {
            container = new ThreadContainer();
            container.threadable = threadable;
            // c.debug_id = id;
            idMap.put(id, container);
        }

        // Create ThreadContainers for each of the references which don't
        // have them. Link each of the referenced messages together in the
        // order implied by the references field, unless they are already
        // linked.
        ThreadContainer parentRef = null;
        {
            final String[] refs = threadable.messageReferences();
            final int len = refs.length;
            for (int i = 0; i < len; i++) {
                final String refString = refs[i];
                ThreadContainer ref = idMap.get(refString);
                if (ref == null) {
                    ref = new ThreadContainer();
                    // ref.debug_id = ref_string;
                    idMap.put(refString, ref);
                }
                // If we have references A B C D, make D be a child of C, etc,
                // except if they have parents already.
                //
                if (parentRef != null && // there is a parent
                ref.parent == null && // don't have a parent already
                parentRef != ref && // not a tight loop
                !parentRef.findChild(ref)) { // not a wide loop
                    // Ok, link it into the parent's child list.
                    ref.parent = parentRef;
                    ref.next = parentRef.child;
                    parentRef.child = ref;
                }
                parentRef = ref;
            }
        }

        // At this point 'parent_ref' is set to the container of the last element
        // in the references field. Make that be the parent of this container,
        // unless doing so would introduce a circularity.
        //
        if (parentRef != null && (parentRef == container || container.findChild(parentRef))) {
            parentRef = null;
        }

        if (container.parent != null) {
            // If it has a parent already, that's there because we saw this message
            // in a references field, and presumed a parent based on the other
            // entries in that field. Now that we have the actual message, we can
            // be more definitive, so throw away the old parent and use this new one.
            // Find this container in the parent's child-list, and unlink it.
            //
            // Note that this could cause this message to now have no parent, if it
            // has no references field, but some message referred to it as the
            // non-first element of its references. (Which would have been some
            // kind of lie...)
            //
            ThreadContainer rest, prev;
            for (prev = null, rest = container.parent.child; rest != null; prev = rest, rest = rest.next) {
                if (rest == container) {
                    break;
                }
            }
            if (rest == null) {
                throw new RuntimeException("Didnt find " + container + " in parent" + container.parent);
            }

            if (prev == null) {
                container.parent.child = container.next;
            } else {
                prev.next = container.next;
            }

            container.next = null;
            container.parent = null;
        }

        // If we have a parent, link c into the parent's child list.
        if (parentRef != null) {
            container.parent = parentRef;
            container.next = parentRef.child;
            parentRef.child = container;
        }
    }

    /**
     * Find the root set of the ThreadContainers (and return a root node.)
     * <p>
     * A container is in the root set if it has no parents.
     *
     * @return The root container
     */
    private ThreadContainer findRootSet() {
        final ThreadContainer root = new ThreadContainer();
        // root.debug_id = "((root))";
        for (final ThreadContainer c : idMap.values()) {
            if (c.parent == null) {
                if (c.next != null) {
                    throw new RuntimeException("c.next is " + c.next.toString());
                }
                c.next = root.child;
                root.child = c;
            }
        }
        return root;
    }

    /**
     * Walk through the threads and discard any empty container objects.
     * <p>
     * After calling this, there will only be any empty container objects at depth 0, and those will all have at least two kids.
     *
     * @param parent The parent container
     */
    private void pruneEmptyContainers(final ThreadContainer parent) {
        ThreadContainer container, prev, next;
        for (prev = null, container = parent.child, next = container.next; container != null; prev = container, container = next, next =
            (container == null ? null : container.next)) {

            if (container.threadable == null && container.child == null) {
                // This is an empty container with no kids. Nuke it.
                //
                // Normally such containers won't occur, but they can show up when
                // two messages have References lines that disagree. For example,
                // assuming A and B are messages, and 1, 2, and 3 are references for
                // messages we haven't seen:
                //
                // A has refs: 1 2 3
                // B has refs: 1 3
                //
                // There is ambiguity as to whether 3 is a child of 1 or 2. So,
                // depending on the processing order, we might end up with either
                //
                // -- 1
                // |-- 2
                // |-- 3
                // |-- A
                // |-- B
                // or
                // -- 1
                // |-- 2 <--- non root childless container
                // |-- 3
                // |-- A
                // |-- B
                //
                if (prev == null) {
                    parent.child = container.next;
                } else {
                    prev.next = container.next;
                }

                // Set container to prev so that prev keeps its same value
                // the next time through the loop.
                container = prev;

            } else if (container.threadable == null && // expired, and
            container.child != null && // has kids, and
            (container.parent != null || // not at root, or
            container.child.next == null)) { // only one kid

                // Expired message with kids. Promote the kids to this level.
                // Don't do this if we would be promoting them to the root level,
                // unless there is only one kid.

                ThreadContainer tail;
                final ThreadContainer kids = container.child;

                // Remove this container from the list, replacing it with `kids'.
                if (prev == null) {
                    parent.child = kids;
                } else {
                    prev.next = kids;
                }

                // make each child's parent be this level's parent.
                // make the last child's next be this container's next
                // (splicing `kids' into the list in place of `container'.)
                for (tail = kids; tail.next != null; tail = tail.next) {
                    tail.parent = container.parent;
                }

                tail.parent = container.parent;
                tail.next = container.next;

                // Since we've inserted items in the chain, `next' currently points
                // to the item after them (tail.next); reset that so that we process
                // the newly promoted items the very next time around.
                next = kids;

                // Set container to prev so that prev keeps its same value
                // the next time through the loop.
                container = prev;

            } else if (container.child != null) {
                // A real message with kids.
                // Iterate over its children, and try to strip out the junk.

                pruneEmptyContainers(container);
            }
        }
    }

    /**
     * If any two members of the root set have the same subject, merge them.
     * <p>
     * This is so that messages which don't have <code>References</code> headers at all still get threaded (to the extent possible, at least.)
     */
    private void gatherSubjects() {
        int count = 0;
        for (ThreadContainer c = rootNode.child; c != null; c = c.next) {
            count++;
        }
        // Make the hash table large enough to not need to be rehashed.
        final Map<String, ThreadContainer> subjTable = new HashMap<String, ThreadContainer>(count << 1, 0.9f);
        count = 0;
        for (ThreadContainer c = rootNode.child; c != null; c = c.next) {
            Threadable threadable = c.threadable;
            // If there is no threadable, this is a dummy node in the root set.
            // Only root set members may be dummies, and they always have at least
            // two kids. Take the first kid as representative of the subject.
            if (threadable == null) {
                threadable = c.child.threadable;
            }
            final String subj = threadable.simplifiedSubject();
            if (com.openexchange.java.Strings.isEmpty(subj)) {
                continue;
            }
            final ThreadContainer old = subjTable.get(subj);
            // Add this container to the table if:
            // - There is no container in the table with this subject, or
            // - This one is a dummy container and the old one is not: the dummy
            // one is more interesting as a root, so put it in the table instead.
            // - The container in the table has a "Re:" version of this subject,
            // and this container has a non-"Re:" version of this subject.
            // The non-re version is the more interesting of the two.
            //
            if (old == null || (c.threadable == null && old.threadable != null) || (old.threadable != null && old.threadable.subjectIsReply() && c.threadable != null && !c.threadable.subjectIsReply())) {
                subjTable.put(subj, c);
                count++;
            }
        }

        if (count == 0) {
            return;
        }

        // The subj_table is now populated with one entry for each subject which
        // occurs in the root set. Now iterate over the root set, and gather
        // together the difference.
        //
        ThreadContainer prev, c, rest;
        for (prev = null, c = rootNode.child, rest = c.next; c != null; prev = c, c = rest, rest = (rest == null ? null : rest.next)) {

            Threadable threadable = c.threadable;
            if (threadable == null) {
                threadable = c.child.threadable;
            }

            final String subj = threadable.simplifiedSubject();

            // Don't thread together all subjectless messages; let them dangle.
            if (com.openexchange.java.Strings.isEmpty(subj)) {
                continue;
            }

            final ThreadContainer old = subjTable.get(subj);
            if (old == c) {
                continue;
            }

            // Ok, so now we have found another container in the root set with
            // the same subject. There are a few possibilities:
            //
            // - If both are dummies, append one's children to the other, and remove
            // the now-empty container.
            //
            // - If one container is a dummy and the other is not, make the non-dummy
            // one be a child of the dummy, and a sibling of the other "real"
            // messages with the same subject (the dummy's children.)
            //
            // - If that container is a non-dummy, and that message's subject does
            // not begin with "Re:", but *this* message's subject does, then
            // make this be a child of the other.
            //
            // - If that container is a non-dummy, and that message's subject begins
            // with "Re:", but *this* message's subject does *not*, then make that
            // be a child of this one -- they were misordered. (This happens
            // somewhat implicitly, since if there are two messages, one with Re:
            // and one without, the one without will be in the hash table,
            // regardless of the order in which they were seen.)
            //
            // - Otherwise, make a new dummy container and make both messages be a
            // child of it. This catches the both-are-replies and neither-are-
            // replies cases, and makes them be siblings instead of asserting a
            // hierarchical relationship which might not be true.
            //
            // (People who reply to messages without using "Re:" and without using
            // a References line will break this slightly. Those people suck.)
            //
            // (It has occurred to me that taking the date or message number into
            // account would be one way of resolving some of the ambiguous cases,
            // but that's not altogether straightforward either.)

            // Remove the "second" message from the root set.
            if (prev == null) {
                rootNode.child = c.next;
            } else {
                prev.next = c.next;
            }
            c.next = null;

            if (old.threadable == null && c.threadable == null) {
                // They're both dummies; merge them.
                ThreadContainer tail;
                for (tail = old.child; tail != null && tail.next != null; tail = tail.next) {
                    // Nothing
                }
                tail.next = c.child;
                for (tail = c.child; tail != null; tail = tail.next) {
                    tail.parent = old;
                }
                c.child = null;

            } else if (old.threadable == null || // old is empty, or
            (c.threadable != null && c.threadable.subjectIsReply() && // c has Re, and
            !old.threadable.subjectIsReply())) { // old does not.
                // Make this message be a child of the other.
                c.parent = old;
                c.next = old.child;
                old.child = c;

            } else if (!insistOnRe && (c.threadable != null && !c.threadable.subjectIsReply() && // c has *no* Re, and
            !old.threadable.subjectIsReply())) { // old does not, too.
                // Make this message be a child of the other.
                c.parent = old;
                c.next = old.child;
                old.child = c;

            } else {
                // Make the old and new messages be children of a new dummy container.
                // We do this by creating a new container object for old->msg and
                // transforming the old container into a dummy (by merely emptying it),
                // so that the hash table still points to the one that is at depth 0
                // instead of depth 1.

                final ThreadContainer newc = new ThreadContainer();
                newc.threadable = old.threadable;
                // newc.debug_id = old.debug_id;
                newc.child = old.child;
                for (ThreadContainer tail = newc.child; tail != null; tail = tail.next) {
                    tail.parent = newc;
                }

                old.threadable = null;
                old.child = null;
                // old.debug_id = null;

                c.parent = old;
                newc.parent = old;

                // old is now a dummy; make it have exactly two kids, c and newc.
                old.child = c;
                c.next = newc;
            }

            // we've done a merge, so keep the same `prev' next time around.
            c = prev;
        }
    }
}
