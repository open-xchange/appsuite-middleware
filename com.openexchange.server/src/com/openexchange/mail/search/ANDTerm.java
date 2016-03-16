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
import javax.mail.search.AndTerm;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * {@link ANDTerm}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ANDTerm extends CatenatingTerm {

    private static final long serialVersionUID = 2696976140249947009L;

    /**
     * Initializes a new {@link ANDTerm}
     */
    protected ANDTerm() {
        super();
    }

    /**
     * Initializes a new {@link ANDTerm}
     */
    public ANDTerm(final SearchTerm<?> firstTerm, final SearchTerm<?> secondTerm) {
        super(firstTerm, secondTerm);
    }

    @Override
    public void accept(SearchTermVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void addMailField(final Collection<MailField> col) {
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
        return new AndTerm(t1.getJavaMailSearchTerm(), t2.getJavaMailSearchTerm());
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
        return new AndTerm(t1.getNonWildcardJavaMailSearchTerm(), t2.getNonWildcardJavaMailSearchTerm());
    }

    @Override
    public void contributeTo(FetchProfile fetchProfile) {
        terms[0].contributeTo(fetchProfile);
        terms[1].contributeTo(fetchProfile);
    }

    @Override
    public boolean matches(final Message msg) throws OXException {
        return terms[0].matches(msg) && terms[1].matches(msg);
    }

    @Override
    public boolean matches(final MailMessage mailMessage) throws OXException {
        return terms[0].matches(mailMessage) && terms[1].matches(mailMessage);
    }

    @Override
    public SearchTerm<?> filter(final @SuppressWarnings("unchecked") Set<Class<? extends SearchTerm>> filterSet) {
        if (filterSet.contains(getClass())) {
            return BooleanTerm.FALSE;
        }
        final ANDTerm andTerm = new ANDTerm();
        final boolean replaceFirst = filterSet.contains(terms[0].getClass());
        if (replaceFirst) {
            /*
             * Replace with neutral element
             */
            andTerm.setFirstTerm(BooleanTerm.TRUE);
        } else {
            andTerm.setFirstTerm(terms[0].filter(filterSet));
        }
        if (filterSet.contains(terms[1].getClass())) {
            if (replaceFirst) {
                /*
                 * Replace with fail element since the first element has already been replaced with neutral element.
                 */
                andTerm.setSecondTerm(BooleanTerm.FALSE);
            } else {
                /*
                 * Replace with neutral element
                 */
                andTerm.setSecondTerm(BooleanTerm.TRUE);
            }
        } else {
            andTerm.setSecondTerm(terms[1].filter(filterSet));
        }
        return andTerm;
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
