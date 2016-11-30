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


package com.openexchange.hazelcast.serialization;

import com.hazelcast.nio.serialization.Portable;

/**
 * {@link CustomPortable}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface CustomPortable extends Portable {

    /**
     * The identifier of the dynamic portable factory.<p/>
     *
     * Make sure to supply this identifier in the {@link #getFactoryId()} method.
     */
    static final int FACTORY_ID = DynamicPortableFactory.FACTORY_ID;

    /**
     * Unique id for PortableReport
     */
    public static final int PORTABLEREPORT_CLASS_ID = 9;

    public static final int PORTABLE_CONTEXT_SESSIONS_CLEANER_CLASS_ID = 19;

    /**
     * Unique id for PortableUserSessionsCleaner
     */
    public static final int PORTABLE_USER_SESSIONS_CLEANER_CLASS_ID = 24;

    /**
     * Unique id for PortableSessionFilterApplier
     */
    public static final int PORTABLE_SESSIONS_FILTER_APPLIER_CLASS_ID = 25;

    /**
     * Unique id for PortableAuthnRequestInfo
     */
    public static final int PORTABLE_SAML_AUTHN_REQUEST_INFO = 300;

    /**
     * Unique id for PortableLogoutRequestInfo
     */
    public static final int PORTABLE_SAML_LOGOUT_REQUEST_INFO = 301;

    /**
     * Gets the ID of the dynamic portable factory.<p/>
     *
     * Make sure to supply {@link CustomPortable#FACTORY_ID} here.
     *
     * @return The factory ID.
     */
    @Override
    int getFactoryId();

    /**
     * Gets the class ID of this portable implementation.
     * <p>
     * Choose a not yet used arbitrary identifier <code>> 0</code> for your portable class here and ensure to return the same class ID in
     * the corresponding {@link CustomPortableFactory#getClassId()} method.
     * <p>
     * The following list gives an overview about the <b>already used</b> class IDs (add your IDs here):
     * <pre>
     *   &bull; 1 ----> com.openexchange.sessionstorage.hazelcast.serialization.PortableSession
     *   &bull; 2 ----> com.openexchange.drive.events.ms.PortableDriveEvent
     *   &bull; 3 ----> com.openexchange.ms.internal.portable.PortableMessage
     *   &bull; 4 ----> com.openexchange.caching.events.ms.internal.PortableCacheEvent
     *   &bull; 5 ----> com.openexchange.realtime.hazelcast.serialization.PortableID
     *   &bull; 6 ----> com.openexchange.realtime.hazelcast.serialization.PortableSelectorChoice
     *   &bull; 7 ----> com.openexchange.realtime.hazelcast.serialization.PortableNotInternalPredicate
     *   &bull; 8 ----> com.openexchange.realtime.hazelcast.serialization.PortableMemberPredicate
     *   &bull; 9 ----> com.openexchange.report.appsuite.internal.PortableReport
     *   &bull; 10 ---> com.openexchange.realtime.hazelcast.serialization.PortableHazelcastResource
     *   &bull; 11 ---> com.openexchange.realtime.hazelcast.serialization.PortablePresence
     *   &bull; 12 ---> com.openexchange.realtime.hazelcast.serialization.PortableRoutingInfo
     *   &bull; 13 ---> com.openexchange.realtime.hazelcast.serialization.PortableContextPredicate
     *   &bull; 14 ---> com.openexchange.realtime.hazelcast.serialization.channel.PortableStanzaDispatcher
     *   &bull; 15 ---> com.openexchange.realtime.hazelcast.serialization.cleanup.PortableCleanupDispatcher
     *   &bull; 16 ---> com.openexchange.realtime.hazelcast.serialization.cleanup.PortableCleanupStatus
     *   &bull; 17 ---> com.openexchange.caching.events.ms.internal.PortableCacheKey
     *   &bull; 18 ---> com.openexchange.sessiond.portable.PortableTokenSessionControl
     *   &bull; 19 ---> com.openexchange.sessiond.serialization.PortableContextSessionsCleaner
     *   &bull; 20 ---> com.openexchange.oauth.provider.internal.authcode.portable.PortableAuthCodeInfo
     *   &bull; 21 ---> com.openexchange.realtime.hazelcast.serialization.util.PortableIDToOXExceptionMapEntry
     *   &bull; 22 ---> com.openexchange.realtime.hazelcast.serialization.util.PortableIDToOXExceptionMap
     *   &bull; 23 ---> com.openexchange.session.reservation.impl.portable.PortableReservation
     *   &bull; 24 ---> com.openexchange.sessiond.serialization.PortableUserSessionsCleaner
     *   &bull; 25 ---> com.openexchange.sessiond.serialization.PortableSessionFilterApplier
     *   &bull; 101 --> com.openexchange.push.impl.credstorage.inmemory.portable.PortableCredentials
     *   &bull; 102 --> com.openexchange.push.impl.portable.PortablePushUser
     *   &bull; 103 --> com.openexchange.push.impl.balancing.reschedulerpolicy.portable.PortableCheckForExtendedServiceCallable
     *   &bull; 104 --> com.openexchange.push.impl.balancing.reschedulerpolicy.portable.PortableDropPermanentListenerCallable
     *   &bull; 105 --> com.openexchange.push.impl.balancing.reschedulerpolicy.portable.PortablePlanRescheduleCallable
     *   &bull; 106 --> com.openexchange.push.impl.balancing.registrypolicy.portable.PortableOwner
     *   &bull; 109 --> com.openexchange.push.impl.balancing.reschedulerpolicy.portable.PortableDropAllPermanentListenerCallable
     *   &bull; 110 --> com.openexchange.push.impl.balancing.reschedulerpolicy.portable.PortableStartPermanentListenerCallable
     *   &bull; 107 --> com.openexchange.mail.attachment.impl.portable.PortableCheckForAttachmentToken
     *   &bull; 108 --> com.openexchange.mail.attachment.impl.portable.PortableAttachmentToken
     *   &bull; 200 --> com.openexchange.office.hazelcast.serialization.document.PortableID
     *   &bull; 201 --> com.openexchange.office.hazelcast.serialization.document.PortableDocumentState
     *   &bull; 202 --> com.openexchange.office.hazelcast.serialization.document.PortableDocSaveState
     *   &bull; 204 --> com.openexchange.office.hazelcast.serialization.document.PortableDocumentResources
     *   &bull; 205 --> com.openexchange.office.hazelcast.serialization.document.PortableResourceID
     *   &bull; 206 --> com.openexchange.office.hazelcast.serialization.document.PortableRestoreID
     *   &bull; 207 --> com.openexchange.office.hazelcast.serialization.document.PortableCleanupStatus
     *   &bull; 208 --> com.openexchange.office.hazelcast.serialization.document.PortableMemberPredicate
     *   &bull; 300 --> com.openexchange.saml.impl.hz.PortableAuthnRequestInfo
     *   &bull; 301 --> com.openexchange.saml.impl.hz.PortableLogoutRequestInfo
     *   &bull; 400 --> com.openexchange.sessionstorage.hazelcast.serialization.PortableSessionExistenceCheck
     *   &bull; 401 --> com.openexchange.sessionstorage.hazelcast.serialization.PortableSessionRemoteLookUp
     *   &bull; 500 --> com.openexchange.sms.tools.internal.SMSBucket
     *   &bull; 600 --> com.openexchange.websockets.grizzly.remote.portable.PortableMessageDistributor
     * </pre>
     *
     * @return The class ID
     */
    @Override
    int getClassId();

}
