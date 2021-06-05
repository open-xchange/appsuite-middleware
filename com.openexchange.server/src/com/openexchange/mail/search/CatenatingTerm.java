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

package com.openexchange.mail.search;


/**
 * {@link CatenatingTerm}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public abstract class CatenatingTerm extends SearchTerm<SearchTerm<?>[]> {

    private static final long serialVersionUID = 5177121983676445047L;

    protected final SearchTerm<?>[] terms;

    /**
     * Initializes a new {@link CatenatingTerm}
     */
    protected CatenatingTerm() {
        super();
        terms = new SearchTerm<?>[2];
    }

    /**
     * Initializes a new {@link CatenatingTerm}
     */
    public CatenatingTerm(SearchTerm<?> firstTerm, SearchTerm<?> secondTerm) {
        super();
        terms = new SearchTerm<?>[] { firstTerm, secondTerm };
    }

    /**
     * Gets the search terms that should be catenated as an array of {@link SearchTerm} with length <code>2</code>.
     *
     * @return The terms that should be linked with each other
     */
    @Override
    public SearchTerm<?>[] getPattern() {
        return terms;
    }

    /**
     * Sets the first search term
     *
     * @param firstTerm The first search term
     */
    public void setFirstTerm(SearchTerm<?> firstTerm) {
        terms[0] = firstTerm;
    }

    /**
     * Sets the second search term
     *
     * @param secondTerm The second search term
     */
    public void setSecondTerm(SearchTerm<?> secondTerm) {
        terms[1] = secondTerm;
    }

    /**
     * Gets the first search term
     */
    public SearchTerm<?> getFirstTerm() {
        return terms[0];
    }

    /**
     * Gets the second search term
     */
    public SearchTerm<?> getSecondTerm() {
        return terms[1];
    }

}
