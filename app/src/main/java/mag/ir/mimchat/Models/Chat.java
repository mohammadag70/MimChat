package mag.ir.mimchat.Models;

public class Chat {
    String message;
    String name;
    String date;
    String time;
    String uid;

    public Chat(String message, String name, String date, String time, String uid) {
        this.message = message;
        this.name = name;
        this.date = date;
        this.time = time;
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public String getMessage() {
        return message;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }
}
