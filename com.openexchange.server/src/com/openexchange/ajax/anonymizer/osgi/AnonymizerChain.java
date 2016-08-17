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

package com.openexchange.ajax.anonymizer.osgi;

import java.util.Iterator;
import com.openexchange.ajax.anonymizer.AnonymizerService;
import com.openexchange.ajax.anonymizer.Module;
import com.openexchange.exception.OXException;
import com.openexchange.java.SortableConcurrentList;
import com.openexchange.session.Session;


/**
 * {@link AnonymizerChain}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class AnonymizerChain<E> implements AnonymizerService<E> {

    private final SortableConcurrentList<RankedAnonymizerService<E>> anonymizers;
    private final Module module;

    /**
     * Initializes a new {@link AnonymizerChain}.
     */
    public AnonymizerChain(Module module) {
        super();
        this.module = module;
        anonymizers = new SortableConcurrentList<RankedAnonymizerService<E>>();
    }

    /**
     * Adds given anonymizer
     *
     * @param anonymizer The anonymizer to remove
     * @param ranking The anonymizer's ranking
     * @return <code>true</code> if added; otherwise <code>false</code>
     */
    public boolean addAnonymizer(AnonymizerService<E> anonymizer, int ranking) {
        return anonymizers.add(new RankedAnonymizerService<E>(anonymizer, ranking));
    }

    /**
     * Removes given anonymizer
     *
     * @param anonymizer The anonymizer to remove
     * @param ranking The anonymizer's ranking
     */
    public void removeAnonymizer(AnonymizerService<E> anonymizer, int ranking) {
        anonymizers.remove(new RankedAnonymizerService<E>(anonymizer, ranking));
    }

    @Override
    public Module getModule() {
        return module;
    }

    @Override
    public E anonymize(E entity, Session session) throws OXException {
        if (null == entity) {
            return entity;
        }

        E retval = entity;
        for (Iterator<RankedAnonymizerService<E>> it = anonymizers.iterator(); it.hasNext();) {
            RankedAnonymizerService<E> anonymizer = it.next();
            retval = anonymizer.anonymize(entity, session);
        }
        return retval;
    }

    // ----------------------------------------------------------------------------------------------------------------------

    private static final class RankedAnonymizerService<E> implements AnonymizerService<E>, Comparable<RankedAnonymizerService<E>> {

        private final AnonymizerService<E> anonymizer;
        private final int ranking;
        private final int hash;

        RankedAnonymizerService(AnonymizerService<E> anonymizer, int ranking) {
            super();
            this.anonymizer = anonymizer;
            this.ranking = ranking;

            int prime = 31;
            int result = 1;
            result = prime * result + ((anonymizer == null) ? 0 : anonymizer.hashCode());
            result = prime * result + ranking;
            this.hash = result;
        }

        @Override
        public int compareTo(RankedAnonymizerService<E> o) {
            int thisRanking = this.ranking;
            int otherRanking = o.ranking;
            return thisRanking < otherRanking ? 1 : (thisRanking == otherRanking ? 0 : -1);
        }

        @Override
        public Module getModule() {
            return anonymizer.getModule();
        }

        @Override
        public E anonymize(E entity, Session session) throws OXException {
            return anonymizer.anonymize(entity, session);
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof RankedAnonymizerService)) {
                return false;
            }
            @SuppressWarnings("unchecked") RankedAnonymizerService<E> other = (RankedAnonymizerService<E>) obj;
            if (ranking != other.ranking) {
                return false;
            }
            if (anonymizer == null) {
                if (other.anonymizer != null) {
                    return false;
                }
            } else if (!anonymizer.equals(other.anonymizer)) {
                return false;
            }
            return true;
        }
    }

}
