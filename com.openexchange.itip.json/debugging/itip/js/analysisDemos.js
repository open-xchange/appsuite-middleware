// Request
ox.demos.define("A new Appointment is created by an external organizer").execution(function (env) {
    env.analyze("requestNew.ics");
    env.done();
});

ox.demos.define("An update to an Appointment (rescheduling) is sent by an external organizer").setup(function (env) {
    env.createAppointment("app", {
        organizer: "A@example.com",
        confirmations: [
                {
                    "mail": "A@example.com",
                    "status": 1,
                    "type": 5
                }
            ],
        participants : [
                env.participants.external("A@example.com"),
                env.participants.currentUser()
        ],
        "start_date": env.date("8:00 PM"),
        "end_date": env.date("10:00 PM")
    });
    env.done();
}).execution(function (env) {
    env.analyze("requestReschedule.ics");
    env.done();
});

ox.demos.define("An update to an Appointment (changing the title) is sent by an external organizer").setup(function (env) {
    env.createAppointment("app", {
        organizer: "A@example.com",
        confirmations: [
                {
                    "mail": "A@example.com",
                    "status": 1,
                    "type": 5
                }
            ],
        participants : [
                env.participants.external("A@example.com"),
                env.participants.currentUser()
        ],
        "start_date": env.date("8:00 PM"),
        "end_date": env.date("10:00 PM")
    });
    env.done();
}).execution(function (env) {
    env.analyze("requestUpdate.ics");
    env.done();
});


ox.demos.define("A new Appointment is created by an external organizer, which has a conflict").setup(function (env) {
    env.createAppointment("app", {
        organizer: "A@example.com",
        confirmations: [
                {
                    "mail": "A@example.com",
                    "status": 1,
                    "type": 5
                }
            ],
        participants : [
                env.participants.external("A@example.com"),
                env.participants.currentUser()
        ],
        "start_date": env.date("8:00 PM"),
        "end_date": env.date("10:00 PM")
    });
    env.done();
}).execution(function (env) {
    env.analyze("requestNew.ics");
    env.done();
});

ox.demos.define("A new series with one change exception is created by an external organizer").execution(function (env) {
    env.analyze("requestNewSeriesWithException.ics");
    env.done();
});

ox.demos.define("A series is updated with one change exception (rescheduling) by an external organizer").setup(function(env){
    env.createAppointment("app", {
        organizer: "A@example.com",
        confirmations: [
                {
                    "mail": "A@example.com",
                    "status": 1,
                    "type": 5
                }
            ],
        participants : [
                env.participants.external("A@example.com"),
                env.participants.currentUser()
        ],
        "start_date": env.date("12/4/2017 14:00"),
        "end_date": env.date("12/4/2017 15:00"),
        "recurrence_type": 2, // Weekly
        "interval": 1,
        "days": 2 // Every monday
    });
    env.done();
}).execution(function (env) {
    env.analyze("requestUpdateSeriesAddingExceptionWithRescheduling.ics");
    env.done();
});

ox.demos.define("A series is updated with one change exception (changing the title) by an external organizer").setup(function(env){
    env.createAppointment("app", {
        organizer: "A@example.com",
        confirmations: [
                {
                    "mail": "A@example.com",
                    "status": 1,
                    "type": 5
                }
            ],
        participants : [
                env.participants.external("A@example.com"),
                env.participants.currentUser()
        ],
        "start_date": env.date("12/4/2017 14:00"),
        "end_date": env.date("12/4/2017 15:00"),
        "recurrence_type": 2, // Weekly
        "interval": 1,
        "days": 2 // Every monday
    });
    env.done();
}).execution(function (env) {
    env.analyze("requestUpdateSeriesAddingExceptionWithDifferentTitle.ics");
    env.done();
});

ox.demos.define("A series is updated with one participant added by an external organizer").setup(function (env) {
    env.createAppointment("app", {
        organizer: "A@example.com",
        confirmations: [
                {
                    "mail": "A@example.com",
                    "status": 1,
                    "type": 5
                }
            ],
        participants : [
                env.participants.external("A@example.com"),
                env.participants.currentUser()
        ],
        "start_date": env.date("8:00 PM"),
        "end_date": env.date("10:00 PM")
    });
    env.done();
}).execution(function (env) {
    env.analyze("requestAddParticipant.ics");
    env.done();
});

ox.demos.define("A series is updated with one participant changing state by an external organizer").setup(function (env) {
    env.createAppointment("app", {
        organizer: "A@example.com",
        confirmations: [
                {
                    "mail": "A@example.com",
                    "status": 1,
                    "type": 5
                },
                {
                    "mail": "B@example.com",
                    "status": 0,
                    "type": 5
                }
                
            ],
        participants : [
                env.participants.external("A@example.com"),
                env.participants.external("B@example.com"),
                env.participants.currentUser()
        ],
        "start_date": env.date("8:00 PM"),
        "end_date": env.date("10:00 PM")
    });
    env.done();
}).execution(function (env) {
    env.analyze("requestChangeParticipantState.ics");
    env.done();
});

