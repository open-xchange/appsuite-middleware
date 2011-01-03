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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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


package com.openexchange.publish.microformats.osgi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.publish.PublicationService;
import com.openexchange.publish.microformats.ContactPictureServlet;
import com.openexchange.publish.microformats.FormStrings;
import com.openexchange.publish.microformats.InfostoreFileServlet;
import com.openexchange.publish.microformats.MicroformatServlet;
import com.openexchange.publish.microformats.OXMFPublicationService;
import com.openexchange.publish.microformats.tools.ContactTemplateUtils;
import com.openexchange.publish.microformats.tools.InfostoreTemplateUtils;
import com.openexchange.templating.TemplateService;

public class PublicationServicesActivator implements BundleActivator {

    private List<ServiceRegistration> serviceRegistrations = new ArrayList<ServiceRegistration>();

    private OXMFPublicationService contactPublisher;

    private OXMFPublicationService infostorePublisher;

    public void start(BundleContext context) throws Exception {
        contactPublisher = new OXMFPublicationService();
        contactPublisher.setFolderType("contacts");
        contactPublisher.setRootURL("/publications/contacts");
        contactPublisher.setTargetDisplayName(FormStrings.TARGET_NAME_CONTACTS);
        contactPublisher.setTargetId("com.openexchange.publish.microformats.contacts.online");
        contactPublisher.setDefaultTemplateName("contacts.tmpl");
        
        Map<String, Object> additionalVars = new HashMap<String, Object>();
        additionalVars.put("utils", new ContactTemplateUtils());
        
        MicroformatServlet.registerType("contacts", contactPublisher,additionalVars);
        ContactPictureServlet.setContactPublisher(contactPublisher);

        serviceRegistrations.add(context.registerService(PublicationService.class.getName(), contactPublisher, null));

        infostorePublisher = new OXMFPublicationService();
        infostorePublisher.setFolderType("infostore");
        infostorePublisher.setRootURL("/publications/infostore");
        infostorePublisher.setTargetDisplayName(FormStrings.TARGET_NAME_INFOSTORE);
        infostorePublisher.setTargetId("com.openexchange.publish.microformats.infostore.online");
        infostorePublisher.setDefaultTemplateName("infostore.tmpl");
        InfostoreFileServlet.setInfostorePublisher(infostorePublisher);

        HashMap<String, Object> infoAdditionalVars = new HashMap<String, Object>();
        infoAdditionalVars.put("utils", new InfostoreTemplateUtils());

        MicroformatServlet.registerType("infostore", infostorePublisher, infoAdditionalVars);

        serviceRegistrations.add(context.registerService(PublicationService.class.getName(), infostorePublisher, null));

    }

    public void stop(BundleContext context) throws Exception {
        for (ServiceRegistration registration : serviceRegistrations) {
            registration.unregister();
        }
    }
    
    public void setTemplateService(TemplateService templateService) {
        infostorePublisher.setTemplateService(templateService);
        contactPublisher.setTemplateService(templateService);
    }

}
