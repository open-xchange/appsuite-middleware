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

package com.openexchange;

import java.io.IOException;
import java.util.Random;
import junit.framework.AssertionFailedError;
import org.xml.sax.SAXException;
import com.meterware.httpunit.AuthorizationRequiredException;
import com.meterware.httpunit.HeadMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.session.LoginTools;
import com.openexchange.ajax.session.actions.LoginRequest;
import com.openexchange.ajax.session.actions.LoginResponse;
import com.openexchange.ajax.session.actions.LogoutRequest;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.exception.OXException;

/**
 * Tests if AJAX and WebDAV requests may get mixed up.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class MixTest {

    /**
     * Prevent instantiation.
     */
    private MixTest() {
        super();
    }

    /**
     * @param args
     * @throws InterruptedException
     */
    public static void main(final String[] args) throws InterruptedException {
        final int count = 3;
        final Thread[] ajax = new Thread[count];
        final Thread[] webdav = new Thread[count];
        try {
            AJAXConfig.init();
        } catch (final OXException e) {
            e.printStackTrace();
            return;
        }
        for (int i = 0; i < count; i++) {
            ajax[i] = new Thread(new AjaxLogin());
            ajax[i].start();
            webdav[i] = new Thread(new WebDAVLogin());
            webdav[i].start();
        }
        for (int i = 0; i < count; i++) {
            ajax[i].join();
            webdav[i].join();
        }
    }

    private static class AjaxLogin implements Runnable {

        private final AJAXSession session = new AJAXSession();

        private final LoginRequest request = new LoginRequest(
            AJAXConfig.getProperty(Property.LOGIN),
            AJAXConfig.getProperty(Property.PASSWORD),
            LoginTools.generateAuthId(),
            MixTest.class.getName(),
            "6.15.0");

        private final Random rand = new Random(System.currentTimeMillis());

        AjaxLogin() {
            super();
        }

        @Override
        public void run() {
            while (true) {
                LoginResponse resp = null;
                try {
                    resp = Executor.execute(session, request);
                    session.setId(resp.getSessionId());
                    Executor.execute(session, new LogoutRequest());
                    session.setId(null);
                } catch (final AssertionFailedError e) {

                    System.out.println("Login failed! " + e.getMessage());
                } catch (final Throwable t) {
                    t.printStackTrace();
                } finally {
                    session.getConversation().clearContents();
                    try {
                        Thread.sleep(rand.nextInt(10));
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static class WebDAVLogin implements Runnable {

        private final Random rand = new Random(System.currentTimeMillis());

        WebDAVLogin() {
            super();
        }

        @Override
        public void run() {
            final WebConversation conv = new WebConversation();
            final WebRequest req = new HeadMethodWebRequest(
                AJAXConfig.getProperty(Property.PROTOCOL) + "://"
                    + AJAXConfig.getProperty(Property.HOSTNAME)
                    + "/servlet/webdav.infostore");
            conv.setAuthorization(
                AJAXConfig.getProperty(Property.LOGIN),
                AJAXConfig.getProperty(Property.PASSWORD));
            try {
                while (true) {
                    try {
                        final WebResponse resp = conv.getResponse(req);
                        if (resp.getResponseCode() != 200
                            || !"httpd/unix-directory".equals(resp.getContentType())) {
                            System.out.println("discovered mod_jk problem!");
                        }
                        if (resp.getResponseCode() == 200
                            && resp.getContentType().startsWith("text/javascript")) {
                            System.out.println("Invalid body found! \""
                                + resp.getText() + "\"");
                        }
                    } catch (final AuthorizationRequiredException e) {
                        System.out.println("Login failed.");
                    }
                    Thread.sleep(rand.nextInt(10));
                }
            } catch (final IOException e) {
                e.printStackTrace();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            } catch (final SAXException e) {
                e.printStackTrace();
            }
        }
    }
}
