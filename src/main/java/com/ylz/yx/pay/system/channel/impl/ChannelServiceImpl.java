package com.ylz.yx.pay.system.channel.impl;

import com.ylz.svc.apic.ApiExpose;
import com.ylz.svc.bind.Binding;
import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.svc.data.dao.support.PageModel;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.entity.PayFwqd00;
import com.ylz.yx.pay.core.entity.PayFwzfgx;
import com.ylz.yx.pay.core.entity.PayZfqd00;
import com.ylz.yx.pay.core.exception.CustomException;
import com.ylz.yx.pay.core.exception.HttpStatus;
import com.ylz.yx.pay.system.channel.ChannelService;
import com.ylz.yx.pay.system.channel.model.PayFwqd00RQ;
import com.ylz.yx.pay.system.channel.model.PayFwzfgxRQ;
import com.ylz.yx.pay.system.channel.model.QueryParam;
import com.ylz.yx.pay.annotation.BusinessLog;
import com.ylz.yx.pay.annotation.BusinessType;
import com.ylz.yx.pay.utils.DateKit;
import com.ylz.yx.pay.utils.StringKit;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiExpose("channel")
@Service("channelService")
public class ChannelServiceImpl implements ChannelService {

    @Autowired
    private JdbcGateway jdbcGateway;

    /**
     * 查询支付渠道列表
     **/
    @Override
    @ApiExpose("queryZfqdList")
    public Map<String, Object> queryZfqdList() {
        List<PayZfqd00> payZfqd00List = jdbcGateway.selectList("pay.zfqd00.selectAllList");
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", payZfqd00List);
        resultMap.put("total", payZfqd00List.size());
        return resultMap;
    }

    /**
     * 查询支付渠道详情
     **/
    @Override
    @ApiExpose("detailZfqd")
    public Object detailZfqd(PayZfqd00 payZfqd00) {
        return jdbcGateway.selectOne("pay.zfqd00.selectByPrimaryKey", payZfqd00.getId0000());
    }

    /**
     * 添加支付渠道
     **/
    @Override
    @ApiExpose("addZfqd")
    @BusinessLog(title = "支付渠道管理", businessType = BusinessType.INSERT)
    public void addZfqd(PayZfqd00 payZfqd00) {
        PayZfqd00 zfqd00 = jdbcGateway.selectOne("pay.zfqd00.selectByQdmc00", payZfqd00.getZfqdmc());
        if(zfqd00 != null){
            throw new CustomException(HttpStatus.BAD_REQUEST, "支付渠道名称重复，不允许添加！");
        }
        payZfqd00.setZffs00("[" + payZfqd00.getZffs00() + "]");
        jdbcGateway.update("pay.zfqd00.insertSelective", payZfqd00);
    }

    /**
     * 修改支付渠道
     **/
    @Override
    @ApiExpose("updZfqd")
    @BusinessLog(title = "支付渠道管理", businessType = BusinessType.UPDATE, sqlName = "pay.zfqd00.selectByPrimaryKey", sqlParam = "id0000")
    public void updZfqd(PayZfqd00 payZfqd00) {
        PayZfqd00 zfqd00 = jdbcGateway.selectOne("pay.zfqd00.selectByQdmc00", payZfqd00.getZfqdmc());
        if(zfqd00 != null && !payZfqd00.getId0000().equals(zfqd00.getId0000())){
            throw new CustomException(HttpStatus.BAD_REQUEST, "支付渠道名称重复，不允许修改！");
        }
        payZfqd00.setZffs00("[" + payZfqd00.getZffs00() + "]");
        jdbcGateway.update("pay.zfqd00.updateByPrimaryKeySelective", payZfqd00);

    }

    /**
     * 删除支付渠道
     **/
    @Override
    @ApiExpose("delZfqd")
    @BusinessLog(title = "支付渠道管理", businessType = BusinessType.DELETE)
    public void delZfqd(PayZfqd00 payZfqd00) {
        payZfqd00.setSfsc00(CS.YES);
        jdbcGateway.update("pay.zfqd00.updateByPrimaryKeySelective", payZfqd00);
    }

    /**
     * 查询服务渠道列表
     **/
    @Override
    @ApiExpose("queryFwqdList")
    public Map<String, Object> queryFwqdList(QueryParam param) {
        if (param.getPageSize() == null || param.getPageIndex() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "传入的参数不完整！");
        }

        int pageSize = Integer.valueOf(param.getPageSize());
        int pageIndex = Integer.valueOf(param.getPageIndex());
        PageModel pageModel = new PageModel(pageIndex, pageSize);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", jdbcGateway.selectListByPage("pay.fwqd00.selectAllList", param, pageModel));
        resultMap.put("total", jdbcGateway.selectOne("pay.fwqd00.selectCountAllList", param));

