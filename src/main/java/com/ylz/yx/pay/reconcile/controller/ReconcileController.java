package com.ylz.yx.pay.reconcile.controller;

import cn.hutool.extra.ftp.Ftp;
import cn.hutool.extra.ftp.FtpMode;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.request.AlipayDataDataserviceBillDownloadurlQueryRequest;
import com.alipay.api.response.AlipayDataDataserviceBillDownloadurlQueryResponse;
import com.github.binarywang.wxpay.bean.request.WxPayDownloadBillRequest;
import com.github.binarywang.wxpay.bean.result.WxPayBillInfo;
import com.github.binarywang.wxpay.bean.result.WxPayBillResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.ylz.core.logging.Logger;
import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.yx.pay.config.ApplicationProperty;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.entity.PayFwdzd0;
import com.ylz.yx.pay.core.entity.PayZfqd00;
import com.ylz.yx.pay.core.model.params.alipay.AlipayMchParams;
import com.ylz.yx.pay.core.model.params.pospay.PospayMchParams;
import com.ylz.yx.pay.core.model.params.wxpay.WxpayMchParams;
import com.ylz.yx.pay.payment.model.AlipayClientWrapper;
import com.ylz.yx.pay.payment.model.WxServiceWrapper;
import com.ylz.yx.pay.record.model.PayPosdzd;
import com.ylz.yx.pay.record.model.PayWxdzd0;
import com.ylz.yx.pay.record.model.PayZfbdzd;
import com.ylz.yx.pay.utils.FileUtils;
import com.ylz.yx.pay.utils.HttpClientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RestController
@RequestMapping("/api/reconcile")
public class ReconcileController {

    private final Logger logger = new Logger("pay", "reconcile", ReconcileController.class.getName());

    @Autowired
    private JdbcGateway jdbcGateway;
    @Resource
    private ApplicationProperty applicationProperty;

    //重新下载HIS对账文件
    @RequestMapping("/getHisBillFile")
    public void getHisBillFile(String billDate) {
        if (StringUtils.isNotBlank(applicationProperty.getHisBillFileUrl())) {
            jdbcGateway.insert("pay.fwdzd0.deleteByDzrq00", billDate);
            logger.info("==========请求HIS接口直接返回对账单数据【开始】==========");
            Map<String, Object> reqData = new HashMap();
            reqData.put("billDate", billDate);
            //reqData.put("title", "[{\"titleName\":\"序号\"},{\"titleName\":\"渠道ID\",\"titleKey\":\"APPID0\"},{\"titleName\":\"商户订单号\",\"titleKey\":\"SHDDH0\"},{\"titleName\":\"支付订单号\",\"titleKey\":\"PTDDH0\"},{\"titleName\":\"业务类型\",\"titleKey\":\"YWLX00\"},{\"titleName\":\"交易类型\",\"titleKey\":\"JYLX00\"},{\"titleName\":\"订单名称\",\"titleKey\":\"DDMC00\"},{\"titleName\":\"金额（元）\",\"titleKey\":\"JFJE00\"},{\"titleName\":\"交易时间\",\"titleKey\":\"CZRQSJ\"},{\"titleName\":\"病人姓名\",\"titleKey\":\"BRXM00\"},{\"titleName\":\"卡号\",\"titleKey\":\"ICKH00\"},{\"titleName\":\"住院号\",\"titleKey\":\"ZYGHID\"}]");
            String rspData = HttpClientUtil.doPost(applicationProperty.getHisBillFileUrl(), reqData);
            int length = rspData.split("\r\n").length;
            String[] info = rspData.split("\r\n");
            for (int i = 1; i < length; i++) {
                String[] spl = info[i].split(",");
                PayFwdzd0 payFwdzd0 = PayFwdzd0.builder()
                        .dzrq00(billDate)
                        .fwqdid(spl[1].trim())
                        .fwddh0(spl[2].trim())
                        .xtddh0(spl[3].trim())
                        .ywlx00(spl[4].trim())
                        .jylx00(Double.valueOf(spl[7].trim()) < 0 ? "退款" : "充值")
                        .ddmc00(spl[6].trim())
                        .zfje00(String.valueOf(Math.abs(Double.valueOf(spl[7].trim()))))
                        .jysj00(spl[8].trim())
                        .hzxm00(spl[9].trim())
                        .khzyh0(spl.length == 11?spl[10].trim():"")
                        .build();
                jdbcGateway.insert("pay.fwdzd0.insertSelective", payFwdzd0);
            }
            logger.info("==========请求HIS接口直接返回对账单数据【结束】==========");
        }
    }

