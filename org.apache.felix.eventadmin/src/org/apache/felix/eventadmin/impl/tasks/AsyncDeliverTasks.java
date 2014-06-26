/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.felix.eventadmin.impl.tasks;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.felix.eventadmin.impl.handler.EventHandlerProxy;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

/**
 * This class does the actual work of the asynchronous event dispatch.
 *
 * @author <a href="mailto:dev@felix.apache.org">Felix Project Team</a>
 */
public class AsyncDeliverTasks
{
    /** The thread pool to use to spin-off new threads. */
    private final DefaultThreadPool m_pool;

    /** The deliver task for actually delivering the events. This
     * is the sync deliver tasks as this has all the code for timeout
     * handling etc.
     */
    final SyncDeliverTasks m_deliver_task;

    /** A map of running threads currently delivering async events. */
    final Map<Thread, TaskExecuter> m_running_threads = new HashMap<Thread, TaskExecuter>();

    /** The counter for pending events */
    final AtomicLong postedEvents;

    final AtomicLong deliveredEvents;

    /**
     * The constructor of the class that will use the asynchronous.
     *
     * @param pool The thread pool used to spin-off new asynchronous event
     *      dispatching threads in case of timeout or that the asynchronous event
     *      dispatching thread is used to send a synchronous event
     * @param deliverTask The deliver tasks for dispatching the event.
     */
    public AsyncDeliverTasks(final DefaultThreadPool pool, final SyncDeliverTasks deliverTask)
    {
        m_pool = pool;
        m_deliver_task = deliverTask;
        postedEvents = new AtomicLong();
        deliveredEvents = new AtomicLong();
    }

    public Measurement createMeasurement() {
        long tmpDelivered = deliveredEvents.get();
        long tmpPosted = postedEvents.get();
        return new Measurement(tmpPosted, tmpDelivered);
    }

    /**
     * This does not block an unrelated thread used to send a synchronous event.
     *
     * @param tasks The event handler dispatch tasks to execute
     *
     */
    public void execute(final Collection<EventHandlerProxy> tasks, final Event event)
    {
        /*
        final Iterator i = tasks.iterator();
        boolean hasOrdered = false;
        while ( i.hasNext() )
        {
            final EventHandlerProxy task = (EventHandlerProxy)i.next();
            if ( !task.isAsyncOrderedDelivery() )
            {
                // do somethimg
            }
            else
            {
                hasOrdered = true;
            }

        }
        if ( hasOrdered )
        {*/
            final Thread currentThread = Thread.currentThread();
            TaskExecuter executer = null;
            synchronized (m_running_threads )
            {
                final TaskExecuter runningExecutor = m_running_threads.get(currentThread);
                if ( runningExecutor != null )
                {
                    if (postedEvents.incrementAndGet() < 0L) {
                        postedEvents.set(0L);
                    }
                    runningExecutor.add(tasks, event);
                }
                else
                {
                    executer = new TaskExecuter( tasks, event, currentThread );
                    m_running_threads.put(currentThread, executer);
                }
            }
            if ( executer != null )
            {
                if (postedEvents.incrementAndGet() < 0L) {
                    postedEvents.set(0L);
                }
                m_pool.executeTask(executer);
            }
        //}
    }

    private final class TaskExecuter implements Runnable
    {
        private final LinkedList<EventTask> m_tasks = new LinkedList<EventTask>();

        private final Object m_key;

        public TaskExecuter(final Collection<EventHandlerProxy> tasks, final Event event, final Object key)
        {
            m_key = key;
            m_tasks.addLast(new EventTask(tasks, event));
        }

        @Override
        public void run()
        {
            final Thread currentThread = Thread.currentThread();
            boolean running;
            do
            {
                EventTask eventTask = null;
                synchronized ( m_tasks )
                {
                    eventTask = m_tasks.removeFirst();
                }
                m_deliver_task.execute(eventTask.tasks, eventTask.event, true);
                if (deliveredEvents.incrementAndGet() < 0L) {
                    deliveredEvents.set(0L);
                }
                synchronized ( m_running_threads )
                {
                    running = !m_tasks.isEmpty(); //  m_tasks.size() > 0;
                    if ( !running )
                    {
                        m_running_threads.remove(m_key);
                    }
                }
            } while ( running && !currentThread.isInterrupted() );
        }

        public void add(final Collection<EventHandlerProxy> tasks, final Event event)
        {
            synchronized ( m_tasks )
            {
                m_tasks.addLast(new EventTask(tasks, event));
            }
        }
    }

    private final class EventTask {

        final Collection<EventHandlerProxy> tasks;
        final Event event;

        EventTask(Collection<EventHandlerProxy> tasks, Event event) {
            super();
            this.tasks = tasks;
            this.event = event;
        }
    }

    public final class Measurement {

        private final long timestamp;
        private final long measuredPostedEvents;
        private final long measuredDeliveredEvents;

        Measurement(long postedEvents, long deliveredEvents) {
            super();
            this.measuredPostedEvents = postedEvents;
            this.measuredDeliveredEvents = deliveredEvents;
            this.timestamp = System.currentTimeMillis();
        }

        /**
         * Gets the time in milliseconds after 1970-01-01 00:00:00 UTC when this measurement was created.
         */
        public long getTimestamp() {
            return timestamp;
        }

        /**
         * Gets the total number of events that have been enqueued via {@link EventAdmin#postEvent(Event)}.
         */
        public long getPostedEvents() {
            return measuredPostedEvents;
        }

        /**
         * Gets the total number of events that have already been delivered.
         */
        public long getDeliveredEvents() {
            return measuredDeliveredEvents;
        }
    }

}
