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

package com.openexchange.folderstorage.internal.performers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.test.mock.MockFactory;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link UserSharedFoldersPerformerTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class UserSharedFoldersPerformerTest {

    private UserSharedFoldersPerformer performer;

    private ServerSession session = MockFactory.getServerSession();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        performer = new UserSharedFoldersPerformer(session, null);
    }

    @After
    public void tearDown() throws Exception {}

    @Test
    public void testCallAndWait_interrupted_getInterruptError() throws InterruptedException {
        CompletionService<Object> completionService = Mockito.mock(CompletionService.class);
        Mockito.when(completionService.take()).thenThrow(InterruptedException.class);

        try {
            performer.callAndWait(completionService, 1);
            fail("No exception occurred!");
        } catch (OXException e) {
            assertTrue(FolderExceptionErrorMessage.INTERRUPT_ERROR.equals(e));
            assertFalse(FolderExceptionErrorMessage.UNEXPECTED_ERROR.equals(e));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCallAndWait_illegalArgumentException_getIAE() throws InterruptedException, OXException {
        CompletionService<Object> completionService = Mockito.mock(CompletionService.class);
        Mockito.when(completionService.take()).thenThrow(IllegalArgumentException.class);

        performer.callAndWait(completionService, 1);
        fail("No exception occurred!");
    }

    @Test
    public void testCallAndWait_executionExceptionWithoutCause_getUnexpectedError() throws InterruptedException {
        CompletionService<Object> completionService = Mockito.mock(CompletionService.class);
        Mockito.when(completionService.take()).thenThrow(ExecutionException.class);

        try {
            performer.callAndWait(completionService, 1);
            fail("No exception occurred!");
        } catch (OXException e) {
            assertFalse(FolderExceptionErrorMessage.INTERRUPT_ERROR.equals(e));
            assertTrue(FolderExceptionErrorMessage.UNEXPECTED_ERROR.equals(e));
        }
    }

    @Test
    public void testCallAndWait_executionException_getUnexpectedError() throws InterruptedException, ExecutionException {
        CompletionService<Object> completionService = Mockito.mock(CompletionService.class);
        ExecutionException executionException = Mockito.mock(ExecutionException.class);
        Mockito.when(executionException.getCause()).thenReturn(new IllegalArgumentException());
        java.util.concurrent.Future<Object> future = (java.util.concurrent.Future<Object>) Mockito.mock(java.util.concurrent.Future.class);
        Mockito.when(future.get()).thenThrow(executionException);
        Mockito.when(completionService.take()).thenReturn((java.util.concurrent.Future<Object>) future);

        try {

            performer.callAndWait(completionService, 1);
            fail("No exception occurred!");
        } catch (OXException e) {
            assertFalse(FolderExceptionErrorMessage.INTERRUPT_ERROR.equals(e));
            assertTrue(FolderExceptionErrorMessage.UNEXPECTED_ERROR.equals(e));
        }
    }

}
