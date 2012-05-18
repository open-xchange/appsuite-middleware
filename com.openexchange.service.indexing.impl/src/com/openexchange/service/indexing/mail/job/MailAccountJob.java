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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import com.openexchange.exception.OXException;
import com.openexchange.log.LogFactory;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.service.indexing.IndexingService;
import com.openexchange.service.indexing.impl.Services;
import com.openexchange.service.indexing.mail.Constants;
import com.openexchange.service.indexing.mail.MailJobInfo;

/**
 * {@link MailAccountJob}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccountJob extends AbstractMailJob {

    private static final long serialVersionUID = -382130217643032914L;

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(MailAccountJob.class));

    private static final String SIMPLE_NAME = MailAccountJob.class.getSimpleName();

    private final Set<String> filter;

    /**
     * Initializes a new {@link MailAccountJob}.
     * 
     * @param info The job information
     */
    public MailAccountJob(final MailJobInfo info) {
        this(info, Collections.<String> emptySet());
    }

    /**
     * Initializes a new {@link MailAccountJob}.
     * 
     * @param info The job information
     * @param filterFullNames The filter full names
     */
    public MailAccountJob(final MailJobInfo info, final String... filterFullNames) {
        this(info, new HashSet<String>(Arrays.asList(filterFullNames)));
    }

    /**
     * Initializes a new {@link MailAccountJob}.
     * 
     * @param info The job information
     * @param filter The filter full names
     */
    public MailAccountJob(final MailJobInfo info, final Set<String> filter) {
        super(info);
        this.filter = new HashSet<String>(filter);
    }

    private List<String> getList() throws OXException {
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
        try {
            mailAccess = storageAccess.mailAccessFor();
            mailAccess.connect(true);
            final List<String> fullNames = new LinkedList<String>();
            handleSubfolders(MailFolder.DEFAULT_FOLDER_ID, mailAccess.getFolderStorage(), fullNames);
            return fullNames;
        } finally {
            storageAccess.releaseMailAccess();
        }
    }

    private void handleSubfolders(final String fullName, final IMailFolderStorage folderStorage, final List<String> fullNames) throws OXException {
        for (final MailFolder mailFolder : folderStorage.getSubfolders(fullName, true)) {
            final String subFullName = mailFolder.getFullname();
            fullNames.add(subFullName);
            handleSubfolders(subFullName, folderStorage, fullNames);
        }
    }

    @Override
    protected void performMailJob() throws OXException, InterruptedException {
        try {
            final IndexingService indexingService = Services.getService(IndexingService.class);
            if (null == filter || filter.isEmpty()) {
                final List<String> list = getList();
                for (final String fullName : list) {
                    addJobIfShouldSync(indexingService, fullName);
                }
            } else {
                final List<String> list = new ArrayList<String>(filter);
                for (final String fullName : list) {
                    addJobIfShouldSync(indexingService, fullName);
                }
            }
        } catch (final RuntimeException e) {
            LOG.warn(SIMPLE_NAME + " failed: " + info, e);
        }
    }

    private void addJobIfShouldSync(final IndexingService indexingService, final String fullName) {
        final FolderJob folderJob = new FolderJob(fullName, info).setSpan(Constants.HOUR_MILLIS);
        try {
            indexingService.addJob(folderJob);
        } catch (final OXException e) {
            LOG.warn(fullName + " indexing job could not be scheduled: " + info, e);
        }
    }

}
