package com.zhou.compress.compressor;

public interface ICompressor {

    public void setPath(String inPath,String outPath);

    public long getAvailableCount();

    public long getRawFileSize();

    public long getCompressedFileSize();

    public boolean isCompressSuccess();

    public boolean check();

    public long caculateRawFileSize();

    public boolean compress();

    public long caculateCompressedFileSize();

    public void done();
}
