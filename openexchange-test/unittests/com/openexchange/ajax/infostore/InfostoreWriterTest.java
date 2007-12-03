package com.openexchange.ajax.infostore;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.openexchange.ajax.writer.InfostoreWriter;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.tools.iterator.ArrayIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;

public class InfostoreWriterTest extends TestCase {
	
	private static final List<DocumentMetadata> DUMMY_DATA = new ArrayList<DocumentMetadata>();
	
	private static final Date now = new Date(1153481105872l);
	
	
	static {
		// Create some Dummy Values
		
		DocumentMetadataImpl dm = new DocumentMetadataImpl();
		dm.setFolderId(22);
		dm.setModifiedBy(1);
		
		dm = new DocumentMetadataImpl(dm);
		dm.setCreationDate(now);
		dm.setCreatedBy(1);
		dm.setDescription("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.");
		dm.setLastModified(now);
		dm.setTitle("Google");
		dm.setURL("http://www.google.de");
		dm.setVersion(0);
		dm.setId(1);
		DUMMY_DATA.add(dm);
		
		dm = new DocumentMetadataImpl(dm);
		dm.setURL(null);
		dm.setCreationDate(new Date(now.getTime()-24*3600000));
		dm.setLastModified(new Date(now.getTime()-18*3600000));
		dm.setDescription("Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.");
		dm.setTitle("Knowledge");
		dm.setId(2);
		DUMMY_DATA.add(dm);
		
		dm = new DocumentMetadataImpl(dm);
		dm.setURL("http://www.nice-files.de");
		dm.setVersion(3);
		dm.setFileMIMEType("text/html");
		dm.setFileName("gnatzel.html");
		dm.setTitle("File Attached");
		dm.setFileSize(4*1024);
		dm.setCreationDate(new Date(now.getTime()-48*3600000));
		dm.setLastModified(new Date(now.getTime()-6*3600000));
		dm.setId(3);
		DUMMY_DATA.add(dm);
	
		
	}
	
	public void testWriteList() throws Exception{
		StringWriter results = new StringWriter();
		InfostoreWriter w = new InfostoreWriter(new JSONWriter(new PrintWriter(results)));
		
		w.writeMetadata(new SearchIteratorAdapter(DUMMY_DATA.iterator()), new Metadata[]{Metadata.URL_LITERAL, Metadata.TITLE_LITERAL, Metadata.CREATED_BY_LITERAL}, TimeZone.getTimeZone("utc"));
		
		JSONArray listOfArrays = new JSONArray(results.toString());
		
		assertEquals(3, listOfArrays.length());
		
		Set<String> urls = new HashSet<String>(Arrays.asList("http://www.nice-files.de", "http://www.google.de"));
		Set<String> titles = new HashSet<String>(Arrays.asList("Google", "Knowledge", "File Attached"));
		String createdBy = "1";
		for(int i = 0 ; i < 3; i++) {
			JSONArray result = listOfArrays.getJSONArray(i);
			assertEquals(3,result.length());
			assertEquals(createdBy,result.getString(2));
			assertTrue(titles.remove(result.getString(1)));
			urls.remove(result.getString(0));
		}
		
		assertTrue(urls.isEmpty());
		assertTrue(titles.isEmpty());
	}
	
