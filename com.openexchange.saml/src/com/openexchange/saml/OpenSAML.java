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

package com.openexchange.saml;

import java.io.StringReader;
import javax.xml.namespace.QName;
import org.joda.time.format.DateTimeFormatter;
import org.opensaml.Configuration;
import org.opensaml.saml1.binding.artifact.SAML1ArtifactBuilderFactory;
import org.opensaml.saml2.binding.artifact.SAML2ArtifactBuilderFactory;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.parse.ParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.security.SecurityConfiguration;
import org.opensaml.xml.util.XMLHelper;
import org.opensaml.xml.validation.ValidatorSuite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A wrapper class around {@link Configuration} combined with some utility methods.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public final class OpenSAML {

    /**
     * Gets the date format used to string'ify SAML's {@link org.joda.time.DateTime} objects.
     *
     * @return date format used to string'ify date objects
     */
    public DateTimeFormatter getSAMLDateFormatter() {
        return Configuration.getSAMLDateFormatter();
    }

    /**
     * Gets the artifact factory for the library.
     *
     * @return artifact factory for the library
     */
    public SAML1ArtifactBuilderFactory getSAML1ArtifactBuilderFactory() {
        return Configuration.getSAML1ArtifactBuilderFactory();
    }

    /**
     * Gets the artifact factory for the library.
     *
     * @return artifact factory for the library
     */
    public SAML2ArtifactBuilderFactory getSAML2ArtifactBuilderFactory() {
        return Configuration.getSAML2ArtifactBuilderFactory();
    }

    /**
     * Get the currently configured ParserPool instance.
     *
     * @return the currently ParserPool
     */
    public ParserPool getParserPool() {
        return Configuration.getParserPool();
    }

    /**
     * Gets the QName for the object provider that will be used for XMLObjects that do not have a registered object provider.
     *
     * @return the QName for the default object provider
     */
    public QName getDefaultProviderQName() {
        return Configuration.getDefaultProviderQName();
    }

    /**
     * Gets the XMLObject builder factory that has been configured with information from loaded configuration files.
     *
     * @return the XMLObject builder factory
     */
    public XMLObjectBuilderFactory getBuilderFactory() {
        return Configuration.getBuilderFactory();
    }

    /**
     * Gets the XMLObject marshaller factory that has been configured with information from loaded configuration files.
     *
     * @return the XMLObject marshaller factory
     */
    public MarshallerFactory getMarshallerFactory() {
        return Configuration.getMarshallerFactory();
    }

    /**
     * Gets the XMLObject unmarshaller factory that has been configured with information from loaded configuration files.
     *
     * @return the XMLObject unmarshaller factory
     */
    public UnmarshallerFactory getUnmarshallerFactory() {
        return Configuration.getUnmarshallerFactory();
    }

    /**
     * Gets a configured ValidatorSuite by its ID.
     *
     * @param suiteId the suite's ID
     * @return the ValidatorSuite or null if no suite was registered under that ID
     */
    public ValidatorSuite getValidatorSuite(String suiteId) {
        return Configuration.getValidatorSuite(suiteId);
    }

    /**
     * Get the global security configuration.
     *
     * @return the global security configuration instance
     */
    public SecurityConfiguration getGlobalSecurityConfiguration() {
        return Configuration.getGlobalSecurityConfiguration();
    }

    /**
     * Builds an instance of the given SAML type
     *
     * @param clazz The class to build an instance of
     * @return The built instance
     * @throws IllegalArgumentException If the passed class does not specify a public static field "DEFAULT_ELEMENT_NAME" of type QName
     */
    @SuppressWarnings("unchecked")
    public <T> T buildSAMLObject(Class<T> clazz) {
        QName defaultElementName;
        try {
            defaultElementName = (QName) clazz.getDeclaredField("DEFAULT_ELEMENT_NAME").get(null);
            return (T) getBuilderFactory().getBuilder(defaultElementName).buildObject(defaultElementName);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        } catch (SecurityException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    /**
     * Takes an {@link XMLObject} and marshalls it into its string representation.
     *
     * @param object The XML object
     * @return The XML string
     */
    public String marshall(XMLObject object) throws MarshallingException {
        Element authRequestElement = getMarshallerFactory().getMarshaller(object).marshall(object);
        return XMLHelper.nodeToString(authRequestElement);
    }

    @SuppressWarnings("unchecked")
    public <T> T unmarshall(Class<T> clazz, String xml) throws XMLParserException, UnmarshallingException, ClassCastException {
        Document doc = getParserPool().parse(new StringReader(xml));
        Element element = doc.getDocumentElement();
        return (T) getUnmarshallerFactory().getUnmarshaller(element).unmarshall(element);
    }
}
