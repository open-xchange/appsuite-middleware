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

package com.openexchange.http.grizzly.util;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.servlet.http.HttpServletRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import com.openexchange.http.grizzly.eas.EASCommandCodes;

/**
 * {@link RequestToolsTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.2
 */
public class RequestToolsTest {

    private static String SYNC = EASCommandCodes.SYNC.getCommandName().toLowerCase();

    private static String PING = EASCommandCodes.PING.getCommandName().toLowerCase();

    private static String SYNC_UPDATE_PATH = "/syncUpdate".toLowerCase();

    private static String PING_PATH = "/Ping".toLowerCase();

    @Mock
    private HttpServletRequest servletRequest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        RequestTools.USM_PATH_CACHE.invalidateAll();
    }

     @Test
     public void testIsIgnoredEasRequest_noEASRequest_ReturnFalse() {
        Mockito.when(servletRequest.getRequestURI()).thenReturn("/No_EAS_Request");

        Set<String> ignoredEasCommands = new CopyOnWriteArraySet<String>(Arrays.asList(SYNC, PING));

        boolean ignoredEasRequest = RequestTools.isIgnoredEasRequest(servletRequest, ignoredEasCommands);
        Assert.assertFalse("Non EAS request should return false for definition of 'ignore eas request'", ignoredEasRequest);
    }

     @Test
     public void testIsIgnoredEasRequest_syncCommandThatShouldBeIgnored_ReturnTrue() {
        Mockito.when(servletRequest.getRequestURI()).thenReturn(RequestTools.EAS_URI_DEFAULT);
        Mockito.when(servletRequest.getParameter(RequestTools.EAS_CMD)).thenReturn("Sync");

        Set<String> ignoredEasCommands = new CopyOnWriteArraySet<String>(Arrays.asList(SYNC, PING));

        boolean ignoredEasRequest = RequestTools.isIgnoredEasRequest(servletRequest, ignoredEasCommands);
        Assert.assertTrue("Given command is defined as beeing ignored!", ignoredEasRequest);
    }

     @Test
     public void testIsIgnoredEasRequest_syncCommandThatShouldNotBeIgnored_ReturnFalse() {
        Mockito.when(servletRequest.getRequestURI()).thenReturn(RequestTools.EAS_URI_DEFAULT);
        Mockito.when(servletRequest.getParameter(RequestTools.EAS_CMD)).thenReturn("Sync");

        Set<String> ignoredEasCommands = new CopyOnWriteArraySet<String>(Arrays.asList(PING));

        boolean ignoredEasRequest = RequestTools.isIgnoredEasRequest(servletRequest, ignoredEasCommands);
        Assert.assertFalse("Given command is defined as not beeing ignored but was not ignored!", ignoredEasRequest);
    }

     @Test
     public void testIsIgnoredEasRequest_easRequestWithUnknownAction_ReturnFalse() {
        Mockito.when(servletRequest.getRequestURI()).thenReturn(RequestTools.EAS_URI_DEFAULT);
        Mockito.when(servletRequest.getParameter(RequestTools.EAS_CMD)).thenReturn("thisIsNotAValidRequest");

        Set<String> ignoredEasCommands = new CopyOnWriteArraySet<String>(Arrays.asList(PING));

        boolean ignoredEasRequest = RequestTools.isIgnoredEasRequest(servletRequest, ignoredEasCommands);
        Assert.assertFalse("Given command is defined as not beeing ignored but was not ignored!", ignoredEasRequest);
    }

     @Test
     public void testIsIgnoredEasRequest_base64encodedSyncCommandThatShouldNotBeIngored_returnFalse() {
        Mockito.when(servletRequest.getRequestURI()).thenReturn(RequestTools.EAS_URI_DEFAULT);
        Mockito.when(servletRequest.getQueryString()).thenReturn("jAAJBAp2MTQwRGV2aWNlAApTbWFydFBob25l");

        Set<String> ignoredEasCommands = new CopyOnWriteArraySet<String>(Arrays.asList(PING));

        boolean ignoredEasRequest = RequestTools.isIgnoredEasRequest(servletRequest, ignoredEasCommands);
        Assert.assertFalse(ignoredEasRequest);
    }

     @Test
     public void testIsIgnoredEasRequest_base64encodedSyncCommandThatsShouldBeIgnored_returnTrue() {
        Mockito.when(servletRequest.getRequestURI()).thenReturn(RequestTools.EAS_URI_DEFAULT);
        Mockito.when(servletRequest.getQueryString()).thenReturn("jAAJBAp2MTQwRGV2aWNlAApTbWFydFBob25l");

        Set<String> ignoredEasCommands = new CopyOnWriteArraySet<String>(Arrays.asList(SYNC));

        boolean ignoredEasRequest = RequestTools.isIgnoredEasRequest(servletRequest, ignoredEasCommands);
        Assert.assertTrue(ignoredEasRequest);
    }

     @Test
     public void testIsIgnoredEasRequest_base64encodedSyncWithoutARelationToBase64EncodedRequest_returnFalse() {
        Mockito.when(servletRequest.getRequestURI()).thenReturn(RequestTools.EAS_URI_DEFAULT);
        Mockito.when(servletRequest.getQueryString()).thenReturn("ZGZhZHNmYWRzZHNmYWRzZmRmYWRz");

        Set<String> ignoredEasCommands = new CopyOnWriteArraySet<String>(Arrays.asList(SYNC));

        boolean ignoredEasRequest = RequestTools.isIgnoredEasRequest(servletRequest, ignoredEasCommands);
        Assert.assertFalse(ignoredEasRequest);
    }

     @Test
     public void testIsIgnoredUSMRequest_noUsmRequest_ReturnFalse() {
        Mockito.when(servletRequest.getRequestURI()).thenReturn("/No_USM_Request");

        Set<String> ignoredUsmCommands = new CopyOnWriteArraySet<String>(Arrays.asList(SYNC_UPDATE_PATH));

        boolean ignoredUsmRequest = RequestTools.isIgnoredUsmRequest(servletRequest, ignoredUsmCommands);
        Assert.assertFalse("Non USM request should return false for definition of 'ignore usm request'", ignoredUsmRequest);
    }

     @Test
     public void testIsIgnoredUsmRequest_syncCommandThatShouldBeIgnored_ReturnTrue() {
        Mockito.when(servletRequest.getRequestURI()).thenReturn(RequestTools.USM_URI_DEFAULT);
        Mockito.when(servletRequest.getPathInfo()).thenReturn(SYNC_UPDATE_PATH);

        Set<String> ignoredUsmCommands = new CopyOnWriteArraySet<String>(Arrays.asList(SYNC_UPDATE_PATH, PING_PATH));

        boolean ignoredUsmRequest = RequestTools.isIgnoredUsmRequest(servletRequest, ignoredUsmCommands);
        Assert.assertTrue("Given command is defined as beeing ignored but was not!", ignoredUsmRequest);
    }

     @Test
     public void testIsIgnoredUsmRequest_syncCommandThatShouldNotBeIgnored_ReturnFalse() {
        Mockito.when(servletRequest.getRequestURI()).thenReturn(RequestTools.USM_URI_DEFAULT);
        Mockito.when(servletRequest.getPathInfo()).thenReturn(SYNC_UPDATE_PATH);

        Set<String> ignoredUsmCommands = new CopyOnWriteArraySet<String>(Arrays.asList(PING_PATH));

        boolean ignoredUsmRequest = RequestTools.isIgnoredUsmRequest(servletRequest, ignoredUsmCommands);
        Assert.assertFalse("Given command is defined as not beeing ignored but was not ignored!", ignoredUsmRequest);
    }

     @Test
     public void testIsIgnoredUsmRequest_usmRequestWithUnknownPath_ReturnFalse() {
        Mockito.when(servletRequest.getRequestURI()).thenReturn(RequestTools.USM_URI_DEFAULT);
        Mockito.when(servletRequest.getPathInfo()).thenReturn("/thisIsNotAValidPath");

        Set<String> ignoredUsmCommands = new CopyOnWriteArraySet<String>(Arrays.asList(SYNC_UPDATE_PATH, PING_PATH));

        boolean ignoredUsmRequest = RequestTools.isIgnoredUsmRequest(servletRequest, ignoredUsmCommands);
        Assert.assertFalse("Given command is defined as not beeing ignored but was not ignored!", ignoredUsmRequest);
    }

     @Test
     public void testIsIgnoredUsmRequest_pathIsNull_ReturnFalse() {
        Mockito.when(servletRequest.getRequestURI()).thenReturn(RequestTools.USM_URI_DEFAULT);
        Mockito.when(servletRequest.getPathInfo()).thenReturn(null);

        Set<String> ignoredUsmCommands = new CopyOnWriteArraySet<String>(Arrays.asList(SYNC_UPDATE_PATH, PING_PATH));

        boolean ignoredUsmRequest = RequestTools.isIgnoredUsmRequest(servletRequest, ignoredUsmCommands);
        Assert.assertFalse("Given command is defined as not beeing ignored but was not ignored!", ignoredUsmRequest);
    }

     @Test
     public void testIsIgnoredUsmRequest_cacheUsedToReturnValue_returnTrue() {
        Mockito.when(servletRequest.getRequestURI()).thenReturn(RequestTools.USM_URI_DEFAULT);
        Mockito.when(servletRequest.getPathInfo()).thenReturn(SYNC_UPDATE_PATH);

        Set<String> ignoredUsmCommands = new CopyOnWriteArraySet<String>(Arrays.asList(SYNC_UPDATE_PATH));

        boolean ignoredUsmRequest = RequestTools.isIgnoredUsmRequest(servletRequest, ignoredUsmCommands);
        Assert.assertTrue("Given command is defined as not beeing ignored but was not ignored!", ignoredUsmRequest);

        boolean ignoredUsmRequest2 = RequestTools.isIgnoredUsmRequest(servletRequest, ignoredUsmCommands);
        Assert.assertTrue("Given command is defined as not beeing ignored but was not ignored!", ignoredUsmRequest2);

        Assert.assertEquals("Received wrong cache size. Should only contain one path", 1, RequestTools.USM_PATH_CACHE.size());
    }

     @Test
     public void testIsIgnoredUsmRequest_cacheUsedForTwoPaths_returnTrue() {
        Mockito.when(servletRequest.getRequestURI()).thenReturn(RequestTools.USM_URI_DEFAULT);
        Mockito.when(servletRequest.getPathInfo()).thenReturn(SYNC_UPDATE_PATH, PING_PATH);

        Set<String> ignoredUsmCommands = new CopyOnWriteArraySet<String>(Arrays.asList(SYNC_UPDATE_PATH, PING_PATH));

        boolean ignoredUsmRequest = RequestTools.isIgnoredUsmRequest(servletRequest, ignoredUsmCommands);
        Assert.assertTrue("Given command is defined as not beeing ignored but was not ignored!", ignoredUsmRequest);

        boolean ignoredUsmRequest2 = RequestTools.isIgnoredUsmRequest(servletRequest, ignoredUsmCommands);
        Assert.assertTrue("Given command is defined as not beeing ignored but was not ignored!", ignoredUsmRequest2);

        Assert.assertEquals("Received wrong cache size. Should only contain one path", 2, RequestTools.USM_PATH_CACHE.size());
        Assert.assertNotNull("Cannot find path for syncupdate in cache", RequestTools.USM_PATH_CACHE.getIfPresent(SYNC_UPDATE_PATH.toLowerCase()));
        Assert.assertNotNull("Cannot find path for ping in cache", RequestTools.USM_PATH_CACHE.getIfPresent(PING_PATH.toLowerCase()));
    }
}
