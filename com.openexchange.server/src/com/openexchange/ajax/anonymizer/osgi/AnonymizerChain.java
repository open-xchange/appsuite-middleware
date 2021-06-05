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
