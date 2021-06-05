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

package com.openexchange.twitter;

import java.util.Date;

/**
 * {@link User} - Basic user information element.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface User {

    /**
     * Returns the id of the user
     *
     * @return The id of the user
     */
    long getId();

    /**
     * Returns the name of the user
     *
     * @return The name of the user
     */
    String getName();

    /**
     * Returns the screen name of the user
     *
     * @return The screen name of the user
     */
    String getScreenName();

    /**
     * Returns the location of the user
     *
     * @return The location of the user
     */
    String getLocation();

    /**
     * Returns the description of the user
     *
     * @return The description of the user
     */
    String getDescription();

    /**
     * Returns the profile image URL of the user
     *
     * @return The profile image URL of the user
     */
    String getProfileImageURL();

    /**
     * Returns the URL of the user
     *
     * @return The URL of the user
     */
    String getURL();

    /**
     * Test if the user status is protected
     *
     * @return <code>true</code> if the user status is protected; otherwise <code>false</code>
     */
    boolean isProtected();

    /**
     * Returns the number of followers
     *
     * @return The number of followers
     */
    int getFollowersCount();

    /**
     * Gets the user profile's background color as RGB HEX string; e.g. <code>d3e7f0</code>.
     *
     * @return The user profile's background color
     */
    String getProfileBackgroundColor();

    /**
     * Gets the user profile's text color as RGB HEX string; e.g. <code>d3e7f0</code>.
     *
     * @return The user profile's text color
     */
    String getProfileTextColor();

    /**
     * Gets the user profile's link color as RGB HEX string; e.g. <code>d3e7f0</code>.
     *
     * @return The user profile's link color
     */
    String getProfileLinkColor();

    /**
     * Gets the user profile's sidebar fill color as RGB HEX string; e.g. <code>d3e7f0</code>.
     *
     * @return The user profile's sidebar fill color
     */
    String getProfileSidebarFillColor();

    /**
     * Gets the user profile's sidebar border color as RGB HEX string; e.g. <code>d3e7f0</code>.
     *
     * @return The user profile's sidebar border color
     */
    String getProfileSidebarBorderColor();

    /**
     * Gets the number of friends.
     *
     * @return The number of friends.
     */
    int getFriendsCount();

    /**
     * Gets the creation date.
     *
     * @return The creation date.
     */
    Date getCreatedAt();

    /**
     * Gets the number of user's favorites.
     *
     * @return The number of user's favorites.
     */
    int getFavouritesCount();

    /**
     * Gets the UTC offset.
     *
     * @return The UTC offset
     */
    int getUtcOffset();

    /**
     * Gets the time zone id.
     *
     * @return The time zone id.
     */
    String getTimeZone();

    /**
     * Gets the URL of user profile's background image.
     *
     * @return The URL of user profile's background image
     */
    String getProfileBackgroundImageUrl();

    /**
     * Gets the status count.
     *
     * @return The status count
     */
    int getStatusesCount();

    /**
     * Checks if this user is enabling geo location.
     *
     * @return <code>true</code> if this user is enabling geo location; otherwise <code>false</code>
     */
    boolean isGeoEnabled();

    /**
     * Checks if this user is a verified celebrity.
     *
     * @return <code>true</code> if this user is a verified celebrity; otherwise <code>false</code>
     */
    boolean isVerified();

}
