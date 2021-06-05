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

package com.openexchange.user.copy.osgi;

import java.util.Collection;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.user.copy.CopyUserTaskService;

/**
 * {@link CommandActivator}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class CommandActivator extends HousekeepingActivator {

    public static final class UtilCommandProvider implements CommandProvider {

        private final BundleContext context;

        public UtilCommandProvider(final BundleContext context) {
            super();
            this.context = context;
        }

        @Override
        public String getHelp() {
            final StringBuilder help = new StringBuilder();
            help.append("\tucs - lists all registered CopyUserTaskServices.\n");
            return help.toString();
        }

        public void _ucs(final CommandInterpreter commandInterpreter) {
            try {
                final Collection<ServiceReference<CopyUserTaskService>> references = context.getServiceReferences(CopyUserTaskService.class, null);
                for (final ServiceReference<CopyUserTaskService> reference : references) {
                    final CopyUserTaskService service = context.getService(reference);
                    commandInterpreter.println(service.getClass().toString());
                }
            } catch (InvalidSyntaxException e) {
                commandInterpreter.println("Error: " + e.getMessage());
            }
        }
    }

    /**
     * The constructor
     */
    public CommandActivator() {
        super();
    }

    @Override
    public void startBundle() throws Exception {
        registerService(CommandProvider.class, new UtilCommandProvider(context));
    }

    @Override
    public void stopBundle() throws Exception {
        unregisterServices();

    }

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

}
