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

package com.openexchange.ajax.framework;

import static org.junit.Assert.assertNotNull;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.config.actions.GetRequest;
import com.openexchange.ajax.config.actions.GetResponse;
import com.openexchange.ajax.config.actions.SetRequest;
import com.openexchange.ajax.config.actions.Tree;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.LocaleTools;

/**
 * Help and caching class for values of the preferences tree.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class UserValues {

    private final AJAXClient client;

    private String inboxFolder;
    private String sentFolder;
    private String trashFolder;
    private String draftsFolder;
    private String sendAddress;
    private Integer privateInfostoreFolder;
    private Integer infostoreTrashFolder;
    private Locale locale;
    private int privateAppointmentFolder = -1;
    private int privateContactFolder = -1;
    private int privateTaskFolder = -1;
    private TimeZone timeZone;
    private int userId = -1;
    private int contextId = -1;

    private String defaultAddress;

    public UserValues(final AJAXClient client) {
        super();
        this.client = client;
    }

    public String getInboxFolder() throws OXException, IOException, JSONException {
        if (null == inboxFolder) {
            inboxFolder = client.execute(new GetRequest(Tree.InboxFolder)).getString();
        }
        return inboxFolder;
    }

    public String getSentFolder() throws OXException, IOException, JSONException {
        if (null == sentFolder) {
            sentFolder = client.execute(new GetRequest(Tree.SentFolder)).getString();
        }
        return sentFolder;
    }

    public String getTrashFolder() throws OXException, IOException, JSONException {
        if (null == trashFolder) {
            trashFolder = client.execute(new GetRequest(Tree.TrashFolder)).getString();
        }
        return trashFolder;
    }

    public String getDraftsFolder() throws OXException, IOException, JSONException {
        if (null == draftsFolder) {
            draftsFolder = client.execute(new GetRequest(Tree.DraftsFolder)).getString();
        }
        return draftsFolder;
    }

    public int getPrivateInfostoreFolder() throws OXException, IOException, JSONException {
        if (null == privateInfostoreFolder) {
            initPrivateFolders();
        }
        return privateInfostoreFolder.intValue();
    }

    public int getInfostoreTrashFolder() throws OXException, IOException, JSONException {
        if (null == infostoreTrashFolder) {
            infostoreTrashFolder = Integer.valueOf(client.execute(new GetRequest(Tree.InfostoreTrashFolder)).getString());
        }
        return infostoreTrashFolder.intValue();
    }

    public String getSendAddress() throws OXException, IOException, JSONException {
        if (null == sendAddress) {
            sendAddress = client.execute(new GetRequest(Tree.SendAddress)).getString();
        }
        return sendAddress;
    }

    public Locale getLocale() throws OXException, IOException, JSONException {
        if (null == locale) {
            final String localeId = client.execute(new GetRequest(Tree.Language)).getString();
            locale = LocaleTools.getLocale(localeId);
        }
        return locale;
    }

    public int getPrivateAppointmentFolder() throws OXException, IOException, JSONException {
        if (-1 == privateAppointmentFolder) {
            initPrivateFolders();
        }
        return privateAppointmentFolder;
    }

    public int getPrivateContactFolder() throws OXException, IOException, JSONException {
        if (-1 == privateContactFolder) {
            initPrivateFolders();
        }
        return privateContactFolder;
    }

    public int getPrivateTaskFolder() throws OXException, IOException, JSONException {
        if (-1 == privateTaskFolder) {
            initPrivateFolders();
        }
        return privateTaskFolder;
    }

    private void initPrivateFolders() throws OXException, IOException, JSONException {
        GetResponse configGetResponse = client.execute(new GetRequest(Tree.PrivateFolders));
        JSONObject jsonObject = configGetResponse.getJSON();
        assertNotNull(jsonObject);
        privateAppointmentFolder = Integer.parseInt(jsonObject.getString("calendar"));
        privateContactFolder = Integer.parseInt(jsonObject.getString("contacts"));
        privateTaskFolder = Integer.parseInt(jsonObject.getString("tasks"));
        privateInfostoreFolder = Integer.valueOf(jsonObject.getString("infostore"));
    }

    public Date getServerTime() throws OXException, IOException, JSONException {
        long serverTime = client.execute(new GetRequest(Tree.CurrentTime)).getLong();
        serverTime -= getTimeZone().getOffset(serverTime);
        return new Date(serverTime);
    }

    public TimeZone getTimeZone() throws OXException, IOException, JSONException {
        if (null == timeZone) {
            final String tzId = client.execute(new GetRequest(Tree.TimeZone)).getString();
            timeZone = TimeZone.getTimeZone(tzId);
        }
        return timeZone;
    }

    public void setTimeZone(final TimeZone timeZone) throws OXException, IOException, JSONException {
        client.execute(new SetRequest(Tree.TimeZone, timeZone.getID()));
    }

    public int getUserId() throws OXException, IOException, JSONException {
        if (-1 == userId) {
            userId = client.execute(new GetRequest(Tree.Identifier)).getInteger();
        }
        return userId;
    }

    public int getContextId() throws OXException, IOException, JSONException {
        if (-1 == contextId) {
            contextId = client.execute(new GetRequest(Tree.ContextID)).getInteger();
        }
        return contextId;
    }

    public String getDefaultAddress() throws OXException, IOException, JSONException {
        if (null == defaultAddress) {
            defaultAddress = client.execute(new GetRequest(Tree.DefaultAddress)).getString();
        }
        return defaultAddress;
    }
}
