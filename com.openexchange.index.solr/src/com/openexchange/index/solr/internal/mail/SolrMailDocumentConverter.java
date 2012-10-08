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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.index.solr.internal.mail;

import java.util.List;
import java.util.Map;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexField;
import com.openexchange.index.IndexResult;
import com.openexchange.index.solr.internal.SolrIndexResult;
import com.openexchange.index.solr.internal.SolrResultConverter;
import com.openexchange.mail.dataobjects.ContentAwareMailMessage;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.text.TextFinder;


/**
 * {@link SolrMailDocumentConverter}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrMailDocumentConverter implements SolrResultConverter<MailMessage> {

    @Override
    public IndexDocument<MailMessage> convert(SolrDocument document) throws OXException {
        return convertStatic(document);
    }

    @Override
    public IndexResult<MailMessage> createIndexResult(List<IndexDocument<MailMessage>> documents, Map<IndexField, Map<String, Long>> facetCounts) throws OXException {
        return new SolrIndexResult<MailMessage>(documents.size(), documents, facetCounts);
    }
    
    public static SolrInputDocument convertStatic(int contextId, int userId, IndexDocument<MailMessage> document) throws OXException {
        MailMessage message = document.getObject();
        SolrInputDocument inputDocument = SolrMailHelper.getInstance().inputDocumentFor(message, userId, contextId);
        String text;
        // TODO: Can this be anything else?
        if (message instanceof ContentAwareMailMessage) {
            ContentAwareMailMessage contentAwareMessage = (ContentAwareMailMessage) message;
            text = contentAwareMessage.getPrimaryContent();
            if (text == null) {
                TextFinder textFinder = new TextFinder();
                text = textFinder.getText(message);
            }
        } else {
            TextFinder textFinder = new TextFinder();
            text = textFinder.getText(message);
        }
        
        if (null != text) {
            String contentField = SolrMailField.CONTENT.solrName();
            if (contentField != null) {
                inputDocument.setField(contentField, text);
            }
        }
        
        String contentFlagField = SolrMailField.CONTENT_FLAG.solrName();
        if (contentFlagField != null) {
            inputDocument.setField(contentFlagField, Boolean.TRUE);    
        }
        
        return inputDocument;
    }
    
    public static IndexDocument<MailMessage> convertStatic(SolrDocument document) throws OXException {
        return SolrMailHelper.getInstance().readDocument(document, MailFillers.allFillers());
    }

}
