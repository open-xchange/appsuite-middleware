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

package com.openexchange.freebusy.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.freebusy.FreeBusyData;
import com.openexchange.freebusy.FreeBusyExceptionCodes;
import com.openexchange.freebusy.provider.FreeBusyProvider;
import com.openexchange.freebusy.service.FreeBusyService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.session.Session;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.userconf.UserConfigurationService;

/**
 * {@link FreeBusyServiceImpl}
 *
 * Default free/busy service implementation.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FreeBusyServiceImpl implements FreeBusyService {

    private final FreeBusyProviderRegistry registry;

    /**
     * Initializes a new {@link FreeBusyServiceImpl}.
     *
     * @param registry The registry to use
     */
    public FreeBusyServiceImpl(FreeBusyProviderRegistry registry) {
        super();
        this.registry = registry;
    }

    @Override
    public Map<String, FreeBusyData> getFreeBusy(final Session session, final List<String> participants, final Date from, final Date until) throws OXException {
        checkFreeBusyEnabled(session);
        checkProvidersAvailable();
        if (1 == registry.getProviders().size()) {
            return registry.getProviders().get(0).getFreeBusy(session, participants, from, until);
        } else {
            ExecutorService executor = FreeBusyServiceLookup.getService(ThreadPoolService.class).getExecutor();
            List<Future<Map<String, FreeBusyData>>> futures = new ArrayList<Future<Map<String, FreeBusyData>>>();
            for (final FreeBusyProvider provider : registry.getProviders()) {
                Future<Map<String, FreeBusyData>> future = executor.submit(new AbstractTask<Map<String, FreeBusyData>>() {
                    @Override
                    public Map<String, FreeBusyData> call() throws Exception {
                        return provider.getFreeBusy(session, participants, from, until);
                    }
                });
                futures.add(future);
            }
            return collectFreeBusyInformation(futures);
        }
    }

    @Override
    public FreeBusyData getFreeBusy(Session session, String participant, Date from, Date until) throws OXException {
        Map<String, FreeBusyData> freeBusyInformation = getFreeBusy(session, Arrays.asList(new String[] { participant }), from, until);
        return null != freeBusyInformation && freeBusyInformation.containsKey(participant) ? freeBusyInformation.get(participant) : null;
    }

    @Override
    public Map<String, FreeBusyData> getMergedFreeBusy(Session session, List<String> participants, Date from, Date until) throws OXException {
        Map<String, FreeBusyData> freeBusyData = this.getFreeBusy(session, participants, from, until);
        FreeBusyData mergedFreeBusyData = new FreeBusyData("merged", from, until);
        for (FreeBusyData data : freeBusyData.values()) {
            data.normalize();
            if (data.hasData()) {
                mergedFreeBusyData.addAll(data.getIntervals());
            }
        }
        mergedFreeBusyData.normalize();
        freeBusyData.put("merged", mergedFreeBusyData);
        return freeBusyData;
    }

    @Override
    public FreeBusyData getMergedFreeBusy(Session session, String participant, Date from, Date until) throws OXException {
        FreeBusyData freeBusyData = this.getFreeBusy(session, participant, from, until);
        freeBusyData.normalize();
        return freeBusyData;
    }

    private static Map<String, FreeBusyData> collectFreeBusyInformation(List<Future<Map<String, FreeBusyData>>> futures) throws OXException {
        Map<String, FreeBusyData> freeBusyInformation = new HashMap<String, FreeBusyData>();
        for (Future<Map<String, FreeBusyData>> future : futures) {
            try {
                Map<String, FreeBusyData> providerData = future.get();
                if (null != providerData && 0 < providerData.size()) {
                    for (Entry<String, FreeBusyData> entry : providerData.entrySet()) {
                        FreeBusyData newData = entry.getValue();
                        FreeBusyData existingData = freeBusyInformation.get(entry.getKey());
                        if (null == existingData ||
                            false == existingData.hasData() && existingData.hasWarnings() && false == newData.hasWarnings()) {
                            // use new data
                            freeBusyInformation.put(entry.getKey(), newData);
                        } else if (null == newData || newData.hasWarnings() && false == newData.hasData()) {
                            // use original data
                            continue;
                        } else {
                            // merge both
                            existingData.add(newData);
                        }
                    }
                }
            } catch (InterruptedException e) {
                throw FreeBusyExceptionCodes.INTERNAL_ERROR.create(e, e.getMessage());
            } catch (ExecutionException e) {
                if (OXException.class.isInstance(e.getCause())) {
                    throw (OXException)e.getCause();
                } else {
                    throw FreeBusyExceptionCodes.INTERNAL_ERROR.create(e.getCause(), e.getCause().getMessage());
                }
            }
        }
        return freeBusyInformation;
    }

    private void checkProvidersAvailable() throws OXException {
        if (null == registry || null == registry.getProviders() || 0 == registry.getProviders().size()) {
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

}
