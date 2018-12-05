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

package com.openexchange.consistency.rmi;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.consistency.ConsistencyService;
import com.openexchange.consistency.Entity;
import com.openexchange.consistency.RepairAction;
import com.openexchange.consistency.RepairPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ConsistencyRMIServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class ConsistencyRMIServiceImpl implements ConsistencyRMIService {

    private static final Logger LOG = LoggerFactory.getLogger(ConsistencyRMIServiceImpl.class);
    private final ServiceLookup services;

    /**
     * Initialises a new {@link ConsistencyRMIServiceImpl}.
     */
    public ConsistencyRMIServiceImpl(ServiceLookup services) {
        super();
        this.services = services;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.rmi.ConsistencyCheckRMIService#checkOrRepairConfigDB(boolean)
     */
    @Override
    public List<String> checkOrRepairConfigDB(boolean repair) throws RemoteException {
        return handle((service) -> {
            LOG.info("RMI invocation for: {} inconsistent configdb", repair ? "Repair" : "List");
            return service.checkOrRepairConfigDB(repair);
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.rmi.ConsistencyCheckRMIService#listMissingFilesInContext(int)
     */
    @Override
    public List<String> listMissingFilesInContext(int contextId) throws RemoteException {
        return handle((service) -> {
            LOG.info("RMI invocation for: Listing missing files in context {}", contextId);
            return service.listMissingFilesInContext(contextId);
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.rmi.ConsistencyCheckRMIService#listMissingFilesInFilestore(int)
     */
    @Override
    public Map<ConsistencyEntity, List<String>> listMissingFilesInFilestore(int filestoreId) throws RemoteException {
        return handle((service) -> {
            LOG.info("RMI invocation for: Listing missing files in filestore {}", filestoreId);
            return convertMap(service.listMissingFilesInFilestore(filestoreId));
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.rmi.ConsistencyCheckRMIService#listMissingFilesInDatabase(int)
     */
    @Override
    public Map<ConsistencyEntity, List<String>> listMissingFilesInDatabase(int databaseId) throws RemoteException {
        return handle((service) -> {
            LOG.info("RMI invocation for: List missing files in database {}", databaseId);
            return convertMap(service.listMissingFilesInDatabase(databaseId));
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.rmi.ConsistencyCheckRMIService#listAllMissingFiles()
     */
    @Override
    public Map<ConsistencyEntity, List<String>> listAllMissingFiles() throws RemoteException {
        return handle((service) -> {
            LOG.info("RMI invocation for: List all missing files");
            return convertMap(service.listAllMissingFiles());
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.rmi.ConsistencyCheckRMIService#listUnassignedFilesInContext(int)
     */
    @Override
    public List<String> listUnassignedFilesInContext(int contextId) throws RemoteException {
        return handle((service) -> {
            LOG.info("RMI invocation for: List all unassigned files in context {}", contextId);
            return service.listUnassignedFilesInContext(contextId);
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.rmi.ConsistencyCheckRMIService#listUnassignedFilesInFilestore(int)
     */
    @Override
    public Map<ConsistencyEntity, List<String>> listUnassignedFilesInFilestore(int filestoreId) throws RemoteException {
        return handle((service) -> {
            LOG.info("RMI invocation for: List all unassigned files in filestore {}", filestoreId);
            return convertMap(service.listMissingFilesInFilestore(filestoreId));
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.rmi.ConsistencyCheckRMIService#listUnassignedFilesInDatabase(int)
     */
    @Override
    public Map<ConsistencyEntity, List<String>> listUnassignedFilesInDatabase(int databaseId) throws RemoteException {
        return handle((service) -> {
            LOG.info("RMI invocation for: List all unassigned files in database {}", databaseId);
            return convertMap(service.listUnassignedFilesInDatabase(databaseId));
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.rmi.ConsistencyCheckRMIService#listAllUnassignedFiles()
     */
    @Override
    public Map<ConsistencyEntity, List<String>> listAllUnassignedFiles() throws RemoteException {
        return handle((service) -> {
            LOG.info("RMI invocation for: List all unassigned files");
            return convertMap(service.listAllUnassignedFiles());
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.rmi.ConsistencyRMIService#repairFilesInContext(int, java.lang.String, java.lang.String)
     */
    @Override
    public void repairFilesInContext(int contextId, String repairPolicy, String repairAction) throws RemoteException {
        handle((service) -> {
            LOG.info("RMI invocation for: Repair files in context {} with repair policy {} and repair action {}", contextId, repairPolicy, repairAction);
            service.repairFilesInContext(contextId, RepairPolicy.valueOf(repairPolicy.toUpperCase()), RepairAction.valueOf(repairAction.toUpperCase()));
            return null;
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.rmi.ConsistencyRMIService#repairFilesInFilestore(int, java.lang.String, java.lang.String)
     */
    @Override
    public void repairFilesInFilestore(int filestoreId, String repairPolicy, String repairAction) throws RemoteException {
        handle((service) -> {
            LOG.info("RMI invocation for: Repair files in filestore {} with repair policy {} and repair action {}", filestoreId, repairPolicy, repairAction);
            service.repairFilesInFilestore(filestoreId, RepairPolicy.valueOf(repairPolicy.toUpperCase()), RepairAction.valueOf(repairAction.toUpperCase()));
            return null;
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.rmi.ConsistencyRMIService#repairFilesInDatabase(int, java.lang.String, java.lang.String)
     */
    @Override
    public void repairFilesInDatabase(int databaseId, String repairPolicy, String repairAction) throws RemoteException {
        handle((service) -> {
            LOG.info("RMI invocation for: Repair files in database {} with repair policy {} and repair action {}", databaseId, repairPolicy, repairAction);
            service.repairFilesInDatabase(databaseId, RepairPolicy.valueOf(repairPolicy.toUpperCase()), RepairAction.valueOf(repairAction.toUpperCase()));
            return null;
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.rmi.ConsistencyRMIService#repairAllFiles(java.lang.String, java.lang.String)
     */
    @Override
    public void repairAllFiles(String repairPolicy, String repairAction) throws RemoteException {
        handle((service) -> {
            LOG.info("RMI invocation for: Repair files with repair policy {} and repair action {}", repairPolicy, repairAction);
            service.repairAllFiles(RepairPolicy.valueOf(repairPolicy.toUpperCase()), RepairAction.valueOf(repairAction.toUpperCase()));
            return null;
        });
    }

    //////////////////////////////////////// HELPERS //////////////////////////////////////

    /**
     * Converts an Entity objects to {@link ConsistencyEntity} objects
     *
     * @param entity The entity object to convert
     * @return the {@link ConsistencyEntity}
     */
    private ConsistencyEntity toConsistencyEntity(Entity entity) {
        switch (entity.getType()) {
            case Context:
                return new ConsistencyEntity(entity.getContext().getContextId());
            case User:
                return new ConsistencyEntity(entity.getContext().getContextId(), entity.getUser().getId());
            default:
                throw new IllegalArgumentException("Unknown entity type: " + entity.getType());
        }
    }

    /**
     * Converts the keys of the specified {@link Map} from {@link Entity} to {@link ConsistencyEntity}
     * 
     * @param entities The map to convert
     * @return The converted map
     */
    private Map<ConsistencyEntity, List<String>> convertMap(Map<Entity, List<String>> entities) {
        return entities.entrySet().stream().collect(Collectors.toMap(e -> toConsistencyEntity(e.getKey()), e -> e.getValue()));
    }

    /**
     * Applies the given {@link ConsistencyWorker} and handles errors
     * 
     * @param w The worker
     * @return The output from the worker
     * @throws RemoteException If {@link OXException} happens
     */
    private <T> T handle(ConsistencyPerformer<T> performer) throws RemoteException {
        try {
            return performer.perform(services.getServiceSafe(ConsistencyService.class));
        } catch (final OXException e) {
            LOG.error("", e);
            final Exception wrapMe = new Exception(e.getMessage());
            throw new RemoteException(e.getMessage(), wrapMe);
        } catch (RuntimeException e) {
            LOG.error("", e);
            throw e;
        }
    }

    /**
     * 
     * {@link ConsistencyPerformer}
     * 
     * @param <T> - The outcome of the operation
     */
    @FunctionalInterface
    private interface ConsistencyPerformer<T> {

        /**
         * Performs the denoted consistency operation
         * 
         * @param service The {@link ConsistencyService}
         * @return The outcome of the operation
         * @throws OXException if an error is occurred
         */
        T perform(ConsistencyService service) throws OXException;
    }
}
