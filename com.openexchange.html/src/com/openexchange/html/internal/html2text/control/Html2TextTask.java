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

package com.openexchange.html.internal.html2text.control;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import com.openexchange.html.internal.HtmlServiceImpl;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadRenamer;

/**
 * {@link Html2TextTask} - A task that performs HTML-to-text conversion {@link Delayed ready} for being used in a {@link DelayQueue} by
 * remembering the time stamp when this task should be terminated (deadline). That time stamp is compared to specified timeout.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.1
 */
public class Html2TextTask extends AbstractTask<String> implements Delayed {

    /** The special poison task to stop taking from queue */
    public static final Html2TextTask POISON = new Html2TextTask(null, false, 0, null) {

        @Override
        public int compareTo(Delayed o) {
            return -1;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return 0L;
        }
    };

    // ---------------------------------------------------------------------------------------------------------------------------

    private final String html;
    private final boolean appendHref;
    private final int timeoutSec;
    private final HtmlServiceImpl htmlService;
    private volatile long stamp;
    private volatile Thread worker;

    /**
     * Initializes a new {@link Html2TextTask}.
     *
     * @param html The HTML content to parse
     * @param appendHref Whether to append an anchor's <code>href</code> information
     * @param timeoutSec The timeout seconds
     * @param htmlService The service instance
     */
    public Html2TextTask(String html, boolean appendHref, int timeoutSec, HtmlServiceImpl htmlService) {
        super();
        this.html = html;
        this.appendHref = appendHref;
        this.timeoutSec = timeoutSec;
        this.htmlService = htmlService;
    }

    @Override
    public int compareTo(Delayed o) {
        long thisStamp = this.stamp;
        long otherStamp = ((Html2TextTask) o).stamp;
        return (thisStamp < otherStamp ? -1 : (thisStamp == otherStamp ? 0 : 1));
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long toGo = stamp - System.currentTimeMillis();
        return unit.convert(toGo, TimeUnit.MILLISECONDS);
    }

    @Override
    public void setThreadName(ThreadRenamer threadRenamer) {
        threadRenamer.renamePrefix("Html2Text");
    }

    @Override
    public String call() {
        Html2TextControl control = Html2TextControl.getInstance();
        stamp = System.currentTimeMillis() + (timeoutSec * 1000L);
        worker = Thread.currentThread();
        control.add(this);
        try {
            return htmlService.doHtml2Text(html, appendHref);
        } finally {
            control.remove(this);
        }
    }

    /**
     * Interrupts this task (if currently processed).
     */
    public void interrupt() {
        Thread worker = this.worker;
        if (null != worker) {
            worker.interrupt();
        }
    }

}
