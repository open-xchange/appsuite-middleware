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

package com.openexchange.calendar.itip.generators;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import com.openexchange.calendar.itip.ITipRole;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.participants.ConfirmStatus;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;


/**
 * {@link NotificationParticipant}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class NotificationParticipant implements Cloneable {

    private Set<ITipRole> roles;
    private boolean external;
    private String email;
    private int identifier = -1;
    private String displayName;
    private ConfirmStatus confirmStatus = ConfirmStatus.NONE;
    private String comment;
    private User user;
    private Context ctx;
    private NotificationConfiguration configuration;
	private Locale locale;
	private boolean resource;
	private TimeZone tz;
	private int folderId;
	private boolean virtual;

    public NotificationParticipant(ITipRole role, boolean external, String email) {
        super();
        this.roles = EnumSet.of(role);
        this.external = external;
        this.email = email;
    }

    public NotificationParticipant(Set<ITipRole> roles, boolean external, String email) {
        super();
        this.roles = roles;
        this.external = external;
        this.email = email;
    }

    public NotificationParticipant(ITipRole role, boolean external, String email, int identifier) {
        super();
        this.roles = EnumSet.of(role);
        this.external = external;
        this.email = email;
        this.identifier = identifier;
    }

    public NotificationParticipant(Set<ITipRole> roles, boolean external, String email, int identifier) {
        super();
        this.roles = roles;
        this.external = external;
        this.email = email;
        this.identifier = identifier;
    }

    public String getDisplayName() {
        if (displayName == null) {
            return email;
        }
        return displayName;
    }


    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean hasRole(ITipRole role) {
    	return roles.contains(role);
    }

    public boolean isExternal() {
        return external;
    }


    public void setExternal(boolean external) {
        this.external = external;
    }


    public String getEmail() {
        return email;
    }


    public void setEmail(String email) {
        this.email = email;
    }

    public ConfirmStatus getConfirmStatus() {
        return confirmStatus;
    }


    public void setConfirmStatus(ConfirmStatus confirmStatus) {
        this.confirmStatus = confirmStatus;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((email == null) ? 0 : email.hashCode());
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        NotificationParticipant other = (NotificationParticipant) obj;
        if (email == null) {
            if (other.email != null) {
                return false;
            }
        } else if (!email.equals(other.email)) {
            return false;
        }
        return true;
    }


    public boolean matches(Participant object) {
        if (object instanceof ExternalUserParticipant) {
            return ((ExternalUserParticipant) object).getEmailAddress().equalsIgnoreCase(email);
        }
        if (object instanceof UserParticipant) {
            UserParticipant up = (UserParticipant) object;
            return up.getIdentifier() == identifier;
        }
        return false;
    }

    public int getIdentifier() {
        return identifier;
    }


    public String getComment() {
        return comment;
    }


    public void setComment(String comment) {
        this.comment = comment;
    }


    public User getUser() {
        return user;
    }


    public void setUser(User user) {
        this.user = user;
    }


    public void setContext(Context ctx) {
        this.ctx = ctx;
    }

    public Context getContext() {
        return ctx;
    }

    public NotificationConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(NotificationConfiguration configuration) {
        this.configuration = configuration;
    }

	public Locale getLocale() {
		if (locale == null) {
			return Locale.getDefault();
		}
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public void setResource(boolean b) {
		this.resource = true;
	}

	public boolean isResource() {
		return resource;
	}

	public TimeZone getTimeZone() {
		if (tz == null) {
			return TimeZone.getDefault();
		}
		return tz;
	}

	public void setTimezone(TimeZone tz) {
		this.tz = tz;
	}

	@Override
	public String toString() {
		return "NotificationParticipant [roles=" + roles + ", external="
				+ external + ", email=" + email + ", identifier=" + identifier
				+ ", displayName=" + displayName + "]";
	}

	public int getFolderId() {
		return folderId;
	}

	public void setFolderId(int folderId) {
		this.folderId = folderId;
	}

	@Override
    public NotificationParticipant clone() {
		NotificationParticipant clone = new NotificationParticipant(roles, external, comment);
		clone.roles = new HashSet<ITipRole>(this.roles);
		clone.external = this.external;
		clone.email = this.email;
		clone.identifier = this.identifier;
		clone.displayName = this.displayName;
		clone.confirmStatus = this.confirmStatus;
		clone.comment = this.comment;
		clone.user = this.user;
		clone.ctx = this.ctx;
		clone.configuration = this.configuration;
		clone.locale = this.locale;
		clone.resource = this.resource;
		clone.tz = this.tz;
		clone.folderId = this.folderId;
		clone.virtual = this.virtual;

		return clone;
	}

	public void setVirtual(boolean b) {
		this.virtual = b;
	}

	public boolean isVirtual() {
		return virtual;
	}

}
