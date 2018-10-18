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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.subscribe.google.parser.consumers;

import java.util.function.BiConsumer;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.extensions.PostalAddress;
import com.openexchange.groupware.container.Contact;

/**
 * {@link UnstructuredPostalAddressConsumer} - Parses the contact's postal addresses. Note that google
 * can store an unlimited mount of postal addresses for a contact due to their different
 * data model (probably EAV). Our contacts API however can only store a home, business and other
 * address, therefore we only fetch the first three we encounter. Furthermore, we set as home
 * address the {@link PostalAddress} that is marked as primary.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class UnstructuredPostalAddressConsumer implements BiConsumer<ContactEntry, Contact> {

    /**
     * Initialises a new {@link UnstructuredPostalAddressConsumer}.
     */
    public UnstructuredPostalAddressConsumer() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.function.BiConsumer#accept(java.lang.Object, java.lang.Object)
     */
    @Override
    public void accept(ContactEntry t, Contact u) {
        if (!t.hasPostalAddresses()) {
            return;
        }
        int count = 0;
        for (PostalAddress pa : t.getPostalAddresses()) {
            if (pa.getPrimary()) {
                u.setAddressHome(pa.getValue());
            }
            switch (count++) {
                case 0:
                    u.setAddressHome(pa.getValue());
                    break;
                case 1:
                    u.setAddressBusiness(pa.getValue());
                    break;
                case 2:
                    u.setAddressOther(pa.getValue());
                    break;
                default:
                    return;
            }
        }
    }
}
