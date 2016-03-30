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

package com.openexchange.groupware.contact.helpers;

import static com.openexchange.java.Autoboxing.i;
import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.container.Contact;


/**
 * {@link ContactDatabaseSetter}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ContactDatabaseSetter extends AbstractContactSwitcherWithDelegate {

    public ContactDatabaseSetter() {
        super();
        final ContactSwitcher setter = new ContactSetter();
        super.setDelegate(setter);
    }

    /**
     * @see com.openexchange.groupware.contact.helpers.AbstractContactSwitcherWithDelegate#markasdistributionlist(java.lang.Object[])
     */
    @Override
    public Object markasdistributionlist(Object[] objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("MarkAsDistributionList");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }


        final boolean value = castIntegerToBoolean(objects[1]);
        conObj.setMarkAsDistributionlist(value);
        return conObj;
    }

    /**
     * @see com.openexchange.groupware.contact.helpers.AbstractContactSwitcherWithDelegate#privateflag(java.lang.Object[])
     */
    @Override
    public Object privateflag(Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("PrivateFlag");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }

        final boolean value = castIntegerToBoolean(objects[1]);
        conObj.setPrivateFlag(value);
        return conObj;
    }

    @Override
    public Object creationdate(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("CreationDate");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }

        final long timestamp = (Long) objects[1];
        final Date value = new Date(timestamp);
        conObj.setCreationDate(value);
        return conObj;
    }

    @Override
    public Object lastmodified(final Object... objects) throws OXException {
        if (objects.length < 2) {
            throw ContactExceptionCodes.TOO_FEW_ATTRIBUTES.create("LastModified");
        }
        final Contact conObj = (Contact) objects[0];
        if(objects[1] == null) {
            return conObj;
        }

        final long timestamp = (Long) objects[1];
        final Date value = new Date(timestamp);
        conObj.setLastModified(value);
        return conObj;
    }

    private boolean castIntegerToBoolean(Object object) throws OXException {
        if (object instanceof Integer) {
            final int value = i((Integer) object);
            final boolean boolValue;
            if (value == 0) {
                boolValue = false;
            } else {
                boolValue = true;
            }

            return boolValue;
        }

       throw ContactExceptionCodes.UNEXPECTED_ERROR.create("Could not cast to boolean. Object was not an integer.");
    }
}
