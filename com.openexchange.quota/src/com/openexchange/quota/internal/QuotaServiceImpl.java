///*
// *
// *    OPEN-XCHANGE legal information
// *
// *    All intellectual property rights in the Software are protected by
// *    international copyright laws.
// *
// *
// *    In some countries OX, OX Open-Xchange, open xchange and OXtender
// *    as well as the corresponding Logos OX Open-Xchange and OX are registered
// *    trademarks of the Open-Xchange, Inc. group of companies.
// *    The use of the Logos is not covered by the GNU General Public License.
// *    Instead, you are allowed to use these Logos according to the terms and
// *    conditions of the Creative Commons License, Version 2.5, Attribution,
// *    Non-commercial, ShareAlike, and the interpretation of the term
// *    Non-commercial applicable to the aforementioned license is published
// *    on the web site http://www.open-xchange.com/EN/legal/index.html.
// *
// *    Please make sure that third-party modules and libraries are used
// *    according to their respective licenses.
// *
// *    Any modifications to this package must retain all copyright notices
// *    of the original copyright holder(s) for the original code used.
// *
// *    After any such modifications, the original and derivative code shall remain
// *    under the copyright of the copyright holder(s) and/or original author(s)per
// *    the Attribution and Assignment Agreement that can be located at
// *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
// *    given Attribution for the derivative code and a license granting use.
// *
// *     Copyright (C) 2004-2020 Open-Xchange, Inc.
// *     Mail: info@open-xchange.com
// *
// *
// *     This program is free software; you can redistribute it and/or modify it
// *     under the terms of the GNU General Public License, Version 2 as published
// *     by the Free Software Foundation.
// *
// *     This program is distributed in the hope that it will be useful, but
// *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
// *     for more details.
// *
// *     You should have received a copy of the GNU General Public License along
// *     with this program; if not, write to the Free Software Foundation, Inc., 59
// *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
// *
// */
//
//package com.openexchange.quota.internal;
//
//import java.util.EnumMap;
//import java.util.HashSet;
//import java.util.Set;
//import com.openexchange.exception.OXException;
//import com.openexchange.quota.Quota;
//import com.openexchange.quota.QuotaRestriction;
//import com.openexchange.quota.QuotaService;
//import com.openexchange.quota.Resource;
//import com.openexchange.quota.ResourceDescription;
//import com.openexchange.quota.ServiceProvider;
//import com.openexchange.quota.UnlimitedQuota;
//import com.openexchange.quota.osgi.QuotaActivator;
//import com.openexchange.session.Session;
//
///**
// * {@link QuotaServiceImpl}
// *
// * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
// */
//public final class QuotaServiceImpl implements QuotaService {
//
//    private final Set<Class<?>> trackedServices;
//    private final EnumMap<Resource, QuotaRestriction> restrictions;
//    private final QuotaActivator services;
//    private final ServiceProvider serviceProvider;
//
//    /**
//     * Initializes a new {@link QuotaServiceImpl}.
//     */
//    public QuotaServiceImpl(final QuotaActivator services) {
//        super();
//        restrictions = new EnumMap<Resource, QuotaRestriction>(Resource.class);
//        this.services = services;
//        trackedServices = new HashSet<Class<?>>(8);
//        serviceProvider = new ServiceProviderImpl(services);
//    }
//
//    @Override
//    public Quota getQuotaFor(final Resource resource, final Session session) throws OXException {
//        return getQuotaFor(resource, ResourceDescription.getEmptyResourceDescription(), session);
//    }
//
//    @Override
//    public Quota getQuotaFor(final Resource resource, final ResourceDescription resourceDescription, final Session session) throws OXException {
//        if (null == resource || null == session) {
//            return UnlimitedQuota.getInstance();
//        }
//        final QuotaRestriction quotaRestriction;
//        synchronized (restrictions) {
//            quotaRestriction = restrictions.get(resource);
//        }
//        if (null == quotaRestriction) {
//            return UnlimitedQuota.getInstance();
//        }
//        return quotaRestriction.getQuota(
//            resource,
//            null == resourceDescription ? ResourceDescription.getEmptyResourceDescription() : resourceDescription,
//            session,
//            serviceProvider);
//    }
//
//    /**
//     * Adds given quota restriction.
//     *
//     * @param restriction The quota restriction.
//     * @return <code>true</code> if added; otherwise <code>false</code>
//     */
//    public boolean addQuotaRestriction(final QuotaRestriction restriction) {
//        synchronized (restrictions) {
//            if (restrictions.containsKey(restriction.getResource())) {
//                return false;
//            }
//            final Class<?>[] clazzes = restriction.getNeededServices();
//            if (null != clazzes) {
//                final QuotaActivator services = this.services;
//                for (final Class<?> clazz : clazzes) {
//                    if (null == services.getService(clazz) && !trackedServices.contains(clazz)) {
//                        // Schedule tracker for that service
//                        services.trackService(clazz).open();
//                    }
//                }
//            }
//            restrictions.put(restriction.getResource(), restriction);
//            return true;
//        }
//    }
//
//    /**
//     * Removes quota restriction.
//     *
//     * @param restriction The quota restriction.
//     */
//    public void removeQuotaRestriction(final QuotaRestriction restriction) {
//        synchronized (restrictions) {
//            restrictions.remove(restriction.getResource());
//        }
//    }
//
//    @Override
//    public String toString() {
//        return "QuotaServiceImpl";
//    }
//
//}
