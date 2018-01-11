package eu.kmann.ws2pipe;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.google.gson.Gson;

import eu.kmann.ws2pipe.Response.Status;

public class Websocket2PipeGateway extends WebSocketServer
{

    private Gson gson = new Gson();

    private RandomAccessFile pipe;

    private static String RESULT_OK = "OK";

    private static String RESULT_NO_PIPE_CONNECTION = "NO PIPE CONNECTION";

    private static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");

    public static void main(String[] args) throws IOException, InterruptedException
    {

        Websocket2PipeGateway websocket2Pipe = new Websocket2PipeGateway();
        websocket2Pipe.start();
        System.out.println("Press enter to exit");
        try
        {
            System.in.read();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        websocket2Pipe.stop();
    }

    public Websocket2PipeGateway()
    {
        super(new InetSocketAddress("localhost", 12000));
    }

    @Override
    public void onError(WebSocket conn, Exception ex)
    {
        conn.send(createResponse(false, ex.getMessage()));
    }

    @Override
    public void onMessage(WebSocket conn, String message)
    {
        Message parsedMessage = null;
        try
        {
            parsedMessage = gson.fromJson(message, Message.class);
        }
        catch (Exception e)
        {
            conn.send(createResponse(false, "wrong format"));
        }

        conn.send(handleMessage(parsedMessage));
    }

    private synchronized String handleMessage(Message parsedMessage)
    {
        if (parsedMessage.messageType == null)
        {
            throw new IllegalArgumentException("Unknown message type: " + parsedMessage.messageType);
        }

        switch (parsedMessage.messageType)
        {
            case GATEWAY_MESSAGE:
                return handleGatewayMessage(parsedMessage);

            case PIPE_MESSAGE:
                return handlePipeMessage(parsedMessage);

            default:
                throw new IllegalArgumentException();
        }
    }

    private String handleGatewayMessage(Message parsedMessage)
    {
        if (parsedMessage.command == null)
        {
            throw new IllegalArgumentException("Unknown command: " + parsedMessage.command);
        }

        switch (parsedMessage.command)
        {
            case CONNECT:
                if (parsedMessage.pipeName == null)
                {
                    throw new IllegalArgumentException("Pipe name must not be null");
                }
                closePipe();
                openPipe(parsedMessage);
                break;

            case DISCONNECT:
                closePipe();
                break;

            default:
                throw new IllegalArgumentException();
        }

        return createResponse(true, "");
    }

    private String handlePipeMessage(Message parsedMessage)
    {
        if (pipe != null)
        {
            try
            {
                pipe.write(parsedMessage.message.getBytes());
            }
            catch (IOException e)
            {
                throw new RuntimeException("Error while writing command: " + parsedMessage.message, e);
            }

            try
            {
                // data size in pipe is first acquirable after reading one byte
                byte firstByte = pipe.readByte();
                byte[] responseBytes = new byte[(int) pipe.length() + 1];
                responseBytes[0] = firstByte;
                pipe.readFully(responseBytes, 1, responseBytes.length - 1);

                return createResponse(true, new String(responseBytes, CHARSET_UTF8));
            }
            catch (EOFException e)
            {
                return RESULT_OK;
            }
            catch (Exception e)
            {
                throw new RuntimeException("Error while reading response for command: " + parsedMessage.message, e);
            }
        }
        else
        {
            return RESULT_NO_PIPE_CONNECTION;
        }
    }

    private void openPipe(Message parsedMessage)
    {
        try
        {
            pipe = new RandomAccessFile(parsedMessage.pipeName, "rw");
        }
        catch (FileNotFoundException e)
        {
            throw new IllegalArgumentException("Error while opening pipe", e);
        }
    }

    private void closePipe()
    {
        if (pipe != null)
        {
            try
            {
                pipe.close();
            }
            catch (IOException e)
            {
                pipe = null;
            }
        }
    }

    private String createResponse(boolean success, String message)
    {
        Response response = new Response();

        if (success)
        {
            response.status = Status.OK;
            response.response = message;
        }
        else
        {
            response.status = Status.ERROR;
            response.error = message;
        }

        return gson.toJson(response);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake)
    {
        // System.out.println("onOpen");
    }

    @Override
    public void onStart()
    {
        // System.out.println("onStart");
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote)
    {
        // System.out.println("onClose");
    }
}
