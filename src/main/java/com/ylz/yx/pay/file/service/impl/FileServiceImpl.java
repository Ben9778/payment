package com.ylz.yx.pay.file.service.impl;

import com.ylz.svc.web.HttpResponse;
import com.ylz.yx.pay.config.ApplicationProperty;
import com.ylz.yx.pay.file.service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Service
public class FileServiceImpl implements FileService {

    @Resource
    private ApplicationProperty applicationProperty;

    @Override
    public Object saveFile(MultipartFile file, String saveDirAndFileName) {
        try {

            String savePath = applicationProperty.getFileStoragePath();

            File saveFile = new File(savePath + saveDirAndFileName);

            //如果文件夹不存在则创建文件夹
            File dir = saveFile.getParentFile();
            if(!dir.exists()) {
                dir.mkdirs();
            }
            file.transferTo(saveFile);

        } catch (Exception e) {

        }
        Map<String, Object> map = new HashMap();
        if(saveDirAndFileName.contains("img")){
            map.put("fileName", "/uploadFiles/file/"+saveDirAndFileName);
        } else {
            map.put("fileName", saveDirAndFileName);
        }
        return new HttpResponse(map);
    }
}
