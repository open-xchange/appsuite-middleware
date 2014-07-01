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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.find.common;

import com.openexchange.find.facet.DefaultDisplayItem;
import com.openexchange.find.facet.DisplayItemVisitor;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Strings;

/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class ContactDisplayItem extends DefaultDisplayItem {

    private final Contact contact;

    private final String defaultValue;

    public ContactDisplayItem(final Contact contact) {
        super();
        this.contact = contact;
        this.defaultValue = extractDefaultValue(contact);
    }

    @Override
    public void accept(DisplayItemVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Contact getItem() {
        return contact;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String toString() {
        return "ContactDisplayItem [contact=" + contact.getDisplayName() + "(" + contact.getObjectID() + ")]";
    }

    public static String extractDefaultValue(Contact contact) {
        StringBuilder sb = new StringBuilder(64);
        String displayName = contact.getDisplayName();
        if (Strings.isEmpty(displayName)) {
            String surName = contact.getSurName();
            String givenName = contact.getGivenName();
            if (Strings.isEmpty(surName)) {
                if (!Strings.isEmpty(givenName)) {
                    sb.append(givenName);
                }
            } else {
                if (Strings.isEmpty(givenName)) {
                    sb.append(surName);
                } else {
                    sb.append(surName).append(", ").append(givenName);
                }
            }
        } else {
            sb.append(displayName);
        }

        String primaryAddress = extractPrimaryMailAddress(contact);
        if (primaryAddress != null) {
            if (sb.length() == 0) {
                sb.append(primaryAddress);
            } else {
                sb.append(" (").append(primaryAddress).append(')');
            }
        }

        return sb.toString();
    }

    private static String extractPrimaryMailAddress(Contact contact) {
        String address = contact.getEmail1();
        if (Strings.isEmpty(address)) {
            address = contact.getEmail2();
        }
        if (Strings.isEmpty(address)) {
            address = contact.getEmail3();
        }

        return address;
    }
}
