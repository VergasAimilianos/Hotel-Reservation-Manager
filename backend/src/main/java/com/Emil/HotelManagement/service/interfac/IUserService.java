package com.Emil.HotelManagement.service.interfac;

import com.Emil.HotelManagement.dto.LoginRequest;
import com.Emil.HotelManagement.dto.Response;
import com.Emil.HotelManagement.entity.User;

public interface IUserService {

    Response register(User user);

    Response login(LoginRequest logingRequest);

    Response getAllUsers();

    Response getUserBookingHistory(String userId);

    Response deleteUser(String userId);

    Response getUserById(String userId);

    Response getMyInfo(String email);


}
