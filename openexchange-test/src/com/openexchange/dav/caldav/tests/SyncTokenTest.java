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

package com.openexchange.dav.caldav.tests;

import java.util.Calendar;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.ReportMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.junit.Test;
import com.openexchange.dav.Config;
import com.openexchange.dav.PropertyNames;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.caldav.CalDAVTest;
import com.openexchange.dav.reports.SyncCollectionReportInfo;

/**
 * {@link SyncTokenTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.2
 */
public class SyncTokenTest extends CalDAVTest {

    @Test
    public void testEmptySyncToken() throws Exception {
        syncCollection("", StatusCodes.SC_MULTISTATUS);
    }

    @Test
    public void testNoSyncToken() throws Exception {
        syncCollection(null, StatusCodes.SC_MULTISTATUS);
    }

    @Test
    public void testRegularSyncToken() throws Exception {
        syncCollection(String.valueOf(System.currentTimeMillis()), StatusCodes.SC_MULTISTATUS);
    }

    @Test
    public void testOutdatedSyncToken() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -2);
        syncCollection(String.valueOf(calendar.getTimeInMillis()), StatusCodes.SC_FORBIDDEN);
    }

    @Test
    public void testMalformedSyncToken() throws Exception {
        syncCollection("wurstpeter", StatusCodes.SC_FORBIDDEN);
    }

    @Test
    public void testOutdatedTruncatedSyncToken() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -2);
        String token = calendar.getTimeInMillis() + ".4";
        syncCollection(token, StatusCodes.SC_MULTISTATUS);
    }

    protected MultiStatusResponse[] syncCollection(String syncToken, int expectedResponse) throws Exception {
        String uri = getBaseUri() + Config.getPathPrefix() + "/caldav/" + encodeFolderID(getDefaultFolderID());
        DavPropertyNameSet props = new DavPropertyNameSet();
        props.add(PropertyNames.GETETAG);
        ReportInfo reportInfo = new SyncCollectionReportInfo(syncToken, props);
        ReportMethod report = null;
        try {
            report = new ReportMethod(uri, reportInfo);
            return getWebDAVClient().doReport(report, expectedResponse);
        } finally {
            release(report);
        }
    }

}
