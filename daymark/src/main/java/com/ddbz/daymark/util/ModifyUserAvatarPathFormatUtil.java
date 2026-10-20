package com.ddbz.daymark.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * 将前端返回的MultipartFile文件保存到本地，并返回文件名
 */
@Component
@Slf4j
public class ModifyUserAvatarPathFormatUtil{

    @Value("${app.base-url}")
    private String baseUrl;

    /**
     * 将用户上传的图片转换格式保存，并将图片根据名称转换为String类型
     * @param file
     * @return
     * @throws IOException
     */
    public String save(MultipartFile file)throws IOException{
        log.info("正在上传用户头像");

        if(file == null){
            log.info("未检测到图片");
            return null;
        }

        //获取用户上传头像文件名
        String fileName = file.getOriginalFilename();

        //将用户传来的头像名称改写
        String extension = fileName.substring(fileName.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + extension;

        //保存文件
        file.transferTo(new File("D:/zxc/project/Daymark/upload/"+newFileName));
        log.info("成功上传用户头像");
        return baseUrl+newFileName;
    }

}
