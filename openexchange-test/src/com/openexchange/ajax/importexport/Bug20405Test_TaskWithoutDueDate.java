package com.openexchange.ajax.importexport;

import java.util.TimeZone;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.ajax.task.ManagedTaskTest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;

public class Bug20405Test_TaskWithoutDueDate extends ManagedTaskTest {

	public Bug20405Test_TaskWithoutDueDate(String name) {
		super(name);
	}

	public String ical = "BEGIN:VCALENDAR\n" +
			"VERSION:2.0\n" +
			"PRODID:-//ABC Corporation//NONSGML My Product//EN\n" +
			"BEGIN:VTODO\n" +
			"DTSTAMP:20110919T120516Z\n" +
			"SUMMARY:2 Beginn heute\\, F\u00e4llig offen\n" +
			"CLASS:PRIVATE\n" +
			"LAST-MODIFIED:20110919T115459Z\n" +
			"STATUS:IN-PROCESS\n" +
			"CREATED:20110919T115459Z\n" +
			"DTSTART;VALUE=DATE:20110919\n" +
			"PRIORITY:5\n" +
			"TRANSP:OPAQUE\n" +
			"END:VTODO\n" +
			"END:VCALENDAR";

	public void testWhateverIsBroken() throws Exception{
		ICalImportRequest importRequest = new ICalImportRequest(folderID, ical);
		ICalImportResponse importResponse = getClient().execute(importRequest);
		//System.out.println(importResponse.getResponse());
		assertEquals(System.getProperty("line.separator")+importResponse.getResponse(), 1,importResponse.getImports().length);

		int id = Integer.parseInt(importResponse.getImports()[0].getObjectId());

		TimeZone tz = getClient().getValues().getTimeZone();
		GetResponse getResponse = getClient().execute(new GetRequest(folderID, id, false));
		assertFalse(getResponse.getTask(tz).containsEndDate());
	}
}
