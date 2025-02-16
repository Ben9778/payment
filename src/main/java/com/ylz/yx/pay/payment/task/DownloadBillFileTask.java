package com.ylz.yx.pay.payment.task;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
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
import com.ylz.yx.pay.core.model.params.pospay.PospayMchParams;
import com.ylz.yx.pay.record.model.PayPosdzd;
import com.ylz.yx.pay.record.model.PayWxdzd0;
import com.ylz.yx.pay.record.model.PayYldzd0;
import com.ylz.yx.pay.record.model.PayZfbdzd;
import com.ylz.yx.pay.core.entity.PayZfqd00;
import com.ylz.yx.pay.core.model.params.alipay.AlipayMchParams;
import com.ylz.yx.pay.core.model.params.union.UnionpayMchParams;
import com.ylz.yx.pay.core.model.params.wxpay.WxpayMchParams;
import com.ylz.yx.pay.core.model.params.ysf.YsfpayMchParams;
import com.ylz.yx.pay.payment.channel.unionpay.utils.UnionHttpUtil;
import com.ylz.yx.pay.payment.channel.unionpay.utils.UnionSignUtils;
import com.ylz.yx.pay.payment.model.AlipayClientWrapper;
import com.ylz.yx.pay.payment.model.WxServiceWrapper;
import com.ylz.yx.pay.system.ftp.model.PayDzdrz0;
import com.ylz.yx.pay.system.ftp.model.XtFtp000;
import com.ylz.yx.pay.utils.DateKit;
import com.ylz.yx.pay.utils.FileUtils;
import com.ylz.yx.pay.utils.HttpClientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 下载对账单定时任务
 */
@Component
@EnableScheduling
@ConditionalOnProperty(value = "enable.scheduling.bill", havingValue = "true")
public class DownloadBillFileTask {

    private final Logger logger = new Logger("pay", "downloadFileBill", DownloadBillFileTask.class.getName());

    private final static ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(10);

