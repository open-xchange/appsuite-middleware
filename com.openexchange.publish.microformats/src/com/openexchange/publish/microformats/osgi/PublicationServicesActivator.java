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


package com.openexchange.publish.microformats.osgi;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.publish.PublicationService;
import com.openexchange.publish.microformats.ContactPictureServlet;
import com.openexchange.publish.microformats.FormStrings;
import com.openexchange.publish.microformats.InfostoreFileServlet;
import com.openexchange.publish.microformats.MicroformatServlet;
import com.openexchange.publish.microformats.OXMFPublicationService;
import com.openexchange.publish.microformats.tools.ContactTemplateUtils;
import com.openexchange.publish.microformats.tools.InfostoreTemplateUtils;
import com.openexchange.templating.TemplateService;

public class PublicationServicesActivator extends HousekeepingActivator {

    private volatile OXMFPublicationService contactPublisher;
    private volatile OXMFPublicationService infostorePublisher;

    /**
     * Initializes a new {@link PublicationServicesActivator}.
     */
    public PublicationServicesActivator() {
        super();
    }

    @Override
    public void startBundle() throws Exception {
        Services.setServiceLookup(this);

        trackService(HostnameService.class);

        final OXMFPublicationService contactPublisher = new OXMFPublicationService();
        this.contactPublisher = contactPublisher;
        contactPublisher.setFolderType("contacts");
        contactPublisher.setRootURL("/publications/contacts");
        contactPublisher.setTargetDisplayName(FormStrings.TARGET_NAME_CONTACTS);
        contactPublisher.setTargetId("com.openexchange.publish.microformats.contacts.online");
        contactPublisher.setDefaultTemplateName("contacts.tmpl");

        final Map<String, Object> additionalVars = new HashMap<String, Object>(1);
        additionalVars.put("utils", new ContactTemplateUtils());

        MicroformatServlet.registerType("contacts", contactPublisher,additionalVars);
        ContactPictureServlet.setContactPublisher(contactPublisher);

        registerService(PublicationService.class, contactPublisher, null);

        final OXMFPublicationService infostorePublisher = new OXMFPublicationService();
        this.infostorePublisher = infostorePublisher;
        infostorePublisher.setFolderType("infostore");
        infostorePublisher.setRootURL("/publications/infostore");
        infostorePublisher.setTargetDisplayName(FormStrings.TARGET_NAME_INFOSTORE);
        infostorePublisher.setTargetId("com.openexchange.publish.microformats.infostore.online");
        infostorePublisher.setDefaultTemplateName("infostore.tmpl");
        InfostoreFileServlet.setInfostorePublisher(infostorePublisher);

        final HashMap<String, Object> infoAdditionalVars = new HashMap<String, Object>();
        infoAdditionalVars.put("utils", new InfostoreTemplateUtils());

        MicroformatServlet.registerType("infostore", infostorePublisher, infoAdditionalVars);

        registerService(PublicationService.class, infostorePublisher, null);

    }

    @Override
    public void stopBundle() throws Exception {
        super.stopBundle();
        Services.setServiceLookup(null);
    }

    /**
     * Applies given template service.
     *
     * @param templateService The template service
     */
    public void setTemplateService(final TemplateService templateService) {
        infostorePublisher.setTemplateService(templateService);
        contactPublisher.setTemplateService(templateService);
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return null;
    }

}
