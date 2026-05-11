package btvn.it210_project.utility;

import btvn.it210_project.model.entity.Genre;
import btvn.it210_project.model.entity.Room;
import btvn.it210_project.model.entity.Seat;
import btvn.it210_project.model.entity.User;
import btvn.it210_project.model.entity.UserProfile;
import btvn.it210_project.repository.GenreRepository;
import btvn.it210_project.repository.RoomRepository;
import btvn.it210_project.repository.SeatRepository;
import btvn.it210_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final GenreRepository genreRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final SeatRepository seatRepository; // Bắt buộc phải có để thao tác với Ghế

    @Override
    public void run(String... args) throws Exception {

        // 1. Tạo Thể loại
        if (genreRepository.count() == 0) {
            Genre g1 = new Genre();
            g1.setGenreName("Hành động");
            Genre g2 = new Genre();
            g2.setGenreName("Kinh dị");
            genreRepository.saveAll(List.of(g1, g2));
        }

        // 2. Tạo Phòng chiếu
        if (roomRepository.count() == 0) {
            Room r1 = new Room();
            r1.setRoomName("Cinema 01");
            r1.setCapacity(50);
            Room r2 = new Room();
            r2.setRoomName("Cinema 02");
            r2.setCapacity(100);
            roomRepository.saveAll(List.of(r1, r2));
        }

        // 3. Tạo Admin
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPasswordHash("123456");
            admin.setRole("ADMIN");
            admin.setStatus(true);

            UserProfile profile = new UserProfile();
            profile.setFullName("Quản Trị Viên Hệ Thống");
            profile.setEmail("admin@smartcinema.com");
            profile.setPhone("0999999999");

            profile.setUser(admin);
            admin.setUserProfile(profile);

            userRepository.save(admin);
        }

        // 4. TỰ ĐỘNG TẠO SƠ ĐỒ GHẾ (THỦ PHẠM LÀ ĐÂY)
        if (seatRepository.count() == 0) {
            List<Room> rooms = roomRepository.findAll();
            List<Seat> seatsToSave = new ArrayList<>();

            for (Room room : rooms) {
                char rowChar = 'A';
                int seatsPerRow = 10; // Cứ 10 ghế là 1 hàng

                // Tránh lỗi chia cho 0 nếu phòng lỡ chưa cài sức chứa
                int capacity = (room.getCapacity() != null && room.getCapacity() > 0) ? room.getCapacity() : 50;
                int totalRows = capacity / seatsPerRow;

                for (int i = 0; i < totalRows; i++) {
                    for (int j = 1; j <= seatsPerRow; j++) {
                        Seat seat = new Seat();
                        seat.setRoom(room);
                        seat.setSeatName(String.valueOf((char) (rowChar + i)) + j); // Ghép tên: A1, A2...
                        seatsToSave.add(seat);
                    }
                }
            }
            seatRepository.saveAll(seatsToSave);
            System.out.println("Đã tự động tạo sơ đồ ghế (A1, A2...) cho các phòng!");
        }
    }
}