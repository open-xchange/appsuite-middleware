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

package com.openexchange.admin.rmi;

import org.junit.Test;
import com.openexchange.admin.rmi.dataobjects.Context;

/**
 * Tries to reproduce the log messages described in bug 27065. This test does not really test or assert something. Therefore the logs must
 * be readable and the master -> slave replication needs to be slower than calling RMI methods. Especially the second requirement is hard to
 * provide automatically for this test. Maybe a parallel thread that causes high load on the replication can be used for this.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class Bug27065Test extends AbstractRMITest {

    private Context context;

    /**
     * Initialises a new {@link Bug27065Test}.
     */
    public Bug27065Test() {
        super();
    }

    /**
     * Reproduce the log message
     */
    @Test
    public void reproduceMessages() throws Exception {
        context = getContextManager().create(contextAdminCredentials);
        Thread.sleep(400); // Only necessary if the replication from master to slave becomes a little bit slow.
        getContextManager().changeModuleAccess(new Context(context.getId()), "webmail");
        getContextManager().downgrade(new Context(context.getId()));
    }
}
