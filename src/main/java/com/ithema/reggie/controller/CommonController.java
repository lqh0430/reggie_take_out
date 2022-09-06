package com.ithema.reggie.controller;

import com.ithema.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

/**
 * @version 1.0
 * @Author LQH02
 * @Description
 * @CreateDate 2022/8/30 17:55
 */
@RestController
@Slf4j
@RequestMapping("/common")
public class CommonController {


    @Value("${reggie.path}")
    private String basePath;


    /**
     * 上传文件
     * //@ReuqusetPart("file") 该注解专门用于指定文件类型, 这样形参列表里的参数就不需要固定为file了
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(@RequestPart("file")MultipartFile file) throws IOException {
        log.info("上传的文件名={}",file.getName());

        //获取上传文件的原始文件名
        String originalFilename = file.getOriginalFilename();//abc.jpg
        //截取出该文件名的后缀
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        //使用UUID + 拼接上后缀 生成新的文件名,防止文件名重复
        String fileName = UUID.randomUUID().toString() + suffix;

        //创建目录对象
        File dir = new File(basePath);
        if (!dir.exists()) {
            //目录不存在,则新建目录
            dir.mkdirs();
        }

        //将图片转存到指定目录下
        file.transferTo(new File(basePath + fileName));

        return R.success(fileName);
    }

    /**
     * 文件下载
     * @param name 上传的文件名
     * @param response
     */
    @GetMapping("/download")
    public void download(String name,HttpServletResponse response) {
        log.info("上传过后要下载的文件名={}",name);

        try {
            //通过输入流读取上传的文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));
            //通过输出流将文件写回浏览器,在浏览器展示图片
            ServletOutputStream outputStream = response.getOutputStream();
            //文件返回格式(设置为图片jpg)
            response.setContentType("image/jpeg");

            int len = 0;
            byte[] bytes = new byte[1024];
            while ( (len = fileInputStream.read(bytes)) != -1 ) {
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }

            //关闭资源
            outputStream.close();
            fileInputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
