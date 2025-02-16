package com.ylz.yx.pay.system.role.impl;

import com.ylz.svc.apic.ApiExpose;
import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.yx.pay.core.exception.CustomException;
import com.ylz.yx.pay.core.exception.HttpStatus;
import com.ylz.yx.pay.annotation.BusinessLog;
import com.ylz.yx.pay.annotation.BusinessType;
import com.ylz.yx.pay.system.role.PowerService;
import com.ylz.yx.pay.system.role.model.*;
import com.ylz.yx.pay.utils.CommonUtil;
import com.ylz.yx.pay.utils.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@ApiExpose("role")
@Service("powerService")
public class PowerServiceImpl implements PowerService {

    @Autowired
    private JdbcGateway jdbcGateway;

    /**
     * 查询角色列表
     **/
    @ApiExpose("queryRoleList")
    @Override
    public Map<String, Object> queryRoleList(ReqParam param) throws Exception {
        Map<String, Object> map = MapUtils.Obj2Map(param);
        return CommonUtil.getPageData("system.ptjsb000.getRoleList", "system.ptjsb000.countRoleList", map, jdbcGateway);
    }

    /**
     * 添加角色
     **/
    @ApiExpose("addRole")
    @Override
    @BusinessLog(title = "角色管理", businessType = BusinessType.INSERT)
    public void addRole(ReqParam param) throws Exception {
        Integer seqNum = jdbcGateway.selectOne("system.bmygbm00.getSeqNum");
        Map<String, Object> map = MapUtils.Obj2Map(param);
        Ptjsb000 ptjsb000 = jdbcGateway.selectOne("system.ptjsb000.getRoleByJsmc00", map);
        if(ptjsb000 != null){
            throw new CustomException(HttpStatus.BAD_REQUEST, "角色名称重复，不允许添加！");
        }
        map.put("jsid00", seqNum);
        int count = jdbcGateway.insert("system.ptjsb000.insertPtjsb000", map);
        if (count <= 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "新增失败");
        }
        String jhgnidList = (String) map.get("jhgnid");
        if (StringUtils.isNotEmpty(jhgnidList)) {
            String[] jhgnidArr = jhgnidList.split(",");
            for (String jhgnid : jhgnidArr) {
                Ptjsgnb0 bean = new Ptjsgnb0();
                bean.setJhgnid(jhgnid);
                bean.setJsid00(seqNum);
                bean.setCjr000(Integer.valueOf(map.get("cjr000").toString()));
                jdbcGateway.insert("system.ptjsgnb0.insertPtjsgnb0", bean);
            }
        }
    }

    /**
     * 查询用户角色权限
     **/
    @ApiExpose("queryRolePermissionList")
    @Override
    public List<TreeStructure> queryRolePermissionList(ReqParam param) throws Exception {
        Map<String, Object> map = MapUtils.Obj2Map(param);
        List<TreeStructure> resultList = jdbcGateway.selectList("system.ptjhgnhb.getRolePermissionList", map);

        if (resultList.isEmpty()) {
            return resultList;
        }

        for (TreeStructure bean : resultList) {
            if (bean.getLevel() == 1) {
                bean.setComponent("Layout");
            }
        }

        return CommonUtil.tranformTreeStructure(resultList, resultList.get(resultList.size() - 1).getLevel(), null);
    }

    /**
     * 修改角色
     **/
    @ApiExpose("updRoleInfo")
    @Override
    @BusinessLog(title = "角色管理", businessType = BusinessType.UPDATE)
    public void updRoleInfo(ReqParam param) throws Exception {
        Map<String, Object> map = MapUtils.Obj2Map(param);
        int count = jdbcGateway.update("system.ptjsb000.updatePtjsb000", map);
        if (count <= 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "修改失败");
        }
        String jhgnidList = (String) map.get("jhgnid");
        if (StringUtils.isNotEmpty(jhgnidList)) {
            jdbcGateway.delete("system.ptjsgnb0.deletePtjsgnb0", map.get("jsid00"));
            String[] jhgnidArr = jhgnidList.split(",");
            for (String jhgnid : jhgnidArr) {
                Ptjsgnb0 bean = new Ptjsgnb0();
                bean.setJhgnid(jhgnid);
                bean.setJsid00(Integer.valueOf(map.get("jsid00").toString()));
                bean.setCjr000(Integer.valueOf(map.get("cjr000").toString()));
                jdbcGateway.insert("system.ptjsgnb0.insertPtjsgnb0", bean);
            }
        }
    }

    /**
     * 查询角色权限
     **/
    @ApiExpose("queryRoleOwnPermissionList")
    @Override
    public List<TreeStructure> queryRoleOwnPermissionList(ReqParam param) throws Exception {
        Map<String, Object> map = MapUtils.Obj2Map(param);
        List<String> permissionIds = jdbcGateway.selectList("system.ptjhgnhb.getRolePermissionId", map);
        map.put("jslx00", "1");
        List<TreeStructure> resultList = jdbcGateway.selectList("system.ptjhgnhb.getRolePermissionList", map);

        if (resultList.isEmpty()) {
            return resultList;
        }

        for (TreeStructure bean : resultList) {
            if (permissionIds.contains(bean.getKey())) {
                bean.setChecked(true);
            }
            if("PAY_WEB_SYKB".equals(bean.getKey())){
                bean.setChecked(true);
                bean.setDisabled(true);
            }
        }

        return CommonUtil.tranformTreeStructure(resultList, resultList.get(resultList.size() - 1).getLevel(), null);
    }

}
