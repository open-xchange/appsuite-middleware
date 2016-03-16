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

package com.openexchange.exception.interception;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.openexchange.exception.OXException;


/**
 * Abstract implementation of {@link OXExceptionInterceptor} that should be used to create custom {@link OXExceptionInterceptor}s.<br>
 * <br>
 * With that you only have to define responsibilities and implement what should be do while intercepting by overriding {@link
 * AbstractOXExceptionInterceptor.intercept(OXException)}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public abstract class AbstractOXExceptionInterceptor implements OXExceptionInterceptor {

    /** List of {@link Responsibility} the extending {@link OXExceptionInterceptor} is responsible for **/
    protected final Queue<Responsibility> responsibilitites = new ConcurrentLinkedQueue<Responsibility>();

    /** The service ranking */
    protected final int ranking;

    /**
     * Initializes a new {@link AbstractOXExceptionInterceptor}.
     *
     * @param ranking The ranking of this {@link OXExceptionInterceptor} compared to other ones
     */
    protected AbstractOXExceptionInterceptor(int ranking) {
        super();
        this.ranking = ranking;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract OXExceptionArguments intercept(OXException oxException);

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Responsibility> getResponsibilities() {
        return responsibilitites;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addResponsibility(Responsibility responsibility) {
        this.responsibilitites.add(responsibility);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isResponsible(String module, String action) {
        for (Responsibility responsibility : responsibilitites) {
            if (responsibility.implies(module, action)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRanking() {
        return ranking;
    }
}
