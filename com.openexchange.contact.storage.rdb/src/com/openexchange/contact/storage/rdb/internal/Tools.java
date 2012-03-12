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
import java.util.List;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.container.Contact;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link Tools} - Provides constants and utility methods to create SQL statements.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class Tools {

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
      
//    private static ContactField getByColumnLabel(final String columnLabel) {
//    	
//    	
//    }
    
    public static OXException truncation(final Connection connection, final DataTruncation e, final Contact contact, final Table table) {
        final String[] columns = DBUtils.parseTruncatedFields(e);
        final OXException.Truncated[] truncatedExceptions = new OXException.Truncated[columns.length];
        for (int i = 0; i < columns.length; i++) {
        	/*
        	 * determine contact field by column label        	
        	 */
			final String columnlabel = columns[i];
						
        	
		}
//        
//        
//            	
//        final StringBuilder sFields = new StringBuilder();
//
//        for (final String field : columns) {
//            final ContactField cf = ContactField.getByFieldName(field);
//            if (cf == null) {
//                sFields.append(field);
//            } else {
//                sFields.append(cf.getReadableName());
//            }
//            sFields.append(", ");
//        }
//        sFields.setLength(sFields.length() - 2);
//        final OXException.Truncated[] truncateds = new OXException.Truncated[columns.length];
//        for (int i = 0; i < columns.length; i++) {
//            for (int j = 0; j < 650; j++) {
//                final Mapper mapper = mapping[j];
//                if ((mapper != null) && mapper.getDBFieldName().equals(columns[i])) {
//                    int tmp = 0;
//                    try {
//                        tmp = DBUtils.getColumnSize(con, table, columns[i]);
//                    } catch (final SQLException e) {
//                        LOG.error(e.getMessage(), e);
//                        tmp = 0;
//                    }
//                    final int maxSize = tmp;
//                    final int attributeId = j;
//                    truncateds[i] = new OXException.Truncated() {
//
//                        @Override
//                        public int getId() {
//                            return attributeId;
//                        }
//
//                        @Override
//                        public int getLength() {
//                            return Charsets.getBytes(mapping[attributeId].getValueAsString(co), Charsets.UTF_8).length;
//                        }
//
//                        @Override
//                        public int getMaxSize() {
//                            return maxSize;
//                        }
//                    };
//                }
//            }
//        }
//        final OXException retval;
//        if (truncateds.length > 0) {
//            retval = ContactExceptionCodes.DATA_TRUNCATION.create(
//                se,
//                sFields.toString(),
//                I(truncateds[0].getMaxSize()),
//                I(truncateds[0].getLength()));
//        } else {
//            retval = ContactExceptionCodes.DATA_TRUNCATION.create(se, sFields.toString(), I(-1), I(-1));
//        }
//        for (final OXException.Truncated truncated : truncateds) {
//            retval.addProblematic(truncated);
//        }
//        return retval;

        //TODO
        return ContactExceptionCodes.DATA_TRUNCATION.create();
    }
    
    private Tools() {
        // prevent instantiation
    }

}
