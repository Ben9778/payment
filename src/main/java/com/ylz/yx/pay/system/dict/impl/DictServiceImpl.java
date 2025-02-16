package com.ylz.yx.pay.system.dict.impl;

import com.alibaba.fastjson.JSONObject;
import com.ylz.svc.apic.ApiExpose;
import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.yx.pay.config.ApplicationProperty;
import com.ylz.yx.pay.core.exception.CustomException;
import com.ylz.yx.pay.core.exception.HttpStatus;
import com.ylz.yx.pay.system.dict.model.XtZd0000;
import com.ylz.yx.pay.system.dict.query.AddDictParam;
import com.ylz.yx.pay.system.dict.query.QueryParam;
import com.ylz.yx.pay.system.dict.query.UpdDictParam;
import com.ylz.yx.pay.system.dict.DictService;
import com.ylz.yx.pay.system.dict.vo.GetAllDictVO;
import com.ylz.yx.pay.system.dict.vo.GlobalDict;
import com.ylz.yx.pay.annotation.BusinessLog;
import com.ylz.yx.pay.annotation.BusinessType;
import com.ylz.yx.pay.utils.StringKit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiExpose("dict")
@Service("dictService")
public class DictServiceImpl implements DictService {

    @Autowired
    private StringRedisTemplate redis;

    public static RedisTemplate staticRedis;

    @Resource
    private ApplicationProperty applicationProperty;

    @PostConstruct
    public void init(){
        staticRedis = redis;

        HashMap<String, GlobalDict> globalDictMap = getAllDictInMap();
        for(Map.Entry entry:globalDictMap.entrySet()){
            staticRedis.opsForValue().set(entry.getKey(),JSONObject.toJSONString(entry.getValue()));
        }
//
        List<GetAllDictVO> dictList = getAllDictList();
        for(GetAllDictVO item:dictList){
            staticRedis.opsForValue().set(item.getId0000(),JSONObject.toJSONString(item));
        }
    }

    private List<GetAllDictVO> getAllDictList() {
        List<GetAllDictVO> list =  jdbcGateway.selectList("system.zd0000.getAllDict");
        return list;
    }

    @Resource
    private JdbcGateway jdbcGateway;

    /**
     * 根据父级ID查询字典列表
     **/
    @Override
    @ApiExpose("getDictListByParentId")
    public List<XtZd0000> getDictListByParentId(QueryParam param) {

        Map sqlParam = new HashMap();
        sqlParam.put("fjid00",param.getFjid00());
        if(StringUtils.isNotBlank(param.getFjid00())){
            sqlParam.put("keyword","");
        } else {
            sqlParam.put("keyword", param.getKeyword());
        }
        sqlParam.put("sfqy00",param.getSfqy00());

        return jdbcGateway.selectList("system.zd0000.getDictListByParentId",sqlParam);
    }

    /**
     * 根据字典key查询字典列表（只允许查询第一级字典）
     **/
    @Override
    @ApiExpose("getDictList")
    public Object getDictList(QueryParam param){
        if(param.getZdlx00().equals("1")){//支付渠道
            param.setKey000("pay_channel");
        } else if(param.getZdlx00().equals("2")){//支付方式
            param.setKey000(param.getZfqd00());
        } else if(param.getZdlx00().equals("3")){//订单类型
            param.setKey000("order_type");
        } else if(param.getZdlx00().equals("4")){//服务渠道
            param.setYylx00("0");
            return jdbcGateway.selectList("pay.fwqd00.getDictList", param);
        } else if(param.getZdlx00().equals("5")){//支付状态
            param.setKey000("order_state");
        } else if(param.getZdlx00().equals("6")){//退款状态
            param.setKey000("refund_state");
        } else if(param.getZdlx00().equals("7")){//对账结果
            param.setKey000("record_result");
        } else if(param.getZdlx00().equals("8")){//处理状态
            param.setKey000("handle_state");
        } else if(param.getZdlx00().equals("9")){//服务渠道
            param.setYylx00("1");
            return jdbcGateway.selectList("pay.fwqd00.getDictList", param);
        }

        return jdbcGateway.selectList("system.zd0000.getDictList",param);
    }

