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

import com.openexchange.mail.MailField;
import com.openexchange.mail.MailListField;

/**
 * {@link SearchUtility}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SearchUtility {

    /**
     * Initializes a new {@link SearchUtility}
     */
    private SearchUtility() {
        super();
    }

    /**
     * Parses the fields and pattern received from UI into an appropriate instance of {@link SearchTerm}.
     * <p>
     * Currently the supported search fields by UI are limited to:
     * <ul>
     * <li>{@link MailListField#FROM}</li>
     * <li>{@link MailListField#TO}</li>
     * <li>{@link MailListField#CC}</li>
     * <li>{@link MailListField#BCC}</li>
     * <li>{@link MailListField#SUBJECT}</li>
     * </ul>
     * All other are mapped to an instance of {@link BodyTerm} to search for certain pattern inside a mail's text body.
     *
     * @param searchFields The search fields as an array of <code>int</code>
     * @param patterns The search patterns
     * @param linkWithOR Whether to link with a logical OR; otherwise to link with a logical AND
     * @return An appropriate search term
     */
    public static SearchTerm<?> parseFields(final int[] searchFields, final String[] patterns, final boolean linkWithOR) {
        final MailField[] fields = MailField.getFields(searchFields);
        SearchTerm<?> retval = null;
        for (int i = 0; i < fields.length; i++) {
            if (!com.openexchange.java.Strings.isEmpty(patterns[i])) {
                final SearchTerm<?> term;
                switch (fields[i]) {
                case FROM:
                    term = new FromTerm(patterns[i]);
                    break;
                case TO:
                    term = new ToTerm(patterns[i]);
                    break;
                case CC:
                    term = new CcTerm(patterns[i]);
                    break;
                case BCC:
                    term = new BccTerm(patterns[i]);
                    break;
                case SUBJECT:
                    term = new SubjectTerm(patterns[i]);
                    break;
                case BODY:
                    term = new BodyTerm(patterns[i]);
                    break;
                default:
                    term = new BodyTerm(patterns[i]);
                    break;
                }
                retval = (retval == null) ? term : (linkWithOR ? new ORTerm(retval, term) : new ANDTerm(retval, term));
            }
        }
        return retval;
    }
}
