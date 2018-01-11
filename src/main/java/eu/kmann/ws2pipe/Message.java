package eu.kmann.ws2pipe;

public class Message
{
    MessageType messageType;

    Command command;

    String pipeName;

    String message;

    public enum Command
    {
     CONNECT,
     DISCONNECT;
    }

    public enum MessageType
    {
     GATEWAY_MESSAGE,
     PIPE_MESSAGE;
    }
}
