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
package com.openexchange.admin.reseller.rmi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.HashSet;

import org.junit.Test;

import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.reseller.rmi.exceptions.OXResellerException;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class OXResellerInterfaceTest extends OXResellerAbstractTest {

    @Test
    public void testCreate() throws MalformedURLException, RemoteException, NotBoundException, InvalidDataException, InvalidCredentialsException, StorageException, OXResellerException {
        final Credentials creds = DummyMasterCredentials();

        final OXResellerInterface oxresell = (OXResellerInterface)Naming.lookup(getRMIHostUrl() + OXResellerInterface.RMI_NAME);
        
        ResellerAdmin adm = oxresell.create(TestAdminUser(), creds);
        ResellerAdmin admch = oxresell.create(TestAdminUser(TESTCHANGEUSER,"Test Change User"), creds);

        System.out.println(adm);
        
        assertNotNull(adm);
        assertNotNull(admch);
        assertTrue(adm.getId() > 0);
        assertTrue(admch.getId() > 0);
    }

    @Test
    public void testCreateWithRestrictions() throws MalformedURLException, RemoteException, NotBoundException, InvalidDataException, InvalidCredentialsException, StorageException, OXResellerException {
        final Credentials creds = DummyMasterCredentials();

        final OXResellerInterface oxresell = (OXResellerInterface)Naming.lookup(getRMIHostUrl() + OXResellerInterface.RMI_NAME);

        ResellerAdmin adm = TestAdminUser(TESTRESTRICTIONUSER,"Test Restriction User");
        HashSet<Restriction> res = new HashSet<Restriction>();
        res.add(MaxContextRestriction());
        res.add(MaxContextQuotaRestriction());
        adm.setRestrictions(res);
        adm = oxresell.create(adm, creds);

        System.out.println(adm);
        
        assertNotNull(adm);
        assertTrue(adm.getId() > 0);
    }

    @Test
    public void testChange() throws MalformedURLException, RemoteException, NotBoundException, InvalidDataException, StorageException, OXResellerException, InvalidCredentialsException {
        final Credentials creds = DummyMasterCredentials();

        final OXResellerInterface oxresell = (OXResellerInterface)Naming.lookup(getRMIHostUrl() + OXResellerInterface.RMI_NAME);
        
        ResellerAdmin adm = new ResellerAdmin(TESTCHANGEUSER);
        final String newdisp = "New Display name";
        adm.setDisplayname(newdisp);
        
        oxresell.change(adm, creds);
        
        ResellerAdmin chadm = oxresell.getData(new ResellerAdmin(TESTCHANGEUSER), creds);
        
        assertEquals(adm.getDisplayname(), chadm.getDisplayname());
    }

    @Test
    public void testChangeName() throws MalformedURLException, RemoteException, NotBoundException, InvalidDataException, StorageException, OXResellerException, InvalidCredentialsException {
        final Credentials creds = DummyMasterCredentials();

        final OXResellerInterface oxresell = (OXResellerInterface)Naming.lookup(getRMIHostUrl() + OXResellerInterface.RMI_NAME);
        
        
        ResellerAdmin adm = oxresell.getData(new ResellerAdmin(TESTCHANGEUSER), creds);
        adm.setName(CHANGEDNAME);
        oxresell.change(adm, creds);
        ResellerAdmin newadm = new ResellerAdmin();
        newadm.setId(adm.getId());
        ResellerAdmin chadm = oxresell.getData(newadm, creds);
        assertEquals(adm.getName(), chadm.getName());
    }

    @Test
    public void testGetData() throws MalformedURLException, RemoteException, NotBoundException, InvalidDataException, InvalidCredentialsException, StorageException, PoolException, SQLException, OXResellerException {
        final Credentials creds = DummyMasterCredentials();
        final ResellerAdmin adm = TestAdminUser();
        
        final OXResellerInterface oxresell = (OXResellerInterface)Naming.lookup(getRMIHostUrl() + OXResellerInterface.RMI_NAME);
        
        final ResellerAdmin dbadm = oxresell.getData(new ResellerAdmin(TESTUSER), creds);
        
        assertEquals(adm.getName(), dbadm.getName());
        assertEquals(adm.getDisplayname(), dbadm.getDisplayname());
    }
    
    @Test
    public void testGetDataWithRestrictions() throws MalformedURLException, RemoteException, NotBoundException, InvalidDataException, InvalidCredentialsException, StorageException, PoolException, SQLException, OXResellerException {
        final Credentials creds = DummyMasterCredentials();
        final ResellerAdmin adm = TestAdminUser(TESTRESTRICTIONUSER,"Test Restriction User");
        
        final OXResellerInterface oxresell = (OXResellerInterface)Naming.lookup(getRMIHostUrl() + OXResellerInterface.RMI_NAME);
        
        final ResellerAdmin dbadm = oxresell.getData(adm, creds);
        
        HashSet<Restriction> res = dbadm.getRestrictions();
        assertNotNull(res);
        assertTrue(res.contains(MaxContextRestriction()));
        assertEquals(adm.getName(), dbadm.getName());
        assertEquals(adm.getDisplayname(), dbadm.getDisplayname());
    }

    @Test
    public void testList() throws MalformedURLException, RemoteException, NotBoundException, InvalidDataException, StorageException, InvalidCredentialsException {
        final Credentials creds = DummyMasterCredentials();
        final OXResellerInterface oxresell = (OXResellerInterface)Naming.lookup(getRMIHostUrl() + OXResellerInterface.RMI_NAME);
        
        ResellerAdmin[] res = oxresell.list("*", creds);
        for(final ResellerAdmin adm : res) {
            System.out.println(adm);
        }
        assertEquals(3, res.length);
    }

    @Test
    public void testDelete() throws MalformedURLException, RemoteException, NotBoundException, InvalidDataException, StorageException, OXResellerException, InvalidCredentialsException {
        final Credentials creds = DummyMasterCredentials();

        final OXResellerInterface oxresell = (OXResellerInterface)Naming.lookup(getRMIHostUrl() + OXResellerInterface.RMI_NAME);

        oxresell.delete(TestAdminUser(), creds);
        oxresell.delete(new ResellerAdmin(CHANGEDNAME), creds);
        oxresell.delete(TestAdminUser(TESTRESTRICTIONUSER,"Test Restriction User"), creds);
    }


}
