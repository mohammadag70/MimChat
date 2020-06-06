package mag.ir.mimchat.Models;

public class Message {
    private String from, message, type, date, time;

    public Message() {
    }

    public Message(String from, String message, String type, String date, String time) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.date = date;
        this.time = time;
    }

    public String getFrom() {
        return from;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }
}
