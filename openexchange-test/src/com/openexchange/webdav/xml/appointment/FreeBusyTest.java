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

package com.openexchange.webdav.xml.appointment;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.xml.sax.SAXException;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.appointment.recurrence.ManagedAppointmentTest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;



/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class FreeBusyTest extends ManagedAppointmentTest {

    private Appointment appointment;
    private Date now;
    private Date inAnHour;
    private UserValues values;
    private int contextId;
    private final DateFormat formatter = new SimpleDateFormat("yyyyMMdd"); //used by freebusy.java

    public FreeBusyTest(String name) {
        super(name);
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        values = getClient().getValues();

        contextId = getContextID(getClient());

        appointment = new Appointment();
        now = new Date();
        inAnHour = new Date( now.getTime() + 1000 * 60 * 60);
        appointment.setStartDate( now );
        appointment.setEndDate( inAnHour );
        appointment.setParentFolderID(folder.getObjectID());
    }



    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }


    public void testReserved() throws Exception{
        checkFreeBusy(appointment, Appointment.RESERVED);
    }

    public void testTemporary() throws Exception{
        checkFreeBusy(appointment, Appointment.TEMPORARY);
    }

    public void testAbsentOnBusiness() throws Exception{
        checkFreeBusy(appointment, Appointment.ABSENT);
    }

    public void testFree() throws Exception{
        checkFreeBusy(appointment, Appointment.FREE);
    }


    private void checkFreeBusy(Appointment app, int expectedState) throws OXException, Exception{
        app.setShownAs( expectedState );
        calendarManager.insert(appointment);
        int actualState = getFreeBusyState(app);
        assertEquals("Wrong free/busy state", expectedState, actualState);
    }


    private int getFreeBusyState(Appointment app) throws IOException, SAXException, OXException, JSONException, ParseException {
        String[] address = values.getDefaultAddress().split("@");
        assertEquals("Default address ("+values.getDefaultAddress()+") should contain one @", 2, address.length);

        List<FreeBusyInformation> freeBusyStates = getFreeBusyState(getSecondWebConversation(), address[0], address[1], app.getStartDate());
        for(FreeBusyInformation info : freeBusyStates){
            if(Math.abs(info.start.getTime() - now.getTime()) < 2000
            && Math.abs(info.end.getTime() - inAnHour.getTime()) < 2000) {
                return info.type;
            }
        }
        return -1;
    }

    protected List<FreeBusyInformation> getFreeBusyState(final WebConversation webCon, String username, String mailserver, Date start) throws IOException, SAXException, ParseException {
        String startFreebusy = formatter .format(start);
        String endFreebusy = formatter.format( new Date( start.getTime() + 24*60*60*1000)); //todo: May fail if appointment is > 1day
        String url = "http://"+getHostName()+"/servlet/webdav.freebusy?contextid="+contextId+"&username="+username+"&server="+mailserver+"&start="+startFreebusy+"&end="+endFreebusy;
        WebRequest request = new GetMethodWebRequest(url);
        WebResponse response = webCon.getResponse(request);
        List<FreeBusyInformation> states = parseFreeBusyResponse(response);
        return states;
    }


    private List<FreeBusyInformation> parseFreeBusyResponse(WebResponse response) throws IOException, ParseException {
        List<FreeBusyInformation> result = new LinkedList<FreeBusyInformation>();
        //FREEBUSY;FBTYPE=BUSY-UNAVAILABLE:20100629T104851Z/20100629T114851Z
        String text = response.getText();
        String[] lines = text.split("\n");
        for(String line: lines){
            if(line.startsWith("FREEBUSY")){
                Pattern pattern = Pattern.compile("FBTYPE=(.+)?:(.+)?/(.+)");
                Matcher matcher = pattern.matcher(line);
                matcher.find();
                assertEquals("Must find three groups."+System.getProperty("line.separator")+text, 3, matcher.groupCount());
                String type = matcher.group(1);
                String start = matcher.group(2);
                String end = matcher.group(3);
                result.add(new FreeBusyInformation(type, start, end));
            }

        }
        // System.out.println(text);
        return result;
    }


    protected int getContextID(AJAXClient client) throws IOException, SAXException {
        String url = "http://"+getHostName()+"/ajax/config/context_id?session="+client.getSession().getId();
        WebRequest request = new GetMethodWebRequest(url);
        WebResponse response = client.getSession().getConversation().getResponse(request);
        String text = response.getText();
        String sub = text.substring(8, text.length()-1); //TODO: exchange ugly hack for JSON parser
        return Integer.parseInt(sub);
    }
}
