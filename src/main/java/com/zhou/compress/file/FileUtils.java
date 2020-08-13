package com.zhou.compress.file;

import com.zhou.compress.util.StringUtil;

import java.io.File;

public class FileUtils {
    // 遍历文件夹
    public static void eachFileDir(String filePath, CallBack callBack) {
        if (isDir(filePath)){
            String[] filePaths = new File(filePath).list();
            if (filePaths != null) {
                for (String tempFilePath : filePaths) {
                    if (isDir(tempFilePath)) {
                        eachFileDir(tempFilePath, callBack);
                    } else {
                        checkImageFileAndCallBack(tempFilePath, callBack);
                    }
                }
            }
        }else if (exists(filePath)){
            checkImageFileAndCallBack(filePath, callBack);
        }
    }

    // 将图片路径返回
    private static void checkImageFileAndCallBack(String path, CallBack callBack) {
        if (path.toUpperCase().endsWith(".JPG") || path.toUpperCase().endsWith(".PNG")) {
            callBack.call(path);
        }
    }

    // 是否为文件目录
    public static boolean isDir(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.isDirectory();
    }

    // 文件是否存在
    public static boolean exists(String filePath){
        File file = new File(filePath);
        return file.exists() ;
    }

    public static boolean isImage(String filePath){
        if (!StringUtil.isEmpty(filePath)){
            String upperCasePath = filePath.toUpperCase();
            if (upperCasePath.endsWith(".JPG") || upperCasePath.endsWith(".PNG") || upperCasePath.endsWith(".JPEG")){
                return true;
            }
        }
        return false;
    }

    public static long getFileSize(String path) {
        if (StringUtil.isEmpty(path)){
            return 0 ;
        }else {
            File file = new File(path);
            if (file.exists() && file.isDirectory()){
                return 0;
            }else if (file.exists()){
                return file.length();
            }
        }
        return 0;
    }
}
