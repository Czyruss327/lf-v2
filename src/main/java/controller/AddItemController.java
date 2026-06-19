package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Item;

import java.io.File;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * AddItemController
 * Handles the "Report an Item" form.
 */
public class AddItemController implements Initializable {

    @FXML
    private TextField nameField;
    @FXML
    private RadioButton lostRadio;
    @FXML
    private RadioButton foundRadio;
    @FXML
    private ToggleGroup statusGroup;
    @FXML
    private TextField colorField;
    @FXML
    private DatePicker datePicker;
    @FXML
    private ComboBox<String> locationCombo;
    @FXML
    private TextField imagePathField;
    @FXML
    private Label errorLabel;

    private Consumer<Item> onItemAdded;
    private File selectedImageFile;

    private static int nextId = 100; // auto-increment for new items

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        errorLabel.setText("");
    }

    /** Callback fired when a new item is successfully submitted. */
    public void setOnItemAdded(Consumer<Item> callback) {
        this.onItemAdded = callback;
    }

    @FXML
    private void onBrowseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Item Photo");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"));
        Stage stage = (Stage) nameField.getScene().getWindow();
        selectedImageFile = chooser.showOpenDialog(stage);
        if (selectedImageFile != null) {
            imagePathField.setText(selectedImageFile.getAbsolutePath());
        }
    }

    @FXML
    private void onSubmit() {
        errorLabel.setText("");

        // Validation
        if (nameField.getText().isBlank()) {
            errorLabel.setText("Item name is required.");
            return;
        }
        if (statusGroup.getSelectedToggle() == null) {
            errorLabel.setText("Please select Unclaimed or Claimed.");
            return;
        }
        if (datePicker.getValue() == null) {
            errorLabel.setText("Date is required.");
            return;
        }
        if (locationCombo.getValue() == null) {
            errorLabel.setText("Please select a location.");
            return;
        }

        // Build item
        Item.Status status = (statusGroup.getSelectedToggle() == lostRadio)
                ? Item.Status.LOST
                : Item.Status.FOUND;

        String dateStr = datePicker.getValue()
                .format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));

        String imgPath = selectedImageFile != null
                ? selectedImageFile.toURI().toString()
                : "";

        Item newItem = new Item(
                nextId++,
                nameField.getText().trim(),
                status,
                colorField.getText().trim(),
                dateStr,
                locationCombo.getValue(),
                imgPath);

        if (onItemAdded != null)
            onItemAdded.accept(newItem);
        onClose();
    }

    @FXML
    private void onClose() {
        ((Stage) nameField.getScene().getWindow()).close();
    }
}
