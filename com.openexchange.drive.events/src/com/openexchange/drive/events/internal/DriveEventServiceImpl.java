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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.drive.events.internal;

import static com.openexchange.file.storage.FileStorageEventConstants.FOLDER_ID;
import static com.openexchange.file.storage.FileStorageEventConstants.FOLDER_PATH;
import static com.openexchange.file.storage.FileStorageEventConstants.PARENT_FOLDER_ID;
import static com.openexchange.file.storage.FileStorageEventConstants.SESSION;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.commons.logging.Log;
import org.osgi.service.event.Event;
import com.openexchange.config.ConfigurationService;
import com.openexchange.drive.DriveAction;
import com.openexchange.drive.DriveVersion;
import com.openexchange.drive.events.DriveEvent;
import com.openexchange.drive.events.DriveEventPublisher;
import com.openexchange.drive.events.DriveEventService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageEventHelper;
import com.openexchange.session.Session;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link DriveEventServiceImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveEventServiceImpl implements org.osgi.service.event.EventHandler, DriveEventService {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(DriveEventServiceImpl.class);

    private static final List<DriveAction<? extends DriveVersion>> SYNC_DIRECTORIES_ACTION;
    static {
        SYNC_DIRECTORIES_ACTION = new ArrayList<DriveAction<? extends DriveVersion>>(1);
        SYNC_DIRECTORIES_ACTION.add(new SyncDirectoriesAction());
    }

    private final List<DriveEventPublisher> publishers;
    private final ConcurrentMap<Integer, FolderBuffer> folderBuffers;
    private final ScheduledTimerTask periodicPublisher;
    private final int consolidationTime;
    private final int maxDelayTime ;
    private final int defaultDelayTime;

    public DriveEventServiceImpl() throws OXException {
        super();
        this.publishers = new CopyOnWriteArrayList<DriveEventPublisher>();
        this.folderBuffers = new ConcurrentHashMap<Integer, FolderBuffer>();
        ConfigurationService configService = DriveEventServiceLookup.getService(ConfigurationService.class, true);
        this.consolidationTime = configService.getIntProperty("com.openexchange.drive.events.consolidationTime", 2000);
        this.maxDelayTime = configService.getIntProperty("com.openexchange.drive.events.maxDelayTime", 20000);
        this.defaultDelayTime = configService.getIntProperty("com.openexchange.drive.events.defaultDelayTime", 5000);
        int publisherDelay = configService.getIntProperty("com.openexchange.drive.events.publisherDelay", 5000);
        this.periodicPublisher = DriveEventServiceLookup.getService(TimerService.class, true).scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {
                try {
                    Iterator<FolderBuffer> iterator = folderBuffers.values().iterator();
                    while (iterator.hasNext()) {
                        FolderBuffer buffer = iterator.next();
                        if (buffer.isReady()) {
                            iterator.remove();
                            publish(buffer);
                        }
                    }
                } catch (Exception e) {
                    LOG.warn("error publishing drive events.", e);
                }
            }
        }, publisherDelay, publisherDelay);
    }

    public void stop() {
        if (null != periodicPublisher) {
            periodicPublisher.cancel();
        }
    }

    private void publish(FolderBuffer buffer) {
        if (null != buffer) {
            Set<String> folderIDs = buffer.getFolderIDs();
            if (null != folderIDs && 0 < folderIDs.size()) {
                DriveEvent event = new DriveEventImpl(buffer.getContexctID(), folderIDs, SYNC_DIRECTORIES_ACTION);
                LOG.debug("Publishing buffered: " + event);
                for (DriveEventPublisher publisher : publishers) {
                    publisher.publish(event);
                }
            }
        }
    }

    private static boolean check(Event event) {
        return null != event && event.containsProperty(SESSION) &&
            (event.containsProperty(FOLDER_ID) || event.containsProperty(PARENT_FOLDER_ID));
    }

    @Override
    public void handleEvent(Event event) {
        /*
         * check event
         */
        if (LOG.isDebugEnabled()) {
            LOG.debug(FileStorageEventHelper.createDebugMessage("event", event));
        }
        if (false == check(event)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Unable to handle incomplete event: " + event);
            }
            return;
        }
        /*
         * extract properties
         */
        Session session = (Session)event.getProperty(SESSION);
        Integer contextID = Integer.valueOf(session.getContextId());
        String folderID = (String)(event.containsProperty(PARENT_FOLDER_ID) ?
            event.getProperty(PARENT_FOLDER_ID) : event.getProperty(FOLDER_ID));
        String[] folderPath = (String[])event.getProperty(FOLDER_PATH);
        /*
         * get buffer for this context
         */
        FolderBuffer buffer = folderBuffers.get(contextID);
        if (null == buffer) {
            buffer = new FolderBuffer(contextID, consolidationTime, maxDelayTime, defaultDelayTime);
            FolderBuffer existingBuffer = folderBuffers.putIfAbsent(Integer.valueOf(contextID), buffer);
            if (null != existingBuffer) {
                buffer = existingBuffer;
            }
        }
        /*
         * add to buffer
         */
        if (null != folderPath) {
            buffer.add(session, folderID, Arrays.asList(folderPath));
        } else {
            buffer.add(session, folderID);
        }
    }

    @Override
    public void registerPublisher(DriveEventPublisher publisher) {
        if (publishers.add(publisher) && LOG.isDebugEnabled()) {
            LOG.debug("Added drive event publisher: " + publisher);
        }
    }

    @Override
    public void unregisterPublisher(DriveEventPublisher publisher) {
        if (publishers.remove(publisher) && LOG.isDebugEnabled()) {
            LOG.debug("Removed drive event publisher: " + publisher);
        }
    }

}