    //重新下载微信对账文件
    @RequestMapping("/getWxBillFile")
    public void getWxBillFile(String billDate) {
        logger.info("==========自动下载微信对账文件【开始】==========");

        jdbcGateway.insert("pay.wxdzd0.deleteByDzrq00", billDate);

        List<PayZfqd00> payZfqd00List = jdbcGateway.selectList("pay.zfqd00.selectAll");
        for (PayZfqd00 payZfqd00 : payZfqd00List) {
            switch (payZfqd00.getQdbm00()) {
                case CS.IF_CODE.WXPAY:
                    WxpayMchParams wxpayParams = JSONObject.parseObject(payZfqd00.getQdpz00(), WxpayMchParams.class);
                    try {
                        WxPayService wxPayService = WxServiceWrapper.buildWxServiceWrapper(wxpayParams).getWxPayService();

                        WxPayDownloadBillRequest request = new WxPayDownloadBillRequest();
                        request.setBillDate(billDate);
                        request.setBillType("ALL");
                        if (StringUtils.isNotBlank(wxpayParams.getSubMchId())) {
                            request.setSubMchId(wxpayParams.getSubMchId());
                        }

                        WxPayBillResult result = wxPayService.downloadBill(request);

                        List<WxPayBillInfo> billInfoList = result.getBillInfoList();
                        logger.info("微信对账单总笔数："+ billInfoList.size());
                        for (WxPayBillInfo wxPayBillInfo : billInfoList) {
                            PayWxdzd0 payWxdzd0 = new PayWxdzd0();
                            payWxdzd0.setDzrq00(billDate);
                            payWxdzd0.setJysj00(wxPayBillInfo.getTradeTime());
                            payWxdzd0.setGzzhid(wxPayBillInfo.getAppId());
                            payWxdzd0.setShhid0(wxpayParams.getMchId());
                            payWxdzd0.setZshhid(wxpayParams.getSubMchId());
                            payWxdzd0.setSbh000(wxPayBillInfo.getDeviceInfo());
                            payWxdzd0.setZfddh0(wxPayBillInfo.getTransactionId());
                            payWxdzd0.setXtddh0(wxPayBillInfo.getOutTradeNo());
                            payWxdzd0.setYhbs00(wxPayBillInfo.getOpenId());
                            payWxdzd0.setJylx00(wxPayBillInfo.getTradeType());
                            payWxdzd0.setJyzt00(wxPayBillInfo.getTradeState());
                            payWxdzd0.setFkyh00(wxPayBillInfo.getBankType());
                            payWxdzd0.setHbzl00(wxPayBillInfo.getFeeType());
                            payWxdzd0.setZje000(Double.valueOf(wxPayBillInfo.getTotalFee()));
                            payWxdzd0.setQyhbje(Double.valueOf(wxPayBillInfo.getCouponFee()));
                            payWxdzd0.setZftkdh(wxPayBillInfo.getRefundId());
                            payWxdzd0.setXttkdh(wxPayBillInfo.getOutRefundNo());
                            payWxdzd0.setTkje00(Double.valueOf(wxPayBillInfo.getSettlementRefundFee()));
                            payWxdzd0.setQyhbtk(Double.valueOf(wxPayBillInfo.getCouponRefundFee()));
                            payWxdzd0.setTklx00(wxPayBillInfo.getRefundChannel());
                            payWxdzd0.setTkzt00(wxPayBillInfo.getRefundState());
                            payWxdzd0.setDdmc00(wxPayBillInfo.getBody());
                            payWxdzd0.setShsjb0(wxPayBillInfo.getAttach());
                            payWxdzd0.setSxf000(Double.valueOf(wxPayBillInfo.getPoundage()));
                            payWxdzd0.setFl0000(Double.valueOf(wxPayBillInfo.getPoundageRate().replace("%", "")));
                            payWxdzd0.setDdje00(Double.valueOf(wxPayBillInfo.getTotalAmount()));
                            payWxdzd0.setSqtkje(Double.valueOf(wxPayBillInfo.getAppliedRefundAmount()));
                            payWxdzd0.setFlbz00(wxPayBillInfo.getFeeRemark());
                            payWxdzd0.setTksqsj(wxPayBillInfo.getRefundTime());
                            payWxdzd0.setTkcgsj(wxPayBillInfo.getRefundSuccessTime());
                            jdbcGateway.insert("pay.wxdzd0.insertSelective", payWxdzd0);
                        }
                        logger.info("微信支付下载对账文件成功");
                    } catch (WxPayException e) {
                        logger.error("微信支付下载对账文件异常：", e);
                    }
                    break;
                default:
                    break;
            }
        }
        logger.info("==========自动下载微信对账文件【结束】==========");
    }

