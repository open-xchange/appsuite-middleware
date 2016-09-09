---
title: Web Sockets
---

# Introduction

Starting with v7.8.3 the Open-Xchange Middleware supports accepting Web Sockets that are opened by authenticated request. Hence, a Web Socket
representation in the Open-Xchange Middleware always has an association to a certain Open-Xchange session.


# How it works

Once a Web Socket is initiated by a Browser, the special `"session"` URL parameter along-side with accompanying Open-Xchange cookies are
expected to be passed along-side with the upgrade request that initiates to establish a Web Socket connection. Moreover, the upgrade request
should be routed to a certain path to distinguish between Web Sockets' natures. E.g. having an upgrade request to path `"/socket.io"`
indicates that the Web Socket is supposed to be used to serve Socket.IO clients. Thus, the class `com.openexchange.websockets.WebSocket`
representing an accepted Web Socket provides

 - The identifier of the associated session
 - The identifiers of the associated user and context
 - Any URL query parameters that were available from Web Socket connect request
 - The path from the Web Socket connect request
 - A Web Socket session `com.openexchange.websockets.WebSocketSession` bound to the Web Socket allowing to store/cache associated data

If the upgrade request gets authenticated (session verification, etc.) the Web Socket is accepted, bound to specified session and added to
OSGi Web Socket Service (`com.openexchange.websockets.WebSocketService`) that manages/controls open Web Sockets. That Web Socket Service supports
basic methods to

 - Check if there is any open Web Socket for a user and optionally accepts a path filter expression (e.g. `/socket.io/*`) to also check for a certain path criterion
 - Send direct text messages via all open Web Socket for a certain user and optionally accepts a path filter expression (e.g. `/socket.io/*`) to only consider those Web Socket linked to a certain path
 - Gather monitoring data; for instance number of connected Web Sockets

In addition, the mentioned methods work cluster-wide. So checking for existence of a Web Socket for a user using a path filter expression
checks the the cluster if there is any node that satisfies the look-up. Moreover, sending a message also works cluster-wide.

Furthermore, it is possible to OSGi-wise register instances of `com.openexchange.websockets.WebSocketListener`. The registered instances will
receive call-backs in case of certain events that happen on the Web Socket connection. Currently:

 - On Web Socket connect
 - On Web Socket close
 - On a received text message

# Transcoding of received/sent Web Socket messages

An instance of `com.openexchange.websockets.WebSocket` allows to set a `com.openexchange.websockets.MessageTranscoder`. Every in-bound and
out-bound message is then routed through the appropriate `MessageTranscoder` methods before it reaches the end-point:

 - onInboundMessage
 - onOutboundMessage

For instance, a `MessageTranscoder` is used to encode/decode outgoing/incoming messages to be conform to Socket.IO protocol. See
implementations in `com.openexchange.socketio.websocket.WsTransportConnection.onInboundMessage()` and `com.openexchange.socketio.websocket.WsTransportConnection.onOutboundMessage()`
