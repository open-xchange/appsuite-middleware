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
import com.openexchange.http.grizzly.eas.EASCommandCodes.EASCommands;

/**
 * {@link RequestToolsTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.2
 */
public class RequestToolsTest {

    private static String SYNC = EASCommands.SYNC.getCommandName();

    private static String PING = EASCommands.PING.getCommandName();

    private static String SYNC_UPDATE_PATH = "/syncUpdate";

    private static String PING_PATH = "/Ping";

    @Mock
    private HttpServletRequest servletRequest;

    @Before
    public void setUp() throws Exception {
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
