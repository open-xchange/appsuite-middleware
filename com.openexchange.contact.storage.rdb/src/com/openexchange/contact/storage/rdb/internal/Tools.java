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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.exception.OXException;
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
     * An array of all contact fields as used by the contacts database table.
     */
    public static final ContactField[] CONTACT_DATABASE_FIELDS;
    static {
        final EnumSet<ContactField> dbFields = EnumSet.complementOf(EnumSet.of(ContactField.IMAGE1_URL, ContactField.IMAGE1_CONTENT_TYPE, 
            ContactField.IMAGE_LAST_MODIFIED, ContactField.IMAGE1, ContactField.DISTRIBUTIONLIST));
        CONTACT_DATABASE_FIELDS = dbFields.toArray(new ContactField[dbFields.size()]);        
    }
    
    /**
     * An array of all contact fields as used by the images database table.
     */
    public static final ContactField[] IMAGE_DATABASE_FIELDS = { ContactField.OBJECT_ID, ContactField.IMAGE1, ContactField.LAST_MODIFIED, 
        ContactField.IMAGE1_CONTENT_TYPE, ContactField.CONTEXTID };

    /**
     * An array of all contact fields as used by the distribution list database table.
     */
    public static final ContactField[] DISTLIST_DATABASE_FIELDS = { ContactField.OBJECT_ID, 
        ContactField.NUMBER_OF_DISTRIBUTIONLIST /* fake intfield02 */, ContactField.NUMBER_OF_LINKS /* fake intfield03 */, 
        ContactField.NUMBER_OF_IMAGES /* fake intfield04 */, ContactField.DISPLAY_NAME, ContactField.SUR_NAME, ContactField.GIVEN_NAME, 
        ContactField.MIDDLE_NAME  /* fake field03 */, ContactField.CONTEXTID };

    /**
     * A set of contact fields containing those fields that are outsourced to the image table.
     */
    private static final EnumSet<ContactField> ADDITIONAL_IMAGE_FIELDS = EnumSet.of(ContactField.IMAGE1_CONTENT_TYPE, 
        ContactField.IMAGE_LAST_MODIFIED, ContactField.IMAGE1);
        
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
            columnsBuilder.append(fields[0].getFieldName());
            for (int i = 1; i < fields.length; i++) {
                columnsBuilder.append(',').append(fields[i].getFieldName());
            }
        }
        return columnsBuilder.toString();
    }
    
    /**
     * Constructs an array of those fields that are relevant for the specific table.  
     *  
     * @param fields
     * @param table
     * @return
     */
    public static ContactField[] filterFields(final ContactField[] fields, final Table table) {
        if (table.isContactTable()) {
            return filterContactTableFields(fields);
        } else if (table.isImageTable()) {
            return filterImageTableFields(fields);
        } else {
            throw new IllegalArgumentException("fields not known for table " + table);
        }
    }
    
    private static ContactField[] filterImageTableFields(final ContactField[] fields) {
        final Set<ContactField> filteredFields = new HashSet<ContactField>();
        if (null != fields) {
            for (final ContactField field : fields) {
                if (ADDITIONAL_IMAGE_FIELDS.contains(field)) {
                    filteredFields.add(field);
                }
            }
        }
        return filteredFields.toArray(new ContactField[filteredFields.size()]);
    }

    private static ContactField[] filterContactTableFields(final ContactField[] fields) {
        final Set<ContactField> filteredFields = new HashSet<ContactField>();
        if (null != fields) {
            for (final ContactField field : fields) {
                if (field.isDBField()) {
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
            columnsParamsBuilder.append(fields[0].getFieldName()).append("=?,");
            for (int i = 1; i < fields.length; i++) {
                columnsParamsBuilder.append(fields[i].getFieldName()).append("=?,");
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
            if (field.isDBField()) {
                final Object value = resultSet.getObject(field.getFieldName());
                if (false == resultSet.wasNull()) {
                    field.doSwitch(Tools.SETTER, contact, value);    
                }
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
    public static void setParameters(final PreparedStatement stmt, Contact contact, ContactField[] fields) throws SQLException, OXException {
        for (int i = 0; i < fields.length; i++) {
            final Object value = fields[i].doSwitch(Tools.GETTER, contact);                           
            stmt.setObject(i, value, fields[i].getSQLType());
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
        member.setEntryID(resultSet.getInt(2));
        member.setEmailfield(resultSet.getInt(3));
        member.setFolderID(resultSet.getInt(4));
        member.setDisplayname(resultSet.getString(5));
        member.setLastname(resultSet.getString(6));
        member.setFirstname(resultSet.getString(7));
        member.setEmailaddress(resultSet.getString(8));
        return member;
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


    
    private Tools() {
        // prevent instantiation
    }

}
