package win.lioil.bluetooth.bean;

public class Student{


    private String id;
    private String name;
    private String gender;
    private String handlerId;
    private String ledId;
    private String grade;


    public String getHandlerId() {
        return handlerId;
    }

    public void setHandlerId(String handlerId) {
        this.handlerId = handlerId;
    }

    public String getLedId() {
        return ledId;
    }

    public void setLedId(String ledId) {
        this.ledId = ledId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return
                "<"+id +
                "," + name +
                "," + gender +
                "," + handlerId+">"
                ;
    }
    public String toString1() {
        return
                "("+ ledId+")";
    }

    public String toStringWithGrade() {
        return
                "<"+id +
                        "," + name +
                        "," + gender +
                        "," + grade +
                        "," + handlerId+">"
                ;
    }
}
