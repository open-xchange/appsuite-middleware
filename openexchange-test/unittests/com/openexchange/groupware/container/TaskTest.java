
package com.openexchange.groupware.container;

import static com.openexchange.groupware.tasks.Task.*;
import java.math.BigDecimal;
import java.util.Date;
import com.openexchange.groupware.tasks.Task;


public class TaskTest extends CalendarObjectTest {

    @Override
    public void testFindDifferingFields() {
        Task dataObject = getTask();
        Task otherDataObject = getTask();

        otherDataObject.setActualCosts(new BigDecimal("1.2"));
        assertDifferences(dataObject, otherDataObject, Task.ACTUAL_COSTS);

        otherDataObject.setActualDuration(12L);
        assertDifferences(dataObject, otherDataObject, Task.ACTUAL_COSTS, Task.ACTUAL_DURATION);


        otherDataObject.setAlarm(new Date(23));
        assertDifferences(dataObject, otherDataObject, Task.ACTUAL_COSTS, Task.ACTUAL_DURATION, Task.ALARM);

        otherDataObject.setBillingInformation("Blupp");
        assertDifferences(
            dataObject,
            otherDataObject,
            Task.ACTUAL_COSTS,
            Task.ACTUAL_DURATION,
            Task.ALARM,
            Task.BILLING_INFORMATION);

        otherDataObject.setCompanies("Blupp");
        assertDifferences(
            dataObject,
            otherDataObject,
            Task.ACTUAL_COSTS,
            Task.ACTUAL_DURATION,
            Task.ALARM,
            Task.BILLING_INFORMATION,
            Task.COMPANIES);

        otherDataObject.setCurrency("Blupp");
        assertDifferences(
            dataObject,
            otherDataObject,
            Task.ACTUAL_COSTS,
            Task.ACTUAL_DURATION,
            Task.ALARM,
            Task.BILLING_INFORMATION,
            Task.COMPANIES,
            Task.CURRENCY);

        otherDataObject.setDateCompleted(new Date(23));
        assertDifferences(
            dataObject,
            otherDataObject,
            Task.ACTUAL_COSTS,
            Task.ACTUAL_DURATION,
            Task.ALARM,
            Task.BILLING_INFORMATION,
            Task.COMPANIES,
            Task.CURRENCY,
            Task.DATE_COMPLETED);

        otherDataObject.setPercentComplete(12);
        assertDifferences(
            dataObject,
            otherDataObject,
            Task.ACTUAL_COSTS,
            Task.ACTUAL_DURATION,
            Task.ALARM,
            Task.BILLING_INFORMATION,
            Task.COMPANIES,
            Task.CURRENCY,
            Task.DATE_COMPLETED,
            Task.PERCENT_COMPLETED);

        otherDataObject.setPriority(12);
        assertDifferences(
            dataObject,
            otherDataObject,
            Task.ACTUAL_COSTS,
            Task.ACTUAL_DURATION,
            Task.ALARM,
            Task.BILLING_INFORMATION,
            Task.COMPANIES,
            Task.CURRENCY,
            Task.DATE_COMPLETED,
            Task.PERCENT_COMPLETED,
            Task.PRIORITY);

        otherDataObject.setProjectID(12);
        assertDifferences(
            dataObject,
            otherDataObject,
            Task.ACTUAL_COSTS,
            Task.ACTUAL_DURATION,
            Task.ALARM,
            Task.BILLING_INFORMATION,
            Task.COMPANIES,
            Task.CURRENCY,
            Task.DATE_COMPLETED,
            Task.PERCENT_COMPLETED,
            Task.PRIORITY,
            Task.PROJECT_ID);

        otherDataObject.setStatus(12);
        assertDifferences(
            dataObject,
            otherDataObject,
            Task.ACTUAL_COSTS,
            Task.ACTUAL_DURATION,
            Task.ALARM,
            Task.BILLING_INFORMATION,
            Task.COMPANIES,
            Task.CURRENCY,
            Task.DATE_COMPLETED,
            Task.PERCENT_COMPLETED,
            Task.PRIORITY,
            Task.PROJECT_ID,
            Task.STATUS);

        otherDataObject.setTargetCosts(new BigDecimal("1.2"));
        assertDifferences(
            dataObject,
            otherDataObject,
            Task.ACTUAL_COSTS,
            Task.ACTUAL_DURATION,
            Task.ALARM,
            Task.BILLING_INFORMATION,
            Task.COMPANIES,
            Task.CURRENCY,
            Task.DATE_COMPLETED,
            Task.PERCENT_COMPLETED,
            Task.PRIORITY,
            Task.PROJECT_ID,
            Task.STATUS,
            Task.TARGET_COSTS);

        otherDataObject.setTargetDuration(12L);
        assertDifferences(
            dataObject,
            otherDataObject,
            Task.ACTUAL_COSTS,
            Task.ACTUAL_DURATION,
            Task.ALARM,
            Task.BILLING_INFORMATION,
            Task.COMPANIES,
            Task.CURRENCY,
            Task.DATE_COMPLETED,
            Task.PERCENT_COMPLETED,
            Task.PRIORITY,
            Task.PROJECT_ID,
            Task.STATUS,
            Task.TARGET_COSTS,
            Task.TARGET_DURATION);

        otherDataObject.setTripMeter("Blupp");
        assertDifferences(
            dataObject,
            otherDataObject,
            Task.ACTUAL_COSTS,
            Task.ACTUAL_DURATION,
            Task.ALARM,
            Task.BILLING_INFORMATION,
            Task.COMPANIES,
            Task.CURRENCY,
            Task.DATE_COMPLETED,
            Task.PERCENT_COMPLETED,
            Task.PRIORITY,
            Task.PROJECT_ID,
            Task.STATUS,
            Task.TARGET_COSTS,
            Task.TARGET_DURATION,
            Task.TRIP_METER);

    }

