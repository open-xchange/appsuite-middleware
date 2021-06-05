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

import java.util.Collection;
import java.util.Set;
import javax.mail.FetchProfile;
import javax.mail.Message;
import javax.mail.search.OrTerm;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * {@link ORTerm}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ORTerm extends CatenatingTerm {

    private static final long serialVersionUID = 2984060879603969678L;

    /**
     * Initializes a new {@link ORTerm}
     */
    protected ORTerm() {
        super();
    }

    /**
     * Initializes a new {@link ORTerm}
     */
    public ORTerm(SearchTerm<?> firstTerm, SearchTerm<?> secondTerm) {
        super(firstTerm, secondTerm);
    }

    @Override
    public void accept(SearchTermVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void addMailField(Collection<MailField> col) {
        terms[0].addMailField(col);
        terms[1].addMailField(col);
    }

    @Override
    public javax.mail.search.SearchTerm getJavaMailSearchTerm() {
        SearchTerm<?> t1 = terms[0];
        SearchTerm<?> t2 = terms[1];
        if (t1 instanceof BooleanTerm) {
            // Neutral
            return t2.getJavaMailSearchTerm();
        }
        if (t2 instanceof BooleanTerm) {
            // Neutral
            return t1.getJavaMailSearchTerm();
        }
        return new OrTerm(t1.getJavaMailSearchTerm(), t2.getJavaMailSearchTerm());
    }

    @Override
    public javax.mail.search.SearchTerm getNonWildcardJavaMailSearchTerm() {
        SearchTerm<?> t1 = terms[0];
        SearchTerm<?> t2 = terms[1];
        if (t1 instanceof BooleanTerm) {
            // Neutral
            return t2.getNonWildcardJavaMailSearchTerm();
        }
        if (t2 instanceof BooleanTerm) {
            // Neutral
            return t1.getNonWildcardJavaMailSearchTerm();
        }
        return new OrTerm(t1.getNonWildcardJavaMailSearchTerm(), t2.getNonWildcardJavaMailSearchTerm());
    }

    @Override
    public void contributeTo(FetchProfile fetchProfile) {
        terms[0].contributeTo(fetchProfile);
        terms[1].contributeTo(fetchProfile);
    }

    @Override
    public boolean matches(Message msg) throws OXException {
        return terms[0].matches(msg) || terms[1].matches(msg);
    }

    @Override
    public boolean matches(MailMessage mailMessage) throws OXException {
        return terms[0].matches(mailMessage) || terms[1].matches(mailMessage);
    }

    @Override
    public SearchTerm<?> filter(final @SuppressWarnings("unchecked") Set<Class<? extends SearchTerm>> filterSet) {
        if (filterSet.contains(getClass())) {
            return BooleanTerm.FALSE;
        }
        final ORTerm orTerm = new ORTerm();
        if (filterSet.contains(terms[0].getClass())) {
            /*
             * Replace with neutral element
             */
            orTerm.setFirstTerm(BooleanTerm.FALSE);
        } else {
            orTerm.setFirstTerm(terms[1].filter(filterSet));
        }
        if (filterSet.contains(terms[1].getClass())) {
            /*
             * Replace with neutral element which fits in any case no matter if first element has already been replaced or not.
             */
            orTerm.setSecondTerm(BooleanTerm.FALSE);
        } else {
            orTerm.setSecondTerm(terms[1].filter(filterSet));
        }
        return orTerm;
    }

    @Override
    public boolean isAscii() {
        return terms[0].isAscii() && terms[1].isAscii();
    }

    @Override
    public boolean containsWildcard() {
        return terms[0].containsWildcard() || terms[1].containsWildcard();
    }
}
