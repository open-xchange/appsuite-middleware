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

package com.openexchange.pns;

import java.util.Date;
import java.util.List;

/**
 * {@link PushSubscription} - Represents a push subscription.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface PushSubscription {

    /**
     * Gets the user identifier
     *
     * @return The user identifier
     */
    int getUserId();

    /**
     * Gets the context identifier
     *
     * @return The context identifier
     */
    int getContextId();

    /**
     * Gets the identifier of the client associated with this subscription.
     *
     * @return The client identifier
     */
    String getClient();

    /**
     * Gets the topics of interest.
     * <p>
     * The returned listing describes the topics in which this subscription is interested.
     * For each topic (a colon-separated string) an asterisk ('*') may be used as a trailing wild-card.
     * More precisely, the string value of each topic must conform to the following grammar:
     *
     * <pre>
     *  topic-description := '*' | topic ( ':*' )?
     *  topic := token ( ':' token )*</pre>
     *
     * @return The topics
     */
    List<String> getTopics();

    /**
     * Gets the identifier of the associated push transport.
     *
     * @return The transport identifier
     */
    String getTransportId();

    /**
     * Gets the subscription's token
     *
     * @return The token
     */
    String getToken();

    /**
     * Gets this subscription's expiration date, i.e. the date after which this subscription automatically expires unless it is refreshed.
     *
     * @return The expiration date, or <code>null</code> if not set
     */
    Date getExpires();

}
