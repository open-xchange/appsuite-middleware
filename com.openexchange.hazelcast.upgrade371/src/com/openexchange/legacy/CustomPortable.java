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


package com.openexchange.legacy;

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
     * <p/>
     * Choose a not yet used arbitrary identifier <code>> 0</code> for your portable class here and ensure to return the same class ID in
     * the corresponding {@link CustomPortableFactory#getClassId()} method.
     * <p/>
     * The following list gives an overview about the <b>already used</b> class IDs (add your IDs here):
     * <ul>
     * <li><code>  1</code>: com.openexchange.sessionstorage.hazelcast.serialization.PortableSession</li>
     * <li><code>  2</code>: com.openexchange.drive.events.ms.PortableDriveEvent</li>
     * <li><code>  3</code>: com.openexchange.ms.internal.portable.PortableMessage</li>
     * <li><code>  4</code>: com.openexchange.caching.events.ms.internal.PortableCacheEvent</li>
     * <li><code>  5</code>: com.openexchange.realtime.hazelcast.serialization.PortableID</li>
     * <li><code>  6</code>: com.openexchange.realtime.hazelcast.serialization.PortableSelectorChoice</li>
     * <li><code>  7</code>: com.openexchange.realtime.hazelcast.serialization.PortableNotInternalPredicate</li>
     * <li><code>  8</code>: com.openexchange.realtime.hazelcast.serialization.PortableMemberPredicate</li>
     * <li><code>  9</code>: com.openexchange.report.appsuite.internal.PortableReport, used via
     * {@link com.openexchange.caching.com.openexchange.hazelcast.serialization.CustomPortable.PORTABLEREPORT_CLASS_ID}</li>
     * <li><code>  10</code>: com.openexchange.realtime.hazelcast.serialization.PortableHazelcastResource</li>
     * <li><code>  11</code>: com.openexchange.realtime.hazelcast.serialization.PortablePresence</li>
     * <li><code>  12</code>: com.openexchange.realtime.hazelcast.serialization.PortableRoutingInfo</li>
     * <li><code>  13</code>: com.openexchange.realtime.hazelcast.serialization.PortableContextPredicate</li>
     * <li><code>  14</code>: com.openexchange.realtime.hazelcast.serialization.channel.PortableStanzaDispatcher</li>
     * <li><code>  15</code>: com.openexchange.realtime.hazelcast.serialization.cleanup.PortableCleanupDispatcher</li>
     * <li><code>  16</code>: com.openexchange.realtime.hazelcast.serialization.cleanup.PortableCleanupStatus</li>
     * <li><code>  17</code>: com.openexchange.caching.events.ms.internal.PortableCacheKey</li>
     * <li><code>  18</code>: com.openexchange.sessiond.portable.PortableTokenSessionControl</li>
     * <li><code>  19</code>: com.openexchange.sessiond.serialization.PortableContextSessionsCleaner</li>
     * <li><code>  20</code>: com.openexchange.oauth.provider.internal.authcode.portable.PortableAuthCodeInfo</li>
     * <li><code>  21</code>: com.openexchange.realtime.hazelcast.serialization.util.PortableIDToOXExceptionMapEntry</li>
     * <li><code>  22</code>: com.openexchange.realtime.hazelcast.serialization.util.PortableIDToOXExceptionMap</li>
     *
     *
     * <li><code>  101</code>: com.openexchange.push.impl.credstorage.inmemory.portable.PortableCredentials</li>
     * <li><code>  102</code>: com.openexchange.push.impl.portable.PortablePushUser</li>
     * <li><code>  103</code>: com.openexchange.push.impl.balancing.reschedulerpolicy.portable.PortableCheckForExtendedServiceCallable</li>
     * <li><code>  104</code>: com.openexchange.push.impl.balancing.reschedulerpolicy.portable.PortableDropPermanentListenerCallable</li>
     * <li><code>  105</code>: com.openexchange.push.impl.balancing.reschedulerpolicy.portable.PortablePlanRescheduleCallable</li>
     * <li><code>  106</code>: com.openexchange.push.impl.balancing.registrypolicy.portable.PortableOwner</li>
     * </ul>
     *
     * @return The class ID
     */
    @Override
    int getClassId();

}
