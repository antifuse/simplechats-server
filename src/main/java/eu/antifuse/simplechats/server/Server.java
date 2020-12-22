package eu.antifuse.simplechats.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Server {
    private final int port;
    private final Set<UserThread> userThreads = new HashSet<>();

    public Server(int port) {
        this.port = port;
    }

    public void execute() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Listening on port " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Connection from IP " + socket.getInetAddress().getHostAddress());
                UserThread newUser = new UserThread(socket, this);
                userThreads.add(newUser);
                newUser.start();
            }
        } catch (IOException ex) {
            System.out.println("Error in the server: " + ex.getMessage());
            ex.printStackTrace();
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

    public void broadcast(String message, UserThread excludeUser) {
        for (UserThread aUser : userThreads) {
            if (aUser != excludeUser) {
                aUser.sendMessage(message);
            }
        }
        System.out.println("Broadcasting: " + message);
    }

    public void removeUser(UserThread aUser) {
        userThreads.remove(aUser);
        this.broadcast(aUser.getUsername() + " left.", null);
    }

    public boolean hasUsers() {
        return !this.userThreads.isEmpty();
    }

    public Set<UserThread> getUserThreads() {
        return userThreads;
    }
}
