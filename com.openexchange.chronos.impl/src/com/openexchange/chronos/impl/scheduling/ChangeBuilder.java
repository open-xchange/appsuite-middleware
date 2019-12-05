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

package com.openexchange.chronos.impl.scheduling;

import java.util.LinkedList;
import java.util.List;
import com.openexchange.annotation.Nullable;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.scheduling.changes.Change;
import com.openexchange.chronos.scheduling.changes.Description;

/**
 * {@link ChangeBuilder}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class ChangeBuilder {

    protected RecurrenceId recurrenceId;
    protected List<Description> descriptions = new LinkedList<>();

    /**
     * Initializes a new {@link ChangeBuilder}.
     */
    public ChangeBuilder() {
        super();
    }

    /**
     * Sets the recurrenceId
     *
     * @param recurrenceId The recurrenceId to set
     * @return This {@link ChangeBuilder} instance
     */
    public ChangeBuilder setRecurrenceId(RecurrenceId recurrenceId) {
        this.recurrenceId = recurrenceId;
        return this;
    }

    /**
     * Sets the descriptions
     *
     * @param descriptions The descriptions to set
     * @return This {@link ChangeBuilder} instance
     */
    public ChangeBuilder setDescriptions(List<Description> descriptions) {
        this.descriptions.addAll(descriptions);
        return this;
    }

    /**
     * Sets the descriptions
     *
     * @param description The description to add
     * @return This {@link ChangeBuilder} instance
     */
    public ChangeBuilder addDescriptions(Description description) {
        this.descriptions.add(description);
        return this;
    }

    public Change build() {
        return new ChangeImpl(this);
    }

}

class ChangeImpl implements Change {

    private final RecurrenceId recurrenceId;
    private final List<Description> descriptions;

    /**
     * Initializes a new {@link ChangeImpl}.
     * 
     * @param builder The builder
     */
    public ChangeImpl(ChangeBuilder builder) {
        super();
        this.recurrenceId = builder.recurrenceId;
        this.descriptions = builder.descriptions;
    }

    @Override
    @Nullable
    public RecurrenceId getRecurrenceId() {
        return recurrenceId;
    }

    @Override
    public List<Description> getDescriptions() {
        return descriptions;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((descriptions == null) ? 0 : descriptions.hashCode());
        result = prime * result + ((recurrenceId == null) ? 0 : recurrenceId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ChangeImpl)) {
            return false;
        }
        ChangeImpl other = (ChangeImpl) obj;
        if (descriptions == null) {
            if (other.descriptions != null) {
                return false;
            }
        } else if (!descriptions.equals(other.descriptions)) {
            return false;
        }
        if (recurrenceId == null) {
            if (other.recurrenceId != null) {
                return false;
            }
        } else if (recurrenceId.compareTo(other.recurrenceId) != 0) {
            return false;
        }
        return true;
    }

}
