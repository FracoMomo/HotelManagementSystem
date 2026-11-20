import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/** Exercises the real JavaFX controls and dialogs against the original shared data. */
class HotelUiTest {
    private static final List<HotelData.Room> INITIAL_ROOMS = new ArrayList<>(HotelData.rooms);
    private static final List<HotelData.Reservation> INITIAL_RESERVATIONS = new ArrayList<>(HotelData.reservations);
    private static final Deque<Consumer<DialogPane>> DIALOGS = new ArrayDeque<>();
    private static final AtomicReference<Throwable> DIALOG_ERROR = new AtomicReference<>();
    private Main app;
    private Stage stage;

    @BeforeAll
    static void startToolkit() throws Exception {
        CountDownLatch ready = new CountDownLatch(1);
        Platform.startup(() -> {
            Platform.setImplicitExit(false);
            Window.getWindows().addListener((ListChangeListener<Window>) change -> {
                while (change.next()) {
                    for (Window window : change.getAddedSubList()) {
                        Platform.runLater(() -> handleDialog(window));
                    }
                }
            });
            ready.countDown();
        });
        assertTrue(ready.await(15, TimeUnit.SECONDS), "JavaFX did not start");
    }

    private static void handleDialog(Window window) {
        if (window.getScene() == null || !(window.getScene().getRoot() instanceof DialogPane pane)) return;
        try {
            assertFalse(DIALOGS.isEmpty(), "Unexpected dialog: " + pane.getContentText());
            DIALOGS.removeFirst().accept(pane);
        } catch (Throwable error) {
            DIALOG_ERROR.compareAndSet(null, error);
            window.hide();
        }
    }

