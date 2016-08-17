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

import java.util.Comparator;
import java.util.Locale;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class UseCountComparator implements Comparator<Contact> {

    private Comparator<Contact> contactComparator;

    public UseCountComparator(final Locale locale) {
        super();
        this.contactComparator = new SpecialAlphanumSortContactComparator(locale);
    }

    public UseCountComparator(final Comparator<Contact> comp) {
        this(Locale.US);
        this.contactComparator = comp;
    }

    @Override
    public int compare(final Contact o1, final Contact o2) {
        int comp = o2.getUseCount() - o1.getUseCount();
        if (0 != comp) {
            return comp;
        }
        if (o1.getParentFolderID() != o2.getParentFolderID()) {
            if (o1.getParentFolderID() == FolderObject.SYSTEM_LDAP_FOLDER_ID) {
                return -1;
            }
            if (o2.getParentFolderID() == FolderObject.SYSTEM_LDAP_FOLDER_ID) {
                return 1;
            }
        }
        if (null != contactComparator) {
            return contactComparator.compare(o1, o2);
        }
        return o2.getDisplayName().compareTo(o1.getDisplayName());
    }

}
