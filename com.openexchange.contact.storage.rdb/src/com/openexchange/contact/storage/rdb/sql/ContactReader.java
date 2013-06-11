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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.contact.storage.rdb.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.contact.storage.rdb.internal.RdbServiceLookup;
import com.openexchange.contact.storage.rdb.mapping.Mappers;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.Strings;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;


/**
 * {@link ContactReader}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ContactReader {

    private final Connection connection;
    private final int contextID;
    private final ResultSet resultSet;

    private Context context;

    /**
     * Initializes a new {@link ContactReader}.
     *
     * @param contextID The context ID
     * @param connection The database connection
     * @param resultSet The result set to read contacts from
     */
    public ContactReader(int contextID, Connection connection, ResultSet resultSet) {
        super();
        this.connection = connection;
        this.contextID = contextID;
        this.resultSet = resultSet;
    }

    /**
     * Deserializes all contacts found in the result set, using the supplied fields.
     *
     * @param fields The contact fields to read
     * @return The contacts, or an empty list if no there were no results
     * @throws SQLException
     * @throws OXException
     */
    public List<Contact> readContacts(ContactField[] fields) throws SQLException, OXException {
        List<Contact> contacts = new ArrayList<Contact>();
        while (resultSet.next()) {
            contacts.add(patch(Mappers.CONTACT.fromResultSet(resultSet, fields)));
        }
        return contacts;
    }

    /**
     * Deserializes the first contact found in the result set, using the supplied fields.
     *
     * @param fields The contact fields to read
     * @return The contact, or <code>null</code> if there was no result
     * @throws SQLException
     * @throws OXException
     */
    public Contact readContact(ContactField[] fields) throws SQLException, OXException {
        return resultSet.next() ? patch(Mappers.CONTACT.fromResultSet(resultSet, fields)) : null;
    }

    private Contact patch(Contact contact) throws OXException {
        if (null != contact && 0 < contact.getInternalUserId() && contact.containsEmail1()) {
            UserSettingMail userSettingMail = UserSettingMailStorage.getInstance().getUserSettingMail(
                contact.getInternalUserId(), getContext(), connection);
            String defaultSendAddress = userSettingMail.getSendAddr();
            if (false == Strings.isEmpty(defaultSendAddress)) {
                contact.setEmail1(defaultSendAddress);
            }
        }
        return contact;
    }

    private Context getContext() throws OXException {
        if (null == context) {
            context = RdbServiceLookup.getService(ContextService.class, true).getContext(contextID);
        }
        return context;
    }

}
