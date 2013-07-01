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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.logging.Log;
import com.openexchange.contact.SortOptions;
import com.openexchange.contact.SortOrder;
import com.openexchange.contact.storage.rdb.fields.DistListMemberField;
import com.openexchange.contact.storage.rdb.mapping.Mappers;
import com.openexchange.contact.storage.rdb.sql.Table;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.tools.mappings.MappedTruncation;
import com.openexchange.groupware.tools.mappings.database.DbMapping;
import com.openexchange.java.Charsets;
import com.openexchange.l10n.SuperCollator;
import com.openexchange.log.LogFactory;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link Tools} - Provides constants and utility methods to create SQL statements.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class Tools {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(Tools.class));

    /**
     * Constructs a comma separated string vor the given numeric values.
     *
     * @param values the values
     * @return the csv-string
     */
    public static String toCSV(final int[] values) {
        final com.openexchange.java.StringAllocator stringBuilder = new com.openexchange.java.StringAllocator();
        if (null != values && 0 < values.length) {
            stringBuilder.append(values[0]);
            for (int i = 1; i < values.length; i++) {
                stringBuilder.append(',').append(values[i]);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Gets a string to be used as parameter values in <code>INSERT</code>- or
     * <code>UPDATE</code>-statements.
     *
     * @param count the number of parameters
     * @return the parameter string without surrounding parentheses, e.g.
     * "?,?,?,?"
     */
    public static String getParameters(final int count) {
        final com.openexchange.java.StringAllocator parametersBuilder = new com.openexchange.java.StringAllocator(2 * count);
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

    /**
     * Extracts the relevant information from a {@link DataTruncation}
     * exception and puts it into a corresponding {@link OXException}.
     *
     * @param connection
     * @param e
     * @param contact
     * @param table
     * @return
     * @throws OXException
     */
    public static OXException getTruncationException(final Connection connection, final DataTruncation e, final Contact contact,
    		final Table table) throws OXException {
        final String[] truncatedColumns = DBUtils.parseTruncatedFields(e);
        final com.openexchange.java.StringAllocator stringBuilder = new com.openexchange.java.StringAllocator();
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
			truncatedAttributes[i] = new MappedTruncation<Contact>(mapping, maximumSize, actualSize, mapping.getReadableName());
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
            truncationException = ContactExceptionCodes.DATA_TRUNCATION.create(e, stringBuilder.toString(),
            		Integer.valueOf(truncated.getMaxSize()), Integer.valueOf(truncated.getLength()));
        } else {
            truncationException = ContactExceptionCodes.DATA_TRUNCATION.create(e, stringBuilder.toString(), Integer.valueOf(-1),
            		Integer.valueOf(-1));
        }
        for (final OXException.Truncated truncated : truncatedAttributes) {
            truncationException.addProblematic(truncated);
        }
        return truncationException;
    }

    private Tools() {
        // prevent instantiation
    }

	/**
	 * Parses a numerical identifier from a string, wrapping a possible
	 * NumberFormatException into an OXException.
	 *
	 * @param id the id string
	 * @return the parsed identifier
	 * @throws OXException
	 */
	public static int parse(final String id) throws OXException {
		try {
			return Integer.parseInt(id);
		} catch (final NumberFormatException e) {
			throw ContactExceptionCodes.ID_PARSING_FAILED.create(e, id);
		}
	}

	/**
	 * Parses an array of numerical identifiers from a string, wrapping a
	 * possible NumberFormatException into an OXException.
	 *
	 * @param id the id string
	 * @return the parsed identifier
	 * @throws OXException
	 */
	public static int[] parse(final String[] ids) throws OXException {
	    if (null == ids) {
	        return new int[0];
	    }
        try {
            final int[] intIDs = new int[ids.length];
            for (int i = 0; i < intIDs.length; i++) {
                intIDs[i] = Integer.parseInt(ids[i]);
            }
            return intIDs;
        } catch (final NumberFormatException e) {
			throw ContactExceptionCodes.ID_PARSING_FAILED.create(e, Arrays.toString(ids));
        }
    }

    /**
     * Updates a distribution list member with properties read out from the
     * referenced contact.
     *
     * @param member the distribution list member
     * @param referencedContact the contact referenced by the member
     * @return The updated fields, or an empty array if no fields were updated
     * @throws OXException
     */
    public static DistListMemberField[] updateMember(DistListMember member, Contact referencedContact) throws OXException {
        List<DistListMemberField> updatedFields = new ArrayList<DistListMemberField>();
        if (referencedContact.containsObjectID() && referencedContact.getObjectID() != member.getEntryID()) {
            member.setEntryID(referencedContact.getObjectID());
            updatedFields.add(DistListMemberField.CONTACT_ID);
        }
        if (referencedContact.containsParentFolderID() && referencedContact.getParentFolderID() != member.getFolderID()) {
            member.setFolderID(referencedContact.getParentFolderID());
            updatedFields.add(DistListMemberField.CONTACT_FOLDER_ID);
        }
    	if (referencedContact.containsDisplayName()) {
    		if (null == referencedContact.getDisplayName() && null != member.getDisplayname() ||
    				null != referencedContact.getDisplayName() && false == referencedContact.getDisplayName().equals(member.getDisplayname())) {
    			member.setDisplayname(referencedContact.getDisplayName());
                updatedFields.add(DistListMemberField.DISPLAY_NAME);
    		}
    	}
    	if (referencedContact.containsSurName()) {
    		if (null == referencedContact.getSurName() && null != member.getLastname() ||
    				null != referencedContact.getSurName() && false == referencedContact.getSurName().equals(member.getLastname())) {
    			member.setLastname(referencedContact.getSurName());
                updatedFields.add(DistListMemberField.LAST_NAME);
    		}
    	}
    	if (referencedContact.containsGivenName()) {
    		if (null == referencedContact.getGivenName() && null != member.getFirstname() ||
    				null != referencedContact.getGivenName() && false == referencedContact.getGivenName().equals(member.getFirstname())) {
    			member.setFirstname(referencedContact.getGivenName());
                updatedFields.add(DistListMemberField.FIRST_NAME);
    		}
    	}
    	if (referencedContact.containsEmail1() && DistributionListEntryObject.EMAILFIELD1 == member.getEmailfield()) {
    		if (null == referencedContact.getEmail1() && null != member.getEmailaddress() ||
    				null != referencedContact.getEmail1() && false == referencedContact.getEmail1().equals(member.getEmailaddress())) {
    			member.setEmailaddress(referencedContact.getEmail1(), false);
                updatedFields.add(DistListMemberField.MAIL);
    		}
    	}
    	if (referencedContact.containsEmail2() && DistributionListEntryObject.EMAILFIELD2 == member.getEmailfield()) {
    		if (null == referencedContact.getEmail2() && null != member.getEmailaddress() ||
    				null != referencedContact.getEmail2() && false == referencedContact.getEmail2().equals(member.getEmailaddress())) {
    			member.setEmailaddress(referencedContact.getEmail2(), false);
                updatedFields.add(DistListMemberField.MAIL);
    		}
    	}
    	if (referencedContact.containsEmail3() && DistributionListEntryObject.EMAILFIELD3 == member.getEmailfield()) {
    		if (null == referencedContact.getEmail3() && null != member.getEmailaddress() ||
    				null != referencedContact.getEmail3() && false == referencedContact.getEmail3().equals(member.getEmailaddress())) {
    			member.setEmailaddress(referencedContact.getEmail3(), false);
                updatedFields.add(DistListMemberField.MAIL);
    		}
    	}
    	return updatedFields.toArray(new DistListMemberField[updatedFields.size()]);
    }

    /**
     * Gets the ORDER BY clause from the sort options.
     *
     * @param sortOptions the sort options
     * @return the ORDER BY clause, or an empty String if not specified
     * @throws OXException
     */
    public static String getOrderClause(final SortOptions sortOptions) throws OXException {
        final com.openexchange.java.StringAllocator stringBuilder = new com.openexchange.java.StringAllocator();
        if (null != sortOptions && false == SortOptions.EMPTY.equals(sortOptions)) {
            final SortOrder[] order = sortOptions.getOrder();
            if (null != order && 0 < order.length) {
                stringBuilder.append("ORDER BY ");
                final SuperCollator collator = SuperCollator.get(sortOptions.getCollation());
                stringBuilder.append(getOrderClause(order[0], collator));
                for (int i = 1; i < order.length; i++) {
                    stringBuilder.append(' ').append(getOrderClause(order[i], collator));
                }
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Gets the LIMIT clause from the sort options.
     *
     * @param sortOptions the sort options
     * @return the LIMIT clause, or an empty String if not specified
     * @throws OXException
     */
    public static String getLimitClause(final SortOptions sortOptions) throws OXException {
        final com.openexchange.java.StringAllocator stringBuilder = new com.openexchange.java.StringAllocator();
        if (null != sortOptions && false == SortOptions.EMPTY.equals(sortOptions)) {
            if (0 < sortOptions.getLimit()) {
                stringBuilder.append("LIMIT ");
                if (0 < sortOptions.getRangeStart()) {
                    stringBuilder.append(sortOptions.getRangeStart()).append(',');
                }
                stringBuilder.append(sortOptions.getLimit());
            }
        }
        return stringBuilder.toString();
    }

    private static String getOrderClause(final SortOrder order, final SuperCollator collator) throws OXException {
        final com.openexchange.java.StringAllocator stringBuilder = new com.openexchange.java.StringAllocator();
        if (null == collator || SuperCollator.DEFAULT.equals(collator)) {
            stringBuilder.append(Mappers.CONTACT.get(order.getBy()).getColumnLabel());
        } else {
            stringBuilder.append("CONVERT (").append(Mappers.CONTACT.get(order.getBy()).getColumnLabel()).append(" USING '")
                .append(collator.getSqlCharset()).append("') COLLATE '").append(collator.getSqlCollation()).append('\'');
        }
        if (Order.ASCENDING.equals(order.getOrder())) {
            stringBuilder.append(" ASC");
        } else if (Order.DESCENDING.equals(order.getOrder())) {
            stringBuilder.append(" DESC");
        }
        return stringBuilder.toString();
    }


}
