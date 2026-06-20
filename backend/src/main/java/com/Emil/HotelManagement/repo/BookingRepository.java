package com.Emil.HotelManagement.repo;

import com.Emil.HotelManagement.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByRoomId(Long roomId);

    List<Booking> findByUserId(Long userId);

    Optional<Booking> findByBookingConfirmationCode(String bookingConfirmationCode);


}
