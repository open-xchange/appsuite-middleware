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

/**
 * The ThreadContainer object is used to encapsulate an {@code Threadable} object (it holds some intermediate state used while threading.)
 * This is a friendly class that doesn't escape from this module.
 */
final class ThreadContainer {

    Threadable threadable;
    ThreadContainer parent;
    ThreadContainer child;
    ThreadContainer next;

    /**
     * Initializes a new {@link ThreadContainer}.
     */
    ThreadContainer() {
        super();
    }

    // Copy the ThreadContainer tree structure down into the underlying
    // Threadable objects (that is, make the Threadable tree look like
    // the ThreadContainer tree.)
    //
    @SuppressWarnings("null")
    void flush() {
        ThreadContainer tc = this;
        while (tc != null) {
            final Threadable threadable = tc.threadable;
            final boolean hasThreadable = threadable != null;
            if (tc.parent != null && !hasThreadable) {
                // Only the rootNode is allowed to not have a threadable.
                throw new Error("no threadable in " + this.toString());
            }
            // Drop parent reference
            tc.parent = null;
            // Handle child
            final ThreadContainer childContainer = tc.child;
            if (hasThreadable) {
                threadable.setChild(childContainer == null ? null : childContainer.threadable);
            }
            if (childContainer != null) {
                childContainer.flush();
                tc.child = null;
            }
            // Handle next
            final ThreadContainer nextContainer = tc.next;
            if (hasThreadable) {
                threadable.setNext(nextContainer == null ? null : nextContainer.threadable);
            }
            // Drop next reference & point to next sibling
            tc.next = null;
            tc.threadable = null;
            tc = nextContainer;
        }
        /*-
         *
         *

        if (threadable != null) {
            threadable.setChild(child == null ? null : child.threadable);
        }

        if (child != null) {
            child.flush();
            child = null;
        }

        if (threadable != null) {
            threadable.setNext(next == null ? null : next.threadable);
        }

        if (next != null) {
            next.flush();
            next = null;
        }

        threadable = null;
         *
         */
    }

    /**
     * Returns <code>true</code> if child is under self's tree. This is used for detecting circularities in the references header.
     *
     * @param target The target container
     * @return <code>true</code> if child is under self's tree; otherwise <code>false</code>
     */
    boolean findChild(final ThreadContainer target) {
        try {
            if (child == null) {
                return false;
            } else if (child == target) {
                return true;
            } else {
                return child.findChild(target);
            }
        } catch (final StackOverflowError error) {
            return false;
        }
    }

    /**
     * Reverse the children (child through child.next.next.next...)
     */
    void reverseChildren() {
        if (child != null) {
            // reverse the children (child through child.next.next.next...)
            ThreadContainer kid, prev, rest;
            for (prev = null, kid = child, rest = kid.next; kid != null; prev = kid, kid = rest, rest = (rest == null ? null : rest.next)) {
                kid.next = prev;
            }
            child = prev;

            // then do it for the kids
            for (kid = child; kid != null; kid = kid.next) {
                kid.reverseChildren();
            }
        }
    }
}
