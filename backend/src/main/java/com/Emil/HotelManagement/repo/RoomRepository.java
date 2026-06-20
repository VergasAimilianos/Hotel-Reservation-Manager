package com.Emil.HotelManagement.repo;

import com.Emil.HotelManagement.entity.Booking;
import com.Emil.HotelManagement.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("SELECT DISTINCT r.roomType FROM Room r")
    List<String> findDistinctRoomType();

    @Query("SELECT r FROM Room r WHERE r.id NOT IN (SELECT b.room.id FROM Booking b)")
    List<Room> getAllAvailableRooms();

    @Query("SELECT r from Room r WHERE r.roomType LIKE %:roomType% AND r.id NOT IN (SELECT bk.room.id FROM Booking bk WHERE"+ "(bk.checkInDate <= :checkOutDate) AND (bk.checkOutDate >= :checkInDate))") // finds all room types that contain the :roomType and excludes those that are booked during the checkInDate-CheckOutDate
    List<Room> findAvailableRoomsByDateAndType(LocalDate checkInDate,LocalDate checkOutDate, String roomType);
    /*
    @Query("""
    SELECT r
    FROM Room r
    WHERE r.roomType LIKE CONCAT('%', :roomType, '%')
    AND NOT EXISTS (
    SELECT 1
    FROM Booking bk
    WHERE bk.room = r
    AND bk.checkInDate <= :checkOutDate
    AND bk.checkOutDate >= :checkInDate
    )
    """)
    Better performance on larger databases ?
     */

}
