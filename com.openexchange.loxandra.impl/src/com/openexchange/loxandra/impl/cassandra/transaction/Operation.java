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
package com.openexchange.loxandra.impl.cassandra.transaction;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import me.prettyprint.hector.api.beans.Composite;

/**
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class Operation {

	private final String columnFamilyName;
	private final OperationAction action;
	private OperationState state;
	private final int sequenceNumber;
	private final String objectRowKey;
	private final Composite lockedObject;
	private JSONObject jsonOpData;

	/** Operation data - column name-value pairs */
	private final Map<String, String> opData;

	/**
	 * Default constructor
	 *
	 * @param cf ColumnFamily Name
	 * @param t Operation action
	 * @param seqNum sequence number
	 * @param rowKey rowKey for the locked object
	 */
	public Operation(String cf, OperationAction t, int seqNum, String rowKey) {
		columnFamilyName = cf;
		action = t;
		sequenceNumber = seqNum;
		objectRowKey = rowKey;
		jsonOpData = new JSONObject();

		opData = new HashMap<String, String>();
		lockedObject = new Composite(cf, rowKey.toString());
	}

	/**
	 * Add operation data
	 *
	 * @param column name
	 * @param data
	 */
	public void addOperationData(String columnName, String data) {
		opData.put(columnName, data);
	}

	/**
	 * Returns an iterator for all the keys in the set
	 *
	 * @return an iterator
	 */
	public Iterator<String> getColumnNamesIterator() {
		return opData.keySet().iterator();
	}

	/**
	 * Get the data of the operation for a specific column
	 * @param c
	 * @return
	 */
	public String getData(String c) {
		return opData.get(c);
	}

	/**
	 * Getter for column family name
	 *
	 * @return the column family name
	 */
	public String getColumnFamilyName() {
		return columnFamilyName;
	}

	/**
	 * Getter for operation action
	 * @return the operation action
	 */
	public OperationAction getAction() {
		return action;
	}

	/**
	 * Getter for sequence number
	 * @return the sequence number
	 */
	public int getSequenceNumber() {
		return sequenceNumber;
	}

	/**
	 * @return the object row key
	 */
	public Composite getLockedObject() {
		return lockedObject;
	}

	/**
	 * @return the objectRowKey
	 */
	public String getObjectRowKey() {
		return objectRowKey;
	}

	public void compileJSONObject() {
		jsonOpData = new JSONObject();
		try {
			jsonOpData.put("cf", columnFamilyName);
			jsonOpData.put("rowkey", objectRowKey);
			jsonOpData.put("action", action.toString());
			jsonOpData.put("seqNum", sequenceNumber);
			jsonOpData.put("data", opData);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		System.out.println(jsonOpData.toString());
	}

	public JSONObject getJSONData() {
		return jsonOpData;
	}

	public void addOperationData(JSONObject data) {
		Iterator<String> it = data.keys();
		while (it.hasNext()) {
			String string = it.next();
			try {
				opData.put(string, data.getString(string));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}