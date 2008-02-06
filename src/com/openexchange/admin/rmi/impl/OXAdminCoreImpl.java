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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
package com.openexchange.admin.rmi.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.openexchange.admin.daemons.AdminDaemon;
import com.openexchange.admin.rmi.OXAdminCoreInterface;

public class OXAdminCoreImpl implements OXAdminCoreInterface {

    private static final Log log = LogFactory.getLog(OXAdminCoreImpl.class);
    
    private BundleContext context = null;
    
    
    /**
     * @param context
     */
    public OXAdminCoreImpl(BundleContext context) {
        super();
        this.context = context;
    }


    public boolean allPluginsLoaded() throws RemoteException {
        final ArrayList<Bundle> bundlelist = AdminDaemon.getBundlelist();
        final Bundle[] bundles = context.getBundles();
        final List<Bundle> allbundlelist = Arrays.asList(bundles);
        // First one is the system bundle, which is always loaded, so we ignore it here. As we cannot remove
        // in this type of list we make a new one...
        final List<Bundle> subList = allbundlelist.subList(1, allbundlelist.size());
        
        final boolean containsAll = bundlelist.containsAll(subList);
        if (containsAll) {
            return true;
        } else {
            // We have to introduce a new list because a sublist can't be modified
            final ArrayList<Bundle> arrayList = new ArrayList<Bundle>(subList);
            arrayList.removeAll(bundlelist);
            log.error("The following bundles aren't started: " + arrayList);
            return false;
        }
    }

}
