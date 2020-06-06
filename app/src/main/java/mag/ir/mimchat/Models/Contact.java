package mag.ir.mimchat.Models;

public class Contact {

    public Contact() {
    }

    String name = "";
    String image = "";
    String status = "";
    String uid = "";
    String background = "";

    public Contact(String name, String image, String status, String uid, String background) {
        this.name = name;
        this.image = image;
        this.status = status;
        this.uid = uid;
        this.background = background;
    }

    public String getBackground() {
        return background;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getStatus() {
        return status;
    }

    public String getUid() {
        return uid;
    }
}