// Add
ox.demos.define("Add an exception to an existing series").setup(function (env) {
    env.createAppointment("app", {
        organizer: "A@example.com",
        confirmations: [
                {
                    "mail": "A@example.com",
                    "status": 1,
                    "type": 5
                }
            ],
        participants : [
                env.participants.external("A@example.com"),
                env.participants.currentUser()
        ],
        "start_date": env.date("8:00 PM"),
        "end_date": env.date("10:00 PM"),
        "recurrence_type": 2, // Weekly
        "interval": 1,
        "ocurrences": 10,
        "days": 2 // Every monday
    });
    env.done();
}).execution(function (env) {
    env.analyze("addException.ics");
    env.done();
});

ox.demos.define("Add an exception to an existing series which would have conflicts").setup(function (env) {
    env.createAppointment("app", {
        organizer: "A@example.com",
        confirmations: [
                {
                    "mail": "A@example.com",
                    "status": 1,
                    "type": 5
                }
            ],
        participants : [
                env.participants.external("A@example.com"),
                env.participants.currentUser()
        ],
        "start_date": env.date("8:00 PM"),
        "end_date": env.date("10:00 PM"),
        "recurrence_type": 2, // Weekly
        "interval": 1,
        "ocurrences": 10,
        "days": 2 // Every monday
    });
    
    env.createAppointment("conflict", {
        organizer: "A@example.com",
        confirmations: [
                {
                    "mail": "A@example.com",
                    "status": 1,
                    "type": 5
                }
            ],
        participants : [
                env.participants.external("A@example.com"),
                env.participants.currentUser()
        ],
        "start_date": env.timestamp(Date.parse("next monday").add({hours: 22})),
        "end_date": env.timestamp(Date.parse("next monday").add({hours: 23}))
    });
    env.done();
}).execution(function (env) {
    env.analyze("addException.ics");
    env.done();
});

ox.demos.define("Add an exception to an unknown series").execution(function (env) {
    env.analyze("addExceptionToFantasyUID.ics");
    env.done();
});


ox.demos.define("Add a redundant exception").setup(function(env) {
    env.createAppointment("app", {
        organizer: "A@example.com",
        confirmations: [
                {
                    "mail": "A@example.com",
                    "status": 1,
                    "type": 5
                }
            ],
        participants : [
                env.participants.external("A@example.com"),
                env.participants.currentUser()
        ],
        "start_date": env.date("8:00 PM"),
        "end_date": env.date("10:00 PM"),
        "recurrence_type": 2, // Weekly
        "interval": 1,
        "ocurrences": 10,
        "days": 2 // Every monday
    }).done(function () {
        env.createException("exception", {
            organizer: "A@example.com",
            confirmations: [
                    {
                        "mail": "A@example.com",
                        "status": 1,
                        "type": 5
                    }
                ],
            participants : [
                    env.participants.external("A@example.com"),
                    env.participants.currentUser()
            ],
            "start_date":  env.timestamp(Date.parse("next monday").add({hours: 19})),
            "end_date": env.timestamp(Date.parse("next monday").add({hours: 20})),
            "id": env.apps.app.id,
            "recurrence_date_position": env.timestamp(Date.parse("next monday"))
        });
        env.done();
    });
}).execution(function (env) {
    env.analyze("addException.ics");
    env.done();
});

// Counter
ox.demos.define("An attendee counters with a rescheduling").setup(function (env) {
    env.createAppointment("app", {
        organizer: env.user.email1,
        confirmations: [
                {
                    "mail": "A@example.com",
                    "status": 1,
                    "type": 5
                }
            ],
        participants : [
                env.participants.currentUser(),
                env.participants.external("A@example.com")
        ],
        "start_date": env.date("8:00 PM"),
        "end_date": env.date("10:00 PM")
    });
    env.done();
}).execution(function (env) {
    env.analyze("counterReschedule.ics");
    env.done();
});

ox.demos.define("An attendee counters with changing the title").setup(function (env) {
    env.createAppointment("app", {
        organizer: env.user.email1,
        confirmations: [
                {
                    "mail": "A@example.com",
                    "status": 1,
                    "type": 5
                }
            ],
        participants : [
                env.participants.currentUser(),
                env.participants.external("A@example.com")
        ],
        "start_date": env.date("8:00 PM"),
        "end_date": env.date("10:00 PM")
    });
    env.done();
}).execution(function (env) {
    env.analyze("counterUpdate.ics");
    env.done();
});

