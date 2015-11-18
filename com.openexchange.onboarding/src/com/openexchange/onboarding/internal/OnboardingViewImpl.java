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

package com.openexchange.onboarding.internal;

import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.onboarding.Device;
import com.openexchange.onboarding.EntityPath;
import com.openexchange.onboarding.Module;
import com.openexchange.onboarding.OnboardingConfiguration;
import com.openexchange.onboarding.OnboardingSelection;
import com.openexchange.onboarding.Platform;
import com.openexchange.onboarding.service.OnboardingView;
import com.openexchange.session.Session;

/**
 * {@link OnboardingViewImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class OnboardingViewImpl implements OnboardingView {

    private final EnumSet<Platform> platforms;
    private final EnumSet<Device> devices;
    private final EnumSet<Module> modules;
    private final List<OnboardingSelection> selections;

    /**
     * Initializes a new {@link OnboardingViewImpl}.
     */
    public OnboardingViewImpl() {
        super();
        platforms = EnumSet.noneOf(Platform.class);
        devices = EnumSet.noneOf(Device.class);
        modules = EnumSet.noneOf(Module.class);
        selections = new LinkedList<OnboardingSelection>();
    }

    /**
     * Adds the specified on-boarding configurations to this view
     *
     * @param configurations The configurations to add
     * @param session The session providing user data
     * @throws OXException If adding to this view fails
     */
    public void add(Collection<OnboardingConfiguration> configurations, Session session) throws OXException {
        for (OnboardingConfiguration configuration : configurations) {
            add(configuration, session);
        }
    }

    /**
     * Adds the specified on-boarding configuration to this view
     *
     * @param configuration The configuration to add
     * @param session The session providing user data
     * @throws OXException If adding to this view fails
     */
    public void add(OnboardingConfiguration configuration, Session session) throws OXException {
        List<EntityPath> entityPaths = configuration.getEntityPaths(session);
        for (EntityPath entityPath : entityPaths) {
            platforms.add(entityPath.getPlatform());
            devices.add(entityPath.getDevice());
            modules.add(entityPath.getModule());
            selections.addAll(configuration.getSelections(entityPath, session));
        }
    }

    @Override
    public EnumSet<Platform> getPlatforms() {
        return platforms;
    }

    @Override
    public EnumSet<Device> getDevices() {
        return devices;
    }

    @Override
    public EnumSet<Module> getModules() {
        return modules;
    }

    @Override
    public List<OnboardingSelection> getSelections() {
        return selections;
    }

}
