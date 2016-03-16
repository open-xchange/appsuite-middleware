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
    public NOTTerm(final SearchTerm<?> term) {
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
    public void setTerm(final SearchTerm term) {
        this.term = term;
    }


    @Override
    public void addMailField(final Collection<MailField> col) {
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
    public boolean matches(final Message msg) throws OXException {
        return !term.matches(msg);
    }

    @Override
    public boolean matches(final MailMessage mailMessage) throws OXException {
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
