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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.mobilenotifier.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.mobilenotifier.AbstractMobileNotifierService;
import com.openexchange.mobilenotifier.MobileNotifierProviders;
import com.openexchange.mobilenotifier.NotifyItem;
import com.openexchange.mobilenotifier.NotifyTemplate;
import com.openexchange.mobilenotifier.utility.MobileNotifierFileUtility;
import com.openexchange.session.Session;

/**
 * {@link ExampleCalendarMobileNotifierService} - Example calendar implementation of a mobile notifier service
 * 
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class ExampleCalendarMobileNotifierService extends AbstractMobileNotifierService {
    public ExampleCalendarMobileNotifierService() {
        super();
    }

    @Override
    public String getProviderName() {
        return MobileNotifierProviders.APPOINTMENT.getProviderName();
    }

    @Override
    public String getFrontendName() {
        return MobileNotifierProviders.APPOINTMENT.getFrontendName();
    }

    // public List<List<NotifyItem>> ?
    @Override
    public List<List<NotifyItem>> getItems(final Session session) throws OXException {
        List<NotifyItem> item1 = new ArrayList<NotifyItem>();
        item1.add(new NotifyItem("title", "This is a test title1"));
        item1.add(new NotifyItem("location", "This is a test location1"));
        item1.add(new NotifyItem("description", "This is a test description1"));
        List<NotifyItem> item2 = new ArrayList<NotifyItem>();
        item2.add(new NotifyItem("title", "This is a test title2"));
        item2.add(new NotifyItem("location", "This is a test location2"));
        item2.add(new NotifyItem("description", "This is a test description2"));
        List<List<NotifyItem>> notifyItems = new ArrayList<List<NotifyItem>>();
        notifyItems.add(item1);
        notifyItems.add(item2);
        return notifyItems;
    }

    @Override
    public NotifyTemplate getTemplate() throws OXException {
        // main attributes
        final String fileName = MobileNotifierProviders.APPOINTMENT.getTemplateFileName();
        final String title = MobileNotifierProviders.APPOINTMENT.getTitle();
        final String htmlTemplate = MobileNotifierFileUtility.getTemplateFileContent(fileName);
        int index = 2;
        // additional attribute
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("attribute", "a value");
        attributes.put("isAnotherAttribute", new Boolean(true));
        NotifyTemplate nt = new NotifyTemplate(title, htmlTemplate, true, index);
        nt.setAttributes(attributes);
        return nt;
    }

    @Override
    public void putTemplate(String changedTemplate) throws OXException {
        MobileNotifierFileUtility.writeTemplateFileContent(MobileNotifierProviders.APPOINTMENT.getTemplateFileName(), changedTemplate);
    }
}
