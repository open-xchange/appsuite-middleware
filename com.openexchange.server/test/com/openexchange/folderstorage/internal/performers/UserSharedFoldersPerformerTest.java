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
    public void tearDown() {}

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
        Mockito.when(completionService.take()).thenThrow(InterruptedException.class);

        try {
            performer.callAndWait(completionService, 1);
            fail("No exception occurred!");
        } catch (OXException e) {
            assertTrue(FolderExceptionErrorMessage.INTERRUPT_ERROR.equals(e));
            assertFalse(FolderExceptionErrorMessage.UNEXPECTED_ERROR.equals(e));
        }
    }

    @Test
    public void testCallAndWait_executionException_getUnexpectedError() throws InterruptedException, ExecutionException {
        CompletionService<Object> completionService = Mockito.mock(CompletionService.class);
        ExecutionException executionException = Mockito.mock(ExecutionException.class);
        Mockito.when(executionException.getCause()).thenReturn(new IllegalArgumentException());
        java.util.concurrent.Future<Object> future = Mockito.mock(java.util.concurrent.Future.class);
        Mockito.when(future.get()).thenThrow(executionException);
        Mockito.when(completionService.take()).thenReturn(future);

        try {

            performer.callAndWait(completionService, 1);
            fail("No exception occurred!");
        } catch (OXException e) {
            assertFalse(FolderExceptionErrorMessage.INTERRUPT_ERROR.equals(e));
            assertTrue(FolderExceptionErrorMessage.UNEXPECTED_ERROR.equals(e));
        }
    }

}
