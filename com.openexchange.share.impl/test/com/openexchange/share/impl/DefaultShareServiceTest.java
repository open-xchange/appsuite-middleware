///*
// *
// *    OPEN-XCHANGE legal information
// *
// *    All intellectual property rights in the Software are protected by
// *    international copyright laws.
// *
// *
// *    In some countries OX, OX Open-Xchange, open xchange and OXtender
// *    as well as the corresponding Logos OX Open-Xchange and OX are registered
// *    trademarks of the OX Software GmbH group of companies.
// *    The use of the Logos is not covered by the GNU General Public License.
// *    Instead, you are allowed to use these Logos according to the terms and
// *    conditions of the Creative Commons License, Version 2.5, Attribution,
// *    Non-commercial, ShareAlike, and the interpretation of the term
// *    Non-commercial applicable to the aforementioned license is published
// *    on the web site http://www.open-xchange.com/EN/legal/index.html.
// *
// *    Please make sure that third-party modules and libraries are used
// *    according to their respective licenses.
// *
// *    Any modifications to this package must retain all copyright notices
// *    of the original copyright holder(s) for the original code used.
// *
// *    After any such modifications, the original and derivative code shall remain
// *    under the copyright of the copyright holder(s) and/or original author(s)per
// *    the Attribution and Assignment Agreement that can be located at
// *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
// *    given Attribution for the derivative code and a license granting use.
// *
// *     Copyright (C) 2016-2020 OX Software GmbH
// *     Mail: info@open-xchange.com
// *
// *
// *     This program is free software; you can redistribute it and/or modify it
// *     under the terms of the GNU General Public License, Version 2 as published
// *     by the Free Software Foundation.
// *
// *     This program is distributed in the hope that it will be useful, but
// *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
// *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
// *     for more details.
// *
// *     You should have received a copy of the GNU General Public License along
// *     with this program; if not, write to the Free Software Foundation, Inc., 59
// *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
// *
// */
//
//package com.openexchange.share.impl;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNull;
//import static org.junit.Assert.assertTrue;
//import java.sql.Connection;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//import org.junit.Before;
//import org.junit.Test;
//import org.mockito.Mockito;
//import com.openexchange.contact.storage.ContactUserStorage;
//import com.openexchange.database.DatabaseService;
//import com.openexchange.groupware.container.FolderObject;
//import com.openexchange.groupware.contexts.Context;
//import com.openexchange.java.util.UUIDs;
//import com.openexchange.server.ServiceLookup;
//import com.openexchange.session.Session;
//import com.openexchange.share.AuthenticationMode;

