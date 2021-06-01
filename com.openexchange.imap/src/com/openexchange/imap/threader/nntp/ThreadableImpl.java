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
    public ThreadableImpl(com.openexchange.imap.threader.Threadable delegatee) {
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
    public void setChild(Threadable child) {
        delegatee.setChild(null == child ? null : ((ThreadableImpl) child).delegatee);
    }

    @Override
    public void setNext(Threadable next) {
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

        protected IteratorImpl(Enumeration<com.openexchange.imap.threader.Threadable> enumeration) {
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
