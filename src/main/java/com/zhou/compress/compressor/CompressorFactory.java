package com.zhou.compress.compressor;

import com.zhou.compress.file.PropertiesUtil;

import static com.zhou.compress.file.PropertiesUtil.COMPRESSOR_OPTI_PNG;
import static com.zhou.compress.file.PropertiesUtil.COMPRESSOR_TINYPNG;

public class CompressorFactory implements ICompressor {

    private ICompressor compressor ;

    private CompressorFactory() {
        String compressorValue = PropertiesUtil.get().getCompressor();
        if (COMPRESSOR_TINYPNG.equalsIgnoreCase(compressorValue)){
            compressor = new TinypngCompressor();
        }else if (COMPRESSOR_OPTI_PNG.equalsIgnoreCase(compressorValue)){
            compressor = new OptipngCompressor();
        }
    }

    private static CompressorFactory instance;
    public static CompressorFactory get(){
        if (instance == null){
            instance = new CompressorFactory();
        }
        return instance;
    }

    @Override
    public void setPath(String inPath, String outPath) {
        compressor.setPath(inPath,outPath);
    }

    @Override
    public long getAvailableCount() {
        return compressor.getAvailableCount();
    }

    @Override
    public long getRawFileSize() {
        return compressor.getRawFileSize();
    }

    @Override
    public long getCompressedFileSize() {
        return compressor.getCompressedFileSize();
    }

    @Override
    public boolean isCompressSuccess() {
        return compressor.isCompressSuccess();
    }

    @Override
    public boolean check() {
        return compressor.check();
    }

    @Override
    public long caculateRawFileSize() {
        return compressor.caculateRawFileSize();
    }

    @Override
    public boolean compress() {
        return compressor.compress();
    }

    @Override
    public long caculateCompressedFileSize() {
        return compressor.caculateCompressedFileSize();
    }

    @Override
    public void done() {
        compressor.done();
    }
}
