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
                    this.folderIDs.add(String.valueOf(operands[i + 1].getValue()));
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
