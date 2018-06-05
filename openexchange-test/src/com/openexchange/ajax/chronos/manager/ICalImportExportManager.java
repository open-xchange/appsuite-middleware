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

package com.openexchange.ajax.chronos.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

    public String importICalFile(String session, String folder, File file, boolean suppressNotification, boolean ignoreUIDs) throws Exception {
        return importApi.importICal(session, folder, file, suppressNotification, ignoreUIDs, true);
    }

    public String exportICalFile(String session, String folder) throws ApiException {
        return exportApi.exportAsICal(session, folder);
    }

    public String exportICalBatchFile(String session, List<InfoItemExport> body) throws ApiException {
        return exportApi.exportAsICal_0(session, body);
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
            } catch (JSONException e) {
                return Collections.emptyList();
            }
        }
        return eventIds;
    }

}
