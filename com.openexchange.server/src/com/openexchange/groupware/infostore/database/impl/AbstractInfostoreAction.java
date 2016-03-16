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

package com.openexchange.groupware.infostore.database.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONInputStream;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.database.IncorrectStringSQLException;
import com.openexchange.database.tx.AbstractDBAction;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.utils.GetSwitch;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.session.Session;
import com.openexchange.tools.exceptions.SimpleIncorrectStringAttribute;
import com.openexchange.tools.session.ServerSession;

public abstract class AbstractInfostoreAction extends AbstractDBAction {

    /** The associated session (optional) */
    protected final Session optSession;

    /** The catalog queries */
	private InfostoreQueryCatalog queries;

	/**
	 * Initializes a new {@link AbstractInfostoreAction}.
	 *
	 * @param optSession The optional session
	 */
	protected AbstractInfostoreAction(Session optSession) {
	    super();
	    this.optSession = optSession;
	}

	private static User getUser(Session session) throws OXException {
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getUser();
        }
        return UserStorage.getInstance().getUser(session.getUserId(), session.getContextId());
    }

	/**
     * Launders specified <code>OXException</code> instance.
     *
     * @param e The <code>OXException</code> instance
     * @param optSession The optional session
     * @return The appropriate <code>OXException</code> instance
     * @throws OXException If operation fails
     */
    public static OXException launderOXException(OXException e, Session optSession) throws OXException {
        Throwable cause = e.getCause();
        if (!(cause instanceof IncorrectStringSQLException)) {
            return e;
        }

        return handleIncorrectStringError((IncorrectStringSQLException) cause, optSession);
    }

    /**
     * Handles specified <code>IncorrectStringSQLException</code> instance.
     *
     * @param incorrectStringError The incorrect string error to handle
     * @param optSession The optional session
     * @return The appropriate <code>OXException</code> instance
     * @throws OXException If operation fails
     */
    public static OXException handleIncorrectStringError(IncorrectStringSQLException incorrectStringError, Session optSession) throws OXException {
        Metadata metadata = Metadata.get(incorrectStringError.getColumn());
        if (null == metadata) {
            return InfostoreExceptionCodes.INVALID_CHARACTER_SIMPLE.create(incorrectStringError);
        }
        return handleIncorrectStringError(incorrectStringError, metadata, optSession);
    }

    /**
     * Handles specified <code>IncorrectStringSQLException</code> instance.
     *
     * @param incorrectStringError The incorrect string error to handle
     * @param metadata The associated meta data
     * @param optSession The optional session
     * @return The appropriate <code>OXException</code> instance
     * @throws OXException If operation fails
     */
    public static OXException handleIncorrectStringError(IncorrectStringSQLException incorrectStringError, Metadata metadata, Session optSession) throws OXException {
        if (null == optSession) {
            return InfostoreExceptionCodes.INVALID_CHARACTER.create(incorrectStringError, incorrectStringError.getIncorrectString(), incorrectStringError.getColumn());
        }
        String displayName = metadata.getDisplayName();
        if (null == displayName) {
            return InfostoreExceptionCodes.INVALID_CHARACTER.create(incorrectStringError, incorrectStringError.getIncorrectString(), incorrectStringError.getColumn());
        }

        String translatedName = StringHelper.valueOf(getUser(optSession).getLocale()).getString(displayName);
        OXException oxe = InfostoreExceptionCodes.INVALID_CHARACTER.create(incorrectStringError, incorrectStringError.getIncorrectString(), translatedName);
        oxe.addProblematic(new SimpleIncorrectStringAttribute(metadata.getId(), incorrectStringError.getIncorrectString()));
        return oxe;
    }

	@Override
	protected int doUpdates(List<UpdateBlock> updates) throws OXException {
	    try {
            return super.doUpdates(updates);
        } catch (OXException e) {
            throw launderOXException(e, optSession);
        }
	}

	@Override
	protected int doUpdates(UpdateBlock... updates) throws OXException {
	    try {
            return super.doUpdates(updates);
        } catch (OXException e) {
            throw launderOXException(e, optSession);
        }
	}

    protected final void fillStmt(final PreparedStatement stmt, final Metadata[] fields, final DocumentMetadata doc, final Object...additional) throws SQLException {
        fillStmt(1, stmt, fields, doc, additional);
    }

    /**
     * Fills the supplied prepared statement using the values of the denoted fields from a document.
     *
     * @param parameterIndex The (1-based) parameter index to start with
     * @param stmt The statement to populate
     * @param fields The used fields
     * @param doc The document to get the field values from
     * @param additional Any additional arbitrary fields to set in the statement after the document values were set
     * @return The updated parameter index
     * @throws SQLException
     */
    protected final int fillStmt(final int parameterIndex, final PreparedStatement stmt, final Metadata[] fields, final DocumentMetadata doc, final Object...additional) throws SQLException {
        final GetSwitch get = new GetSwitch(doc);
        int index = parameterIndex;
        for(final Metadata m : fields) {
            if (Metadata.META_LITERAL.getId() == m.getId()) {
                setMeta(index++, stmt, doc);
            } else {
                stmt.setObject(index++, process(m, m.doSwitch(get)));
            }
        }
        for(final Object o : additional) {
            stmt.setObject(index++, o);
        }
        return index;
    }

    private final Object process(final Metadata field, final Object value) {
        switch (field.getId()) {
        default:
            return value;
        case Metadata.CREATION_DATE:
        case Metadata.LOCKED_UNTIL:
        case Metadata.LAST_MODIFIED_UTC:
            return Long.valueOf(((Date) value).getTime());
        case Metadata.LAST_MODIFIED:
            return (value != null) ? Long.valueOf(((Date) value).getTime()) : Long.valueOf(System.currentTimeMillis());
        }
    }

    private final void setMeta(int parameterIndex, final PreparedStatement stmt, final DocumentMetadata doc) throws SQLException {
        final Map<String, Object> meta = doc.getMeta();
        if (null == meta || meta.isEmpty()) {
            stmt.setNull(parameterIndex, java.sql.Types.BLOB); // meta
        } else {
            try {
                final Object coerced = JSONCoercion.coerceToJSON(meta);
                if (null == coerced || JSONObject.NULL.equals(coerced)) {
                    stmt.setNull(parameterIndex, java.sql.Types.BLOB); // meta
                } else {
                    stmt.setBinaryStream(parameterIndex, new JSONInputStream((JSONValue) coerced, "US-ASCII")); // meta
                }
            } catch (final JSONException e) {
                throw new SQLException("Meta information could not be coerced to a JSON equivalent.", e);
            }
        }
    }

	public void setQueryCatalog(final InfostoreQueryCatalog queries){
		this.queries = queries;
	}

	public InfostoreQueryCatalog getQueryCatalog(){
		return this.queries;
	}

}