    /**
     * 添加字典
     **/
    @Override
    @ApiExpose("addDict")
    @BusinessLog(title = "字典管理", businessType = BusinessType.INSERT)
    public void addDict(AddDictParam addDictParam)  {

        //判断key是否重复
        Map sqlParam = new HashMap();
        sqlParam.put("fjid00",addDictParam.getFjid00());
        sqlParam.put("key000",addDictParam.getKey000());
        List list = jdbcGateway.selectList("system.zd0000.getDictListByParentIdAndKey",sqlParam);
        if(list.size()>0){
            throw new CustomException(HttpStatus.BAD_REQUEST, "key重复，不允许添加");
        }

        XtZd0000 model = new XtZd0000();
        BeanUtils.copyProperties(addDictParam,model);
        model.setId0000(StringKit.getUUID());

        jdbcGateway.insert("system.zd0000.insertSelective",model);
        init();
        return ;
    }

    /**
     * 修改字典
     **/
    @Override
    @ApiExpose("updDict")
    @BusinessLog(title = "字典管理", businessType = BusinessType.UPDATE, sqlName = "system.zd0000.selectByPrimaryKey", sqlParam = "id0000")
    public void updDict(UpdDictParam param)  {
        if(StringUtils.isEmpty(param.getId0000()) || StringUtils.isEmpty(param.getKey000())
                || StringUtils.isEmpty(param.getZdmc00())){
            throw new CustomException(HttpStatus.BAD_REQUEST, "必填项值不能为空！");
        }
        //判断key是否重复
        Map sqlParam = new HashMap();
        sqlParam.put("fjid00",param.getFjid00());
        sqlParam.put("key000",param.getKey000());
        sqlParam.put("id0000",param.getId0000());
        List list = jdbcGateway.selectList("system.zd0000.getBrotherKeysExist",sqlParam);
        if(list.size()>0){
            throw new CustomException(HttpStatus.BAD_REQUEST, "key重复，不允许修改");
        }
        XtZd0000 model = new XtZd0000();
        BeanUtils.copyProperties(param,model);

        jdbcGateway.update("system.zd0000.updateByPrimaryKeySelective",model);
        if(param.getKey000().equals("isCache")){
            String isCache = jdbcGateway.selectOne("system.zd0000.getSysParam", "isCache");
            ApplicationProperty.IS_USE_CACHE = isCache.equals("true");
        } else if(param.getKey000().equals("isCheckUrl")){
            String isCheckUrl = jdbcGateway.selectOne("system.zd0000.getSysParam", "isCheckUrl");
            applicationProperty.setIsCheckUrl(isCheckUrl.equals("true"));//是否校验地址 true/false
        } else if(param.getKey000().equals("intranetIp")){
            applicationProperty.setIntranetIp(jdbcGateway.selectOne("system.zd0000.getSysParam", "intranetIp"));//允许访问的地址网段
        } else if(param.getKey000().equals("paySiteBackUrl")){
            applicationProperty.setPaySiteBackUrl(jdbcGateway.selectOne("system.zd0000.getSysParam", "paySiteBackUrl"));//支付网关后端地址
        } else if(param.getKey000().equals("paySiteFrontUrl")){
            applicationProperty.setPaySiteFrontUrl(jdbcGateway.selectOne("system.zd0000.getSysParam", "paySiteFrontUrl"));//支付网关前端地址
        } else if(param.getKey000().equals("fileStoragePath")){
            applicationProperty.setFileStoragePath(jdbcGateway.selectOne("system.zd0000.getSysParam", "fileStoragePath"));//文件存放地址
        } else if(param.getKey000().equals("mchName")){
            applicationProperty.setMchName(jdbcGateway.selectOne("system.zd0000.getSysParam", "mchName"));//商户名称
        } else if(param.getKey000().equals("ylzCommonUrl")){
            applicationProperty.setYlzCommonUrl(jdbcGateway.selectOne("system.zd0000.getSysParam", "ylzCommonUrl"));//通用接口地址
        } else if(param.getKey000().equals("hisManufacturer")){
            applicationProperty.setHisManufacturer(jdbcGateway.selectOne("system.zd0000.getSysParam", "hisManufacturer"));//HIS厂家
        } else if(param.getKey000().equals("hisOperator")){
            applicationProperty.setHisOperator(jdbcGateway.selectOne("system.zd0000.getSysParam", "hisOperator"));//HIS系统操作员
        } else if(param.getKey000().equals("isHisViewData")){
            //是否直接获取HIS对账数据 true/false
            String isHisViewData = jdbcGateway.selectOne("system.zd0000.getSysParam", "isHisViewData");
            applicationProperty.setIsHisViewData(isHisViewData.equals("true"));
        } else if(param.getKey000().equals("hisBillFileUrl")){
            applicationProperty.setHisBillFileUrl(jdbcGateway.selectOne("system.zd0000.getSysParam", "hisBillFileUrl"));//请求HIS对账数据地址
        } else if(param.getKey000().equals("isHisOrder")) {
            //判断HIS是否存在订单 true/false
            String isHisOrder = jdbcGateway.selectOne("system.zd0000.getSysParam", "isHisOrder");
            applicationProperty.setIsHisOrder(isHisOrder.equals("true"));
        } else if(param.getKey000().equals("isRefundApproval")) {
            //是否需要走退款审批流程 true/false
            String isRefundApproval = jdbcGateway.selectOne("system.zd0000.getSysParam", "isRefundApproval");
            applicationProperty.setIsRefundApproval(isRefundApproval.equals("true"));
        }
        init();
        return ;

    }

