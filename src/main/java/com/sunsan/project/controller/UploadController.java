package com.sunsan.project.controller;


import com.sunsan.framework.util.TimeString;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UploadController {


    private String getUploadPath(int num) {
        //num,1：表示上传图片，2：表示上传视频，3：表示上传文档,4:表示上传PDF
        String path = "";
        if (StringUtils.isEmpty(path)) {
            path = new File(SystemUtils.getUserDir(), "upload").getAbsolutePath();
        }
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        String dateStr = String.format("%s%02d", year, month);
        path = Paths.get(path, dateStr).toAbsolutePath().toString();
        new File(path).mkdirs();
        return path;
    }

    private String getFileName(String oldFileName) {
        String ext = "." + FilenameUtils.getExtension(oldFileName);
        String fileName = new TimeString().getTimeString() + "_" + RandomUtils.nextInt(1000, 9000) + ext;
        return fileName;
    }

    @ApiOperation(value = "上传文件", notes = "", authorizations = {@Authorization(value = "api_key")})
    @ApiResponses(value = {@ApiResponse(code = 200, message = "successful")})
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> upload(@RequestPart(value = "file") MultipartFile file) throws Exception {
        File saveFile = new File(getUploadPath(1), getFileName(file.getOriginalFilename()));
        file.transferTo(saveFile);
        return ResponseEntity.ok(null);
    }


}
