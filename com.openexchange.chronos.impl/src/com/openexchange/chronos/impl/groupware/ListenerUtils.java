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

package com.openexchange.chronos.impl.groupware;

import java.util.List;
import java.util.stream.Collectors;
import com.openexchange.chronos.Attendee;
import com.openexchange.java.Strings;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ColumnFieldOperand;
import com.openexchange.search.internal.operands.ConstantOperand;

/**
 * {@link ListenerUtils}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
final class ListenerUtils {

    /**
     * Creates a new {@link SearchTerm} with {@link SingleOperation#EQUALS} as operand
     * 
     * @return A new {@link SingleSearchTerm}
     */
    static SingleSearchTerm equalsTerm() {
        return new SingleSearchTerm(SingleOperation.EQUALS);
    }

    /**
     * Creates a new {@link SearchTerm} with {@link SingleOperation#EQUALS} as operand
     * on the given field as {@link ColumnFieldOperand}
     * 
     * @param field The field to search on
     * @return A new {@link SingleSearchTerm}
     */
    static SingleSearchTerm eqaulsFieldTerm(Enum<?> field) {
        return equalsTerm().addOperand(new ColumnFieldOperand<Enum<?>>(field));
    }

    /**
     * Creates a new {@link SearchTerm} with {@link SingleOperation#EQUALS} as operand
     * on the given field as {@link ColumnFieldOperand} for the specific user
     * 
     * @param field The field to search on
     * @param userID The user to search
     * @return A new {@link SingleSearchTerm}
     */
    static SingleSearchTerm equalsFieldUserTerm(Enum<?> field, int userID) {
        return eqaulsFieldTerm(field).addOperand(new ConstantOperand<Integer>(Integer.valueOf(userID)));
    }

    /**
     * Get folder identifier per attendee. Empty folder IDs will be skipped.
     * 
     * @param list The {@link Attendee}s list
     * @return A {@link List} containing the attendees folder identifiers
     */
    static List<String> getAttendeeFolders(List<Attendee> list) {
        return list.stream().filter(a -> !Strings.isEmpty(a.getFolderId())).map(Attendee::getFolderId).collect(Collectors.toList());
    }

    /**
     * Get the user from an attendee list.
     * 
     * @param userId The identifier of the user
     * @param attendees The {@link Attendee}s
     * @return The user as {@link List} of {@link Attendee}s. {@link List#size()} might be greater than <code>1</code>!
     */
    static List<Attendee> getUser(int userId, List<Attendee> attendees) {
        return attendees.stream().filter(e -> e.getEntity() == userId).collect(Collectors.toList());
    }

}
