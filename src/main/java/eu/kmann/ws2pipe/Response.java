package eu.kmann.ws2pipe;

public class Response
{
    Status status;
    
    String error;
    
    String response;

    public enum Status
    {
     OK,
     ERROR;
    }
}
