package com.moneysupermarket.controller;

import java.io.BufferedInputStream;
import java.io.IOException;

import com.moneysupermarket.service.StorageService;

import java.io.InputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Created by brynach.jones on 28/11/2017.
 */
@Controller
public class UploadPageController {

    @Autowired
    StorageService storageService;

    private static String detail;

    @RequestMapping("/upload")
    public String upload(Model model) {
        return "index";
    }

    @RequestMapping("/upload-file")
    public String uploadFile(@RequestParam(value = "response", required = false, defaultValue = "No Response") String response, Model model) {
        model.addAttribute("response", getDetail());

        return "uploadFile";
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile multipartFile,
                                   RedirectAttributes redirectAttributes) throws IOException {

        InputStream inputStream = new BufferedInputStream(multipartFile.getInputStream());
        String response = storageService.store(inputStream);

        setDetail(response);

        return "redirect:/upload-file?response=" + response;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
