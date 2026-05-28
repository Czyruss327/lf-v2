# PUPSRC Lost and Found – UI Fix Package

## Two bugs fixed in this update

### Bug 1 – Window can't be maximized
**Cause:** `primaryStage.setResizable(false)` was set in App.java.
**Fix:** See `App_fix.java` — set `setResizable(true)` and add min width/height instead.

### Bug 2 – Burger menu didn't show the full-height drawer
**Cause:** The Navbar was in the `<top>` of a BorderPane, so its StackPane was only
54 px tall. The overlay and drawer were clipped to that strip.
**Fix:** Navbar.fxml is now a plain HBox (just the bar). NavbarController creates the
overlay + drawer in Java and injects them into the PAGE's root StackPane via:
    navbarController.attachDrawerTo(rootStack);
This means the drawer covers the full window height correctly.

---

## File structure

```
pupsrc/
├── fxml/
│   ├── Navbar.fxml            ← Plain HBox bar only (no drawer in FXML)
│   ├── Account.fxml           ← Root is now StackPane (fx:id="rootStack")
│   └── AddAdmin.fxml          ← Root is now StackPane (fx:id="rootStack")
├── controller/
│   ├── NavbarController.java  ← Builds drawer in Java; attachDrawerTo(rootStack)
│   ├── AccountController.java ← Calls attachDrawerTo in Platform.runLater
│   └── AddAdminController.java
├── model/
│   ├── Admin.java
│   ├── AdminStore.java
│   └── HistoryEntry.java
├── css/
│   └── style.css
└── App_fix.java               ← Patch for App.java start() method
```

## Adding the drawer to a NEW page

1. Make the page root a StackPane with fx:id="rootStack"
2. Add `<fx:include fx:id="navbar" source="Navbar.fxml"/>` inside a nested BorderPane top
3. In your controller:
   ```java
   @FXML private StackPane      rootStack;
   @FXML private NavbarController navbarController;

   @Override
   public void initialize(URL url, ResourceBundle rb) {
       Platform.runLater(() -> {
           navbarController.attachDrawerTo(rootStack);
           navbarController.setNavigationHandler(this::onNavigate);
       });
   }
   ```

## Default dev credentials
Username: admin | Password: admin123
