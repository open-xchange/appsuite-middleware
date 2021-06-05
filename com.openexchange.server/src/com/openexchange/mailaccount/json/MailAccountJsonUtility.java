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

package com.openexchange.mailaccount.json;

import static com.openexchange.java.Strings.isEmpty;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.java.util.Tools;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link MailAccountJsonUtility}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class MailAccountJsonUtility {

    /**
     * Initializes a new {@link MailAccountJsonUtility}.
     */
    private MailAccountJsonUtility() {
        super();
    }

    /**
     * Parses the attributes from passed comma-separated list.
     *
     * @param colString The comma-separated list
     * @return The parsed attributes
     */
    public static List<Attribute> getColumns(String colString) {
        List<Attribute> attributes = null;
        if (Strings.isNotEmpty(colString)) {
            if ("all".equalsIgnoreCase(colString)) {
                // All columns
                return Arrays.asList(Attribute.values());
            }

            attributes = new LinkedList<Attribute>();
            for (String col : Strings.splitByComma(colString)) {
                if (Strings.isNotEmpty(col)) {
                    int id = parseInt(col);
                    Attribute attr = id > 0 ? Attribute.getById(id) : null;
                    if (null != attr) {
                        attributes.add(attr);
                    }
                }
            }
            return attributes;
        }

        // All columns
        return Arrays.asList(Attribute.values());
    }

    private static int parseInt(String col) {
        return Tools.getUnsignedInteger(col);
    }

    /**
     * Checks validity of values for needed fields.
     *
     * @param accountDescription The account description
     * @param checkForPrimaryAddress <code>true</code> to check for primary address presence; otherwise <code>false</code>
     * @throws OXException If a needed field's value is invalid
     */
    public static void checkNeededFields(final MailAccountDescription accountDescription, boolean checkForPrimaryAddress) throws OXException {
        // Check needed fields
        if (isEmpty(accountDescription.getMailServer())) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(MailAccountFields.MAIL_URL);
        }
        if (isEmpty(accountDescription.getLogin())) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(MailAccountFields.LOGIN);
        }
        if (isEmpty(accountDescription.getPassword()) && false == accountDescription.isMailOAuthAble()) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(MailAccountFields.PASSWORD);
        }
        if (checkForPrimaryAddress && isEmpty(accountDescription.getPrimaryAddress())) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(MailAccountFields.PRIMARY_ADDRESS);
        }
    }

}
