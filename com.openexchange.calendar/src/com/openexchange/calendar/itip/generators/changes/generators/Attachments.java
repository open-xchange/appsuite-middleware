package com.openexchange.calendar.itip.generators.changes.generators;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.openexchange.calendar.AppointmentDiff;
import com.openexchange.calendar.itip.Messages;
import com.openexchange.calendar.itip.generators.AttachmentMemory;
import com.openexchange.calendar.itip.generators.Sentence;
import com.openexchange.calendar.itip.generators.changes.ChangeDescriptionGenerator;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.contexts.Context;

public class Attachments implements ChangeDescriptionGenerator {

	private final AttachmentMemory memory;
	
	
	public Attachments(AttachmentMemory memory) {
		this.memory = memory;
	}
	
	@Override
    public String[] getFields() {
		return new String[] {};
	}

	@Override
    public List<Sentence> getDescriptions(Context ctx, Appointment original,
			Appointment updated, AppointmentDiff diff, Locale locale,
			TimeZone timezone) throws OXException {
		List<Sentence> sentences = new ArrayList<Sentence>(1);
		if (memory.hasAttachmentChanged(updated.getObjectID(), ctx.getContextId())) {
			sentences.add(new Sentence(Messages.ATTACHMENTS_CHANGED));
		}
		return sentences;
	}

}
