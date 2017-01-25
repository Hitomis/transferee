package com.hitomi.transferimage.progress;

public interface ProgressListener {

    void progress(long bytesRead, long contentLength, boolean done);

}
