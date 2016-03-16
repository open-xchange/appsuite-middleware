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

package com.openexchange.contact.internal;

import java.util.ArrayList;
import java.util.List;

import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.Operand;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;

/**
 * {@link SearchTermAnalyzer} - Extracts related information from search terms.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SearchTermAnalyzer {

    private final List<String> folderIDs = new ArrayList<String>();

    public SearchTermAnalyzer(final SearchTerm<?> term) {
    	super();
    	this.analyzeTerm(term);
    }

    /**
     * Gets the detected folder IDs present in the underlying term.
     *
     * @return the folder IDs
     */
    public List<String> getFolderIDs() {
    	return this.folderIDs;
    }

    /**
     * Gets a value indicating whether folder IDs have been detected or not.
     *
     * @return
     */
    public boolean hasFolderIDs() {
    	return null != this.folderIDs && 0 < this.folderIDs.size();
    }

	private void analyzeTerm(final SearchTerm<?> term) {
		if (SingleSearchTerm.class.isInstance(term)) {
			this.analyzeTerm((SingleSearchTerm)term);
		} else if (CompositeSearchTerm.class.isInstance(term)) {
			this.analyzeTerm((CompositeSearchTerm)term);
		} else {
			throw new IllegalArgumentException("Need either a 'SingleSearchTerm' or 'CompositeSearchTerm'.");
		}
	}

	private void analyzeTerm(final SingleSearchTerm term) {
		final Operand<?>[] operands = term.getOperands();
		for (int i = 0; i < operands.length; i++) {
			if (Operand.Type.COLUMN == operands[i].getType()) {
				ContactField field = null;
				final Object value = operands[i].getValue();
				if (null == value) {
					throw new IllegalArgumentException("column operand without value: " + operands[i]);
				} else if (ContactField.class.isInstance(value)) {
					field = (ContactField)value;
				} else {
					//TODO: this is basically for backwards compatibility until ajax names are no longer used in search terms
					field = ContactField.getByAjaxName(value.toString());
				}
				if (null != field && ContactField.FOLDER_ID.equals(field) && i + 1 < operands.length &&
						null != operands[i + 1] && null != operands[i + 1].getValue()) {
					this.folderIDs.add((String)operands[i + 1].getValue());
					i++;
				}
			}
		}
	}

	private void analyzeTerm(final CompositeSearchTerm term) {
		for (final SearchTerm<?> operand : term.getOperands()) {
			analyzeTerm(operand);
		}
	}

}
