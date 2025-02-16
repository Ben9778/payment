package com.ylz.yx.pay.index.impl;

import com.ylz.svc.apic.ApiExpose;
import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.yx.pay.index.IndexService;
import com.ylz.yx.pay.index.model.QueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@ApiExpose("index")
@Service("indexService")
public class IndexServiceImpl implements IndexService {

    @Autowired
    JdbcGateway jdbcGateway;

    @Override
    @ApiExpose("cxhzsj")
    public Object cxhzsj(QueryParam param) {
        return jdbcGateway.selectOne("system.index.cxhzsj", param);
    }

    @Override
    @ApiExpose("cxmrsj")
    public Object cxmrsj(QueryParam param) {
        List<Map> list = jdbcGateway.selectList("system.index.cxmrsj", param);
        return list;
    }

    @Override
    @ApiExpose("fwqdsr")
    public Object fwqdsr(QueryParam param) {
        List<Map> list = jdbcGateway.selectList("system.index.fwqdsr", param);
        return list;
    }

    @Override
    @ApiExpose("zfqdsr")
    public Object zfqdsr(QueryParam param) {
        List<Map> list = jdbcGateway.selectList("system.index.zfqdsr", param);
        return list;
    }
}