ox.demos.define("An attendee counters with an additional attendee").setup(function (env) {
    env.createAppointment("app", {
        organizer: env.user.email1,
        confirmations: [
                {
                    "mail": "A@example.com",
                    "status": 1,
                    "type": 5
                }
            ],
        participants : [
                env.participants.currentUser(),
                env.participants.external("A@example.com")
        ],
        "start_date": env.date("8:00 PM"),
        "end_date": env.date("10:00 PM")
    });
    env.done();
}).execution(function (env) {
    env.analyze("counterAddParticipant.ics");
    env.done();
});

ox.demos.define("An attendee wants to reschedule a series exception").setup(function (env) {
    env.createAppointment("app", {
        organizer: env.user.email1,
        confirmations: [
                {
                    "mail": "A@example.com",
                    "status": 1,
                    "type": 5
                }
            ],
        participants : [
                env.participants.external("A@example.com"),
                env.participants.currentUser()
        ],
        "start_date": env.date("8:00 PM"),
        "end_date": env.date("10:00 PM"),
        "recurrence_type": 2, // Weekly
        "interval": 1,
        "ocurrences": 10,
        "days": 2 // Every monday
    });
    env.done();
}).execution(function (env) {
    env.analyze("counterRescheduleException.ics");
    env.done();
});

ox.demos.define("An attendee counters an appointment that doesn't exist").execution(function (env) {
   env.analyze("counterUpdateFantasyUID.ics"); 
});


// Reply

ox.demos.define("An attendee accepts").setup(function (env) {
    env.createAppointment("app", {
        organizer: env.user.email1,
        confirmations: [
                {
                    "mail": "A@example.com",
                    "status": 0,
                    "type": 5
                }
            ],
        participants : [
                env.participants.currentUser(),
                env.participants.external("A@example.com")
        ],
        "start_date": env.date("8:00 PM"),
        "end_date": env.date("10:00 PM")
    });
    env.done();
}).execution(function (env) {
    env.analyze("replyAccept.ics");
    env.done();
});

ox.demos.define("An attendee declines").setup(function (env) {
    env.createAppointment("app", {
        organizer: env.user.email1,
        confirmations: [
                {
                    "mail": "A@example.com",
                    "status": 1,
                    "type": 5
                }
            ],
        participants : [
                env.participants.currentUser(),
                env.participants.external("A@example.com")
        ],
        "start_date": env.date("8:00 PM"),
        "end_date": env.date("10:00 PM")
    });
    env.done();
}).execution(function (env) {
    env.analyze("replyDecline.ics");
    env.done();
});

ox.demos.define("An attendee accepts tentatively").setup(function (env) {
    env.createAppointment("app", {
        organizer: env.user.email1,
        confirmations: [
                {
                    "mail": "A@example.com",
                    "status": 1,
                    "type": 5
                }
            ],
        participants : [
                env.participants.currentUser(),
                env.participants.external("A@example.com")
        ],
        "start_date": env.date("8:00 PM"),
        "end_date": env.date("10:00 PM")
    });
    env.done();
}).execution(function (env) {
    env.analyze("replyTentative.ics");
    env.done();
});

// TODO: The above three for series ocurrences

ox.demos.define("A party crasher joins an appointment").setup(function (env) {
    env.createAppointment("app", {
        organizer: env.user.email1,
        confirmations: [
                {
                    "mail": "A@example.com",
                    "status": 1,
                    "type": 5
                }
            ],
        participants : [
                env.participants.currentUser(),
                env.participants.external("A@example.com")
        ],
        "start_date": env.date("8:00 PM"),
        "end_date": env.date("10:00 PM")
    });
    env.done();
}).execution(function (env) {
    env.analyze("replyPartyCrasher.ics");
    env.done();
});


ox.demos.define("An attendee accepts an appointment that doesn't exist").execution(function (env) {
    env.analyze("replyFantasyUID.ics");
    env.done();
});


// Cancel
ox.demos.define("An appointment is cancelled by an external organizer").setup(function (env) {
    env.createAppointment("app", {
        organizer: "A@example.com",
        confirmations: [
                {
                    "mail": "A@example.com",
                    "status": 1,
                    "type": 5
                }
            ],
        participants : [
                env.participants.external("A@example.com"),
                env.participants.currentUser()
        ],
        "start_date": env.date("8:00 PM"),
        "end_date": env.date("10:00 PM")
    });
    env.done();
}).execution(function (env) {
    env.analyze("cancelAppointment.ics");
    env.done();
});

