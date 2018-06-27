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

package com.openexchange.metrics.dropwizard.types;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import com.codahale.metrics.Snapshot;
import com.openexchange.exception.OXException;
import com.openexchange.metrics.exceptions.MetricExceptionCode;
import com.openexchange.metrics.types.Timer;

/**
 * {@link DropwizardTimer}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DropwizardTimer implements Timer {

    private com.codahale.metrics.Timer delegate;

    /**
     * Initialises a new {@link DropwizardTimer}.
     */
    public DropwizardTimer(com.codahale.metrics.Timer timer) {
        this.delegate = timer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.types.Timer#update(long, java.util.concurrent.TimeUnit)
     */
    @Override
    public void update(long duration, TimeUnit timeUnit) {
        delegate.update(duration, timeUnit);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.types.Timer#time(java.util.concurrent.Callable)
     */
    @Override
    public <T> T time(Callable<T> event) throws OXException {
        try {
            return delegate.time(event);
        } catch (Exception e) {
            throw MetricExceptionCode.ERROR_WHILE_TIMING.create(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.types.Timer#time(java.lang.Runnable)
     */
    @Override
    public void time(Runnable event) {
        delegate.time(event);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.types.Timer#getCount()
     */
    @Override
    public long getCount() {
        return delegate.getCount();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.types.Timer#getFifteenMinuteRate()
     */
    @Override
    public double getFifteenMinuteRate() {
        return delegate.getFifteenMinuteRate();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.types.Timer#getFiveMinuteRate()
     */
    @Override
    public double getFiveMinuteRate() {
        return delegate.getFiveMinuteRate();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.types.Timer#getMeanRate()
     */
    @Override
    public double getMeanRate() {
        return delegate.getMeanRate();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.metrics.types.Timer#getOneMinuteRate()
     */
    @Override
    public double getOneMinuteRate() {
        return delegate.getOneMinuteRate();
    }

    /**
     * Returns a snapshot of the values.
     * 
     * @return a snapshot of the values.
     */
    public Snapshot getSnapshot() {
        return delegate.getSnapshot();
    }
}
