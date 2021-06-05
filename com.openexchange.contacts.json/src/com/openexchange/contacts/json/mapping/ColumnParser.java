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

package com.openexchange.contacts.json.mapping;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.contacts.json.actions.IDBasedContactAction;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.java.Strings;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link ColumnParser}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ColumnParser {

    /**
     * Gets the mapped contact fields to use for querying the contact service based on the supplied column IDs. Besides the plain mapping
     * via {@link ContactMapper#getFields}, this includes a special treatment for the {@link Contact#IMAGE1} column. Additionally, any
     * virtual field as defined at {@link IDBasedContactAction#VIRTUAL_FIELDS} is excluded implicitly.
     *
     * @param columnIDs The column IDs as requested by the client
     * @param mandatoryFields An optional list of mandatory contact fields to be added, independently of the requested column IDs
     * @return The contact fields to use for querying the contact service
     * @throws OXException
     */
    public static ContactField[] getFieldsToQuery(int[] columnIDs, ContactField...mandatoryFields) throws OXException {
        Set<ContactField> mandatory = new HashSet<>();
        mandatory.addAll(Arrays.asList(mandatoryFields));
        /*
         * check for special handling
         */
        for (int i = 0; i < columnIDs.length; i++) {
            if (Contact.IMAGE1_URL == columnIDs[i] || Contact.IMAGE1 == columnIDs[i]) {
                columnIDs[i] = Contact.NUMBER_OF_IMAGES; // query NUMBER_OF_IMAGES to set image URL afterwards
                mandatory.add(ContactField.IMAGE_LAST_MODIFIED);
            } else if (DataObject.LAST_MODIFIED_UTC == columnIDs[i]) {
                columnIDs[i] = DataObject.LAST_MODIFIED; // query LAST_MODIFIED to set LAST_MODIFIED_UTC afterwards
            }
        }
        /*
         * get mapped fields
         */
        return ContactMapper.getInstance().getFields(columnIDs, IDBasedContactAction.VIRTUAL_FIELDS, mandatory.toArray(new ContactField[mandatory.size()]));
    }

    /**
     * Parses column IDs from the supplied string, which is expected to contain numerical identifiers separated by the comma separator
     * '<code>,</code>'. Obsolete column IDs are replaced with the corresponding valid ones implicitly.
     * <p/>
     * Additionally, the <code>all</code> and <code>list</code> shortcuts are recognized and mapped to the column IDs defined at
     * {@link IDBasedContactAction#COLUMNS_ALIAS_ALL} and {@link IDBasedContactAction#COLUMNS_ALIAS_LIST} respectively.
     *
     * @param commaSeparated The value to parse
     * @return The parsed columnd IDs
     * @throws OXException
     */
    public static int[] parseColumns(String commaSeparated) throws OXException {
        if (null == commaSeparated) {
            return null;
        }
        if ("all".equals(commaSeparated)) {
            return IDBasedContactAction.COLUMNS_ALIAS_ALL;
        }
        if ("list".equals(commaSeparated)) {
            return IDBasedContactAction.COLUMNS_ALIAS_LIST;
        }
        String[] splitted = Strings.splitByComma(commaSeparated);
        int[] columnIDs = new int[splitted.length];
        try {
            for (int i = 0; i < splitted.length; i++) {
                columnIDs[i] = Integer.parseInt(splitted[i]);
            }
        } catch (NumberFormatException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(e, "columns", commaSeparated);
        }
        return columnIDs;
    }

    /**
     * Initializes a new {@link ColumnParser}.
     */
    private ColumnParser() {
        // prevent initialization
    }
}
