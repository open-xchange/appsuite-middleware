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

package com.openexchange.ajax.customizer.folder.osgi;

import java.util.ArrayList;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.ajax.customizer.folder.FolderGetCustomizer;
import com.openexchange.ajax.customizer.folder.FolderGetPathCustomizer;
import com.openexchange.ajax.customizer.folder.FolderRootCustomizer;
import com.openexchange.ajax.customizer.folder.FolderSubfoldersCustomizer;
import com.openexchange.ajax.customizer.folder.FolderUpdatesCustomizer;
import com.openexchange.ajax.customizer.folder.multi.MultiResponseCustomizer;


/**
 * {@link FolderCustomizerTracker}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class FolderCustomizerTracker implements ServiceTrackerCustomizer {
    
    private static final Class[] CLASSES = new Class[]{FolderGetCustomizer.class, FolderGetPathCustomizer.class, FolderRootCustomizer.class, FolderSubfoldersCustomizer.class, FolderUpdatesCustomizer.class };
    
    private BundleContext context;
    private MultiResponseCustomizer customizer;

    private List<ServiceTracker> trackers = new ArrayList<ServiceTracker>(5);
    
    public FolderCustomizerTracker(BundleContext context, MultiResponseCustomizer customizer) {
        this.customizer = customizer;
        this.context = context;
    }

    public Object addingService(ServiceReference reference) {
        Object service  = context.getService(reference);
        if(FolderGetCustomizer.class.isInstance(service)) {
            customizer.addFolderGetCustomizer((FolderGetCustomizer) service);
        }
        if(FolderGetPathCustomizer.class.isInstance(service)) {
            customizer.addFolderGetPathCustomizer((FolderGetPathCustomizer) service);
        }
        if(FolderRootCustomizer.class.isInstance(service)) {
            customizer.addFolderRootCustomizer((FolderRootCustomizer) service);
        }
        if(FolderSubfoldersCustomizer.class.isInstance(service)) {
            customizer.addFolderSubfoldersCustomizer((FolderSubfoldersCustomizer) service);
        }
        if(FolderUpdatesCustomizer.class.isInstance(service)) {
            customizer.addFolderUpdatesCustomizer((FolderUpdatesCustomizer) service);
        }
        return service;
    }

    public void modifiedService(ServiceReference reference, Object service) {
        
    }

    public void removedService(ServiceReference reference, Object service) {
        if(FolderGetCustomizer.class.isInstance(service)) {
            customizer.removeFolderGetCustomizer((FolderGetCustomizer) service);
        }
        if(FolderGetPathCustomizer.class.isInstance(service)) {
            customizer.removeFolderGetPathCustomizer((FolderGetPathCustomizer) service);
        }
        if(FolderRootCustomizer.class.isInstance(service)) {
            customizer.removeFolderRootCustomizer((FolderRootCustomizer) service);
        }
        if(FolderSubfoldersCustomizer.class.isInstance(service)) {
            customizer.removeFolderSubfoldersCustomizer((FolderSubfoldersCustomizer) service);
        }
        if(FolderUpdatesCustomizer.class.isInstance(service)) {
            customizer.removeFolderUpdatesCustomizer((FolderUpdatesCustomizer) service);
        }
    }
    
    public void open() {
        for(Class clazz : CLASSES) {
            ServiceTracker serviceTracker = new ServiceTracker(context, clazz.getName(), this);
            serviceTracker.open();
            trackers.add(serviceTracker);
        }
    }
    
    public void close() {
        for(ServiceTracker serviceTracker : trackers) {
            serviceTracker.close();
        }
    }
    
}
