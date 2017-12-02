package com.mmerhav.weiboidextractor.core.writer;

import com.mmerhav.weiboidextractor.core.extractor.WeiboData;

import java.io.IOException;
import java.util.List;

public interface WeiboDataWriter {

    void write(List<WeiboData> weiboDataList) throws IOException;
}
