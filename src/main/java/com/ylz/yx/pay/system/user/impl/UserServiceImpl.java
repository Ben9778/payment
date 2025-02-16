package com.ylz.yx.pay.system.user.impl;

import com.alibaba.fastjson.JSON;
import com.yilz.his.MdEncrypt.IMdEncryptText;
import com.yilz.his.MdEncrypt.MdEncryptText;
import com.ylz.svc.apic.ApiExpose;
import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.svc.web.token.JwtUtils;
import com.ylz.yx.pay.annotation.IntranetIp;
import com.ylz.yx.pay.core.exception.CustomException;
import com.ylz.yx.pay.core.exception.HttpStatus;
import com.ylz.yx.pay.system.dict.impl.DictServiceImpl;
import com.ylz.yx.pay.annotation.BusinessLog;
import com.ylz.yx.pay.annotation.BusinessStatus;
import com.ylz.yx.pay.annotation.BusinessType;
import com.ylz.yx.pay.system.log.model.XtCzrzjl;
import com.ylz.yx.pay.system.log.utils.ServletUtils;
import com.ylz.yx.pay.system.role.model.Ptjsygb0;
import com.ylz.yx.pay.system.user.UserService;
import com.ylz.yx.pay.system.user.model.Bmygbm00;
import com.ylz.yx.pay.system.user.model.LoginParam;
import com.ylz.yx.pay.system.user.model.QueryParam;
import com.ylz.yx.pay.system.user.model.UserParam;
import com.ylz.yx.pay.utils.CommonUtil;
import com.ylz.yx.pay.utils.MapUtils;
import com.ylz.yx.pay.utils.StringKit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@ApiExpose("user")
@Service("userService")
public class UserServiceImpl implements UserService {

    @Autowired
    private JdbcGateway jdbcGateway;

