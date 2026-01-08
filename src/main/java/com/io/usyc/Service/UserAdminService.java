package com.io.usyc.Service;

import com.io.usyc.Dto.*;

import java.util.List;

public interface UserAdminService {
    UserCreateRes createUser(UserCreateReq req);
    void changePassword(Long userId, PasswordChangeReq req);

    List<UserListItemRes> listUsers(Integer plantelId, Boolean active, String roleCode, String q);

    UserRes updateUser(Long userId, UserUpdateReq req);

}

