package com.openexchange.json;

import java.io.StringWriter;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

public class JSONWriterTest extends TestCase{
	public void testEmptyObject(){
		StringWriter w = new StringWriter();
		JSONWriter json = new JSONWriter(w);
		
		try {
			json.object();
			json.endObject();
			assertEquals("{}", w.toString());
		} catch (JSONException e) {
			fail(e.getLocalizedMessage());
		}
	}
	
	public void testObjectWithScalars(){
		StringWriter w = new StringWriter();
		JSONWriter json = new JSONWriter(w);
		
		try {
			json.object();
			json.key("string");
			json.value("String");
			json.key("integer");
			json.value(23);
			json.key("float");
			json.value(3.14);
			json.key("boolean");
			json.value(true);
			json.endObject();
			JSONObject object = new JSONObject(w.toString());
			assertEquals("String", object.optString("string"));
			assertEquals(23, object.optLong("integer"));
			assertEquals(3.14, object.optDouble("float"));
			assertTrue(object.optBoolean("boolean"));
			
		} catch (JSONException e) {
			fail(e.getLocalizedMessage());
		}
	}
	
	public void testEmptyArray(){
		StringWriter w = new StringWriter();
		JSONWriter json = new JSONWriter(w);
		
		try {
			json.array();
			json.endArray();
			assertEquals("[]", w.toString());
		} catch (JSONException e) {
			fail(e.getLocalizedMessage());
		}
	}
	
	public void testArrayWithScalars(){
		StringWriter w = new StringWriter();
		JSONWriter json = new JSONWriter(w);
		
		try {
			json.array();
			json.value("string");
			json.value(23);
			json.value(3.14);
			json.value(true);
			json.endArray();
			JSONArray arr = new JSONArray(w.toString());
			assertEquals("string", arr.getString(0));
			assertEquals(23, arr.getLong(1));
			assertEquals(3.14, arr.getDouble(2));
			assertTrue(arr.getBoolean(3));
			
		} catch (JSONException e) {
			fail(e.getLocalizedMessage());
		}
	}
	
	public void testObjectWithObjects(){
		StringWriter w = new StringWriter();
		JSONWriter json = new JSONWriter(w);
		
		try {
			json.object();
			json.key("object1");
			json.object();
			json.key("first");
			json.value("Tester");
			json.key("last");
			json.value("von Testingen");
			json.endObject();
			json.key("object2");
			json.object();
			json.key("first");
			json.value("Testerine");
			json.key("last");
			json.value("von Testeringen");
			json.endObject();
			json.endObject();
			JSONObject object = new JSONObject(w.toString());
			JSONObject object1 = object.getJSONObject("object1");
			JSONObject object2 = object.getJSONObject("object2");
			
			assertEquals("Tester", object1.optString("first"));
			assertEquals("von Testingen", object1.optString("last"));
			assertEquals("Testerine", object2.optString("first"));
			assertEquals("von Testeringen", object2.optString("last"));
			
			
		} catch (JSONException e) {
			fail(e.getLocalizedMessage());
		}
	}
	
	public void testObjectWithArrays(){
		StringWriter w = new StringWriter();
		JSONWriter json = new JSONWriter(w);
		
		try {
			json.object();
			json.key("array1");
			json.array();
			json.value("Tester");
			json.value("von Testingen");
			json.endArray();
			json.key("array2");
			json.array();
			json.value("Testerine");
			json.value("von Testeringen");
			json.endArray();
			json.endObject();
			JSONObject object = new JSONObject(w.toString());
			JSONArray arr1 = object.getJSONArray("array1");
			JSONArray arr2 = object.getJSONArray("array2");
			
			assertEquals("Tester", arr1.getString(0));
			assertEquals("von Testingen", arr1.getString(1));
			assertEquals("Testerine", arr2.getString(0));
			assertEquals("von Testeringen", arr2.getString(1));
			
			
		} catch (JSONException e) {
			fail(e.getLocalizedMessage());
		}
	}
	
	public void testArrayWithObjects(){
		StringWriter w = new StringWriter();
		JSONWriter json = new JSONWriter(w);
		
		try {
			json.array();
			json.object();
			json.key("first");
			json.value("Tester");
			json.key("last");
			json.value("von Testingen");
			json.endObject();
			json.object();
			json.key("first");
			json.value("Testerine");
			json.key("last");
			json.value("von Testeringen");
			json.endObject();
			json.endArray();
			JSONArray arr = new JSONArray(w.toString());
			
			JSONObject object1 = arr.getJSONObject(0);
			JSONObject object2 = arr.getJSONObject(1);
			
			assertEquals("Tester", object1.optString("first"));
			assertEquals("von Testingen", object1.optString("last"));
			assertEquals("Testerine", object2.optString("first"));
			assertEquals("von Testeringen", object2.optString("last"));
			
		} catch (JSONException e) {
			fail(e.getLocalizedMessage());
		}
	}
	
	public void testComplex(){
		
	}
	
	public void testInvalid(){
		
	}
}