    @Override
    public void testAttrAccessors() {
        Task object = new Task();

        // STATUS
        assertFalse(object.contains(STATUS));
        assertFalse(object.containsStatus());

        object.setStatus(-12);
        assertTrue(object.contains(STATUS));
        assertTrue(object.containsStatus());
        assertEquals(-12, object.get(STATUS));

        object.set(STATUS,12);
        assertEquals(12, object.getStatus());

        object.remove(STATUS);
        assertFalse(object.contains(STATUS));
        assertFalse(object.containsStatus());



        // TARGET_DURATION
        assertFalse(object.contains(TARGET_DURATION));
        assertFalse(object.containsTargetDuration());

        object.setTargetDuration(-12L);
        assertTrue(object.contains(TARGET_DURATION));
        assertTrue(object.containsTargetDuration());
        assertEquals(-12L, object.get(TARGET_DURATION));

        object.set(TARGET_DURATION,12L);
        assertEquals(12L, (Object) object.getTargetDuration());

        object.remove(TARGET_DURATION);
        assertFalse(object.contains(TARGET_DURATION));
        assertFalse(object.containsTargetDuration());



        // DATE_COMPLETED
        assertFalse(object.contains(DATE_COMPLETED));
        assertFalse(object.containsDateCompleted());

        object.setDateCompleted(new Date(42));
        assertTrue(object.contains(DATE_COMPLETED));
        assertTrue(object.containsDateCompleted());
        assertEquals(new Date(42), object.get(DATE_COMPLETED));

        object.set(DATE_COMPLETED,new Date(23));
        assertEquals(new Date(23), object.getDateCompleted());

        object.remove(DATE_COMPLETED);
        assertFalse(object.contains(DATE_COMPLETED));
        assertFalse(object.containsDateCompleted());



        // TARGET_COSTS
        assertFalse(object.contains(TARGET_COSTS));
        assertFalse(object.containsTargetCosts());

        object.setTargetCosts(new BigDecimal("-1.2"));
        assertTrue(object.contains(TARGET_COSTS));
        assertTrue(object.containsTargetCosts());
        assertEquals(new BigDecimal("-1.2"), object.get(TARGET_COSTS));

        object.set(TARGET_COSTS, new BigDecimal("1.2"));
        assertEquals(new BigDecimal("1.2"), object.getTargetCosts());

        object.remove(TARGET_COSTS);
        assertFalse(object.contains(TARGET_COSTS));
        assertFalse(object.containsTargetCosts());



        // PRIORITY
        assertFalse(object.contains(PRIORITY));
        assertFalse(object.containsPriority());

        object.setPriority(-12);
        assertTrue(object.contains(PRIORITY));
        assertTrue(object.containsPriority());
        assertEquals(-12, object.get(PRIORITY));

        object.set(PRIORITY,12);
        assertEquals(12, object.getPriority());

        object.remove(PRIORITY);
        assertFalse(object.contains(PRIORITY));
        assertFalse(object.containsPriority());



        // BILLING_INFORMATION
        assertFalse(object.contains(BILLING_INFORMATION));
        assertFalse(object.containsBillingInformation());

        object.setBillingInformation("Bla");
        assertTrue(object.contains(BILLING_INFORMATION));
        assertTrue(object.containsBillingInformation());
        assertEquals("Bla", object.get(BILLING_INFORMATION));

        object.set(BILLING_INFORMATION,"Blupp");
        assertEquals("Blupp", object.getBillingInformation());

        object.remove(BILLING_INFORMATION);
        assertFalse(object.contains(BILLING_INFORMATION));
        assertFalse(object.containsBillingInformation());



        // ALARM
        assertFalse(object.contains(ALARM));
        assertFalse(object.containsAlarm());

        object.setAlarm(new Date(42));
        assertTrue(object.contains(ALARM));
        assertTrue(object.containsAlarm());
        assertEquals(new Date(42), object.get(ALARM));

        object.set(ALARM,new Date(23));
        assertEquals(new Date(23), object.getAlarm());

        object.remove(ALARM);
        assertFalse(object.contains(ALARM));
        assertFalse(object.containsAlarm());



        // PERCENT_COMPLETED
        assertFalse(object.contains(PERCENT_COMPLETED));
        assertFalse(object.containsPercentComplete());

        object.setPercentComplete(-12);
        assertTrue(object.contains(PERCENT_COMPLETED));
        assertTrue(object.containsPercentComplete());
        assertEquals(-12, object.get(PERCENT_COMPLETED));

        object.set(PERCENT_COMPLETED,12);
        assertEquals(12, object.getPercentComplete());

        object.remove(PERCENT_COMPLETED);
        assertFalse(object.contains(PERCENT_COMPLETED));
        assertFalse(object.containsPercentComplete());



        // COMPANIES
        assertFalse(object.contains(COMPANIES));
        assertFalse(object.containsCompanies());

        object.setCompanies("Bla");
        assertTrue(object.contains(COMPANIES));
        assertTrue(object.containsCompanies());
        assertEquals("Bla", object.get(COMPANIES));

        object.set(COMPANIES,"Blupp");
        assertEquals("Blupp", object.getCompanies());

        object.remove(COMPANIES);
        assertFalse(object.contains(COMPANIES));
        assertFalse(object.containsCompanies());



        // CURRENCY
        assertFalse(object.contains(CURRENCY));
        assertFalse(object.containsCurrency());

        object.setCurrency("Bla");
        assertTrue(object.contains(CURRENCY));
        assertTrue(object.containsCurrency());
        assertEquals("Bla", object.get(CURRENCY));

        object.set(CURRENCY,"Blupp");
        assertEquals("Blupp", object.getCurrency());

        object.remove(CURRENCY);
        assertFalse(object.contains(CURRENCY));
        assertFalse(object.containsCurrency());



        // ACTUAL_COSTS
        assertFalse(object.contains(ACTUAL_COSTS));
        assertFalse(object.containsActualCosts());

        object.setActualCosts(new BigDecimal("-1.2"));
        assertTrue(object.contains(ACTUAL_COSTS));
        assertTrue(object.containsActualCosts());
        assertEquals(new BigDecimal("-1.2"), object.get(ACTUAL_COSTS));

        object.set(ACTUAL_COSTS, new BigDecimal("1.2"));
        assertEquals(new BigDecimal("1.2"), object.getActualCosts());

        object.remove(ACTUAL_COSTS);
        assertFalse(object.contains(ACTUAL_COSTS));
        assertFalse(object.containsActualCosts());



        // PROJECT_ID
        assertFalse(object.contains(PROJECT_ID));
        assertFalse(object.containsProjectID());

        object.setProjectID(-12);
        assertTrue(object.contains(PROJECT_ID));
        assertTrue(object.containsProjectID());
        assertEquals(-12, object.get(PROJECT_ID));

        object.set(PROJECT_ID,12);
        assertEquals(12, object.getProjectID());

        object.remove(PROJECT_ID);
        assertFalse(object.contains(PROJECT_ID));
        assertFalse(object.containsProjectID());



        // TRIP_METER
        assertFalse(object.contains(TRIP_METER));
        assertFalse(object.containsTripMeter());

        object.setTripMeter("Bla");
        assertTrue(object.contains(TRIP_METER));
        assertTrue(object.containsTripMeter());
        assertEquals("Bla", object.get(TRIP_METER));

        object.set(TRIP_METER,"Blupp");
        assertEquals("Blupp", object.getTripMeter());

        object.remove(TRIP_METER);
        assertFalse(object.contains(TRIP_METER));
        assertFalse(object.containsTripMeter());



        // ACTUAL_DURATION
        assertFalse(object.contains(ACTUAL_DURATION));
        assertFalse(object.containsActualDuration());

        object.setActualDuration(-12L);
        assertTrue(object.contains(ACTUAL_DURATION));
        assertTrue(object.containsActualDuration());
        assertEquals(-12L, object.get(ACTUAL_DURATION));

        object.set(ACTUAL_DURATION,12L);
        assertEquals(12L, (Object) object.getActualDuration());

        object.remove(ACTUAL_DURATION);
        assertFalse(object.contains(ACTUAL_DURATION));
        assertFalse(object.containsActualDuration());

    }


    public Task getTask() {
        Task task = new Task();

        fillTask(task);

        return task;
    }

    public void fillTask(Task object) {
        super.fillCalendarObject(object);

        object.setActualCosts(new BigDecimal("-1.2"));

        object.setActualDuration(-12L);

        object.setAfterComplete(new Date(42));

        object.setAlarm(new Date(42));

        object.setBillingInformation("Bla");

        object.setCompanies("Bla");

        object.setCurrency("Bla");

        object.setDateCompleted(new Date(42));

        object.setPercentComplete(-12);

        object.setPriority(-12);

        object.setProjectID(-12);

        object.setStatus(-12);

        object.setTargetCosts(new BigDecimal("-1.2"));

        object.setTargetDuration(-12L);

        object.setTripMeter("Bla");
    }
}
