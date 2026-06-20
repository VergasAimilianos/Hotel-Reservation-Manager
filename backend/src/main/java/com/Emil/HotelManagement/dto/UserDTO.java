package com.Emil.HotelManagement.dto;

import com.Emil.HotelManagement.entity.Booking;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)

public class UserDTO {

    private Long id;
    private String email;
    private String phoneNumber;
    private String name;
    private String role;
    private List<BookingDTO> bookings = new ArrayList<>();

}
