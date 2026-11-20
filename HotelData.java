import java.util.ArrayList;

public class HotelData {

    public static class Room {
        int number;
        String type;
        double rate;
        String status;

        Room(int number, String type, double rate, String status) {
            this.number = number;
            this.type = type;
            this.rate = rate;
            this.status = status;
        }
    }

    public static class Reservation {
        int id;
        String guest;
        String contact;
        int roomNumber;
        String checkIn;
        String checkOut;
        String status;
        double payment;

        Reservation(int id, String guest, String contact, int roomNumber,
                    String checkIn, String checkOut) {
            this.id = id;
            this.guest = guest;
            this.contact = contact;
            this.roomNumber = roomNumber;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.status = "Reserved";
            this.payment = 0;
        }
    }

    public static ArrayList<Room> rooms = new ArrayList<>();
    public static ArrayList<Reservation> reservations = new ArrayList<>();
    public static int nextReservationId = 1001;

    static {
        rooms.add(new Room(101, "Single", 1500, "Available"));
        rooms.add(new Room(102, "Double", 2000, "Reserved"));
        rooms.add(new Room(103, "Deluxe", 3500, "Available"));
        rooms.add(new Room(104, "Suite", 5000, "Occupied"));
        rooms.add(new Room(105, "Single", 1500, "Available"));
        rooms.add(new Room(106, "Double", 2000, "Available"));
        rooms.add(new Room(107, "Deluxe", 3500, "Reserved"));
        rooms.add(new Room(108, "Suite", 5000, "Available"));
        rooms.add(new Room(109, "Single", 1500, "Available"));
        rooms.add(new Room(110, "Double", 2000, "Available"));

        addReservation(1001, "Alex Morgan", "0917-555-0101", 102,
                "2026-11-20", "2026-11-23", "Reserved", 2000);
        addReservation(1002, "Jamie Rivera", "0917-555-0102", 104,
                "2026-11-19", "2026-11-22", "Checked In", 5000);
        addReservation(1003, "Taylor Santos", "0917-555-0103", 103,
                "2026-11-15", "2026-11-18", "Checked Out", 7000);
        addReservation(1004, "Casey Reyes", "0917-555-0104", 107,
                "2026-11-21", "2026-11-24", "Reserved", 0);
        addReservation(1005, "Jordan Cruz", "0917-555-0105", 105,
                "2026-11-10", "2026-11-12", "Cancelled", 0);
        nextReservationId = 1006;
    }

    private static void addReservation(int id, String guest, String contact, int roomNumber,
                                       String checkIn, String checkOut, String status, double payment) {
        Reservation reservation = new Reservation(
                id, guest, contact, roomNumber, checkIn, checkOut
        );
        reservation.status = status;
        reservation.payment = payment;
        reservations.add(reservation);
    }

    public static Room findRoom(int number) {
        for (Room room : rooms) {
            if (room.number == number) return room;
        }
        return null;
    }

    public static Reservation findReservation(int id) {
        for (Reservation reservation : reservations) {
            if (reservation.id == id) return reservation;
        }
        return null;
    }

    public static int countRooms(String status) {
        int count = 0;
        for (Room room : rooms) {
            if (room.status.equals(status)) count++;
        }
        return count;
    }

    public static double getRevenue() {
        double total = 0;
        for (Reservation reservation : reservations) {
            total += reservation.payment;
        }
        return total;
    }
}
