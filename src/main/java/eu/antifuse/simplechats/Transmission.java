package eu.antifuse.simplechats;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.*;

public class Transmission implements Serializable {
    static Map<String,String> escapeTable = new HashMap<String, String>() {{
        put("&","&nd");
        put(":","&cl");
        put("ä","&au");
        put("ö","&ou");
        put("ü","&uu");
        put("ß","&su");
        put("Ä","&AU");
        put("Ö","&OU");
        put("Ü","&UU");
        put("ẞ","&SU");
    }};
    static Map<String,String> deEscapeTable = new HashMap<String, String>() {{
        put("&nd","&");
        put("&cl",":");
        put("&au","ä");
        put("&ou","ö");
        put("&uu","ü");
        put("&su","ß");
        put("&AU","Ä");
        put("&OU","Ö");
        put("&UU","Ü");
        put("&SU","ẞ");
    }};
    private TransmissionType type;
    private String[] payload;

    public Transmission(TransmissionType type, String ...payload) {
        this.payload = payload;
        this.type = type;
    }

    public String data(int index) {
        return index < this.payload.length ? this.payload[index] : null;
    }

    public String[] getData() {
        return this.payload;
    }

    public int size() {
        return this.payload.length;
    }

    public TransmissionType getType() {
        return this.type;
    }

    public String serialize() {
        return this.type.name()+":"+String.join(":", Arrays.stream(this.payload).map(Transmission::escapeString).toArray(String[]::new));
    }

    public static Transmission deserialize(String serial) {
        String[] args = serial.split(":");
        args = Arrays.stream(args).map(Transmission::deEscapeString).toArray(String[]::new);
        TransmissionType type = TransmissionType.valueOf(args[0]);
        ArrayList<String> argsList = new ArrayList<>(Arrays.asList(args));
        argsList.remove(0);
        return new Transmission(type, argsList.toArray(new String[0]));
    }

    public static String escapeString(String input) {
        for(String key: escapeTable.keySet()) {
            input = input.replace(key, escapeTable.get(key));
        }
        return input;
    }

    public static String deEscapeString(String input) {
        for(String key: deEscapeTable.keySet()) {
            input = input.replace(key, deEscapeTable.get(key));
        }
        return input;
    }

    public enum TransmissionType implements Serializable{
        MESSAGE,
        SYSTEM,
        NAMECHANGE,
        USERLIST,
        LEAVE,
        JOIN,
        DIRECT,
        RQ_DISCONNECT,
        RQ_NICK,
        RQ_LIST,
        RQ_SEND,
        RQ_DIRECT
    }
}
