package com.openexchange.webdav.xml.appointment;

import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.webdav.xml.AppointmentTest;
import java.util.Date;

public class ListTest extends AppointmentTest {
	
	public void testPropFindWithModified() throws Exception {
		Date modified = new Date();
		
		AppointmentObject appointmentObj = createAppointmentObject("testPropFindWithModified");
		int objectId1 = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		int objectId2 = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		
		AppointmentObject[] appointmentArray = listAppointment(webCon, appointmentFolderId, modified, "NEW_AND_MODIFIED", PROTOCOL + hostName, login, password);
		
		assertTrue("check response", appointmentArray.length >= 2);
		
		int[][] objectIdAndFolderId = { {objectId1, appointmentFolderId }, { objectId2, appointmentFolderId } };
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password );

	}
	
	public void testPropFindWithDelete() throws Exception {
		Date modified = new Date();
		
		AppointmentObject appointmentObj = createAppointmentObject("testPropFindWithDelete");
		int objectId1 = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		int objectId2 = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		
		int[][] objectIdAndFolderId = { { objectId1, appointmentFolderId }, { objectId2, appointmentFolderId } };
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password);
		
		AppointmentObject[] appointmentArray = listAppointment(webCon, appointmentFolderId, modified, "DELETED", PROTOCOL + hostName, login, password);

		assertTrue("wrong response array length (length=" + appointmentArray.length + ")", appointmentArray.length >= 2);
	}
	
	public void testPropFindWithObjectId() throws Exception {
		AppointmentObject appointmentObj = createAppointmentObject("testPropFindWithObjectId");
		int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		
		AppointmentObject loadAppointment = loadAppointment(webCon, objectId, appointmentFolderId, PROTOCOL + hostName, login, password);
		
		int[][] objectIdAndFolderId = { { objectId ,appointmentFolderId } };
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password );
	}
	
	public void testListWithAllFields() throws Exception {
		Date modified = new Date();
		
		AppointmentObject appointmentObj = new AppointmentObject();
		appointmentObj.setTitle("testListWithAllFields");
		appointmentObj.setStartDate(startTime);
		appointmentObj.setEndDate(endTime);
		appointmentObj.setLocation("Location");
		appointmentObj.setShownAs(AppointmentObject.FREE);
		appointmentObj.setParentFolderID(appointmentFolderId);
		appointmentObj.setPrivateFlag(true);
		appointmentObj.setLabel(2);
		appointmentObj.setNote("note");
		appointmentObj.setCategories("testcat1,testcat2,testcat3");
		
		int objectId = insertAppointment(webCon, appointmentObj, PROTOCOL + hostName, login, password);
		
		AppointmentObject[] appointmentArray = listAppointment(webCon, appointmentFolderId, modified, "NEW_AND_MODIFIED", PROTOCOL + hostName, login, password);
		
		assertEquals("wrong response array length", 1, appointmentArray.length);
		
		AppointmentObject loadAppointment = appointmentArray[0];
		
		appointmentObj.setObjectID(objectId);
		compareObject(appointmentObj, loadAppointment);
		
		int[][] objectIdAndFolderId = { {objectId, appointmentFolderId } };
		deleteAppointment(webCon, objectIdAndFolderId, PROTOCOL + hostName, login, password );
	}
}

