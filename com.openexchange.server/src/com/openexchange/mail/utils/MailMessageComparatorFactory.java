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

package com.openexchange.mail.utils;

import java.util.Locale;
import com.openexchange.mail.FlaggingMode;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.session.Session;

/**
 * {@link MailMessageComparatorFactory}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class MailMessageComparatorFactory {

    public static MailMessageComparator createComparator(MailSortField sortField, OrderDirection order, Locale locale, Session session, boolean userFlagsEnabled) {
        Integer flaggingColor = FlaggingMode.FLAGGED_IMPLICIT.equals(FlaggingMode.getFlaggingMode(session)) ? FlaggingMode.getFlaggingColor(session) : null;
        return new MailMessageComparator(sortField, order == OrderDirection.DESC, locale, userFlagsEnabled, flaggingColor);
    }

}
