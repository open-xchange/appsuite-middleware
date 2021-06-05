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

package com.openexchange.database.cleanup.impl.rmi;

import java.rmi.RemoteException;
import java.util.List;
import com.openexchange.database.cleanup.DatabaseCleanUpService;
import com.openexchange.database.cleanup.rmi.DatabaseCleanUpRMIService;
import com.openexchange.exception.OXException;

/**
 * {@link DatabaseCleanUpRMIServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v8.0.0
 */
public class DatabaseCleanUpRMIServiceImpl implements DatabaseCleanUpRMIService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DatabaseCleanUpRMIServiceImpl.class);

    private final DatabaseCleanUpService service;

    /**
     * Initializes a new {@link DatabaseCleanUpRMIServiceImpl}.
     *
     * @param service The service
     */
    public DatabaseCleanUpRMIServiceImpl(DatabaseCleanUpService service) {
        super();
        this.service = service;
    }

    @Override
    public List<String> getCleanUpJobs() throws RemoteException {
        try {
            return service.getCleanUpJobs();
        } catch (OXException e) {
            LOG.error("", e);
            throw new RemoteException(e.getPlainLogMessage(), e);
        } catch (RuntimeException | Error e) {
            LOG.error("", e);
            throw e;
        }
    }


}
