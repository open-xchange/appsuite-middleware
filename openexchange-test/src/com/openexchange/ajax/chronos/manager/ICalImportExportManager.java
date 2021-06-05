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

package com.openexchange.ajax.chronos.manager;

import static org.junit.Assert.assertEquals;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.dmfs.rfc5545.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.EventId;
import com.openexchange.testing.httpclient.models.InfoItemExport;
import com.openexchange.testing.httpclient.modules.ExportApi;
import com.openexchange.testing.httpclient.modules.ImportApi;

/**
 * {@link ICalImportExportManager}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.0
 */
public class ICalImportExportManager {

    public static final String RECURRENCE_IMPORT_ICS = "ical_recurrence_import.ics";
    public static final String RECURRENCE_IMPORT_ICS_SUMMARY = "Test";
    public static final String RECURRENCE_IMPORT_ICS_UID = "15279a8a-6305-41a5-8de9-029319782b98";
    public static final String RECURRENCE_IMPORT_ICS_RECURRENCE_ID = "20171010T060000Z";

    public static final String SINGLE_IMPORT_ICS = "single_test_event.ics";
    public static final String SINGLE_IMPORT_ICS_SUMMARY = "TestSingle";
    public static final String SINGLE_IMPORT_ICS_UID = "e316a2a4-83bf-441f-ad2d-9622e3210772";

    public static final String SERIES_IMPORT_ICS = "test_series.ics";
    public static final String SERIES_IMPORT_ICS_SUMMARY = "TestSeries";
    public static final String SERIES_IMPORT_ICS_UID = "fbbd81e8-4a81-4092-bc9b-7e3a5cbb5861";

    public static final String FLOATING_ICS = "MWB-2.ics";
    public static final String FLOATING_ICS_SUMMARY = "Flight to Berlin-Tegel";
    public static final String FLOATING_ICS_UID = "12345abcdef_CGNTXL";

    private final ExportApi exportApi;
    private final ImportApi importApi;

    /**
     * Initializes a new {@link ICalImportExportManager}.
     */
    public ICalImportExportManager(ExportApi exportApi, ImportApi importApi) {
        super();
        this.exportApi = exportApi;
        this.importApi = importApi;
    }

    public String importICalFile(String folder, File file, Boolean suppressNotification, Boolean ignoreUIDs) throws Exception {
        return importApi.importICal(folder, file, Boolean.FALSE, suppressNotification, ignoreUIDs, Boolean.TRUE);
    }

    public String exportICalFile(String folder) throws ApiException {
        return exportApi.exportAsICalGetReq(folder);
    }

    public String exportICalBatchFile(List<InfoItemExport> body) throws ApiException {
        return exportApi.exportAsICal(body);
    }

    public List<EventId> parseImportJSONResponseToEventIds(String response) throws JSONException {
        JSONObject object = new JSONObject(response);
        JSONArray data = object.optJSONArray("data");
        if (data == null) {
            return Collections.emptyList();
        }
        int length = data.length();
        if (length <= 0) {
            return Collections.emptyList();
        }
        List<EventId> eventIds = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            JSONObject tuple = data.getJSONObject(i);
            try {
                String folderId = tuple.getString("folder_id");
                String objectId = tuple.getString("id");
                EventId eventId = new EventId();
                eventId.setFolder(folderId);
                eventId.setId(objectId);
                eventIds.add(eventId);
            } catch (@SuppressWarnings("unused") JSONException e) {
                return Collections.emptyList();
            }
        }
        return eventIds;
    }


    public static void assertRecurrenceID(String recurrenceId, String recurrenceId2) {
        assertEquals(CalendarUtils.decode(recurrenceId).shiftTimeZone(DateTime.UTC), CalendarUtils.decode(recurrenceId2).shiftTimeZone(DateTime.UTC));
    }

}
