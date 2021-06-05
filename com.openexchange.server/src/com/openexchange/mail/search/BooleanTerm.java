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

        public static BooleanSearchTerm getInstance(boolean value) {
            return value ? _TRUE : _FALSE;
        }

        private static final long serialVersionUID = -8073302646525000957L;

        private final boolean value;

        private BooleanSearchTerm(boolean value) {
            super();
            this.value = value;
        }

        @Override
        public boolean match(Message msg) {
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
    private BooleanTerm(boolean value) {
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
    public void addMailField(Collection<MailField> col) {
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
    public boolean matches(Message msg) throws OXException {
        return value;
    }

    @Override
    public boolean matches(MailMessage mailMessage) {
        return value;
    }
}
