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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.admin.soap;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.xsd.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.soap.dataobjects.xsd.Context;
import com.openexchange.admin.soap.dataobjects.xsd.Database;
import com.openexchange.admin.soap.dataobjects.xsd.Entry;
import com.openexchange.admin.soap.dataobjects.xsd.Filestore;
import com.openexchange.admin.soap.dataobjects.xsd.SOAPMapEntry;
import com.openexchange.admin.soap.dataobjects.xsd.SOAPStringMap;
import com.openexchange.admin.soap.dataobjects.xsd.SOAPStringMapMap;
import com.openexchange.admin.soap.dataobjects.xsd.User;
import com.openexchange.admin.soap.dataobjects.xsd.UserModuleAccess;

/**
 * {@link OXContextServicePortTypeImpl}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OXContextServicePortTypeImpl implements OXContextServicePortType {

    public static final AtomicReference<com.openexchange.admin.rmi.OXContextInterface> RMI_REFERENCE = new AtomicReference<com.openexchange.admin.rmi.OXContextInterface>();

    /**
     * Initializes a new {@link OXContextServicePortTypeImpl}.
     */
    public OXContextServicePortTypeImpl() {
        super();
    }

    @Override
    public void change(final Change parameters) throws StorageException_Exception, InvalidCredentialsException_Exception, InvalidDataException_Exception, NoSuchContextException_Exception, RemoteException_Exception {
        final OXContextInterface contextInterface = RMI_REFERENCE.get();
        if (null == contextInterface) {
            throw new RemoteException_Exception("Missing OXContextInterface service");
        }
        try {
            contextInterface.change(soap2Context(parameters.getCtx().getValue()), soap2Credentials(parameters.getAuth().getValue()));
        } catch (final RemoteException e) {
            throw new RemoteException_Exception(e.getMessage(), e);
        } catch (final InvalidCredentialsException e) {
            throw new InvalidCredentialsException_Exception(e.getMessage(), e);
        } catch (final NoSuchContextException e) {
            throw new NoSuchContextException_Exception(e.getMessage(), e);
        } catch (final StorageException e) {
            throw new StorageException_Exception(e.getMessage(), e);
        } catch (final InvalidDataException e) {
            throw new InvalidDataException_Exception(e.getMessage(), e);
        }
    }

    @Override
    public UserModuleAccess getModuleAccess(final Context ctx, final Credentials auth) throws StorageException_Exception, InvalidCredentialsException_Exception, InvalidDataException_Exception, NoSuchContextException_Exception, RemoteException_Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void enable(final Enable parameters) throws StorageException_Exception, InvalidCredentialsException_Exception, InvalidDataException_Exception, NoSuchContextException_Exception, RemoteException_Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void disableAll(final DisableAll parameters) throws StorageException_Exception, InvalidCredentialsException_Exception, NoSuchReasonException_Exception, InvalidDataException_Exception, RemoteException_Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void changeModuleAccessByName(final ChangeModuleAccessByName parameters) throws StorageException_Exception, InvalidCredentialsException_Exception, InvalidDataException_Exception, NoSuchContextException_Exception, RemoteException_Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public List<Context> listAll(final Credentials auth) throws StorageException_Exception, InvalidCredentialsException_Exception, InvalidDataException_Exception, RemoteException_Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer moveContextDatabase(final Context ctx, final Database dstDatabaseId, final Credentials auth) throws StorageException_Exception, InvalidCredentialsException_Exception, InvalidDataException_Exception, OXContextException_Exception, NoSuchContextException_Exception, RemoteException_Exception, DatabaseUpdateException_Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Context createModuleAccess(final Context ctx, final User adminUser, final UserModuleAccess access, final Credentials auth) throws StorageException_Exception, InvalidCredentialsException_Exception, InvalidDataException_Exception, ContextExistsException_Exception, RemoteException_Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void changeModuleAccess(final ChangeModuleAccess parameters) throws StorageException_Exception, InvalidCredentialsException_Exception, InvalidDataException_Exception, NoSuchContextException_Exception, RemoteException_Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public Context createModuleAccessByName(final Context ctx, final User adminUser, final String accessCombinationName, final Credentials auth) throws StorageException_Exception, InvalidCredentialsException_Exception, InvalidDataException_Exception, ContextExistsException_Exception, RemoteException_Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void enableAll(final EnableAll parameters) throws StorageException_Exception, InvalidCredentialsException_Exception, RemoteException_Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public List<Context> listByFilestore(final Filestore fs, final Credentials auth) throws StorageException_Exception, InvalidCredentialsException_Exception, InvalidDataException_Exception, RemoteException_Exception, NoSuchFilestoreException_Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer getAdminId(final Context ctx, final Credentials auth) throws StorageException_Exception, InvalidCredentialsException_Exception, NoSuchContextException_Exception, RemoteException_Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void delete(final Delete parameters) throws StorageException_Exception, InvalidCredentialsException_Exception, InvalidDataException_Exception, NoSuchContextException_Exception, RemoteException_Exception, DatabaseUpdateException_Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void disable(final Disable parameters) throws StorageException_Exception, InvalidCredentialsException_Exception, NoSuchReasonException_Exception, InvalidDataException_Exception, OXContextException_Exception, NoSuchContextException_Exception, RemoteException_Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public String getAccessCombinationName(final Context ctx, final Credentials auth) throws StorageException_Exception, InvalidCredentialsException_Exception, InvalidDataException_Exception, NoSuchContextException_Exception, RemoteException_Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void downgrade(final Downgrade parameters) throws StorageException_Exception, InvalidCredentialsException_Exception, InvalidDataException_Exception, NoSuchContextException_Exception, RemoteException_Exception, DatabaseUpdateException_Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public Context create(final Context ctx, final User adminUser, final Credentials auth) throws StorageException_Exception, InvalidCredentialsException_Exception, InvalidDataException_Exception, ContextExistsException_Exception, RemoteException_Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer moveContextFilestore(final Context ctx, final Filestore dstFilestoreId, final Credentials auth) throws StorageException_Exception, InvalidCredentialsException_Exception, NoSuchReasonException_Exception, InvalidDataException_Exception, OXContextException_Exception, NoSuchContextException_Exception, RemoteException_Exception, NoSuchFilestoreException_Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Context> list(final String searchPattern, final Credentials auth) throws StorageException_Exception, InvalidCredentialsException_Exception, InvalidDataException_Exception, RemoteException_Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean exists(final Context ctx, final Credentials auth) throws StorageException_Exception, InvalidCredentialsException_Exception, InvalidDataException_Exception, RemoteException_Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Context> listByDatabase(final Database db, final Credentials auth) throws StorageException_Exception, InvalidCredentialsException_Exception, InvalidDataException_Exception, RemoteException_Exception, NoSuchDatabaseException_Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Context getData(final Context ctx, final Credentials auth) throws StorageException_Exception, InvalidCredentialsException_Exception, InvalidDataException_Exception, NoSuchContextException_Exception, RemoteException_Exception {
        // TODO Auto-generated method stub
        return null;
    }

    private static com.openexchange.admin.rmi.dataobjects.Credentials soap2Credentials(final Credentials soapCredentials) {
        if (null == soapCredentials) {
            return null;
        }
        final com.openexchange.admin.rmi.dataobjects.Credentials ret = new com.openexchange.admin.rmi.dataobjects.Credentials();
        ret.setLogin(soapCredentials.getLogin().getValue());
        ret.setPassword(soapCredentials.getPassword().getValue());
        return ret;
    }

    private static com.openexchange.admin.rmi.dataobjects.Context soap2Context(final Context soapContext) {
        if (null == soapContext) {
            return null;
        }
        final com.openexchange.admin.rmi.dataobjects.Context ret = new com.openexchange.admin.rmi.dataobjects.Context();
        ret.setAverage_size(soapContext.getAverageSize().getValue());
        ret.setEnabled(soapContext.getEnabled().getValue());
        ret.setFilestore_name(soapContext.getFilestoreName().getValue());
        ret.setFilestoreId(soapContext.getFilestoreId().getValue());
        ret.setId(soapContext.getId().getValue());
        ret.setLoginMappings(new HashSet<String>(soapContext.getLoginMappings()));
        ret.setMaxQuota(soapContext.getMaxQuota().getValue());
        ret.setName(soapContext.getName().getValue());
        ret.setUsedQuota(soapContext.getUsedQuota().getValue());
        ret.setReadDatabase(soap2Database(soapContext.getReadDatabase().getValue()));
        ret.setWriteDatabase(soap2Database(soapContext.getWriteDatabase().getValue()));
        ret.setUserAttributes(soap2MapMap(soapContext.getUserAttributes().getValue()));
        return ret;
    }

    private static com.openexchange.admin.rmi.dataobjects.Database soap2Database(final Database soapDatabase) {
        if (null == soapDatabase) {
            return null;
        }
        final com.openexchange.admin.rmi.dataobjects.Database ret = new com.openexchange.admin.rmi.dataobjects.Database();
        ret.setClusterWeight(soapDatabase.getClusterWeight().getValue());
        ret.setCurrentUnits(soapDatabase.getCurrentUnits().getValue());
        ret.setDriver(soapDatabase.getDriver().getValue());
        ret.setId(soapDatabase.getId().getValue());
        ret.setLogin(soapDatabase.getLogin().getValue());
        ret.setMaster(soapDatabase.getMaster().getValue());
        ret.setMasterId(soapDatabase.getMasterId().getValue());
        ret.setMaxUnits(soapDatabase.getMaxUnits().getValue());
        ret.setName(soapDatabase.getName().getValue());
        ret.setPassword(soapDatabase.getPassword().getValue());
        ret.setPoolHardLimit(soapDatabase.getPoolHardLimit().getValue());
        ret.setPoolInitial(soapDatabase.getPoolInitial().getValue());
        ret.setPoolMax(soapDatabase.getPoolMax().getValue());
        ret.setRead_id(soapDatabase.getReadId().getValue());
        ret.setScheme(soapDatabase.getScheme().getValue());
        ret.setUrl(soapDatabase.getUrl().getValue());
        return ret;
    }

    private static Map<String, Map<String, String>> soap2MapMap(final SOAPStringMapMap soapStringMapMap) {
        if (null == soapStringMapMap) {
            return null;
        }
        final List<SOAPMapEntry> entries = soapStringMapMap.getEntries();
        final Map<String, Map<String, String>> map = new HashMap<String, Map<String,String>>(entries.size());
        for (final SOAPMapEntry soapMapEntry : entries) {
            map.put(soapMapEntry.getKey().getValue(), soap2Map(soapMapEntry.getValue().getValue()));
        }
        return map;
    }

    private static Map<String, String> soap2Map(final SOAPStringMap soapStringMap) {
        if (null == soapStringMap) {
            return null;
        }
        final List<Entry> entries = soapStringMap.getEntries();
        final Map<String, String> map = new HashMap<String, String>(entries.size());
        for (final Entry entry : entries) {
            map.put(entry.getKey().getValue(), entry.getValue().getValue());
        }
        return map;
    }
}
