package com.openexchange.groupware.infostore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.openexchange.groupware.Init;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.ContextStorage;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.database.impl.InfostoreQueryCatalog;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tx.AbstractActionTest;
import com.openexchange.groupware.tx.DBPoolProvider;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.tools.sql.DBUtils;

public abstract class AbstractInfostoreActionTest extends AbstractActionTest {

	private User user;
	private Context ctx;
	private List<DocumentMetadata> infoitems = new ArrayList<DocumentMetadata>();
	private List<DocumentMetadata> updatedInfoitems = new ArrayList<DocumentMetadata>();
	
	private InfostoreQueryCatalog queryCatalog;
	private DBProvider provider;
	private InfostoreFacade infostore;

	public void setUp() throws Exception {
		Init.startServer();
		ContextStorage.init();
		provider = new DBPoolProvider();
		queryCatalog = new InfostoreQueryCatalog();
		ctx = ContextStorage.getInstance().getContext(ContextStorage.getInstance().getContextId("defaultcontext"));
		user = UserStorage.getInstance(ctx).getUser(UserStorage.getInstance(ctx).getUserId("francisco"));
		
		initDocMeta();
	}

	public void tearDown() throws Exception {
		Init.stopServer();
	}
	
	protected User getUser() {
		return user;
	}

	protected Context getContext() {
		return ctx;
	}

	protected List<DocumentMetadata> getDocuments() {
		return infoitems;
	}
	
	protected List<DocumentMetadata> getUpdatedDocuments() {
		return updatedInfoitems;
	}

	protected InfostoreQueryCatalog getQueryCatalog() {
		return queryCatalog;
	}

	protected DBProvider getProvider() {
		return provider;
	}
	
	protected InfostoreFacade getInfostore(){
		return infostore;
	}
	
	protected UserConfiguration getUserConfiguration(){
		return null;
	}
	
	protected void assertNoResult(String sql, Object...args) throws TransactionException, SQLException {
		assertFalse(hasResult(sql, args));
	}

	protected void assertResult(String sql, Object...args) throws TransactionException, SQLException {
		assertTrue(hasResult(sql, args));
	}

	protected boolean hasResult(String sql, Object[] args) throws TransactionException, SQLException {
		Connection readCon = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			readCon = getProvider().getReadConnection(getContext());
			stmt = readCon.prepareStatement(sql);
			int i = 1;
			for(Object arg : args) {
				stmt.setObject(i++,arg);
			}
			rs = stmt.executeQuery();
			return rs.next();
		} finally {
			DBUtils.closeSQLStuff(rs, stmt);
			getProvider().releaseReadConnection(getContext(), readCon);
		}
	}
	
	private void initDocMeta() {
		DocumentMetadata m = new DocumentMetadataImpl();
		m.setCategories("cat1, cat2, cat3");
		m.setColorLabel(12);
		m.setCreatedBy(12);
		m.setCreationDate(new Date());
		m.setDescription("desc");
		m.setFileMD5Sum("3j4klhl");
		m.setFileMIMEType("text/plain");
		m.setFileName("test.txt");
		m.setFileSize(123332);
		m.setFolderId(12);
		m.setId(101024);
		m.setLastModified(new Date());
		m.setModifiedBy(23);
		m.setTitle("title");
		m.setURL("http://gnirz.com");
		m.setVersion(2);
		m.setVersionComment("vc");
		
		infoitems.add(m);
		
		m = new DocumentMetadataImpl(m);
		m.setId(102048);
		
		infoitems.add(m);
		
		m = new DocumentMetadataImpl(m);
		m.setId(104096);
		
		infoitems.add(m);
		
		m = new DocumentMetadataImpl(m);
		m.setId(108192);
		
		infoitems.add(m);
		
		m = new DocumentMetadataImpl(m);
		m.setId(101024);
		m.setColorLabel(42);
		m.setFileName("updated.txt");
		updatedInfoitems.add(m);
		
		m = new DocumentMetadataImpl(m);
		m.setId(102048);
		
		updatedInfoitems.add(m);
		
		m = new DocumentMetadataImpl(m);
		m.setId(104096);
		
		updatedInfoitems.add(m);
		
		m = new DocumentMetadataImpl(m);
		m.setId(108192);
		
		updatedInfoitems.add(m);
		
	}

}
