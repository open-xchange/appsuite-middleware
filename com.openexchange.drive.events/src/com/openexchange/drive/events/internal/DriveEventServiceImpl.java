/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.drive.events.internal;

import static com.openexchange.file.storage.FileStorageEventConstants.FILE_NAME;
import static com.openexchange.file.storage.FileStorageEventConstants.FOLDER_ID;
import static com.openexchange.file.storage.FileStorageEventConstants.FOLDER_PATH;
import static com.openexchange.file.storage.FileStorageEventConstants.OLD_PARENT_FOLDER_ID;
import static com.openexchange.file.storage.FileStorageEventConstants.PARENT_FOLDER_ID;
import static com.openexchange.file.storage.FileStorageEventConstants.SESSION;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.osgi.service.event.Event;
import com.openexchange.config.ConfigurationService;
import com.openexchange.drive.DriveService;
import com.openexchange.drive.DriveUtility;
import com.openexchange.drive.events.DriveEvent;
import com.openexchange.drive.events.DriveEventImpl;
import com.openexchange.drive.events.DriveEventPublisher;
import com.openexchange.drive.events.DriveEventService;
import com.openexchange.exception.ExceptionUtils;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageEventConstants;
import com.openexchange.file.storage.FileStorageEventHelper;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link DriveEventServiceImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveEventServiceImpl implements org.osgi.service.event.EventHandler, DriveEventService {

    /** The logger constant */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DriveEventServiceImpl.class);

    private final List<DriveEventPublisher> publishers;
    private final ConcurrentMap<Integer, FolderBuffer> folderBuffers;
    private final ScheduledTimerTask periodicPublisher;
    private final int consolidationTime;
    private final int maxDelayTime;
    private final int defaultDelayTime;
    private final DriveUtility driveUtility;

    /**
     * Initializes a new {@link DriveEventServiceImpl}.
     *
     * @throws OXException If services are missing
     */
    public DriveEventServiceImpl() throws OXException {
        super();
        this.publishers = new CopyOnWriteArrayList<DriveEventPublisher>();
        final ConcurrentMap<Integer, FolderBuffer> folderBuffers = new ConcurrentHashMap<Integer, FolderBuffer>();
        this.folderBuffers = folderBuffers;
        this.driveUtility = DriveEventServiceLookup.getService(DriveService.class, true).getUtility();
        ConfigurationService configService = DriveEventServiceLookup.getService(ConfigurationService.class, true);
        this.consolidationTime = configService.getIntProperty("com.openexchange.drive.events.consolidationTime", 1000);
        this.maxDelayTime = configService.getIntProperty("com.openexchange.drive.events.maxDelayTime", 10000);
        this.defaultDelayTime = configService.getIntProperty("com.openexchange.drive.events.defaultDelayTime", 2500);
        int publisherDelay = configService.getIntProperty("com.openexchange.drive.events.publisherDelay", 2500);
        this.periodicPublisher = DriveEventServiceLookup.getService(TimerService.class, true).scheduleWithFixedDelay(() -> {
            try {
                for (Iterator<FolderBuffer> iterator = folderBuffers.values().iterator(); iterator.hasNext();) {
                    FolderBuffer buffer = iterator.next();
                    if (buffer.isReady()) {
                        iterator.remove();
                        notifyPublishers(buffer);
                    }
                }
            } catch (Exception e) {
                LOG.warn("error publishing drive events.", e);
            } catch (Throwable t) {
                ExceptionUtils.handleThrowable(t);
                LOG.warn("error publishing drive events.", t);
            }
        }, publisherDelay, publisherDelay);
    }

    /**
     * Stops distributing events by canceling the periodic publisher.
     */
    public void stop() {
        if (null != periodicPublisher) {
            periodicPublisher.cancel();
        }
    }

    /**
     * Notifies all registered publishers about the supplied drive event.
     *
     * @param event The event to distribute
     */
    public void notifyPublishers(DriveEvent event) {
        LOG.debug("Publishing: {}", event);
        for (DriveEventPublisher publisher : publishers) {
            if (event.isRemote() && publisher.isLocalOnly()) {
                // skip
            } else {
                publisher.publish(event);
            }
        }
    }

    void notifyPublishers(FolderBuffer buffer) {
        if (null != buffer) {
            Set<String> folderIDs = buffer.getFolderIDs();
            if (null != folderIDs && 0 < folderIDs.size()) {
                notifyPublishers(new DriveEventImpl(buffer.getContexctID(), folderIDs, buffer.getFolderContentChanges(), buffer.isContentsChangedOnly(), false, buffer.getPushToken()));
            }
        }
    }

    private static boolean check(Event event) {
        return null != event && event.containsProperty(SESSION) && (event.containsProperty(FOLDER_ID) || event.containsProperty(PARENT_FOLDER_ID));
    }

    static boolean isAboutChangedContents(Event event) {
        String topic = event.getTopic();
        return FileStorageEventConstants.CREATE_TOPIC.equals(topic) || FileStorageEventConstants.UPDATE_TOPIC.equals(topic) || FileStorageEventConstants.DELETE_TOPIC.equals(topic);
    }

    @Override
    public void handleEvent(final Event event) {
        try {
            LOG.trace("{}", new Object() {

                @Override
                public String toString() {
                    return FileStorageEventHelper.createDebugMessage("event", event);
                }
            });
            /*
             * check event
             */
            if (false == check(event)) {
                LOG.debug("Unable to handle incomplete event: {}", event);
                return;
            }
            Session session = (Session) event.getProperty(SESSION);
            String fileName = (String) event.getProperty(FILE_NAME);
            if (Strings.isNotEmpty(fileName) && (driveUtility.isInvalidFileName(fileName) || driveUtility.isIgnoredFileName(fileName, session))) {
                LOG.trace("Skipping event processing for ignored file: {}", fileName);
                return;
            }
            AbstractTask<Void> insertTask = createInsertTask(event, session, folderBuffers, consolidationTime, maxDelayTime, defaultDelayTime);
            /*
             * add event to buffer asynchronously if possible
             */
            ThreadPoolService threadPoolService = DriveEventServiceLookup.getService(ThreadPoolService.class, false);
            if (null != threadPoolService) {
                threadPoolService.submit(insertTask, CallerRunsBehavior.<Void> getInstance());
            } else {
                insertTask.call();
            }
        } catch (Exception e) {
            LOG.warn("Error adding drive event to folder buffer ", e);
        }
    }

    @Override
    public void registerPublisher(DriveEventPublisher publisher) {
        if (publishers.add(publisher)) {
            LOG.debug("Added drive event publisher: {}", publisher);
        }
    }

    @Override
    public void unregisterPublisher(DriveEventPublisher publisher) {
        if (publishers.remove(publisher)) {
            LOG.debug("Removed drive event publisher: {}", publisher);
        }
    }

    /**
     * Creates a task to insert affected folders into buffer
     */
    private AbstractTask<Void> createInsertTask(Event event, Session session, ConcurrentMap<Integer, FolderBuffer> folderBuffers, int consolidationTime, int maxDelayTime, int defaultDelayTime) {
        return new AbstractTask<Void>() {

            @Override
            public Void call() throws Exception {
                /*
                 * extract properties
                 */
                Integer contextID = Integer.valueOf(session.getContextId());
                String folderID = (String) (event.containsProperty(PARENT_FOLDER_ID) ? event.getProperty(PARENT_FOLDER_ID) : event.getProperty(FOLDER_ID));
                String oldParentFolderID = (String) event.getProperty(OLD_PARENT_FOLDER_ID);
                String[] folderPath = (String[]) event.getProperty(FOLDER_PATH);
                /*
                 * get buffer for this context
                 */
                FolderBuffer buffer = folderBuffers.get(contextID);
                if (null == buffer) {
                    buffer = new FolderBuffer(contextID.intValue(), consolidationTime, maxDelayTime, defaultDelayTime);
                    FolderBuffer existingBuffer = folderBuffers.putIfAbsent(contextID, buffer);
                    if (null != existingBuffer) {
                        buffer = existingBuffer;
                    }
                }
                /*
                 * add to buffer
                 */
                buffer.add(session, folderID, null != folderPath ? Arrays.asList(folderPath) : null, isAboutChangedContents(event));
                if (null != oldParentFolderID) {
                    buffer.add(session, oldParentFolderID, null, false);
                }
                return null;
            }
        };
    }

}
