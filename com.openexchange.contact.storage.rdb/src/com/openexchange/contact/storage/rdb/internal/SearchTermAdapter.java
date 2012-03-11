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

package com.openexchange.contact.storage.rdb.internal;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.openexchange.contact.storage.rdb.mapping.Mappers;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.Operand;
import com.openexchange.search.Operation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SearchTerm.OperationPosition;
import com.openexchange.search.SingleSearchTerm;

/**
 * {@link SearchTermAdapter} - helps constructing the database statement for a search term.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SearchTermAdapter {
	
	private final StringBuilder stringBuilder;
	private final List<Object> parameters;
	private final String charset;
	
	public SearchTermAdapter(final SearchTerm<?> term, final String charset) throws OXException {
		super();
		this.stringBuilder = new StringBuilder();
		this.parameters = new ArrayList<Object>();
		this.charset = charset;
		this.append(term);
	}
    
	public SearchTermAdapter(final SearchTerm<?> term) throws OXException {
		this(term, null);
	}
    
	/**
	 * Gets the parameter values that were detected in the search term during 
	 * parsing to be included in the database statement in the correct order.
	 *  
	 * @return the parameters
	 */
	public Object[] getParameters() {
		return this.parameters.toArray(new Object[0]);
	}
	
	/**
	 * Gets the constructed <code>WHERE</code>-clause for the search term to be
	 * used in the database statement, without the leading <code>WHERE</code>.  
	 * 
	 * @return the search clause
	 */
	public String getClause() {
		final String clause = this.stringBuilder.toString().trim();
		if (0 < clause.length()) {
			return clause;
		} else {
			return "TRUE";
		}
	}
	
	/**
	 * Sets the detected database parameters in the supplied prepared statement,
	 * beginning at the specified parameter index.
	 * 
	 * @param stmt the statement to set the parameters for
	 * @param parameterIndex the start index to set the parameters
	 * @throws SQLException
	 */
	public void setParameters(final PreparedStatement stmt, int parameterIndex) throws SQLException {
		for (final Object parameter : parameters) {
			stmt.setObject(parameterIndex++, parameter);
		}		
	}
	
	private void append(final SearchTerm<?> term) throws OXException {
		if (SingleSearchTerm.class.isInstance(term)) {
			this.append((SingleSearchTerm)term);
		} else if (CompositeSearchTerm.class.isInstance(term)) {
			this.append((CompositeSearchTerm)term);
		} else {
			throw new IllegalArgumentException("Need either an 'SingleSearchTerm' or 'CompositeSearchTerm'.");
		}
	}
	
	private void append(final SingleSearchTerm term) throws OXException {
		final Operand<?>[] operands = term.getOperands();
		final Operation operation = term.getOperation();
		this.stringBuilder.append(" ( ");
		for (int i = 0; i < operands.length; i++) {
			if (OperationPosition.BEFORE.equals(operation.getSqlPosition())) {
				stringBuilder.append(operation.getSqlRepresentation());				
			}
			append(operands[i]);
			if (OperationPosition.AFTER.equals(operation.getSqlPosition())) {
				stringBuilder.append(' ').append(operation.getSqlRepresentation());
			} else if (OperationPosition.BETWEEN.equals(operation.getSqlPosition()) && i != operands.length - 1) {
				//don't place an operator after the last operand here
				stringBuilder.append(' ').append(operation.getSqlRepresentation()).append(' ');
			}

		}
		stringBuilder.append(" ) ");
	}
	
	private void append(final CompositeSearchTerm term) throws OXException {
		final Operation operation = term.getOperation();
		final SearchTerm<?>[] terms = term.getOperands();
		stringBuilder.append(" ( ");
		if (OperationPosition.BEFORE.equals(operation.getSqlPosition())) {
			stringBuilder.append(operation.getSqlRepresentation());
		}
		for (int i = 0; i < terms.length; i++) {
			append(terms[i]);
			if (OperationPosition.AFTER.equals(operation.getSqlPosition())) {
				stringBuilder.append(' ').append(operation.getSqlRepresentation());
			} else  if (OperationPosition.BETWEEN.equals(operation.getSqlPosition()) && i != terms.length - 1) {
				//don't place an operator after the last operand
				stringBuilder.append(' ').append(operation.getSqlRepresentation()).append("    ");
			}
		}
		stringBuilder.append(" ) ");
	}
	
    private void append(final Operand<?> operand) throws OXException {
		if (Operand.Type.COLUMN.equals(operand.getType())) {
			final Object value = operand.getValue();
			if (null == value) {
				throw new IllegalArgumentException("column operand without value: " + operand);
			}
			//TODO: don't use getByAjaxName in storage layer
			final String columnLabel = Mappers.CONTACT.get(ContactField.getByAjaxName(value.toString())).getColumnLabel();
			if (null != this.charset) {
				stringBuilder.append("CONVERT(").append(columnLabel).append(" USING ").append(this.charset).append(')');
			} else {
				stringBuilder.append(columnLabel);
			}
		} else if (Operand.Type.CONSTANT.equals(operand.getType())) {
			parameters.add(operand.getValue());
			stringBuilder.append('?');
		} else {
			throw new IllegalArgumentException("unknown type in operand: " + operand.getType());
		}
    }

}
