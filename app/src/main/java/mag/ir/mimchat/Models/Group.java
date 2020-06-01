package mag.ir.mimchat.Models;

public class Group {

    String name;
    String date;
    String time;
    String uid;
    String username;

    public Group() {
    }

    public Group(String name, String date, String time, String uid, String username) {
        this.name = name;
        this.date = date;
        this.time = time;
        this.uid = uid;
        this.username = username;
    }

    public Group(String name, String date, String time, String uid) {
        this.name = name;
        this.date = date;
        this.time = time;
        this.uid = uid;
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

    public String getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }
}
