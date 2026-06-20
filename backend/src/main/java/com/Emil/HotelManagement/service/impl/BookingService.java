package com.Emil.HotelManagement.service.impl;

import com.Emil.HotelManagement.dto.BookingDTO;
import com.Emil.HotelManagement.dto.Response;
import com.Emil.HotelManagement.entity.Booking;
import com.Emil.HotelManagement.entity.Room;
import com.Emil.HotelManagement.entity.User;
import com.Emil.HotelManagement.exception.MyException;
import com.Emil.HotelManagement.repo.BookingRepository;
import com.Emil.HotelManagement.repo.RoomRepository;
import com.Emil.HotelManagement.repo.UserRepository;
import com.Emil.HotelManagement.service.interfac.IBookingService;
import com.Emil.HotelManagement.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


@Service

public class BookingService implements IBookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomService roomService;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Response saveBooking(Long roomId, Long userId, Booking bookingRequest) {
        Response response = new Response();

        try{
            if(bookingRequest.getCheckOutDate().isBefore(bookingRequest.getCheckInDate())){
                throw new IllegalArgumentException("Check Out Date cannot be before Check In Date");
            }

            Room room = roomRepository.findById(roomId).orElseThrow(() -> new MyException("Room Not Found"));
            User user = userRepository.findById(userId).orElseThrow(() -> new MyException("User Not Found"));
            List<Booking> existingBookings = room.getBookings();

            if(!roomIsAvailable(bookingRequest, existingBookings)){
                throw new MyException("Room Not Available");
            }

            bookingRequest.setRoom(room);
            bookingRequest.setUser(user);
            String confirmationCode = Utils.generateConfirmationCode(10);
            bookingRequest.setBookingConfirmationCode(confirmationCode);
            bookingRepository.save(bookingRequest);

            response.setStatusCode(200);
            response.setMessage("Booking Saved Successfully");
            response.setBookingConfirmationCode(confirmationCode);


        }catch(MyException e){
            response.setStatusCode(404);
            response.setMessage(e.getMessage());

        }catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error saving booking "+ e.getMessage());

        }

        return response;
    }



    @Override
    public Response findBookingByConfirmationCode(String bookingConfirmationCode) {
        Response response = new Response();

        try{
            Booking booking = bookingRepository.findByBookingConfirmationCode(bookingConfirmationCode).orElseThrow(() -> new MyException("Booking Not Found"));
            BookingDTO bookingDTO = Utils.mapBookingEntityToBookingDTOPlusBookedRoom(booking, true);

            response.setStatusCode(200);
            response.setMessage("Booking found!");
            response.setBooking(bookingDTO);


        }catch(MyException e){
            response.setStatusCode(404);
            response.setMessage(e.getMessage());

        }catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error finding booking "+ e.getMessage());

        }

        return response;
    }

    @Override
    public Response getAllBookings() {
        Response response = new Response();

        try{
            List<Booking> bookingList = bookingRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
            List<BookingDTO> bookingDTOList = Utils.mapBookingListEntityToBookingListDTO(bookingList);

            response.setStatusCode(200);
            response.setMessage("Bookings found!");
            response.setBookingDTOList(bookingDTOList);


        }catch(MyException e){
            response.setStatusCode(404);
            response.setMessage(e.getMessage());

        }catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error finding bookings "+ e.getMessage());

        }

        return response;
    }

    @Override
    public Response cancelBooking(Long bookingId) {
        Response response = new Response();

        try{
            bookingRepository.findById(bookingId).orElseThrow(() -> new MyException("Booking Not Found"));
            bookingRepository.deleteById(bookingId);

            response.setStatusCode(200);
            response.setMessage("Booking deleted!");


        }catch(MyException e){
            response.setStatusCode(404);
            response.setMessage(e.getMessage());

        }catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error cancelling booking "+ e.getMessage());

        }

        return response;
    }

    private boolean roomIsAvailable(Booking bookingRequest, List<Booking> existingBookings) {
        return existingBookings.stream().noneMatch(existing -> overlaps(bookingRequest, existing));
    }

    private boolean overlaps(Booking requested, Booking existing) {
        LocalDate requestedCheckInDate  = requested.getCheckInDate();
        LocalDate requestedCheckOutDate = requested.getCheckOutDate();
        LocalDate existingCheckInDate   = existing.getCheckInDate();
        LocalDate existingCheckOutDate  = existing.getCheckOutDate();

        // Two ranges overlap if: requestedIn < existingOut AND existingIn < requestedOut
        return requestedCheckInDate.isBefore(existingCheckOutDate) && existingCheckInDate.isBefore(requestedCheckOutDate);
    }
}