    //重新下载支付宝对账文件
    @RequestMapping("/getZfbBillFile")
    public void getZfbBillFile(String billDate) {
        logger.info("==========自动下载支付宝对账文件【开始】==========");

        jdbcGateway.insert("pay.zfbdzd.deleteByDzrq00", billDate);

        List<PayZfqd00> payZfqd00List = jdbcGateway.selectList("pay.zfqd00.selectAll");
        for (PayZfqd00 payZfqd00 : payZfqd00List) {
            switch (payZfqd00.getQdbm00()) {
                case CS.IF_CODE.ALIPAY:
                    AlipayMchParams alipayParams = JSONObject.parseObject(payZfqd00.getQdpz00(), AlipayMchParams.class);
                    AlipayDataDataserviceBillDownloadurlQueryRequest req = new AlipayDataDataserviceBillDownloadurlQueryRequest();
                    Map<String, Object> bizContent = new HashMap<String, Object>();
                    // bill_type
                    // 账单类型，商户通过接口或商户经开放平台授权后其所属服务商通过接口可以获取以下账单类型：trade、signcustomer；
                    // trade指商户基于支付宝交易收单的业务账单；
                    // signcustomer是指基于商户支付宝余额收入及支出等资金变动的帐务账单；
                    bizContent.put("bill_type", "trade");
                    // bill_date 账单时间：日账单格式为yyyy-MM-dd，月账单格式为yyyy-MM。
                    bizContent.put("bill_date", billDate);
                    req.setBizContent(JSON.toJSONString(bizContent));

                    AlipayDataDataserviceBillDownloadurlQueryResponse response = AlipayClientWrapper.buildAlipayClientWrapper(alipayParams).execute(req);
                    logger.info("==========自动下载支付宝对账文件【开始】==========" + response.toString());
                    // 将接口返回的对账文件下载地址传入urlStr
                    String urlStr = response.getBillDownloadUrl();
                    URL url = null;
                    HttpURLConnection conn = null;
                    ZipInputStream in = null;
                    BufferedReader br = null;
                    try {
                        url = new URL(urlStr);
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setConnectTimeout(5 * 1000);
                        conn.setRequestMethod("GET");
                        conn.connect();
                        in = new ZipInputStream(conn.getInputStream(), Charset.forName("GBK"));
                        br = new BufferedReader(new InputStreamReader(in, "GBK"));
                        ZipEntry zipFile;
                        while ((zipFile = in.getNextEntry()) != null) {
                            if (zipFile.isDirectory()) {
                                // 目录不处理
                            }
                            // 获得cvs名字，检测文件是否存在
                            String fileName = zipFile.getName();
                            logger.info("对账单解析，输出文件名称：" + fileName);
                            if (!Objects.isNull(fileName) && fileName.indexOf(".") != -1 && !fileName.contains("汇总")) {
                                String line;
                                int i = 0;
                                // 按行读取数据
                                while ((line = br.readLine()) != null) {
                                    if (!line.startsWith("#")) {
                                        logger.info("解析数据：" + line);
                                        if (i > 0) {
                                            String[] lines = line.split(",", -1);
                                            PayZfbdzd payZfbdzd = PayZfbdzd.builder()
                                                    .dzrq00(billDate)
                                                    .zfddh0(lines[0].trim())
                                                    .xtddh0(lines[1].trim())
                                                    .ywlx00(lines[2].trim())
                                                    .ddmc00(lines[3].trim())
                                                    .ddcjsj(lines[4].trim())
                                                    .ddzfsj(lines[5].trim())
                                                    .mdbh00(lines[6].trim())
                                                    .mdmc00(lines[7].trim())
                                                    .czy000(lines[8].trim())
                                                    .zdh000(lines[9].trim())
                                                    .dfzh00(lines[10].trim())
                                                    .zfje00(Double.valueOf(lines[11].trim()))
                                                    .sjss00(Double.valueOf(lines[12].trim()))
                                                    .zfbhb0(Double.valueOf(lines[13].trim()))
                                                    .jfb000(Double.valueOf(lines[14].trim()))
                                                    .zfbyh0(Double.valueOf(lines[15].trim()))
                                                    .sjyh00(Double.valueOf(lines[16].trim()))
                                                    .qhxje0(Double.valueOf(lines[17].trim()))
                                                    .qmc000(lines[18].trim())
                                                    .sjhbje(Double.valueOf(lines[19].trim()))
                                                    .kxfje0(Double.valueOf(lines[20].trim()))
                                                    .tkddh0(lines[21].trim())
                                                    .fwf000(Double.valueOf(lines[22].trim()))
                                                    .fr0000(Double.valueOf(lines[23].trim()))
                                                    .bz0000(lines[24].trim())
                                                    .build();
                                            jdbcGateway.insert("pay.zfbdzd.insertSelective", payZfbdzd);
                                        }
                                        i++;
                                    }
                                }
                            }
                        }
                        logger.info("支付宝下载对账文件成功");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (br != null) br.close();
                            if (in != null) in.close();
                            if (conn != null) conn.disconnect();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        logger.info("==========自动下载支付宝对账文件【结束】==========");
    }

    //重新下载银联POS对账文件
    @RequestMapping("/getPosBillFile")
    public void getPosBillFile(String billDate) {
        logger.info("==========自动下载银联POS对账文件【开始】==========");

        jdbcGateway.insert("pay.posdzd.deleteByDzrq00", billDate);

        List<PayZfqd00> payZfqd00List = jdbcGateway.selectList("pay.zfqd00.selectAll");
        for (PayZfqd00 payZfqd00 : payZfqd00List) {
            switch (payZfqd00.getQdbm00()) {
                case CS.IF_CODE.POSPAY:
                    PospayMchParams pospayMchParams = JSONObject.parseObject(payZfqd00.getQdpz00(), PospayMchParams.class);
                    try {
                        String url = pospayMchParams.getFtpUrl();
                        String ip = "";
                        String port = "";
                        String dir = "/";
                        if (url.indexOf(":") > 0) {
                            ip = url.split(":")[0];
                            port = url.split(":")[1].split("/")[0];
                            if (url.indexOf("/") > 0) {
                                dir = url.split("/")[1];
                            }
                        } else {
                            ip = url;
                        }
                        if (StringUtils.isEmpty(port)) {
                            port = "21";
                        }
                        // 帐号密码连接FTP服务器
                        Ftp ftp = new Ftp(ip, Integer.valueOf(port), pospayMchParams.getFtpUser(), pospayMchParams.getFtpPwd());
                        // 连接策略(Passive:模式被动;Active:主动模式 )
                        ftp.setMode(FtpMode.Active);
                        // 下载远程文件(下载指定文件，到指定路径的指定文件)
                        ftp.download(dir, pospayMchParams.getFtpFileName().replace("{date}", billDate), new File(applicationProperty.getFileStoragePath() + "\\billFile\\pospay\\" + billDate + "\\" + pospayMchParams.getFtpFileName().replace("{date}", billDate)));
                        // 关闭连接
                        ftp.close();

                        //遍历目录下的所有文件
                        List<String> fileList = FileUtils.getFiles(applicationProperty.getFileStoragePath() + "\\billFile\\pospay\\" + billDate);
                        for (String filepath : fileList) {
                            final int[] i = {0};
                            // 读取⽂件内容到Stream流中，按⾏读取
                            File file = new File(filepath);
                            FileInputStream readIn = new FileInputStream(file);
                            InputStreamReader read = new InputStreamReader(readIn, "GBK");
                            BufferedReader bufferedReader = new BufferedReader(read);
                            String oneLine= bufferedReader.readLine();
                            while((oneLine= bufferedReader.readLine()) != null){
                                System.out.println(oneLine);
                                String[] line = oneLine.split("\\|", -1);
                                PayPosdzd payPosdzd = PayPosdzd.builder()
                                        .dzrq00(billDate)
                                        .qsrq00(line[0].trim())
                                        .shh000(line[1].trim())
                                        .zdh000(line[2].trim())
                                        .jylx00(line[3].trim())
                                        .lsh000(line[4].trim())
                                        .xtckh0(line[5].trim())
                                        .jysj00(line[6].trim())
                                        .kh0000(line[7].trim())
                                        .fkh000(line[8].trim())
                                        .jyje00(Math.abs(Double.valueOf(line[9].trim())))
                                        .sxf000(Math.abs(Double.valueOf(line[10].trim())))
                                        .qsje00(Math.abs(Double.valueOf(line[11].trim())))
                                        .build();
                                jdbcGateway.insert("pay.posdzd.insertSelective", payPosdzd);
                            }
                            read.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
        logger.info("==========自动下载银联POS对账文件【结束】==========");
    }

    //重新对账
    @RequestMapping("/billCheck")
    public void BillCheck(String billDate) {
        logger.info("==========修改对账标志【开始】==========");
        // 支付宝对账单
        jdbcGateway.update("pay.zfbdzd.updateByDzrq00", billDate);
        // 微信对账单
        jdbcGateway.update("pay.wxdzd0.updateByDzrq00", billDate);
        // POS对账单
        jdbcGateway.update("pay.posdzd.updateByDzrq00", billDate);
        // 服务渠道对账单
        jdbcGateway.update("pay.fwdzd0.updateByDzrq00", billDate);
        logger.info("==========修改对账标志【结束】==========");
        logger.info("==========自动对账【开始】==========");
        // 调用存储过程进行对账
        jdbcGateway.call("pay.dzmxb0.callSP_PAY_BILL", billDate);
        logger.info("==========自动对账【结束】==========");
        logger.info("==========对账汇总数据统计【开始】==========");
        // 插入对账汇总数据
        jdbcGateway.delete("pay.dzhzb0.delete", billDate);
        jdbcGateway.insert("pay.dzhzb0.insert", billDate);
        // 插入服务渠道对账汇总数据
        jdbcGateway.delete("pay.fwdzhz.delete", billDate);
        jdbcGateway.insert("pay.fwdzhz.insert", billDate);
        // 插入支付渠道对账汇总数据
        jdbcGateway.delete("pay.zfdzhz.delete", billDate);
        jdbcGateway.insert("pay.zfdzhz.insert", billDate);
        logger.info("==========对账汇总数据统计【结束】==========");
    }
}
