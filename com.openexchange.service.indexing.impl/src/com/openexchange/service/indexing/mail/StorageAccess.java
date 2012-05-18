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

package com.openexchange.service.indexing.mail;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.exception.OXException;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexDocument.Type;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.index.IndexResult;
import com.openexchange.index.QueryParameters.Builder;
import com.openexchange.index.SearchHandler;
import com.openexchange.index.mail.MailIndexField;
import com.openexchange.mail.IndexRange;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.smal.SmalAccessService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.service.indexing.impl.Services;
import com.openexchange.service.indexing.mail.job.AbstractMailJob;
import com.openexchange.session.Session;

/**
 * {@link StorageAccess} - Provides access to both index and mail storage.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class StorageAccess implements Serializable {

    /**
     * The serial version UID.
     */
    private static final long serialVersionUID = -9004934781962232958L;

    /**
     * ID, FLAGS, and COLOR_LABEL
     */
    public static final MailField[] FIELDS = new MailField[] { MailField.ID, MailField.FLAGS, MailField.COLOR_LABEL };

    /**
     * The mail index document type.
     */
    private static final Type MAIL = IndexDocument.Type.MAIL;

    private final MailJobInfo info;

    private transient volatile IndexAccess<MailMessage> indexAccess;

    private transient MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess;

    /**
     * Initializes a new {@link StorageAccess}.
     * 
     * @param info The job info
     */
    public StorageAccess(final MailJobInfo info) {
        super();
        this.info = info;
    }

    /**
     * The writeObject method is responsible for writing the state of the object for its particular class so that the corresponding
     * readObject method can restore it. The default mechanism for saving the Object's fields can be invoked by calling
     * out.defaultWriteObject. The method does not need to concern itself with the state belonging to its superclasses or subclasses. State
     * is saved by writing the individual fields to the ObjectOutputStream using the writeObject method or by using the methods for
     * primitive data types supported by DataOutput.
     * 
     * @param out The object output stream
     * @throws IOException If an I/O error occurs
     */
    private void writeObject(final java.io.ObjectOutputStream out) throws IOException {
        close();
        out.defaultWriteObject();
    }

    /**
     * The readObject method is responsible for reading from the stream and restoring the
     * classes fields. It may call in.defaultReadObject to invoke the default mechanism for restoring the object's non-static and
     * non-transient fields. The defaultReadObject method uses information in the stream to assign the fields of the object saved in the
     * stream with the correspondingly named fields in the current object. This handles the case when the class has evolved to add new
     * fields. The method does not need to concern itself with the state belonging to its superclasses or subclasses. State is saved by
     * writing the individual fields to the ObjectOutputStream using the writeObject method or by using the methods for primitive data types
     * supported by DataOutput.
     * 
     * @param in The object input stream
     * @throws IOException If an I/O error occurs
     * @throws ClassNotFoundException If a class cast error occurs
     */
    private void readObject(final java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        /*
         * Restore common fields
         */
        in.defaultReadObject();
        close();
    }

    /**
     * Closes this storage access.
     */
    public void close() {
        releaseAccess();
        releaseMailAccess();
    }

    /**
     * Gets the associated index access.
     * 
     * @return The index access
     * @throws OXException If access cannot be returned
     */
    public IndexAccess<MailMessage> getIndexAccess() throws OXException {
        IndexAccess<MailMessage> tmp = indexAccess;
        if (null == tmp) {
            synchronized (this) {
                tmp = indexAccess;
                if (null == tmp) {
                    final IndexFacadeService service = Services.getService(IndexFacadeService.class);
                    if (null == service) {
                        throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(IndexFacadeService.class.getName());
                    }
                    tmp = service.acquireIndexAccess(com.openexchange.groupware.Types.EMAIL, info.userId, info.contextId);
                    indexAccess = tmp;
                }
            }
        }
        return tmp;
    }

    /**
     * Releases associated index access.
     */
    public void releaseAccess() {
        final IndexAccess<MailMessage> indexAccess = this.indexAccess;
        if (null == indexAccess) {
            return;
        }
        final IndexFacadeService indexFacade = Services.getService(IndexFacadeService.class);
        if (null != indexFacade) {
            try {
                indexFacade.releaseIndexAccess(indexAccess);
            } catch (final OXException e) {
                final Log log = com.openexchange.log.Log.valueOf(LogFactory.getLog(AbstractMailJob.class));
                log.warn("Closing index access failed.", e);
            } catch (final RuntimeException e) {
                final Log log = com.openexchange.log.Log.valueOf(LogFactory.getLog(AbstractMailJob.class));
                log.warn("Closing index access failed.", e);
            } finally {
                this.indexAccess = null;
            }
        }
    }

    /**
     * Gets the tracked SMAL access service.
     * 
     * @return The SMAL access service
     */
    private SmalAccessService getSmalAccessService() {
        return Services.getService(SmalAccessService.class);
    }

    /**
     * Gets a connected {@link MailAccess} instance appropriate for this job.
     * 
     * @return The new {@link MailAccess} instance
     * @throws OXException If initialization of {@link MailAccess} instance fails
     */
    public MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccessFor() throws OXException {
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> tmp = mailAccess;
        if (null == tmp) {
            synchronized (this) {
                tmp = mailAccess;
                if (null == tmp) {
                    /*
                     * Fake session & signaling not to lookup cache
                     */
                    final Session session = new FakeSession(info.primaryPassword, info.userId, info.contextId);
                    session.setParameter("com.openexchange.mail.lookupMailAccessCache", Boolean.FALSE);
                    tmp = getSmalAccessService().getUnwrappedInstance(session, info.accountId);
                    /*
                     * Safety close & not cacheable
                     */
                    tmp.close(true);
                    tmp.setCacheable(false);
                    /*
                     * Parameterize configuration
                     */
                    final MailConfig mailConfig = tmp.getMailConfig();
                    mailConfig.setLogin(info.login);
                    mailConfig.setPassword(info.password);
                    mailConfig.setServer(info.server);
                    mailConfig.setPort(info.port);
                    mailConfig.setSecure(info.secure);
                    tmp.connect(true);
                    this.mailAccess = tmp;
                }
            }
        }
        return tmp;
    }

    /**
     * Closes associated mail access.
     */
    public void releaseMailAccess() {
        getSmalAccessService().closeUnwrappedInstance(mailAccess);
        mailAccess = null;
    }

    /**
     * Gets all messages from specified folder retrieved from index.
     * 
     * @param fullName The folder's full name
     * @return All messages
     * @throws OXException If retrieval fails
     */
    public List<MailMessage> allMailsFromIndex(final String fullName) throws OXException, InterruptedException {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("accountId", Integer.valueOf(info.accountId));
        final Builder queryBuilder =
            new Builder(params).setType(MAIL).setFolder(fullName).setSortField(MailIndexField.RECEIVED_DATE).setOrder("desc").setHandler(
                SearchHandler.ALL_REQUEST);
        final IndexResult<MailMessage> indexResult = getIndexAccess().query(queryBuilder.build(), null);
        if (0 >= indexResult.getNumFound()) {
            return Collections.emptyList();
        }
        final List<IndexDocument<MailMessage>> results = indexResult.getResults();
        final List<MailMessage> mails = new ArrayList<MailMessage>(results.size());
        for (final IndexDocument<MailMessage> indexDocument : results) {
            mails.add(indexDocument.getObject());
        }
        return mails;
    }

    /**
     * Gets all messages from specified folder retrieved from storage.
     * 
     * @param fullName The folder's full name
     * @param mailFields
     * @return All messages
     * @throws OXException If retrieval fails
     */
    public List<MailMessage> allMailsFromStorage(final String fullName) throws OXException {
        return allMailsFromStorage(fullName, FIELDS);
    }

    /**
     * Gets all messages from specified folder retrieved from storage.
     * 
     * @param fullName The folder's full name
     * @param mailFields
     * @return All messages
     * @throws OXException If retrieval fails
     */
    public List<MailMessage> allMailsFromStorage(final String fullName, final MailField[] mailFields) throws OXException {
        return Arrays.asList(mailAccessFor().getMessageStorage().searchMessages(
            fullName,
            IndexRange.NULL,
            MailSortField.RECEIVED_DATE,
            OrderDirection.ASC,
            null,
            mailFields == null ? FIELDS : mailFields));
    }

}
