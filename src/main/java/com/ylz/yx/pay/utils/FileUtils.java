package com.ylz.yx.pay.utils;

import com.ylz.yx.pay.payment.channel.unionpay.utils.SDKConstants;
import com.ylz.yx.pay.payment.channel.unionpay.utils.SDKUtil;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtils {

    private final static Logger logger = LoggerFactory.getLogger(FileUtils.class);

    /**
     * 功能：解析交易返回的fileContent字符串并落地 （ 解base64，解DEFLATE压缩并落地）<br>
     * 适用到的交易：对账文件下载，批量交易状态查询<br>
     * @param data 返回报文map<br>
     * @param fileDirectory 落地的文件目录（绝对路径）
     * @param encoding 上送请求报文域encoding字段的值<br>
     */
    public static String deCodeFileContent(Map<String, String> data, String fileDirectory, String encoding) {
        // 解析返回文件
        String filePath = null;
        String fileContent = data.get(SDKConstants.param_fileContent);
        if (null != fileContent && !"".equals(fileContent)) {
            FileOutputStream out = null;
            try {
                byte[] fileArray = SDKUtil.inflater(Base64.decodeBase64(fileContent));
                if (SDKUtil.isEmpty(data.get("fileName"))) {
                    filePath = fileDirectory + data.get("merId")
                            + "_" + data.get("batchNo") + "_"
                            + data.get("txnTime") + ".txt";
                } else {
                    filePath = fileDirectory + data.get("fileName");
                }
                File file = new File(filePath);
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();
                out = new FileOutputStream(file);
                out.write(fileArray, 0, fileArray.length);
                out.flush();
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }finally{
                try {
                    out.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return filePath;
    }

    public static List<String> unzip(String zipFilePath, String outPutDirectory){
        List<String> fileList = new ArrayList<String>();
        try {
            ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFilePath));//输入源zip路径
            BufferedInputStream bin = new BufferedInputStream(zin);
            BufferedOutputStream bout = null;
            File file=null;
            ZipEntry entry;
            try {
                while((entry = zin.getNextEntry())!=null && !entry.isDirectory()){
                    file = new File(outPutDirectory,entry.getName());
                    if(!file.exists()){
                        (new File(file.getParent())).mkdirs();
                    }
                    bout = new BufferedOutputStream(new FileOutputStream(file));
                    int b;
                    while((b=bin.read())!=-1){
                        bout.write(b);
                    }
                    bout.flush();
                    fileList.add(file.getAbsolutePath());
                    System.out.println(file+"解压成功");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally{
                try {
                    bin.close();
                    zin.close();
                    if(bout!=null){
                        bout.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return fileList;
    }

    /**
     * 功能：解析全渠道商户对账文件中的ZM文件并以List<Map>方式返回
     * 适用交易：对账文件下载后对文件的查看
     * @param filePath ZM文件全路径
     * @return 包含每一笔交易中 序列号 和 值 的map序列
     */
    public static List<Map> parseZMFile(String filePath){
        int[] lengthArray = {3,11,11,6,10,19,12,4,2,21,2,32,2,6,10,13,13,4,15,2,2,6,2,4,32,1,21,15,1,15,32,13,13,8,32,13,13,12,2,1,32,98};
        return parseFile(filePath,lengthArray);
    }

    /**
     * 功能：解析全渠道商户对账文件中的ZME文件并以List<Map>方式返回
     * 适用交易：对账文件下载后对文件的查看
     * @param filePath ZME文件全路径
     * @return 包含每一笔交易中 序列号 和 值 的map序列
     */
    public static List<Map> parseZMEFile(String filePath){
        int[] lengthArray = {3,11,11,6,10,19,12,4,2,2,6,10,4,12,13,13,15,15,1,12,2,135};
        return parseFile(filePath,lengthArray);
    }

    /**
     * 功能：解析全渠道商户 ZM,ZME对账文件
     * @param filePath
     * @param lengthArray 参照《全渠道平台接入接口规范 第3部分 文件接口》 全渠道商户对账文件 6.1 ZM文件和6.2 ZME 文件 格式的类型长度组成int型数组
     * @return
     */
    private static List<Map> parseFile(String filePath, int[] lengthArray){
        List<Map> ZmDataList = new ArrayList<Map>();
        try {
            String encoding="gbk"; //文件是gbk编码
            File file=new File(filePath);
            if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file), StandardCharsets.ISO_8859_1);
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while((lineTxt = bufferedReader.readLine()) != null){
                    byte[] bs = lineTxt.getBytes(StandardCharsets.ISO_8859_1);
                    //解析的结果MAP，key为对账文件列序号，value为解析的值
                    Map<Integer,String> ZmDataMap = new LinkedHashMap<Integer,String>();
                    //左侧游标
                    int leftIndex = 0;
                    //右侧游标
                    int rightIndex = 0;
                    for(int i=0;i<lengthArray.length;i++){
                        rightIndex = leftIndex + lengthArray[i];
                        String filed = new String(Arrays.copyOfRange(bs, leftIndex,rightIndex), encoding);
                        leftIndex = rightIndex+1;
                        ZmDataMap.put(i, filed);
                    }
                    ZmDataList.add(ZmDataMap);
                }
                read.close();
            }else{
                System.out.println("找不到指定的文件");
            }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }

        return ZmDataList;
    }

    public static String getFileContentTable(List<Map> dataList,String file){
        StringBuffer  tableSb = new StringBuffer("对账文件的规范参考 https://open.unionpay.com/ajweb/help/file/ 产品接口规范->平台接口规范:文件接口</br> 文件【"+file + "】解析后内容如下：");
        tableSb.append("<table border=\"1\">");
        if(dataList.size() > 0){
            Map<Integer,String> dataMapTmp = dataList.get(0);
            tableSb.append("<tr>");
            for(Iterator<Integer> it = dataMapTmp.keySet().iterator();it.hasNext();){
                Integer key = it.next();
                String value = dataMapTmp.get(key);
                System.out.println("序号："+ (key+1) + " 值: '"+ value +"'");
                tableSb.append("<td>序号"+(key+1)+"</td>");
            }
            tableSb.append("</tr>");
        }

        for(int i=0;i<dataList.size();i++){
            System.out.println("行数: "+ (i+1));
            Map<Integer,String> dataMapTmp = dataList.get(i);
            tableSb.append("<tr>");
            for(Iterator<Integer> it = dataMapTmp.keySet().iterator();it.hasNext();){
                Integer key = it.next();
                String value = dataMapTmp.get(key);
                System.out.println("序号："+ (key+1) + " 值: '"+ value +"'");
                tableSb.append("<td>"+value+"</td>");
            }
            tableSb.append("</tr>");
        }
        tableSb.append("</table>");
        return tableSb.toString();
    }

    /**
     * @Author：
     * @Description：获取某个目录下所有直接下级文件，不包括目录下的子目录的下的文件，所以不用递归获取
     * @Date：
     */
    public static List<String> getFiles(String path) {
        List<String> files = new ArrayList<String>();
        File file = new File(path);
        File[] tempList = file.listFiles();

        for (int i = 0; i < tempList.length; i++) {
            if (tempList[i].isFile()) {
                files.add(tempList[i].toString());
            }
            if (tempList[i].isDirectory()) {
                //这里就不递归了
            }
        }
        return files;
    }
}
