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

package com.openexchange.index.internal;

import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.server.ServiceLookup;


/**
 * {@link IndexServiceLookup}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class IndexServiceLookup implements ServiceLookup {

    private static final IndexServiceLookup INSTANCE = new IndexServiceLookup();

    private final AtomicReference<ServiceLookup> serviceLookupRef;


    private IndexServiceLookup() {
        super();
        serviceLookupRef = new AtomicReference<ServiceLookup>();
    }

    public static IndexServiceLookup getInstance() {
        return INSTANCE;
    }

    @Override
    public <S> S getService(Class<? extends S> clazz) {
        final ServiceLookup serviceLookup = serviceLookupRef.get();
        if (null == serviceLookup) {
            return null;
        }

        return serviceLookup.getService(clazz);
    }
    
    @Override
    public <S> S getOptionalService(Class<? extends S> clazz) {
        final ServiceLookup serviceLookup = serviceLookupRef.get();
        if (null == serviceLookup) {
            return null;
        }

        return serviceLookup.getOptionalService(clazz);
    }

    public void setServiceLookup(final ServiceLookup serviceLookup) {
        serviceLookupRef.set(serviceLookup);
    }
    
    

}
