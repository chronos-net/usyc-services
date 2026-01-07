package com.io.usyc.Service;

import com.io.usyc.Dto.PasswordChangeReq;
import com.io.usyc.Dto.UserCreateReq;
import com.io.usyc.Dto.UserCreateRes;
import com.io.usyc.Dto.UserListItemRes;

import java.util.List;

public interface UserAdminService {
    UserCreateRes createUser(UserCreateReq req);
    void changePassword(Long userId, PasswordChangeReq req);

    List<UserListItemRes> listUsers(Integer plantelId, Boolean active, String roleCode, String q);
}

