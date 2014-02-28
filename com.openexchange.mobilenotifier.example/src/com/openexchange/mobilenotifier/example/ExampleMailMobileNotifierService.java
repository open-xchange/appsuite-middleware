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
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.mobilenotifier.AbstractMobileNotifierService;
import com.openexchange.mobilenotifier.MobileNotifierProviders;
import com.openexchange.mobilenotifier.NotifyItem;
import com.openexchange.mobilenotifier.NotifyTemplate;
import com.openexchange.mobilenotifier.utility.MobileNotifierFileUtility;
import com.openexchange.session.Session;

/**
 * {@link ExampleMailMobileNotifierService} - Example mail implementation of a mobile notifier service
 * 
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 */
public class ExampleMailMobileNotifierService extends AbstractMobileNotifierService {

    public ExampleMailMobileNotifierService() {
        super();
    }

    @Override
    public String getProviderName() {
        return MobileNotifierProviders.MAIL.getProviderName();
    }

    @Override
    public String getFrontendName() {
        return MobileNotifierProviders.MAIL.getFrontendName();
    }

    @Override
    public List<List<NotifyItem>> getItems(final Session session) throws OXException {
        List<NotifyItem> item1 = new ArrayList<NotifyItem>();
        item1.add(new NotifyItem("from", "heinrich@example.com"));
        item1.add(new NotifyItem("subject", "a subject"));
        item1.add(new NotifyItem("received_date", "12.04.2013 - 12:45:00"));
        item1.add(new NotifyItem("flags", "flag"));
        item1.add(new NotifyItem("attachements", "an attachment"));
        List<NotifyItem> item2 = new ArrayList<NotifyItem>();
        item2.add(new NotifyItem("from", "anotherheinrich@example.com"));
        item2.add(new NotifyItem("subject", "another subject"));
        item2.add(new NotifyItem("received_date", "18.04.2013 - 12:45:00"));
        item2.add(new NotifyItem("flags", "another flag"));
        item2.add(new NotifyItem("attachements", "another attachment"));

        List<List<NotifyItem>> notifyItems = new ArrayList<List<NotifyItem>>();
        notifyItems.add(item1);
        notifyItems.add(item2);
        return notifyItems;
    }

    @Override
    public NotifyTemplate getTemplate() throws OXException {
        final String fileName = MobileNotifierProviders.MAIL.getTemplateFileName();
        final String htmlTemplate = MobileNotifierFileUtility.getTemplateFileContent(fileName);
        final String title = MobileNotifierProviders.MAIL.getTitle();
        int index = 1;

        return new NotifyTemplate(title, htmlTemplate, false, index);
    }

    @Override
    public void putTemplate(String changedTemplate) throws OXException {
        MobileNotifierFileUtility.writeTemplateFileContent(MobileNotifierProviders.MAIL.getTemplateFileName(), changedTemplate);
    }
}
