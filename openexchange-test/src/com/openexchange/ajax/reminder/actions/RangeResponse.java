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

package com.openexchange.ajax.reminder.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.parser.ReminderParser;
import com.openexchange.ajax.reminder.ReminderTools;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class RangeResponse extends AbstractAJAXResponse {

    private List<ReminderObject> reminders;

    /**
     * @param response
     */
    RangeResponse(final Response response) {
        super(response);
    }

    public ReminderObject[] getReminder(final TimeZone timeZone) throws OXException {
        if (null == reminders) {
            final ReminderParser parser = new ReminderParser(timeZone);
            final JSONArray array = (JSONArray) getData();
            reminders = new ArrayList<ReminderObject>(array.length());
            for (int i = 0; i < array.length(); i++) {
                try {
                    final JSONObject jremind = array.getJSONObject(i);
                    final ReminderObject reminder = new ReminderObject();
                    parser.parse(reminder, jremind);
                    reminders.add(reminder);
                } catch (final JSONException e) {
                    throw OXJSONExceptionCodes.JSON_READ_ERROR.create(array.toString());
                }
            }
        }
        return reminders.toArray(new ReminderObject[reminders.size()]);
    }

    public ReminderObject getReminderByTarget(final TimeZone timeZone, final int targetId) throws OXException {
        return ReminderTools.searchByTarget(getReminder(timeZone), targetId);
    }
}
