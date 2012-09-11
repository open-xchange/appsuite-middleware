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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.zmal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.dom4j.Namespace;
import org.dom4j.QName;
import com.openexchange.tools.net.URIDefaults;
import com.openexchange.zmal.config.ZmalConfig;
import com.openexchange.zmal.config.ZmalConfig.PreauthInfo;
import com.zimbra.common.auth.ZAuthToken;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.Element.ElementFactory;
import com.zimbra.common.soap.Element.JSONElement;
import com.zimbra.common.soap.Element.XMLElement;
import com.zimbra.common.soap.SoapHttpTransport;

/**
 * {@link ZmalSoapPerformer} - Performs SOAP requests with an XPath-inspired syntax, takes care of authenticating, generating the envelope,
 * sending the request, and parsing the response.
 * <p>
 * 
 * <pre>
 * GetAccountInfoRequest/account=user1 -v @by=name
 * 
 * Sending admin auth request to https://localhost:7071/service/admin/soap
 * 
 * &lt;GetAccountInfoRequest xmlns="urn:zimbraAdmin"&gt;
 *   &lt;account by="name"&gt;user1&lt;/account&gt;
 * &lt;/GetAccountInfoRequest&gt;
 * </pre>
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ZmalSoapPerformer {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(ZmalSoapPerformer.class);
    private static final boolean DEBUG = LOG.isDebugEnabled();

    private int contextId;
    private int userId;
    private String mUrl;
    private String mAdminUrl;
    private String mServer;
    private PreauthInfo mPreAuth;
    private String mMailboxName;
    private String mAdminAccountName;
    private String mTargetAccountName;
    private String mPassword;
    private String mAuthToken;
    private ZAuthToken zAuthToken;
    private ElementFactory mFactory;
    private boolean mUseSession;
    private boolean mUseJson;
    private String mSelect;
    private final ZmalConfig config;

    /**
     * Initializes a new {@link ZmalSoapPerformer}.
     */
    protected ZmalSoapPerformer(final ZmalConfig config) {
        super();
        mUseSession = false;
        mUseJson = false;
        this.config = config;
    }
    
    /**
     * Sets the preauth
     *
     * @param mPreAuth The preauth to set
     */
    public void setPreAuthKey(PreauthInfo preAuth) {
        this.mPreAuth = preAuth;
    }
    
    /**
     * Gets the URL for administration interface.
     *
     * @return The URL for administration interface
     */
    public String getAdminUrl() {
        return mAdminUrl;
    }
    
    /**
     * Gets the auth token
     * 
     * @return The auth token
     */
    public String getAuthToken() {
        return mAuthToken;
    }
    
    /**
     * Gets the Zimbra auth token
     *
     * @return The Zimbra auth token
     */
    public ZAuthToken getZAuthToken() {
        return zAuthToken;
    }

    /**
     * Sets the userId
     * 
     * @param userId The userId to set
     */
    public ZmalSoapPerformer setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    /**
     * Gets the userId
     * 
     * @return The userId
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Sets the contextId
     * 
     * @param contextId The contextId to set
     */
    public ZmalSoapPerformer setContextId(int contextId) {
        this.contextId = contextId;
        return this;
    }

    /**
     * Gets the contextId
     * 
     * @return The contextId
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * Gets the configuration
     * 
     * @return The configuration
     */
    public ZmalConfig getConfig() {
        return config;
    }

    /**
     * Sets the select string
     * 
     * @param select The select string
     */
    public ZmalSoapPerformer setSelect(final String select) {
        this.mSelect = select;
        return this;
    }
    
    /**
     * Gets the select string
     *
     * @return The select string
     */
    public String getSelect() {
        return mSelect;
    }
    
    /**
     * Gets the use-json flag.
     *
     * @return The use-json flag
     */
    public boolean isUseJson() {
        return mUseJson;
    }

    /**
     * Sets whether to use JSON format.
     * 
     * @param useJson <code>true</code> to use JSON format; otherwise <code>false</code>
     */
    public ZmalSoapPerformer setUseJson(final boolean useJson) {
        this.mUseJson = useJson;
        mFactory = (mUseJson ? JSONElement.mFactory : XMLElement.mFactory);
        return this;
    }

    /**
     * Parse from passed {@link ZmalConfig configuration}.
     * 
     * @param force <code>true</code> to force parse operation; otherwise <code>false</code>
     */
    public void parse(boolean force) {
        if (!force && null != mMailboxName) {
            return;
        }
        mFactory = (mUseJson ? JSONElement.mFactory : XMLElement.mFactory);
        mAuthToken = null;
        zAuthToken = null;
        mMailboxName = config.getLogin();
        mPassword = config.getPassword();
        mPreAuth = config.getPreauth();
        // Compose URL
        /*-
         * SOAP service URL, usually http[s]://host:port/service/soap or https://host:port/service/admin/soap.
         */
        {
            final StringBuilder sb = new StringBuilder(64);
            sb.append(config.getServer());
            final int port = config.getPort();
            if (port > 0 && port != URIDefaults.IMAP.getPort()) {
                sb.append(port);
            }
            mServer = sb.toString();
            // Prepend protocol
            sb.insert(0, config.isSecure() ? "https://" : "http://");
            final int l = sb.length();
            sb.append("/service/soap");
            mUrl = sb.toString();
            sb.setLength(l);
            sb.append("/service/admin/soap");
            mAdminUrl = sb.toString();
        }
    }
    
    /**
     * Gets the server; host[:port] of server to connect to
     *
     * @return The server
     */
    public String getServer() {
        return mServer;
    }
    
    /**
     * Gets the URL.
     * 
     * @return The URL
     */
    public String getUrl() {
        return mUrl;
    }

    /**
     * Resets fields.
     */
    public void reset() {
        mUrl = null;
        mAdminUrl = null;
        mServer = null;
        mMailboxName = null;
        mAdminAccountName = null;
        mTargetAccountName = null;
        mPassword = null;
        mPreAuth = null;
        mAuthToken = null;
        zAuthToken = null;
        mFactory = null;
        mUseSession = false;
        mUseJson = false;
        mSelect = null;
    }

    /**
     * Performs an authentication request and returns auth-token.
     * 
     * @param timeout The timeout
     * @return The auth-token
     * @throws ServiceException If a service error occurs
     * @throws IOException If an I/O error occurs
     */
    public String mailboxAuth(final int timeout) throws ServiceException, IOException {
        String authToken = mAuthToken;
        if (null != authToken) {
            return authToken;
        }
        if (DEBUG) {
            LOG.debug("Sending auth request to " + mUrl);
        }
        // Create transport
        final SoapHttpTransport transport = new SoapHttpTransport(mUrl);
        transport.setDebugListener(new ZmalDebugListener());
        transport.setTimeout(timeout);

        // Create auth element
        final Element auth = mFactory.createElement(AccountConstants.AUTH_REQUEST);
        final Element account = auth.addElement(AccountConstants.E_ACCOUNT).setText(mMailboxName);
        account.addAttribute(AdminConstants.A_BY, AdminConstants.BY_NAME);
        if (null == mPreAuth) {
            auth.addElement(AccountConstants.E_PASSWORD).setText(mPassword);
        } else {
            /*-
             * <AuthRequest xmlns="urn:zimbraAccount">
                <account>john.doe@domain.com</account>
                <preauth timestamp="1135280708088" expires="0">b248f6cfd027edd45c5369f8490125204772f844</preauth>
               </AuthRequest>
             */
            final Element preauth = auth.addElement(AccountConstants.E_PREAUTH).setText(mPreAuth.preauth);
            preauth.addAttribute("timestamp", mPreAuth.timestamp).addAttribute("expires", mPreAuth.expires);
        }

        // Authenticate and get auth token
        final Element response = transport.invoke(auth, false, !mUseSession, null);
        if (DEBUG) {
            LOG.debug(response.prettyPrint());
        }
        authToken = response.getAttribute(AccountConstants.E_AUTH_TOKEN);
        mAuthToken = authToken;
        Element eAuthToken = response.getElement(AccountConstants.E_AUTH_TOKEN);
        zAuthToken = new ZAuthToken(eAuthToken, false);
        return authToken;
    }

    /**
     * Parses specified XPath-inspired syntax
     * 
     * @param type The SOAP API type
     * @param paths The XPath-inspired syntax; e.g. <tt>"DumpSessionsRequest @groupByAccount=1 @listSessions=1"</tt>
     * @return The parsed element
     */
    public Element parse(final ZmalType type, final String... paths) {
        // Determine namespace by type
        final Namespace mNamespace = type.getNamespace();
        Element element = null;
        if (paths.length > 0) {
            // Build request from command line.
            for (final String path : paths) {
                element = processPath(element, path, mNamespace);
            }
        }
        // Find the root.
        if (null == element) {
            return null;
        }
        Element request = element;
        Element p;
        while (null != (p = request.getParent())) {
            request = p;
        }
        return request;
    }

    /**
     * Performs a SOAP request.
     * 
     * @param type The SOAP API type
     * @param paths The paths; e.g. <tt>"DumpSessionsRequest @groupByAccount=1 @listSessions=1"</tt>
     * @return The SOAP response
     * @throws ServiceException If a service error occurs
     * @throws IOException If an I/O error occurs
     */
    public ZmalSoapResponse perform(final ZmalType type, final String... paths) throws ServiceException, IOException {
        // Assemble SOAP request.
        final String mMailboxName = this.mMailboxName;
        if (null == mMailboxName) {
            parse(false);
        }
        // Parse XPath syntax
        Element request = parse(type, paths);
        if (request == null) {
            return null;
        }
        return perform0(type, request);
    }

    /**
     * Performs specified SOAP request as represented by given element.
     * 
     * @param type The SOAP API type
     * @param request The element representing the SOAP request
     * @return The SOAP response
     * @throws ServiceException If a service error occurs
     * @throws IOException If an I/O error occurs
     */
    public ZmalSoapResponse perform(final ZmalType type, Element request) throws ServiceException, IOException {
        // Assemble SOAP request.
        final String mMailboxName = this.mMailboxName;
        if (null == mMailboxName) {
            parse(false);
        }
        return perform0(type, request);
    }

    private ZmalSoapResponse perform0(final ZmalType type, Element request) throws ServiceException, IOException {
        final int timeout = config.getZmalProperties().getZmalTimeout();
        // Authenticate (and remember auth-token)
        mailboxAuth(timeout);
        // Send request and return response
        final SoapHttpTransport transport = new SoapHttpTransport(mUrl);
        transport.setDebugListener(new ZmalDebugListener());
        transport.setTimeout(timeout);
        transport.setAuthToken(mAuthToken);
        if (!type.equals(ZmalType.ADMIN) && mTargetAccountName != null) {
            transport.setTargetAcctName(mTargetAccountName);
        }
        final Element response = transport.invoke(request, false, !mUseSession, null);
        // Select result.
        List<Element> results = null;
        String resultString = null;
        if (mSelect == null) {
            results = new ArrayList<Element>();
            results.add(response);
        } else {
            // Create bogus root element, to allow us to find the first element in the path.
            final Element root = response.getFactory().createElement("root");
            response.detach();
            root.addElement(response);

            final String[] parts = SPLIT_SLASH.split(mSelect, 0);
            if (parts.length > 0) {
                final String lastPart = parts[parts.length - 1];
                if (lastPart.length() > 0 && '@' == lastPart.charAt(0)) {
                    parts[parts.length - 1] = lastPart.substring(1);
                    resultString = root.getPathAttribute(parts);
                } else {
                    results = root.getPathElementList(parts);
                }
            }
        }
        return new ZmalSoapResponse(results, resultString);
    }

    private static final Pattern SPLIT_EQUAL = Pattern.compile(Pattern.quote("="));
    private static final Pattern SPLIT_SLASH = Pattern.compile(Pattern.quote("/"));

    /**
     * Processes a path that's relative to the given root. The path is in an XPath-like format:
     * <p>
     * <tt>element1/element2[/@attr][=value]</tt>
     * </p>
     * If a value is specified, it sets the text of the last element or attribute in the path.
     * 
     * @param start <tt>Element</tt> that the path is relative to, or <tt>null</tt> for the root
     * @param path An XPath-like path of elements and attributes; e.g. <tt>"SearchRequest/query=in:inbox"</tt>
     * @param namespace The name space
     */
    private Element processPath(final Element start, final String path, Namespace namespace) {
        // Parse out value, if it's specified.
        String value = null;
        String pazz = path;
        if (pazz.indexOf('=') >= 0) {
            final String[] parts = SPLIT_EQUAL.split(pazz, 0);
            pazz = parts[0];
            value = parts[1];
        }
        // Find the first element.
        Element element = start;
        // Walk parts and implicitly create elements.
        String part = null;
        {
            final String[] parts = SPLIT_SLASH.split(pazz, 0);
            for (int i = 0; i < parts.length; i++) {
                part = parts[i];
                if (element == null) {
                    final QName name = QName.get(part, namespace);
                    element = mFactory.createElement(name);
                } else if ("..".equals(part)) {
                    element = element.getParent();
                } else if (part.length() <= 0 || '@' != part.charAt(0)) {
                    element = element.addElement(part);
                }
            }
        }
        // Set either element text or attribute value
        if (value != null && part != null) {
            if (part.length() > 0 && '@' == part.charAt(0)) {
                final String attrName = part.substring(1);
                element.addAttribute(attrName, value);
            } else {
                element.setText(value);
            }
        }
        return element;
    }

    protected static String formatServiceException(final ServiceException e) {
        if (null == e) {
            return null;
        }
        final Throwable cause = e.getCause();
        return "ERROR: " + e.getCode() + " (" + e.getMessage() + ")" + (cause == null ? "" : " (cause: " + cause.getClass().getName() + " " + cause.getMessage() + ")");
    }

}
