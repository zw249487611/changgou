package com.changgou.controller;

import com.changgou.file.FastDFSFile;
import com.changgou.goods.entity.Result;
import com.changgou.goods.entity.StatusCode;
import com.changgou.util.FastDFSUtil;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "/upload")
@CrossOrigin
public class FileUploadController {
    /**
     * 文件上传
     */
    @PostMapping
    public Result upload(@RequestParam(value = "file") MultipartFile file) throws Exception {
        //封装文件对象
        FastDFSFile fastDFSFile = new FastDFSFile(
                file.getOriginalFilename(),//文件名称:1.jpg
                file.getBytes(),//文件字节数组
                StringUtils.getFilenameExtension(file.getOriginalFilename()) //获取文件扩展名
        );

        //调用fastDFSUtil工具类将文件传入FastDFS中
        String upload = FastDFSUtil.upload(fastDFSFile);

        String url = FastDFSUtil.getTrackerInfo() + "/" + upload;

        return new Result(true, StatusCode.OK, "上传成功",url);
    }
}
