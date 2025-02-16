package com.ylz.yx.pay.system.user;

import com.ylz.yx.pay.system.user.model.LoginParam;
import com.ylz.yx.pay.system.user.model.QueryParam;
import com.ylz.yx.pay.system.user.model.UserParam;

import java.util.Map;

public interface UserService {

    /**
     * 查询用户列表
     */
    Object login(LoginParam param);

    /**
     * 查询用户列表
     */
    Object queryUserList(QueryParam param) throws Exception;

    /**
     * 修改用户密码
     */
    void updUserPassword(QueryParam param);

    /**
     * 新增用户信息
     */
    void addUserInfo(UserParam param);

    /**
     * 修改用户信息
     */
    void updUserInfo(UserParam param);

    /**
     * 获取用户信息
     */
    Object getUserInfo(UserParam param);

}
