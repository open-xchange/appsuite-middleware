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
 *     Copyright (C) 2018-2020 OX Software GmbH
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
import com.openexchange.consistency.osgi.ConsistencyServiceLookup;
import com.openexchange.exception.OXException;

/**
 * {@link ConsistencyRMIServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class ConsistencyRMIServiceImpl implements ConsistencyRMIService {

    private static final Logger LOG = LoggerFactory.getLogger(ConsistencyRMIServiceImpl.class);

    /**
     * Initialises a new {@link ConsistencyRMIServiceImpl}.
     */
    public ConsistencyRMIServiceImpl() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.rmi.ConsistencyCheckRMIService#checkOrRepairConfigDB(boolean)
     */
    @Override
    public List<String> checkOrRepairConfigDB(boolean repair) throws RemoteException {
        try {
            LOG.info("RMI invocation for: {} inconsistent configdb", repair ? "Repair" : "List");
            ConsistencyService service = ConsistencyServiceLookup.getService(ConsistencyService.class);
            return service.checkOrRepairConfigDB(repair);
        } catch (OXException e) {
            LOG.error("", e);
            Exception wrapMe = new Exception(e.getMessage());
            throw new RemoteException(e.getMessage(), wrapMe);
        } catch (RuntimeException e) {
            LOG.error("", e);
            throw e;
        } catch (Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.rmi.ConsistencyCheckRMIService#listMissingFilesInContext(int)
     */
    @Override
    public List<String> listMissingFilesInContext(int contextId) throws RemoteException {
        try {
            LOG.info("RMI invocation for: Listing missing files in context {}", contextId);
            ConsistencyService service = ConsistencyServiceLookup.getService(ConsistencyService.class);
            return service.listMissingFilesInContext(contextId);
        } catch (OXException e) {
            LOG.error("", e);
            Exception wrapMe = new Exception(e.getMessage());
            throw new RemoteException(e.getMessage(), wrapMe);
        } catch (RuntimeException e) {
            LOG.error("", e);
            throw e;
        } catch (Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.rmi.ConsistencyCheckRMIService#listMissingFilesInFilestore(int)
     */
    @Override
    public Map<ConsistencyEntity, List<String>> listMissingFilesInFilestore(int filestoreId) throws RemoteException {
        try {
            LOG.info("RMI invocation for: Listing missing files in filestore {}", filestoreId);
            ConsistencyService service = ConsistencyServiceLookup.getService(ConsistencyService.class);
            return convertMap(service.listMissingFilesInFilestore(filestoreId));
        } catch (OXException e) {
            LOG.error("", e);
            Exception wrapMe = new Exception(e.getMessage());
            throw new RemoteException(e.getMessage(), wrapMe);
        } catch (RuntimeException e) {
            LOG.error("", e);
            throw e;
        } catch (final Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.rmi.ConsistencyCheckRMIService#listMissingFilesInDatabase(int)
     */
    @Override
    public Map<ConsistencyEntity, List<String>> listMissingFilesInDatabase(int databaseId) throws RemoteException {
        try {
            LOG.info("RMI invocation for: List missing files in database {}", databaseId);
            ConsistencyService service = ConsistencyServiceLookup.getService(ConsistencyService.class);
            return convertMap(service.listMissingFilesInDatabase(databaseId));
        } catch (OXException e) {
            LOG.error("", e);
            Exception wrapMe = new Exception(e.getMessage());
            throw new RemoteException(e.getMessage(), wrapMe);
        } catch (RuntimeException e) {
            LOG.error("", e);
            throw e;
        } catch (Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.rmi.ConsistencyCheckRMIService#listAllMissingFiles()
     */
    @Override
    public Map<ConsistencyEntity, List<String>> listAllMissingFiles() throws RemoteException {
        try {
            LOG.info("RMI invocation for: List all missing files");
            ConsistencyService service = ConsistencyServiceLookup.getService(ConsistencyService.class);
            return convertMap(service.listAllMissingFiles());
        } catch (OXException e) {
            LOG.error("", e);
            final Exception wrapMe = new Exception(e.getMessage());
            throw new RemoteException(e.getMessage(), wrapMe);
        } catch (RuntimeException e) {
            LOG.error("", e);
            throw e;
        } catch (Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.rmi.ConsistencyCheckRMIService#listUnassignedFilesInContext(int)
     */
    @Override
    public List<String> listUnassignedFilesInContext(int contextId) throws RemoteException {
        try {
            LOG.info("RMI invocation for: List all unassigned files in context {}", contextId);
            ConsistencyService service = ConsistencyServiceLookup.getService(ConsistencyService.class);
            return service.listUnassignedFilesInContext(contextId);
        } catch (OXException e) {
            LOG.error("", e);
            final Exception wrapMe = new Exception(e.getMessage());
            throw new RemoteException(e.getMessage(), wrapMe);
        } catch (RuntimeException e) {
            LOG.error("", e);
            throw e;
        } catch (Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.rmi.ConsistencyCheckRMIService#listUnassignedFilesInFilestore(int)
     */
    @Override
    public Map<ConsistencyEntity, List<String>> listUnassignedFilesInFilestore(int filestoreId) throws RemoteException {
        try {
            LOG.info("RMI invocation for: List all unassigned files in filestore {}", filestoreId);
            ConsistencyService service = ConsistencyServiceLookup.getService(ConsistencyService.class);
            return convertMap(service.listMissingFilesInFilestore(filestoreId));
        } catch (OXException e) {
            LOG.error("", e);
            final Exception wrapMe = new Exception(e.getMessage());
            throw new RemoteException(e.getMessage(), wrapMe);
        } catch (RuntimeException e) {
            LOG.error("", e);
            throw e;
        } catch (final Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.rmi.ConsistencyCheckRMIService#listUnassignedFilesInDatabase(int)
     */
    @Override
    public Map<ConsistencyEntity, List<String>> listUnassignedFilesInDatabase(int databaseId) throws RemoteException {
        try {
            LOG.info("RMI invocation for: List all unassigned files in database {}", databaseId);
            ConsistencyService service = ConsistencyServiceLookup.getService(ConsistencyService.class);
            return convertMap(service.listUnassignedFilesInDatabase(databaseId));
        } catch (OXException e) {
            LOG.error("", e);
            final Exception wrapMe = new Exception(e.getMessage());
            throw new RemoteException(e.getMessage(), wrapMe);
        } catch (RuntimeException e) {
            LOG.error("", e);
            throw e;
        } catch (Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.rmi.ConsistencyCheckRMIService#listAllUnassignedFiles()
     */
    @Override
    public Map<ConsistencyEntity, List<String>> listAllUnassignedFiles() throws RemoteException {
        try {
            LOG.info("RMI invocation for: List all unassigned files");
            ConsistencyService service = ConsistencyServiceLookup.getService(ConsistencyService.class);
            return convertMap(service.listAllUnassignedFiles());
        } catch (OXException e) {
            LOG.error("", e);
            Exception wrapMe = new Exception(e.getMessage());
            throw new RemoteException(e.getMessage(), wrapMe);
        } catch (RuntimeException e) {
            LOG.error("", e);
            throw e;
        } catch (Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.rmi.ConsistencyRMIService#repairFilesInContext(int, java.lang.String, java.lang.String)
     */
    @Override
    public void repairFilesInContext(int contextId, String repairPolicy, String repairAction) throws RemoteException {
        try {
            LOG.info("RMI invocation for: Repair files in context {} with repair policy {} and repair action {}", contextId, repairPolicy, repairAction);
            ConsistencyService service = ConsistencyServiceLookup.getService(ConsistencyService.class);
            service.repairFilesInContext(contextId, RepairPolicy.valueOf(repairPolicy.toUpperCase()), RepairAction.valueOf(repairAction.toUpperCase()));
        } catch (OXException e) {
            LOG.error("", e);
            Exception wrapMe = new Exception(e.getMessage());
            throw new RemoteException(e.getMessage(), wrapMe);
        } catch (RuntimeException e) {
            LOG.error("", e);
            throw e;
        } catch (final Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.rmi.ConsistencyRMIService#repairFilesInFilestore(int, java.lang.String, java.lang.String)
     */
    @Override
    public void repairFilesInFilestore(int filestoreId, String repairPolicy, String repairAction) throws RemoteException {
        try {
            LOG.info("RMI invocation for: Repair files in filestore {} with repair policy {} and repair action {}", filestoreId, repairPolicy, repairAction);
            ConsistencyService service = ConsistencyServiceLookup.getService(ConsistencyService.class);
            service.repairFilesInFilestore(filestoreId, RepairPolicy.valueOf(repairPolicy.toUpperCase()), RepairAction.valueOf(repairAction.toUpperCase()));
        } catch (final OXException e) {
            LOG.error("", e);
            Exception wrapMe = new Exception(e.getMessage());
            throw new RemoteException(e.getMessage(), wrapMe);
        } catch (RuntimeException e) {
            LOG.error("", e);
            throw e;
        } catch (Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.rmi.ConsistencyRMIService#repairFilesInDatabase(int, java.lang.String, java.lang.String)
     */
    @Override
    public void repairFilesInDatabase(int databaseId, String repairPolicy, String repairAction) throws RemoteException {
        try {
            LOG.info("RMI invocation for: Repair files in database {} with repair policy {} and repair action {}", databaseId, repairPolicy, repairAction);
            ConsistencyService service = ConsistencyServiceLookup.getService(ConsistencyService.class);
            service.repairFilesInDatabase(databaseId, RepairPolicy.valueOf(repairPolicy.toUpperCase()), RepairAction.valueOf(repairAction.toUpperCase()));
        } catch (final OXException e) {
            LOG.error("", e);
            Exception wrapMe = new Exception(e.getMessage());
            throw new RemoteException(e.getMessage(), wrapMe);
        } catch (RuntimeException e) {
            LOG.error("", e);
            throw e;
        } catch (Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.consistency.rmi.ConsistencyRMIService#repairAllFiles(java.lang.String, java.lang.String)
     */
    @Override
    public void repairAllFiles(String repairPolicy, String repairAction) throws RemoteException {
        try {
            LOG.info("RMI invocation for: Repair files with repair policy {} and repair action {}", repairPolicy, repairAction);
            ConsistencyService service = ConsistencyServiceLookup.getService(ConsistencyService.class);
            service.repairAllFiles(RepairPolicy.valueOf(repairPolicy.toUpperCase()), RepairAction.valueOf(repairAction.toUpperCase()));
        } catch (final OXException e) {
            LOG.error("", e);
            final Exception wrapMe = new Exception(e.getMessage());
            throw new RemoteException(e.getMessage(), wrapMe);
        } catch (RuntimeException e) {
            LOG.error("", e);
            throw e;
        } catch (Error e) {
            LOG.error("", e);
            throw e;
        }
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
}
