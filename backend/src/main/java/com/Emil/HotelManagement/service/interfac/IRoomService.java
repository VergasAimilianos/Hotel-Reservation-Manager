package com.Emil.HotelManagement.service.interfac;

import com.Emil.HotelManagement.dto.Response;
import com.Emil.HotelManagement.entity.Room;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface IRoomService {

    Response  addNewRoom(MultipartFile photo, String roomType, BigDecimal roomPrice, String roomDescription);
    List<String> getAllRoomTypes();
    Response getAllRooms();
    Response deleteRoom(Long roomId);
    Response updateRoom(Long roomId, MultipartFile photo,String roomType, BigDecimal roomPrice, String roomDescription);
    Response getRoomById(Long roomId);
    Response getAvailableRoomsByDateAndType(LocalDate checkInDate, LocalDate checkOutDate, String roomType);
    Response getAllAvailableRooms();



}
