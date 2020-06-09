package mag.ir.mimchat.Models;

public class Message {
    private String from, message, type, date, time, to, fileName, messageId;

    public Message() {
    }

    public String getFileName() {
        return fileName;
    }

    public String getMessageId() {
        return messageId;
    }

    public Message(String from, String to, String message, String type, String date, String time, String fileName, String messageId) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.date = date;
        this.time = time;
        this.to = to;
        this.fileName = fileName;
        this.messageId = messageId;
    }

    public String getTo() {
        return to;
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
