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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.webdav.acl.reports;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.contact.SortOptions;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ConstantOperand;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.webdav.acl.PrincipalProtocol;
import com.openexchange.webdav.acl.PrincipalWebdavFactory;
import com.openexchange.webdav.acl.UserPrincipalResource;
import com.openexchange.webdav.action.WebdavPropfindAction;
import com.openexchange.webdav.action.WebdavRequest;
import com.openexchange.webdav.action.WebdavResponse;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.xml.resources.ResourceMarshaller;

/**
 * {@link PrinicpalPropertySearchReport}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class PrinicpalPropertySearchReport extends WebdavPropfindAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PrinicpalPropertySearchReport.class);

    public static final String NAMESPACE = DAV_NS.getURI();
    public static final String NAME = "principal-property-search";

    /**
     * Pattern to check for valid e-mail addresses (needed for the Max OS client)
     */
    private static final Pattern RFC_2822_SIMPLIFIED_PATTERN = Pattern.compile(
        "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?",
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    /**
     * Initializes a new {@link PrinicpalPropertySearchReport}.
     *
     * @param protocol The principal protocol
     */
    public PrinicpalPropertySearchReport(Protocol protocol) {
        super(protocol);
    }

    @Override
    public void perform(WebdavRequest req, WebdavResponse res) throws WebdavProtocolException {
        if (0 != req.getDepth(0) || false == req.getResource().isCollection()) {
            throw WebdavProtocolException.generalError(req.getUrl(), HttpServletResponse.SC_BAD_REQUEST);
        }
        /*
         * get request body
         */
        boolean forceAllProp = false;
        Document requestBody = null;
        try {
            requestBody = req.getBodyAsDocument();
        } catch (JDOMException e) {
            forceAllProp = true;
        } catch (IOException e) {
            forceAllProp = true;
        }
        /*
         * search matching users
         */
        PrincipalWebdavFactory factory = (PrincipalWebdavFactory)req.getFactory();
        Set<User> users = new HashSet<User>();
        /*
         * prepare composite search term
         */
        CompositeSearchTerm orTerm = new CompositeSearchTerm(CompositeOperation.OR);
        List<Element> propertySearches = requestBody.getRootElement().getChildren("property-search", DAV_NS);
        for (Element propertySearch : propertySearches) {
            Element matchElement = propertySearch.getChild("match", DAV_NS);
            if (null == matchElement || Strings.isEmpty(matchElement.getText())) {
                continue;
            }
            String match = matchElement.getText() + "*"; // always assume starts-with nature
            Element prop = propertySearch.getChild("prop", DAV_NS);
            for (Element element : prop.getChildren()) {
                /*
                 * create a corresponding search term for each supported property
                 */
                if ("displayname".equals(element.getName()) && DAV_NS.equals(element.getNamespace())) {
                    SingleSearchTerm term = new SingleSearchTerm(SingleOperation.EQUALS);
                    term.addOperand(new ContactFieldOperand(ContactField.DISPLAY_NAME));
                    term.addOperand(new ConstantOperand<String>(match));
                    orTerm.addSearchTerm(term);
                } else if ("first-name".equals(element.getName()) && PrincipalProtocol.CALENDARSERVER_NS.equals(element.getNamespace())) {
                    SingleSearchTerm term = new SingleSearchTerm(SingleOperation.EQUALS);
                    term.addOperand(new ContactFieldOperand(ContactField.GIVEN_NAME));
                    term.addOperand(new ConstantOperand<String>(match));
                    orTerm.addSearchTerm(term);
                } else if ("last-name".equals(element.getName()) && PrincipalProtocol.CALENDARSERVER_NS.equals(element.getNamespace())) {
                    SingleSearchTerm term = new SingleSearchTerm(SingleOperation.EQUALS);
                    term.addOperand(new ContactFieldOperand(ContactField.SUR_NAME));
                    term.addOperand(new ConstantOperand<String>(match));
                    orTerm.addSearchTerm(term);
                } else if ("email-address-set".equals(element.getName()) && PrincipalProtocol.CALENDARSERVER_NS.equals(element.getNamespace())) {
                    CompositeSearchTerm emailTerm = new CompositeSearchTerm(CompositeOperation.OR);
                    for (ContactField emailField : new ContactField[] { ContactField.EMAIL1, ContactField.EMAIL2, ContactField.EMAIL3 }) {
                        SingleSearchTerm term = new SingleSearchTerm(SingleOperation.EQUALS);
                        term.addOperand(new ContactFieldOperand(emailField));
                        term.addOperand(new ConstantOperand<String>(match));
                        emailTerm.addSearchTerm(term);
                    }
                    orTerm.addSearchTerm(emailTerm);
                }
            }
        }
        /*
         * perform search
         */
        if (null != orTerm.getOperands() && 0 < orTerm.getOperands().length) {
            SearchIterator<Contact> searchIterator = null;
            try {
                searchIterator = factory.getContactService().searchUsers(factory.getSessionHolder().getSessionObject(), orTerm,
                    new ContactField[] { ContactField.INTERNAL_USERID, ContactField.CONTEXTID }, SortOptions.EMPTY);
                while (searchIterator.hasNext()) {
                    try {
                        Contact contact = searchIterator.next();
                        users.add(factory.getUserService().getUser(contact.getInternalUserId(), contact.getContextId()));
                    } catch (OXException e) {
                        LOG.warn("error resolving user", e);
                    }
                }
            } catch (OXException e) {
                LOG.warn("error searching users", e);
            } finally {
                SearchIterators.close(searchIterator);
            }
        }
        /*
         * marshal response
         */
        Element response = new Element("multistatus", DAV_NS);
        for (Namespace namespace : protocol.getAdditionalNamespaces()) {
            response.addNamespaceDeclaration(namespace);
        }
        ResourceMarshaller marshaller = getMarshaller(req, forceAllProp, requestBody, null);
        for (User user : users) {
            if (Strings.isEmpty(user.getMail()) || false == RFC_2822_SIMPLIFIED_PATTERN.matcher(user.getMail()).matches()) {
                // skip, since the Mac OS client gets into trouble when the TLD is missing in the mail address
                continue;
            }
            response.addContent(marshaller.marshal(new UserPrincipalResource(factory, user, new WebdavPath().append(user.getLoginInfo()))));
        }
        Document responseBody = new Document(response);
        try {
            res.setStatus(Protocol.SC_MULTISTATUS);
            res.setContentType("text/xml; charset=UTF-8");
            outputter.output(responseBody, res.getOutputStream());
        } catch (IOException e) {
            // IGNORE
        }
    }

}
