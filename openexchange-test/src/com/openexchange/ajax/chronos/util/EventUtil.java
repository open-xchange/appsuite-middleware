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
 *    Any modifications to e1 package must retain all copyright notices
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
 *     e1 program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     e1 program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with e1 program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.ajax.chronos.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Objects;
import com.openexchange.testing.httpclient.models.EventData;

/**
 * {@link EventUtil}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class EventUtil {

    public static void compare(EventData e1, EventData e2, boolean assertTrue) {
        if(assertTrue){
            assertTrue("The events aren't equal!", isEqual(e1, e2));
        } else {
            assertFalse("The events are equal, but they shouldn't!", isEqual(e1, e2));
        }
    }

    private static boolean isEqual(EventData e1, EventData e2){
        if (e1 == e2) {
            return true;
          }
          if (e1 == null || e1.getClass() != e2.getClass()) {
            return false;
          }
          return Objects.equals(e1.getStartDate(), e2.getStartDate()) &&
              Objects.equals(e1.getEndDate(), e2.getEndDate()) &&
              Objects.equals(e1.getCreated(), e2.getCreated()) &&
              Objects.equals(e1.getUid(), e2.getUid()) &&
              Objects.equals(e1.getDescription(), e2.getDescription()) &&
              Objects.equals(e1.getAttendees(), e2.getAttendees()) &&
              Objects.equals(e1.getAlarms(), e2.getAlarms()) &&
              Objects.equals(e1.getLastModified(), e2.getLastModified()) &&
              Objects.equals(e1.getModifiedBy(), e2.getModifiedBy()) &&
              Objects.equals(e1.getSummary(), e2.getSummary()) &&
              Objects.equals(e1.getSequence(), e2.getSequence()) &&
              Objects.equals(e1.getId(), e2.getId()) &&
              Objects.equals(e1.getPropertyClass(), e2.getPropertyClass()) &&
              Objects.equals(e1.getOrganizer(), e2.getOrganizer()) &&
              Objects.equals(e1.getTransp(), e2.getTransp()) &&
              Objects.equals(e1.getAllDay(), e2.getAllDay()) &&
              Objects.equals(e1.getColor(), e2.getColor()) &&
              Objects.equals(e1.getFolder(), e2.getFolder()) &&
              Objects.equals(e1.getCreatedBy(), e2.getCreatedBy()) &&
              Objects.equals(e1.getDeleteExceptionDates(), e2.getDeleteExceptionDates()) &&
              Objects.equals(e1.getRecurrenceId(), e2.getRecurrenceId()) &&
              Objects.equals(e1.getCalendarUser(), e2.getCalendarUser()) &&
              Objects.equals(e1.getRrule(), e2.getRrule()) &&
              Objects.equals(e1.getAttachments(), e2.getAttachments()) &&
              Objects.equals(e1.getExtendedProperties(), e2.getExtendedProperties()) &&
              Objects.equals(e1.getGeo(), e2.getGeo()) &&
              Objects.equals(e1.getUrl(), e2.getUrl());
    }

}
