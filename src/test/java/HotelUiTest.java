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

    private Parent root() { return stage.getScene().getRoot(); }
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

    @Test
    void originalScreensRecordsAndNavigationArePreserved() throws Exception {
        fx(() -> {
            assertEquals("10", text("total-rooms"));
            assertEquals("7", text("available-rooms"));
            assertEquals("1", text("occupied-rooms"));
            assertEquals(Ui.money(14000), text("revenue"));
            click("nav-rooms");
            assertEquals(10, table("rooms-table").getItems().size());
            assertEquals(List.of("Room Number", "Room Type", "Rate", "Status"),
                    table("rooms-table").getColumns().stream().map(TableColumn::getText).toList());
            click("nav-reservations");
            assertEquals(5, table("reservations-table").getItems().size());
            assertEquals(7, table("reservations-table").getColumns().size());
            click("nav-reception");
            assertEquals(5, table("reception-table").getItems().size());
            click("nav-payments");
            assertNotNull(root().lookup("#record-payment"));
            assertTrue(root().lookup("#nav-payments").getPseudoClassStates()
                    .contains(PseudoClass.getPseudoClass("active")));
            click("nav-reports");
            assertTrue(labels(root()).contains("Reserved Rooms: 2"));
            RoomTable standalone = new RoomTable();
            TableView<?> original = (TableView<?>) standalone.lookup("#room-information-table");
            assertEquals(5, original.getItems().size());
            assertTrue(original.isEditable());
        });
    }

    @Test
    void bookingUpdatesTablesDashboardAndReports() throws Exception {
        fx(() -> {
            click("nav-reservations");
            booking("101");
            message("Reservation created. ID: 1006");
            click("book-reservation");
            assertEquals(6, table("reservations-table").getItems().size());
            assertEquals("Migration Test", HotelData.findReservation(1006).guest);
            assertEquals("Reserved", HotelData.findRoom(101).status);
            click("nav-dashboard");
            assertEquals("6", text("available-rooms"));
            click("nav-reports");
            assertTrue(labels(root()).contains("Reserved Rooms: 3"));
            click("nav-reservations");
            booking("101");
            message("That room is not available.");
            click("book-reservation");
            assertEquals(6, HotelData.reservations.size());
        });
    }

    @Test
    void cancellationUsesSelectedObjectAfterSortingAndProtectsActiveStays() throws Exception {
        fx(() -> {
            click("nav-reservations");
            TableView<HotelData.Reservation> table = table("reservations-table");
            TableColumn<HotelData.Reservation, ?> idColumn = table.getColumns().getFirst();
            idColumn.setSortType(TableColumn.SortType.DESCENDING);
            table.getSortOrder().add(idColumn);
            table.sort();
            table.getSelectionModel().select(HotelData.findReservation(1004));
            message("Reservation cancelled.");
            click("cancel-reservation");
            assertEquals("Cancelled", HotelData.findReservation(1004).status);
            assertEquals("Available", HotelData.findRoom(107).status);
            assertEquals(1005, table.getItems().getFirst().id);
            assertEquals(1004, table.getSelectionModel().getSelectedItem().id);
            assertEquals("Reserved", HotelData.findReservation(1001).status);
            table.getSelectionModel().select(HotelData.findReservation(1002));
            message("This reservation can no longer be cancelled.");
            click("cancel-reservation");
            assertEquals("Checked In", HotelData.findReservation(1002).status);
        });
    }

    @Test
    void checkInCheckOutAndPaymentsKeepRoomAndRevenueInSync() throws Exception {
        fx(() -> {
            click("nav-payments");
            TableView<HotelData.Reservation> table = table("reception-table");
            table.getSelectionModel().select(HotelData.findReservation(1001));
            message("Check In completed.");
            click("check-in");
            assertEquals("Occupied", HotelData.findRoom(102).status);
            assertEquals("Checked In", HotelData.findReservation(1001).status);
            input("1250");
            message("Payment recorded successfully.");
            click("record-payment");
            assertEquals(3250, HotelData.findReservation(1001).payment);
            assertEquals(15250, HotelData.getRevenue());
            message("Check Out completed.");
            click("check-out");
            assertEquals("Available", HotelData.findRoom(102).status);
            message("Only checked-in guests can check out.");
            click("check-out");
            table.getSelectionModel().select(HotelData.findReservation(1005));
            message("Cannot record payment for a cancelled reservation.");
            click("record-payment");
            click("nav-dashboard");
            assertEquals("8", text("available-rooms"));
            assertEquals(Ui.money(15250), text("revenue"));
        });
    }

    @Test
    void addRoomAndUpdateRateUseNativeDialogsAndSortedSelection() throws Exception {
        fx(() -> {
            click("nav-rooms");
            input("101");
            message("Room number already exists.");
            click("add-room");
            input("111"); input("Single"); input("1800");
            message("Room added successfully.");
            click("add-room");
            assertEquals(11, HotelData.rooms.size());
            assertEquals(1800, HotelData.findRoom(111).rate);
            TableView<HotelData.Room> table = table("rooms-table");
            TableColumn<HotelData.Room, ?> number = table.getColumns().getFirst();
            number.setSortType(TableColumn.SortType.DESCENDING);
            table.getSortOrder().add(number);
            table.sort();
            table.getSelectionModel().select(HotelData.findRoom(101));
            input("1750"); message("Room rate updated.");
            click("update-rate");
            assertEquals(1750, HotelData.findRoom(101).rate);
            assertEquals(1800, HotelData.findRoom(111).rate);
            click("nav-dashboard");
            assertEquals("11", text("total-rooms"));
            assertEquals("8", text("available-rooms"));
        });
    }

    @Test
    void cancelledDialogsMissingSelectionsAndInvalidAmountsDoNotChangeData() throws Exception {
        fx(() -> {
            click("nav-rooms");
            message("Please select a room."); click("update-rate");
            DIALOGS.add(pane -> press(pane, ButtonType.CANCEL)); click("add-room");
            input("111"); input("Single");
            DIALOGS.add(pane -> press(pane, ButtonType.CANCEL)); click("add-room");
            assertEquals(10, HotelData.rooms.size());
            table("rooms-table").getSelectionModel().select(0);
            for (String invalid : List.of("-1", "NaN", "Infinity", "abc")) {
                input(invalid); message("Enter a valid amount."); click("update-rate");
            }
            assertEquals(1500, HotelData.findRoom(101).rate);
            click("nav-reservations");
            message("Select a reservation first."); click("cancel-reservation");
            DIALOGS.add(pane -> press(pane, ButtonType.CANCEL)); click("book-reservation");
            assertEquals(5, HotelData.reservations.size());
            click("nav-reception");
            message("Please select a reservation."); click("check-in");
            table("reception-table").getSelectionModel().select(0);
            for (String invalid : List.of("0", "-1", "NaN", "Infinity", "abc")) {
                input(invalid); message("Enter a valid payment amount."); click("record-payment");
            }
            assertEquals(14000, HotelData.getRevenue());
            DIALOGS.add(pane -> press(pane, ButtonType.NO)); click("nav-exit");
            assertTrue(stage.isShowing());
        });
    }

    @Test
    void renderEveryScreenAtDefaultAndMinimumWindowSizes() throws Exception {
        fx(() -> {
            for (String page : List.of("Dashboard", "Rooms", "Reservations", "Reception", "Reports")) {
                app.showPage(page);
                snapshot("default-" + page.toLowerCase(), root());
            }
            stage.setWidth(1000);
            stage.setHeight(620);
        });
        // A separate JavaFX event lets the resize propagate before the second review.
        fx(() -> {
            for (String page : List.of("Dashboard", "Rooms", "Reservations", "Reception", "Reports")) {
                app.showPage(page);
                snapshot("minimum-" + page.toLowerCase(), root());
            }
            app.showPage("Reservations");
            DIALOGS.add(pane -> {
                snapshot("reservation-dialog", pane);
                press(pane, ButtonType.CANCEL);
            });
            click("book-reservation");
        });
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
