package com.ylz.yx.pay.system.ftp.impl;

import com.ylz.svc.apic.ApiExpose;
import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.svc.data.dao.support.PageModel;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.exception.CustomException;
import com.ylz.yx.pay.core.exception.HttpStatus;
import com.ylz.yx.pay.system.ftp.FtpService;
import com.ylz.yx.pay.system.ftp.model.QueryParam;
import com.ylz.yx.pay.system.ftp.model.XtFtp000;
import com.ylz.yx.pay.annotation.BusinessLog;
import com.ylz.yx.pay.annotation.BusinessType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@ApiExpose("ftp")
@Service("ftpService")
public class FtpServiceImpl implements FtpService {

    @Autowired
    private JdbcGateway jdbcGateway;

    /**
     * 查询列表
     **/
    @Override
    @ApiExpose("queryList")
    public Map<String, Object> queryList(QueryParam param) {
        if (param.getPageSize() == null || param.getPageIndex() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "传入的参数不完整！");
        }

        int pageSize = Integer.valueOf(param.getPageSize());
        int pageIndex = Integer.valueOf(param.getPageIndex());
        PageModel pageModel = new PageModel(pageIndex, pageSize);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", jdbcGateway.selectListByPage("system.ftp000.selectAllList", param, pageModel));
        resultMap.put("total", jdbcGateway.selectOne("system.ftp000.selectCountAllList", param));

        return resultMap;
    }

    /**
     * 查询详情
     **/
    @Override
    @ApiExpose("detail")
    public Object detail(XtFtp000 xtFtp000) {
        return jdbcGateway.selectOne("system.ftp000.selectByPrimaryKey", xtFtp000.getId0000());
    }

    /**
     * 添加
     **/
    @Override
    @ApiExpose("add")
    @BusinessLog(title = "对账单管理", businessType = BusinessType.INSERT)
    public void add(XtFtp000 xtFtp000) {

        XtFtp000 ftp000 = jdbcGateway.selectOne("system.ftp000.selectByQdmc00", xtFtp000.getFwqdmc());
        if(ftp000 != null){
            throw new CustomException(HttpStatus.BAD_REQUEST, "服务渠道名称重复，不允许添加！");
        }

        jdbcGateway.insert("system.ftp000.insertSelective", xtFtp000);
    }

    /**
     * 修改
     **/
    @Override
    @ApiExpose("upd")
    @BusinessLog(title = "对账单管理", businessType = BusinessType.UPDATE, sqlName = "system.ftp000.selectByPrimaryKey", sqlParam = "id0000")
    public void upd(XtFtp000 xtFtp000) {
        XtFtp000 ftp000 = jdbcGateway.selectOne("system.ftp000.selectByQdmc00", xtFtp000.getFwqdmc());
        if(ftp000 != null && !xtFtp000.getId0000().equals(ftp000.getId0000())){
            throw new CustomException(HttpStatus.BAD_REQUEST, "服务渠道名称重复，不允许修改！");
        }
        jdbcGateway.update("system.ftp000.updateByPrimaryKeySelective", xtFtp000);
    }

    /**
     * 删除
     **/
    @Override
    @ApiExpose("del")
    @BusinessLog(title = "对账单管理", businessType = BusinessType.DELETE)
    public void del(XtFtp000 xtFtp000) {
        xtFtp000.setSfsc00(CS.YES);
        jdbcGateway.update("system.ftp000.updateByPrimaryKeySelective", xtFtp000);
    }
}
