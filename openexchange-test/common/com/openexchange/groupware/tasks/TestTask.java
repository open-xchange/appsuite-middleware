package com.openexchange.groupware.tasks;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.openexchange.groupware.calendar.TimeTools;
import com.openexchange.groupware.tasks.Mapping.Mapper;

public class TestTask extends Task {
	protected TimeZone timezone;
	
	public static final int DATES = 1;
	public static final int RECURRENCES = 2;
	
	public TimeZone getTimezone() {
		return timezone;
	}

	public void setTimezone(TimeZone timezone) {
		this.timezone = timezone;
	}
	
	public Calendar getCalendar(){
		return TimeTools.createCalendar(timezone);
	}
	
	public Calendar getCalendar(Date date){
		Calendar calendar = getCalendar();
		calendar.setTime(date);
		return calendar;
	}

	public TestTask(){
		super();
	}

	public TestTask checkConsistencyOf(int... switches){
		for(int currentSwitch : switches)
		switch (currentSwitch) {
		case DATES:
			//appointments without start dates do not work
			if( ! containsStartDate() ) 
				setStartDate(new Date());
			//appointments without end dates do not work
			if( containsStartDate() && ! containsEndDate() )
				setEndDate(this.getStartDate());
			//appointments with end dates before start dates do not work
			if( containsStartDate() && containsEndDate() )
				if( getEndDate().compareTo(getStartDate()) < 0)
					setEndDate( getStartDate() );
			break;
			
		case RECURRENCES:
			if( containsRecurrenceType() 
			|| containsDays() 
			|| containsDayInMonth() 
			|| containsInterval()
			){
				//if there is a recurrence, but no interval is set, set it to 1
				if( ! containsInterval() )
					setInterval(1);
				//if there is no start date, set it to the start of the recurrence

			}
		default:
			break;
		}
		return this;
	}

	//set the day
	protected Date dateAtDayOfTheMonth(Date date, int dayOfTheMonth){
		Calendar time = getCalendar();
		time.setTime(date);
		time.set(Calendar.DAY_OF_MONTH, dayOfTheMonth);
		return time.getTime();
	}
	
	protected Date shiftDateByDays(Date date, int daysToShift){
		Calendar time = getCalendar();
		time.setTime(date);
		time.add(Calendar.DAY_OF_MONTH, daysToShift);

		return time.getTime();
	}

	public TestTask startsToday(){
		this.setStartDate(new Date());
		return this;
	}

	/**
	 * Moves the date one day into the future.
	 * Can be used more than once.
	 * @return
	 */
	public TestTask startsTheFollowingDay(){
		if(! containsStartDate() )
			setStartDate( new Date() );
		setStartDate( shiftDateByDays(getStartDate(), 1));
		return this;
	}
	
	public TestTask endsTheFollowingDay(){
		if(! containsEndDate() )
			setEndDate( new Date() );
		setEndDate( shiftDateByDays(getEndDate(), 1));
		return this;
	}
	
	public TestTask startsTheDayBefore(){
		if(! containsStartDate() )
			setStartDate( new Date() );
		setStartDate( shiftDateByDays(getStartDate(), -1));
		return this;
	}
	
	public TestTask endsTheDayBefore(){
		if(! containsEndDate() )
			setEndDate( new Date() );
		setEndDate( shiftDateByDays(getEndDate(), -1));
		return this;
	}
	
	protected Date setDayToToday(Date date){
		Calendar now = getCalendar();

		Calendar newDate = getCalendar();
		newDate.setTime(date);
		
		newDate.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
		newDate.set(Calendar.YEAR, now.get(Calendar.YEAR));
		newDate.set(Calendar.MONTH, now.get(Calendar.MONTH));

		return newDate.getTime();
	}
	/**
	 * sets the task to start tomorrow. The time of day is
	 * either that set before or, if none was set, it is the 
	 * current time of day.
	 * @return
	 */
	public TestTask startsTomorrow(){
		if(! containsStartDate() )
			setStartDate( new Date() );
		setStartDate( setDayToToday( getStartDate() ) );
		return startsTheFollowingDay();
	}
	
	public TestTask endsTomorrow(){
		if(! containsEndDate() )
			setEndDate( new Date() );
		setEndDate( setDayToToday( getEndDate() ) );
		return endsTheFollowingDay();
	}
	