    private static void fx(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> { action.run(); return null; });
        Platform.runLater(task);
        try {
            task.get(20, TimeUnit.SECONDS);
        } catch (ExecutionException failure) {
            if (failure.getCause() instanceof AssertionError assertion) throw assertion;
            throw failure;
        }
        Throwable dialogError = DIALOG_ERROR.getAndSet(null);
        if (dialogError != null) throw new AssertionError("Dialog interaction failed", dialogError);
    }

    @BeforeEach
    void openApplication() throws Exception {
        fx(() -> {
            DIALOGS.clear();
            HotelData.rooms.clear();
            for (HotelData.Room room : INITIAL_ROOMS) {
                HotelData.rooms.add(new HotelData.Room(room.number, room.type, room.rate, room.status));
            }
            HotelData.reservations.clear();
            for (HotelData.Reservation original : INITIAL_RESERVATIONS) {
                HotelData.Reservation copy = new HotelData.Reservation(original.id, original.guest,
                        original.contact, original.roomNumber, original.checkIn, original.checkOut);
                copy.status = original.status;
                copy.payment = original.payment;
                HotelData.reservations.add(copy);
            }
            HotelData.nextReservationId = 1006;
            app = new Main();
            stage = new Stage();
            app.start(stage);
        });
    }

    @AfterEach
    void closeApplication() throws Exception {
        fx(() -> {
            stage.hide();
            assertTrue(DIALOGS.isEmpty(), "Some expected dialogs were never shown");
        });
    }

    @AfterAll
    static void stopToolkit() {
        Platform.exit();
    }

    private Parent root() { Parent root = stage.getScene().getRoot(); root.applyCss(); root.layout(); return root; }
    private void click(String id) { ((Button) root().lookup("#" + id)).fire(); }
    @SuppressWarnings("unchecked")
    private <T> TableView<T> table(String id) { return (TableView<T>) root().lookup("#" + id); }
    private String text(String id) { return ((Label) root().lookup("#" + id)).getText(); }
    private static void press(DialogPane pane, ButtonType type) { ((Button) pane.lookupButton(type)).fire(); }
    private static void input(String text) {
        DIALOGS.add(pane -> {
            ((TextField) pane.lookup(".text-field")).setText(text);
            press(pane, ButtonType.OK);
        });
    }
    private static void message(String expected) {
        DIALOGS.add(pane -> {
            assertEquals(expected, pane.getContentText());
            press(pane, ButtonType.OK);
        });
    }
    private static void field(DialogPane pane, String id, String value) {
        ((TextField) pane.lookup("#" + id)).setText(value);
    }
    private static void booking(String room) {
        DIALOGS.add(pane -> {
            field(pane, "guest-name", "Migration Test");
            field(pane, "guest-contact", "0917-000-0000");
            field(pane, "reservation-room", room);
            field(pane, "check-in-date", "2026-11-25");
            field(pane, "check-out-date", "2026-11-27");
            press(pane, ButtonType.OK);
        });
    }

    private void login(boolean admin) {
        @SuppressWarnings("unchecked") ComboBox<String> role = (ComboBox<String>) root().lookup("#login-role");
        role.getSelectionModel().select(admin ? 1 : 0);
        ((TextField) root().lookup("#login-username")).setText(admin ? "admin" : "receptionist");
        ((TextField) root().lookup("#login-password")).setText(admin ? "admin123" : "reception123");
        click("staff-login");
    }

    @Test void loginAndRoleBoundaries() throws Exception {
        fx(() -> {
            assertNotNull(root().lookup("#login-screen"));
            click("staff-login");
            assertFalse(text("login-error").isBlank());
            assertNull(app.getRole());
            login(false);
            assertEquals(Role.RECEPTIONIST, app.getRole());
            assertNotNull(root().lookup("#nav-reservations"));
            assertNull(root().lookup("#nav-reports"));
            assertNull(root().lookup("#revenue"));
            click("nav-rooms");
            assertNull(root().lookup("#add-room"));
            assertNull(root().lookup("#update-rate"));
            app.showPage("Reports");
            assertNotNull(root().lookup("#guest-rooms-table"));
            click("nav-logout"); login(true);
            assertEquals(Role.ADMIN, app.getRole());
            assertNotNull(root().lookup("#revenue"));
            assertNull(root().lookup("#nav-reservations"));
            click("nav-rooms");
            assertNotNull(root().lookup("#add-room"));
            click("nav-reports");
            assertTrue(labels(root()).contains("Recorded Revenue: " + Ui.money(14000)));
            app.showPage("Reception");
            assertNull(root().lookup("#reception-table"));
            click("nav-logout"); click("guest-access");
            assertEquals(Role.CUSTOMER, app.getRole());
            assertNull(root().lookup("#nav-reception"));
            assertNull(root().lookup("#nav-reports"));
            assertNull(root().lookup("#nav-reservations"));
            app.showPage("Reservations");
            assertNull(root().lookup("#reservations-table"));
            click("nav-my-booking");
            assertNotNull(root().lookup("#lookup-email"));
            assertNull(root().lookup("#reservations-table"));
            assertTrue(root().lookup("#guest-edit").isDisabled());
        });
    }

    @Test void customerBookingRequiresEmailAndIdAndStaysPreviewOnly() throws Exception {
        fx(() -> {
            click("guest-access"); click("nav-book");
            click("review-booking");
            assertTrue(text("booking-feedback").contains("Complete the required fields"));
            ((TextField) root().lookup("#booking-name")).setText("Sample Guest");
            ((TextField) root().lookup("#booking-email")).setText("guest@example.com");
            ((ComboBox<?>) root().lookup("#booking-room")).getSelectionModel().selectFirst();
            ((DatePicker) root().lookup("#booking-arrival")).setValue(java.time.LocalDate.of(2026, 12, 1));
            ((DatePicker) root().lookup("#booking-departure")).setValue(java.time.LocalDate.of(2026, 12, 3));
            ((CheckBox) root().lookup("#booking-consent")).setSelected(true);
            click("review-booking");
            assertTrue(text("booking-feedback").contains("choose an ID"));
            root().lookup("#booking-id").setUserData(new java.io.File("sample-id.png"));
            ((TextField) root().lookup("#booking-email")).setText("invalid-email");
            click("review-booking");
            assertTrue(text("booking-feedback").contains("Complete the required fields"));
            ((TextField) root().lookup("#booking-email")).setText("guest@example.com");
            click("review-booking");
            assertTrue(text("booking-feedback").contains("Booking preview ready"));
            assertEquals(5, HotelData.reservations.size());
            click("nav-logout"); click("guest-access"); click("nav-book");
            assertEquals("", ((TextField) root().lookup("#booking-email")).getText());
            assertNull(root().lookup("#booking-id").getUserData());
        });
    }

    @Test void receptionExistingActionsAndNewPreviewForms() throws Exception {
        fx(() -> {
            login(false); click("nav-reservations");
            booking("101"); message("Reservation created. ID: 1006"); click("book-reservation");
            assertEquals(6, HotelData.reservations.size());
            table("reservations-table").getSelectionModel().select(0);
            DIALOGS.add(pane -> { assertNotNull(pane.lookup("#edit-departure")); press(pane, ButtonType.CLOSE); });
            click("extend-stay");
            click("nav-reception");
            table("reception-table").getSelectionModel().select(0);
            DIALOGS.add(pane -> { assertNotNull(pane.lookup("#fine-amount")); press(pane, ButtonType.CLOSE); });
            click("damage-fine");
            message("Check In completed."); click("check-in");
            input("1250"); message("Payment recorded successfully."); click("record-payment");
            assertEquals(3250, HotelData.findReservation(1001).payment);
        });
    }

    @Test void adminRoomActionsStillWork() throws Exception {
        fx(() -> {
            login(true); click("nav-rooms");
            input("111"); input("Single"); input("1800"); message("Room added successfully."); click("add-room");
            table("rooms-table").getSelectionModel().select(0);
            input("1750"); message("Room rate updated."); click("update-rate");
            assertEquals(1750, HotelData.findRoom(101).rate);
            assertEquals(11, HotelData.rooms.size());
        });
    }

    @Test void renderRoleScreensAtDefaultAndMinimumSizes() throws Exception {
        fx(() -> captureRoles("default"));
        fx(() -> { stage.setWidth(1000); stage.setHeight(680); });
        fx(() -> captureRoles("minimum"));
    }
    private void captureRoles(String size) {
        app.showLogin(); snapshot(size + "-login", root());
        for (Role role : Role.values()) {
            app.enterWorkspace(role);
            List<String> pages = switch (role) {
                case CUSTOMER -> List.of("Rooms", "Book", "MyBooking");
                case RECEPTIONIST -> List.of("Dashboard", "Rooms", "Reservations", "Reception");
                case ADMIN -> List.of("Dashboard", "Rooms", "Reports");
            };
            for (String page : pages) {
                app.showPage(page);
                snapshot(size + "-" + role.name().toLowerCase() + "-" + page.toLowerCase(), root());
            }
        }
    }

    private static Set<String> labels(Parent node) {
        Set<String> texts = new HashSet<>();
        for (Node child : node.getChildrenUnmodifiable()) {
            if (child instanceof Label label) texts.add(label.getText());
            if (child instanceof Parent parent) texts.addAll(labels(parent));
        }
        return texts;
    }

    private static void snapshot(String name, Parent node) {
        try {
            node.applyCss();
            node.layout();
            WritableImage image = node.snapshot(null, null);
            BufferedImage output = new BufferedImage((int) image.getWidth(), (int) image.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < output.getHeight(); y++) {
                for (int x = 0; x < output.getWidth(); x++) {
                    output.setRGB(x, y, image.getPixelReader().getArgb(x, y));
                }
            }
            Path directory = Path.of("target", "ui-review");
            Files.createDirectories(directory);
            ImageIO.write(output, "png", directory.resolve(name + ".png").toFile());
        } catch (Exception error) {
            throw new IllegalStateException("Could not capture " + name, error);
        }
    }
}
