package com.aton.fileUpload;


import com.sun.org.apache.regexp.internal.RE;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class FileUpload {
    
    @RequestMapping("/")
    public String toMainPage() {
        return "uploadPage";
    }
    
    @PostMapping("/upload")
    public ResponseEntity<FileSystemResource> getUploadFile(@RequestParam("fileUploadTxt") MultipartFile txt,
                                                            @RequestParam("fileUploadHtml") MultipartFile html,
                                                            HttpServletResponse resp) throws IOException {
        //接收文件 及其 准备工作
        InputStream txtInputStream = txt.getInputStream();
        InputStream htmlInputStream = html.getInputStream();
        
        InputStreamReader readerTxt = new InputStreamReader(txtInputStream);
        InputStreamReader readerHtml = new InputStreamReader(htmlInputStream);
        
        BufferedReader brTxt = new BufferedReader(readerTxt);
        BufferedReader brHtml = new BufferedReader(readerHtml);
        
        // String str;
        // while ((str = brTxt.readLine()) != null){
        //     System.out.println(str);
        // }
        
        //处理内容 获得位置数字极其文字内容的map
        Map<Integer, String> txtMapResult = txtHandle(brTxt);
        
        //根据map生成新文件
        File file = htmlHandle(brHtml, txtMapResult);
        
        //输出file文件
        ResponseEntity<FileSystemResource> entity = outPut(file);
        return entity;
    
    }
    
    
    //根据 ==========  划分对象
    public Map<Integer, String> txtHandle(BufferedReader brTxt) throws IOException {
        
        //对输入的txt文档逐行找到位置数字与文本
        String temp;
        HashMap<Integer, String> map = new HashMap<>();
        while ((temp = brTxt.readLine()) != null) {
            if (temp.startsWith("- 您在位置 #")) {
                int i = Integer.parseInt(temp.substring(8, 12));
                temp = brTxt.readLine();
                temp = brTxt.readLine();
                map.put(i, temp);
            }
        }
        
        return map;
    }
    
    //处理html  根据map 创建一个新的html文件
    public File htmlHandle(BufferedReader brHtml, Map map) throws IOException {
        // create new file
        File file = new File("./", "temp.html");
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        
        //read brhtml
        String temp;
        int position;
        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        while ((temp = brHtml.readLine()) != null) {
            //写入所有行
            bufferedWriter.write(temp+"\n");
            //对标注行特殊处理
            if (temp.startsWith("标注")) {
                //获得位置信息
                position = Integer.parseInt(temp.substring(52, temp.length()));
                //把标注行写入
                
                //找到了 直接连续写两次
                for (int i = 0; i < 2; i++) {
                    bufferedWriter.write(brHtml.readLine()+"\n");
                }
                //然后写入map中的替换内容
                bufferedWriter.write(String.valueOf(map.get(position))+"\n");
                
            }
            
            bufferedWriter.flush();
        }
        return file;
        
    }
    
    //输出file文件 封装到请求
    public ResponseEntity<FileSystemResource> outPut(File file) {
        // HttpHeaders httpHeaders = new HttpHeaders();
        // httpHeaders
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Content-Disposition", "attachment; filename=" + file.getName());
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        headers.add("Last-Modified", new Date().toString());
        headers.add("ETag", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok().headers(headers).
                contentLength(file.length()).
                contentType(MediaType.parseMediaType("application/octet-stream")).
                body(new FileSystemResource(file));
    }
        //输出
        // ServletOutputStream outputStream = resp.getOutputStream();
        // outputStream.write(res);
        // outputStream.flush();
        // outputStream.close();
        // txtInputStream.close();
        // htmlInputStream.close();
    
}
