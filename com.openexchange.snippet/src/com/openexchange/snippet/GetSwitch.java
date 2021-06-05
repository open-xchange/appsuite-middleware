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

package com.openexchange.snippet;

/**
 * {@link GetSwitch}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class GetSwitch implements PropertySwitch {

    /**
     * The snippet.
     */
    protected final Snippet snippet;

    /**
     * Initializes a new {@link GetSwitch}.
     *
     * @param snippet The snippet to get properties from
     */
    public GetSwitch(final Snippet snippet) {
        super();
        if (null == snippet) {
            throw new IllegalArgumentException("Snippet must not be null.");
        }
        this.snippet = snippet;
    }

    @Override
    public Object id() {
        return Integer.valueOf(snippet.getId());
    }

    @Override
    public Object properties() {
        return snippet.getUnnamedProperties();
    }

    @Override
    public Object content() {
        return snippet.getContent();
    }

    @Override
    public Object attachments() {
        return snippet.getAttachments();
    }

    @Override
    public Object accountId() {
        return Integer.valueOf(snippet.getAccountId());
    }

    @Override
    public Object type() {
        return snippet.getType();
    }

    @Override
    public Object displayName() {
        return snippet.getDisplayName();
    }

    @Override
    public Object module() {
        return snippet.getModule();
    }

    @Override
    public Object createdBy() {
        return Integer.valueOf(snippet.getCreatedBy());
    }

    @Override
    public Object shared() {
        return Boolean.valueOf(snippet.isShared());
    }

    @Override
    public Object misc() {
        return snippet.getMisc();
    }

}
