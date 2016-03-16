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

package com.openexchange.config.cascade.context.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigProviderService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.context.ContextConfigProvider;
import com.openexchange.config.cascade.context.ContextSetConfigProvider;
import com.openexchange.context.ContextService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link ContextConfigCascadeActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ContextConfigCascadeActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { ContextService.class, ConfigurationService.class, UserPermissionService.class, ConfigViewFactory.class };
    }

    @Override
    protected void startBundle() throws Exception {
        {
            Dictionary<String, Object> properties = new Hashtable<String, Object>(2);
            properties.put("scope", "context");
            registerService(ConfigProviderService.class, new ContextConfigProvider(this), properties);
        }

        {
            Dictionary<String, Object> properties = new Hashtable<String, Object>(2);
            properties.put("scope", "contextSets");
            registerService(ConfigProviderService.class, new ContextSetConfigProvider(this), properties);
        }
    }

}
