/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.saml;

import java.io.StringReader;
import javax.xml.namespace.QName;
import org.opensaml.core.config.Configuration;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.config.SAMLConfigurationSupport;
import org.opensaml.saml.saml1.binding.artifact.SAML1ArtifactBuilderFactory;
import org.opensaml.saml.saml2.binding.artifact.SAML2ArtifactBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.openexchange.exception.OXException;
import net.shibboleth.utilities.java.support.xml.ParserPool;
import net.shibboleth.utilities.java.support.xml.SerializeSupport;
import net.shibboleth.utilities.java.support.xml.XMLParserException;

/**
 * A wrapper class around {@link Configuration} combined with some utility methods.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public final class OpenSAML {

    /**
     * Gets the artifact factory for the library.
     *
     * @return artifact factory for the library
     */
    public SAML1ArtifactBuilderFactory getSAML1ArtifactBuilderFactory() {
        return SAMLConfigurationSupport.getSAML1ArtifactBuilderFactory();
    }

    /**
     * Gets the artifact factory for the library.
     *
     * @return artifact factory for the library
     */
    public SAML2ArtifactBuilderFactory getSAML2ArtifactBuilderFactory() {
        return SAMLConfigurationSupport.getSAML2ArtifactBuilderFactory();
    }

    /**
     * Get the currently configured ParserPool instance.
     *
     * @return the currently ParserPool
     */
    public ParserPool getParserPool() {
        return XMLObjectProviderRegistrySupport.getParserPool();
    }

    /**
     * Gets the QName for the object provider that will be used for XMLObjects that do not have a registered object provider.
     *
     * @return the QName for the default object provider
     */
    public QName getDefaultProviderQName() {
        return XMLObjectProviderRegistrySupport.getDefaultProviderQName();
    }

    /**
     * Gets the XMLObject builder factory that has been configured with information from loaded configuration files.
     *
     * @return the XMLObject builder factory
     */
    public XMLObjectBuilderFactory getBuilderFactory() {
        return XMLObjectProviderRegistrySupport.getBuilderFactory();
    }

    /**
     * Gets the XMLObject marshaller factory that has been configured with information from loaded configuration files.
     *
     * @return the XMLObject marshaller factory
     */
    public MarshallerFactory getMarshallerFactory() {
        return XMLObjectProviderRegistrySupport.getMarshallerFactory();
    }

    /**
     * Gets the XMLObject unmarshaller factory that has been configured with information from loaded configuration files.
     *
     * @return the XMLObject unmarshaller factory
     */
    public UnmarshallerFactory getUnmarshallerFactory() {
        return XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
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
     * @throws OXException
     */
    public String marshall(XMLObject object) throws MarshallingException {
        Element authRequestElement = getMarshallerFactory().getMarshaller(object).marshall(object);
        return SerializeSupport.nodeToString(authRequestElement);
    }

    @SuppressWarnings("unchecked")
    public <T> T unmarshall(@SuppressWarnings("unused") Class<T> clazz, String xml) throws UnmarshallingException, ClassCastException, XMLParserException {
        Document doc = getParserPool().parse(new StringReader(xml));
        Element element = doc.getDocumentElement();
        return (T) getUnmarshallerFactory().getUnmarshaller(element).unmarshall(element);
    }
}
