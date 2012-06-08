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

package com.openexchange.contact.storage.rdb.search;

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

/**
 * {@link DefaultSearchAdapter} 
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class DefaultSearchAdapter implements SearchAdapter {

	/**
	 * Pattern to check whether a string contains SQL wildcards or not
	 */
	private static final Pattern WILDCARD_PATTERN = Pattern.compile("((^|[^\\\\])%)|((^|[^\\\\])_)");

	protected List<Object> parameters;
	protected String charset;

	/**
	 * 
	 * @param charset
	 */
	public DefaultSearchAdapter(String charset) {
		super();
		this.charset = charset;
		this.parameters = new ArrayList<Object>();

	}
	
	@Override
	public Object[] getParameters() {
		return this.parameters.toArray(new Object[parameters.size()]);
	}
	
	@Override
	public void setParameters(PreparedStatement stmt, int parameterIndex) throws SQLException {
		for (Object parameter : parameters) {
			stmt.setObject(parameterIndex++, parameter);
		}		
	}
	
	protected boolean containsWildcards(String pattern) {
		return WILDCARD_PATTERN.matcher(pattern).find();
	}
	
	protected boolean isTextColumn(ContactField field) throws OXException {
		return isTextColumn(Mappers.CONTACT.get(field));
	}

	protected boolean isTextColumn(DbMapping<? extends Object, Contact> mapping) {
		return Types.VARCHAR == mapping.getSqlType();
	}
}