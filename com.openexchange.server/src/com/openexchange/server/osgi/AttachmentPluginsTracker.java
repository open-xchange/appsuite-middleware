/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.server.osgi;

import static com.openexchange.java.Autoboxing.I;
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
        if (chooser == null) {
            LOG.warn("Can't register services for module {} in tracker {}", I(module), getClass().getName());
            return;
        }
        if (contextId == null && folderId == null) {
            chooser.removeForEverything(tracked);
            return;
        }
        if (contextId != null && folderId != null) {
            chooser.removeForContextAndFolder(tracked, contextId.intValue(), folderId.intValue());
            return;
        }
        if (contextId != null) {
            chooser.removeForContext(tracked, contextId.intValue());
            return;
        }
        if (folderId != null) {
            chooser.removeForFolder(tracked, folderId.intValue());
        }
    }


    @Override
    public void addingService(int module, T tracked, ServiceReference reference) {
        Integer contextId = getInt(reference, Constants.OX_OVERRIDE_CONTEXT);
        Integer folderId = getInt(reference, Constants.OX_OVERRIDE_FOLDER);
        Integer ranking = getInt(reference, org.osgi.framework.Constants.SERVICE_RANKING);
        if (ranking == null) {
            ranking = I(0);
        }
        SpecificServiceChooser<T> chooser = getChooser(module);
        if (chooser == null) {
            LOG.error("Can't register services for module {} in tracker {}", I(module), getClass().getName());
            return;
        }
        try {
            if (contextId == null && folderId == null) {
                chooser.registerForEverything(tracked, ranking.intValue());
                return;
            }
            if (contextId != null && folderId != null) {
                chooser.registerForContextAndFolder(tracked, ranking.intValue(), contextId.intValue(), folderId.intValue());
                return;
            }
            if (contextId != null) {
                chooser.registerForContext(tracked, ranking.intValue(), contextId.intValue());
                return;
            }
            if (folderId != null) {
                chooser.registerForFolder(tracked, ranking.intValue(), folderId.intValue());
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
        if (property == null) {
            return null;
        }
        if (Integer.class.isInstance(property))  {
            return (Integer) property;
        }
        return Integer.valueOf(property.toString());
    }

}
