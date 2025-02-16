package com.ylz.yx.pay.file.controller;

import cn.hutool.core.lang.UUID;
import com.ylz.yx.pay.file.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileService fileService;

    @RequestMapping("/upload")
    public Object singleFileUpload(MultipartFile file, String fileType) {
        String fileName = file.getOriginalFilename();
        String fileSuffix = fileName.substring(fileName.lastIndexOf('.')+1);
        // 新文件地址
        String saveDirAndFileName = fileType + "/" + UUID.fastUUID() + "." + fileSuffix;
        return fileService.saveFile(file, saveDirAndFileName);
    }
}
