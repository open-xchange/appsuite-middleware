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

package com.openexchange.ajax.jslob;

import java.util.List;
import org.json.JSONObject;
import com.openexchange.ajax.jslob.actions.ListRequest;
import com.openexchange.ajax.jslob.actions.SetRequest;
import com.openexchange.jslob.JSlob;


/**
 * {@link Bug34552Test}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class Bug34552Test extends AbstractJSlobTest {

    private static final String WIDGETS = "{\"widgets\":{\"user\":{\"tasks_0\":{\"id\":\"tasks_0\",\"enabled\":true,\"index\":1,\"inverse\":false,\"color\":\"green\",\"userWidget\":true,\"type\":\"tasks\",\"props\":{},\"plugin\":\"plugins/portal/tasks/register\"},\"mail_0\":{\"id\":\"mail_0\",\"enabled\":true,\"index\":2,\"inverse\":false,\"color\":\"blue\",\"userWidget\":true,\"type\":\"mail\",\"props\":{},\"plugin\":\"plugins/portal/mail/register\"},\"birthdays_0\":{\"id\":\"birthdays_0\",\"enabled\":true,\"index\":3,\"inverse\":false,\"color\":\"lightblue\",\"userWidget\":true,\"type\":\"birthdays\",\"props\":{},\"plugin\":\"plugins/portal/birthdays/register\"}}}}";

    /**
     * Initializes a new {@link Bug34552Test}.
     * @param name
     */
    public Bug34552Test(String name) {
        super(name);
    }

    public void testUpdateIsVisibleImmediately() throws Exception {
        client.execute(new SetRequest("io.ox/portal", WIDGETS));

        /*
         * As we set the above JSlob, none of the subsequent JSON function calls must fail.
         */
        List<JSlob> jslobs = client.execute(new ListRequest("io.ox/portal")).getJSlobs();
        JSONObject json = jslobs.get(0).getJsonObject();
        JSONObject userWidgets = json.getJSONObject("widgets").getJSONObject("user");
        JSONObject tasksWidget = userWidgets.getJSONObject("tasks_0");
        JSONObject mailWidget = userWidgets.getJSONObject("mail_0");
        JSONObject birthdayWidget = userWidgets.getJSONObject("birthdays_0");

        assertEquals("Wrong widget index", 1, tasksWidget.getInt("index"));
        assertEquals("Wrong widget index", 2, mailWidget.getInt("index"));
        assertEquals("Wrong widget index", 3, birthdayWidget.getInt("index"));

        /*
         * Shuffle and write back
         */
        tasksWidget.put("index", 2);
        mailWidget.put("index", 3);
        birthdayWidget.put("index", 1);

        client.execute(new SetRequest("io.ox/portal", json.toString()));
        jslobs = client.execute(new ListRequest("io.ox/portal")).getJSlobs();
        JSONObject reloaded = jslobs.get(0).getJsonObject();
        userWidgets = reloaded.getJSONObject("widgets").getJSONObject("user");
        tasksWidget = userWidgets.getJSONObject("tasks_0");
        mailWidget = userWidgets.getJSONObject("mail_0");
        birthdayWidget = userWidgets.getJSONObject("birthdays_0");
        assertEquals("Wrong widget index", 2, tasksWidget.getInt("index"));
        assertEquals("Wrong widget index", 3, mailWidget.getInt("index"));
        assertEquals("Wrong widget index", 1, birthdayWidget.getInt("index"));
    }

}
