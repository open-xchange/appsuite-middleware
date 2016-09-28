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

package com.openexchange.jolokia.restrictor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.jolokia.restrictor.PolicyRestrictor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Factory for obtaining a {@link Restrictor} restricted to localhost
 * 
 * @author <a href="mailto:felix.marx@open-xchange.com">Felix Marx</a>
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 */
public final class RestrictorFactoryForLocalhost {
    
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RestrictorFactoryForLocalhost.class);

    private RestrictorFactoryForLocalhost() {
    }

    /**
     * Create a Restrictor with localhost restriciton
     * 
     * @return the restrictor created or <code>null</code> if failed.
     */
    public static PolicyRestrictor createPolicyRestrictor() {
        

        Document document;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            LOG.error("Error while creating PolicyRestrictor for localhost");
            return null;
        }

        Element host = document.createElement("host");
        host.setTextContent("localhost");
        Element hostByIp = document.createElement("host");
        hostByIp.setTextContent("127.0.0.0/8");
        Element hostByIpV6 = document.createElement("host");
        hostByIpV6.setTextContent("0:0:0:0:0:0:0:1");
        Element remote = document.createElement("remote");
        remote.appendChild(host);
        remote.appendChild(hostByIp);
        remote.appendChild(hostByIpV6);
        Element restrict = document.createElement("restrict");
        restrict.appendChild(remote);
        
        //Write access denied, rest of commands set
        Element commands = document.createElement("commands");
        Element read = document.createElement("command");
        read.setTextContent("read");
        Element exec = document.createElement("command");
        exec.setTextContent("exec");
        Element list = document.createElement("command");
        list.setTextContent("list");
        Element search = document.createElement("command");
        search.setTextContent("search");
        Element version = document.createElement("command");
        version.setTextContent("version");
        
        commands.appendChild(read);
        commands.appendChild(exec);
        commands.appendChild(list);
        commands.appendChild(search);
        commands.appendChild(version);
        
        restrict.appendChild(commands);
        
        document.appendChild(restrict);
        
        return new PolicyRestrictor(documentConverter(document));
    }
    
    /**
     * Converts a {@link Document} into an {@link InputStream}
     * 
     * @param document to convert
     * @return input stream based on document
     */
    private static InputStream documentConverter(Document document) {
        ByteArrayOutputStream outputstream = new ByteArrayOutputStream();
        Source source = new DOMSource(document);
        Result result = new StreamResult(outputstream);
        try {
            TransformerFactory.newInstance().newTransformer().transform(source, result);
        } catch (TransformerException | TransformerFactoryConfigurationError e) {
            LOG.error("Error while creating PolicyRestrictor for localhost");
            return null;
        }
        return new ByteArrayInputStream(outputstream.toByteArray());        
    }
}