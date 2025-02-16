package com.ylz.yx.pay.system.log.impl;

import com.ylz.svc.apic.ApiExpose;
import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.yx.pay.system.log.OperLogService;
import com.ylz.yx.pay.system.log.dto.GetOperLogDTO;
import com.ylz.yx.pay.utils.CommonUtil;
import com.ylz.yx.pay.utils.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;


@ApiExpose("operLog")
@Service("operLogService")
public class OperLogServiceImpl implements OperLogService {

    @Autowired
    private JdbcGateway jdbcGateway;

    /**
     * 获取日志列表
     **/
    @ApiExpose("getRzjlList")
    @Override
    public Map<String, Object> getRzjlList(GetOperLogDTO getOperLogDTO) throws Exception {

        Map<String, Object> map = MapUtils.Obj2Map(getOperLogDTO);
        return CommonUtil.getPageData("system.czrzjl.getRzjlList", "system.czrzjl.getCountRzjlList", map, jdbcGateway);
    }

    /**
     * 获取日志报文
     **/
    @ApiExpose("getRzjlbw")
    @Override
    public Map<String, Object> getRzjlbw(GetOperLogDTO getOperLogDTO) {

        return  jdbcGateway.selectOne("system.czrzjl.getRzjlbw",getOperLogDTO.getId0000());
    }

}
