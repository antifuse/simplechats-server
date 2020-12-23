package eu.antifuse.simplechats;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Transmission implements Serializable {
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
        return this.type.name()+":"+String.join(":", Arrays.stream(this.payload).map((value)->value.replace("&","&a").replace(":","&c")).toArray(String[]::new));
    }

    public static Transmission deserialize(String serial) {
        String[] args = serial.split(":");
        args = (String[]) Arrays.stream(args).map(value->value.replace("&c",":").replace("&a", "&")).toArray(String[]::new);
        TransmissionType type = TransmissionType.valueOf(args[0]);
        ArrayList<String> argsList = new ArrayList<>(Arrays.asList(args));
        argsList.remove(0);
        return new Transmission(type, argsList.toArray(new String[0]));
    }

    public enum TransmissionType implements Serializable{
        MESSAGE,
        SYSTEM,
        NAMECHANGE,
        USERLIST,
        LEAVE,
        JOIN,
        RQ_DISCONNECT,
        RQ_NICK,
        RQ_LIST,
        RQ_SEND
    }
}
