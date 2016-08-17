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

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import org.apache.cassandra.utils.ByteBufferUtil;
import org.json.JSONException;
import org.json.JSONObject;

import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
import me.prettyprint.cassandra.serializers.CompositeSerializer;
import me.prettyprint.cassandra.serializers.UUIDSerializer;
import me.prettyprint.cassandra.service.ColumnSliceIterator;
import me.prettyprint.cassandra.service.template.ColumnFamilyResult;
import me.prettyprint.cassandra.service.template.ColumnFamilyTemplate;
import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.Composite;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.exceptions.HUnavailableException;
import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.SliceQuery;

import com.openexchange.exception.OXException;
import com.openexchange.loxandra.impl.cassandra.CassandraEAVContactFactoryServiceImpl;

/**
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 *
 */
public class TransactionManager {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TransactionManager.class);

	private static Map<String, Queue<Transaction>> queue;

	private static String CF_TRANSACTION_LOG = "TransactionLog";

	private static final Keyspace keyspace = CassandraEAVContactFactoryServiceImpl.getKeyspace();

	private static final UUID LOCK_ROW = new UUID(0,0);
	private static final UUID STATUS_ROW = new UUID(Long.MAX_VALUE, Long.MAX_VALUE);

	private static final UUIDSerializer us = UUIDSerializer.get();
	private static final CompositeSerializer cs = CompositeSerializer.get();
	private static final ByteBufferSerializer bbs = ByteBufferSerializer.get();

	/** Locked column's TTL in seconds. A lock shouldn't be acquired more than 10 seconds,
	 * meaning that an operation at that particular column shouldn't exceed this time limit. */
	private static final int columnTTL = 10;

	private static TransactionManager instance;

	/**
	 * Default Constructor
	 */
	private TransactionManager() {
		queue = new HashMap<String, Queue<Transaction>>();
	}

	/**
	 * Check if there are uncommited changes into the 'TransactionLog' CF and if true, then replay
	 */
	public void checkAndReplay() {

		long timeStart = System.currentTimeMillis();

		try {

			Composite start = new Composite();
			start.addComponent(0, OperationState.PENDING.toString(), Composite.ComponentEquality.EQUAL);

			Composite end = new Composite();
			end.addComponent(0, OperationState.PENDING.toString(), Composite.ComponentEquality.GREATER_THAN_EQUAL);

			SliceQuery<UUID, Composite, ByteBuffer> sliceQuery = HFactory.createSliceQuery(keyspace, us, cs, bbs);
			sliceQuery.setColumnFamily(CF_TRANSACTION_LOG).setKey(STATUS_ROW);
			ColumnSliceIterator<UUID, Composite, ByteBuffer> itTransactions = new ColumnSliceIterator<UUID, Composite, ByteBuffer>(sliceQuery, start, end, false);

		 	while (itTransactions.hasNext()) {
				HColumn<Composite, ByteBuffer> hColumn = itTransactions.next();

				try {
					UUID txKey = UUID.fromString(ByteBufferUtil.string((ByteBuffer)hColumn.getName().get(1)));

					/*Composite s = new Composite();
					start.addComponent(0, ++opsCount, Composite.ComponentEquality.EQUAL);

					Composite e = new Composite();
					end.addComponent(0, ++opsCount, Composite.ComponentEquality.EQUAL);*/


					//SliceQuery<UUID, Composite, ByteBuffer> query = HFactory.createSliceQuery(keyspace, us, cs, bbs);
					//query.setKey(txKey).setColumnFamily(CF_TRANSACTION_LOG);
				 	//Iterator<HColumn<Composite, ByteBuffer>> itOps = query.setColumnFamily(CF_TRANSACTION_LOG).execute().get().getColumns().iterator();
					//ColumnSliceIterator<UUID, Composite, ByteBuffer> itOps = new ColumnSliceIterator<UUID, Composite, ByteBuffer>(query, null, new Composite("\uFFFF"), false);
					//ColumnSliceIterator<UUID, Composite, ByteBuffer> itOps = new ColumnSliceIterator<UUID, Composite, ByteBuffer>(query, s, e, false);

				 	//Iterator<HColumn<Composite, ByteBuffer>> it = sliceQ.getColumns().iterator();


					// EVERYTHING IS A FUCKING MESS >:-@

					//System.out.println("KEY[1]: " + ByteBufferUtil.string((ByteBuffer)hColumn.getName().get(1)));



					/*ArrayList<Operation> ops = new ArrayList<Operation>();
					Operation o;
					int pSeqNum = 0;
					while (itOps.hasNext()) {
						HColumn<Composite, ByteBuffer> h = (HColumn<Composite, ByteBuffer>) itOps.next();

						int seqNum = ((ByteBuffer)h.getName().get(0)).get(0);
						String cf = ByteBufferUtil.string((ByteBuffer)h.getName().get(1));
						OperationAction oa = OperationAction.getByString(ByteBufferUtil.string((ByteBuffer)h.getName().get(2)));
						String c = null;
						if (oa == null)
							c = ByteBufferUtil.string((ByteBuffer)h.getName().get(2));
						String data = ByteBufferUtil.string(h.getValue());

						/*if (seqNum > pSeqNum && oa != null) {
							pSeqNum = seqNum;
							//o = new Operation(cf, oa, pSeqNum, 1);
						}*/


						//System.out.println(Integer.valueOf(ByteBufferUtil.string((ByteBuffer)h.getName().get(0))));

						/*o = new Operation(ByteBufferUtil.string((ByteBuffer)h.getName().get(1)),
											OperationAction.getByString(ByteBufferUtil.string((ByteBuffer)h.getName().get(2))),
											ByteBufferUtil.toInt((ByteBuffer)h.getName().get(0)),
											ByteBufferUtil.string((ByteBuffer)h.getValue()));
						ops.add(o);*/

						/*byte[] b = ((ByteBuffer)h.getName().get(0)).array();
						String str = new String(b, "UTF8");

						System.out.println(/*"TXKEY: " + txKey +
											"CF: " + cf +
											", ACTION: " + oa +
											", SEQ: " + seqNum +
											", COLUMN: " + c +
											", DATA: " + ByteBufferUtil.string(h.getValue()));

					}*/


					//WORKS! ^^
					Transaction t = new Transaction(txKey);

					ColumnFamilyTemplate<UUID, Composite> template = new ThriftColumnFamilyTemplate<UUID, Composite>(keyspace, CF_TRANSACTION_LOG, us, cs);
					ColumnFamilyResult<UUID, Composite> res = template.queryColumns(txKey);

					Iterator<Composite> it = res.getColumnNames().iterator();

					while(it.hasNext()) {
						Composite c = it.next();
						String status = res.getString(c);
						if (!status.equals(OperationState.SUCCEEDED.toString())) {
							String data = res.getString(it.next());
							System.out.println("Data pending: " + data);
							try {
								JSONObject j = new JSONObject(data);
								Operation o = new Operation(j.getString("cf"), OperationAction.getByString(j.getString("action")), j.getInt("seqNum"), j.getString("rowkey"));
								o.addOperationData(j.getJSONObject("data"));
								t.addOperation(o);
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}

					try {
						t.commit();
					} catch (OXException e) {
						e.printStackTrace();
					}

					/*
					int pSeqNum = 0;
					int cSeqNum = 0;
					Iterator<Composite> it = res.getColumnNames().iterator();
					while(it.hasNext()) {
						Composite c = it.next();
						cSeqNum = ((ByteBuffer)c.get(0)).get(0);
						String cc = ByteBufferUtil.string((ByteBuffer)c.get(1));
						if (cc.equals("Status") && res.getString(c).equals(OperationState.SUCCEEDED.toString())) {
							Composite cPending = new Composite(cSeqNum, "Data");
							System.out.println(cPending);
							System.out.println("Data pending: " + res.getString(cPending));
						}
						/*
						cSeqNum = ((ByteBuffer)c.get(0)).get(0);
						if (pSeqNum < cSeqNum) {
							pSeqNum = cSeqNum;
							System.out.println("pSeqNum: " + pSeqNum+
											", cSeqNum: " + cSeqNum +
											", data: " + res.getString(c));
						}
					}*/

					//new Transaction(txKey, ops);

				} catch (CharacterCodingException e) {
					e.printStackTrace();
				}
			}

		 	long timeEnd = System.currentTimeMillis();
		 	System.out.println("Runtime for checkAndReplay(): " + (timeEnd - timeStart) + " msec.");
		} catch (HUnavailableException h) {
			log.error("Cannot replay transaction logs, because there may not be enough replicas present to handle consistency level.");
		}
	}

	/**
	 * Acquire lock
	 *
	 * @param tx Transaction to acquire the lock for
	 * @return true if the lock has been successfully acquired, false otherwise
	 */
	public synchronized boolean acquireLock(Transaction tx) throws OXException {
		boolean lockAcquired = false;
		Mutator<UUID> m = HFactory.createMutator(keyspace, us);
		Iterator<Operation> iter = tx.getOperations().iterator();

		while (iter.hasNext()) {
			Operation type = iter.next();

			String rowKey = type.getObjectRowKey().toString();

			Composite start = new Composite();
		 	start.addComponent(0, type.getColumnFamilyName(), Composite.ComponentEquality.EQUAL);
		 	start.addComponent(1, rowKey, Composite.ComponentEquality.EQUAL);

		 	Composite end = new Composite();
		 	end.addComponent(0, type.getColumnFamilyName(), Composite.ComponentEquality.EQUAL);
		 	end.addComponent(1, rowKey, Composite.ComponentEquality.EQUAL);

		 	SliceQuery<UUID, Composite, ByteBuffer> sliceQuery = HFactory.createSliceQuery(keyspace, us, cs, bbs);
		 	ColumnSlice<Composite, ByteBuffer> slice = sliceQuery.setColumnFamily(CF_TRANSACTION_LOG)
		 														.setKey(LOCK_ROW)
		 														.setRange(start, end, false, 1).execute().get();
		 	lockAcquired = slice.getColumns().size() == 0 ? true : false;

		 	//If another transaction already acquired a lock for one of the required objects, then put in queue
		 	if (!lockAcquired) {
		 		String key = type.getColumnFamilyName() + "-" + type.getObjectRowKey();
		 		Queue<Transaction> q = queue.get(key);
		 		if (q == null) {
		 			q = new FIFOQueue<Transaction>();
		 		}
		 		q.add(tx);
		 		queue.put(key, q);

		 		log.info("waiting for lock {} to be released", key);

		 		return lockAcquired;
		 	}

		 	HColumn<Composite, UUID> h = HFactory.createColumn(type.getLockedObject(), tx.getTXKey());
		 	h.setTtl(columnTTL);
		 	m.addInsertion(LOCK_ROW, CF_TRANSACTION_LOG, h);
		}

		try {
			m.execute();
		} catch (HectorException h) {
			h.printStackTrace();
			throw new OXException(666, "Failed to log the transaction");
		}

		return lockAcquired;
	}

	/**
	 * Release lock
	 *
	 * @param tx Transaction to release the lock for
	 * @return true if the lock has been successfully released, otherwise false
	 * @throws OXException
	 */
	public synchronized void releaseLock(Transaction tx) throws OXException {

		Mutator<UUID> m = HFactory.createMutator(keyspace, us);
		Iterator<Operation> it = tx.getOperations().iterator();

		List<Queue<Transaction>> lq = new ArrayList<Queue<Transaction>>();

		while (it.hasNext()) {
			Operation operation = it.next();
			m.addDeletion(LOCK_ROW, CF_TRANSACTION_LOG, operation.getLockedObject(), cs);
			Queue<Transaction> q = queue.get(operation.getColumnFamilyName() + "-" + operation.getObjectRowKey());
			if (q != null) {
                lq.add(q);
            }
		}

		m.execute();

		Iterator<Queue<Transaction>> itQ = lq.iterator();
		while (itQ.hasNext()) {
			Queue<Transaction> queue = itQ.next();
			Transaction txQ = queue.poll();
			synchronized (txQ) {
				txQ.notify();
			}
		}
	}

	/**
	 * Returns the instance of the TransactionManager. Optimized with the double locking mechanism.
	 * @return a TransactionManager instance
	 */
	public static TransactionManager getInstance() {
		if (instance == null) {
			synchronized(TransactionManager.class) {
				if (instance == null) {
                    instance = new TransactionManager();
                }
			}
		}

		return instance;
	}
}
