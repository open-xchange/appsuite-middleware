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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.rest.services.users.mailmapping;

import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.mailmapping.MailResolver;
import com.openexchange.mailmapping.ResolvedMail;
import com.openexchange.server.MockingServiceLookup;
import com.openexchange.user.UserService;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


/**
 * {@link MailMappingServiceTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MailMappingServiceTest {
    
    private MailMappingService service = null;
    private MailResolver resolver = null;
    private MockingServiceLookup services = null;
    
    @Before
    public void setup() throws OXException {
        service = new MailMappingService();
        resolver = mock(MailResolver.class);
        services = new MockingServiceLookup();
        
        service.setContext(resolver);
        service.setServices(services);
        
        mockContext();
    }
    
    private void mockContext() throws OXException {
        ContextService contexts = services.mock(ContextService.class);
        
        Context ctx = mock(Context.class);
        when(contexts.getContext(42)).thenReturn(ctx);
    
        User charlie = mock(User.class);
        when(charlie.getPreferredLanguage()).thenReturn("en_US");
        when(charlie.getDisplayName()).thenReturn("Charlie");
        
        User linus = mock(User.class);
        when(linus.getPreferredLanguage()).thenReturn("de_DE");
        when(linus.getPreferredLanguage()).thenReturn("Linus");
        
        UserService users = services.mock(UserService.class);
        when(users.getUser(12, ctx)).thenReturn(charlie);
        when(users.getUser(13, ctx)).thenReturn(linus);
        
    }

    @Test
    public void testResolveMail() throws OXException {
        when(resolver.resolve("charlie@test.invalid")).thenReturn(new ResolvedMail(12, 42));
        
        Map<String, Object> resolved = (Map<String, Object>) service.resolve("charlie@test.invalid");
        assertEquals(1, resolved.size());
        
        Map<String, Object> resolvedEntry = (Map<String, Object>) resolved.get("charlie@test.invalid");
        assertEquals(12, resolvedEntry.get("uid"));
        assertEquals(42, resolvedEntry.get("cid"));
        
        Map<String, String> user = (Map<String, String>) resolvedEntry.get("user");
        assertEquals("en_US", user.get("language"));
        assertEquals("Charlie", user.get("displayName"));
        
    }
    
    @Test
    public void testResolveMultipleMails() throws OXException {
        when(resolver.resolve("charlie@test.invalid")).thenReturn(new ResolvedMail(12, 42));
        when(resolver.resolve("linus@test.invalid")).thenReturn(new ResolvedMail(13, 42));
        
        Map<String, Object> resolved = (Map<String, Object>) service.resolve("charlie@test.invalid;linus@test.invalid");
        assertEquals(2, resolved.size());
        
        Map<String, Object> charlie = (Map<String, Object>) resolved.get("charlie@test.invalid");
        assertEquals(12, charlie.get("uid"));
        assertEquals(42, charlie.get("cid"));

        Map<String, Object> linus = (Map<String, Object>) resolved.get("linus@test.invalid");
        assertEquals(13, linus.get("uid"));
        assertEquals(42, linus.get("cid"));

    }
    
    @Test
    public void testResolveUnknownMail() throws OXException {
        Map<String, Object> resolved = (Map<String, Object>) service.resolve("unknown@test.invalid");
        assertTrue(resolved.isEmpty());
    }
}
