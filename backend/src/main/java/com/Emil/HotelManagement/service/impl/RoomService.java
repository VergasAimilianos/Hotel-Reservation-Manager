package com.Emil.HotelManagement.service.impl;

import com.Emil.HotelManagement.dto.Response;
import com.Emil.HotelManagement.dto.RoomDTO;
import com.Emil.HotelManagement.entity.Room;
import com.Emil.HotelManagement.exception.MyException;
import com.Emil.HotelManagement.repo.BookingRepository;
import com.Emil.HotelManagement.repo.RoomRepository;
import com.Emil.HotelManagement.service.AwsS3Service;
import com.Emil.HotelManagement.service.interfac.IRoomService;
import com.Emil.HotelManagement.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class RoomService implements IRoomService {

    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private AwsS3Service awsS3Service;


    @Override
    public Response addNewRoom(MultipartFile photo, String roomType, BigDecimal roomPrice, String roomDescription) {
        Response response = new Response();

        try {
            String imageUrl = awsS3Service.saveImageToS3(photo);
            Room room = new Room();
            room.setRoomPhotoUrl(imageUrl);
            room.setRoomType(roomType);
            room.setRoomPrice(roomPrice);
            room.setRoomDescription(roomDescription);
            Room savedRoom = roomRepository.save(room);
            RoomDTO roomDTO = Utils.mapRoomEntityToRoomDTO(savedRoom);

            response.setStatusCode(200);
            response.setMessage("Room added");
            response.setRoom(roomDTO);

        }catch(Exception e){
            response.setStatusCode(500);
            response.setMessage("Error adding a room "+e.getMessage());
        }

        return response;
    }

    @Override
    public List<String> getAllRoomTypes() {

        return roomRepository.findDistinctRoomType();
    }

    @Override
    public Response getAllRooms() {
        Response response = new Response();

        try {
            List<Room> roomList = roomRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
            List<RoomDTO> roomDTOList = Utils.mapRoomListEntityToRoomListDTO(roomList);

            response.setStatusCode(200);
            response.setMessage("Rooms retrieved");
            response.setRoomDTOList(roomDTOList);

        }catch(Exception e){
            response.setStatusCode(500);
            response.setMessage("Error getting rooms "+e.getMessage());
        }

        return response;
    }

    @Override
    public Response deleteRoom(Long roomId) {
        Response response = new Response();

        try {
            roomRepository.findById(roomId).orElseThrow(() -> new MyException("Room id " + roomId + " not found"));
            roomRepository.deleteById(roomId);

            response.setStatusCode(200);
            response.setMessage("Room deleted");

        }catch(MyException e){
            response.setStatusCode(404);
            response.setMessage(e.getMessage());

        }catch(Exception e){
            response.setStatusCode(500);
            response.setMessage("Error deleting room "+e.getMessage());
        }

        return response;
    }

    @Override
    public Response updateRoom(Long roomId, MultipartFile photo, String roomType, BigDecimal roomPrice, String roomDescription) {
        Response response = new Response();

        try {
            String imageUrl = null;
            if(photo!=null && !photo.isEmpty()){
                imageUrl = awsS3Service.saveImageToS3(photo);

            }
            Room room = roomRepository.findById(roomId).orElseThrow(() -> new MyException("Room ID: " + roomId + " not found"));

            if(roomType!=null) room.setRoomType(roomType);
            if(roomPrice!=null) room.setRoomPrice(roomPrice);
            if(roomDescription!=null) room.setRoomDescription(roomDescription);
            if(imageUrl!=null) room.setRoomPhotoUrl(imageUrl);

            Room updatedRoom = roomRepository.save(room);
            RoomDTO roomDTO = Utils.mapRoomEntityToRoomDTO(updatedRoom);

            response.setStatusCode(200);
            response.setMessage("Room updated");
            response.setRoom(roomDTO);


        }catch(MyException e){
            response.setStatusCode(404);
            response.setMessage(e.getMessage());

        }catch(Exception e){
            response.setStatusCode(500);
            response.setMessage("Error updating room "+e.getMessage());
        }

        return response;    }

    @Override
    public Response getRoomById(Long roomId) {
        Response response = new Response();

        try {
            Room room = roomRepository.findById(roomId).orElseThrow(() -> new MyException("Room ID: " + roomId + " not found"));
            RoomDTO roomDTO = Utils.mapRoomEntityToRoomDTOPlusBookings(room); // take the bookings as well

            response.setStatusCode(200);
            response.setMessage("Room found!");
            response.setRoom(roomDTO);

        }catch(MyException e){
            response.setStatusCode(404);
            response.setMessage(e.getMessage());

        }catch(Exception e){
            response.setStatusCode(500);
            response.setMessage("Error deleting room "+e.getMessage());
        }

        return response;
    }

    @Override
    public Response getAvailableRoomsByDateAndType(LocalDate checkInDate, LocalDate checkOutDate, String roomType) {
        Response response = new Response();

        try {
            List<Room> availableRooms = roomRepository.findAvailableRoomsByDateAndType(checkInDate, checkOutDate, roomType);
            List<RoomDTO> roomDTOList = Utils.mapRoomListEntityToRoomListDTO(availableRooms);

            response.setStatusCode(200);
            response.setMessage("Rooms found!");
            response.setRoomDTOList(roomDTOList);

        }catch(Exception e){
            response.setStatusCode(500);
            response.setMessage("Error finding rooms "+e.getMessage());
        }

        return response;
    }

    @Override
    public Response getAllAvailableRooms() {
        Response response = new Response();

        try {
            List<Room> roomList = roomRepository.getAllAvailableRooms();
            List<RoomDTO> roomDTOList = Utils.mapRoomListEntityToRoomListDTO(roomList);

            response.setStatusCode(200);
            response.setMessage("Rooms found!");
            response.setRoomDTOList(roomDTOList);

        }catch(Exception e){
            response.setStatusCode(500);
            response.setMessage("Error deleting room "+e.getMessage());
        }

        return response;    }
}
