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

package com.openexchange.i18n.tools.replacement;

import java.text.DateFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import com.openexchange.groupware.i18n.Notifications;
import com.openexchange.groupware.notify.EmailableParticipant;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.i18n.tools.TemplateReplacement;
import com.openexchange.i18n.tools.TemplateToken;

/**
 * {@link ResourcesReplacement} - Replacement for
 * {@link TemplateToken#RESOURCES resources}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class ResourcesReplacement implements TemplateReplacement {

    private static final String CRLF = "\r\n";

    private boolean changed;

    private Locale locale;

    private StringHelper stringHelper;

    private SortedSet<EmailableParticipant> resourcesSet;

    /**
     * Initializes a new {@link ResourcesReplacement}
     */
    public ResourcesReplacement(final SortedSet<EmailableParticipant> resourcesSet) {
        super();
        this.resourcesSet = resourcesSet;
    }

    @Override
    public boolean changed() {
        return changed;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final ResourcesReplacement clone = (ResourcesReplacement) super.clone();
        clone.locale = (Locale) (locale == null ? null : locale.clone());
        clone.stringHelper = null;
        clone.resourcesSet = new TreeSet<EmailableParticipant>();
        for (final EmailableParticipant p : resourcesSet) {
            clone.resourcesSet.add((EmailableParticipant) p.clone());
        }
        return clone;
    }

    @Override
    public TemplateReplacement getClone() throws CloneNotSupportedException {
        return (TemplateReplacement) clone();
    }

    @Override
    public boolean relevantChange() {
        if (!changed) {
            return false;
        }
        for (final Iterator<EmailableParticipant> iter = resourcesSet.iterator(); iter.hasNext();) {
            final EmailableParticipant participant = iter.next();
            if (participant.state == EmailableParticipant.STATE_NEW) {
                /*
                 * Resource was newly added
                 */
                return true;
            } else if (participant.state == EmailableParticipant.STATE_REMOVED) {
                /*
                 * Resource was removed
                 */
                return true;
            }
        }
        return false;
    }

    @Override
    public String getReplacement() {
        if (resourcesSet.isEmpty()) {
            return getStringHelper().getString(Notifications.NO_RESOURCES);
        }
        final int size = resourcesSet.size();
        final StringBuilder b = new StringBuilder(size << 5);
        final StringHelper stringHelper = getStringHelper();
        final Iterator<EmailableParticipant> iter = resourcesSet.iterator();
        /*
         * Process fist resource
         */
        boolean added = processResource(b, stringHelper, iter.next());
        /*
         * Process remaining (if any)
         */
        for (int i = 1; i < size; i++) {
            if (added) {
                b.append(CRLF);
            }
            added = processResource(b, stringHelper, iter.next());
        }
        return b.toString();
    }

    private boolean processResource(final StringBuilder b, final StringHelper stringHelper,
            final EmailableParticipant resource) {
        String name = resource.displayName;
        if (name == null) {
            name = resource.email;
        }
        if (changed) {
            if (resource.state == EmailableParticipant.STATE_NEW) {
                /*
                 * Resource was newly added
                 */
                b.append(TemplateReplacement.PREFIX_MODIFIED).append(stringHelper.getString(Notifications.ADDED))
                        .append(": ");
                b.append(name);
            } else if (resource.state == EmailableParticipant.STATE_REMOVED) {
                /*
                 * Resource was removed
                 */
                b.append(TemplateReplacement.PREFIX_MODIFIED).append(stringHelper.getString(Notifications.REMOVED))
                        .append(": ");
                b.append(name);
            } else {
                /*
                 * Resource was neither newly added nor removed
                 */
                b.append(name);
            }
            return true;
        }
        if (resource.state != EmailableParticipant.STATE_REMOVED) {
            /*
             * Just add resources's display name
             */
            b.append(name);
            return true;
        }
        return false;
    }

    @Override
    public TemplateToken getToken() {
        return TemplateToken.RESOURCES;
    }

    @Override
    public TemplateReplacement setChanged(final boolean changed) {
        this.changed = changed;
        return this;
    }

    private Locale getLocale() {
        if (locale == null) {
            locale = Locale.ENGLISH;
        }
        return locale;
    }

    private StringHelper getStringHelper() {
        if (stringHelper == null) {
            stringHelper = StringHelper.valueOf(getLocale());
        }
        return stringHelper;
    }

    @Override
    public TemplateReplacement setLocale(final Locale locale) {
        if (locale == null || locale.equals(this.locale)) {
            return this;
        }
        this.locale = locale;
        stringHelper = null;
        return this;
    }

    @Override
    public TemplateReplacement setTimeZone(final TimeZone timeZone) {
        return this;
    }

    @Override
    public boolean merge(final TemplateReplacement other) {
        if (!ResourcesReplacement.class.isInstance(other)) {
            /*
             * Class mismatch or null
             */
            return false;
        }
        if (!TemplateToken.RESOURCES.equals(other.getToken())) {
            /*
             * Token mismatch
             */
            return false;
        }
        if (!other.changed()) {
            /*
             * Other replacement does not reflect a changed value; leave
             * unchanged
             */
            return false;
        }
        final ResourcesReplacement o = (ResourcesReplacement) other;
        this.changed = true;
        if (this.resourcesSet == null || o.resourcesSet != null) {
            this.resourcesSet = o.resourcesSet;
        }
        return true;
    }

    @Override
    public TemplateReplacement setDateFormat(DateFormat dateFormat) {
        return this;
    }

}
