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

import java.io.IOException;

import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpState;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.client.methods.ReportMethod;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.openexchange.dav.PropertyNames;

/**
 * {@link SyncCollectionReportMethod} - Report method for the 
 * "sync-collection" request.
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SyncCollectionReportMethod extends ReportMethod {
	
	private String syncToken;
	private Document responseDocument = null;

	public SyncCollectionReportMethod(String uri, ReportInfo reportInfo) throws IOException {
		super(uri, reportInfo);
		this.syncToken = null;
	}
	
	public String getSyncTokenFromResponse() {
		return syncToken;
	}
	
	public SyncCollectionResponse getResponseBodyAsSyncCollection() throws IOException, DavException {
        checkUsed();
		return new SyncCollectionResponse(this.getResponseBodyAsMultiStatus(), this.syncToken);  
	}
	
    @Override
    public Document getResponseBodyAsDocument() throws IOException {
    	if (null == this.responseDocument) {
    		this.responseDocument = super.getResponseBodyAsDocument();
    	}
        return responseDocument;
    }
	
    @Override
    protected void processResponseBody(HttpState httpState, HttpConnection httpConnection) {
    	super.processResponseBody(httpState, httpConnection);
    	Document document = null;
    	try {
			document = getResponseBodyAsDocument();
		} catch (IOException e) {
			e.printStackTrace();
		}
        if (null != document) {
            ElementIterator it = DomUtil.getChildren(
                document.getDocumentElement(),
                PropertyNames.SYNC_TOKEN.getName(),
                PropertyNames.SYNC_TOKEN.getNamespace());
            if (it.hasNext()) {
                Element respElem = it.nextElement();
                this.syncToken = respElem.getTextContent();
            }
        }
    }	

}
