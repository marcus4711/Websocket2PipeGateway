Starts a local websocket server on port 12000. Used to connect to a local named pipe.

Uses JSON formatted messages to control and communicate.

Create connection to named pipe
```JSON
{
	"messageType": "GATEWAY_MESSAGE",
	"command": "CONNECT",
	"pipeName": "\\.\pipe\myPipe"
}
```

Disconnect from named pipe
```JSON
{
	"messageType": "GATEWAY_MESSAGE",
	"command": "DISCONNECT"
}
```

Send a message  to pipe
```JSON
{
	"messageType": "PIPE_MESSAGE",
	"message": "Hello world"
}
```

Respose status could be OK or ERROR. If OK, then response is set else error is set. 
```JSON
{
	"status": "OK"
	"response": "Hello there"
}
```

```JSON
{
	"status": "ERROR"
	"error": "File not found"
}
```