	public void testCategories() throws Exception{
		StringWriter results = new StringWriter();
		InfostoreWriter w = new InfostoreWriter(new JSONWriter(new PrintWriter(results)));
		
		DocumentMetadata m = new DocumentMetadataImpl();
		m.setCategories("cat1, cat2, cat3");
		
		w.writeMetadata(new ArrayIterator(new DocumentMetadata[]{m}),new Metadata[]{Metadata.CATEGORIES_LITERAL},TimeZone.getTimeZone("utc"));
		JSONArray listOfArrays = new JSONArray(results.toString());
		JSONArray metadata1 = listOfArrays.getJSONArray(0);
		JSONArray categories = metadata1.getJSONArray(0);
		
		assertEquals("cat1",categories.get(0));
		assertEquals("cat2",categories.get(1));
		assertEquals("cat3",categories.get(2));
		
		results = new StringWriter();
		w = new InfostoreWriter(new JSONWriter(new PrintWriter(results)));
		w.write(m,TimeZone.getTimeZone("utc"));
		
		
		JSONObject o = new JSONObject(results.toString());
		categories = o.getJSONArray("categories");
		
		assertEquals("cat1",categories.get(0));
		assertEquals("cat2",categories.get(1));
		assertEquals("cat3",categories.get(2));
		
		
		m.setCategories(null);
		
		results = new StringWriter();
		w = new InfostoreWriter(new JSONWriter(new PrintWriter(results)));
		w.writeMetadata(new ArrayIterator(new DocumentMetadata[]{m}),new Metadata[]{Metadata.CATEGORIES_LITERAL},TimeZone.getTimeZone("utc"));
		
		listOfArrays = new JSONArray(results.toString());
		metadata1 = listOfArrays.getJSONArray(0);
		categories = metadata1.getJSONArray(0);
		
		assertEquals(0,categories.length());
		
		results = new StringWriter();
		w = new InfostoreWriter(new JSONWriter(new PrintWriter(results)));
		w.write(m,TimeZone.getTimeZone("utc"));
		
		o = new JSONObject(results.toString());
		categories = o.getJSONArray("categories");
		assertEquals(0,categories.length());
		
		
	}
	
	public void testWriteObject() throws Exception{
		DocumentMetadataImpl dm = new DocumentMetadataImpl();
		dm.setFolderId(22);
		dm.setModifiedBy(1);
		
		dm = new DocumentMetadataImpl(dm);
		dm.setCreationDate(now);
		dm.setCreatedBy(1);
		dm.setDescription("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.");
		dm.setLastModified(now);
		dm.setTitle("Google");
		dm.setURL("http://www.google.de");
		dm.setVersion(0);
		dm.setId(1);
		
		StringWriter result = new StringWriter();
		InfostoreWriter w = new InfostoreWriter(new JSONWriter(new PrintWriter(result)));
		
		w.write(dm,TimeZone.getTimeZone("utc"));
		
		JSONObject o = new JSONObject(result.toString());
		
		assertEquals(dm.getDescription(), o.getString("description"));
		assertEquals(dm.getTitle(), o.getString("title"));
		assertEquals(dm.getURL(), o.getString("url"));
	}
	
	public void testLocked() throws Exception{
		DocumentMetadataImpl dm = new DocumentMetadataImpl();
		dm.setFolderId(22);
		dm.setModifiedBy(1);
		
		dm = new DocumentMetadataImpl(dm);
		dm.setCreationDate(now);
		dm.setCreatedBy(1);
		dm.setDescription("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.");
		dm.setLastModified(now);
		dm.setTitle("Google");
		dm.setURL("http://www.google.de");
		dm.setVersion(0);
		dm.setId(1);
		dm.setLockedUntil(new Date(System.currentTimeMillis()-1000));
		
		StringWriter result = new StringWriter();
		InfostoreWriter w = new InfostoreWriter(new JSONWriter(new PrintWriter(result)));
		
		w.write(dm,TimeZone.getTimeZone("utc"));
		
		JSONObject o = new JSONObject(result.toString());
		
		assertEquals(0,o.getLong(Metadata.LOCKED_UNTIL_LITERAL.getName()));
		
		long later = System.currentTimeMillis()+1000;
		
		dm.setLockedUntil(new Date(later));
		
		result = new StringWriter();
		w = new InfostoreWriter(new JSONWriter(new PrintWriter(result)));
		
		w.write(dm,TimeZone.getTimeZone("utc"));
		
		o = new JSONObject(result.toString());
		
		assertEquals(later,o.getLong(Metadata.LOCKED_UNTIL_LITERAL.getName()));
		
	}
	
	public void testTimeZone() throws Exception {
		DocumentMetadataImpl dm = new DocumentMetadataImpl();
		
		dm.setLastModified(new Date(230023));
		
		StringWriter result = new StringWriter();
		InfostoreWriter w = new InfostoreWriter(new JSONWriter(new PrintWriter(result)));
		
		w.write(dm,TimeZone.getTimeZone("Europe/Berlin"));
		
		JSONObject o = new JSONObject(result.toString());
		
		assertEquals(3830023,o.getLong(Metadata.LAST_MODIFIED_LITERAL.getName()));
		
	}
	
}
