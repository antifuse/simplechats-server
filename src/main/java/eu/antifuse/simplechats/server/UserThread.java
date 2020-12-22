package eu.antifuse.simplechats.server;

import java.io.*;
import java.net.Socket;
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        writer.println("Connected!");
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
                String message = reader.readLine();
                if (message.equals("DC")) break;
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

    public void sendMessage(String message) {
        writer.println(message);
    }

    public void handleMessage(String message) {
        String[] mWArgs = message.split(" ");
        switch (mWArgs[0]) {
            case "UN": {
                if (this.username != null) {
                    server.broadcast(this.username + " changed their name to " + mWArgs[1], null);
                } else {
                    server.broadcast(mWArgs[1] + " connected", null);
                }
                this.username = mWArgs[1];
                break;
            }
            case "SN": {
                if (this.username == null) {
                    writer.println("You need to specify a user name.");
                    break;
                }
                String content = message.substring(3);
                server.broadcast("[" + this.username + "] " + content, null);
                break;
            }
            case "LS": {
                Set<UserThread> users = server.getUserThreads();
                this.sendMessage(users.size() + " online users:");
                for (UserThread u: server.getUserThreads()) {
                    this.sendMessage(u.getUsername());
                }
                break;
            }
        }
    }

}
