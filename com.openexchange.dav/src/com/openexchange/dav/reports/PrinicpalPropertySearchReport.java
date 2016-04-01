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

package com.openexchange.dav.reports;

import static com.openexchange.webdav.protocol.Protocol.DAV_NS;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.SortOptions;
import com.openexchange.dav.CUType;
import com.openexchange.dav.DAVFactory;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.actions.PROPFINDAction;
import com.openexchange.dav.mixins.PrincipalURL;
import com.openexchange.dav.mixins.ResourceId;
import com.openexchange.dav.principals.groups.GroupPrincipalCollection;
import com.openexchange.dav.principals.groups.GroupPrincipalResource;
import com.openexchange.dav.principals.resources.ResourcePrincipalCollection;
import com.openexchange.dav.principals.resources.ResourcePrincipalResource;
import com.openexchange.dav.principals.users.UserPrincipalCollection;
import com.openexchange.dav.principals.users.UserPrincipalResource;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.resource.Resource;
import com.openexchange.resource.ResourceService;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ConstantOperand;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.user.UserService;
import com.openexchange.webdav.action.WebdavRequest;
import com.openexchange.webdav.action.WebdavResponse;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.xml.resources.ResourceMarshaller;
/**
 * {@link PrinicpalPropertySearchReport}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class PrinicpalPropertySearchReport extends PROPFINDAction {

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
    public PrinicpalPropertySearchReport(DAVProtocol protocol) {
        super(protocol);
    }

    @Override
    public void perform(WebdavRequest request, WebdavResponse response) throws WebdavProtocolException {
        /*
         * get request body
         */
        Document requestBody = optRequestBody(request);
        if (null == requestBody) {
            throw WebdavProtocolException.generalError(request.getUrl(), HttpServletResponse.SC_BAD_REQUEST);
        }
        Element applyToPrincipalCollectionSet = requestBody.getRootElement().getChild("apply-to-principal-collection-set", DAV_NS);
        List<Element> propertySearches = requestBody.getRootElement().getChildren("property-search", DAV_NS);
        DAVFactory factory = (DAVFactory) request.getFactory();
        /*
         * search matching users
         */
        Set<User> users = new HashSet<User>();
        if (null != applyToPrincipalCollectionSet || UserPrincipalCollection.NAME.equals(request.getUrl().name()) || 0 == request.getUrl().size()) {
            /*
             * prepare composite search term
             */
            CompositeSearchTerm orTerm = new CompositeSearchTerm(CompositeOperation.OR);
            for (Element propertySearch : propertySearches) {
                Element matchElement = propertySearch.getChild("match", DAV_NS);
                if (null == matchElement || Strings.isEmpty(matchElement.getText())) {
                    continue;
                }
                String pattern = getPattern(matchElement);
                Element prop = propertySearch.getChild("prop", DAV_NS);
                for (Element element : prop.getChildren()) {
                    /*
                     * create a corresponding search term for each supported property
                     */
                    if ("displayname".equals(element.getName()) && DAV_NS.equals(element.getNamespace())) {
                        SingleSearchTerm term = new SingleSearchTerm(SingleOperation.EQUALS);
                        term.addOperand(new ContactFieldOperand(ContactField.DISPLAY_NAME));
                        term.addOperand(new ConstantOperand<String>(pattern));
                        orTerm.addSearchTerm(term);
                    } else if ("first-name".equals(element.getName()) && DAVProtocol.CALENDARSERVER_NS.equals(element.getNamespace())) {
                        SingleSearchTerm term = new SingleSearchTerm(SingleOperation.EQUALS);
                        term.addOperand(new ContactFieldOperand(ContactField.GIVEN_NAME));
                        term.addOperand(new ConstantOperand<String>(pattern));
                        orTerm.addSearchTerm(term);
                    } else if ("last-name".equals(element.getName()) && DAVProtocol.CALENDARSERVER_NS.equals(element.getNamespace())) {
                        SingleSearchTerm term = new SingleSearchTerm(SingleOperation.EQUALS);
                        term.addOperand(new ContactFieldOperand(ContactField.SUR_NAME));
                        term.addOperand(new ConstantOperand<String>(pattern));
                        orTerm.addSearchTerm(term);
                    } else if ("email-address-set".equals(element.getName()) && DAVProtocol.CALENDARSERVER_NS.equals(element.getNamespace())) {
                        CompositeSearchTerm emailTerm = new CompositeSearchTerm(CompositeOperation.OR);
                        for (ContactField emailField : new ContactField[] { ContactField.EMAIL1, ContactField.EMAIL2, ContactField.EMAIL3 }) {
                            SingleSearchTerm term = new SingleSearchTerm(SingleOperation.EQUALS);
                            term.addOperand(new ContactFieldOperand(emailField));
                            term.addOperand(new ConstantOperand<String>(pattern));
                            emailTerm.addSearchTerm(term);
                        }
                        orTerm.addSearchTerm(emailTerm);
                    } else if ("calendar-user-address-set".equals(element.getName()) && DAVProtocol.CAL_NS.equals(element.getNamespace())) {
                        int userID = extractPrincipalID(pattern, CUType.INDIVIDUAL);
                        if (-1 != userID) {
                            SingleSearchTerm term = new SingleSearchTerm(SingleOperation.EQUALS);
                            term.addOperand(new ContactFieldOperand(ContactField.INTERNAL_USERID));
                            term.addOperand(new ConstantOperand<Integer>(Integer.valueOf(userID)));
                            orTerm.addSearchTerm(term);
                        } else if (pattern.startsWith("mailto:")) {
                            SingleSearchTerm term = new SingleSearchTerm(SingleOperation.EQUALS);
                            term.addOperand(new ContactFieldOperand(ContactField.EMAIL1));
                            term.addOperand(new ConstantOperand<String>(pattern.substring(7)));
                            orTerm.addSearchTerm(term);
                        }
                    }
                }
            }
            /*
             * perform search
             */
            if (null != orTerm.getOperands() && 0 < orTerm.getOperands().length) {
                SearchIterator<Contact> searchIterator = null;
                try {
                    searchIterator = factory.requireService(ContactService.class).searchUsers(factory.getSessionObject(), orTerm,
                        new ContactField[] { ContactField.INTERNAL_USERID, ContactField.CONTEXTID }, SortOptions.EMPTY);
                    while (searchIterator.hasNext()) {
                        try {
                            Contact contact = searchIterator.next();
                            users.add(factory.requireService(UserService.class).getUser(contact.getInternalUserId(), contact.getContextId()));
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
        }
        /*
         * search matching groups
         */
        Set<Group> groups = new HashSet<Group>();
        if (null != applyToPrincipalCollectionSet || GroupPrincipalCollection.NAME.equals(request.getUrl().name()) || 0 == request.getUrl().size()) {
            for (Element propertySearch : propertySearches) {
                Element matchElement = propertySearch.getChild("match", DAV_NS);
                if (null == matchElement || Strings.isEmpty(matchElement.getText())) {
                    continue;
                }
                String pattern = getPattern(matchElement);
                Element prop = propertySearch.getChild("prop", DAV_NS);
                for (Element element : prop.getChildren()) {
                    /*
                     * search by displayname only
                     */
                    if ("displayname".equals(element.getName()) && DAV_NS.equals(element.getNamespace())) {
                        try {
                            Group[] foundGroups = GroupStorage.getInstance().searchGroups(pattern, false, factory.getContext());
                            if (null != foundGroups) {
                                groups.addAll(Arrays.asList(foundGroups));
                            }
                        } catch (OXException e) {
                            LOG.warn("error searching groups", e);
                        }
                    } else if ("calendar-user-address-set".equals(element.getName()) && DAVProtocol.CAL_NS.equals(element.getNamespace())) {
                        int groupID = extractPrincipalID(pattern, CUType.GROUP);
                        if (-1 != groupID) {
                            try {
                                Group group = GroupStorage.getInstance().getGroup(groupID, factory.getContext());
                                if (null != group) {
                                    groups.add(group);
                                }
                            } catch (OXException e) {
                                LOG.warn("error searching groups", e);
                            }
                        }
                    }
                }
            }
        }
        /*
         * search matching resources
         */
        Set<Resource> resources = new HashSet<Resource>();
        if (null != applyToPrincipalCollectionSet || ResourcePrincipalCollection.NAME.equals(request.getUrl().name()) || 0 == request.getUrl().size()) {
            for (Element propertySearch : propertySearches) {
                Element matchElement = propertySearch.getChild("match", DAV_NS);
                if (null == matchElement || Strings.isEmpty(matchElement.getText())) {
                    continue;
                }
                String pattern = getPattern(matchElement);
                Element prop = propertySearch.getChild("prop", DAV_NS);
                for (Element element : prop.getChildren()) {
                    /*
                     * search by displayname or mail address
                     */
                    if ("displayname".equals(element.getName()) && DAV_NS.equals(element.getNamespace())) {
                        try {
                            Resource[] foundResources = factory.requireService(ResourceService.class).searchResources(pattern, factory.getContext());
                            if (null != foundResources && 0 < foundResources.length) {
                                resources.addAll(Arrays.asList(foundResources));
                            }
                        } catch (OXException e) {
                            LOG.warn("error searching resources", e);
                        }
                    } else if ("email-address-set".equals(element.getName()) && DAV_NS.equals(element.getNamespace())) {
                        try {
                            Resource[] foundResources = factory.requireService(ResourceService.class).searchResourcesByMail(pattern, factory.getContext());
                            if (null != foundResources && 0 < foundResources.length) {
                                resources.addAll(Arrays.asList(foundResources));
                            }
                        } catch (OXException e) {
                            LOG.warn("error searching resources", e);
                        }
                    } else if ("calendar-user-address-set".equals(element.getName()) && DAVProtocol.CAL_NS.equals(element.getNamespace())) {
                        int resourceID = extractPrincipalID(pattern, CUType.RESOURCE);
                        if (-1 != resourceID) {
                            try {
                                Resource resource = factory.requireService(ResourceService.class).getResource(resourceID, factory.getContext());
                                if (null != resource) {
                                    resources.add(resource);
                                }
                            } catch (OXException e) {
                                LOG.warn("error searching resources", e);
                            }
                        }
                    }
                }
            }
        }
        /*
         * marshal response
         */
        Element multistatusElement = prepareMultistatusElement();
        ResourceMarshaller marshaller = getMarshaller(request, requestBody, new WebdavPath().toString());
        for (User user : users) {
            if (Strings.isEmpty(user.getMail()) || false == RFC_2822_SIMPLIFIED_PATTERN.matcher(user.getMail()).matches()) {
                // skip, since the Mac OS client gets into trouble when the TLD is missing in the mail address
                continue;
            }
            multistatusElement.addContent(marshaller.marshal(new UserPrincipalResource(factory, user)));
        }
        for (Group group : groups) {
            multistatusElement.addContent(marshaller.marshal(new GroupPrincipalResource(factory, group)));
        }
        for (Resource resource : resources) {
            multistatusElement.addContent(marshaller.marshal(new ResourcePrincipalResource(factory, resource)));
        }
        sendMultistatusResponse(response, multistatusElement);
    }

    /**
     * Gets the search pattern indicated by the supplied <code>match</code> element.
     *
     * @param matchElement The match element to extract the pattern from
     * @return The search pattern
     */
    private static String getPattern(Element matchElement) {
        String match = matchElement.getText();
        Attribute matchTypeAttribute = matchElement.getAttribute("match-type");
        if (null == matchTypeAttribute || Strings.isEmpty(matchTypeAttribute.getValue())) {
            return match + '*'; // default to "starts-with"
        }
        switch (matchTypeAttribute.getValue()) {
            case "equals":
                return match;
            case "contains":
                return '*' + match + '*';
            case "ends-with":
                return '*' + match;
            default:
                return match + '*';
        }
    }

    /**
     * Tries to extract the targeted principal identifier directly from the supplied pattern as used in a
     * <code>calendar-user-address-set</code> property search.
     *
     * @param pattern The pattern to match
     * @param cuType The calendar user type to match
     * @return The principal identifier, or <code>-1</code> if none could be extracted
     */
    private static int extractPrincipalID(String pattern, CUType cuType) {
        String trimmedPattern = Strings.trimStart(Strings.trimEnd(pattern, '*'), '*');
        /*
         * try principal URL
         */
        PrincipalURL principalURL = PrincipalURL.parse(trimmedPattern);
        if (null != principalURL && cuType.equals(principalURL.getType())) {
            return principalURL.getPrincipalID();
        } else {
            /*
             * try resource ID
             */
            ResourceId resourceId = ResourceId.parse(trimmedPattern);
            if (null != resourceId && cuType.getType() == resourceId.getparticipantType()) {
                return resourceId.getPrincipalID();
            }
        }
        return -1;
    }

}
