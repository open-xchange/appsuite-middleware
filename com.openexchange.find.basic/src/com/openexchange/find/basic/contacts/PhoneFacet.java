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

package com.openexchange.find.basic.contacts;

import java.util.List;
import com.openexchange.find.contacts.ContactsFacetType;
import com.openexchange.find.contacts.ContactsStrings;
import com.openexchange.find.facet.FormattableDisplayItem;
import com.openexchange.groupware.contact.helpers.ContactField;

/**
 * {@link PhoneFacet}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class PhoneFacet extends ContactSearchFieldFacet {

    private static final long serialVersionUID = -9131205652463933031L;

    static final ContactField[] PHONE_FIELDS = {
        ContactField.TELEPHONE_ASSISTANT, ContactField.TELEPHONE_BUSINESS1, ContactField.TELEPHONE_BUSINESS2,
        ContactField.TELEPHONE_CALLBACK, ContactField.TELEPHONE_CAR, ContactField.TELEPHONE_COMPANY, ContactField.TELEPHONE_HOME1,
        ContactField.TELEPHONE_HOME2, ContactField.TELEPHONE_IP, ContactField.TELEPHONE_ISDN, ContactField.TELEPHONE_OTHER,
        ContactField.TELEPHONE_PAGER, ContactField.TELEPHONE_PRIMARY, ContactField.TELEPHONE_RADIO, ContactField.TELEPHONE_TELEX,
        ContactField.TELEPHONE_TTYTDD, ContactField.CELLULAR_TELEPHONE1, ContactField.CELLULAR_TELEPHONE2
    };

    /**
     * Initializes a new {@link PhoneFacet}.
     *
     * @param query The query to insert into the display item
     * @param tokenized The tokenized query to insert into the filter
     */
    public PhoneFacet(String query, List<String> tokenized) {
        super(ContactsFacetType.PHONE, new FormattableDisplayItem(ContactsStrings.FACET_PHONE, query), tokenized);
    }

    @Override
    protected ContactField[] getFields() {
        return PHONE_FIELDS;
    }

}
