# Atmosphere

## The namespaces and addressing elements

The namespace ox is used for internal Stanzas.

# Atmosphere Presence Protocol Spec

A payload element is an object in the form of

~~~
{
  "namespace" : "this is the namespace of the payload element"
  "element" : "this is the name of the payload element"
  "data" : ""
}
~~~

The namespace is only needed when the payload element isn't from the default
namespace or the enclosing namespace
data can be:

- a primitive json type
- an array of payload elements 
- a json object containing other payload elements or Arrays of payload elements

## Initial Presence

The initial presence message only has to consist of the obligatory session key
and the element key specifying that this message is a presence message.  This is
enough to send a broadcast to all subscribed users and signal that the user is
now available.

~~~{.javascript}
{
     "session" : "$session",
     "element" : "presence"
};
~~~

## Directed Presence

To send a directed Presence to a user that isn't in your roster but to whom you
want to advertise your presence for e.g. a short chat you just have to ad the
additional to key.

~~~{.javascript}
{
     "session" : "$session",
     "element" : "presence",
     "to": "ox://marens@1337/ox7ui
};
~~~

## Update after initial Presence

After the user's availability was broadcasted via the initial presence message
the user may choose to change his Presence status. The priority instructs the
Server to which resource messages should be routed. Resources with negative
priority never receive messages.
Optionals keys/elementss/elements: priority

~~~{.javascript}
{
  "session" : "$session",
  "element" : "presence",
  "payloads" : [
    {"element" : "status", "data" : "away"},
    {"element" : "message", "data" : "I'll be back!"},
    {"element" : "priority", "data" : "1"}
  ]
};
~~~

## Final unavailable Presence

The final unavailable presence broadcast to inform your subscribed contacts
that you are gone. Type has to be set to unavailable.
Optionals keys/elements: message

~~~{.javascript}
{
    "session" : "$session",
    "element" : "presence",
    "type": "unavailable",
    payloads : [
      {"element" : "message", "data" : "Bye!"}
    ]
};
~~~

## Subscribe Request

Mr. X wants to subscribe to the presence of marens@1337.
Optionals keys/elements: message

~~~{.javascript}
{
  "session" : "$session",
  "element" : "presence",
  "to" : "ox://marens@1337",
  "type" : "subscribe",
  "payloads" : [
    {"element" : "message", "data" : "Hello marens, please let me subscribe to your presence, WBR., Mr. X"}
  ]
}
~~~

## Subscribed Response

To accept a former subscription request from marens@1337.
Optionals keys/elements: message

~~~{.javascript}
{
  "session" : "$session",
  "element" : "presence",
  "to" : "ox://mrx@1337",
  "type" : "subscribed",
  "payloads" : [
    {"element" : "message", "data" :  "Hello Mr. X!"}
  ]
}
~~~

## UnSubscribed Presence Message

To refuse a former presence subscription request or to cancel an already granted
presence subscription.
Optionals keys/elements: message

~~~{.javascript}
{
  "session" : "$session",
  "element" : "presence",
  "to" : "ox://mrx@1337",
  "type" : "unsubscribed",
  "payloads" : [
    {"element" : "message", "data" : "Bye Mr. X!"}
  ]
}
~~~

## UnSubscribe Request
If you no longer want to be informed about the presence status of another
entity.

~~~{.javascript}
{
  "session" : "$session",
  "element" : "presence",
  "to" : "ox://marens@1337",
  "type" : "unsubscribe"
}
~~~
