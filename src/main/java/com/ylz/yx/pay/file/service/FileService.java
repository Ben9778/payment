package com.ylz.yx.pay.file.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    Object saveFile(MultipartFile file, String saveDirAndFileName);
}
