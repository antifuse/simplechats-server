package eu.antifuse.simplechats.server;

import eu.antifuse.simplechats.Transmission;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Set;

public class UserThread extends Thread {
    private Socket socket;
    private Server server;
    private PrintWriter writer;
    private String username;
    private BufferedReader reader;

    public UserThread(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        this.username = null;
        InputStream input = null;
        OutputStream output = null;
        try {
            input = socket.getInputStream();
            output = socket.getOutputStream();
            this.reader = new BufferedReader(new InputStreamReader(input));
            this.writer = new PrintWriter(output, true);
            writer.println(new Transmission(Transmission.TransmissionType.SYSTEM, "Connected!").serialize());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void run() {
        while (true) {
            try {
                Transmission message = Transmission.deserialize(reader.readLine());
                if (message.getType() == Transmission.TransmissionType.RQ_DISCONNECT) break;
                System.out.println(message);
                this.handleMessage(message);

            } catch (IOException ex) {
                System.out.println("Error in UserThread: " + ex.getMessage());
                ex.printStackTrace();
                break;
            }
        }
        server.removeUser(this);
        writer.close();
    }

    public void sendMessage(Transmission message) {
        writer.println(message.serialize());
    }

    public void handleMessage(Transmission message) {
        switch (message.getType()) {
            case RQ_NICK: {
                if (this.username != null) {
                    server.broadcast(new Transmission(Transmission.TransmissionType.NAMECHANGE, this.username, message.data(0)), null);
                } else {
                    server.broadcast(new Transmission(Transmission.TransmissionType.JOIN, message.data(0)), null);
                }
                this.username = message.data(0);
                break;
            }
            case RQ_SEND: {
                if (this.username == null) {
                    this.sendMessage(new Transmission(Transmission.TransmissionType.SYSTEM, "err.nousername"));
                    break;
                }
                String content = message.data(0);
                server.broadcast(new Transmission(Transmission.TransmissionType.MESSAGE, this.username, content), null);
                break;
            }
            case RQ_LIST: {
                Set<UserThread> users = server.getUserThreads();
                ArrayList<String> names = new ArrayList<>();
                users.forEach((user)-> {
                    names.add(user.getUsername());
                });
                this.sendMessage(new Transmission(Transmission.TransmissionType.USERLIST, names.toArray(new String[0])));
                break;
            }
        }
    }

}
