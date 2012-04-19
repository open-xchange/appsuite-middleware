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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.contacts.json.search;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.parser.DataParser;
import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactConfig;
import com.openexchange.groupware.contact.Search;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ColumnOperand;
import com.openexchange.search.internal.operands.ConstantOperand;

/**
 * {@link SearchTermParser} - Constructs {@link SearchTerm}s from 
 * {@link JSONObject}s as defined by the HTTP API. 
 * 
 * @see <a href="http://oxpedia.org/index.php?title=HTTP_API#Search_users">HTTP_API#Search_users</a>
 * @see <a href="http://oxpedia.org/index.php?title=HTTP_API#Search_contacts">HTTP_API#Search_contacts</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SearchTermParser {	
	
	private final SearchTerm<?> term;
	private final String[] folderIDs;

	/**
	 * Initialzes a new {@link SearchTermParser}.
	 * 
	 * @param json the JSON object to parse
	 * @throws OXException 
	 * @throws JSONException 
	 */
    public SearchTermParser(final JSONObject json) throws JSONException, OXException {
    	this(json, null);
    }

    /**
	 * Initialzes a new {@link SearchTermParser}.
	 * 
	 * @param json the JSON object to parse
     * @param folderIDs the folder IDs to limit the search for
     * @throws JSONException
     * @throws OXException
     */
    public SearchTermParser(final JSONObject json, final String[] folderIDs) throws JSONException, OXException {
        super();
        this.term = json.hasAndNotNull("pattern") ? parseSearchTerm(json) : parseSearchTermAlternative(json);
        this.folderIDs = folderIDs;
    }

	/**
	 * @return the term
	 */
	public SearchTerm<?> getSearchTerm() {
		return term;
	}
    
    /**
     * Parses a search term from JSON.
     * 
     * @param json
     * @return
     * @throws JSONException
     * @throws OXException
     */
    private SearchTerm<?> parseSearchTerm(final JSONObject json) throws JSONException, OXException {
    	if (false == json.hasAndNotNull("pattern")) {
    		throw OXException.mandatoryField("pattern");
    	}
    	final String pattern = json.getString("pattern");
    	/*
    	 * use start letter term when set
    	 */
    	SearchTerm<?> searchTerm = json.hasAndNotNull("startletter") ? getStartLetterTerm(pattern) : null;
    	if (null == searchTerm || null == searchTerm.getOperands() || 0 == searchTerm.getOperands().length) {
    		/*
    		 * fallback to display name search
    		 */
    		searchTerm = getSearchTerm(ContactField.DISPLAY_NAME, pattern, true, true);
    	}
    	/*
    	 * combine with folders term when set
    	 */
    	return combineWithFoldersTerm(searchTerm, json);
    }
    
    /**
     * Parses an alternative search term from JSON.
     * 
     * @param json
     * @return
     * @throws JSONException
     * @throws OXException
     */
    private SearchTerm<?> parseSearchTermAlternative(final JSONObject json) throws JSONException, OXException {
    	/*
    	 * create composite term
    	 */
    	final boolean emailAutoComplete = json.has("emailAutoComplete") && json.getBoolean("emailAutoComplete");
    	final boolean orSearch = emailAutoComplete || json.has("orSearch") && json.getBoolean("orSearch");
    	CompositeSearchTerm searchTerm = new CompositeSearchTerm(orSearch ? CompositeOperation.OR : CompositeOperation.AND);
    	/*
    	 * add search criteria
    	 */
    	for (final ContactField field : ALTERNATIVE_SEARCH_FIELDS) {
        	if (json.hasAndNotNull(field.getAjaxName())) {
        		final String pattern = json.getString(field.getAjaxName());
        		searchTerm.addSearchTerm(getSearchTerm(field, pattern, false == orSearch, true));
        	}
		}
    	/*
    	 * combine with email auto complete 
    	 */
    	if (emailAutoComplete) {
    		searchTerm = getCompositeTerm(searchTerm, HAS_EMAIL_TERM);
    	}
    	return combineWithFoldersTerm(searchTerm, json);
    }
    
    /**
     * Creates an 'AND' composite term, combining the given search term with 
     * the set folders or the folders found in the json object.
     * 
     * @param term
     * @param json
     * @return
     * @throws JSONException
     * @throws OXException
     */
    private SearchTerm<?> combineWithFoldersTerm(final SearchTerm<?> term, final JSONObject json) throws JSONException, OXException {
    	final SearchTerm<?> foldersTerm;
    	if (null != this.folderIDs) {
    		foldersTerm = getFoldersTerm(folderIDs);
    	} else if (json.hasAndNotNull("folder")) {
    		foldersTerm = parseFoldersTerm(json);
    	} else {
    		foldersTerm = null;
    	}    	
    	if (null != foldersTerm && null != foldersTerm.getOperands() && 0 < foldersTerm.getOperands().length) {
    		return getCompositeTerm(foldersTerm, term);
    	} else {
    		return term;
    	}
    }
    
    /**
     * Creates a new 'AND' composite search term using the supplied terms as
     * operands.
     * 
     * @param term1 the first term
     * @param term2 the second term
     * @return the composite search term
     */
    private static CompositeSearchTerm getCompositeTerm(final SearchTerm<?> term1, final SearchTerm<?> term2) {
		final CompositeSearchTerm andTerm = new CompositeSearchTerm(CompositeOperation.AND);    		
		andTerm.addSearchTerm(term1);
		andTerm.addSearchTerm(term2);
		return andTerm;
    }

    /**
     * Parses the "folder" information from the supplied json object and puts 
     * the folder IDs into a suitable search term. 
     * 
     * @param json
     * @return
     * @throws JSONException
     * @throws OXException
     */
    private static SearchTerm<?> parseFoldersTerm(final JSONObject json) throws JSONException, OXException {
		if (JSONArray.class.isInstance(json.get("folder"))) {
			return getFoldersTerm(DataParser.parseJSONStringArray(json, "folder"));			
        } else {
        	return getFolderTerm(DataParser.parseString(json, "folder"));
        }
    }
    
    /**
     * Creates a search term to find contacts based on their parent folder.
     * 
     * @param folderIDs the IDs of the folders
     * @return the search term
     */
	private static SearchTerm<?> getFoldersTerm(final String[] folderIDs) {
		if (null == folderIDs || 0 == folderIDs.length) {
			return null;			
		} else if (1 == folderIDs.length) {
			return getFolderTerm(folderIDs[0]);
		} else {
    		final CompositeSearchTerm orTerm = new CompositeSearchTerm(CompositeOperation.OR);
			for (final String folderID : folderIDs) {
				orTerm.addSearchTerm(getFolderTerm(folderID));
			}
			return orTerm;
		}
	}
	
    /**
     * Creates a search term to find contacts based on their parent folder.
     * 
     * @param folderID the ID of the folder
     * @return the search term
     */
    private static SingleSearchTerm getFolderTerm(final String folderID) {
    	final SingleSearchTerm term = new SingleSearchTerm(SingleOperation.EQUALS);
    	term.addOperand(new ContactFieldOperand(ContactField.FOLDER_ID));
    	term.addOperand(new ConstantOperand<String>(folderID));
    	return term;
    }
	
    /**
     * Creates a search term to find contacts based on their start letter.
     * 
     * @param pattern the start letter pattern
     * @return the search term
     */
	private static SearchTerm<?> getStartLetterTerm(final String pattern) {
		final String field = ContactConfig.getInstance().getString(ContactConfig.Property.LETTER_FIELD);            
		if (".".equals(pattern) || "#".equals(pattern)) {
			final CompositeSearchTerm andTerm = new CompositeSearchTerm(CompositeOperation.AND);
			final CompositeSearchTerm orTerm = new CompositeSearchTerm(CompositeOperation.OR);
			final SingleSearchTerm lessThanTerm = new SingleSearchTerm(SingleOperation.LESS_THAN);
			lessThanTerm.addOperand(new ColumnOperand(field));
			lessThanTerm.addOperand(new ConstantOperand<String>("0*"));
			orTerm.addSearchTerm(lessThanTerm);
			final SingleSearchTerm greaterThanTerm = new SingleSearchTerm(SingleOperation.GREATER_THAN);
			greaterThanTerm.addOperand(new ColumnOperand(field));
			greaterThanTerm.addOperand(new ConstantOperand<String>("z*"));
			orTerm.addSearchTerm(greaterThanTerm);
			andTerm.addSearchTerm(orTerm);
			final SingleSearchTerm notEqualsTerm = new SingleSearchTerm(SingleOperation.NOT_EQUALS);
			notEqualsTerm.addOperand(new ColumnOperand(field));
			notEqualsTerm.addOperand(new ConstantOperand<String>("z*"));
			andTerm.addSearchTerm(notEqualsTerm);
			return andTerm;
		} else if (pattern.matches("\\d")) {
			final CompositeSearchTerm andTerm = new CompositeSearchTerm(CompositeOperation.AND);
			final SingleSearchTerm greaterThanTerm = new SingleSearchTerm(SingleOperation.GREATER_THAN);
			greaterThanTerm.addOperand(new ColumnOperand(field));
			greaterThanTerm.addOperand(new ConstantOperand<String>("0*"));
			andTerm.addSearchTerm(greaterThanTerm);
			final SingleSearchTerm lessThanTerm = new SingleSearchTerm(SingleOperation.LESS_THAN);
			lessThanTerm.addOperand(new ColumnOperand(field));
			lessThanTerm.addOperand(new ConstantOperand<String>("a*"));
			andTerm.addSearchTerm(lessThanTerm);
			return andTerm;
		} else if (false == "all".equals(pattern)) {
			/*
			 * ( ! ( <field> IS NULL ) AND <field> LIKE '<pattern>%' ) OR ( <field> IS NULL AND <fallbackField> LIKE '<pattern>%' )
			 */
			final ContactField fallbackField = ContactField.DISPLAY_NAME;
			final CompositeSearchTerm orTerm = new CompositeSearchTerm(CompositeOperation.OR);
			final CompositeSearchTerm andTerm = new CompositeSearchTerm(CompositeOperation.AND);
			final CompositeSearchTerm andTerm2 = new CompositeSearchTerm(CompositeOperation.AND);
			/*
			 * ! ( <field> IS NULL )
			 */
			final CompositeSearchTerm notTerm = new CompositeSearchTerm(CompositeOperation.NOT);
			final SingleSearchTerm isNullTerm = new SingleSearchTerm(SingleOperation.ISNULL);
			isNullTerm.addOperand(new ColumnOperand(field));
			notTerm.addSearchTerm(isNullTerm);
			andTerm.addSearchTerm(notTerm);
			/*
			 * <field> LIKE '<pattern>%'
			 */
			final String preparedPattern = addWildcards(pattern, false, true); 
			final SingleSearchTerm equalsTerm = new SingleSearchTerm(SingleOperation.EQUALS);
			equalsTerm.addOperand(new ColumnOperand(field));
			equalsTerm.addOperand(new ConstantOperand<String>(preparedPattern));
			andTerm.addSearchTerm(equalsTerm);
			/*
			 * <field> IS NULL
			 */
			andTerm2.addSearchTerm(isNullTerm);
			/*
			 * <fallbackField> LIKE '<pattern>%'
			 */
			final SingleSearchTerm equalsTerm2 = new SingleSearchTerm(SingleOperation.EQUALS);
			equalsTerm2.addOperand(new ContactFieldOperand(fallbackField));
			equalsTerm2.addOperand(new ConstantOperand<String>(preparedPattern));
			andTerm2.addSearchTerm(equalsTerm2);

			orTerm.addSearchTerm(andTerm);
			orTerm.addSearchTerm(andTerm2);
			return orTerm;
		} else {
			/*
			 * no valid start letter pattern
			 */
			return null;
		}
	}
	
	private static String addWildcards(final String pattern, final boolean prependWildcard, final boolean appendWildcard) {
		if (null != pattern && (appendWildcard || prependWildcard)) {
			final int length = pattern.length();
			if (0 == length) {
				return "*";
			} else {
				return String.format("%s%s%s", 
						prependWildcard && '*' != pattern.charAt(0) ? "*" : "",
						pattern,
						appendWildcard && '*' != pattern.charAt(length - 1) ? "*" : "");
			}
		} else {
			return pattern;
		}
	}
   
    /**
     * Gets a search term for a contact field, optionally surrounding the search pattern with wildcards.
     * 
     * @param field
     * @param pattern
     * @param prependWildcard
     * @param appendWildcard
     * @return
     * @throws OXException
     */
    private static SingleSearchTerm getSearchTerm(final ContactField field, final String pattern, final boolean prependWildcard, 
    		final boolean appendWildcard) throws OXException {
		final SingleSearchTerm term = new SingleSearchTerm(SingleOperation.EQUALS);
		term.addOperand(new ContactFieldOperand(field));
		Search.checkPatternLength(pattern);
		term.addOperand(new ConstantOperand<String>(addWildcards(pattern, prependWildcard, appendWildcard)));
		return term;
    }

	/**
	 * Contact fields that may be used by the 'Search contacts alternative'
	 */
    private static final ContactField[] ALTERNATIVE_SEARCH_FIELDS = { ContactField.SUR_NAME, ContactField.GIVEN_NAME, 
    	ContactField.DISPLAY_NAME, ContactField.EMAIL1, ContactField.EMAIL2, ContactField.EMAIL3, ContactField.COMPANY,
    	ContactField.STREET_BUSINESS, ContactField.CITY_BUSINESS, ContactField.DEPARTMENT, ContactField.CATEGORIES, 
    	ContactField.YOMI_FIRST_NAME, ContactField.YOMI_LAST_NAME, ContactField.YOMI_COMPANY    	
    };
    
    /**
     * Search term to ensure that found contacts have at least one e-mail address, or represent a distribution list 
     */
    private static final CompositeSearchTerm HAS_EMAIL_TERM;
    static {
    	final ContactField[] emailFields = { ContactField.EMAIL1, ContactField.EMAIL2, ContactField.EMAIL3 };
		final CompositeSearchTerm andTerm = new CompositeSearchTerm(CompositeOperation.AND);
    	for (final ContactField field : emailFields) {
    		final SingleSearchTerm term = new SingleSearchTerm(SingleOperation.ISNULL);
    		term.addOperand(new ContactFieldOperand(field));
    		andTerm.addSearchTerm(term);
    	}
    	final CompositeSearchTerm notTerm = new CompositeSearchTerm(CompositeOperation.NOT);
    	notTerm.addSearchTerm(andTerm);
    	final SingleSearchTerm distributionListTerm = new SingleSearchTerm(SingleOperation.GREATER_THAN);
    	distributionListTerm.addOperand(new ContactFieldOperand(ContactField.NUMBER_OF_DISTRIBUTIONLIST));
    	distributionListTerm.addOperand(new ConstantOperand<Integer>(0));
    	HAS_EMAIL_TERM = new CompositeSearchTerm(CompositeOperation.OR);
    	HAS_EMAIL_TERM.addSearchTerm(notTerm);
    	HAS_EMAIL_TERM.addSearchTerm(distributionListTerm);
    	
    }
    
}
