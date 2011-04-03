Hudsucker, a proxy of some kind.
================================

Hudsucker is an attempt to make a *non-terminating* SSL proxy daemon, primarily for load balancing SSL connections on a busy site.

Hudsucker's design will consist of two parts, a __daemon__, running on the front end, and some number of __client__ processes, each running on its own node.

The daemon does the following:

	* Listens for SSL requests on a socket, and when it gets one, it:
		* Inspects the ClientHello for a session token, and if there is one, tries to find the client that previously served that token's connection.
		* If there is no token, that token hasn't been seen before, or if the client associated with that token has gone offline, then find some client that isn't 
		* Forwards data on that connection to the server that should handle it.
	* Listens for messages from client nodes. These messages contain:
		* "I am now alive" -- marks the client as ready to receive connections.
		* "I am going to die" -- marks the client as not ready to receive connections.
		* "I am still here" -- marks that client as still ready to receive connections (without any of these messages in a timely manner, and without any active connections to that client, that client is assumed dead).
		* "I found a token!" -- marks that client as having talked with an SSL client using a specific session token. If that token is seen again, that client should be preferred for the connection.
		
The client does the following:

	* When it starts up, it tells the daemon that it it ready to serve.
	* When it will go down, it tells the daemon that it is no longer ready to serve.
	* Periodically, it will call the daemon and let it know that it is still there (possibly, with a health report on how well it can handle connections).
	* When its SSL server (probably a separate process, like a HTTP server) makes a connection, it tells the daemon about the session that just started (that is, sends it the session ID).

__THIS IS EXPERIMENTAL__. It might not work. I've never written a proxy before.

It's also not done and probably doesn't work yet. Don't use it. (thanks!)

*LONG LIVE THE HUD!*