    /**
     * 开启/禁用 字典
     **/
    @Override
    @ApiExpose("switchDictById")
    @BusinessLog(title = "字典管理", businessType = BusinessType.UPDATE, sqlName = "system.zd0000.selectByPrimaryKey", sqlParam = "id0000")
    public void switchDictById(QueryParam param){
        XtZd0000 zd0000 = new XtZd0000();
        zd0000.setId0000(param.getId0000());
        zd0000.setSfqy00(param.getSfqy00());
        jdbcGateway.update("system.zd0000.updateByPrimaryKeySelective",zd0000);
        init();
    }

    /**
     * 删除字典
     **/
    @Override
    @ApiExpose("deleteDictById")
    @BusinessLog(title = "字典管理", businessType = BusinessType.DELETE)
    public void deleteDictById(QueryParam param){
        jdbcGateway.delete("deleteDictById",param.getId0000());
        init();
    }

    /**
     * 查询所有字典，使用数组存储同级元素
     **/
    @Override
    @ApiExpose("getAllDict")
    public List getAllDict() {
        List<GetAllDictVO> list =  jdbcGateway.selectList("system.zd0000.getAllDict");

        //List转化为Map，主键ID作为key
        Map<String,GetAllDictVO> dictMap = new HashMap<String,GetAllDictVO>();
        for(int i=0;i<list.size();i++){
            GetAllDictVO item = list.get(i);
            dictMap.put(item.getId0000(),item);
        }
        //将所有子元素和父元素连接
        for(GetAllDictVO item:list){
            String fjid00 = item.getFjid00();
            if(fjid00!=null&&fjid00!=""){
                if(dictMap.get(fjid00)!=null) {
                    dictMap.get(fjid00).addChild(item);
                }
            }
        }
        //List中去除非顶级元素
        for(int i=0;i<list.size();i++){
            GetAllDictVO item = list.get(i);
            String fjid00 = item.getFjid00();
            if(fjid00!=null&&fjid00!=""){
                list.remove(i);
                i--;
            }
        }
        return list;
    }

    /**
     * 查询所有字典，使用key-value存储同级元素
     **/
    @Override
    public HashMap getAllDictInMap() {
        List<GlobalDict> list =  jdbcGateway.selectList("system.zd0000.getAllDictInMap");

        //List转化为Map，主键ID作为key
        Map<String,GlobalDict> dictMap = new HashMap<String,GlobalDict>();
        for(int i=0;i<list.size();i++){
            GlobalDict item = list.get(i);
            dictMap.put(item.getId0000(),item);
        }
        //将所有子元素和父元素连接
        for(GlobalDict item:list){
            String fjid00 = item.getFjid00();
            if(fjid00!=null&&fjid00!=""){
                if(dictMap.get(fjid00)!=null){
                    dictMap.get(fjid00).getChildren().put(item.getKey000(),item);
                }
            }
        }
        //返回Map,将key从Id0000改为key000
        HashMap resultMap = new HashMap();
        for(String key:dictMap.keySet()){
            GlobalDict item = dictMap.get(key);
            String fjid00=item.getFjid00();
            if(fjid00==null||fjid00==""){
                resultMap.put(item.getKey000(),item);
            }
        }
        return resultMap;
    }

    public static GlobalDict getDictByKey000(String key000){

        Object result = staticRedis.opsForValue().get(key000);
        if(result == null){
            return null;
        }
        return JSONObject.parseObject(result.toString(),GlobalDict.class);
    }

    public static GetAllDictVO getDictById0000(String id0000){

        Object result = staticRedis.opsForValue().get(id0000);
        if(result == null){
            return null;
        }
        return JSONObject.parseObject(result.toString(),GetAllDictVO.class);
    }

}
