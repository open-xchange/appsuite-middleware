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

package com.openexchange.server.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import com.openexchange.server.impl.Constants;
import com.openexchange.tools.service.ServicePriorityConflictException;
import com.openexchange.tools.service.SpecificServiceChooser;


/**
 * {@link AttachmentPluginsTracker}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public abstract class AttachmentPluginsTracker<T> extends ModuleSpecificServiceTracker<T> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AttachmentPluginsTracker.class);

    public AttachmentPluginsTracker(BundleContext context, Class<T> toTrack) {
        super(context, toTrack);
    }

    @Override
    public void removedService(int module, T tracked, ServiceReference reference) {
        Integer contextId = getInt(reference, Constants.OX_OVERRIDE_CONTEXT);
        Integer folderId = getInt(reference, Constants.OX_OVERRIDE_FOLDER);

        SpecificServiceChooser<T> chooser = getChooser(module);
        if(chooser == null) {
            LOG.warn("Can't register services for module {} in tracker {}", module, getClass().getName());
        }
        if(contextId == null && folderId == null) {
            chooser.removeForEverything(tracked);
            return;
        }
        if(contextId != null && folderId != null) {
            chooser.removeForContextAndFolder(tracked, contextId, folderId);
            return;
        }
        if(contextId != null) {
            chooser.removeForContext(tracked, contextId);
            return;
        }
        if(folderId != null) {
            chooser.removeForFolder(tracked, folderId);
        }
    }


    @Override
    public void addingService(int module, T tracked, ServiceReference reference) {
        Integer contextId = getInt(reference, Constants.OX_OVERRIDE_CONTEXT);
        Integer folderId = getInt(reference, Constants.OX_OVERRIDE_FOLDER);
        Integer ranking = getInt(reference, org.osgi.framework.Constants.SERVICE_RANKING);
        if(ranking == null) {
            ranking = 0;
        }
        SpecificServiceChooser<T> chooser = getChooser(module);
        if(chooser == null) {
            LOG.error("Can't register services for module {} in tracker {}", module, getClass().getName());
            return;
        }
        try {
            if(contextId == null && folderId == null) {
                chooser.registerForEverything(tracked, ranking);
                return;
            }
            if(contextId != null && folderId != null) {
                chooser.registerForContextAndFolder(tracked, ranking, contextId, folderId);
                return;
            }
            if(contextId != null) {
                chooser.registerForContext(tracked, ranking, contextId);
                return;
            }
            if(folderId != null) {
                chooser.registerForFolder(tracked, ranking, folderId);
            }

        } catch (ServicePriorityConflictException x) {
            LOG.error("Could not register service {} with contextId: {} for folder: {} with ranking: {}. A conflicting service has already been registered for the combination", tracked, contextId, folderId, ranking);
        }
    }

    @Override
    public void modifiedService(int module, T tracked, ServiceReference reference) {

    }


    public abstract SpecificServiceChooser<T> getChooser(int module);


    protected Integer getInt(ServiceReference reference, String key) {
        Object property = reference.getProperty(key);
        if(property == null) {
            return null;
        }
        if(Integer.class.isInstance(property))  {
            return (Integer) property;
        }
        return Integer.parseInt(property.toString());
    }

}