//import com.openexchange.share.Share;
//import com.openexchange.share.ShareTarget;
//import com.openexchange.share.storage.ShareStorage;
//import com.openexchange.user.UserService;
//import com.openexchange.userconf.UserPermissionService;
//
//
///**
// * {@link DefaultShareServiceTest}
// *
// * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
// * @since v7.8.0
// */
//public class DefaultShareServiceTest implements ServiceLookup {
//
//    private static final int CONTEXT_ID = 1;
//
//    private static final int USER_ID = 1;
//
//    private Map<Class<?>, Object> services;
//
//    private DefaultShareService shareService;
//
//    private SimShareStorage shareStorage;
//
//    private Session session;
//
//    private Share s1;
//
//    private Share s2;
//
//    private ShareTarget t1;
//
//    private ShareTarget t2;
//
//    private ShareTarget t3;
//
//    @Before
//    public void setUp() throws Exception {
//        shareService = new DefaultShareService(this);
//        session = Mockito.mock(Session.class);
//        Mockito.when(session.getContextId()).thenReturn(CONTEXT_ID);
//        Mockito.when(session.getUserId()).thenReturn(USER_ID);
//
//        services = new HashMap<Class<?>, Object>();
//        services.put(DatabaseService.class, Mockito.mock(DatabaseService.class, Mockito.RETURNS_DEEP_STUBS));
//        shareStorage = new SimShareStorage();
//        services.put(ShareStorage.class, shareStorage);
//        services.put(UserService.class, Mockito.mock(UserService.class, Mockito.RETURNS_DEEP_STUBS));
//        services.put(UserPermissionService.class, Mockito.mock(UserPermissionService.class, Mockito.RETURNS_DEEP_STUBS));
//        services.put(ContactUserStorage.class, Mockito.mock(ContactUserStorage.class, Mockito.RETURNS_DEEP_STUBS));
//
//        List<ShareTarget> targets = new ArrayList<ShareTarget>(3);
//        t1 = new ShareTarget(FolderObject.INFOSTORE, "1");
//        t2 = new ShareTarget(FolderObject.INFOSTORE, "2");
//        t3 = new ShareTarget(FolderObject.INFOSTORE, "3");
//        targets.add(t1);
//        targets.add(t2);
//        targets.add(t3);
//        s1 = createShare(2, targets);
//
//        targets = new ArrayList<ShareTarget>(targets);
//        s2 = createShare(3, targets);
//
//        shareStorage.storeShare(s1, null);
//        shareStorage.storeShare(s2, null);
//    }
//
//    @Test
//    public void testTargetsAreRemovedFromShares() throws Exception {
//        List<Share> toDelete = new ArrayList<Share>(2);
//        toDelete.add(new Share(t1.getModule(), t1.getFolder()));
//        toDelete.add(new Share(t2.getModule(), t2.getFolder()));
//        shareService.deleteTargets(session, toDelete, Collections.singletonList(2));
//        ShareList reloaded = shareStorage.loadShare(CONTEXT_ID, s1.getToken(), null);
//        assertEquals(1, reloaded.getTargets().size());
//        Share t = reloaded.getTargets().get(0);
//        assertTrue(t3.getModule() == t.getModule() && t3.getFolder().equals(t.getFolder()));
//    }
//
//    @Test
//    public void testShareAndGuestAreRemovedWhenTargetsAreEmpty() throws Exception {
//        List<Share> toDelete = new ArrayList<Share>(2);
//        toDelete.add(new Share(t1.getModule(), t1.getFolder()));
//        toDelete.add(new Share(t2.getModule(), t2.getFolder()));
//        toDelete.add(new Share(t3.getModule(), t3.getFolder()));
//        shareService.deleteTargets(session, toDelete, Collections.singletonList(2));
//        assertNull(shareStorage.loadShare(CONTEXT_ID, s1.getToken(), null));
//        Mockito.verify(getService(UserService.class)).deleteUser(Mockito.any(Connection.class), Mockito.any(Context.class), Mockito.eq(s1.getGuest()));
//        Mockito.verify(getService(UserPermissionService.class)).deleteUserPermissionBits(Mockito.any(Connection.class), Mockito.any(Context.class), Mockito.eq(s1.getGuest()));
//        Mockito.verify(getService(ContactUserStorage.class)).deleteGuestContact(Mockito.eq(CONTEXT_ID), Mockito.anyInt(), Mockito.any(Date.class), Mockito.any(Connection.class));
//    }
//
//    @Test
//    public void testTargetDeletionForAllSharesIfNoGuestsAreSpecified() throws Exception {
//        List<Share> toDelete = new ArrayList<Share>(2);
//        toDelete.add(new Share(t1.getModule(), t1.getFolder()));
//        toDelete.add(new Share(t2.getModule(), t2.getFolder()));
//        shareService.deleteTargets(session, toDelete, Collections.<Integer>emptyList());
//        List<ShareList> shares = shareStorage.loadSharesForContext(CONTEXT_ID, null);
//        assertEquals(2, shares.size());
//        ShareList rs1 = shares.get(0);
//        assertEquals(1, rs1.getTargets().size());
//        ShareList rs2 = shares.get(1);
//        assertEquals(1, rs2.getTargets().size());
//        Share rt1 = rs1.getTargets().get(0);
//        assertTrue(t3.getModule() == rt1.getModule() && t3.getFolder().equals(rt1.getFolder()));
//        Share rt2 = rs2.getTargets().get(0);
//        assertTrue(t3.getModule() == rt2.getModule() && t3.getFolder().equals(rt2.getFolder()));
//    }
//
//    @Override
//    public <S> S getService(Class<? extends S> clazz) {
//        return (S) services.get(clazz);
//    }
//
//    @Override
//    public <S> S getOptionalService(Class<? extends S> clazz) {
//        return (S) services.get(clazz);
//    }
//
//    private static Share createShare(int guestID, List<ShareTarget> targets) {
//        Share share = new Share();
//        share.setAuthentication(AuthenticationMode.GUEST_PASSWORD);
//        share.setContextID(CONTEXT_ID);
//        share.setCreated(new Date());
//        share.setCreatedBy(USER_ID);
//        share.setGuest(guestID);
//        share.setLastModified(share.getLastModified());
//        share.setModifiedBy(share.getCreatedBy());
//        share.setToken(UUIDs.getUnformattedString(UUID.randomUUID()));
//        share.setTargets(targets);
//        return share;
//    }
//
//}