    /**
     * 登录
     **/
    @Override
    @ApiExpose("login")
    @IntranetIp
    //@BusinessLog(title = "登录页", businessType = BusinessType.OTHER, isSaveRequestData = false)
    public Object login(LoginParam param) {
        String account = param.getAccount();
        String password = param.getPassword();
        Map<String, String> tokenMap = new HashMap<>();
        // 默认设置token的过期时间为一小时
        String tokenValue = DictServiceImpl.getDictByKey000("sys_param").getChildren().get("tokenValue").getValue0();
        tokenMap.put("tokenType", "1");
        if(StringUtils.isNotBlank(tokenValue)){
            tokenMap.put("tokenValue", tokenValue);
        }else{
            tokenMap.put("tokenValue", "60");
        }

        // 生成token口令
        String token = JwtUtils.generateToken(account, tokenMap);
        Map<String, Object> userMap = jdbcGateway.selectOne("system.bmygbm00.getUserInfo", account);

        int userId = 0;
        String key = "";

        if (userMap != null) {
            userId = Integer.parseInt(userMap.get("YGBH00").toString());
            key = (String) userMap.get("YHKL00");
            if (userMap.get("JSID00") == null) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "该用户未授权！");
            }
            if ("2".equals(userMap.get("CZBZ00"))) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "该用户已被停用！");
            }
        } else {
            throw new CustomException(HttpStatus.BAD_REQUEST, "该用户不存在，请重新输入！");
        }

        IMdEncryptText dll = new MdEncryptText();
        String encrypstr = dll.EnPassWord(userId, password);


        // 获取用户信息
        if (encrypstr != null && encrypstr.equalsIgnoreCase(key)) {

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("zwxm00", userMap.get("ZWXM00"));
            resultMap.put("ygbh00", userMap.get("YGBH00"));
            resultMap.put("jslx00", userMap.get("JSLX00"));
            resultMap.put("jsid00", userMap.get("JSID00"));
            resultMap.put("token", token);

            //保存登录日志
            XtCzrzjl xtCzrzjl = new XtCzrzjl();
            xtCzrzjl.setId0000(StringKit.getUUID());
            xtCzrzjl.setCzzt00(BusinessStatus.SUCCESS.ordinal());
            // 返回参数
            xtCzrzjl.setFhcs00(JSON.toJSONString(resultMap));
            xtCzrzjl.setQqurl0(ServletUtils.getRequest().getRequestURI());
            xtCzrzjl.setYgbh00(account);
            // 设置方法名称
            xtCzrzjl.setQqff00("com.ylz.yx.pay.system.user.impl.UserServiceImpl.login()");
            // 设置请求方式
            xtCzrzjl.setQqfs00(ServletUtils.getRequest().getMethod());
            // 设置action动作
            xtCzrzjl.setYwlx00(BusinessType.LOGIN.ordinal());
            // 设置标题
            xtCzrzjl.setCzmk00("登录页");
            jdbcGateway.insert("system.czrzjl.insertSelective",xtCzrzjl);
            return resultMap;
        } else {
            throw new CustomException(HttpStatus.BAD_REQUEST, "密码错误，请重新输入！");
        }
    }

    /**
     * 查询用户列表
     **/
    @Override
    @ApiExpose("queryUserList")
    public Object queryUserList(QueryParam param) throws Exception {
        Map<String, Object> map = MapUtils.Obj2Map(param);
        return CommonUtil.getPageData("system.bmygbm00.getUserList", "system.bmygbm00.countUserList", map, jdbcGateway);
    }

    /**
     * 修改用户密码
     **/
    @Override
    @ApiExpose("updUserPassword")
    @BusinessLog(title = "用户管理", businessType = BusinessType.UPDATE, sqlName = "system.bmygbm00.getUserInfoByYgbh00", sqlParam = "ygbh00")
    public void updUserPassword(QueryParam param) {

        Map<String, Object> userMap = jdbcGateway.selectOne("system.bmygbm00.getUserInfoByYgbh00", param.getYgbh00());
        String key = "";

        if (userMap != null) {
            key = (String) userMap.get("YHKL00");
        } else {
            throw new CustomException(HttpStatus.BAD_REQUEST, "修改密码失败");
        }

        int userId = Integer.valueOf(param.getYgbh00());
        String newPwd = param.getYhmm00();
        String password = param.getJyhmm0();
        IMdEncryptText dll = new MdEncryptText();

        String encrypstr = dll.EnPassWord(userId, password);
        // 获取用户信息
        if (encrypstr != null && encrypstr.equalsIgnoreCase(key)) {
            UserParam param1 = new UserParam();
            param1.setYgbh00(String.valueOf(userId));
            param1.setYhkl00(dll.EnPassWord(userId, newPwd));
            int count = jdbcGateway.update("system.bmygbm00.updateBmygbm00", param1);
            if (count <= 0) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "修改密码失败");
            }
        } else {
            throw new CustomException(HttpStatus.BAD_REQUEST, "修改密码失败");
        }

    }

    /**
     * 添加用户
     **/
    @Override
    @ApiExpose("addUserInfo")
    @BusinessLog(title = "用户管理", businessType = BusinessType.INSERT)
    public void addUserInfo(UserParam param) {
        Integer seqNum = jdbcGateway.selectOne("system.bmygbm00.getSeqNum");
        Bmygbm00 bmygbm00 = new Bmygbm00();
        BeanUtils.copyProperties(param,bmygbm00);
        bmygbm00.setYgbh00(seqNum);

        Map<String, Object> userMap = jdbcGateway.selectOne("system.bmygbm00.getUserInfo", param.getXkh000());
        if(userMap != null){
            throw new CustomException(HttpStatus.BAD_REQUEST, "账号重复，不允许添加");
        }

        IMdEncryptText dll = new MdEncryptText();
        bmygbm00.setYhkl00(dll.EnPassWord(seqNum, param.getYhkl00()));
        int count = jdbcGateway.insert("system.bmygbm00.insertSelective", bmygbm00);
        if(count > 0){
            Ptjsygb0 bean = new Ptjsygb0();
            bean.setYgbh00(seqNum);
            bean.setJsid00(Long.valueOf(param.getJsid00()));
            bean.setCjr000(Integer.valueOf(param.getCjr000()));
            jdbcGateway.insert("system.ptjsygb0.insertPtjsygb0", bean);
        }
    }

    /**
     * 修改用户
     **/
    @Override
    @ApiExpose("updUserInfo")
    @BusinessLog(title = "用户管理", businessType = BusinessType.UPDATE, sqlName = "system.bmygbm00.getUserInfoByYgbh00", sqlParam = "ygbh00")
    public void updUserInfo(UserParam param) {
        if(StringUtils.isNotBlank(param.getYhkl00())){
            IMdEncryptText dll = new MdEncryptText();
            param.setYhkl00(dll.EnPassWord(Integer.valueOf(param.getYgbh00()), param.getYhkl00()));
        }
        int count = jdbcGateway.update("system.bmygbm00.updateBmygbm00", param);
        if (count > 0 && StringUtils.isNotEmpty(param.getJsid00())) {
            Ptjsygb0 bean = new Ptjsygb0();
            bean.setYgbh00(Integer.valueOf(param.getYgbh00()));
            bean.setJsid00(Long.valueOf(param.getJsid00()));
            jdbcGateway.insert("system.ptjsygb0.updatePtjsygb0", bean);
        }
    }

    /**
     * 获取用户数据
     **/
    @Override
    @ApiExpose("getUserInfo")
    public Object getUserInfo(UserParam param) {
        return jdbcGateway.selectOne("system.bmygbm00.getUserInfoByYgbh00", param.getYgbh00());
    }

}
