package com.zhou.compress.file;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhou.compress.util.StringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PropertiesUtil {

    private static PropertiesUtil instance;
    private PropertiesUtil(){}
    public static PropertiesUtil get(){
        if (instance == null){
            instance = new PropertiesUtil();
        }
        return instance;
    }

    private Properties properties;

    private static final String PROPERTY_NAME = "imageCompressionTool.properties";

    private static final String KEY_LAST_COMMIT_SHA = "lastCompressCommitSHA";
    private static final String KEY_TARGET_COMMIT_SHA = "targetCompressCommitSHA";
    private static final String KEY_TARGET_PATH = "targetPath";
    private static final String KEY_LOG_PATH = "logPath";
    private static final String KEY_TOOL_PATH = "toolPath";
    private static final String KEY_Tinify = "TinifyKey";

    private static final String KEY_COMPRESSOR = "compressor";

    private static final String KEY_HAS_COMPRESSOR_SHA = "has_compressor_sha";

    public static final String COMPRESSOR_TINYPNG = "tinypng";

    public static final String COMPRESSOR_OPTI_PNG = "optipng";

    public void load() {
        String propertiesPath = new File(PROPERTY_NAME).getAbsolutePath();
        properties = new Properties();
        try {
            properties.load(new FileInputStream(propertiesPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getLastCommitSha(){
        return properties.getProperty(KEY_LAST_COMMIT_SHA);
    }

    public String getTargetCommitSha(){
        return properties.getProperty(KEY_TARGET_COMMIT_SHA);
    }

    public String getTargetPath(){
        return properties.getProperty(KEY_TARGET_PATH);
    }

    public String getLogPath(){
        String logPath = getToolPath() + "\\" +  properties.getProperty(KEY_LOG_PATH);
        return logPath;
    }

    public List<String> getTinifyKeyList(){
        List<String> tinifyKeyList = new ArrayList<>();
        String TinifyKey= properties.getProperty(KEY_Tinify);
        if (TinifyKey != null && TinifyKey.length() > 0){
            Type listType = new TypeToken<List<String>>(){}.getType();
            tinifyKeyList = new Gson().fromJson(TinifyKey,listType);
        }
        return tinifyKeyList;
    }

    public String getCompressor(){
        return properties.getProperty(KEY_COMPRESSOR);
    }

    public String getToolPath(){
        String toolPath = properties.getProperty(KEY_TOOL_PATH);
        toolPath = StringUtil.isEmpty(toolPath) ? "" : toolPath;
        return toolPath;
    }

    public void saveHasCompressSHA(String commitSHA){
        properties.setProperty(KEY_LAST_COMMIT_SHA,commitSHA);
        properties.setProperty(KEY_TARGET_COMMIT_SHA,commitSHA);
        String propertiesPath = new File(PROPERTY_NAME).getAbsolutePath();
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(propertiesPath, false);//true表示追加打开
            properties.store(fileOutputStream,"");
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
