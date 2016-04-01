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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Pattern;
import com.openexchange.contact.storage.rdb.internal.Tools;
import com.openexchange.contact.storage.rdb.mapping.Mappers;
import com.openexchange.contact.storage.rdb.sql.Table;
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

    /** To make MySQL use the correct indices for UNIONs created from contact search object */
    protected static final boolean IGNORE_INDEX_CID_FOR_UNIONS = true;

    /** Fields that have an alternative index */
    protected static final EnumSet<ContactField> ALTERNATIVE_INDEXED_FIELDS = EnumSet.of(ContactField.EMAIL1, ContactField.EMAIL2,
        ContactField.EMAIL3, ContactField.GIVEN_NAME, ContactField.SUR_NAME, ContactField.DISPLAY_NAME);

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

    protected static String getSelectClause(ContactField[] fields, int forUser) throws OXException {
        return getSelectClause(fields, true, forUser);
    }

    protected static String getSelectClause(ContactField[] fields, boolean withTable, int forUser) throws OXException {
        StringBuilder sb = new StringBuilder(256);
        sb.append("SELECT ");
        sb.append(Mappers.CONTACT.getColumns(fields, Table.CONTACTS.getName() + "."));
        sb.append(" FROM ");
        if (withTable) {
            sb.append(Table.CONTACTS);
        }
        insertObjectUseCountClause(sb, forUser);
        return sb.toString();
    }

    protected static String getSelectClause(ContactField[] fields, boolean withTable) throws OXException {
        StringBuilder sb = new StringBuilder(256);
        sb.append("SELECT ");
        sb.append(Mappers.CONTACT.getColumns(fields, Table.CONTACTS.getName() + "."));
        sb.append(" FROM ");
        if (withTable) {
            sb.append(Table.CONTACTS);
        }
        return sb.toString();
    }

    protected static String getSelectClause(String tableAlias, ContactField[] fields) throws OXException {
        StringBuilder stringBuilder = new StringBuilder(10 * fields.length);
        if (null != fields && 0 < fields.length) {
            stringBuilder.append("SELECT ");
            stringBuilder.append(tableAlias).append('.').append(Mappers.CONTACT.get(fields[0]).getColumnLabel());
            for (int i = 1; i < fields.length; i++) {
                stringBuilder.append(',').append(tableAlias).append('.').append(Mappers.CONTACT.get(fields[i]).getColumnLabel());
            }
            stringBuilder.append(" FROM ").append(Table.CONTACTS);

        }
        return stringBuilder.toString();
    }

	protected static String getContextIDClause(int contextID) throws OXException {
        StringBuilder sb = new StringBuilder(128);
        sb.append(Table.CONTACTS.getName()).append(".").append(Mappers.CONTACT.get(ContactField.CONTEXTID).getColumnLabel()).append("=").append(contextID);
        return sb.toString();
    }

	protected static String getFolderIDsClause(int[] folderIDs) throws OXException {
        String columnlabel = Mappers.CONTACT.get(ContactField.FOLDER_ID).getColumnLabel();
        if (1 == folderIDs.length) {
            return columnlabel + "=" + folderIDs[0];
        } else {
            return columnlabel + " IN (" + Tools.toCSV(folderIDs) + ")";
        }
    }

	protected static String getEMailAutoCompleteClause() throws OXException {
		return getEMailAutoCompleteClause(false);
	}

	protected static String getEMailAutoCompleteClause(boolean ignoreDistributionLists) throws OXException {
        StringBuilder stringBuilder = new StringBuilder()
            .append('(').append(Mappers.CONTACT.get(ContactField.EMAIL1).getColumnLabel()).append("<>'' OR ")
            .append(Mappers.CONTACT.get(ContactField.EMAIL2).getColumnLabel()).append("<>'' OR ")
            .append(Mappers.CONTACT.get(ContactField.EMAIL3).getColumnLabel()).append("<>'')");
        if (ignoreDistributionLists) {
        	stringBuilder.append(" AND ").append(getIgnoreDistributionListsClause());
        } else {
        	stringBuilder.append(" OR ").append(Mappers.CONTACT.get(ContactField.NUMBER_OF_DISTRIBUTIONLIST).getColumnLabel()).append(">0");
        }
        return stringBuilder.toString();
    }

	protected static String getIgnoreDistributionListsClause() throws OXException {
		String dlColumn = Mappers.CONTACT.get(ContactField.NUMBER_OF_DISTRIBUTIONLIST).getColumnLabel();
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("(").append(dlColumn).append("=0 OR ").append(dlColumn).append(" IS NULL)");
		return stringBuilder.toString();
	}

    protected static void insertObjectUseCountClause(StringBuilder stringBuilder, int forUser) {
        int offset = stringBuilder.indexOf("FROM");
        StringBuilder sb = new StringBuilder(1024);
        sb.append(",").append(Table.OBJECT_USE_COUNT).append(".value");
        stringBuilder.insert(offset - 1, sb.toString());
        stringBuilder.append(" LEFT JOIN ").append(Table.OBJECT_USE_COUNT).append(" ON ").append(Table.CONTACTS.getName()).append(".cid=").append(Table.OBJECT_USE_COUNT)
            .append(".cid AND ").append(forUser).append("=").append(Table.OBJECT_USE_COUNT).append(".user AND ")
            .append(Table.CONTACTS.getName()).append(".fid=").append(Table.OBJECT_USE_COUNT).append(".folder AND ")
            .append(Table.CONTACTS.getName()).append(".intfield01=").append(Table.OBJECT_USE_COUNT).append(".object ");
    }
}