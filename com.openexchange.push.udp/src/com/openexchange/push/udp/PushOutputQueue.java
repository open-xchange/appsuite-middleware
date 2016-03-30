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

package com.openexchange.push.udp;

import static com.openexchange.push.udp.PushChannels.ChannelType.EXTERNAL;
import static com.openexchange.push.udp.PushChannels.ChannelType.INTERNAL;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.event.EventFactoryService;
import com.openexchange.event.RemoteEvent;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.push.udp.registry.PushServiceRegistry;
import com.openexchange.tools.StringCollection;

/**
 * {@link PushOutputQueue} - Holds main queue containing objects to deliver.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class PushOutputQueue implements Runnable {

    private static long delay = 60000;

    private static HashMap<PushObject, PushDelayedObject> existingPushObjects = new HashMap<PushObject, PushDelayedObject>();

    private static volatile boolean isEnabled;

    private static volatile boolean isInit;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PushOutputQueue.class);

    private static volatile PushConfiguration pushConfigInterface;

    private static DelayQueue<PushDelayedObject> queue = new DelayQueue<PushDelayedObject>();

    private static Set<RemoteHostObject> remoteHost;

    private static int remoteHostTimeOut = 3600000;

    private static volatile PushChannels channels;

    /**
     * Adds specified push object to queue for delivery.
     *
     * @param pushObject The push object to deliver
     * @throws OXException If an event exception occurs
     */
    public static void add(final PushObject pushObject) throws OXException {
        LOG.debug("add PushObject: {}", pushObject);
        if (!isEnabled) {
            return;
        }
        if (!isInit) {
            throw OXException.general("PushOutputQueue not initialisiert!");
        }
        PushDelayedObject pushDelayedObject = existingPushObjects.get(pushObject);
        if (Types.EMAIL == pushObject.getModule()) {
            // No delay for emails. Push them immediately to the client. EMails can not be changed and every new push event indicates a new
            // email in the INBOX which needs to be transfered to the client.
            // Do not put into existingPushObjects because we do not need to find it again.
            queue.add(new PushDelayedObject(0, pushObject));
        } else if (null != pushDelayedObject) {
            // Delay push for same PIM objects folder because of further changes in the same folder. The maximum for this further delaying
            // is done in the {@link PushDelayedObject}.
            // If existingPushObjects finds a {@link PushDelayedObject} it indicates the same folder and the same affected users.
            pushDelayedObject.updateTime();
        } else {
            pushDelayedObject = new PushDelayedObject(delay, pushObject);
            existingPushObjects.put(pushObject, pushDelayedObject);
            queue.add(pushDelayedObject);
        }
    }

    /**
     * Adds specified register object to queue for delivery.
     *
     * @param registerObject The register object
     * @throws OXException If an event exception occurs
     */
    public static void add(final RegisterObject registerObject) throws OXException {
        add(registerObject, false);
    }

    /**
     * Adds specified register object to queue for delivery.
     *
     * @param registerObject The register object
     * @param noDelay <code>true</code> to immediately deliver the object; otherwise <code>false</code>
     * @throws OXException If an event exception occurs
     */
    public static void add(final RegisterObject registerObject, final boolean noDelay) throws OXException {
        LOG.debug("add RegisterObject: {}", registerObject);

        if (!isEnabled) {
            return;
        }

        if (!isInit) {
            throw OXException.general("PushOutputQueue not initialized!");
        }

        if (noDelay) {
            final PushDelayedObject pushDelayObject = new PushDelayedObject(0, registerObject);
            queue.add(pushDelayObject);
        } else {
            final PushDelayedObject pushDelayObject = new PushDelayedObject(delay, registerObject);
            queue.add(pushDelayObject);
        }
    }

    /**
     * Adds given remote host to set of known Open-Xchange servers to which objects ought to be distributed.
     *
     * @param remoteHostObject The remote host denoting a remote Open-Xchange server
     */
    public static void addRemoteHostObject(final RemoteHostObject remoteHostObject) {
        if (remoteHost.contains(remoteHostObject)) {
            remoteHost.remove(remoteHostObject);
        }
        remoteHost.add(remoteHostObject);
    }

    /**
     * Creates an appropriate binary data package from given push object and delivers it to clients as well as distributes it among linked
     * Open-Xchange servers (if configured).
     *
     * @param pushObject The push object
     */
    private static void createAndDeliverPushPackage(final PushObject pushObject) {
        final int users[] = pushObject.getUsers();
        final int contextId = pushObject.getContextId();
        final int folderId = pushObject.getFolderId();
        {
            final byte[] bytes;
            {
                final StringBuilder sb = new StringBuilder();
                sb.append(folderId);
                sb.append('\1');
                bytes = sb.toString().getBytes();
            }
            for (final int user : users) {
                if (RegisterHandler.isRegistered(user, contextId)) {
                    final RegisterObject registerObj = RegisterHandler.getRegisterObject(user, contextId);
                    try {
                        channels.makeAndSendPackage(bytes, registerObj.getHostAddress(), registerObj.getPort(), EXTERNAL);
                    } catch (final Exception exc) {
                        LOG.error("createPushPackage", exc);
                    }
                }
            }
        }
        /*
         * Distribute
         */
        final long timestamp = pushObject.getTimestamp();
        final int module = pushObject.getModule();
        if (pushObject.isRemote()) {
            /*
             * Distribute to own system
             */
            final EventAdmin eventAdmin = PushServiceRegistry.getServiceRegistry().getService(EventAdmin.class);
            final EventFactoryService factoryService = PushServiceRegistry.getServiceRegistry().getService(EventFactoryService.class);
            if (null != eventAdmin && null != factoryService) {
                /*
                 * Create an event from push object for each affected user
                 */
                final int action;
                final String topic;
                if (Types.FOLDER == module) {
                    action = RemoteEvent.FOLDER_CHANGED;
                    topic = "com/openexchange/remote/folderchanged";
                } else {
                    action = RemoteEvent.FOLDER_CONTENT_CHANGED;
                    topic = "com/openexchange/remote/foldercontentchanged";
                }

                for (final int user : users) {
                    final RemoteEvent remoteEvent = factoryService.newRemoteEvent(folderId, user, contextId, action, module, timestamp);
                    final Dictionary<String, RemoteEvent> ht = new Hashtable<String, RemoteEvent>();
                    ht.put(RemoteEvent.EVENT_KEY, remoteEvent);
                    eventAdmin.postEvent(new Event(topic, ht));
                }
            }
        } else if (pushConfigInterface.isEventDistributionEnabled()) {
            /*-
             * Distribute among linked hosts if
             * 1. Event distribution is enabled by configuration
             * 2. Affected push object was not remotely received
             */
            final Iterator<RemoteHostObject> iter = remoteHost.iterator();
            while (iter.hasNext()) {
                final RemoteHostObject remoteHostObject = iter.next();
                try {
                    final StringBuilder sb = new StringBuilder();
                    sb.append(PushRequest.MAGIC);
                    sb.append('\1');

                    final StringBuilder data = new StringBuilder();
                    data.append(PushRequest.PUSH_SYNC);
                    data.append('\1');
                    data.append(folderId);
                    data.append('\1');
                    data.append(module);
                    data.append('\1');
                    data.append(contextId);
                    data.append('\1');
                    data.append(StringCollection.convertArray2String(pushObject.getUsers()));
                    data.append('\1');
                    data.append(timestamp);

                    sb.append(data.length());
                    sb.append('\1');
                    sb.append(data);

                    final byte b[] = sb.toString().getBytes();

                    if (System.currentTimeMillis() <= (remoteHostObject.getTimer().getTime() + remoteHostTimeOut)) {
                        channels.makeAndSendPackage(b, remoteHostObject.getHost(), remoteHostObject.getPort(), INTERNAL);
                    } else {
                        LOG.trace("remote host object is timed out");
                        iter.remove();
                    }
                } catch (final Exception exc) {
                    LOG.error("createPushPackage", exc);
                }
            }
        }
    }

    /**
     * Creates an appropriate binary data package from given register object and delivers it to clients as well as distributes it among
     * linked Open-Xchange server (if configured).
     *
     * @param registerObject The register object
     */
    private static void createAndDeliverRegisterPackage(final RegisterObject registerObject) {
        if (!registerObject.isRemote()) {
            final StringBuilder sb = new StringBuilder();
            sb.append("OK\1");
            try {
                channels.makeAndSendPackage(sb.toString().getBytes(), registerObject.getHostAddress(), registerObject.getPort(), EXTERNAL);
            } catch (final Exception exc) {
                LOG.error("createRegisterPackage", exc);
            }
        }
        /*-
         * Distribute among linked hosts if
         * 1. Registration distribution is enabled by configuration
         */
        if (pushConfigInterface.isRegisterDistributionEnabled()) {
            final Iterator<RemoteHostObject> iter = remoteHost.iterator();
            while (iter.hasNext()) {
                final RemoteHostObject remoteHostObject = iter.next();
                try {
                    final StringBuilder sb = new StringBuilder();
                    sb.append(PushRequest.MAGIC);
                    sb.append('\1');

                    final StringBuilder data = new StringBuilder();
                    data.append(PushRequest.REGISTER_SYNC);
                    data.append('\1');
                    data.append(registerObject.getUserId());
                    data.append('\1');
                    data.append(registerObject.getContextId());
                    data.append('\1');
                    data.append(registerObject.getHostAddress());
                    data.append('\1');
                    data.append(registerObject.getPort());
                    data.append('\1');
                    data.append(registerObject.isRemote());
                    data.append('\1');

                    sb.append(data.length());
                    sb.append('\1');
                    sb.append(data);

                    final byte b[] = sb.toString().getBytes();
                    if (System.currentTimeMillis() <= (remoteHostObject.getTimer().getTime() + remoteHostTimeOut)) {
                        channels.makeAndSendPackage(b, remoteHostObject.getHost(), remoteHostObject.getPort(), INTERNAL);
                    } else {
                        LOG.trace("remote host object is timed out");
                        iter.remove();
                    }
                } catch (final Exception exc) {
                    LOG.error("createRegisterPackage", exc);
                }
            }
        }
    }

    /**
     * Delivers specified push object to clients and distributes it among linked Open-Xchange servers (if configured).
     *
     * @param pushDelayedObject The push object to deliver
     */
    private static void deliver(final PushDelayedObject pushDelayedObject) {
        final AbstractPushObject abstractPushObject = pushDelayedObject.getPushObject();

        if (abstractPushObject instanceof PushObject) {
            LOG.debug("Send Push Object");

            final PushObject pushObject = (PushObject) abstractPushObject;
            existingPushObjects.remove(pushObject);

            createAndDeliverPushPackage(pushObject);
        } else if (abstractPushObject instanceof RegisterObject) {
            LOG.debug("Send Register Object");

            createAndDeliverRegisterPackage((RegisterObject) abstractPushObject);
        }
    }



    private boolean isRunning = false;

    public PushOutputQueue(final PushConfiguration pushConfigInterface, final PushChannels channels2) {
        PushOutputQueue.pushConfigInterface = pushConfigInterface;

        remoteHost = pushConfigInterface.getRemoteHost();

        if (pushConfigInterface.isPushEnabled()) {
            LOG.info("Starting PushOutputQueue");

            remoteHost = pushConfigInterface.getRemoteHost();

            delay = pushConfigInterface.getOutputQueueDelay();

            remoteHostTimeOut = pushConfigInterface.getRemoteHostTimeOut();

            isEnabled = true;

            isRunning = true;

            channels = channels2;

            final Thread th = new Thread(this);
            th.setName(this.getClass().getName());
            th.start();
        } else {
            LOG.info("PushOutputQueue is disabled");
        }

        isInit = true;
    }

    public void close() {
        isRunning = false;
    }

    @Override
    public void run() {
        while (isRunning) {
            LOG.debug("get push objects from queue: {}", queue.size());

            try {
                // Breaks IBM Java < 1.5.0_sr9
                final PushDelayedObject pushDelayedObject = queue.poll(10, TimeUnit.SECONDS);
                if (pushDelayedObject != null) {
                    deliver(pushDelayedObject);
                }
                // Workaround for IBM Java (always sleeps 10 seconds even if push is added)
                // Bug 11524
                // final PushDelayedObject pushDelayedObject = queue.poll();
                // if (pushDelayedObject != null) {
                // action(pushDelayedObject);
                // } else {
                // Thread.sleep(10000);
                // }
            } catch (final Exception exc) {
                LOG.error("", exc);
            }
        }
    }
}
