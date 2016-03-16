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

package com.openexchange.imap.threader.nntp;

import java.util.Enumeration;
import java.util.Iterator;
import org.apache.commons.net.nntp.Threadable;

/**
 * {@link ThreadableImpl} - Implements {@code org.apache.commons.net.nntp.Threadable};<br>
 * a placeholder interface for threadable message objects
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ThreadableImpl implements Threadable, Iterable<ThreadableImpl> {

    private final com.openexchange.imap.threader.Threadable delegatee;

    /**
     * Initializes a new {@link ThreadableImpl}.
     */
    public ThreadableImpl(final com.openexchange.imap.threader.Threadable delegatee) {
        super();
        this.delegatee = delegatee;
    }

    /**
     * Gets the delegatee.
     *
     * @return The delegatee
     */
    public com.openexchange.imap.threader.Threadable getDelegatee() {
        return delegatee;
    }

    /**
     * Gets the child
     *
     * @return The child
     */
    public Threadable getChild() {
        return new ThreadableImpl(delegatee.kid());
    }

    /**
     * Gets the next
     *
     * @return The next
     */
    public Threadable getNext() {
        return new ThreadableImpl(delegatee.next());
    }

    @Override
    public boolean isDummy() {
        return delegatee.isDummy();
    }

    @Override
    public String messageThreadId() {
        return delegatee.messageID();
    }

    @Override
    public String[] messageThreadReferences() {
        final String[] messageReferences = delegatee.messageReferences();
        return messageReferences == null ? new String[0] : messageReferences;
    }

    @Override
    public String simplifiedSubject() {
        return delegatee.simplifiedSubject();
    }

    @Override
    public boolean subjectIsReply() {
        return delegatee.subjectIsReply();
    }

    @Override
    public void setChild(final Threadable child) {
        delegatee.setChild(null == child ? null : ((ThreadableImpl) child).delegatee);
    }

    @Override
    public void setNext(final Threadable next) {
        delegatee.setNext(null == next ? null : ((ThreadableImpl) next).delegatee);
    }

    @Override
    public Threadable makeDummy() {
        return new ThreadableImpl(delegatee.makeDummy());
    }

    @Override
    public Iterator<ThreadableImpl> iterator() {
        return new IteratorImpl(delegatee.allElements());
    }

    private static final class IteratorImpl implements Iterator<ThreadableImpl> {

        private final Enumeration<com.openexchange.imap.threader.Threadable> enumeration;

        protected IteratorImpl(final Enumeration<com.openexchange.imap.threader.Threadable> enumeration) {
            super();
            this.enumeration = enumeration;
        }

        @Override
        public boolean hasNext() {
            return enumeration.hasMoreElements();
        }

        @Override
        public ThreadableImpl next() {
            return new ThreadableImpl(enumeration.nextElement());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove() method not supported.");
        }
    }

}
