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

package com.openexchange.ajax.chronos.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import com.openexchange.java.Strings;
import com.openexchange.testing.httpclient.models.Conference;
import com.openexchange.testing.httpclient.models.ExtendedPropertyParameter;

/**
 * {@link ConferenceBuilder}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public class ConferenceBuilder {

    private final static Random RANDOM = new Random();

    /**
     * 
     * {@link FEATURES} - All standard features
     *
     * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
     * @since v7.10.4
     * @see <a href="https://tools.ietf.org/html/rfc7986#section-6.3"> RFC 7986 Section 6.3</a>
     */
    public enum FEATURES {
        /** Audio capability */
        AUDIO,
        /** Chat or instant messaging */
        CHAT,
        /** Blog or Atom feed */
        FEED,
        /** Moderator dial-in code */
        MODERATOR,
        /** Phone conference */
        PHONE,
        /** Screen sharing */
        SCREEN,
        /** Video capability */
        VIDEO;
    }

    private final Conference conference;

    private ConferenceBuilder() {
        this.conference = new Conference();

    }

    /**
     * New builder for {@link Conference}
     *
     * @return The builder
     */
    public static ConferenceBuilder newBuilder() {
        return new ConferenceBuilder();
    }

    /**
     * Adds a single feature
     *
     * @param feature The feature to set. See {@link FEATURES}
     * @return The builder
     */
    public ConferenceBuilder addFeature(String feature) {
        if (Strings.isNotEmpty(feature)) {
            List<String> features = conference.getFeatures();
            if (null == features) {
                features = new ArrayList<String>(5);
            }
            features.add(feature);
        }
        return this;
    }

    /**
     * Adds multiple features
     *
     * @param features The features. See {@link FEATURES}
     * @return The builder
     */
    public ConferenceBuilder addFeature(String... features) {
        for (String feature : features) {
            addFeature(feature);
        }
        return this;
    }

    /**
     * Sets {@link FEATURES#AUDIO} and {@link FEATURES#SCREEN} as features
     *
     * @return The builder
     */
    public ConferenceBuilder setDefaultFeatures() {
        return addFeature(FEATURES.AUDIO.name(), FEATURES.SCREEN.name());
    }

    /**
     * Sets the label
     *
     * @param lable The label to set
     * @return The builder
     */
    public ConferenceBuilder setLable(String lable) {
        conference.setLabel(lable);
        return this;
    }

    /**
     * Sets <code>Attendee dial-in</code> as label
     *
     * @return The builder
     */
    public ConferenceBuilder setAttendeeLable() {
        return setLable("Attendee dial-in");
    }

    /**
     * Sets <code>Moderator dial-in</code> as label
     *
     * @return The builder
     */
    public ConferenceBuilder setModeratorLable() {
        return setLable("Moderator dial-in");
    }

    /**
     * Sets the URI
     *
     * @param uri The uri to set
     * @return The builder
     */
    public ConferenceBuilder setUri(String uri) {
        conference.setUri(uri);
        return this;
    }

    /**
     * Sets <code>https://video-chat.example.com/audio?id=X</code> with
     * a random ID as URI
     *
     * @return The builder
     */
    public ConferenceBuilder setVideoChatUri() {
        return setUri("https://video-chat.example.com/audio?id=" + RANDOM.nextInt());
    }

    /**
     * Adds a new extended property to the conference
     *
     * @param key The key of the property
     * @param value The value of the property
     * @return The builder
     */
    public ConferenceBuilder addExtendedPropertyParameter(String key, String value) {
        Map<String, String> extendedParameters = conference.getExtendedParameters();
        if (null == extendedParameters) {
            extendedParameters = new ExtendedPropertyParameter();
        }
        extendedParameters.put(key, value);
        conference.setExtendedParameters(extendedParameters);

        return this;
    }

    /**
     * Add the property <code>group-id=1234</code>
     *
     * @return The builder
     */
    public ConferenceBuilder setGroupId() {
        return addExtendedPropertyParameter("group-id", "1234");
    }

    /**
     * Builds the conference
     *
     * @return A {@link Conference}
     */
    public Conference build() {
        return conference;
    }

    /**
     * Copies a conference item
     *
     * @param from The original conference item
     * @return A new conference item
     */
    public static Conference copy(Conference from) {
        Conference copied = new Conference();
        if (null != from.getExtendedParameters()) {
            // Deep copy to avoid changes in original properties
            ExtendedPropertyParameter parameters = new ExtendedPropertyParameter();
            for (Entry<String, String> entry : from.getExtendedParameters().entrySet()) {
                parameters.put(entry.getKey(), entry.getValue());
            }
            copied.setExtendedParameters(parameters);
        }

        copied.setFeatures(new ArrayList<>(from.getFeatures()));
        copied.setId(from.getId());
        copied.setLabel(from.getLabel());
        copied.setUri(from.getUri());
        return copied;
    }

}
