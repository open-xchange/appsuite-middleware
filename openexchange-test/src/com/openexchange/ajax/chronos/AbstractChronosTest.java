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

package com.openexchange.ajax.chronos;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.ajax.framework.AbstractAPIClientSession;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.TimeZones;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.models.DateTimeData;
import com.openexchange.testing.httpclient.models.EventId;
import com.openexchange.testing.httpclient.models.FoldersVisibilityResponse;
import com.openexchange.testing.httpclient.models.LoginResponse;
import com.openexchange.testing.httpclient.modules.ChronosApi;
import com.openexchange.testing.httpclient.modules.FoldersApi;

/**
 * {@link AbstractChronosTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class AbstractChronosTest extends AbstractAPIClientSession {

    protected SimpleDateFormat BASIC_FORMATER = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
    protected SimpleDateFormat ZULU_FORMATER = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");

    protected ChronosApi api;
    private FoldersApi foldersApi;
    protected String session;
    List<EventId> eventIds;
    protected int calUser;
    private long lastTimeStamp;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        api = new ChronosApi(getClient());
        foldersApi = new FoldersApi(getClient());
        LoginResponse login = login(this.testUser, this.apiClient);
        calUser = login.getUserId();
        session = login.getSession();
    }

    protected void rememberEventId(EventId eventId) {
        if (eventIds == null) {
            eventIds = new ArrayList<>();
        }
        eventIds.add(eventId);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if (eventIds != null) {
            api.deleteEvent(session, System.currentTimeMillis(), eventIds);
        }
    }

    @SuppressWarnings("unchecked")
    protected String getDefaultFolder() throws Exception {
        FoldersVisibilityResponse visibleFolders = foldersApi.getVisibleFolders(session, "event", "1,308", "0");
        if (visibleFolders.getError() != null) {
            throw new OXException(new Exception(visibleFolders.getErrorDesc()));
        }

        Object privateFolders = visibleFolders.getData().getPrivate();
        ArrayList<ArrayList<?>> privateList = (ArrayList<ArrayList<?>>) privateFolders;
        if (privateList.size() == 1) {
            return (String) privateList.get(0).get(0);
        } else {
            for (ArrayList<?> folder : privateList) {
                if ((boolean) folder.get(1)) {
                    return (String) folder.get(0);
                }
            }
        }
        throw new Exception("Unable to find default calendar folder!");
    }

    @SuppressWarnings("unchecked")
    protected String getDefaultFolder(String session, ApiClient client) throws Exception {
        FoldersVisibilityResponse visibleFolders = new FoldersApi(client).getVisibleFolders(session, "event", "1,308", "0");
        if (visibleFolders.getError() != null) {
            throw new OXException(new Exception(visibleFolders.getErrorDesc()));
        }

        Object privateFolders = visibleFolders.getData().getPrivate();
        ArrayList<ArrayList<?>> privateList = (ArrayList<ArrayList<?>>) privateFolders;
        if (privateList.size() == 1) {
            return (String) privateList.get(0).get(0);
        } else {
            for (ArrayList<?> folder : privateList) {
                if ((boolean) folder.get(1)) {
                    return (String) folder.get(0);
                }
            }
        }
        throw new Exception("Unable to find default calendar folder!");
    }

    protected Date getAPIDate(TimeZone localtimeZone, Date localDate, int datesToAdd) {
        Calendar localCalendar = GregorianCalendar.getInstance(localtimeZone);
        localCalendar.setTime(localDate);
        localCalendar.add(Calendar.DAY_OF_YEAR, datesToAdd);
        Calendar utcCalendar = GregorianCalendar.getInstance(TimeZones.UTC);
        utcCalendar.set(localCalendar.get(Calendar.YEAR), localCalendar.get(Calendar.MONTH), localCalendar.get(Calendar.DATE), 0, 0, 0);
        utcCalendar.set(Calendar.MILLISECOND, 0);
        return utcCalendar.getTime();
    }

    protected DateTimeData getDateTime(long millis){
        DateTimeData result = new DateTimeData();
        result.setTzid(TimeZone.getDefault().getID());
        result.setValue(BASIC_FORMATER.format(new Date(millis)));
        return result;
    }

    protected DateTimeData getZuluTime(long millis){
        DateTimeData result = new DateTimeData();
        result.setTzid("UTC");
        result.setValue(BASIC_FORMATER.format(new Date(millis)));
        return result;
    }


    protected DateTimeData addTimeToDateTimeData(DateTimeData data, long millis) throws ParseException {
        Date date = BASIC_FORMATER.parse(data.getValue());
        return getDateTime(date.getTime()+millis);
    }

    protected Date getTime(DateTimeData time) throws ParseException {
        return BASIC_FORMATER.parse(time.getValue());
    }

    protected void setLastTimestamp(long timestamp){
    	this.lastTimeStamp=timestamp;
    }

    protected long getLastTimestamp(){
    	return lastTimeStamp;
    }
}

