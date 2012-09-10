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

package com.openexchange.mail.smal.impl.index;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * {@link IndexEventHandler} - Starts/drops periodic jobs for a user by tracking added/removed sessions.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IndexEventHandler implements EventHandler {
    
    //FIXME:

    @Override
    public void handleEvent(Event event) {
        // TODO Auto-generated method stub
        
    }

//    protected static final org.apache.commons.logging.Log LOG =
//        com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(IndexEventHandler.class));
//
//    private final ConcurrentMap<Key, ConcurrentMap<String, ElapsedFolderJob>> periodicJobs;
//
//    private final ScheduledTimerTask timerTask;
//
//    /**
//     * Initializes a new {@link IndexEventHandler}.
//     */
//    public IndexEventHandler() {
//        super();
//        periodicJobs = new ConcurrentHashMap<Key, ConcurrentMap<String,ElapsedFolderJob>>();
//        final TimerService timerService = SmalServiceLookup.getServiceStatic(TimerService.class);
//        timerTask = timerService.scheduleWithFixedDelay(new PeriodicRunnable(periodicJobs), Constants.HOUR_MILLIS, Constants.HOUR_MILLIS);
//    }
//
//    /**
//     * Closes this event handler.
//     */
//    public void close() {
//        for (final Iterator<ConcurrentMap<String, ElapsedFolderJob>> iter = periodicJobs.values().iterator(); iter.hasNext();) {
//            final ConcurrentMap<String, ElapsedFolderJob> jobs = iter.next();
//            iter.remove();
//            for (final ElapsedFolderJob job : jobs.values()) {
//                job.cancel();
//            }
//        }
//        periodicJobs.clear();
//        timerTask.cancel(true);
//    }
//
//    private boolean addPeriodicJob(final ElapsedFolderJob job, final Session session) {
//        final Key key = keyFor(session);
//        ConcurrentMap<String, ElapsedFolderJob> jobs = periodicJobs.get(key);
//        if (null == jobs) {
//            final ConcurrentMap<String, ElapsedFolderJob> newjobs = new ConcurrentHashMap<String, ElapsedFolderJob>();
//            jobs = periodicJobs.putIfAbsent(key, newjobs);
//            if (null == jobs) {
//                jobs = newjobs;
//            }
//        }
//        return (null == jobs.putIfAbsent(job.getIdentifier(), job));
//    }
//
//    private void dropForLast(final Session session) {
//        final ConcurrentMap<String, ElapsedFolderJob> jobs = periodicJobs.remove(keyFor(session));
//        if (null == jobs) {
//            return;
//        }
//        for (final ElapsedFolderJob job : jobs.values()) {
//            job.cancel();
//        }
//    }
//
//    @Override
//    public void handleEvent(final Event event) {
//        final String topic = event.getTopic();
//        if (SessiondEventConstants.TOPIC_REMOVE_DATA.equals(topic)) {
//            @SuppressWarnings("unchecked") final Map<String, Session> container =
//                (Map<String, Session>) event.getProperty(SessiondEventConstants.PROP_CONTAINER);
//            for (final Session session : container.values()) {
//                handleDroppedSession(session);
//            }
//        } else if (SessiondEventConstants.TOPIC_REMOVE_SESSION.equals(topic)) {
//            final Session session = (Session) event.getProperty(SessiondEventConstants.PROP_SESSION);
//            handleDroppedSession(session);
//        } else if (SessiondEventConstants.TOPIC_REMOVE_CONTAINER.equals(topic)) {
//            @SuppressWarnings("unchecked") final Map<String, Session> container =
//                (Map<String, Session>) event.getProperty(SessiondEventConstants.PROP_CONTAINER);
//            for (final Session session : container.values()) {
//                handleDroppedSession(session);
//            }
//        } else if (SessiondEventConstants.TOPIC_ADD_SESSION.equals(topic)) {
//            final Session session = (Session) event.getProperty(SessiondEventConstants.PROP_SESSION);
//            handleAddedSession(session);
//        } else if (SessiondEventConstants.TOPIC_REACTIVATE_SESSION.equals(topic)) {
//            final Session session = (Session) event.getProperty(SessiondEventConstants.PROP_SESSION);
//            handleAddedSession(session);
//        }
//    }
//
//    private void handleDroppedSession(final Session session) {
//        try {
//            final SessiondService sessiondService = SmalServiceLookup.getServiceStatic(SessiondService.class);
//            if (null != sessiondService && sessiondService.getAnyActiveSessionForUser(session.getUserId(), session.getContextId()) != null) {
//                dropForLast(session);
//            }
//        } catch (final Exception e) {
//            // Failed handling session
//            LOG.warn("Failed handling tracked removed session.", e);
//        }
//    }
//
//    private void handleAddedSession(final Session session) {
//        try {
//            /*
//             * Add jobs
//             */
//            final MailAccountStorageService storageService = SmalServiceLookup.getServiceStatic(MailAccountStorageService.class);
//            final int userId = session.getUserId();
//            final int contextId = session.getContextId();
//            final long start = System.currentTimeMillis() + Constants.HOUR_MILLIS;
//            final Set<String> filter = new HashSet<String>(8);
//            for (final MailAccount account : storageService.getUserMailAccounts(userId, contextId)) {
//                final int accountId = account.getId();
//
//                // FOR TESTING ! ! !
//                if (accountId != MailAccount.DEFAULT_ID) {
//                    continue;
//                }
//                final MailJobInfo.Builder jobInfoBuilder = new MailJobInfo.Builder(userId, contextId);
//                filter.add("INBOX");
//                if (MailAccount.DEFAULT_ID == accountId) {
//                    filter.addAll(getPrimaryFullNames(session, jobInfoBuilder));
//                } else {
//                    MailAccount acc = account;
//                    final String decryptedPW = MailPasswordUtil.decrypt(acc.getPassword(), session, accountId, acc.getLogin(), acc.getMailServer());
//                    jobInfoBuilder.accountId(accountId).login(acc.getLogin()).password(decryptedPW).primaryPassword(session.getPassword());
//                    jobInfoBuilder.port(acc.getMailPort()).server(acc.getMailServer()).secure(acc.isMailSecure());
//
//                    String fn = acc.getDraftsFullname();
//                    if (null == fn) {
//                        acc = Tools.checkFullNames(acc, storageService, session, null);
//                        fn = acc.getDraftsFullname();
//                    }
//                    filter.add(fn);
//
//                    fn = acc.getSentFullname();
//                    if (null == fn) {
//                        acc = Tools.checkFullNames(acc, storageService, session, null);
//                        fn = acc.getSentFullname();
//                    }
//                    filter.add(fn);
//
//                    // fn = acc.getTrashFullname();
//                    // if (null == fn) {
//                    // acc = Tools.checkFullNames(acc, storageService, session, null);
//                    // fn = acc.getTrashFullname();
//                    // }
//                    // filter.add(fn);
//
//                    /*
//                     * TODO: Add custom user folders specified by user
//                     */
//                }
//                /*
//                 * Create job
//                 */
//                final MailJobInfo jobInfo = jobInfoBuilder.build();
//                final MailAccountJob maj = new MailAccountJob(jobInfo, filter);
//                filter.clear();
//                final IndexingService indexingService = SmalServiceLookup.getServiceStatic(IndexingService.class);
//                if (null != indexingService) {
//                    indexingService.addJob(maj);
//                }
//                /*
//                 * Add periodic job
//                 */
//                addPeriodicJob(new ElapsedFolderJob(jobInfo, start), session);
//            }
//        } catch (final Exception e) {
//            // Failed handling session
//            LOG.warn("Failed handling tracked added session.", e);
//        }
//    }
//
//    private List<String> getPrimaryFullNames(final Session session, final Builder jobInfoBuilder) throws OXException {
//        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = null;
//        try {
//            mailAccess = SmalMailAccess.getUnwrappedInstance(session, MailAccount.DEFAULT_ID);
//            mailAccess.connect(true);
//            // Fill builder
//            final MailConfig config = mailAccess.getMailConfig();
//            jobInfoBuilder.accountId(mailAccess.getAccountId()).login(config.getLogin()).password(
//                config.getPassword()).port(config.getPort()).server(config.getServer()).secure(config.isSecure()).primaryPassword(
//                session.getPassword());
//            // Add folders
//            final IMailFolderStorage folderStorage = mailAccess.getFolderStorage();
//            final List<String> fullNames = new ArrayList<String>(3);
//            fullNames.add(folderStorage.getDraftsFolder());
//            fullNames.add(folderStorage.getSentFolder());
//            // fullNames.add(folderStorage.getTrashFolder());
//            return fullNames;
//        } finally {
//            SmalMailAccess.closeUnwrappedInstance(mailAccess);
//        }
//    }
//
//    private static final class PeriodicRunnable implements Runnable {
//
//        private final ConcurrentMap<Key, ConcurrentMap<String, ElapsedFolderJob>> periodicJobs;
//
//        protected PeriodicRunnable(final ConcurrentMap<Key, ConcurrentMap<String, ElapsedFolderJob>> periodicJobs) {
//            super();
//            this.periodicJobs = periodicJobs;
//        }
//
//        @Override
//        public void run() {
//            final long now = System.currentTimeMillis();
//            for (final ConcurrentMap<String, ElapsedFolderJob> jobs : periodicJobs.values()) {
//                for (final ElapsedFolderJob job : jobs.values()) {
//                    if (job.mayStart(now)) {
//                        final IndexingService indexingService = SmalServiceLookup.getServiceStatic(IndexingService.class);
//                        if (null != indexingService) {
//                            try {
//                                indexingService.addJob(job);
//                            } catch (final OXException e) {
//                                LOG.warn("Job could not be scheduled.", e);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private static Key keyFor(final Session session) {
//        return new Key(session.getUserId(), session.getContextId());
//    }
//
//    private static final class Key {
//
//        private final int cid;
//
//        private final int user;
//
//        private final int hash;
//
//        protected Key(final int user, final int cid) {
//            super();
//            this.user = user;
//            this.cid = cid;
//            final int prime = 31;
//            int result = 1;
//            result = prime * result + cid;
//            result = prime * result + user;
//            hash = result;
//        }
//
//        @Override
//        public int hashCode() {
//            return hash;
//        }
//
//        @Override
//        public boolean equals(final Object obj) {
//            if (this == obj) {
//                return true;
//            }
//            if (!(obj instanceof Key)) {
//                return false;
//            }
//            final Key other = (Key) obj;
//            if (cid != other.cid) {
//                return false;
//            }
//            if (user != other.user) {
//                return false;
//            }
//            return true;
//        }
//
//    } // End of class Key

}
