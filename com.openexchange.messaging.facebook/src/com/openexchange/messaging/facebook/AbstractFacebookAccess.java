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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.messaging.facebook;

import static com.openexchange.messaging.facebook.services.FacebookMessagingServiceRegistry.getServiceRegistry;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.w3c.dom.Element;
import com.google.code.facebookapi.FacebookException;
import com.google.code.facebookapi.IFacebookRestClient;
import com.google.code.facebookapi.schema.FqlQueryResponse;
import com.openexchange.context.ContextService;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.ldap.UserException;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.MessagingException;
import com.openexchange.messaging.MessagingFolder;
import com.openexchange.messaging.facebook.parser.user.FacebookFQLUserParser;
import com.openexchange.server.ServiceException;
import com.openexchange.session.Session;
import com.openexchange.user.UserService;


/**
 * {@link AbstractFacebookAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractFacebookAccess {

    protected static Set<String> KNOWN_FOLDER_IDS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(MessagingFolder.ROOT_FULLNAME, FacebookConstants.FOLDER_WALL)));

    protected final Session session;

    protected final MessagingAccount messagingAccount;

    protected final IFacebookRestClient<Object> facebookRestClient;

    protected final int id;

    protected final int user;

    protected final int cid;

    protected final long facebookUserId;

    protected final String facebookSession;

    protected volatile String facebookUserName;

    protected volatile Locale userLocale;

    /**
     * Initializes a new {@link AbstractFacebookAccess}.
     */
    protected AbstractFacebookAccess(final IFacebookRestClient<Object> facebookRestClient, final MessagingAccount messagingAccount, final Session session, final long facebookUserId, final String facebookSession) {
        super();
        this.session = session;
        this.messagingAccount = messagingAccount;
        this.facebookRestClient = facebookRestClient;
        id = messagingAccount.getId();
        user = session.getUserId();
        cid = session.getContextId();
        this.facebookUserId = facebookUserId;
        this.facebookSession = facebookSession;
    }

    public String getFacebookUserName() throws MessagingException {
        String tmp = facebookUserName;
        if (null == tmp) {
            synchronized (this) {
                tmp = facebookUserName;
                if (null == tmp) {
                    try {
                        final FqlQueryResponse fqr =
                            (FqlQueryResponse) facebookRestClient.fql_query(new StringBuilder("SELECT name FROM user WHERE uid = ").append(
                                facebookUserId).toString());
                        facebookUserName =
                            tmp = FacebookFQLUserParser.parseUserDOMElement((Element) fqr.getResults().iterator().next()).getName();
                    } catch (final FacebookException e) {
                        throw FacebookMessagingException.create(e);
                    }
                }
            }
        }
        return tmp;
    }

    protected Locale getUserLocale() throws MessagingException {
        Locale tmp = userLocale;
        if (null == tmp) {
            /*
             * Duplicate initialization isn't harmful; no "synchronized" needed
             */
            try {
                final ContextService cs = getServiceRegistry().getService(ContextService.class, true);
                userLocale = tmp = getServiceRegistry().getService(UserService.class).getUser(user, cs.getContext(cid)).getLocale();
            } catch (final ServiceException e) {
                throw new MessagingException(e);
            } catch (final UserException e) {
                throw new MessagingException(e);
            } catch (final ContextException e) {
                throw new MessagingException(e);
            }
        }
        return tmp;
    }

}
