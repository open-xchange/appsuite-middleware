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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.contact.helpers.ContactDatabaseGetter;
import com.openexchange.groupware.contact.helpers.ContactDatabaseSetter;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.contact.helpers.ContactSwitcher;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;

/**
 * {@link Tools} - Provides constants and utility methods to create SQL statements.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class Tools {

    /**
     * A {@link ContactSwitcher} to set properties in a contact from database values
     */
    public static final ContactSwitcher SETTER = new ContactDatabaseSetter();

    /**
     * A {@link ContactSwitcher} to get properties from a contact to put into the database
     */
    public static final ContactSwitcher GETTER = new ContactDatabaseGetter();
    
        
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
    
    /**
     * Gets the database column names mapped to the supplied contact fields separated by ','-chars; ready to be used in 
     * <code>SELECT</code> clauses.  
     * 
     * @param fields the contact fields
     * @return the ','-separated database column names without parenthesis, e.g. "field01,field02,field03"
     */
    public static String getColumns(final ContactField[] fields) {
        final StringBuilder columnsBuilder = new StringBuilder(10 * fields.length);                
        if (null != fields && 0 < fields.length) {
            columnsBuilder.append(fields[0].getDbName());
            for (int i = 1; i < fields.length; i++) {
                columnsBuilder.append(',').append(fields[i].getDbName());
            }
        }
        return columnsBuilder.toString();
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
     * 
     * @param fields
     * @param validFields
     * @return
     */
    public static ContactField[] filter(final ContactField[] fields, final EnumSet<ContactField> validFields) {
        return filter(fields, validFields, (ContactField[])null);
    }
        
    /**
     * Filters a given array of {@link ContactField}s by a defined set of valid fields. Optionally adds additional mandatory fields to 
     * the result. 
     *  
     * @param fields the fields to filter
     * @param validFields the set of valid fields in the filtered result
     * @param mandatoryFields the mandatory fields to be always added to the result
     * @return the filtered fields
     */
    public static ContactField[] filter(final ContactField[] fields, final EnumSet<ContactField> validFields, final ContactField... mandatoryFields) {
        final Set<ContactField> filteredFields = new HashSet<ContactField>();
        if (null != fields) {
            for (final ContactField field : fields) {
                if (validFields.contains(field)) {
                    filteredFields.add(field);
                }
            }
        }
        if (null != mandatoryFields) {
            for (final ContactField field : mandatoryFields) {
                filteredFields.add(field);
            }
        }
        return filteredFields.toArray(new ContactField[filteredFields.size()]);
    }

    public static ContactField[] filterOut(final ContactField[] fields, final EnumSet<ContactField> forbiddenFields) {
        final Set<ContactField> filteredFields = new HashSet<ContactField>();
        if (null != fields) {
            for (final ContactField field : fields) {
                if (false == forbiddenFields.contains(field)) {
                    filteredFields.add(field);
                }
            }
        }
        return filteredFields.toArray(new ContactField[filteredFields.size()]);
    }
    
    /**
     * Gets a string containing assignments of database column names to parameter values. 
     * 
     * @param fields the contact fields
     * @return the assignments string, e.g. "field01=?,field02=?,field03=?"
     */
    public static String getAssignments(final ContactField[] fields) {
        final StringBuilder columnsParamsBuilder = new StringBuilder(10 * fields.length); 
        if (null != fields && 0 < fields.length) {
            columnsParamsBuilder.append(fields[0].getDbName()).append("=?");
            for (int i = 1; i < fields.length; i++) {
                columnsParamsBuilder.append(',').append(fields[i].getDbName()).append("=?");
            }
        }
        return columnsParamsBuilder.toString();
    }
    
    /**
     * Creates a new {@link Contact} from the current row in the supplied database result set, based on the given fields. 
     * 
     * @param resultSet the result set
     * @param fields the contact fields
     * @return a new contact
     * @throws OXException
     * @throws SQLException
     */
    public static Contact fromResultSet(final ResultSet resultSet, final ContactField[] fields) throws OXException, SQLException {
        final Contact contact = new Contact();
        for (final ContactField field : fields) {
            final Object value = resultSet.getObject(field.getDbName());
            if (false == resultSet.wasNull()) {
                field.doSwitch(Tools.SETTER, contact, value);    
            }
        }
        return contact;
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
    
    /**
     * Sets the supplied statement's database parameters from the fields found in the supplied contact.
     * 
     * @param stmt the statement to fill
     * @param contact the contact to read the values from
     * @param fields the fields to read
     * @throws SQLException
     * @throws OXException
     */
    public static void setParameters(final PreparedStatement stmt, final Contact contact, final ContactField[] fields) throws SQLException, OXException {
        for (int i = 0; i < fields.length; i++) {
            final Object value = fields[i].doSwitch(Tools.GETTER, contact);
            stmt.setObject(i + 1, value, fields[i].getSQLType());
        }
    }

    /**
     * Creates a new {@link DistributionListEntryObject} from the supplied result set, assuming that all 9 colums were fetched
     * in the correct order.
     * 
     * @param resultSet
     * @return
     * @throws SQLException
     * @throws OXException 
     */
    public static DistributionListEntryObject fromDistListResultSet(final ResultSet resultSet) throws SQLException, OXException {
        final DistributionListEntryObject member = new DistributionListEntryObject();
        int entryID = resultSet.getInt(ContactField.NUMBER_OF_DISTRIBUTIONLIST.getDbName()); // intfield02
        if (false == resultSet.wasNull()) {
            member.setEntryID(entryID);
        }
        final int emailField = resultSet.getInt(ContactField.NUMBER_OF_LINKS.getDbName()); // intfield03
        if (false == resultSet.wasNull()) {
            member.setEmailfield(emailField);
        }
        final int folderID = resultSet.getInt(ContactField.NUMBER_OF_IMAGES.getDbName()); // intfield04
        if (false == resultSet.wasNull()) {
            member.setFolderID(folderID);
        }
        final String displayName = resultSet.getString(ContactField.DISPLAY_NAME.getDbName()); // field01
        if (false == resultSet.wasNull()) {
            member.setDisplayname(displayName);
        }
        final String lastName = resultSet.getString(ContactField.SUR_NAME.getDbName()); // field02
        if (false == resultSet.wasNull()) {
            member.setLastname(lastName);
        }
        final String firstName = resultSet.getString(ContactField.GIVEN_NAME.getDbName()); // field03
        if (false == resultSet.wasNull()) {
            member.setFirstname(firstName);
        }
        final String emailAddress = resultSet.getString(ContactField.MIDDLE_NAME.getDbName()); // field04
        if (false == resultSet.wasNull()) {
            member.setEmailaddress(emailAddress);
        }
        return member;
    }   
    
    public static void setParameters(final PreparedStatement stmt, final ContactField[] fields, final DistributionListEntryObject member, final int contactID, final int contextID) throws SQLException, OXException {
        for (int i = 0; i < fields.length; i++) {
            final int parameterIndex = i + 1; 
            switch (fields[i]) {
            case OBJECT_ID: // intfield01
                stmt.setInt(parameterIndex, contactID); 
                break;
            case NUMBER_OF_DISTRIBUTIONLIST: // intfield02 
                if (member.containsEntryID()) { 
                    stmt.setInt(parameterIndex, member.getEntryID());
                } else {
                    stmt.setNull(parameterIndex, Types.INTEGER);
                }
                break;
            case NUMBER_OF_LINKS: // intfield03
                if (member.containsEmailfield()) {
                    stmt.setInt(parameterIndex, member.getEmailfield());
                }
                break;
            case NUMBER_OF_IMAGES: // intfield04
                if (member.containsFolderld()) {
                    stmt.setInt(parameterIndex, member.getFolderID());
                } else {
                    stmt.setNull(parameterIndex, Types.INTEGER);
                }
                break;
            case DISPLAY_NAME: // field01
                if (member.containsDisplayname()) {
                    stmt.setString(parameterIndex, member.getDisplayname());
                } else {
                    stmt.setNull(parameterIndex, Types.VARCHAR);
                }
                break;
            case SUR_NAME: // field02
                if (member.containsLastname()) {
                    stmt.setString(parameterIndex, member.getLastname());
                } else {
                    stmt.setNull(parameterIndex, Types.VARCHAR);
                }
                break;
            case GIVEN_NAME: // field03
                if (member.containsFistname()) {
                    stmt.setString(parameterIndex, member.getFirstname());
                } else {
                    stmt.setNull(parameterIndex, Types.VARCHAR);
                }
                break;
            case MIDDLE_NAME: // field04
                if (member.containsEmailaddress()) {
                    stmt.setString(parameterIndex, member.getEmailaddress());
                } else {
                    stmt.setNull(parameterIndex, Types.VARCHAR);
                }
                break;
            case CONTEXTID: // cid
                stmt.setInt(parameterIndex, contextID);
                break;
            default:
                throw new IllegalArgumentException(fields[i].toString());
            }
        }        
    }
    
    /**
     * Sets the supplied statement's database parameters from the properties found in the supplied contact. 
     * All of the 9 parameters are always set.
     * 
     * @param stmt
     * @param member
     * @param contactID
     * @param contextID
     * @throws SQLException
     * @throws OXException
     */
    public static void setParameters(final PreparedStatement stmt, final DistributionListEntryObject member, final int contactID, final int contextID) throws SQLException, OXException {
        /*
         * intfield01  - ID of corresponding entry in prg_contacts table   
           intfield02  - Object ID of the member's contact if the member is an existing contact
           intfield03  - Which email field of an existing contact (if any) is used for the mail field. 0 independent contact 1 default email field (email1) 2 second email field (email2) 3 third email field (email3) 
           intfield04  - Folder ID of the member's contact if the member is an existing contact
           field01     - Display name 
           field02     - Last name
           field03     - First name
           field04     - E-Mail address
           cid         - Context id
         */
        stmt.setInt(1, contactID);
        if (member.containsEntryID()) {
            stmt.setInt(2, member.getEntryID());
        } else {
            stmt.setNull(2, Types.INTEGER);
        }
        if (member.containsEmailfield()) {
            stmt.setInt(3, member.getEmailfield());
        } else {
            stmt.setNull(3, Types.INTEGER);
        }
        if (member.containsFolderld()) {
            stmt.setInt(4, member.getFolderID());
        } else {
            stmt.setNull(4, Types.INTEGER);
        }
        if (member.containsDisplayname()) {
            stmt.setString(5, member.getDisplayname());
        } else {
            stmt.setNull(5, Types.VARCHAR);
        }
        if (member.containsLastname()) {
            stmt.setString(6, member.getLastname());
        } else {
            stmt.setNull(6, Types.VARCHAR);
        }
        if (member.containsFistname()) {
            stmt.setString(7, member.getFirstname());
        } else {
            stmt.setNull(7, Types.VARCHAR);
        }
        if (member.containsEmailaddress()) {
            stmt.setString(8, member.getEmailaddress());
        } else {
            stmt.setNull(8, Types.VARCHAR);
        }
        stmt.setInt(9, contextID);
    }
    
    public static OXException truncation(final Connection connection, final DataTruncation e, final Contact contact, final Table table) {
        //TODO
        return ContactExceptionCodes.DATA_TRUNCATION.create();
    }

    
    private Tools() {
        // prevent instantiation
    }

}
