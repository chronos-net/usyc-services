package com.io.usyc.Service;

import com.io.usyc.Dto.PasswordChangeReq;
import com.io.usyc.Dto.UserCreateReq;
import com.io.usyc.Dto.UserCreateRes;

public interface UserAdminService {
    UserCreateRes createUser(UserCreateReq req);
    void changePassword(Long userId, PasswordChangeReq req);
}

