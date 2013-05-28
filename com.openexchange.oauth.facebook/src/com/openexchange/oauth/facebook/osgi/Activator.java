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

package com.openexchange.oauth.facebook.osgi;

import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import com.openexchange.capabilities.CapabilityChecker;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.http.deferrer.DeferringURLService;
import com.openexchange.oauth.OAuthService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;


/**
 * {@link Activator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public final class Activator extends HousekeepingActivator {

    public Activator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[]{CapabilityService.class, ConfigViewFactory.class};
    }

    @Override
    protected void startBundle() throws Exception {
        final Filter filter = context.createFilter("(|(" + Constants.OBJECTCLASS + '=' + ConfigurationService.class.getName() + ")(" + Constants.OBJECTCLASS + '=' + OAuthService.class.getName() + ")(" + Constants.OBJECTCLASS + '=' + DeferringURLService.class.getName() + "))");
        FacebookRegisterer fbRegisterer = new FacebookRegisterer(context);
        track(filter, fbRegisterer);
        openTrackers();
        registerService(CapabilityChecker.class, new CapabilityChecker() {

            @Override
            public boolean isEnabled(String capability, Session ses) throws OXException {
                if ("facebook".equals(capability)) {
                    final ServerSession session = ServerSessionAdapter.valueOf(ses);
                    if (session.isAnonymous()) {
                        return false;
                    }
                    final ConfigViewFactory factory = getService(ConfigViewFactory.class);
                    final ConfigView view = factory.getView(session.getUserId(), session.getContextId());
                    return view.opt("com.openexchange.oauth.facebook", boolean.class, Boolean.TRUE).booleanValue();
                }
                return true;

            }
        });
        getService(CapabilityService.class).declareCapability("facebook");

    }

    @Override
    protected void stopBundle() {
        closeTrackers();
        cleanUp();
    }

}
