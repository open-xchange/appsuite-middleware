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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.pns;

import com.openexchange.exception.OXException;

/**
 * {@link PushSubscriptionListener} - A listener for subscription registry events.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface PushSubscriptionListener {

    /**
     * Invoked when a subscription is about to be added to registry.
     *
     * @param subscription The subscription to add
     * @return <code>true</code> to allow given subscription being added; otherwise <code>false</code>
     * @throws OXException If handling fails
     */
    boolean addingSubscription(PushSubscription subscription) throws OXException;

    /**
     * Invoked when a subscription is added to registry.
     *
     * @param subscription The added subscription
     * @throws OXException If handling fails
     */
    void addedSubscription(PushSubscription subscription) throws OXException;

    /**
     * Invoked when a subscription is removed from registry.
     *
     * @param subscription The removed subscription
     * @throws OXException If handling fails
     */
    void removedSubscription(PushSubscription subscription) throws OXException;

    // ----------------------------------------------------------------------------------------------------------------------

    /**
     * Invoked when a subscription provider is about to be added to registry.
     *
     * @param subscription The subscription provider to add
     * @return <code>true</code> to allow given subscription provider being added; otherwise <code>false</code>
     * @throws OXException If handling fails
     */
    boolean addingProvider(PushSubscriptionProvider provider) throws OXException;

    /**
     * Invoked when a subscription provider is added to registry.
     *
     * @param subscription The added subscription provider
     * @throws OXException If handling fails
     */
    void addedProvider(PushSubscriptionProvider provider) throws OXException;

    /**
     * Invoked when a subscription provider is removed from registry.
     *
     * @param provider The removed subscription provider
     * @throws OXException If handling fails
     */
    void removedProvider(PushSubscriptionProvider provider) throws OXException;

}
