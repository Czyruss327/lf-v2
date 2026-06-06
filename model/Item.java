package model;

import javafx.beans.property.*;

/**
 * Model class representing a Lost or Found item.
 */
public class Item {

    public enum Status {
        LOST, FOUND
    }

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final ObjectProperty<Status> status = new SimpleObjectProperty<>();
    private final StringProperty color = new SimpleStringProperty();
    private final StringProperty date = new SimpleStringProperty();
    private final StringProperty location = new SimpleStringProperty();
    private final StringProperty imagePath = new SimpleStringProperty();

    // Reporter info
    private final StringProperty reporterName = new SimpleStringProperty();
    private final StringProperty studentId = new SimpleStringProperty();
    private final StringProperty contactNumber = new SimpleStringProperty();
    private final StringProperty category = new SimpleStringProperty();
    private final StringProperty dateFound = new SimpleStringProperty();

    public Item() {
    }

    public Item(int id, String name, Status status, String color,
            String date, String location, String imagePath) {
        setId(id);
        setName(name);
        setStatus(status);
        setColor(color);
        setDate(date);
        setLocation(location);
        setImagePath(imagePath);
    }

    // id
    public int getId() {
        return id.get();
    }

    public void setId(int v) {
        id.set(v);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    // name
    public String getName() {
        return name.get();
    }

    public void setName(String v) {
        name.set(v);
    }

    public StringProperty nameProperty() {
        return name;
    }

    // status
    public Status getStatus() {
        return status.get();
    }

    public void setStatus(Status v) {
        status.set(v);
    }

    public ObjectProperty<Status> statusProperty() {
        return status;
    }

    public String getStatusLabel() {
        return status.get() == Status.LOST ? "UNCLAIMED" : "CLAIMED";
    }

    // color / description
    public String getColor() {
        return color.get();
    }

    public void setColor(String v) {
        color.set(v);
    }

    public StringProperty colorProperty() {
        return color;
    }

    // date
    public String getDate() {
        return date.get();
    }

    public void setDate(String v) {
        date.set(v);
    }

    public StringProperty dateProperty() {
        return date;
    }

    // location
    public String getLocation() {
        return location.get();
    }

    public void setLocation(String v) {
        location.set(v);
    }

    public StringProperty locationProperty() {
        return location;
    }

    // imagePath
    public String getImagePath() {
        return imagePath.get();
    }

    public void setImagePath(String v) {
        imagePath.set(v);
    }

    public StringProperty imagePathProperty() {
        return imagePath;
    }

    // reporterName
    public String getReporterName() {
        return reporterName.get();
    }

    public void setReporterName(String v) {
        reporterName.set(v);
    }

    public StringProperty reporterNameProperty() {
        return reporterName;
    }

    // studentId
    public String getStudentId() {
        return studentId.get();
    }

    public void setStudentId(String v) {
        studentId.set(v);
    }

    public StringProperty studentIdProperty() {
        return studentId;
    }

    // contactNumber
    public String getContactNumber() {
        return contactNumber.get();
    }

    public void setContactNumber(String v) {
        contactNumber.set(v);
    }

    public StringProperty contactNumberProperty() {
        return contactNumber;
    }

    // category
    public String getCategory() {
        return category.get();
    }

    public void setCategory(String v) {
        category.set(v);
    }

    public StringProperty categoryProperty() {
        return category;
    }

    // dateFound
    public String getDateFound() {
        return dateFound.get();
    }

    public void setDateFound(String v) {
        dateFound.set(v);
    }

    public StringProperty dateFoundProperty() {
        return dateFound;
    }
}