	public TestTask startsYesterday(){
		if(! containsStartDate() )
			setStartDate( new Date() );
		setStartDate( setDayToToday( getStartDate() ) );
		return startsTheDayBefore();
	}
	
	public TestTask endsYesterday(){
		if(! containsEndDate() )
			setEndDate( new Date() );
		setEndDate( setDayToToday( getEndDate() ) );
		return endsTheDayBefore();
	}

	//set the hour
	protected Date dateAtHours(Date date, int hourOfTheDay){
		Calendar time = getCalendar();
		time.setTime(date);
		time.set(Calendar.HOUR_OF_DAY, hourOfTheDay);
		time.set(Calendar.MINUTE, 00);
		return time.getTime();
	}
	public TestTask startsAt(int hourOfTheDay){
		if( ! containsStartDate() )
			setStartDate(new Date());
		setStartDate( dateAtHours(getStartDate(), hourOfTheDay ) );
		return this;
	}
	
	public TestTask endsAt(int hourOfTheDay){
		if( ! containsEndDate() )
			setEndDate( new Date() );
		setEndDate(dateAtHours(getEndDate(), hourOfTheDay) );
		return this;
	}
	
	public TestTask startsAtNoon(){
		return startsAt(12);
	}

	public TestTask startsInTheMorning(){
		return startsAt(6);
	}

	public TestTask startsInTheEvening(){
		return startsAt(18);
	}
	
	public TestTask endsInTheMorning(){
		return endsAt(6);
	}

	public TestTask endsAtNoon(){
		return endsAt(12);
	}
	
	public TestTask endInTheEvening(){
		return endsAt(18);
	}
	

	public TestTask forOneHour(){
		return this;
	}
	
	//set recurrence
	public TestTask everyDay(){
		return this;
	}
	
	
	public TestTask everyWeek(){
		if(! containsStartDate()){
			setStartDate(new Date());
		}
		
		Calendar cal = getCalendar( getStartDate() );
		int dayOfTheWeek = cal.get( Calendar.DAY_OF_WEEK );
		
		return everyWeekOn(dayOfTheWeek);
	}
	
	public TestTask everyWeekOn(int... days){
		//TODO should really start on next day in dayOfTheWeek
		if(! containsStartDate()){
			setStartDate(new Date());
		}
		int daysOfTheWeek = 0;
		for(int day: days){
			daysOfTheWeek |= day;
		}
		setRecurrenceType(WEEKLY);
		setInterval(1);
		setDays( daysOfTheWeek );
		return this;
	}
	
	public TestTask onDay(int day){
		setDayInMonth(day);
		return this;
	}
	
	public TestTask onWeekDays(int days){
		setDays(days);
		return this;
	}
	
	public TestTask inMonth(int month){
		setMonth(month);
		return this;
	}

	public TestTask everyMonth(){
		setRecurrenceType(MONTHLY);
		return this;
	}
	
	public TestTask everyMonthOnDay(int dayOfMonth){
		//this is ugly but compatible to the HTTP_API description as of 2008-11-20
		return everyMonthOnNthWeekday(dayOfMonth, MONDAY | TUESDAY | WEDNESDAY | THURSDAY | FRIDAY | SATURDAY | SUNDAY);
	}
	
	public TestTask everyMonthOnNthWeekday(int n, int weekDay){
		everyMonth();
		onDay(n);
		onWeekDays(weekDay);
		if(! containsInterval()){
			setInterval(1);
		}
		return this;
	}
	
	public TestTask everyOtherMonth(){
		setRecurrenceType(MONTHLY);
		setInterval(2);
		return this;
	}
	public TestTask everyYear(){
		return this;
	}

	public TestTask makeRelatedTo(TestTask originalTask) {
		this.setLastModified(originalTask.getLastModified());
		this.setObjectID(originalTask.getObjectID());
		this.setParentFolderID(originalTask.getParentFolderID());
		return this;
	}
	public TestTask clone(){
		TestTask newTask = new TestTask();
		//copy all fields of Task
		for(int field : Task.ALL_COLUMNS){
			Mapper mapper = Mapping.getMapping(field);
			if(mapper != null &&  mapper.isSet(this) )
					mapper.set(newTask, mapper.get(this) );
		}
		//copy additional TestTask fields
		newTask.setTimezone(getTimezone());
		//TODO: deep-copy participant lists

		return newTask;
	}

}
