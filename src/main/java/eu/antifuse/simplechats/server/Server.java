package eu.antifuse.simplechats.server;

import eu.antifuse.simplechats.Transmission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Server {
    private Logger logger;
    private final int port;
    private final Set<UserThread> userThreads = new HashSet<>();

    public Server(int port) {
        this.logger = LoggerFactory.getLogger(Server.class);
        this.port = port;
    }

    public void execute() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Listening on port {}", port);
            while (true) {
                Socket socket = serverSocket.accept();
                logger.info("Connection from IP " + socket.getInetAddress().getHostAddress());
                UserThread newUser = new UserThread(socket, this);
                userThreads.add(newUser);
                newUser.start();
            }
        } catch (IOException ex) {
            logger.error("Error in the server: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        int port;
        if (args.length < 1) {
            port = 2311;
        } else {
            port = Integer.parseInt(args[0]);
        }
        Server server = new Server(port);
        server.execute();
    }

    public void broadcast(Transmission message, UserThread excludeUser) {
        for (UserThread aUser : userThreads) {
            if (aUser != excludeUser) {
                aUser.sendMessage(message);
            }
        }
        logger.info("Broadcasting: " + message.serialize());
    }

    public void removeUser(UserThread aUser) {
        userThreads.remove(aUser);
        this.broadcast(new Transmission(Transmission.TransmissionType.LEAVE, aUser.getUsername()), null);
    }

    public boolean hasUsers() {
        return !this.userThreads.isEmpty();
    }

    public Set<UserThread> getUserThreads() {
        return userThreads;
    }
}
