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

package com.openexchange.contact.storage.rdb.search;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import com.openexchange.contact.storage.rdb.mapping.Mappers;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactConfig;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.groupware.tools.mappings.database.DbMapping;
import com.openexchange.java.Strings;
import com.openexchange.tools.StringCollection;

/**
 * {@link ContactSearchAdapter}
 *
 * Helps constructing the database statement for a contact search object.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ContactSearchAdapter extends DefaultSearchAdapter {

    private static ContactField startLetterField = null;

    private final StringBuilder stringBuilder;
    private final boolean utf8mb4;

    /**
     * Initializes a new {@link ContactSearchAdapter}.
     *
     * @param contactSearch The used contact search object
     * @param contextID the context ID
     * @param fields the fields to select
     * @param charset The used charset
     * @param utf8mb4 <code>true</code> to use collations based on utf8mb4, <code>false</code>, otherwise
     * @throws OXException
     */
    public ContactSearchAdapter(ContactSearchObject contactSearch, int contextID, ContactField[] fields, String charset, boolean utf8mb4, int forUser) throws OXException {
        super(charset);
        this.stringBuilder = new StringBuilder(256);
        this.utf8mb4 = utf8mb4;
        if (null != contactSearch.getPattern()) {
            appendSearch(contactSearch, contextID, fields, forUser);
        } else {
            appendSearchAlternative(contactSearch, contextID, fields, forUser);
        }
    }

    @Override
    public StringBuilder getClause() {
        return Strings.trim(stringBuilder);
    }

    private void appendSearch(ContactSearchObject contactSearch, int contextID, ContactField[] fields, int forUser) throws OXException {
        stringBuilder.append(getSelectClause(fields, forUser)).append(" WHERE ").append(getContextIDClause(contextID)).append(" AND ");
        /*
         * prefer startletter search if possible
         */
        if (false == contactSearch.isStartLetter() || false == appendStartLetterComparison(contactSearch.getPattern(), contactSearch.isExactMatch())) {
            /*
             * display name search, otherwise
             */
            this.stringBuilder.append(Mappers.CONTACT.get(ContactField.DISPLAY_NAME).getColumnLabel()).append(" LIKE ?");
            this.parameters.add(StringCollection.prepareForSearch(contactSearch.getPattern(), false, true, true));
        }
        /*
         * append folders & users
         */
        if (null != contactSearch.getFolders() && 0 < contactSearch.getFolders().length) {
            stringBuilder.append(" AND ").append(getFolderIDsClause(contactSearch.getFolders()));
        }
        if (null != contactSearch.getUserIds() && 0 < contactSearch.getUserIds().length) {
            stringBuilder.append(" AND ").append(getFolderIDsClause(contactSearch.getFolders()));
        }
    }

    private void appendSearchAlternative(ContactSearchObject contactSearch, int contextID, ContactField[] fields, int forUser) throws OXException {
        Map<ContactField, Object> comparisons = extractComparisons(contactSearch);
        String contextIDClause = getContextIDClause(contextID);
        String folderIDsClause = null != contactSearch.getFolders() && 0 < contactSearch.getFolders().length ? getFolderIDsClause(contactSearch.getFolders()) : null;
        String userIDsClause = null != contactSearch.getUserIds() && 0 < contactSearch.getUserIds().length ? getUserIDsClause(contactSearch.getUserIds()) : null;
        String selectClause = getSelectClause(fields, forUser);

        Iterator<Entry<ContactField, Object>> iterator = comparisons.entrySet().iterator();
        if (iterator.hasNext()) {
            boolean exact = contactSearch.isExactMatch();
            boolean emailAutoComplete = contactSearch.isEmailAutoComplete();
            if (contactSearch.isOrSearch() || emailAutoComplete) {
                /*
                 * construct clause using UNION SELECTs
                 */
                Entry<ContactField, Object> entry = iterator.next();
                appendComparison(contactSearch, contextIDClause, folderIDsClause, userIDsClause, selectClause, entry.getKey(), entry.getValue(), emailAutoComplete, exact);
                while (iterator.hasNext()) {
                    stringBuilder.append(" UNION ");
                    entry = iterator.next();
                    appendComparison(contactSearch, contextIDClause, folderIDsClause, userIDsClause, selectClause, entry.getKey(), entry.getValue(), emailAutoComplete, exact);
                }
            } else {
                /*
                 * construct clause using single SELECT
                 */
                stringBuilder.append(selectClause).append(" WHERE ").append(contextIDClause);
                if (null != folderIDsClause) {
                    stringBuilder.append(" AND ").append(folderIDsClause);
                }
                if (null != userIDsClause) {
                    stringBuilder.append(" AND ").append(userIDsClause);
                }
                Entry<ContactField, Object> entry = iterator.next();
                stringBuilder.append(" AND ");
                appendComparison(entry.getKey(), entry.getValue(), exact);
                while (iterator.hasNext()) {
                    entry = iterator.next();
                    stringBuilder.append(" AND ");
                    appendComparison(entry.getKey(), entry.getValue(), exact);
                }
                if (contactSearch.hasImage()) {
                    stringBuilder.append(" AND ").append(IMG_CLAUSE);
                }
            }
        } else {
            /*
             * no comparison, just use folders/users and context id
             */
            stringBuilder.append(selectClause).append(" WHERE ").append(contextIDClause);
            if (null != folderIDsClause) {
                stringBuilder.append(" AND ").append(folderIDsClause);
            }
            if (null != userIDsClause) {
                stringBuilder.append(" AND ").append(userIDsClause);
            }
            if (contactSearch.hasImage()) {
                stringBuilder.append(" AND ").append(IMG_CLAUSE);
            }
        }

    }

    private static final Pattern P_DIGIT = Pattern.compile("\\d");

    private static boolean isDigit(CharSequence input) {
        return P_DIGIT.matcher(input).matches();
    }

    private boolean appendStartLetterComparison(String pattern, boolean exact) throws OXException {
        String columnLabel = Mappers.CONTACT.get(getStartLetterField()).getColumnLabel();
        if (".".equals(pattern) || "#".equals(pattern)) {
            /*
             * no letter, no digit
             */
            stringBuilder.append('(').append(columnLabel).append("<'0%' OR ").append(columnLabel).append(">'z%') AND ").append(columnLabel).append("NOT LIKE 'z%'");
            return true;
        } else if (isDigit(pattern)) {
            /*
             * digit
             */
            stringBuilder.append(columnLabel).append(">'0%' AND ").append(columnLabel).append("<'a%'");
            return true;
        } else if (false == "all".equals(pattern)) {
            /*
             * match pattern with fallback
             */
            String fallbackColumnLabel = Mappers.CONTACT.get(ContactField.DISPLAY_NAME).getColumnLabel();
            stringBuilder.append('(').append(columnLabel).append(" LIKE ?");
            if (exact) {
                stringBuilder.append(" COLLATE ").append(utf8mb4 ? "utf8mb4_bin" : "utf8_bin");
            }
            stringBuilder.append(" OR (").append(columnLabel).append(" IS NULL AND ").append(fallbackColumnLabel).append(" LIKE ?");
            if (exact) {
                stringBuilder.append(" COLLATE ").append(utf8mb4 ? "utf8mb4_bin" : "utf8_bin");
            }
            stringBuilder.append("))");
            String preparedPattern = StringCollection.prepareForSearch(pattern, false, true, true);
            this.parameters.add(preparedPattern);
            this.parameters.add(preparedPattern);
            return true;
        } else {
            /*
             * no valid pattern
             */
            return false;
        }
    }

    private void appendComparison(ContactSearchObject cso, String contextIDClause, String folderIDsClause, String userIDsClause, String selectClause, ContactField field, Object value, boolean needsEMail, boolean exact) throws OXException {
        stringBuilder.append('(').append(selectClause);
        stringBuilder.append(" WHERE ").append(contextIDClause).append(" AND ");
        appendComparison(field, value, exact);
        if (null != folderIDsClause) {
            stringBuilder.append(" AND ").append(folderIDsClause);
        }
        if (null != userIDsClause) {
            stringBuilder.append(" AND ").append(userIDsClause);
        }
        if (needsEMail) {
            stringBuilder.append(" AND (").append(getEMailAutoCompleteClause()).append(')');
        }
        if (cso.hasImage()) {
            stringBuilder.append(" AND ").append(IMG_CLAUSE);
        }
        stringBuilder.append(')');
    }

    @SuppressWarnings("deprecation")
    private static Map<ContactField, Object> extractComparisons(ContactSearchObject contactSearch) {
        Map<ContactField, Object> comparisons = new HashMap<>();
        if (null != contactSearch.getSurname()) {
            comparisons.put(ContactField.SUR_NAME, contactSearch.getSurname());
        }
        if (null != contactSearch.getGivenName()) {
            comparisons.put(ContactField.GIVEN_NAME, contactSearch.getGivenName());
        }
        if (null != contactSearch.getDisplayName()) {
            comparisons.put(ContactField.DISPLAY_NAME, contactSearch.getDisplayName());
        }
        if (null != contactSearch.getEmail1()) {
            comparisons.put(ContactField.EMAIL1, contactSearch.getEmail1());
        }
        if (null != contactSearch.getEmail2()) {
            comparisons.put(ContactField.EMAIL2, contactSearch.getEmail2());
        }
        if (null != contactSearch.getEmail3()) {
            comparisons.put(ContactField.EMAIL3, contactSearch.getEmail3());
        }
        if (null != contactSearch.getCompany()) {
            comparisons.put(ContactField.COMPANY, contactSearch.getCompany());
        }
        if (null != contactSearch.getCatgories()) {
            comparisons.put(ContactField.CATEGORIES, contactSearch.getCatgories());
        }
        if (null != contactSearch.getStreetBusiness()) {
            comparisons.put(ContactField.STREET_BUSINESS, contactSearch.getStreetBusiness());
        }
        if (null != contactSearch.getCityBusiness()) {
            comparisons.put(ContactField.CITY_BUSINESS, contactSearch.getCityBusiness());
        }
        if (null != contactSearch.getDepartment()) {
            comparisons.put(ContactField.DEPARTMENT, contactSearch.getDepartment());
        }
        if (null != contactSearch.getYomiCompany()) {
            comparisons.put(ContactField.YOMI_COMPANY, contactSearch.getYomiCompany());
        }
        if (null != contactSearch.getYomiFirstName()) {
            comparisons.put(ContactField.YOMI_FIRST_NAME, contactSearch.getYomiFirstName());
        }
        if (null != contactSearch.getYomiLastName()) {
            comparisons.put(ContactField.YOMI_LAST_NAME, contactSearch.getYomiLastName());
        }

        if (0 != contactSearch.getIgnoreOwn() || null != contactSearch.getAnniversaryRange() || null != contactSearch.getBirthdayRange() || null != contactSearch.getBusinessPostalCodeRange() || null != contactSearch.getCreationDateRange() || null != contactSearch.getDynamicSearchField() || null != contactSearch.getDynamicSearchFieldValue() || null != contactSearch.getFrom() || null != contactSearch.getLastModifiedRange() || null != contactSearch.getNumberOfEmployeesRange() || null != contactSearch.getSalesVolumeRange() || null != contactSearch.getOtherPostalCodeRange() || null != contactSearch.getPrivatePostalCodeRange()) {
            throw new UnsupportedOperationException("not implemented");
        }

        return comparisons;
    }

    private void appendComparison(ContactField field, Object value, boolean exact) throws OXException {
        DbMapping<? extends Object, Contact> dbMapping = Mappers.CONTACT.get(field);
        if (isTextColumn(dbMapping)) {
            if (null != this.charset) {
                stringBuilder.append("CONVERT(").append(dbMapping.getColumnLabel()).append(" USING ").append(this.charset).append(')');
            } else {
                stringBuilder.append(dbMapping.getColumnLabel());
            }
            String preparedPattern = StringCollection.prepareForSearch((String) value, false, true);
            if (containsWildcards(preparedPattern)) {
                // use "LIKE" search
                stringBuilder.append(" LIKE ?");
                if (exact) {
                    stringBuilder.append(" COLLATE ").append(utf8mb4 ? "utf8mb4_bin" : "utf8_bin");
                }
                parameters.add(preparedPattern);
            } else {
                stringBuilder.append("=?");
                if (exact) {
                    stringBuilder.append(" COLLATE ").append(utf8mb4 ? "utf8mb4_bin" : "utf8_bin");
                }
                parameters.add(value);
            }
        } else {
            stringBuilder.append(dbMapping.getColumnLabel()).append("=?");
            parameters.add(value);
        }
    }

    private static ContactField getStartLetterField() {
        if (null != startLetterField) {
            return startLetterField;
        }
        String field = ContactConfig.getInstance().getString(ContactConfig.Property.LETTER_FIELD);
        startLetterField = Mappers.CONTACT.getMappedField(field);
        if (null != startLetterField) {
            return startLetterField;
        }
        // fallback to displayname
        return ContactField.DISPLAY_NAME;
    }
}
