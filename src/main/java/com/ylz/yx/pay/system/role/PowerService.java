package com.ylz.yx.pay.system.role;

import com.ylz.yx.pay.system.role.model.ReqParam;
import com.ylz.yx.pay.system.user.model.Personnel;
import com.ylz.yx.pay.system.role.model.Ptjsb000;
import com.ylz.yx.pay.system.role.model.TreeStructure;

import java.util.List;
import java.util.Map;

public interface PowerService {

    /**
     * 查询角色列表
     */
    Map<String, Object> queryRoleList(ReqParam param) throws Exception;

    /**
     * 添加角色
     */
    void addRole(ReqParam param) throws Exception;

    /**
     * 修改角色信息
     */
    void updRoleInfo(ReqParam param) throws Exception;

    /**
     * 查询角色拥有的权限列表
     */
    List<TreeStructure> queryRoleOwnPermissionList(ReqParam param) throws Exception;

    /**
     * 查询角色权限列表
     */
    List<TreeStructure> queryRolePermissionList(ReqParam param) throws Exception;

}
