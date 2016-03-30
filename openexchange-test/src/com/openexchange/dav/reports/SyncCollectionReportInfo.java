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

import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.openexchange.dav.PropertyNames;

/**
 * {@link SyncCollectionReportInfo}
 * 
 * Encapsulates the BODY of a {@link SyncCollectionReport} request ("sync-collection").
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SyncCollectionReportInfo extends ReportInfo {

    private final String syncToken;

    /**
     * Creates a new {@link SyncCollectionReportInfo}.
     * @param syncToken The sync-token to include in the request 
     */
    public SyncCollectionReportInfo(final String syncToken) {
    	this(syncToken, null);
    }

    /**
     * Creates a new {@link SyncCollectionReportInfo}.
     * @param syncToken The sync-token to include in the request 
     * @param propertyNames the properties to include in the request
     */
    public SyncCollectionReportInfo(final String syncToken, final DavPropertyNameSet propertyNames) {
        super(SyncCollectionReport.SYNC_COLLECTION, DavConstants.DEPTH_1, propertyNames);
        this.syncToken = syncToken;
    }
    
    /**
     * Creates a new {@link SyncCollectionReportInfo}.
     * @param propertyNames the properties to include in the request
     */
    public SyncCollectionReportInfo(final DavPropertyNameSet propertyNames) {
    	this(null, propertyNames);
    }
    
    @Override
    public Element toXml(final Document document) {
    	/*
    	 * create sync-collection element
    	 */
    	final Element syncCollectionElement = DomUtil.createElement(document, 
    			SyncCollectionReport.SYNC_COLLECTION.getLocalName(), SyncCollectionReport.SYNC_COLLECTION.getNamespace());
    	syncCollectionElement.setAttributeNS(Namespace.XMLNS_NAMESPACE.getURI(), 
    			Namespace.XMLNS_NAMESPACE.getPrefix() + ":" + PropertyNames.NS_DAV.getPrefix(), PropertyNames.NS_DAV.getURI());
    	/*
    	 * append sync-token element
    	 */
    	if (null != this.syncToken) {
            syncCollectionElement.appendChild(DomUtil.createElement(
            		document, PropertyNames.SYNC_TOKEN.getName(), PropertyNames.NS_DAV, this.syncToken));
    	}
    	/*
    	 * append properties element
    	 */
    	syncCollectionElement.appendChild(super.getPropertyNameSet().toXml(document));
        return syncCollectionElement;
    }
}
