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

package com.openexchange.freebusy.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.freebusy.FreeBusyData;
import com.openexchange.freebusy.FreeBusyExceptionCodes;
import com.openexchange.freebusy.FreeBusyService;
import com.openexchange.freebusy.osgi.FreeBusyProviderListener;
import com.openexchange.freebusy.provider.FreeBusyProvider;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.session.Session;
import com.openexchange.userconf.UserConfigurationService;

/**
 * {@link FreeBusyServiceImpl}
 * 
 * Default free/busy service implementation.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FreeBusyServiceImpl implements FreeBusyService {
    
    private final FreeBusyProviderListener providers;
    
    public FreeBusyServiceImpl(FreeBusyProviderListener providers) {
        super();
        this.providers = providers;
    }
    
    private void checkProvidersAvailable() throws OXException {
        if (null == providers || null == providers.getProviders() || 0 == providers.getProviders().size()) {
            throw FreeBusyExceptionCodes.NO_PROVIDERS_AVAILABLE.create();
        }   
    }

    private void checkFreeBusyEnabled(Session session) throws OXException {
        Context context = FreeBusyServiceLookup.getService(ContextService.class).getContext(session.getContextId());
        UserConfiguration userConfig = FreeBusyServiceLookup.getService(UserConfigurationService.class).getUserConfiguration(
            session.getUserId(), context);
        if (false == userConfig.hasFreeBusy()) {
            throw FreeBusyExceptionCodes.FREEBUSY_NOT_ENABLED.create(session.getUserId(), session.getContextId());
        }
    }

    @Override
    public List<FreeBusyData> getFreeBusy(Session session, List<String> participants, Date from, Date until) throws OXException {
        checkFreeBusyEnabled(session);
        checkProvidersAvailable();
        if (1 == providers.getProviders().size()) {
            return providers.getProviders().get(0).getFreeBusy(session, participants, from, until);
        } else {
            Map<String, FreeBusyData> freeBusyData = new HashMap<String, FreeBusyData>();
            for (FreeBusyProvider provider : providers.getProviders()) {
                for (FreeBusyData providerData : provider.getFreeBusy(session, participants, from, until)) {
                    if (null != providerData) {
                        FreeBusyData data = freeBusyData.get(providerData.getParticipant());
                        if (null == data) {
                            // replace
                            freeBusyData.put(providerData.getParticipant(), providerData);                            
                        } else {
                            // add
                            data.add(providerData);                           
                        }
                    }
                }                
            }
            return new ArrayList<FreeBusyData>(freeBusyData.values());
        }                
    }

    @Override
    public FreeBusyData getFreeBusy(Session session, String participant, Date from, Date until) throws OXException {
        checkFreeBusyEnabled(session);
        checkProvidersAvailable();
        if (1 == providers.getProviders().size()) {
            return providers.getProviders().get(0).getFreeBusy(session, participant, from, until);
        } else {
            FreeBusyData freeBusyData = null;
            for (FreeBusyProvider provider : providers.getProviders()) {
                if (null == freeBusyData || freeBusyData.hasWarnings()) {
                    freeBusyData = provider.getFreeBusy(session, participant, from, until);
                } else {
                    FreeBusyData data = provider.getFreeBusy(session, participant, from, until);
                    if (null != data) {
                        freeBusyData.addAll(data.getIntervals());
                    }
                }                
            }
            return freeBusyData;
        }
    }

    @Override
    public List<FreeBusyData> getMergedFreeBusy(Session session, List<String> participants, Date from, Date until) throws OXException {
        List<FreeBusyData> freeBusyData = this.getFreeBusy(session, participants, from, until);
        for (FreeBusyData data : freeBusyData) {
            data.normalize();
        }
        return freeBusyData;
    }

    @Override
    public FreeBusyData getMergedFreeBusy(Session session, String participant, Date from, Date until) throws OXException {
        FreeBusyData freeBusyData = this.getFreeBusy(session, participant, from, until);
        freeBusyData.normalize();
        return freeBusyData;
    }

}
