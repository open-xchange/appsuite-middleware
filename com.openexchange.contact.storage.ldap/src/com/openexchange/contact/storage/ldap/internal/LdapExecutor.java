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

package com.openexchange.contact.storage.ldap.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsResponseControl;
import javax.naming.ldap.SortKey;
import org.slf4j.Logger;
import com.openexchange.contact.storage.ldap.LdapExceptionCodes;
import com.openexchange.contact.storage.ldap.config.LdapConfig.SearchScope;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link LdapExecutor}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class LdapExecutor  {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(LdapExecutor.class);

    private final LdapFactory factory;
    private final LdapContext context;
    private String defaultNamingContext;

    public LdapExecutor(LdapFactory factory, Session session) throws OXException {
        super();
        this.factory = factory;
        this.context = factory.createContext(session);
    }

    public LdapResult getAttributes(String objectName, String[] attributeNames) throws OXException {
        try {
            return new LdapResult(this.context.getAttributes(objectName, attributeNames), objectName);
        } catch (NamingException e) {
            throw LdapExceptionCodes.ERROR_GETTING_ATTRIBUTE.create(e, e.getMessage());
        }
    }

    public List<LdapResult> search(String baseDN, String filter, String[] attributeNames, SortKey[] sortKeys, int maxResults) throws OXException {
        SearchControls searchControls = factory.createSearchControls(attributeNames, maxResults);
        if (null == baseDN) {
            baseDN = getDefaultNamingContext();
        }
        return search(baseDN, filter, searchControls, sortKeys, false);
    }

    public List<LdapResult> searchDeleted(String filter, String[] attributeNames, SortKey[] sortKeys, int maxResults) throws OXException {
        SearchControls searchControls = factory.createSearchControls(attributeNames, maxResults);
        String searchFilter = null != filter ? "(&" + filter + "(isDeleted=TRUE))" : "(isDeleted=TRUE)";
        String baseDN = getDefaultNamingContext();
        return search(baseDN, searchFilter, searchControls, sortKeys, true);
    }

    private List<LdapResult> search(String baseDN, String filter, SearchControls searchControls, SortKey[] sortKeys, boolean deleted) throws OXException {
        List<LdapResult> results = new ArrayList<LdapResult>();
        byte[] cookie = null;
        try {
            do {
                this.context.setRequestControls(factory.createRequestControls(sortKeys, cookie, deleted));
                Date start = new Date();
                LOG.debug("Search [{}]: {}", baseDN, filter);
                List<LdapResult> ldapResults = LdapResult.getResults(context.search(baseDN, filter, searchControls));
                LOG.debug("Got {} results, {}ms eleapsed.", ldapResults.size(), (new Date().getTime() - start.getTime()));
                results.addAll(ldapResults);
                cookie = extractPagedResultsCookie();
            } while (null != cookie);
            context.setRequestControls(null);
            return results;
        } catch (NamingException e) {
            throw LdapExceptionCodes.LDAP_ERROR.create(e, e.getMessage());
        }
    }

    private byte[] extractPagedResultsCookie() throws NamingException {
        Control[] controls = context.getResponseControls();
        if (null != controls && 0 < controls.length) {
            for (Control control : controls) {
                if (PagedResultsResponseControl.class.isInstance(control)) {
                    return ((PagedResultsResponseControl)control).getCookie();
                }
            }
        }
        return null;
    }

    private String getDefaultNamingContext() throws OXException {
        if (null == this.defaultNamingContext) {
            this.defaultNamingContext = discoverDefaultNamingContext();
        }
        return this.defaultNamingContext;
    }

    private String discoverDefaultNamingContext() throws OXException {
        SearchControls searchControls = factory.createSearchControls(SearchScope.BASE, new String[] { "defaultNamingContext" }, 0);
        try {
            LOG.debug("Search []: (objectClass=*)");
            Date start = new Date();
            List<LdapResult> ldapResults = LdapResult.getResults(context.search("", "(objectClass=*)", searchControls));
            LOG.debug("Got {} results, {}ms eleapsed.", ldapResults.size(), (new Date().getTime() - start.getTime()));
            for (LdapResult ldapResult : ldapResults) {
                String defaultNamingContext = (String)ldapResult.getAttribute("defaultNamingContext");
                if (null != defaultNamingContext) {
                    return defaultNamingContext;
                }
            }
        } catch (NamingException e) {
            throw LdapExceptionCodes.ERROR_GETTING_DEFAULT_NAMING_CONTEXT.create(e, (Object[])null);
        }
        throw LdapExceptionCodes.ERROR_GETTING_DEFAULT_NAMING_CONTEXT.create();
    }

    public void close() {
        Tools.close(context);
    }

}
