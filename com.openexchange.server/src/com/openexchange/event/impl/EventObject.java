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



package com.openexchange.event.impl;

import java.util.Date;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.session.Session;

/**
 * EventObject
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class EventObject {

	private final int module;
	private final int action;
	private final Object obj;
	private final Session session;
	private final Date creationDate;
	private boolean noDelay;

	public EventObject(final Appointment obj, final int action, final Session session) {
	    super();
		this.obj = obj;
        this.module = Types.APPOINTMENT;
        this.action = action;
        this.session = session;
        creationDate = new Date();
	}

	public EventObject(final Task obj, final int action, final Session session) {
        super();
		this.obj = obj;
        this.module = Types.TASK;
        this.action = action;
        this.session = session;
        creationDate = new Date();
	}

	public EventObject(final Contact obj, final int action, final Session session) {
        super();
		this.obj = obj;
        this.module = Types.CONTACT;
        this.action = action;
        this.session = session;
        creationDate = new Date();
	}

	public EventObject(final FolderObject obj, final int action, final Session session) {
        super();
		this.obj = obj;
        this.module = Types.FOLDER;
        this.action = action;
        this.session = session;
        creationDate = new Date();
	}

	public EventObject(final DocumentMetadata obj, final int action, final Session session) {
        super();
		this.obj = obj;
        this.module = Types.INFOSTORE;
        this.action = action;
        this.session = session;
        creationDate = new Date();
	}

	/**
     * Sets the <code>no-delay</code> flag
     *
     * @param noDelay The <code>no-delay</code> flag to set
     * @return This instance
     */
    public EventObject setNoDelay(boolean noDelay) {
        this.noDelay = noDelay;
        return this;
    }

    /**
     * Gets the <code>no-delay</code> flag
     *
     * @return The <code>no-delay</code> flag
     */
    public boolean isNoDelay() {
        return noDelay;
    }

	public int getModule() {
		return module;
	}

	public int getAction() {
		return action;
	}

	public Object getObject() {
		return obj;
	}

	public Session getSessionObject() {
		return session;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public String toString() {
		return new StringBuilder()
		.append("MODULE=")
		.append(module)
		.append(",ACTION=")
		.append(action).toString();
	}
}
