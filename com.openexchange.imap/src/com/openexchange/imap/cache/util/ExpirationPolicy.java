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

package com.openexchange.imap.cache.util;

import com.javacodegeeks.concurrent.ConcurrentLinkedHashMap.Entry;
import com.javacodegeeks.concurrent.EvictionPolicy;

/**
 * {@link ExpirationPolicy} - Expiration eviction policy for {@code com.javacodegeeks.concurrent.ConcurrentLinkedHashMap}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ExpirationPolicy implements EvictionPolicy {

    private static interface Checker {

        boolean check(final Entry<?, ?> accessedEntry, final long now);
    }

    private static final Checker DUMMY_CHECKER = new Checker() {

        @Override
        public boolean check(final Entry<?, ?> accessedEntry, final long now) {
            return true;
        }
    };

    private static final class AgeChecker implements Checker {

        private final long ageThresholdMillis;

        protected AgeChecker(final long ageThresholdMillis) {
            super();
            this.ageThresholdMillis = ageThresholdMillis;
        }

        @Override
        public boolean check(final Entry<?, ?> accessedEntry, final long now) {
            final long accessedEntryAge = (now - accessedEntry.getCreationTime());
            return accessedEntryAge < ageThresholdMillis;
        }

    }

    private static final class IdleChecker implements Checker {

        private final long idleTimeThresholdMillis;

        protected IdleChecker(final long idleTimeThresholdMillis) {
            super();
            this.idleTimeThresholdMillis = idleTimeThresholdMillis;
        }

        @Override
        public boolean check(final Entry<?, ?> accessedEntry, final long now) {
            final long accessedEntryIdleTime = (now - accessedEntry.getLastAccessTime());
            return accessedEntryIdleTime < idleTimeThresholdMillis;
        }

    }

    private final Checker ageChecker;
    private final Checker idleChecker;

    /**
     * Initializes a new {@link ExpirationPolicy}.
     *
     * @param ageThresholdMillis The age threshold; pass 0 or less to ignore
     * @param idleTimeThresholdMillis The idle threshold; pass 0 or less to ignore
     */
    public ExpirationPolicy(final long ageThresholdMillis, final long idleTimeThresholdMillis) {
        super();
        this.ageChecker = ageThresholdMillis <= 0 ? DUMMY_CHECKER : new AgeChecker(ageThresholdMillis);
        this.idleChecker = idleTimeThresholdMillis <= 0 ? DUMMY_CHECKER : new IdleChecker(idleTimeThresholdMillis);
    }

    @Override
    public boolean accessOrder() {
        return true;
    }

    @Override
    public boolean insertionOrder() {
        return false;
    }

    @Override
    public Entry<?, ?> evictElement(final Entry<?, ?> head) {
        return head.getAfter();
    }

    @Override
    public Entry<?, ?> recordInsertion(final Entry<?, ?> head, final Entry<?, ?> insertedEntry) {
        return null;
    }

    @Override
    public Entry<?, ?> recordAccess(final Entry<?, ?> head, final Entry<?, ?> accessedEntry) {
        final long now = System.currentTimeMillis();
        if (idleChecker.check(accessedEntry, now) && ageChecker.check(accessedEntry, now)) {
            return head;
        }
        return accessedEntry.getAfter();
    }

}
