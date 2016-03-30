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

package com.openexchange.groupware.container;

import com.openexchange.groupware.container.participants.ConfirmableParticipant;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * {@link ConfirmationsDiffer}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class ConfirmationsDiffer extends Differ<CalendarObject> {

    @SuppressWarnings("unchecked")
    @Override
    public Difference getDifference(CalendarObject original, CalendarObject update) {
        if (!update.containsConfirmations()) {
            return null;
        }

        if (!original.containsConfirmations() && update.containsConfirmations()) {
            Difference difference = new Difference(CalendarObject.CONFIRMATIONS);
            difference.getAdded().addAll(Arrays.asList(update.getConfirmations()));
            return difference;
        }

        if (original.getConfirmations() == update.getConfirmations()) {
            return null;
        }

        if (original.getConfirmations() == null) {
            Difference difference = new Difference(CalendarObject.CONFIRMATIONS);
            difference.getAdded().addAll(Arrays.asList(update.getConfirmations()));
            return difference;
        }

        boolean isDifferent = false;
        Difference difference = new Difference(CalendarObject.CONFIRMATIONS);

        for (ConfirmableParticipant o : original.getConfirmations()) {
            boolean found = false;
            for (ConfirmableParticipant u : update.getConfirmations()) {
                if (o.getEmailAddress() != null && o.getEmailAddress().equalsIgnoreCase(u.getEmailAddress())) {
                    Change change = getChange(o, u);
                    found = true;
                    if (change != null) {
                        isDifferent = true;
                        difference.getChanged().add(change);
                    }
                    break;
                }
            }
            if (!found) {
                difference.getRemoved().add(o);
                isDifferent = true;
            }
        }

        for (ConfirmableParticipant u : update.getConfirmations()) {
            boolean found = false;
            for (ConfirmableParticipant o : original.getConfirmations()) {
                if (u.getEmailAddress() != null && u.getEmailAddress().equalsIgnoreCase(o.getEmailAddress())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                difference.getAdded().add(u);
                isDifferent = true;
            }
        }

        return isDifferent ? difference : null;
    }

    private Change getChange(ConfirmableParticipant original, ConfirmableParticipant update) {
        boolean changed = false;

        ConfirmationChange change = new ConfirmationChange(original.getEmailAddress());
        if (original.getConfirm() != update.getConfirm()) {
            changed = true;
            change.setStatus(original.getConfirm(), update.getConfirm());
        }

        if (original.getMessage() != update.getMessage() && (original.getMessage() == null && update.getMessage() != null || !original.getMessage().equals(update.getMessage()))) {
            changed = true;
            change.setMessage(original.getMessage(), update.getMessage());
            change.setStatus(original.getConfirm(), update.getConfirm()); // Also set status on change.
        }

        return changed ? change : null;
    }

    @Override
    public int getColumn() {
        return CalendarObject.CONFIRMATIONS;
    }

}
