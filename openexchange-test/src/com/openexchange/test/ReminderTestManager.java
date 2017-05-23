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

package com.openexchange.test;

import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.ReminderFields;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.CommonDeleteResponse;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.reminder.actions.DeleteRequest;
import com.openexchange.ajax.reminder.actions.UpdatesRequest;
import com.openexchange.ajax.reminder.actions.UpdatesResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.reminder.ReminderObject;

/**
 * 
 * {@link ReminderTestManager}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public class ReminderTestManager implements TestManager {

    private AJAXClient client;

    private final List<ReminderObject> createdEntities = new ArrayList<ReminderObject>();

    private TimeZone timezone;

    private AbstractAJAXResponse lastResponse;

    private boolean failOnError;

    private Exception lastException;

    private Date lastModification;

    public TimeZone getTimezone() {
        return timezone;
    }

    public void setTimezone(TimeZone timezone) {
        this.timezone = timezone;
    }

    public ReminderTestManager(AJAXClient client) {
        this.setClient(client);

        try {
            timezone = client.getValues().getTimeZone();
        } catch (OXException e) {
            // wait for finally block
        } catch (IOException e) {
            // wait for finally block
        } catch (JSONException e) {
            // wait for finally block
        } finally {
            if (timezone == null) {
                timezone = TimeZone.getTimeZone("Europe/Berlin");
            }
        }
    }

    public void setClient(AJAXClient client) {
        this.client = client;
    }

    public AJAXClient getClient() {
        return client;
    }

    public void setLastResponse(AbstractAJAXResponse lastResponse) {
        this.lastResponse = lastResponse;
    }

    @Override
    public AbstractAJAXResponse getLastResponse() {
        return lastResponse;
    }

    @Override
    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    @Override
    public boolean getFailOnError() {
        return failOnError;
    }

    @Override
    public boolean doesFailOnError() {
        return getFailOnError();
    }

    public void setLastException(Exception lastException) {
        lastException.printStackTrace();
        this.lastException = lastException;
    }

    @Override
    public Exception getLastException() {
        return lastException;
    }

    @Override
    public boolean hasLastException() {
        return lastException != null;
    }

    public void setLastModification(Date lastModification) {
        this.lastModification = lastModification;
    }

    public Date getLastModification() {
        return lastModification;
    }

    @Override
    public void cleanUp() {
        for (ReminderObject reminder : new ArrayList<ReminderObject>(createdEntities)) {
            delete(reminder);
            if (getLastResponse().hasError()) {
                org.slf4j.LoggerFactory.getLogger(ReminderTestManager.class).warn("Unable to delete the reminder with id {} in folder '{}': {}", reminder.getObjectId(), reminder.getFolder(), getLastResponse().getException().getMessage());
            }

        }
    }

    public void delete(ReminderObject reminder) {
        DeleteRequest request = new DeleteRequest(reminder, false);
        CommonDeleteResponse deleteResponse = execute(request);
        setLastResponse(deleteResponse);
    }

    private <T extends AbstractAJAXResponse> T execute(final AJAXRequest<T> request) {
        try {
            return getClient().execute(request);
        } catch (OXException e) {
            setLastException(e);
            if (failOnError) {
                fail("AjaxException during task creation: " + e.getLocalizedMessage());
            }
        } catch (IOException e) {
            setLastException(e);
            if (failOnError) {
                fail("IOException during task creation: " + e.getLocalizedMessage());
            }
        } catch (JSONException e) {
            setLastException(e);
            if (failOnError) {
                fail("JsonException during task creation: " + e.getLocalizedMessage());
            }
        }
        return null;
    }

    public List<ReminderObject> updates(Date timestamp) throws JSONException, OXException, IOException {
        UpdatesRequest request = new UpdatesRequest(timestamp);
        UpdatesResponse updatesResponse = execute(request);
        setLastResponse(updatesResponse);
        
        final JSONArray jsonArray = (JSONArray)updatesResponse.getData();
        List<ReminderObject> reminder = new ArrayList<>(jsonArray.length());
        for (int a = 0; a < jsonArray.length(); a++) {
            ReminderObject object = new ReminderObject();
            final JSONObject jsonReminder = jsonArray.getJSONObject(a);

            object.setObjectId(DataParser.parseInt(jsonReminder, ReminderFields.ID));
            object.setTargetId(DataParser.parseInt(jsonReminder, ReminderFields.TARGET_ID));
            object.setFolder(DataParser.parseInt(jsonReminder, ReminderFields.FOLDER));
            object.setDate(DataParser.parseTime(jsonReminder, ReminderFields.ALARM, getClient().getValues().getTimeZone()));
            object.setLastModified(DataParser.parseDate(jsonReminder, ReminderFields.LAST_MODIFIED));
            object.setUser(DataParser.parseInt(jsonReminder, ReminderFields.USER_ID));
            object.setRecurrenceAppointment(DataParser.parseBoolean(jsonReminder, ReminderFields.RECURRENCE_APPOINTMENT));
            reminder.add(object);
        }
        return reminder;
    }

}
