package com.wechat.ferry.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;

public class ImgUtils {
    public static String encodeFileToBase64Binary(String fileName) throws IOException {
        File file = new File(fileName);
        FileInputStream fileInputStreamReader = new FileInputStream(file);
        byte[] bytes = new byte[(int) file.length()];
        fileInputStreamReader.read(bytes);

        // 使用Base64编码
        String encodedfile = Base64.getEncoder().encodeToString(bytes);
        fileInputStreamReader.close();

        return encodedfile;
    }
}
