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

package com.openexchange.subscribe.parser;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.upload.ManagedUploadFile;
import com.openexchange.session.Session;
import com.openexchange.subscribe.XingSubscription;
import com.openexchange.subscribe.XingSubscriptionHandler;

/**
 * {@link XingHandler}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class XingHandler extends ContactHandler implements XingSubscriptionHandler {

    private static final Log LOG = LogFactory.getLog(XingHandler.class);

    public void handleSubscription(XingSubscription xingSubscription) {
        try {
            ContactObject[] xingContactsForUser = new XingContactParser().getXingContactsForUser(
                xingSubscription.getXingUserName(),
                xingSubscription.getXingPassword());
            List<ContactObject> contacts = Arrays.asList(xingContactsForUser);
            storeContacts(new XingSubscriptionSession(xingSubscription), xingSubscription.getTargetFolder(), contacts);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        } catch (SAXException e) {
            LOG.error(e.getMessage(), e);
        } catch (ContextException e) {
            LOG.error(e.getMessage(), e);
        } catch (OXException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private static final class XingSubscriptionSession implements Session {

        private XingSubscription subscription;

        public XingSubscriptionSession(XingSubscription subscription) {
            this.subscription = subscription;
        }

        public int getContextId() {
            return subscription.getContextId();
        }

        public String getLocalIp() {
            throw new UnsupportedOperationException();
        }

        public String getLogin() {
            throw new UnsupportedOperationException();
        }

        public String getLoginName() {
            throw new UnsupportedOperationException();
        }

        public Object getParameter(String name) {
            throw new UnsupportedOperationException();
        }

        public String getPassword() {
            throw new UnsupportedOperationException();
        }

        public String getRandomToken() {
            throw new UnsupportedOperationException();
        }

        public String getSecret() {
            throw new UnsupportedOperationException();
        }

        public String getSessionID() {
            throw new UnsupportedOperationException();
        }

        public ManagedUploadFile getUploadedFile(String id) {
            throw new UnsupportedOperationException();
        }

        public int getUserId() {
            return subscription.getUserId();
        }

        public String getUserlogin() {
            throw new UnsupportedOperationException();
        }

        public void putUploadedFile(String id, ManagedUploadFile uploadFile) {
            throw new UnsupportedOperationException();
        }

        public void removeRandomToken() {
            throw new UnsupportedOperationException();
        }

        public ManagedUploadFile removeUploadedFile(String id) {
            throw new UnsupportedOperationException();
        }

        public void removeUploadedFileOnly(String id) {
            throw new UnsupportedOperationException();
        }

        public void setParameter(String name, Object value) {
            throw new UnsupportedOperationException();
        }

        public boolean touchUploadedFile(String id) {
            throw new UnsupportedOperationException();
        }
    }

}
