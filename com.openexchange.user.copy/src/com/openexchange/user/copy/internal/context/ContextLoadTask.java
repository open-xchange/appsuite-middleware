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

package com.openexchange.user.copy.internal.context;

import static com.openexchange.java.Autoboxing.i;
import java.util.Map;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.user.copy.CopyUserTaskService;
import com.openexchange.user.copy.ObjectMapping;
import com.openexchange.user.copy.internal.CopyTools;

/**
 * Loads the source and the destination context and returns them to put them into the Map for further copy tasks.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class ContextLoadTask implements CopyUserTaskService {

    private final ContextService contextService;

    public ContextLoadTask(final ContextService contextService) {
        super();
        this.contextService = contextService;
    }

    @Override
    public String[] getAlreadyCopied() {
        return new String[0];
    }

    @Override
    public String getObjectName() {
        return Context.class.getName();
    }

    @Override
    public ContextMapping copyUser(final Map<String, ObjectMapping<?>> copied) throws OXException {
        final CopyTools copyTools = new CopyTools(copied);
        final ContextMapping retval = new ContextMapping();
        final Integer sourceId = copyTools.getSourceContextId();
        final Integer destinationId = copyTools.getDestinationContextId();
        final Context source = contextService.getContext(i(sourceId));
        final Context destination = contextService.getContext(i(destinationId));
        retval.addMapping(sourceId, source, destinationId, destination);

        return retval;
    }

    @Override
    public void done(final Map<String, ObjectMapping<?>> copied, final boolean failed) {
        // Nothing to do.
    }
}
