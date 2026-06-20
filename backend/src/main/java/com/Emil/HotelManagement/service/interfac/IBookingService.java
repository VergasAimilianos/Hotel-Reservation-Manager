package com.Emil.HotelManagement.service.interfac;


import com.Emil.HotelManagement.dto.Response;
import com.Emil.HotelManagement.entity.Booking;

public interface IBookingService {

    Response saveBooking(Long roomId, Long userId, Booking bookingRequest);

    Response findBookingByConfirmationCode(String bookingConfirmationCode);

    Response getAllBookings();
    Response cancelBooking(Long bookingId);
}
