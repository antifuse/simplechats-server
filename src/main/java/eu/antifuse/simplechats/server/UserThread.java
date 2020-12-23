package eu.antifuse.simplechats.server;

import eu.antifuse.simplechats.Transmission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Set;

public class UserThread extends Thread {
    private Logger logger;
    private Socket socket;
    private Server server;
    private PrintWriter writer;
    private String username;
    private BufferedReader reader;

    public UserThread(Socket socket, Server server) {
        this.logger = LoggerFactory.getLogger(Server.class);
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
            logger.info("Created new user thread for IP {}", socket.getInetAddress().toString());
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
                logger.error("Error in UserThread: " + ex.getMessage());
                break;
            }
        }
        logger.info("Logging out user {} with IP {}", this.username, socket.getInetAddress());
        server.removeUser(this);
        writer.close();
    }

    public void sendMessage(Transmission message) {
        writer.println(message.serialize());
    }

    public void handleMessage(Transmission message) {
        logger.info("Received {} request from user thread {}. Processing...", message.getType().name(), this.username != null ? this.username:this.socket.getInetAddress());
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
                    logger.info("Sent err.nousername to IP {}", this.socket.getInetAddress());
                    this.sendMessage(new Transmission(Transmission.TransmissionType.SYSTEM, "err.nousername"));
                    break;
                }
                String content = message.data(0);
                logger.info("Message to all from user {}: {}", this.username, content);
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
                logger.info("Sent {} user names to {}", names.size(), this.username != null ? this.username : this.socket.getInetAddress());
                break;
            }
            case RQ_DIRECT: {
                UserThread rec = this.server.getUserThreads().stream().filter((thread)->thread.getUsername().equals(message.data(0))).findFirst().orElse(null);
                if (rec == null) {
                    this.sendMessage(new Transmission(Transmission.TransmissionType.SYSTEM, "error.recNotFound"));
                    logger.info("Sent err.recNotFound ({}) to user {} ", message.data(0), this.username);
                }
                else {
                    rec.sendMessage(new Transmission(Transmission.TransmissionType.MESSAGE, this.username, message.data(1)));
                    logger.info("Direct message to {} from {}: {}", message.data(0), this.username, message.data(1));
                }
                break;
            }
        }
    }

}
