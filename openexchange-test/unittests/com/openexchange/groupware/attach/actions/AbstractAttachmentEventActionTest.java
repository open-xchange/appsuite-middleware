package com.openexchange.groupware.attach.actions;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.openexchange.groupware.attach.AttachmentEvent;
import com.openexchange.groupware.attach.AttachmentListener;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.groupware.tx.TransactionException;

public abstract class AbstractAttachmentEventActionTest extends
		AbstractAttachmentActionTest {
	
	
	protected static final class MockAttachmentListener implements AttachmentListener {
		
		List<AttachmentMetadata> attached = new ArrayList<AttachmentMetadata>();
		Set<Integer> detached = new HashSet<Integer>();
		
		
		public long attached(AttachmentEvent e) throws Exception{
			attached.add(e.getAttachment());
			e.getWriteConnection();
			return System.currentTimeMillis();
		}
		
		public long detached(AttachmentEvent e) throws Exception{
			for(int id : e.getDetached()) {
				detached.add(id);
			}
			e.getWriteConnection();
			return System.currentTimeMillis();
		}
		
		public List<AttachmentMetadata> getAttached(){
			return attached;
		}
		
		public Set<Integer> getDetached(){
			return detached;
		}
		
		public void clear(){
			attached.clear();
			detached.clear();
		}
	}
	
	public static final class MockDBProvider implements DBProvider{

		private Set<Integer> readIds = new HashSet<Integer>();
		private Set<Integer> writeIds = new HashSet<Integer>();
		
		private boolean ok = true;
		private boolean called = false;
		
		private StringBuffer log = new StringBuffer();
		
		public Connection getReadConnection(Context ctx) throws TransactionException {
			StupidConnection stupid = new StupidConnection();
			readIds.add(stupid.getCounter());
			log.append("Get ReadConnection: "+stupid.getCounter()+"\n");
			called = true;
			return stupid;
		}

		public Connection getWriteConnection(Context ctx) throws TransactionException {
			StupidConnection stupid = new StupidConnection();
			writeIds.add(stupid.getCounter());
			log.append("Get WriteConnection: "+stupid.getCounter()+"\n");
			called = true;
			return stupid;
		}

		public void releaseReadConnection(Context ctx, Connection con) {
			int counter = ((StupidConnection) con).getCounter();
			ok(readIds.remove(counter));
			log.append("Release ReadConnection: "+counter+"\n");

		}

		public void releaseWriteConnection(Context ctx, Connection con) {
			int counter = ((StupidConnection) con).getCounter();
			ok(writeIds.remove(counter));
			log.append("Release WriteConnection: "+counter+"\n");
		}
		
		private void ok(boolean b) {
			this.ok = ok && b;
		}
		
		public boolean allOK(){
			return ok && readIds.isEmpty() && writeIds.isEmpty();
		}
		
		public boolean called(){
			return called;
		}
		
		public String getStatus(){
			return String.format("OK : %s ReadIds: %s WriteIds: %s Called: %s \n LOG: %s", String.valueOf(ok), readIds.toString(), writeIds.toString(), String.valueOf(called), log.toString());
		}
		
	}
	
	private static final class StupidConnection implements Connection{

		private static int COUNTER = 0;
		
		private int refCount;
		
		public StupidConnection(){
			COUNTER++;
			refCount = COUNTER;
		}
		
		public int getCounter(){
			return refCount;
		}

		public void clearWarnings() throws SQLException {
			// TODO Auto-generated method stub
			
		}

		public void close() throws SQLException {
			// TODO Auto-generated method stub
			
		}

		public void commit() throws SQLException {
			// TODO Auto-generated method stub
			
		}

		public Statement createStatement() throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public Statement createStatement(int arg0, int arg1) throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public Statement createStatement(int arg0, int arg1, int arg2) throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean getAutoCommit() throws SQLException {
			// TODO Auto-generated method stub
			return false;
		}

		public String getCatalog() throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public int getHoldability() throws SQLException {
			// TODO Auto-generated method stub
			return 0;
		}

		public DatabaseMetaData getMetaData() throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public int getTransactionIsolation() throws SQLException {
			// TODO Auto-generated method stub
			return 0;
		}

		public Map<String, Class<?>> getTypeMap() throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public SQLWarning getWarnings() throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean isClosed() throws SQLException {
			// TODO Auto-generated method stub
			return false;
		}

		public boolean isReadOnly() throws SQLException {
			// TODO Auto-generated method stub
			return false;
		}

		public String nativeSQL(String arg0) throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public CallableStatement prepareCall(String arg0) throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public CallableStatement prepareCall(String arg0, int arg1, int arg2) throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public CallableStatement prepareCall(String arg0, int arg1, int arg2, int arg3) throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public PreparedStatement prepareStatement(String arg0) throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public PreparedStatement prepareStatement(String arg0, int arg1) throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public PreparedStatement prepareStatement(String arg0, int[] arg1) throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public PreparedStatement prepareStatement(String arg0, String[] arg1) throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public PreparedStatement prepareStatement(String arg0, int arg1, int arg2) throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public PreparedStatement prepareStatement(String arg0, int arg1, int arg2, int arg3) throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public void releaseSavepoint(Savepoint arg0) throws SQLException {
			// TODO Auto-generated method stub
			
		}

		public void rollback() throws SQLException {
			// TODO Auto-generated method stub
			
		}

		public void rollback(Savepoint arg0) throws SQLException {
			// TODO Auto-generated method stub
			
		}

		public void setAutoCommit(boolean arg0) throws SQLException {
			// TODO Auto-generated method stub
			
		}

		public void setCatalog(String arg0) throws SQLException {
			// TODO Auto-generated method stub
			
		}

		public void setHoldability(int arg0) throws SQLException {
			// TODO Auto-generated method stub
			
		}

		public void setReadOnly(boolean arg0) throws SQLException {
			// TODO Auto-generated method stub
			
		}

		public Savepoint setSavepoint() throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public Savepoint setSavepoint(String arg0) throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		public void setTransactionIsolation(int arg0) throws SQLException {
			// TODO Auto-generated method stub
			
		}

		public void setTypeMap(Map<String, Class<?>> arg0) throws SQLException {
			// TODO Auto-generated method stub
			
		}
		
		
	}
}
