/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.jslob;

import static org.junit.Assert.assertEquals;
import java.util.List;
import org.json.JSONObject;
import org.junit.Test;
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
     * 
     * @param name
     */
    public Bug34552Test() {
        super();
    }

    @Test
    public void testUpdateIsVisibleImmediately() throws Exception {
        getClient().execute(new SetRequest("io.ox/portal", WIDGETS));

        /*
         * As we set the above JSlob, none of the subsequent JSON function calls must fail.
         */
        List<JSlob> jslobs = getClient().execute(new ListRequest("io.ox/portal")).getJSlobs();
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

        getClient().execute(new SetRequest("io.ox/portal", json.toString()));
        jslobs = getClient().execute(new ListRequest("io.ox/portal")).getJSlobs();
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
