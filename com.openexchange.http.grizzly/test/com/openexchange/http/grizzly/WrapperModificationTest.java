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

package com.openexchange.http.grizzly;

import static org.junit.Assert.assertTrue;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.util.DataChunk;
import org.glassfish.grizzly.servlet.HttpServletRequestImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.http.grizzly.http.servlet.HttpServletRequestWrapper;

/**
 * {@link WrapperModificationTest} Concurrent Modification can have to be synchronized Bug #41581
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since 7.8.1
 */
public class WrapperModificationTest {

    private ExecutorService executorService;

    @Before
    public void setUp() throws Exception {
        executorService = Executors.newFixedThreadPool(10);
    }

    @After
    public void tearDown() throws Exception {
        executorService.shutdownNow();
    }

    @Test
    public void test() throws Exception {
        HttpRequestPacket hrp = HttpRequestPacket.builder().build();
        Field remoteAddrC = HttpRequestPacket.class.getDeclaredField("remoteAddressC");
        remoteAddrC.setAccessible(true);
        DataChunk.class.cast(remoteAddrC.get(hrp)).setString("127.0.0.1");
        hrp.setServerPort(80);

        Request request = Request.create();
        Field packet = Request.class.getDeclaredField("request");
        packet.setAccessible(true);
        packet.set(request, hrp);

        HttpServletRequestImpl hsri = HttpServletRequestImpl.create();
        hsri.initialize(request, null);

        HttpServletRequestWrapper hsrw = new HttpServletRequestWrapper(hsri);
        System.out.println(hsrw);

        List<Future<Exception>> manips = new ArrayList<Future<Exception>>();
        for (int i = 0; i < 10; i++) {
            manips.add(executorService.submit(new ManipCall(hsrw)));
        }
        List<Exception> exceptions = new ArrayList<Exception>();
        for (Future<Exception> future : manips) {
            Exception exception = future.get();
            if (exception != null) {
                exceptions.add(exception);
            }
        }

        if(!exceptions.isEmpty()) {
            System.out.println(exceptions);
        }
        assertTrue(exceptions.isEmpty());
    }

    class ManipCall implements Callable<Exception> {

        private HttpServletRequestWrapper hsr;

        public ManipCall(HttpServletRequestWrapper hsr) {
            this.hsr = hsr;
        }

        @Override
        public Exception call() throws Exception {
            try {
                for (int i = 0; i < 20000; i++) {
                    hsr.putParameter("foo", "bar");
                }
            } catch (Exception e) {
                return e;
            }
            return null;
        }

    }

}
