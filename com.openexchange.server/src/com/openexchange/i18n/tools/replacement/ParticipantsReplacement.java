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
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.i18n.Notifications;
import com.openexchange.groupware.notify.EmailableParticipant;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.i18n.tools.TemplateReplacement;
import com.openexchange.i18n.tools.TemplateToken;

/**
 * {@link ParticipantsReplacement} - Replacement for {@link TemplateToken#PARTICIPANTS participants}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ParticipantsReplacement implements TemplateReplacement {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ParticipantsReplacement.class);

    private static final String CRLF = "\r\n";

    private boolean changed;

    private Locale locale;

    private StringHelper stringHelper;

    private SortedSet<EmailableParticipant> participantsSet;

    /**
     * Initializes a new {@link ParticipantsReplacement}
     */
    public ParticipantsReplacement(final SortedSet<EmailableParticipant> participantsSet) {
        super();
        this.participantsSet = participantsSet;
    }

    @Override
    public boolean changed() {
        return changed;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final ParticipantsReplacement clone = (ParticipantsReplacement) super.clone();
        clone.locale = (Locale) (locale == null ? null : locale.clone());
        clone.stringHelper = null;
        clone.participantsSet = new TreeSet<EmailableParticipant>();
        for (final EmailableParticipant p : participantsSet) {
            clone.participantsSet.add((EmailableParticipant) p.clone());
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
        for (EmailableParticipant participant : participantsSet) {
            if (participant.state == EmailableParticipant.STATE_NEW) {
                /*
                 * Participant was newly added
                 */
                return true;
            } else if (participant.state == EmailableParticipant.STATE_REMOVED) {
                /*
                 * Participant was removed
                 */
                return true;
            }
        }
        return false;
    }

    @Override
    public String getReplacement() {
        if (participantsSet.isEmpty()) {
            return "";
        }
        final int size = participantsSet.size();
        final StringBuilder b = new StringBuilder(size << 5);
        final Locale l = getLocale();
        final StringHelper stringHelper = getStringHelper();
        final Iterator<EmailableParticipant> iter = participantsSet.iterator();
        /*
         * Process first
         */
        boolean added = processParticipant(b, l, stringHelper, iter.next());
        /*
         * Iterate remaining (if any)
         */
        for (int i = 1; i < size; i++) {
            if (added) {
                b.append(CRLF);
            }
            added = processParticipant(b, l, stringHelper, iter.next());
        }
        return b.toString();
    }

    private boolean processParticipant(final StringBuilder b, final Locale l, final StringHelper stringHelper, final EmailableParticipant participant) {
        String name = participant.displayName;
        if (name == null) {
            name = participant.email;
        }
        if (changed) {
            if (participant.state == EmailableParticipant.STATE_NEW) {
                /*
                 * Participant was newly added
                 */
                b.append(TemplateReplacement.PREFIX_MODIFIED).append(stringHelper.getString(Notifications.ADDED)).append(": ");
                b.append(name);
                appendStatus(b, l, stringHelper, participant);
            } else if (participant.state == EmailableParticipant.STATE_REMOVED) {
                /*
                 * Participant was removed
                 */
                b.append(TemplateReplacement.PREFIX_MODIFIED).append(stringHelper.getString(Notifications.REMOVED)).append(": ");
                b.append(name);
            } else {
                /*
                 * Participant was neither newly added nor removed
                 */
                b.append(name);
                appendStatus(b, l, stringHelper, participant);
            }
            return true;
        }
        if (participant.state != EmailableParticipant.STATE_REMOVED) {
            /*
             * Just add participant's display name
             */
            b.append(name);
            appendStatus(b, l, stringHelper, participant);
            return true;
        }
        return false;
    }

    private void appendStatus(final StringBuilder b, final Locale l, final StringHelper stringHelper, final EmailableParticipant participant) {
        if (participant.type == Participant.USER) {
            b.append(" (");
            b.append(new StatusReplacement(participant.confirm, l).getReplacement());
            if (!com.openexchange.java.Strings.isEmpty(participant.confirmMessage)) {
                b.append(": ").append(participant.confirmMessage);
            }
            b.append(')');
        } else if (participant.type == Participant.EXTERNAL_USER) {
            b.append(" (");
            b.append(stringHelper.getString(Notifications.STATUS_EXTERNAL));
            b.append(')');
        }
    }

    @Override
    public TemplateToken getToken() {
        return TemplateToken.PARTICIPANTS;
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
        if (!ParticipantsReplacement.class.isInstance(other)) {
            /*
             * Class mismatch or null
             */
            return false;
        }
        if (!TemplateToken.PARTICIPANTS.equals(other.getToken())) {
            /*
             * Token mismatch
             */
            return false;
        }
        if (!other.changed()) {
            /*
             * Other replacement does not reflect a changed value; leave unchanged
             */
            return false;
        }
        final ParticipantsReplacement o = (ParticipantsReplacement) other;
        this.changed = true;
        if (this.participantsSet == null || o.participantsSet != null) {
            this.participantsSet = o.participantsSet;
        }
        return true;
    }

    @Override
    public TemplateReplacement setDateFormat(DateFormat dateFormat) {
        return this;
    }

}
