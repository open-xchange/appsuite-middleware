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
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.openexchange.contact.storage.rdb.mapping.Mappers;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.tools.mappings.database.DbMapping;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.Operand;
import com.openexchange.search.Operand.Type;
import com.openexchange.search.Operation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SearchTerm.OperationPosition;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.tools.StringCollection;

/**
 * {@link SearchTermAdapter} - Helps constructing the database statement for a search term.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SearchTermAdapter {
	
	/**
	 * Pattern to check whether a string contains SQL wildcards or not
	 */
	private static final Pattern WILDCARD_PATTERN = Pattern.compile("((^|[^\\\\])%)|((^|[^\\\\])_)");
	
	private final StringBuilder stringBuilder;
	private final List<Object> parameters;
	private final String charset;
	
	/**
	 * Initializes a new {@link SearchTermAdapter}.
	 * 
	 * @param term
	 * @param charset
	 * @throws OXException
	 */
	public SearchTermAdapter(final SearchTerm<?> term, final String charset) throws OXException {
		super();
		this.stringBuilder = new StringBuilder();
		this.parameters = new ArrayList<Object>();
		this.charset = charset;
		this.append(term);
	}
    
	/**
	 * Initializes a new {@link SearchTermAdapter}.
	 * 
	 * @param term
	 * @throws OXException
	 */
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
		return this.parameters.toArray(new Object[parameters.size()]);
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
	
	private void append(SingleSearchTerm term) throws OXException {
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
	
	private void append(CompositeSearchTerm term) throws OXException {
		stringBuilder.append(" ( ");
		if (false == appendAsInClause(term)) {
			Operation operation = term.getOperation();
			SearchTerm<?>[] terms = term.getOperands();
			if (OperationPosition.BEFORE.equals(operation.getSqlPosition())) {
				stringBuilder.append(operation.getSqlRepresentation());
			}
			for (int i = 0; i < terms.length; i++) {
				append(terms[i]);
				if (OperationPosition.AFTER.equals(operation.getSqlPosition())) {
					stringBuilder.append(' ').append(operation.getSqlRepresentation());
				} else  if (OperationPosition.BETWEEN.equals(operation.getSqlPosition()) && i != terms.length - 1) {
					//don't place an operator after the last operand
					stringBuilder.append(' ').append(operation.getSqlRepresentation()).append(' ');
				}
			}
		}
		stringBuilder.append(" ) ");
	}
	
	/**
	 * Tries to interpret and append a composite term as <code>IN</code>-
	 * clause, so that a composite 'OR' term where each nested 'EQUALS' 
	 * operation targets the same column gets optimized to a suitable 
	 * <code>column IN (value1,value2,...)</code>.
	 * 
	 * @param compositeTerm the composite term
	 * @return <code>true</code>, if the term was appended as 'IN' clause, <code>false</code>, otherwise 
	 * @throws OXException
	 */
	private boolean appendAsInClause(CompositeSearchTerm compositeTerm) throws OXException {
		/*
		 * check operation
		 */
		if (false == CompositeOperation.OR.equals(compositeTerm.getOperation())) {
			return false; // only 'OR' composite operations
		}
		/*
		 * check operands
		 */
		if (null == compositeTerm.getOperands() || 2 > compositeTerm.getOperands().length) {
			return false; // at least 2 operands
		}
		List<Object> constantValues = new ArrayList<Object>();
		Object commonColumnValue = null;
		for (SearchTerm<?> term : compositeTerm.getOperands()) {
			if (false == SingleSearchTerm.class.isInstance(term)) {
				return false; // only nested single search terms
			}
			SingleSearchTerm singleSearchTerm = (SingleSearchTerm)term;
			if (false == SingleOperation.EQUALS.equals(singleSearchTerm.getOperation())) {
				return false; // only 'EQUALS' operations in nested terms
			}
			Object columnValue = null;
			Object constantValue = null;
			for (Operand<?> operand : singleSearchTerm.getOperands()) {
				if (Type.COLUMN.equals(operand.getType())) {
					columnValue = operand.getValue();
				} else if (Type.CONSTANT.equals(operand.getType())) {
					constantValue = operand.getValue();
				} else {
					return false; // only 'COLUMN' = 'CONSTANT' operations
				}
			}
			if (null == columnValue || null == constantValue) {
				return false; // only 'COLUMN' = 'CONSTANT' operations
			}
			if (null == commonColumnValue) {
				commonColumnValue = columnValue; // first column value
			} else if (false == commonColumnValue.equals(columnValue)) {
				return false; // only equal column value
			}
			if (String.class.isInstance(constantValue)) {
				String preparedPattern = StringCollection.prepareForSearch((String)constantValue, false, true);
				if (WILDCARD_PATTERN.matcher(preparedPattern).find()) {
					return false; // no wildcards
				}
			}
			constantValues.add(constantValue);						
		}
		if (null == commonColumnValue || 2 > constantValues.size()) {
			return false;
		}		
		/*
		 * all checks passed, build IN clause
		 */
		int sqlType = this.appendColumnOperand(commonColumnValue);
		this.stringBuilder.append(" IN (");
		this.appendConstantOperand(constantValues.get(0), sqlType);
		for (int i = 1; i < constantValues.size(); i++) {
			this.stringBuilder.append(',');
			this.appendConstantOperand(constantValues.get(i), sqlType);
		}
		this.stringBuilder.append(") ");
		return true; 
	}
	
    private void append(Operand<?> operand) throws OXException {
		final Object value = operand.getValue();
		if (null == value) {
			throw new IllegalArgumentException("got no value for operand");
		} else if (Operand.Type.COLUMN.equals(operand.getType())) {
			appendColumnOperand(value);
		} else if (Operand.Type.CONSTANT.equals(operand.getType())) {
			appendConstantOperand(value);
		} else {
			throw new IllegalArgumentException("unknown type in operand: " + operand.getType());
		}
    }

	private void appendConstantOperand(Object value) throws OXException {
		this.appendConstantOperand(value, Integer.MIN_VALUE);
	}

	private void appendConstantOperand(Object value, int sqlType) throws OXException {
		if (String.class.isInstance(value)) {
			if (Types.INTEGER == sqlType) {
				// fallback for numeric folder IDs in rdb storage
				parameters.add(Tools.parse((String)value));
			} else {
				String preparedPattern = StringCollection.prepareForSearch((String)value, false, true);
				if (WILDCARD_PATTERN.matcher(preparedPattern).find()) {
					// use "LIKE" search 
					final int index = stringBuilder.lastIndexOf("=");
					stringBuilder.replace(index, index + 1, "LIKE");		
				}
				parameters.add(preparedPattern);
			}
		} else {
			parameters.add(value);
		}
		stringBuilder.append('?');
	}

	/**
	 * Appends a value as column operand.
	 * 
	 * @param value the value
	 * @return the sql type of the column
	 * @throws OXException
	 */
	private int appendColumnOperand(Object value) throws OXException {
		DbMapping<? extends Object, Contact> dbMapping = getMapping(value);
		if (null == dbMapping) {
			throw new IllegalArgumentException("unable to determine database mapping for column operand value: " + value);
		}
		if (null != this.charset && Types.VARCHAR == dbMapping.getSqlType()) {
			stringBuilder.append("CONVERT(").append(dbMapping.getColumnLabel()).append(" USING ").append(this.charset).append(')');
		} else {
			stringBuilder.append(dbMapping.getColumnLabel());
		}
		return dbMapping.getSqlType();
	}
	
	private static DbMapping<? extends Object, Contact> getMapping(Object value) throws OXException {
		ContactField field = null;
		if (ContactField.class.isInstance(value)) {
			field = (ContactField)value;
		} else {
			//TODO: this is basically for backwards compatibility until ajax names are no longer used in search terms
			field = ContactField.getByAjaxName(value.toString());
			if (null == field) {
				// try column name
				field = Mappers.CONTACT.getMappedField(value.toString());					
			}				
		}
		if (null == field) {
			throw new IllegalArgumentException("unable to determine contact field for column operand value: " + value);
		}
		return Mappers.CONTACT.get(field);
	}

}
