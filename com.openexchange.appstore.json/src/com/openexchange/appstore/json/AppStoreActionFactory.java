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

package com.openexchange.appstore.json;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.appstore.json.actions.CrawlAction;
import com.openexchange.appstore.json.actions.GetAction;
import com.openexchange.appstore.json.actions.InstallAction;
import com.openexchange.appstore.json.actions.InstalledAction;
import com.openexchange.appstore.json.actions.ManifestAction;
import com.openexchange.appstore.json.actions.ReleaseAction;
import com.openexchange.appstore.json.actions.RevokeAction;
import com.openexchange.appstore.json.actions.StatusAction;
import com.openexchange.appstore.json.actions.UninstallAction;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link AppStoreActionFactory}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class AppStoreActionFactory implements AJAXActionServiceFactory {

    private final Map<String, AJAXActionService> ACTIONS = new HashMap<String, AJAXActionService>();

    public AppStoreActionFactory(ServiceLookup services) {
        super();
        ACTIONS.put(CrawlAction.ACTION, new CrawlAction(services));
        ACTIONS.put(GetAction.ACTION, new GetAction(services));
        ACTIONS.put(InstallAction.ACTION, new InstallAction(services));
        ACTIONS.put(InstalledAction.ACTION, new InstalledAction(services));
        ACTIONS.put(ManifestAction.ACTION, new ManifestAction(services));
        ACTIONS.put(ReleaseAction.ACTION, new ReleaseAction(services));
        ACTIONS.put(RevokeAction.ACTION, new RevokeAction(services));
        ACTIONS.put(StatusAction.ACTION, new StatusAction(services));
        ACTIONS.put(UninstallAction.ACTION, new UninstallAction(services));
    }

    @Override
    public AJAXActionService createActionService(String action) throws OXException {
        return ACTIONS.get(action);
    }

}
