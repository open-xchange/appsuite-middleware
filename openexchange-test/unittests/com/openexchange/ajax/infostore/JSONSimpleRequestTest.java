package com.openexchange.ajax.infostore;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;

import com.openexchange.ajax.request.JSONSimpleRequest;
import com.openexchange.ajax.request.SimpleRequest;

public class JSONSimpleRequestTest extends TestCase{
	public void testGetParameter() throws Exception{
		String json = "{\"param1\" : \"value1\", \"param2\" : \"value2\"}";
		SimpleRequest req = new JSONSimpleRequest(new JSONObject(json));
		
		assertEquals("value1",req.getParameter("param1"));
		assertEquals("value2",req.getParameter("param2"));
		assertNull((req.getParameter("param3")));
	}
	
	public void testGetParameterValues() throws Exception{
		String json = "{\"param\" : \"value1,value2,value3,value4\"}";
		SimpleRequest req = new JSONSimpleRequest(new JSONObject(json));
		
		String[] values = req.getParameterValues("param");
		
		assertEquals(4,values.length);
		assertEquals("value1",values[0]);
		assertEquals("value2",values[1]);
		assertEquals("value3",values[2]);
		assertEquals("value4",values[3]);
		
	}
	
	public void testGetBody() throws Exception{
		String json = "{\"data\" : [1,2,3,4] }";
		SimpleRequest req = new JSONSimpleRequest(new JSONObject(json));
		
		JSONArray array = (JSONArray) req.getBody();
		
		assertEquals(4, array.length());
		for(int i = 1; i < 5; i++){
			assertEquals(i,array.getInt(i-1));
		}
	}
}
