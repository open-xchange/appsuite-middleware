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
import javax.mail.FetchProfile;
import javax.mail.Message;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * {@link BooleanTerm}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class BooleanTerm extends SearchTerm<Boolean> {

    private static final long serialVersionUID = 5351872902045670432L;

    private static final class BooleanSearchTerm extends javax.mail.search.SearchTerm {

        private static final BooleanSearchTerm _TRUE = new BooleanSearchTerm(true);

        private static final BooleanSearchTerm _FALSE = new BooleanSearchTerm(false);

        public static BooleanSearchTerm getInstance(final boolean value) {
            return value ? _TRUE : _FALSE;
        }

        private static final long serialVersionUID = -8073302646525000957L;

        private final boolean value;

        private BooleanSearchTerm(final boolean value) {
            super();
            this.value = value;
        }

        @Override
        public boolean match(final Message msg) {
            return value;
        }

    }

    /**
     * The boolean term for <code>true</code>
     */
    public static final BooleanTerm TRUE = new BooleanTerm(true);

    /**
     * The boolean term for <code>false</code>
     */
    public static final BooleanTerm FALSE = new BooleanTerm(false);

    private final boolean value;

    /**
     * Initializes a new {@link BooleanTerm}
     */
    private BooleanTerm(final boolean value) {
        super();
        this.value = value;
    }

    @Override
    public void accept(SearchTermVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Boolean getPattern() {
        return Boolean.valueOf(value);
    }

    @Override
    public void addMailField(final Collection<MailField> col) {
        // Nothing
    }

    @Override
    public javax.mail.search.SearchTerm getJavaMailSearchTerm() {
        return BooleanSearchTerm.getInstance(value);
    }

    @Override
    public javax.mail.search.SearchTerm getNonWildcardJavaMailSearchTerm() {
        return getJavaMailSearchTerm();
    }

    @Override
    public void contributeTo(FetchProfile fetchProfile) {
        // Nothing
    }

    @Override
    public boolean matches(final Message msg) throws OXException {
        return value;
    }

    @Override
    public boolean matches(final MailMessage mailMessage) {
        return value;
    }
}
