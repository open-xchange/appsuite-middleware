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

 package com.openexchange.ajax.parser;

import java.util.LinkedList;
import java.util.List;
import org.apache.commons.logging.Log;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.contact.sqlinjectors.SQLInjector;
import com.openexchange.groupware.contact.sqlinjectors.StringSQLInjector;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.Operand;
import com.openexchange.search.Operation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SearchTerm.OperationPosition;
import com.openexchange.search.SingleSearchTerm;

 /**
  * @author tobiasprinz
  * @param <T>
  */

public class ContactSearchtermSqlConverter  implements ContactSearchTermConverter {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(ContactSearchtermSqlConverter.class);

	private static final String FOLDER_AJAXNAME = ContactField.FOLDER_ID.getAjaxName();

	private StringBuilder bob;

	private List<SQLInjector> injectors;

	private List<String> folders;

	private boolean nextIsFolder;

	private String charset;

	public ContactSearchtermSqlConverter() {
		initialize();
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getCharset() {
		return charset;
	}

	/**
	 * Takes a search term (basically an Abstract Syntax Tree, AST,
	 * which is usually created from JSON sent by the GUI) and forms
	 * a WHERE clause as used by SQL database queries.
	 *
	 */
	@Override
    public <T> void parse(SearchTerm<T> term) {
		traverseViaInOrder(term);
	}

	/**
	 * Resets the whole object so you don't get data from the last parse process.
	 * Called during instantiation and before every new parse cycle.
	 */
	protected void initialize(){
		bob = new StringBuilder();
		injectors = new LinkedList<SQLInjector>();
		folders = new LinkedList<String>();
		nextIsFolder = false;
	}

	/**
	 * @return A string that can be added to a WHERE. It does not contain values but ?'s
	 */
	public String getPreparedWhereString(){
		return bob.toString()
			.replaceAll("\\s+", " ") //shrink whitespaces
			.trim(); //remove ending whitespaces
	}

	/**
	 * @return A list of injectors that can be used on the resulting prepared string to replace the ?'s with values (to prevent SQLInjection attacks)
	 */
	public List<SQLInjector> getInjectors(){
		return injectors;
	}

	/**
	 * Returns the folders that where queried. These are still in the
	 * result of #getPreparedWhereString(), too, but you might want
	 * them separately to check for access rights.
	 *
	 * Result is of a generic type since mail folders can be strings but
	 * other modules generally use Integers.
	 *
	 * @return A list of folders that are requested to be searched
	 */
	@Override
    public List<String> getFolders(){
		return folders;
	}

	public boolean hasFolders(){
		return folders.size() != 0;
	}

	protected <T> void traverseViaInOrder(SearchTerm<T> term) {
		if(term instanceof SingleSearchTerm) {
            traverseViaInorder((SingleSearchTerm) term);
        } else if(term instanceof CompositeSearchTerm) {
            traverseViaInorder((CompositeSearchTerm) term);
        } else {
            LOG.error("Got a search term that was neither Composite nor Single. How? " + System.getProperty("line.separator") + term);
        }
	}

	protected void traverseViaInorder(SingleSearchTerm term) {
		Operand<?>[] operands = term.getOperands();
		Operation operation = term.getOperation();
		bob.append(" ( ");


		for(int i = 0; i < operands.length; i++){
			Operand<?> o = operands[i];

			if(operation.getSqlPosition() == OperationPosition.BEFORE) {
                bob.append(operation.getSqlRepresentation());
            }

			if(o.getType() == Operand.Type.COLUMN){
				String value = (String) o.getValue();
				handleFolder(o);

				String field = translateFromJSONtoDB( value);
				field = handlePrefix(field);
				field = handleCharset(field);
			bob.append(field);
			}

			if(o.getType() == Operand.Type.CONSTANT){
				String value = (String) o.getValue();
				handleFolder(o);
				value = handlePatternMatching(value);
				injectors.add(new StringSQLInjector(value));
				bob.append(handleCharset("?"));
			}

			if(operation.getSqlPosition() == OperationPosition.AFTER) {
                bob.append(' ').append(operation.getSqlRepresentation());
            }

			if(operation.getSqlPosition() == OperationPosition.BETWEEN) {
                if((i+1) < operands.length) {
                    bob.append(' ').append(operation.getSqlRepresentation()).append(' ');
                }
            }

		}
		bob.append(" ) ");
	}

	protected void traverseViaInorder(CompositeSearchTerm term) {
		Operation operation = term.getOperation();
		SearchTerm<?>[] operands = term.getOperands();

		bob.append(" ( ");

		if(operation.getSqlPosition() == OperationPosition.BEFORE) {
            bob.append(operation.getSqlRepresentation());
        }

		for(int i = 0; i < operands.length; i++){
			traverseViaInOrder(operands[i]);

			if(operation.getSqlPosition() == OperationPosition.AFTER) {
                bob.append(' ').append(operation.getSqlRepresentation());
            }
			if(operation.getSqlPosition() == OperationPosition.BETWEEN) {
                if((i+1) < operands.length) {
                    bob.append(' ').append(operation.getSqlRepresentation()).append("    ");
                }
            }
		}
		bob.append(" ) ");

	}

	/**
	 * Called to check whether an argument given is about a folder. We
 * extract these separately because our access system works folder-based.
	 */
	protected void handleFolder(Operand<?> o) {
		if(o.getType() == Operand.Type.COLUMN && o.getValue().equals(FOLDER_AJAXNAME)) {
            nextIsFolder = true;
        }

	if(o.getType() == Operand.Type.CONSTANT && nextIsFolder){
				folders.add((String) o.getValue());
				nextIsFolder = false;
		}
	}

	/**
	 * Called to check whether an argument given contains asterisks
	 * which work as wildcards. For SQL, we need to replace them with
	 * percentage-signs and to replace an equals-sign with a LIKE
	 * statement.
	 *
	 * @return the value reformatted for SQL use (means: with some % in most cases)
	 */
	protected String handlePatternMatching(String value) {
		if(value.indexOf('*') < 0) {
            return value;
        }

		value = value.replaceAll("\\*", "%");

		int index = bob.lastIndexOf("=");
		bob.replace(index, index+1, "LIKE");

		return value;
	}

	/**
	 * Writes a CONVERT statement around the field in case there was a charset given
	 */
	protected String handleCharset(String field) {
		if(charset == null) {
            return field;
        }
		return new com.openexchange.java.StringAllocator("CONVERT(").append(field).append(" USING ").append(getCharset()).append(')').toString();
	}

	/**
	 * Adds a prefix to the field - if set.
	 */
	protected String handlePrefix(String field) {
		if(getPrefix() == null) {
            return field;
        }
		return new com.openexchange.java.StringAllocator(getPrefix()).append('.').append(field).toString();
	}


	/**
	 * Translates a value given by the GUI to one in the database
	 * @param fieldvalue As usable by the database (usually something hardly descriptive like 'field01' or 'intfield07')
	 * @return
	 */

	public String translateFromJSONtoDB(String fieldname) {
 		ContactField field = ContactField.getByAjaxName(fieldname);
 		if((field != null) && (field.getDbName() != null)) {
            return field.getDbName();
        } else {
            return fieldname;
        }
 	}

 	/**
 	 * @return the prefix our database queries usually use to refer to a table.
 	 */
	protected String getPrefix() {
 		return "co";
 	}
 }