        return resultMap;
    }

    /**
     * 查询服务渠道详情
     **/
    @Override
    @ApiExpose("detailFwqd")
    public Object detailFwqd(PayFwqd00RQ payFwqd00RQ) {
        return jdbcGateway.selectOne("pay.fwqd00.detailInfo", payFwqd00RQ.getId0000());
    }

    /**
     * 添加服务渠道
     **/
    @Override
    @ApiExpose("addFwqd")
    @BusinessLog(title = "服务渠道管理", businessType = BusinessType.INSERT)
    public void addFwqd(PayFwqd00RQ payFwqd00RQ) {

        PayFwqd00 payFwqd00 = jdbcGateway.selectOne("pay.fwqd00.selectByQdmc00", payFwqd00RQ.getQdmc00());
        if(payFwqd00 != null){
            throw new CustomException(HttpStatus.BAD_REQUEST, "服务渠道名称重复，不允许添加！");
        }

        String id0000 = StringKit.getUUID();
        payFwqd00 = new PayFwqd00();
        BeanUtils.copyProperties(payFwqd00RQ, payFwqd00);
        payFwqd00.setId0000(id0000);
        payFwqd00.setQdyxq0(DateKit.parseDate(payFwqd00RQ.getQdyxq0(), "yyyy-MM-dd"));
        int count = jdbcGateway.update("pay.fwqd00.insertSelective", payFwqd00);
        if (count > 0) {
            List<PayFwzfgxRQ> payFwzfgxRQList = payFwqd00RQ.getZfqds0();
            for (PayFwzfgxRQ payFwzfgxRQ : payFwzfgxRQList) {
                PayFwzfgx payFwzfgx = new PayFwzfgx();
                payFwzfgx.setFwqdid(id0000);
                BeanUtils.copyProperties(payFwzfgxRQ, payFwzfgx);
                payFwzfgx.setZffs00("[" + payFwzfgx.getZffs00() + "]");
                jdbcGateway.update("pay.fwzfgx.insertSelective", payFwzfgx);
            }
        }
    }

    /**
     * 修改服务渠道
     **/
    @Override
    @ApiExpose("updFwqd")
    @BusinessLog(title = "服务渠道管理", businessType = BusinessType.UPDATE, sqlName = "pay.fwqd00.selectByPrimaryKey", sqlParam = "id0000")
    public void updFwqd(PayFwqd00RQ payFwqd00RQ) {

        PayFwqd00 fwqd00 = jdbcGateway.selectOne("pay.fwqd00.selectByQdmc00", payFwqd00RQ.getQdmc00());
        if(fwqd00 != null && !payFwqd00RQ.getId0000().equals(fwqd00.getId0000())){
            throw new CustomException(HttpStatus.BAD_REQUEST, "服务渠道名称重复，不允许修改！");
        }

        PayFwqd00 payFwqd00 = new PayFwqd00();
        BeanUtils.copyProperties(payFwqd00RQ, payFwqd00);
        payFwqd00.setQdyxq0(DateKit.parseDate(payFwqd00RQ.getQdyxq0(), "yyyy-MM-dd"));
        int count = jdbcGateway.update("pay.fwqd00.updateByPrimaryKeySelective", payFwqd00);
        if (count > 0) {
            if (payFwqd00RQ.getZfqds0() != null) {
                List<PayFwzfgxRQ> payFwzfgxRQList = payFwqd00RQ.getZfqds0();
                jdbcGateway.update("pay.fwzfgx.deleteByPrimaryKey", payFwqd00.getId0000());
                for (PayFwzfgxRQ payFwzfgxRQ : payFwzfgxRQList) {
                    PayFwzfgx payFwzfgx = new PayFwzfgx();
                    payFwzfgx.setFwqdid(payFwqd00.getId0000());
                    BeanUtils.copyProperties(payFwzfgxRQ, payFwzfgx);
                    payFwzfgx.setZffs00("[" + payFwzfgx.getZffs00() + "]");
                    jdbcGateway.update("pay.fwzfgx.insertSelective", payFwzfgx);
                }
            }
        }
    }

    /**
     * 删除服务渠道
     **/
    @Override
    @ApiExpose("delFwqd")
    @BusinessLog(title = "服务渠道管理", businessType = BusinessType.DELETE)
    public void delFwqd(PayFwqd00RQ payFwqd00RQ) {
        PayFwqd00 payFwqd00 = new PayFwqd00();
        payFwqd00.setSfsc00(CS.YES);
        payFwqd00.setId0000(payFwqd00RQ.getId0000());
        jdbcGateway.update("pay.fwqd00.updateByPrimaryKeySelective", payFwqd00);
    }

    @Override
    @ApiExpose("queryZfqdTree")
    public Object queryZfqdTree(@Binding(nullable = true) QueryParam param) {
        if(param == null){
            return jdbcGateway.selectList("pay.zfqd00.selectZfqd00");
        }
        return jdbcGateway.selectList("pay.zfqd00.selectZfqd00Tree", param);
    }

    @Override
    @ApiExpose("getPrivateKey")
    public Object getPrivateKey() {
        return StringKit.getUUID() + StringKit.getUUID() + StringKit.getUUID() + StringKit.getUUID();
    }


}
