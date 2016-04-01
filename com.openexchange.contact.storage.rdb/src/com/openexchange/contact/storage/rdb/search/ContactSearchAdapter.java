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

	/**
	 * Initializes a new {@link ContactSearchAdapter}.
	 *
	 * @param contactSearch The used contact search object
	 * @param contextID the context ID
	 * @param fields the fields to select
	 * @param charset The used charset
	 * @throws OXException
	 */
    public ContactSearchAdapter(ContactSearchObject contactSearch, int contextID, ContactField[] fields, String charset, int forUser) throws OXException {
		super(charset);
		this.stringBuilder = new StringBuilder(256);
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
		 * append folders
		 */
		stringBuilder.append(" AND ").append(getFolderIDsClause(contactSearch.getFolders()));
	}

    private void appendSearchAlternative(ContactSearchObject contactSearch, int contextID, ContactField[] fields, int forUser) throws OXException {
		Map<ContactField, Object> comparisons = extractComparisons(contactSearch);
		String contextIDClause = getContextIDClause(contextID);
		String folderIDsClause = getFolderIDsClause(contactSearch.getFolders());
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
                appendComparison(contextIDClause, folderIDsClause, selectClause, entry.getKey(), entry.getValue(),
                    emailAutoComplete, exact);
	            while (iterator.hasNext()) {
	                stringBuilder.append(" UNION ");
	                entry = iterator.next();
	                appendComparison(contextIDClause, folderIDsClause, selectClause, entry.getKey(), entry.getValue(), emailAutoComplete, exact);
	            }
	        } else {
	            /*
	             * construct clause using single SELECT
	             */
                stringBuilder.append(selectClause).append(" WHERE ").append(contextIDClause).append(" AND ").append(folderIDsClause).append(" AND ");
                Entry<ContactField, Object> entry = iterator.next();
                appendComparison(entry.getKey(), entry.getValue(), exact);
                while (iterator.hasNext()) {
                    entry = iterator.next();
                    stringBuilder.append(" AND ");
                    appendComparison(entry.getKey(), entry.getValue(), exact);
                }
	        }
		} else {
		    /*
		     * no comparison, just use folders and context id
		     */
            stringBuilder.append(selectClause).append(" WHERE ").append(contextIDClause).append(" AND ").append(folderIDsClause);
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
			stringBuilder.append('(').append(columnLabel).append("<'0%' OR ").append(columnLabel).append(">'z%') AND ")
				.append(columnLabel).append("NOT LIKE 'z%'");
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
			    stringBuilder.append(" COLLATE utf8_bin");
            }
            stringBuilder.append(" OR (").append(columnLabel).append(" IS NULL AND ").append(fallbackColumnLabel).append(" LIKE ?");
            if (exact) {
                stringBuilder.append(" COLLATE utf8_bin");
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

	private void appendComparison(String contextIDClause, String folderIDsClause, String selectClause, ContactField field, Object value, boolean needsEMail, boolean exact) throws OXException {
		stringBuilder.append('(').append(selectClause);
		stringBuilder.append(" WHERE ").append(contextIDClause).append(" AND ");
		appendComparison(field, value, exact);
		stringBuilder.append(" AND ").append(folderIDsClause);
		if (needsEMail) {
			stringBuilder.append(" AND (").append(getEMailAutoCompleteClause()).append(')');
		}
		stringBuilder.append(')');
	}

	private static Map<ContactField, Object> extractComparisons(ContactSearchObject contactSearch) {
		Map<ContactField, Object> comparisons = new HashMap<ContactField, Object>();
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

		if (0 != contactSearch.getIgnoreOwn() || null != contactSearch.getAnniversaryRange() ||
				null != contactSearch.getBirthdayRange() || null != contactSearch.getBusinessPostalCodeRange() ||
				null != contactSearch.getCreationDateRange() || null != contactSearch.getDynamicSearchField() ||
				null != contactSearch.getDynamicSearchFieldValue() || null != contactSearch.getFrom() ||
				null != contactSearch.getLastModifiedRange() || null != contactSearch.getNumberOfEmployeesRange() ||
				null != contactSearch.getSalesVolumeRange() || null != contactSearch.getOtherPostalCodeRange() ||
				null != contactSearch.getPrivatePostalCodeRange()) {
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
			String preparedPattern = StringCollection.prepareForSearch((String)value, false, true);
			if (containsWildcards(preparedPattern)) {
				// use "LIKE" search
				stringBuilder.append(" LIKE ?");
				if (exact) {
				    stringBuilder.append(" COLLATE utf8_bin");
                }
				parameters.add(preparedPattern);
			} else {
				stringBuilder.append("=?");
                if (exact) {
                    stringBuilder.append(" COLLATE utf8_bin");
                }
				parameters.add(value);
			}
		} else {
			stringBuilder.append(dbMapping.getColumnLabel()).append("=?");
			parameters.add(value);
		}
	}

	private static ContactField getStartLetterField() throws OXException {
		if (null == startLetterField) {
			String field = ContactConfig.getInstance().getString(ContactConfig.Property.LETTER_FIELD);
			startLetterField = Mappers.CONTACT.getMappedField(field);
			if (null == startLetterField) {
				// fallback to displayname
				startLetterField = ContactField.DISPLAY_NAME;
			}
		}
		return startLetterField;
	}

}
