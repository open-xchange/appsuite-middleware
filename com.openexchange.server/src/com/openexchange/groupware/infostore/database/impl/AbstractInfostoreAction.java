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

package com.openexchange.groupware.infostore.database.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import com.openexchange.database.tx.AbstractDBAction;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.utils.GetSwitch;
import com.openexchange.groupware.infostore.utils.Metadata;

public abstract class AbstractInfostoreAction extends AbstractDBAction {

	private InfostoreQueryCatalog queries = null;

    protected final void fillStmt(final PreparedStatement stmt, final Metadata[] fields, final DocumentMetadata doc, final Object...additional) throws SQLException {
        fillStmt(1, stmt, fields, doc, additional);
    }

    /**
     * Fills the supplied prepared statement using the values of the denoted fields from a document.
     *
     * @param parameterIndex The (1-based) parameter index to start with
     * @param stmt The statement to populate
     * @param fields The used fields
     * @param doc The document to get the field values from
     * @param additional Any additional arbitrary fields to set in the statement after the document values were set
     * @return The updated parameter index
     * @throws SQLException
     */
    protected final int fillStmt(int parameterIndex, final PreparedStatement stmt, final Metadata[] fields, final DocumentMetadata doc, final Object...additional) throws SQLException {
        final GetSwitch get = new GetSwitch(doc);
        for(final Metadata m : fields) {
            stmt.setObject(parameterIndex++, process(m, m.doSwitch(get)));
        }

        for(final Object o : additional) {
            stmt.setObject(parameterIndex++, o);
        }
        return parameterIndex;
    }

	private final Object process(final Metadata field, final Object value) {
		switch(field.getId()) {
		default : return value;
		    case Metadata.CREATION_DATE:
		    case Metadata.LOCKED_UNTIL :
		    case Metadata.LAST_MODIFIED_UTC:
		    	return Long.valueOf(((Date)value).getTime());
		    case Metadata.LAST_MODIFIED:
		    	return (value != null) ?  Long.valueOf(((Date)value).getTime()) : Long.valueOf(System.currentTimeMillis());

		}
	}

	public void setQueryCatalog(final InfostoreQueryCatalog queries){
		this.queries = queries;
	}

	public InfostoreQueryCatalog getQueryCatalog(){
		return this.queries;
	}

}