ox.demos.define("A change exception is cancelled by an external organizer").setup(function(env) {
    env.createAppointment("app", {
        organizer: "A@example.com",
        confirmations: [
                {
                    "mail": "A@example.com",
                    "status": 1,
                    "type": 5
                }
            ],
        participants : [
                env.participants.external("A@example.com"),
                env.participants.currentUser()
        ],
        "start_date": env.date("8:00 PM"),
        "end_date": env.date("10:00 PM"),
        "recurrence_type": 2, // Weekly
        "interval": 1,
        "ocurrences": 10,
        "days": 2 // Every monday
    }).done(function () {
        env.createException("exception", {
            organizer: "A@example.com",
            confirmations: [
                    {
                        "mail": "A@example.com",
                        "status": 1,
                        "type": 5
                    }
                ],
            participants : [
                    env.participants.external("A@example.com"),
                    env.participants.currentUser()
            ],
            "start_date":  env.timestamp(Date.parse("next monday").add({hours: 19})),
            "end_date": env.timestamp(Date.parse("next monday").add({hours: 20})),
            "id": env.apps.app.id,
            "recurrence_date_position": env.timestamp(Date.parse("next monday"))
        });
        env.done();
    });
}).execution(function (env) {
    env.analyze("cancelException.ics");
    env.done();
});

ox.demos.define("A regular series ocurrence is cancelled by an external organizer").setup(function(env) {
    env.createAppointment("app", {
        organizer: "A@example.com",
        confirmations: [
                {
                    "mail": "A@example.com",
                    "status": 1,
                    "type": 5
                }
            ],
        participants : [
                env.participants.external("A@example.com"),
                env.participants.currentUser()
        ],
        "start_date": env.date("8:00 PM"),
        "end_date": env.date("10:00 PM"),
        "recurrence_type": 2, // Weekly
        "interval": 1,
        "ocurrences": 10,
        "days": 2 // Every monday
    });
    env.done();
}).execution(function (env) {
    env.analyze("cancelException.ics");
    env.done();
});


ox.demos.define("An appointment is cancelled by an external organizer that does not exist").execution(function (env) {
    env.analyze("cancelAppointmentWithFantasyUID.ics");
    env.done();
});


// Declinecounter
ox.demos.define("A counter is declined by an external organizer").setup(function (env) {
    env.createAppointment("app", {
        organizer: "A@example.com",
        confirmations: [
                {
                    "mail": "A@example.com",
                    "status": 1,
                    "type": 5
                }
            ],
        participants : [
                env.participants.external("A@example.com"),
                env.participants.currentUser()
        ],
        "start_date": env.date("8:00 PM"),
        "end_date": env.date("10:00 PM")
    });
    env.done();
}).execution(function (env) {
    env.analyze("declinecounterForAppointment.ics");
    env.done();
});

ox.demos.define("A counter is declined by an external organizer for an appointment that doesn't exist").execution(function (env) {
    env.analyze("declinecounterForAppointmentWithFantasyUID.ics");
    env.done();
});

// Refresh
ox.demos.define("An attendee wants to refresh an appointment").setup(function (env) {
    env.createAppointment("app", {
        organizer: "A@example.com",
        confirmations: [
                {
                    "mail": "A@example.com",
                    "status": 1,
                    "type": 5
                }
            ],
        participants : [
                env.participants.external("A@example.com"),
                env.participants.currentUser()
        ],
        "start_date": env.date("8:00 PM"),
        "end_date": env.date("10:00 PM")
    });
    env.done();
}).execution(function (env) {
    env.analyze("refreshAppointment.ics");
    env.done();
});

ox.demos.define("An attendee wants to refresh an appointment that does not exist").execution(function (env) {
    env.analyze("refreshAppointmentWithFantasyUID.ics");
    env.done();
})

// Publish

ox.demos.define("An appointment is announced").execution(function (env) {
    env.analyze("publishAppointment.ics");
    env.done();
});

ox.demos.define("An appointment that was announced is rescheduled").setup(function (env) {
    env.createAppointment("app", {
        organizer: "A@example.com",
        confirmations: [
                {
                    "mail": "A@example.com",
                    "status": 1,
                    "type": 5
                }
            ],
        participants : [
                env.participants.external("A@example.com"),
                env.participants.currentUser()
        ],
        "start_date": env.date("8:00 PM"),
        "end_date": env.date("10:00 PM")
    });
    env.done();
}).execution(function (env) {
    env.analyze("publishAppointmentReschedule.ics");
    env.done();
});

ox.demos.define("An appointment that was announced is updated with a changed title").setup(function (env) {
    env.createAppointment("app", {
        organizer: "A@example.com",
        confirmations: [
                {
                    "mail": "A@example.com",
                    "status": 1,
                    "type": 5
                }
            ],
        participants : [
                env.participants.external("A@example.com"),
                env.participants.currentUser()
        ],
        "start_date": env.date("8:00 PM"),
        "end_date": env.date("10:00 PM")
    });
    env.done();
}).execution(function (env) {
    env.analyze("publishAppointmentUpdate.ics");
    env.done();
});


//TODO: Delegation
//TODO: Attachments
