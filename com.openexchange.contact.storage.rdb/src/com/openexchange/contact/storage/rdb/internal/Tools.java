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

import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.contact.storage.TruncatedContactAttribute;
import com.openexchange.contact.storage.rdb.mapping.Mappers;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.tools.mappings.database.DbMapping;
import com.openexchange.java.Charsets;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link Tools} - Provides constants and utility methods to create SQL statements.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class Tools {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(Tools.class));
	
    /**
     * Constructs an array containing the object IDs of the supplied {@link Contact}s.
     *  
     * @param contacts the contacts to get the IDs from
     * @return the IDs
     */
    public static int[] getObjectIDs(final List<Contact> contacts) {
        if (null == contacts) {
            return null;
        }
        final int[] objectIDs = new int[contacts.size()];
        for (int i = 0; i < objectIDs.length; i++) {
            objectIDs[i] = contacts.get(i).getObjectID();                        
        }
        return objectIDs;
    }
    
    public static String toCSV(final int[] values) {
        final StringBuilder stringBuilder = new StringBuilder();
        if (null != values && 0 < values.length) {
            stringBuilder.append(values[0]);
            for (int i = 1; i < values.length; i++) {
                stringBuilder.append(',').append(values[i]);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Gets a string to be used as parameter values in <code>INSERT</code>- or <code>UPDATE</code>-statements.
     *   
     * @param count the number of parameters 
     * @return the parameter string without surrounding parentheses, e.g. "?,?,?,?"
     */
    public static String getParameters(final int count) {
        final StringBuilder parametersBuilder = new StringBuilder(2 * count);
        if (0 < count) {
            parametersBuilder.append('?');
            for (int i = 1; i < count; i++) {
                parametersBuilder.append(",?");                
            }
        }
        return parametersBuilder.toString();
    }

    private static int getMaximumSize(final Connection connection, final Table table, final String columnLabel) {
    	try {
			return DBUtils.getColumnSize(connection, table.toString(), columnLabel);
        } catch (final SQLException x) {
            LOG.error(x.getMessage(), x);
            return 0;
        }
    }
    
    public static OXException truncation(final Connection connection, final DataTruncation e, final Contact contact, final Table table)
    		throws OXException {
        final String[] truncatedColumns = DBUtils.parseTruncatedFields(e);
        final StringBuilder stringBuilder = new StringBuilder();
        /*
         * create truncated attributes
         */
        final OXException.Truncated[] truncatedAttributes = new OXException.Truncated[truncatedColumns.length];
        for (int i = 0; i < truncatedColumns.length; i++) {
        	final String columnLabel = truncatedColumns[i];
        	final int maximumSize =  getMaximumSize(connection, table, columnLabel);
        	final ContactField field = Mappers.CONTACT.getMappedField(columnLabel);
    		final DbMapping<? extends Object, Contact> mapping = Mappers.CONTACT.get(field);
    		final Object object = mapping.get(contact);
			final int actualSize = null != object && String.class.isInstance(object) ? 
					Charsets.getBytes((String) object, Charsets.UTF_8).length : 0;
			stringBuilder.append(mapping.getReadableName());       		        		
			truncatedAttributes[i] = new TruncatedContactAttribute(field, maximumSize, actualSize);
        	if (i != truncatedColumns.length - 1) {
        		stringBuilder.append(", ");
        	}
		}
        /*
         * create truncation exception
         */
        final OXException truncationException;
        if (truncatedAttributes.length > 0) {
            final OXException.Truncated truncated = truncatedAttributes[0];
            truncationException = ContactExceptionCodes.DATA_TRUNCATION.create(e, stringBuilder.toString(), Integer.valueOf(truncated.getMaxSize()),
                Integer.valueOf(truncated.getLength()));
        } else {
            truncationException = ContactExceptionCodes.DATA_TRUNCATION.create(e, stringBuilder.toString(), Integer.valueOf(-1), Integer.valueOf(-1));
        }
        for (final OXException.Truncated truncated : truncatedAttributes) {
            truncationException.addProblematic(truncated);
        }
        return truncationException;
    }
    
    private Tools() {
        // prevent instantiation
    }

}
