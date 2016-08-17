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

package com.openexchange.calendar.printing.osgi;

import static com.openexchange.calendar.printing.CPServiceRegistry.getInstance;
import com.openexchange.ajax.osgi.AbstractSessionServletActivator;
import com.openexchange.calendar.printing.CPServlet;
import com.openexchange.calendar.printing.preferences.CalendarPrintingEnabled;
import com.openexchange.calendar.printing.templating.CalendarTemplateHelperFactory;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.html.HtmlService;
import com.openexchange.i18n.I18nService;
import com.openexchange.osgi.RegistryCustomizer;
import com.openexchange.resource.ResourceService;
import com.openexchange.ajax.printing.TemplateHelperFactory;
import com.openexchange.templating.TemplateService;
import com.openexchange.user.UserService;

/**
 * {@link CalendarPrintingActivator} - The activator for calendar printing.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CalendarPrintingActivator extends AbstractSessionServletActivator {

    @Override
    protected void handleAvailability(final Class<?> clazz) {
        register();
    }

    @Override
    protected void startBundle() {
        track(I18nService.class, new I18nCustomizer(context));
        track(UserService.class, new RegistryCustomizer<UserService>(context, UserService.class, getInstance()));
        track(GroupService.class, new RegistryCustomizer<GroupService>(context, GroupService.class, getInstance()));
        track(ConfigViewFactory.class, new RegistryCustomizer<ConfigViewFactory>(context, ConfigViewFactory.class, getInstance()));
        openTrackers();
        register();
        registerService(PreferencesItemService.class, new CalendarPrintingEnabled());
        registerService(TemplateHelperFactory.class, new CalendarTemplateHelperFactory(this));

        final CapabilityService capabilityService = getService(CapabilityService.class);
        capabilityService.declareCapability("calendar-printing");
    }

    @Override
    protected void stopBundle() throws Exception {
        try {
            super.stopBundle();
        } finally {
            CPServlet.setServiceLookup(null);
        }
    }

    private void register() {
        final TemplateService templates = getService(TemplateService.class);
        final AppointmentSqlFactoryService appointmentSqlFactory = getService(AppointmentSqlFactoryService.class);
        final CalendarCollectionService collectionService = getService(CalendarCollectionService.class);

        if (templates == null || appointmentSqlFactory == null || collectionService == null) {
            return;
        }

        CPServlet.setServiceLookup(this);

        registerSessionServlet(getService(DispatcherPrefixService.class).getPrefix()+"printCalendar", new CPServlet());
    }

    @Override
    protected Class<?>[] getAdditionalNeededServices() {
        return new Class<?>[] { TemplateService.class, AppointmentSqlFactoryService.class, CalendarCollectionService.class, DispatcherPrefixService.class, HtmlService.class, UserService.class, ResourceService.class };
    }
}