    @Autowired
    private JdbcGateway jdbcGateway;
    @Resource
    private ApplicationProperty applicationProperty;


//    @Scheduled(cron = "30 36 8 * * ?")
    public void testBill() throws IOException {

        /*long count = DateKit.getDifferDay("2022-06-30", "2022-07-13");
        int days = (int) count;
        Date date = DateKit.formatDate("2022-06-30");
        for(int ii = 0;ii<=days;ii++) {
            Date toDate = DateKit.addDay(date, ii);
            String billDate = DateKit.DateFormat(toDate,"yyyyMMdd");

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


        }*/
        /*String billDate = "20220715";
        logger.info("==========请求HIS接口直接返回对账单数据【开始】==========");
            //获取需要下载的对账单日期
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
            logger.info("==========请求HIS接口直接返回对账单数据【结束】==========");*/

        //getHisBillFile();

        //DownloadBillFile();

        //BillCheckInfo();

        //BillCheckTotal();

        //DownloadFtpFile();

        logger.info("==========自动下载支付渠道对账文件【开始】==========");
        //查询出商户应用的配置信息
        List<PayZfqd00> payZfqd00List = jdbcGateway.selectList("pay.zfqd00.selectAll");
        for (PayZfqd00 payZfqd00 : payZfqd00List) {
            switch (payZfqd00.getQdbm00()) {
                case CS.IF_CODE.WXPAY:
                    String wxBillDate = "20220912";
                    WxpayMchParams wxpayParams = JSONObject.parseObject(payZfqd00.getQdpz00(), WxpayMchParams.class);
                    scheduledThreadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            WeiXinDownload(wxBillDate, wxpayParams);
                        }
                    });
                    break;
                case CS.IF_CODE.POSPAY:
                    PospayMchParams pospayMchParams = JSONObject.parseObject(payZfqd00.getQdpz00(), PospayMchParams.class);
                    scheduledThreadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            long count = DateKit.getDifferDay("2022-06-30", "2022-07-13");
                            int days = (int) count;
                            Date date = DateKit.formatDate("2022-06-30");
                            for(int i = 0;i<=days;i++) {
                                Date toDate = DateKit.addDay(date, i);
                                String today = DateKit.DateFormat(toDate,"yyyyMMdd");
                                PosDownload(today, pospayMchParams);
                            }
                        }
                    });

                    break;

                default:
                    break;
            }
        }
        logger.info("==========自动下载支付渠道对账文件【结束】==========");
    }


    /**
     * 请求HIS视图获取对账数据
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void getHisViewData() {
        if (applicationProperty.getIsHisViewData()) {
            logger.info("==========请求HIS视图获取对账数据【开始】==========");
            //获取需要下载的对账单日期
            String billDate = DateUtil.format(DateUtil.offsetDay(new Date(), -1).toJdkDate(), "yyyyMMdd");
            jdbcGateway.insert("pay.fwdzd0.insertHisData", billDate);
            logger.info("==========请求HIS视图获取对账数据【结束】==========");
        }
    }

    /**
     * 请求HIS接口直接返回对账单数据
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void getHisBillFile() {
        if (StringUtils.isNotBlank(applicationProperty.getHisBillFileUrl())) {
            logger.info("==========请求HIS接口直接返回对账单数据【开始】==========");
            //获取需要下载的对账单日期
            String billDate = DateUtil.format(DateUtil.offsetDay(new Date(), -1).toJdkDate(), "yyyyMMdd");
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


    /**
     * 请求HIS接口生成对账单文件（HTTP方式）
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void generateFile() {
        logger.info("==========请求HIS接口生成对账文件【开始】==========");
        //获取需要下载的对账单日期
        String billDate = DateUtil.format(DateUtil.offsetDay(new Date(), -1).toJdkDate(), "yyyyMMdd");
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("qqfs00", "1");
        List<XtFtp000> ftp000List = jdbcGateway.selectList("system.ftp000.selectAll", reqMap);
        for (XtFtp000 xtFtp000 : ftp000List) {
            Map<String, String> reqData = new HashMap();
            reqData.put("billDate", billDate);
            Map<String, String> rspData = UnionHttpUtil.post(reqData, xtFtp000.getFtpdz0(), false);

            PayDzdrz0 payDzdrz0 = new PayDzdrz0();
            payDzdrz0.setQqdz00(xtFtp000.getFtpdz0());
            payDzdrz0.setDzrq00(billDate);
            payDzdrz0.setWjmc00(rspData.get("fileName"));
            jdbcGateway.insert("pay.dzdrz0.insertSelective", payDzdrz0);
        }
        logger.info("==========请求HIS接口生成对账文件【结束】==========");
    }

    /**
     * 请求HIS下载生成对账单文件（HTTP方式）
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void downlaodFile() {
        logger.info("==========请求HIS接口下载对账文件【开始】==========");
        //获取需要下载的对账单日期
        String billDate = DateUtil.format(DateUtil.offsetDay(new Date(), -1).toJdkDate(), "yyyyMMdd");
        List<PayDzdrz0> dzdrz0List = jdbcGateway.selectList("pay.dzdrz0.selectAllList");
        for (PayDzdrz0 payDzdrz0 : dzdrz0List) {
            Map<String, String> reqData = new HashMap();
            reqData.put("fileName", payDzdrz0.getWjmc00());
            Map<String, String> rspData = UnionHttpUtil.post(reqData, payDzdrz0.getQqdz00(), false);
            if (rspData != null) {
                payDzdrz0.setDzrq00(billDate);
                payDzdrz0.setWjmc00(payDzdrz0.getWjmc00());
                jdbcGateway.insert("pay.dzdrz0.update", payDzdrz0);
            }

        }
        logger.info("==========请求HIS接口下载对账文件【结束】==========");
    }

    /**
     * 自动下载服务渠道对账文件（FTP方式）
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void DownloadFtpFile() throws IOException {
        logger.info("==========自动下载服务渠道对账文件【开始】==========");
        //获取需要下载的对账单日期
        String billDate = DateUtil.format(DateUtil.offsetDay(new Date(), -1).toJdkDate(), "yyyyMMdd");
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("qqfs00", "0");
        List<XtFtp000> ftp000List = jdbcGateway.selectList("system.ftp000.selectAll", reqMap);
        for (XtFtp000 xtFtp000 : ftp000List) {
            String url = xtFtp000.getFtpdz0().replace("ftp://", "");
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
            Ftp ftp = new Ftp(ip, Integer.valueOf(port), xtFtp000.getFtpzh0(), xtFtp000.getFtpmm0());
            // 连接策略(Passive:模式被动;Active:主动模式 )
            ftp.setMode(FtpMode.Active);
            // 下载远程文件(下载指定文件，到指定路径的指定文件)
            ftp.download(dir, xtFtp000.getWjmc00().replace("{date}", billDate), new File(applicationProperty.getFileStoragePath() + "\\billFile\\" + billDate + "\\" + xtFtp000.getWjmc00().replace("{date}", billDate)));
            // 关闭连接
            ftp.close();
        }
        logger.info("==========自动下载服务渠道对账文件【结束】==========");
    }

    /**
     * 解析服务渠道对账文件
     */
    @Scheduled(cron = "0 0 5 * * ?")
    public void AnalysisFtpFile() throws IOException {
        logger.info("==========自动解析服务渠道对账文件【开始】==========");
        //获取需要对账的对账单日期
        String billDate = DateUtil.format(DateUtil.offsetDay(new Date(), -1).toJdkDate(), "yyyyMMdd");
        //遍历目录下的所有文件
        List<String> fileList = FileUtils.getFiles(applicationProperty.getFileStoragePath() + "\\billFile\\" + billDate);
        for (String filepath : fileList) {
            final int[] i = {0};
            // 读取⽂件内容到Stream流中，按⾏读取
            Stream<String> lines = Files.lines(Paths.get(filepath));
            // 随机⾏顺序进⾏数据处理
            lines.forEach(ele -> {
                logger.info("解析数据：" + ele);
                if (i[0] > 0) {
                    String[] line = ele.split(",", -1);
                    PayFwdzd0 payFwdzd0 = PayFwdzd0.builder()
                            .dzrq00(billDate)
                            .fwqdmc(filepath.split("_")[1].replace(".csv", ""))
                            .fwqdid(line[0].trim())
                            .fwddh0(line[1].trim())
                            .xtddh0(line[2].trim())
                            .ywlx00(line[3].trim())
                            .jylx00(line[4].trim())
                            .ddmc00(line[5].trim())
                            .zfje00(line[6].trim())
                            .jysj00(line[7].trim())
                            .hzxm00(line[8].trim())
                            .khzyh0(line[9].trim())
                            .build();
                    jdbcGateway.insert("pay.fwdzd0.insertSelective", payFwdzd0);
                }
                i[0]++;
            });
        }
        logger.info("==========自动解析服务渠道对账文件【结束】==========");
    }

    /**
     * 自动下载支付渠道对账文件
     */
    @Scheduled(cron = "0 15 10 * * ?")
    public void DownloadBillFile() {
        logger.info("==========自动下载支付渠道对账文件【开始】==========");
        //查询出商户应用的配置信息
        List<PayZfqd00> payZfqd00List = jdbcGateway.selectList("pay.zfqd00.selectAll");
        for (PayZfqd00 payZfqd00 : payZfqd00List) {
            switch (payZfqd00.getQdbm00()) {
                case CS.IF_CODE.ALIPAY:
                    String zfbBillDate = DateUtil.format(DateUtil.offsetDay(new Date(), -1).toJdkDate(), "yyyy-MM-dd");
                    AlipayMchParams alipayParams = JSONObject.parseObject(payZfqd00.getQdpz00(), AlipayMchParams.class);
                    scheduledThreadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            AlipayDownload(zfbBillDate, alipayParams);
                        }
                    });

                    break;
                case CS.IF_CODE.WXPAY:
                    String wxBillDate = DateUtil.format(DateUtil.offsetDay(new Date(), -1).toJdkDate(), "yyyyMMdd");
                    WxpayMchParams wxpayParams = JSONObject.parseObject(payZfqd00.getQdpz00(), WxpayMchParams.class);
                    scheduledThreadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            WeiXinDownload(wxBillDate, wxpayParams);
                        }
                    });
                    break;
                case CS.IF_CODE.YSFPAY:
                    String billDate = DateUtil.format(DateUtil.offsetDay(new Date(), -1).toJdkDate(), "yyyyMMdd");
                    YsfpayMchParams ysfpayMchParams = JSONObject.parseObject(payZfqd00.getQdpz00(), YsfpayMchParams.class);
                    break;
                case CS.IF_CODE.UNIONPAY:
                    String ylBillDate = DateUtil.format(DateUtil.offsetDay(new Date(), -1).toJdkDate(), "yyyyMMdd");
                    UnionpayMchParams unionpayMchParams = JSONObject.parseObject(payZfqd00.getQdpz00(), UnionpayMchParams.class);
                    scheduledThreadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            UnionDownload(ylBillDate, unionpayMchParams);
                        }
                    });
                    break;
                case CS.IF_CODE.POSPAY:
                    String posBillDate = DateUtil.format(DateUtil.offsetDay(new Date(), -1).toJdkDate(), "yyyyMMdd");
                    PospayMchParams pospayMchParams = JSONObject.parseObject(payZfqd00.getQdpz00(), PospayMchParams.class);
                    scheduledThreadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            PosDownload(posBillDate, pospayMchParams);
                        }
                    });
                    break;
                default:
                    break;
            }
        }
        logger.info("==========自动下载支付渠道对账文件【结束】==========");
    }


    //@Scheduled(cron = "0 10 10 * * ?")
    public void test() {
        String billDate = DateUtil.format(DateUtil.offsetDay(new Date(), -1).toJdkDate(), "yyyyMMdd");
        String file = applicationProperty.getFileStoragePath() + "/billFile/" + billDate + "/INN" + billDate.substring(2) + "00ZM_777290058198017.txt";
        logger.info("==========对账文件名称==========" + file);
        List<Map> DataList = new ArrayList<>();
        if (file.indexOf("ZM_") != -1) {
            DataList = FileUtils.parseZMFile(file);
        } else if (file.indexOf("ZME_") != -1) {
            DataList = FileUtils.parseZMEFile(file);
        }
        for (Map map : DataList) {
            PayYldzd0 payYldzd0 = PayYldzd0.builder()
                    .dzrq00(billDate)
                    .jydm00(map.get(0).toString().trim())
                    .dljgbs(map.get(1).toString().trim())
                    .fsjgbs(map.get(2).toString().trim())
                    .xtgzh0(map.get(3).toString().trim())
                    .jycssj(map.get(4).toString().trim())
                    .zh0000(map.get(5).toString().trim())
                    .jyje00(Long.valueOf(map.get(6).toString().trim()))
                    .shlb00(map.get(7).toString().trim())
                    .zdlx00(map.get(8).toString().trim())
                    .zfddh0(map.get(9).toString().trim())
                    .zffsj0(map.get(10).toString().trim())
                    .xtddh0(map.get(11).toString().trim())
                    .zfklx0(map.get(12).toString().trim())
                    .yxtgzh(map.get(13).toString().trim())
                    .yjyrq0(map.get(14).toString().trim())
                    .shsxf0(map.get(15).toString().trim())
                    .jsje00(map.get(16).toString().trim())
                    .zffs00(map.get(17).toString().trim())
                    .jtshdm(map.get(18).toString().trim())
                    .jylx00(map.get(19).toString().trim())
                    .jyzl00(map.get(20).toString().trim())
                    .ywlx00(map.get(21).toString().trim())
                    .zhlx00(map.get(22).toString().trim())
                    .zdlx01(map.get(23).toString().trim())
                    .zdhm00(map.get(24).toString().trim())
                    .jhfs00(map.get(25).toString().trim())
                    .yzfddh(map.get(26).toString().trim())
                    .shdm00(map.get(27).toString().trim())
                    .fzrzfs(map.get(28).toString().trim())
                    .ejshdm(map.get(29).toString().trim())
                    .ejshjc(map.get(30).toString().trim())
                    .ejfzrz(map.get(31).toString().trim())
                    .qsje00(map.get(32).toString().trim())
                    .zdh000(map.get(33).toString().trim())
                    .shzdy0(map.get(34).toString().trim())
                    .yhje00(map.get(35).toString().trim())
                    .fpje00(map.get(36).toString().trim())
                    .fqfksx(map.get(37).toString().trim())
                    .fqfkqs(map.get(38).toString().trim())
                    .jyjz00(map.get(39).toString().trim())
                    .yxtddh(map.get(40).toString().trim())
                    .blsy00(map.get(41).toString().trim())
                    .build();
            jdbcGateway.insert("pay.yldzd0.insertSelective", payYldzd0);
        }
    }

    /**
     * 下载支付渠道对账文件（补偿机制）
     */
    @Scheduled(cron = "0 30,45 10 * * ?")
    public void CompensateDownloadBillFile() {
        logger.info("==========自动下载支付渠道对账文件（补偿机制）【开始】==========");
        //获取需要下载的对账单日期
        String billDate = DateUtil.format(DateUtil.offsetDay(new Date(), -1).toJdkDate(), "yyyyMMdd");
        //查询出商户应用的配置信息
        List<PayZfqd00> payZfqd00List = jdbcGateway.selectList("pay.zfqd00.selectAll");
        for (PayZfqd00 payZfqd00 : payZfqd00List) {
            switch (payZfqd00.getQdbm00()) {
                case CS.IF_CODE.ALIPAY:
                    String zfbBillDate = DateUtil.format(DateUtil.offsetDay(new Date(), -1).toJdkDate(), "yyyy-MM-dd");
                    Integer zfbCount = jdbcGateway.selectOne("pay.zfbdzd.selectCount", billDate);
                    if (zfbCount == 0) {
                        AlipayMchParams alipayParams = JSONObject.parseObject(payZfqd00.getQdpz00(), AlipayMchParams.class);
                        scheduledThreadPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                AlipayDownload(zfbBillDate, alipayParams);
                            }
                        });
                    }
                    break;
                case CS.IF_CODE.WXPAY:
                    String wxBillDate = DateUtil.format(DateUtil.offsetDay(new Date(), -1).toJdkDate(), "yyyyMMdd");
                    Integer wxCount = jdbcGateway.selectOne("pay.wxdzd0.selectCount", billDate);
                    if (wxCount == 0) {
                        WxpayMchParams wxpayParams = JSONObject.parseObject(payZfqd00.getQdpz00(), WxpayMchParams.class);
                        scheduledThreadPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                WeiXinDownload(wxBillDate, wxpayParams);
                            }
                        });
                    }
                    break;
                case CS.IF_CODE.YSFPAY:
                    YsfpayMchParams ysfpayMchParams = JSONObject.parseObject(payZfqd00.getQdpz00(), YsfpayMchParams.class);
                    break;
                case CS.IF_CODE.UNIONPAY:
                    String ylBillDate = DateUtil.format(DateUtil.offsetDay(new Date(), -1).toJdkDate(), "yyyyMMdd");
                    Integer ylCount = jdbcGateway.selectOne("pay.yldzd0.selectCount", billDate);
                    if (ylCount == 0) {
                        UnionpayMchParams unionpayMchParams = JSONObject.parseObject(payZfqd00.getQdpz00(), UnionpayMchParams.class);
                        scheduledThreadPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                UnionDownload(ylBillDate, unionpayMchParams);
                            }
                        });
                    }
                    break;
                case CS.IF_CODE.POSPAY:
                    String posBillDate = DateUtil.format(DateUtil.offsetDay(new Date(), -1).toJdkDate(), "yyyyMMdd");
                    Integer posCount = jdbcGateway.selectOne("pay.posdzd.selectCount", billDate);
                    if (posCount == 0) {
                        PospayMchParams pospayMchParams = JSONObject.parseObject(payZfqd00.getQdpz00(), PospayMchParams.class);
                        scheduledThreadPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                PosDownload(posBillDate, pospayMchParams);
                            }
                        });
                    }
                    break;
                default:
                    break;
            }
        }
        logger.info("==========自动下载支付渠道对账文件（补偿机制）【结束】==========");
    }

    /**
     * 自动对账
     */
    @Scheduled(cron = "0 15 11 * * ?")
    public void BillCheckInfo() {
        logger.info("==========自动对账【开始】==========");
        //获取需要下载的对账单日期
        String billDate = DateUtil.format(DateUtil.offsetDay(new Date(), -1).toJdkDate(), "yyyyMMdd");
        // 调用存储过程进行对账
        jdbcGateway.call("pay.dzmxb0.callSP_PAY_BILL", billDate);
        logger.info("==========自动对账【结束】==========");
    }

    /**
     * 对账汇总数据统计
     */
    @Scheduled(cron = "0 30 11 * * ?")
    public void BillCheckTotal() {
        logger.info("==========对账汇总数据统计【开始】==========");
        //获取对账单日期
        String billDate = DateUtil.format(DateUtil.offsetDay(new Date(), -1).toJdkDate(), "yyyyMMdd");
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

    /**
     * 下载微信对账文件
     *
     * @param billDate    对账日期
     * @param wxpayParams
     */
    public void WeiXinDownload(String billDate, WxpayMchParams wxpayParams) {
        logger.info("==========自动下载微信对账对账文件【开始】==========");
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
        logger.info("==========自动下载微信支付对账文件【结束】==========");
    }

    /**
     * 下载支付宝对账文件
     *
     * @param billDate     对账日期
     * @param alipayParams
     */
    public void AlipayDownload(String billDate, AlipayMchParams alipayParams) {
        logger.info("==========自动下载支付宝对账文件【开始】==========");
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
        logger.info("==========自动下载支付宝对账文件【结束】==========");
    }

    public void UnionDownload(String billDate, UnionpayMchParams unionpayMchParams) {
        logger.info("==========自动下载银联对账文件【开始】==========");
        Map<String, String> data = new HashMap<String, String>();

        /***银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改***/
        data.put("version", unionpayMchParams.getVersion());               //版本号 全渠道默认值
        data.put("encoding", "UTF-8");             //字符集编码 可以使用UTF-8,GBK两种方式
        data.put("signMethod", unionpayMchParams.getSignMethod()); //签名方法
        data.put("txnType", "76");                           //交易类型 76-对账文件下载
        data.put("txnSubType", "01");                        //交易子类型 01-对账文件下载
        data.put("bizType", "000000");                       //业务类型，固定

        /***商户接入参数***/
        data.put("accessType", "0");                         //接入类型，商户接入填0，不需修改
        data.put("merId", unionpayMchParams.getMerId());                             //商户代码，请替换正式商户号测试，如使用的是自助化平台注册的777开头的商户号，该商户号没有权限测文件下载接口的，请使用测试参数里写的文件下载的商户号和日期测。如需777商户号的真实交易的对账文件，请使用自助化平台下载文件。
        data.put("settleDate", billDate.substring(4));                  //清算日期，如果使用正式商户号测试则要修改成自己想要获取对账文件的日期， 测试环境如果使用700000000000001商户号则固定填写0119
        data.put("txnTime", DateUtil.format(new Date(), DatePattern.PURE_DATETIME_PATTERN));       //订单发送时间，取系统时间，格式为YYYYMMDDhhmmss，必须取当前时间，否则会报txnTime无效
        data.put("fileType", "00");                          //文件类型，一般商户填写00即可

        /**请求参数设置完毕，以下对请求参数进行签名并发送http post请求，接收同步应答报文------------->**/
        //签名
        String isvPrivateCertFile = applicationProperty.getFileStoragePath() + unionpayMchParams.getPrivateCertFile();
        String isvPrivateCertPwd = unionpayMchParams.getPrivateCertPwd();
        Map<String, String> reqData = UnionSignUtils.signByCertInfo(data, isvPrivateCertFile, isvPrivateCertPwd, "UTF-8");             //报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可。

        // 调起上游接口
        Map<String, String> rspData = UnionHttpUtil.post(reqData, unionpayMchParams.getFileUrl(), unionpayMchParams.isIfValidateRemoteCert()); //发送请求报文并接受同步应答（默认连接超时时间30秒，读取返回结果超时时间30秒）;这里调用signData之后，调用submitUrl之前不能对submitFromData中的键值对做任何修改，如果修改会导致验签不通过
        /**对应答码的处理，请根据您的业务逻辑来编写程序,以下应答码处理逻辑仅供参考------------->**/

        //应答码规范参考open.unionpay.com帮助中心 下载  产品接口规范  《平台接入接口规范-第5部分-附录》
        if (!rspData.isEmpty()) {
            if (UnionSignUtils.validate(rspData, applicationProperty.getFileStoragePath(), unionpayMchParams, "UTF-8")) {
                logger.info("验证签名成功");
                String respCode = rspData.get("respCode");
                if ("00".equals(respCode)) {
                    String outPutDirectory = applicationProperty.getFileStoragePath() + "/billFile/unionpay/" + billDate + "/";
                    // 交易成功，解析返回报文中的fileContent并落地
                    String zipFilePath = FileUtils.deCodeFileContent(rspData, outPutDirectory, "UTF-8");
                    //对落地的zip文件解压缩并解析
                    List<String> fileList = FileUtils.unzip(zipFilePath, outPutDirectory);
                    //解析ZM，ZME文件
                    for (String file : fileList) {
                        List<Map> DataList = new ArrayList<>();
                        if (file.indexOf("ZM_") != -1) {
                            DataList = FileUtils.parseZMFile(file);
                        } else if (file.indexOf("ZME_") != -1) {
                            DataList = FileUtils.parseZMEFile(file);
                        }
                        for (Map map : DataList) {
                            PayYldzd0 payYldzd0 = PayYldzd0.builder()
                                    .dzrq00(billDate)
                                    .jydm00(map.get(0).toString())
                                    .dljgbs(map.get(1).toString())
                                    .fsjgbs(map.get(2).toString())
                                    .xtgzh0(map.get(3).toString())
                                    .jycssj(map.get(4).toString())
                                    .zh0000(map.get(5).toString())
                                    .jyje00(Long.valueOf(map.get(6).toString()))
                                    .shlb00(map.get(7).toString())
                                    .zdlx00(map.get(8).toString())
                                    .zfddh0(map.get(9).toString())
                                    .zffsj0(map.get(10).toString())
                                    .xtddh0(map.get(11).toString())
                                    .zfklx0(map.get(12).toString())
                                    .yxtgzh(map.get(13).toString())
                                    .yjyrq0(map.get(14).toString())
                                    .shsxf0(map.get(15).toString())
                                    .jsje00(map.get(16).toString())
                                    .zffs00(map.get(17).toString())
                                    .jtshdm(map.get(18).toString())
                                    .jylx00(map.get(19).toString())
                                    .jyzl00(map.get(20).toString())
                                    .ywlx00(map.get(21).toString())
                                    .zhlx00(map.get(22).toString())
                                    .zdlx01(map.get(23).toString())
                                    .zdhm00(map.get(24).toString())
                                    .jhfs00(map.get(25).toString())
                                    .yzfddh(map.get(26).toString())
                                    .shdm00(map.get(27).toString())
                                    .fzrzfs(map.get(28).toString())
                                    .ejshdm(map.get(29).toString())
                                    .ejshjc(map.get(30).toString())
                                    .ejfzrz(map.get(31).toString())
                                    .qsje00(map.get(32).toString())
                                    .zdh000(map.get(33).toString())
                                    .shzdy0(map.get(34).toString())
                                    .yhje00(map.get(35).toString())
                                    .fpje00(map.get(36).toString())
                                    .fqfksx(map.get(37).toString())
                                    .fqfkqs(map.get(38).toString())
                                    .jyjz00(map.get(39).toString())
                                    .yxtddh(map.get(40).toString())
                                    .blsy00(map.get(41).toString())
                                    .build();
                            jdbcGateway.insert("pay.yldzd0.insertSelective", payYldzd0);
                        }
                    }
                    //TODO
                } else {
                    //其他应答码为失败请排查原因
                    //TODO
                }
            } else {
                logger.error("验证签名失败");
                //TODO 检查验证签名失败的原因
            }
        } else {
            //未返回正确的http状态
            logger.error("未获取到返回报文或返回http状态码非200");
        }
        logger.info("==========自动下载银联对账文件【结束】==========");
    }

    private void PosDownload(String billDate, PospayMchParams pospayMchParams) {
        logger.info("==========自动下载银联POS对账文件【开始】==========");
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

                /*Stream<String> lines = Files.lines(Paths.get(filepath));
                // 随机⾏顺序进⾏数据处理
                lines.forEach(ele -> {
                    logger.info("解析POS对账数据：" + ele);
                    if (i[0] > 0) {
                        String[] line = ele.split("\\|", -1);
                        PayPosdzd payPosdzd = PayPosdzd.builder()
                                .dzrq00(billDate)
                                .qsrq00(line[0].trim())
                                .shh000(line[1].trim())
                                .zdh000(line[2].trim())
                                .lsh000(line[3].trim())
                                .xtckh0(line[4].trim())
                                .jysj00(line[5].trim())
                                .kh0000(line[6].trim())
                                .fkh000(line[7].trim())
                                .jyje00(Math.abs(Double.valueOf(line[8].trim())))
                                .sxf000(Math.abs(Double.valueOf(line[9].trim())))
                                .qsje00(Math.abs(Double.valueOf(line[10].trim())))
                                .build();
                        jdbcGateway.insert("pay.posdzd.insertSelective", payPosdzd);
                    }
                    i[0]++;
                });*/
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("==========自动下载银联POS对账文件【结束】==========");
    }
}
