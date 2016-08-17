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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import me.prettyprint.cassandra.serializers.CompositeSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.serializers.UUIDSerializer;
import me.prettyprint.cassandra.service.clock.MicrosecondsClockResolution;
import me.prettyprint.cassandra.service.template.ColumnFamilyTemplate;
import me.prettyprint.cassandra.service.template.ColumnFamilyUpdater;
import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate;
import me.prettyprint.cassandra.utils.TimeUUIDUtils;
import me.prettyprint.hector.api.ClockResolution;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.Composite;
import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;

import com.openexchange.exception.OXException;
import com.openexchange.loxandra.impl.cassandra.CassandraEAVContactFactoryServiceImpl;
import com.openexchange.loxandra.impl.cassandra.transaction.Operation;
import com.openexchange.loxandra.impl.cassandra.transaction.Transaction;
import com.openexchange.loxandra.impl.cassandra.transaction.TransactionManager;

/**
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 *
 */
public class Transaction {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Transaction.class);

	private UUID txKey;

	private static String CF_TRANSACTION_LOG = "TransactionLog";

	private static final int attempsToAcquireLock = 10;

	private final ColumnFamilyTemplate<UUID, Composite> transactionTemplate;

	private final Keyspace keyspace;

	private List<Operation> operations;

	private static final UUIDSerializer us = UUIDSerializer.get();
	private static final CompositeSerializer cs = CompositeSerializer.get();
	private static final StringSerializer ss = StringSerializer.get();

	private static final UUID STATUS_ROW = new UUID(Long.MAX_VALUE, Long.MAX_VALUE);

	/** sleep threshold in msec */
	private static final long waitThreshold = 50;

	private static final boolean DO_CLEAN = true;

	/**
	 * Default constructor
	 */
	public Transaction() {
		operations = new ArrayList<Operation>();

		keyspace = CassandraEAVContactFactoryServiceImpl.getKeyspace();
		transactionTemplate = new ThriftColumnFamilyTemplate<UUID, Composite>(keyspace, CF_TRANSACTION_LOG, us, cs);
	}

	/**
	 * Constructs a new transaction and gets as parameters Operation objects
	 * @param ops Operation objects
	 */
	public Transaction(Operation ... ops) {
		this();

		for(Operation o: ops) {
			operations.add(o);
		}
		generateTXKey();
	}

	public Transaction(ArrayList<Operation> ops) {
		this();
		operations = ops;
		generateTXKey();
	}

	/**
	 * Constructor used to re-create a transaction from the db
	 * @param txKey
	 */
	public Transaction(UUID txKey, ArrayList<Operation> ops) {
		this();
		this.txKey = txKey;
		operations = ops;

		try {
			executeOperations();
			cleanupTransactionLog();
		} catch (OXException e) {
			e.printStackTrace();
		}
	}

	public Transaction(UUID txKey) {
		this();
		this.txKey = txKey;
	}

	/**
	 * Generate a transaction key
	 */
	private void generateTXKey() {
		ClockResolution clock = new MicrosecondsClockResolution();
		txKey = TimeUUIDUtils.getTimeUUID(clock.createClock());
	}

	/**
	 * Commit the Transaction
	 * @throws OXException
	 */
	public void commit() throws OXException {
		if (txKey == null) {
			generateTXKey();

			try {
				log();
			} catch (HectorException h) {
				h.printStackTrace();
			}

		}

		boolean lockAcquired = false;
		int attemps = 0;

		while (!(lockAcquired = TransactionManager.getInstance().acquireLock(this)) && attemps <= attempsToAcquireLock)  {
			attemps++;
			try {
				System.out.println("WAITING - Attemps: " + attemps);
				synchronized (this) {
					wait(waitThreshold);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// failed to acquire lock, aborting
		if (lockAcquired == false) {
            throw new OXException(666, "LOCKING TIMED OUT");
        }

		// lock acquired, executing operations
		try {
			System.out.println("LOCK ACQUIRED! YEAH!");
			executeOperations();
			if (DO_CLEAN) {
                cleanupTransactionLog();
            }
			TransactionManager.getInstance().releaseLock(this);
		} catch (HectorException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Write transaction to commit log
	 */
	private void log() {
		Iterator<Operation> it = operations.iterator();
		while (it.hasNext()) {
			Operation o = it.next();

			ColumnFamilyUpdater<UUID, Composite> transactionUpdater = transactionTemplate.createUpdater(txKey);

			Composite composite = new Composite();
			composite.add(o.getSequenceNumber());
			composite.add("Status");
			System.out.println(composite);
			transactionUpdater.setString(composite, OperationState.CREATED.toString());

			composite = new Composite();
			composite.add(o.getSequenceNumber());
			composite.add("zData");
			System.out.println(composite);
			transactionUpdater.setString(composite, o.getJSONData().toString());



			/*Iterator<String> itKeys = o.getColumnNamesIterator();
			while (itKeys.hasNext()) {
				String key = (String) itKeys.next();

				Composite compo = new Composite();
				compo.add(o.getSequenceNumber());
				compo.add(o.getColumnFamilyName());
				compo.add(key);

				Object obj = o.getData(key);
				if (obj instanceof UUID) {
					transactionUpdater.setUUID(compo, (UUID)o.getData(key));
				} else if (obj instanceof Date) {
					transactionUpdater.setDate(compo, (Date)o.getData(key));
				} else {
					transactionUpdater.setString(compo, (String)o.getData(key));
				}

				transactionUpdater.setString(compo, o.getData(key));

				Object rowKey = o.getObjectRowKey();
				if (rowKey instanceof UUID)
					transactionUpdater.setUUID(new Composite(o.getSequenceNumber(), o.getColumnFamilyName(), "ROW_KEY"), (UUID)rowKey);
				else if (rowKey instanceof String)
					transactionUpdater.setString(new Composite(o.getSequenceNumber(), o.getColumnFamilyName(), "ROW_KEY"), (String)rowKey);

				transactionUpdater.setString(new Composite(o.getSequenceNumber(), o.getColumnFamilyName(), "ROW_KEY"), o.getObjectRowKey());
				transactionUpdater.setString(new Composite(o.getSequenceNumber(), o.getColumnFamilyName(), o.getAction().toString()), OperationState.CREATED.toString());
			}*/

			try {
				transactionTemplate.update(transactionUpdater);
				Mutator<UUID> m = HFactory.createMutator(keyspace, us);
				m.insert(STATUS_ROW, CF_TRANSACTION_LOG, HFactory.createColumn(new Composite(OperationState.PENDING.toString(), txKey.toString()), getOperationsCount()));
			} catch (HectorException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Execute Operations in the Transaction
	 * @throws OXException
	 */
	private void executeOperations() throws OXException {
		Iterator<Operation> operationsIterator = operations.iterator();

		while (operationsIterator.hasNext()) {

			Operation o = operationsIterator.next();
			Iterator<String> itColumnNames = o.getColumnNamesIterator();

			log.info("Executing {} in {} with ROW KEY: {}", o.getAction(), o.getColumnFamilyName(), o.getLockedObject());

			ColumnFamilyUpdater<UUID, Composite> transactionUpdater = transactionTemplate.createUpdater(txKey);


			// INSERT OPERATION (also UPDATE)
			if (o.getAction().equals(OperationAction.INSERT)) {
				Mutator<UUID> m = HFactory.createMutator(keyspace, us);

				while (itColumnNames.hasNext()) {
					String columnName = itColumnNames.next();
					Composite compo = createComposite(columnName);
					m.addInsertion(UUID.fromString(o.getObjectRowKey()), o.getColumnFamilyName(), HFactory.createColumn(compo, o.getData(columnName)));
				}

				try {
					m.execute();
					//transactionUpdater.setString(new Composite(o.getSequenceNumber(), o.getColumnFamilyName(), o.getAction().toString()), OperationState.SUCCEEDED.toString());
					transactionUpdater.setString(new Composite(o.getSequenceNumber(), "Status"), OperationState.SUCCEEDED.toString());
					transactionTemplate.update(transactionUpdater);
				} catch (HectorException h) {
					h.printStackTrace();
					throw new OXException(666, "Operation failed!");
				}



			// DELETE OPERATION
			} else if (o.getAction().equals(OperationAction.DELETE)) {

				Mutator<UUID> m = HFactory.createMutator(keyspace, us);

				if (!itColumnNames.hasNext()) { //if no column names specified, delete the entire row
					m.addDeletion(UUID.fromString(o.getObjectRowKey()), o.getColumnFamilyName());
				} else {

					while (itColumnNames.hasNext()) {
						String columnName = itColumnNames.next();
						Composite compo = createComposite(columnName);
						m.addDeletion(UUID.fromString(o.getObjectRowKey()), o.getColumnFamilyName(), compo, cs);
					}
				}

				try {
					m.execute();
					//transactionUpdater.setString(new Composite(o.getSequenceNumber(), o.getColumnFamilyName(), o.getAction().toString()), OperationState.SUCCEEDED.toString());
					transactionUpdater.setString(new Composite(o.getSequenceNumber(), "Status"), OperationState.SUCCEEDED.toString());
					transactionTemplate.update(transactionUpdater);
				} catch (HectorException h) {
					h.printStackTrace();
					throw new OXException(666, "Operation failed!");
				}


			// INCREMENT OPERATION
			} else if (o.getAction().equals(OperationAction.INCREMENT)) {
				while (itColumnNames.hasNext()) { //usually one iteration, since we only do 1 increment per operation
					String key = itColumnNames.next();
					UUID cn = UUID.fromString(key);
					try {
						Mutator<String> mu = HFactory.createMutator(keyspace, ss);
						mu.incrementCounter(o.getObjectRowKey(), o.getColumnFamilyName(), cn, Long.parseLong(o.getData(key)));
						//transactionUpdater.setString(new Composite(o.getSequenceNumber(), o.getColumnFamilyName(), o.getAction().toString()), OperationState.SUCCEEDED.toString());
						transactionUpdater.setString(new Composite(o.getSequenceNumber(), "Status"), OperationState.SUCCEEDED.toString());
						transactionTemplate.update(transactionUpdater);
					} catch (HectorException h) {
						h.printStackTrace();
						throw new OXException(666, "Operation failed!");
					}
				}


			// DECREMENT OPERATION
			} else if (o.getAction().equals(OperationAction.DECREMENT)) {

				while (itColumnNames.hasNext()) { //usually one iteration, since we only do 1 increment per operation
					String key = itColumnNames.next();
					UUID cn = UUID.fromString(key);
					try {
						Mutator<String> mu = HFactory.createMutator(keyspace, ss);
						mu.decrementCounter(o.getObjectRowKey(), o.getColumnFamilyName(), cn, Long.parseLong(o.getData(key)));
						//transactionUpdater.setString(new Composite(o.getSequenceNumber(), o.getColumnFamilyName(), o.getAction().toString()), OperationState.SUCCEEDED.toString());
						transactionUpdater.setString(new Composite(o.getSequenceNumber(), "Status"), OperationState.SUCCEEDED.toString());
						transactionTemplate.update(transactionUpdater);
					} catch (HectorException h) {
						h.printStackTrace();
						throw new OXException(666, "Operation failed!");
					}
				}
			}
		}

		try {
			Mutator<UUID> m = HFactory.createMutator(keyspace, us);
			if (DO_CLEAN) {
                m.delete(STATUS_ROW, CF_TRANSACTION_LOG, new Composite(OperationState.PENDING.toString(), txKey.toString()), cs);
            }
		} catch (HectorException h) {
			h.printStackTrace();
		}
	}

	/**
	 * Recreate a composite key out of a string column name separated by a semicolon
	 * @param columnName
	 * @return
	 */
	private Composite createComposite(String columnName) {
		String[] compositeKeys = columnName.split(":");

		Composite compo = new Composite();
		for (int i = 0; i < compositeKeys.length; i++) {
			compo.add(compositeKeys[i]);
		}

		return compo;
	}

	/**
	 * Add an Operation to the Transaction
	 * @param o Operation to be added
	 */
	public void addOperation(Operation o) {
		o.compileJSONObject();
		operations.add(o);
	}

	/**
	 * Cleanup transaction log
	 */
	private void cleanupTransactionLog() {
		Mutator<UUID> m = HFactory.createMutator(keyspace, us);
		m.addDeletion(txKey, CF_TRANSACTION_LOG);
		if (DO_CLEAN) {
            m.execute();
        }
	}


	/**
	 * Rollback the Transaction
	 * @throws UnsupportedOperationException
	 */
	public void rollback() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Getter for operations
	 * @return the operations of the transaction as a List
	 */
	public List<Operation> getOperations() {
		return operations;
	}

	/**
	 * Getter for txKey
	 * @return
	 */
	public UUID getTXKey() {
		return txKey;
	}

	/**
	 * Return the operations count
	 * @return
	 */
	public int getOperationsCount() {
		return operations.size();
	}
}