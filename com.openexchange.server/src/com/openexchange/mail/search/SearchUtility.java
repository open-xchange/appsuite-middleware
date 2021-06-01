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
    public static SearchTerm<?> parseFields(int[] searchFields, String[] patterns, boolean linkWithOR) {
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
