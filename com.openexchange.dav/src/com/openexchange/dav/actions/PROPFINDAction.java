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

package com.openexchange.dav.actions;

import static com.openexchange.webdav.protocol.Protocol.DAV_NS;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.XMLOutputter;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.webdav.action.WebdavResponse;
import com.openexchange.webdav.protocol.Protocol;

/**
 * {@link PROPFINDAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public abstract class PROPFINDAction extends DAVAction {

    protected static final XMLOutputter OUTPUTTER = new XMLOutputter();

    /**
     * Initializes a new {@link PROPFINDAction}.
     *
     * @param protocol The underlying WebDAV protocol
     */
    public PROPFINDAction(DAVProtocol protocol) {
        super(protocol);
    }

    /**
     * Prepares an XML element ready to be used as root element for a multistatus response, containing any additionally defined
     * namespace declarations of the underlying protocol.
     *
     * @return A new multistatus element
     */
    protected Element prepareMultistatusElement() {
        Element multistatusElement = new Element("multistatus", DAV_NS);
        for (Namespace namespace : protocol.getAdditionalNamespaces()) {
            multistatusElement.addNamespaceDeclaration(namespace);
        }
        return multistatusElement;
    }

    /**
     * Sends a multistatus response.
     *
     * @param response The WebDAV response to write to
     * @param multistatusElement The root element for the multistatus response
     */
    protected void sendMultistatusResponse(WebdavResponse response, Element multistatusElement) {
        sendXMLResponse(response, new Document(multistatusElement), Protocol.SC_MULTISTATUS);
    }

}
