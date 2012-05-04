package com.openexchange.calendar.itip.generators.changes.generators;

import java.util.Arrays;

import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.openexchange.ajax.fields.AppointmentFields;
import com.openexchange.calendar.AppointmentDiff;
import com.openexchange.calendar.itip.Messages;
import com.openexchange.calendar.itip.generators.ArgumentType;
import com.openexchange.calendar.itip.generators.Sentence;
import com.openexchange.calendar.itip.generators.changes.ChangeDescriptionGenerator;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.contexts.Context;

public class ShownAs implements ChangeDescriptionGenerator {

	@Override
    public String[] getFields() {
		return new String[]{AppointmentFields.SHOW_AS};
	}

	@Override
    public List<Sentence> getDescriptions(Context ctx, Appointment original,
			Appointment updated, AppointmentDiff diff, Locale locale,
			TimeZone timezone) throws OXException {
		
		Sentence sentence = new Sentence(Messages.HAS_CHANGED_SHOWN_AS).add(string(updated.getShownAs()), ArgumentType.SHOWN_AS, updated.getShownAs());
		
		return Arrays.asList(sentence);
	}

	private Object string(int shownAs) {
        switch(shownAs) {
        case Appointment.RESERVED: return Messages.RESERVERD;
        case Appointment.TEMPORARY: return Messages.TEMPORARY;
        case Appointment.ABSENT: return Messages.ABSENT;
        case Appointment.FREE: return Messages.FREE;
        }
        return "Unknown";
	}

}
