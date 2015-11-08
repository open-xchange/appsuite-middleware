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

package com.openexchange.carddav.reports;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import com.openexchange.carddav.CarddavProtocol;
import com.openexchange.carddav.resources.CardDAVCollection;
import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.PreconditionException;
import com.openexchange.dav.actions.DAVPropfindAction;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.java.Strings;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ConstantOperand;
import com.openexchange.webdav.action.WebdavRequest;
import com.openexchange.webdav.action.WebdavResponse;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.xml.resources.ResourceMarshaller;

/**
 * {@link AddressbookQueryReport}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class AddressbookQueryReport extends DAVPropfindAction {

    public static final String NAMESPACE = CarddavProtocol.CARD_NS.getURI();
    public static final String NAME = "addressbook-query";

    /**
     * Initializes a new {@link AddressbookQueryReport}.
     *
     * @param protocol The protocol
     */
    public AddressbookQueryReport(DAVProtocol protocol) {
        super(protocol);
    }

    @Override
    public void perform(WebdavRequest request, WebdavResponse response) throws WebdavProtocolException {
        if (false == CardDAVCollection.class.isInstance(request.getResource())) {
            throw WebdavProtocolException.generalError(request.getUrl(), HttpServletResponse.SC_BAD_REQUEST);
        }
        Document requestBody = optRequestBody(request);
        CardDAVCollection collection = (CardDAVCollection) request.getResource();
        SearchTerm<?> searchTerm = parseFilter(requestBody.getRootElement().getChild("filter", CarddavProtocol.CARD_NS));
        List<WebdavResource> resources;
        if (null == searchTerm) {
            /*
             * get all elements in collection
             */
            resources = collection.getChildren();
        } else {
            /*
             * get filterered resources
             */
            resources = collection.getFilteredObjects(searchTerm);
        }
        ResourceMarshaller marshaller = getMarshaller(request, requestBody);
        Element multistatusElement = prepareMultistatusElement();
        List<Element> elements = new ArrayList<Element>();
        for (WebdavResource resource : resources) {
            elements.addAll(marshaller.marshal(resource));
        }
        multistatusElement.addContent(elements);
        sendMultistatusResponse(response, multistatusElement);
    }

    private SearchTerm<?> parsePropFilter(Element propFilterElement) throws WebdavProtocolException {
        String name = propFilterElement.getAttributeValue("name");
        ContactField[] matchingFields = getMatchingFields(name);
        if (null == matchingFields || 0 == matchingFields.length) {
            throw new PreconditionException(CarddavProtocol.CARD_NS.getURI(), "supported-filter", null, HttpServletResponse.SC_FORBIDDEN);
        }
        Element isNotDefinedElement = propFilterElement.getChild("is-not-defined", CarddavProtocol.CARD_NS);
        if (null != isNotDefinedElement) {
            if (1 == matchingFields.length) {
                SingleSearchTerm isNullTerm = new SingleSearchTerm(SingleOperation.ISNULL);
                isNullTerm.addOperand(new ContactFieldOperand(matchingFields[0]));
                return isNullTerm;
            }
            CompositeSearchTerm compositeTerm = new CompositeSearchTerm(CompositeOperation.AND);
            for (ContactField field : matchingFields) {
                SingleSearchTerm isNullTerm = new SingleSearchTerm(SingleOperation.ISNULL);
                isNullTerm.addOperand(new ContactFieldOperand(field));
                compositeTerm.addSearchTerm(isNullTerm);
            }
            return compositeTerm;
        }
        Element textMatchElement = propFilterElement.getChild("text-match", CarddavProtocol.CARD_NS);
        String matchType = textMatchElement.getAttributeValue("match-type");
        String match = textMatchElement.getValue();
        if (Strings.isEmpty(matchType) || "contains".equals(matchType)) {
            match = '*' + match + '*';
        } else if ("starts-with".equals(matchType)) {
            match = match + '*';
        } else if ("ends-with".equals(matchType)) {
            match = '*' + match;
        }
        SearchTerm<?> term;
        if (1 == matchingFields.length) {
            SingleSearchTerm singleSearchTerm = new SingleSearchTerm(SingleOperation.EQUALS);
            singleSearchTerm.addOperand(new ContactFieldOperand(matchingFields[0]));
            singleSearchTerm.addOperand(new ConstantOperand<String>(match));
            term = singleSearchTerm;
        } else {
            CompositeSearchTerm compositeTerm = new CompositeSearchTerm(CompositeOperation.AND);
            for (ContactField field : matchingFields) {
                SingleSearchTerm singleSearchTerm = new SingleSearchTerm(SingleOperation.EQUALS);
                singleSearchTerm.addOperand(new ContactFieldOperand(field));
                singleSearchTerm.addOperand(new ConstantOperand<String>(match));
                compositeTerm.addSearchTerm(singleSearchTerm);
            }
            term = compositeTerm;
        }
        if ("yes".equals(textMatchElement.getAttribute("negate-condition"))) {
            CompositeSearchTerm notTerm = new CompositeSearchTerm(CompositeOperation.NOT);
            notTerm.addSearchTerm(term);
            term = notTerm;
        }
        return term;
    }

    private static ContactField[] getMatchingFields(String attributeName) {
        if (null == attributeName) {
            return null;
        }
        switch (attributeName) {
            case "EMAIL":
                return new ContactField[] { ContactField.EMAIL1, ContactField.EMAIL2, ContactField.EMAIL3 };
            case "FN":
                return new ContactField[] { ContactField.DISPLAY_NAME };
            case "N":
                return new ContactField[] { ContactField.SUR_NAME, ContactField.GIVEN_NAME, ContactField.MIDDLE_NAME, ContactField.TITLE, ContactField.SUFFIX };
            case "NICKNAME":
                return new ContactField[] { ContactField.NICKNAME };
            case "COMPANY":
                return new ContactField[] { ContactField.COMPANY };
            default:
                return null;
        }
    }

    private SearchTerm<?> parseFilter(Element filterElement) throws WebdavProtocolException {
        if (null == filterElement) {
            return null;
        }
        List<Element> propFilterElements = filterElement.getChildren("prop-filter", CarddavProtocol.CARD_NS);
        if (null == propFilterElements || 0 == propFilterElements.size()) {
            return null;
        }
        if (1 == propFilterElements.size()) {
            return parsePropFilter(propFilterElements.get(0));
        }
        CompositeOperation operation = CompositeOperation.OR;
        Attribute testAttribute = filterElement.getAttribute("test");
        if (null != testAttribute && "allof".equals(testAttribute.getValue())) {
            operation = CompositeOperation.AND;
        }
        CompositeSearchTerm compositeTerm = new CompositeSearchTerm(operation);
        for (Element propFilterElement : propFilterElements) {
            compositeTerm.addSearchTerm(parsePropFilter(propFilterElement));
        }
        return compositeTerm;
    }

}
