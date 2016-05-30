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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.file.storage.mail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.openexchange.file.storage.mail.FullName.Type;

/**
 * {@link FullNameCollection}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class FullNameCollection implements Iterable<FullName> {

    /** The full name of the virtual attachment folder containing all attachments */
    public final String fullNameAll;

    /** The full name of the virtual attachment folder containing received attachments */
    public final String fullNameReceived;

    /** The full name of the virtual attachment folder containing sent attachments */
    public final String fullNameSent;

    /**
     * Initializes a new {@link FullNameCollection}.
     *
     * @param fullNameAll The full name of the virtual attachment folder containing all attachments
     * @param fullNameReceived The full name of the virtual attachment folder containing received attachments
     * @param fullNameSent The full name of the virtual attachment folder containing sent attachments
     */
    public FullNameCollection(String fullNameAll, String fullNameReceived, String fullNameSent) {
        super();
        this.fullNameAll = fullNameAll;
        this.fullNameReceived = fullNameReceived;
        this.fullNameSent = fullNameSent;
    }

    @Override
    public Iterator<FullName> iterator() {
        return asList().iterator();
    }

    /**
     * Gets the full name for specified type.
     *
     * @param type The type
     * @return The associated full name or <code>null</code>
     */
    public FullName getFullNameFor(Type type) {
        if (null == type) {
            return null;
        }

        switch (type) {
            case ALL:
                return null == fullNameAll ? null : new FullName(fullNameAll, Type.ALL);
            case RECEIVED:
                return null == fullNameReceived ? null : new FullName(fullNameReceived, Type.RECEIVED);
            case SENT:
                return null == fullNameSent ? null : new FullName(fullNameSent, Type.SENT);
            case DEFAULT:
                return new FullName("", Type.DEFAULT);
            default:
                return null;
        }
    }

    /**
     * Gets the {@link List} view for this collection.
     *
     * @return The list
     */
    public List<FullName> asList() {
        List<FullName> fullNames = new ArrayList<FullName>(3);
        if (null != fullNameAll) {
            fullNames.add(new FullName(fullNameAll, Type.ALL));
        }
        if (null != fullNameReceived) {
            fullNames.add(new FullName(fullNameReceived, Type.RECEIVED));
        }
        if (null != fullNameSent) {
            fullNames.add(new FullName(fullNameSent, Type.SENT));
        }
        return fullNames;
    }

}
