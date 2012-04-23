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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.subscribe.crawler;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.subscribe.crawler.internal.AbstractStep;

/**
 * {@link GoogleCalendarICalStep}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class GoogleCalendarICalStep extends AbstractStep<CalendarDataObject[], UnexpectedPage> {

    private String url;

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(GoogleCalendarICalStep.class));

    public GoogleCalendarICalStep() {
        super();
    }

    public GoogleCalendarICalStep(String description, String url) {
        this.description = description;
        this.url = url;
    }

    @Override
    public void execute(WebClient webClient) {
        ArrayList<CalendarDataObject> tempEvents = new ArrayList<CalendarDataObject>();
        ArrayList<CalendarDataObject> events = new ArrayList<CalendarDataObject>();
        try {
            Page page = webClient.getPage(url);
            byte[] bytes = page.getWebResponse().getContentAsBytes();
            // Unzip
            int BUFFER = 1024;
            // BufferedOutputStream dest = null;
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(bis));
            ZipEntry entry;
            ICalParser iCalParser = workflow.getActivator().getICalParser();
            while ((entry = zis.getNextEntry()) != null) {
                LOG.info("Extracting: " + entry);
                int count;
                byte data[] = new byte[BUFFER];
                // write the files to the disk
                ByteArrayOutputStream dest = new ByteArrayOutputStream();
                // dest = new BufferedOutputStream(fos, BUFFER);
                while ((count = zis.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
                String iCalFile = dest.toString("UTF-8");

                if (iCalParser != null) {
                    tempEvents = (ArrayList<CalendarDataObject>) iCalParser.parseAppointments(
                        iCalFile,
                        TimeZone.getDefault(),
                        new ContextImpl(23),
                        new ArrayList<ConversionError>(),
                        new ArrayList<ConversionWarning>());
                } else {
                    LOG.error("No iCal-Parser found!");
                }
                events.addAll(tempEvents);
            }
            zis.close();


        } catch (ConversionError e) {
            LOG.error(e.getMessage(), e);
        } catch (FailingHttpStatusCodeException e) {
            LOG.error(e.getMessage(), e);
        } catch (MalformedURLException e) {
            LOG.error(e.getMessage(), e);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }

        output = new CalendarDataObject[events.size()];
        for (int i = 0; i < events.size() && i < output.length; i++) {
            output[i] = events.get(i);
        }
        executedSuccessfully = true;
    }

    public String getBaseUrl() {
        return "";
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
