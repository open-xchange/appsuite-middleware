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
import javax.mail.search.NotTerm;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * {@link NOTTerm}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class NOTTerm extends SearchTerm<SearchTerm<?>> {

    private static final long serialVersionUID = 2984060879603969678L;

    private SearchTerm<?> term;

    /**
     * Initializes a new {@link NOTTerm}
     */
    protected NOTTerm() {
        super();
        term = null;
    }

    /**
     * Initializes a new {@link NOTTerm}
     */
    public NOTTerm(SearchTerm<?> term) {
        super();
        this.term = term;
    }

    @Override
    public void accept(SearchTermVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Gets the search terms that should be linked with an OR as an array of {@link SearchTerm} with length <code>2</code>.
     *
     * @return The terms that should be linked with an OR
     */
    @Override
    public SearchTerm<?> getPattern() {
        return term;
    }

    /**
     * Sets the search term
     *
     * @param term The search term
     */
    public void setTerm(SearchTerm term) {
        this.term = term;
    }


    @Override
    public void addMailField(Collection<MailField> col) {
        term.addMailField(col);
    }

    @Override
    public javax.mail.search.SearchTerm getJavaMailSearchTerm() {
        return new NotTerm(term.getJavaMailSearchTerm());
    }

    @Override
    public javax.mail.search.SearchTerm getNonWildcardJavaMailSearchTerm() {
        return new NotTerm(term.getNonWildcardJavaMailSearchTerm());
    }

    @Override
    public void contributeTo(FetchProfile fetchProfile) {
        term.contributeTo(fetchProfile);
    }

    @Override
    public boolean matches(Message msg) throws OXException {
        return !term.matches(msg);
    }

    @Override
    public boolean matches(MailMessage mailMessage) throws OXException {
        return !term.matches(mailMessage);
    }

    @Override
    public SearchTerm<?> filter(final @SuppressWarnings("unchecked") Set<Class<? extends SearchTerm>> filterSet) {
        return term.filter(filterSet);
    }

    @Override
    public boolean isAscii() {
        return term.isAscii();
    }

    @Override
    public boolean containsWildcard() {
        return term.containsWildcard();
    }
}
