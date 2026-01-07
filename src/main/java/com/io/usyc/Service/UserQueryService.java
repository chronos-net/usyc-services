package com.io.usyc.Service;

import com.io.usyc.Dto.UserListItemRes;

import java.util.List;

public interface UserQueryService {
    List<UserListItemRes> listUsers(Integer plantelId, Boolean active, String roleCode, String q);
}