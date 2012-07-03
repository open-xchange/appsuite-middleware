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

package com.openexchange.service.indexing.mail.job;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.QueryParameters;
import com.openexchange.index.SearchHandler;
import com.openexchange.index.solr.IndexFolderManager;
import com.openexchange.log.LogFactory;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.service.indexing.mail.MailJobInfo;

/**
 * {@link RemoveFolderJob}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RemoveFolderJob extends AbstractMailJob {

    private static final long serialVersionUID = -7677441731409939202L;

    private static final String SIMPLE_NAME = RemoveFolderJob.class.getSimpleName();

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(RemoveFolderJob.class));

    private final String fullName;

    /**
     * Initializes a new {@link RemoveFolderJob} with default span.
     * <p>
     * This job is performed is span is exceeded and if able to exclusively set sync flag.
     * 
     * @param fullName The folder full name
     * @param info The job information
     */
    public RemoveFolderJob(final String fullName, final MailJobInfo info) {
        super(info);
        this.fullName = fullName;
    }

    @Override
    protected void performMailJob() throws OXException, InterruptedException {
        try {
            /*
             * Check flags of contained mails
             */
            IndexFolderManager.deleteFolderEntry(contextId, userId, Types.EMAIL, String.valueOf(accountId), fullName);
            final IndexAccess<MailMessage> indexAccess = storageAccess.getIndexAccess();
            final Map<String, Object> params = new HashMap<String, Object>();
            params.put("accountId", Integer.valueOf(accountId));
            final QueryParameters query = new QueryParameters.Builder(params).setHandler(SearchHandler.ALL_REQUEST).setType(MAIL).setFolders(Collections.singleton(fullName)).build();
            indexAccess.deleteByQuery(query);            
        } catch (final RuntimeException e) {
            LOG.error(SIMPLE_NAME + " \"" + info + "\" failed.", e);
        }
    }

}
