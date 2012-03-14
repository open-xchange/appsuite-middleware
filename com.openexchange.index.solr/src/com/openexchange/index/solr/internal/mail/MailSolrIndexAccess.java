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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

import java.util.Collection;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexResult;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.TriggerType;
import com.openexchange.index.solr.internal.AbstractSolrIndexAccess;
import com.openexchange.index.solr.internal.SolrIndexIdentifier;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * {@link MailSolrIndexAccess}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailSolrIndexAccess extends AbstractSolrIndexAccess<MailMessage> {

    private final TriggerType triggerType;

    /**
     * Initializes a new {@link MailSolrIndexAccess}.
     * 
     * @param identifier The Solr server identifier
     */
    public MailSolrIndexAccess(final SolrIndexIdentifier identifier, final TriggerType triggerType) {
        super(identifier);
        this.triggerType = triggerType;
    }

    @Override
    public void addEnvelopeData(final IndexDocument<MailMessage> document) throws OXException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.index.IndexAccess#addEnvelopeData(java.util.Collection)
     */
    @Override
    public void addEnvelopeData(final Collection<IndexDocument<MailMessage>> documents) throws OXException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.index.IndexAccess#addContent(com.openexchange.index.IndexDocument)
     */
    @Override
    public void addContent(final IndexDocument<MailMessage> document) throws OXException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.index.IndexAccess#addContent(java.util.Collection)
     */
    @Override
    public void addContent(final Collection<IndexDocument<MailMessage>> documents) throws OXException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.index.IndexAccess#addAttachments(com.openexchange.index.IndexDocument)
     */
    @Override
    public void addAttachments(final IndexDocument<MailMessage> document) throws OXException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.index.IndexAccess#addAttachments(java.util.Collection)
     */
    @Override
    public void addAttachments(final Collection<IndexDocument<MailMessage>> documents) throws OXException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.index.IndexAccess#deleteById(java.lang.String)
     */
    @Override
    public void deleteById(final String id) throws OXException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.index.IndexAccess#deleteByQuery(java.lang.String)
     */
    @Override
    public void deleteByQuery(final String query) throws OXException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.index.IndexAccess#query(com.openexchange.index.QueryParameters)
     */
    @Override
    public IndexResult<MailMessage> query(final QueryParameters parameters) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TriggerType getTriggerType() {
        return triggerType;
    }

}
