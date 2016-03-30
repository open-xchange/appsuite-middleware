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

package com.openexchange.contacts.json.mapping;

import com.openexchange.contacts.json.actions.ContactAction;
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
     * virtual field as defined at {@link ContactAction#VIRTUAL_FIELDS} is excluded implicitly.
     *
     * @param columnIDs The column IDs as requested by the client
     * @param mandatoryFields An optional list of mandatory contact fields to be added, independently of the requested column IDs
     * @return The contact fields to use for querying the contact service
     * @throws OXException
     */
    public static ContactField[] getFieldsToQuery(int[] columnIDs, ContactField...mandatoryFields) throws OXException {
        /*
         * check for special handling
         */
        for (int i = 0; i < columnIDs.length; i++) {
            if (Contact.IMAGE1_URL == columnIDs[i] || Contact.IMAGE1 == columnIDs[i]) {
                columnIDs[i] = Contact.NUMBER_OF_IMAGES; // query NUMBER_OF_IMAGES to set image URL afterwards
            } else if (DataObject.LAST_MODIFIED_UTC == columnIDs[i]) {
                columnIDs[i] = DataObject.LAST_MODIFIED; // query LAST_MODIFIED to set LAST_MODIFIED_UTC afterwards
            }
        }
        /*
         * get mapped fields
         */
        return ContactMapper.getInstance().getFields(columnIDs, ContactAction.VIRTUAL_FIELDS, mandatoryFields);
    }

    /**
     * Parses column IDs from the supplied string, which is expected to contain numerical identifiers separated by the comma separator
     * '<code>,</code>'. Obsolete column IDs are replaced with the corresponding valid ones implicitly.
     * <p/>
     * Additionally, the <code>all</code> and <code>list</code> shortcuts are recognized and mapped to the column IDs defined at
     * {@link ContactAction#COLUMNS_ALIAS_ALL} and {@link ContactAction#COLUMNS_ALIAS_LIST} respectively.
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
            return ContactAction.COLUMNS_ALIAS_ALL;
        }
        if ("list".equals(commaSeparated)) {
            return ContactAction.COLUMNS_ALIAS_LIST;
        }
        String[] splitted = Strings.splitByComma(commaSeparated);
        int[] columnIDs = new int[splitted.length];
        try {
            for (int i = 0; i < splitted.length; i++) {
                columnIDs[i] = replaceObsoleteColumnID(Integer.valueOf(splitted[i]));
            }
        } catch (NumberFormatException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(e, "columns", commaSeparated);
        }
        return columnIDs;
    }

    /**
     * Replaces any obsolete column ID in the supplied array with it's correct one in case it indicates an outdated column ID.<p/>
     * See also Bug #25300.
     *
     * @param columnIDs The column IDs
     * @return The array containing valid column ID
     */
    public static int[] replaceObsoleteColumnIDs(int[] columnIDs) throws OXException {
        if (null != columnIDs) {
            for (int i = 0; i < columnIDs.length; i++) {
                columnIDs[i] = replaceObsoleteColumnID(columnIDs[i]);
            }
        }
        return columnIDs;
    }

    /**
     * Replaces the supplied column ID with the correct one in case it indicates an outdated column ID.<p/>
     * See also Bug #25300.
     *
     * @param columnID The column ID
     * @return The replaced column ID in case a replacement is necessary, or the ID itself, otherwise
     */
    private static int replaceObsoleteColumnID(int columnID) {
        switch (columnID) {
        case 610:
            return Contact.YOMI_FIRST_NAME;
        case 611:
            return Contact.YOMI_LAST_NAME;
        case 612:
            return Contact.YOMI_COMPANY;
        case 613:
            return Contact.ADDRESS_HOME;
        case 614:
            return Contact.ADDRESS_BUSINESS;
        case 615:
            return Contact.ADDRESS_OTHER;
        default:
            return columnID;
        }
    }

    /**
     * Initializes a new {@link ColumnParser}.
     */
    private ColumnParser() {
        // prevent initialization
    }
}
