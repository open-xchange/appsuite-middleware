
# Calendar account configuration

This chapter describes the configuration of different calendar accounts.

## ICal feeds

Various ICal feeds can be subscribed by the given URI and optional basic authentication.

For example:

```json
{
  "configuration":{
    "uri":"http://example.org/feed/theExampleFeed.ics"
  }
}
```

If basic authentication is required the body should contain 'login', 'password' or both as desired by the endpoint, for instance:

```json
{
  "configuration":{
    "uri":"http://example.org/feed/theSecureExampleFeed.ics"
    "login":"johnDoe",
    "password":"myPassword"
  }
}
```

## Google

The google calendar account basically only needs the id of a valid google oauth account with the `calendar_ro` scope.

For example:

```json
{
  "configuration":{
    "oauthId":5
  }
}
```

This way the middleware creates a google calendar account which uses the given oauth account. During the initialization of the account the `folders` field is filled with all available google calendars.
This json object contains another json objects for each calendar, whereby the key of each entry is the id of the google calendar (E.g. `test_account@gmail.com`). Each google calendar object contains different informations about the calendar (e.g. the color, default reminders etc.). But the most important field is the `enabled` field which defines whether a google calendar is subscribed or not. By default only the primary account is enabled (and therefore subscribed). If the client wants to enable other accounts too, it can do so by setting the `enabled` field to `true` and updating the account. 

Example configuration after creation:

```json
 "configuration": {
            "oauthId": 41,
            "folders": {
                "test_account@gmail.com": {
                    "enabled": true,
                    "color": "#9fe1e7",
                    "default_reminders": [
                        {
                            "action": "DISPLAY",
                            "duration": "-PT30M"
                        }
                    ],
                    "primary": true
                },
                "#contacts@group.v.calendar.google.com": {
                    "enabled": false,
                    "color": "#92e1c0",
                    "default_reminders": []
                },
                "en.german#holiday@group.v.calendar.google.com": {
                    "enabled": false,
                    "color": "#16a765",
                    "default_reminders": []
                },
                "e_2_en#weeknum@group.v.calendar.google.com": {
                    "enabled": false,
                    "color": "#42d692",
                    "default_reminders": []
                }
            }
        }
```