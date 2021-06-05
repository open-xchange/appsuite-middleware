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

package com.openexchange.userfeedback;

/**
 *
 * {@link Feedback}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public class Feedback extends FeedbackMetaData {

    private Object content;

    Feedback(Builder builder) {
        super(builder);
        this.content = builder.content;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object pContent) {
        content = pContent;
    }

    /**
     * Creates a new builder instance.
     *
     * @return The builder instance
     */
    public static Builder builder(FeedbackMetaData meta) {
        return new Builder(meta);
    }

    public static class Builder extends FeedbackMetaData.Builder {

        Object content;

        Builder(FeedbackMetaData meta) {
            super(meta);
        }

        /**
         * Sets the stored content for this feedback
         *
         * @param content The content to set
         * @return This builder
         */
        public Builder setContent(Object content) {
            this.content = content;
            return this;
        }

        /**
         * Creates the <code>Feedback</code> instance from this builder's arguments.
         *
         * @return The <code>Feedback</code> instance
         */
        @Override
        public Feedback build() {
            return new Feedback(this);
        }
    }

}
