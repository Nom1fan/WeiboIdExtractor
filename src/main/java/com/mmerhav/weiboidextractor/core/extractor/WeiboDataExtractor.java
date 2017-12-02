package com.mmerhav.weiboidextractor.core.extractor;

import com.mmerhav.weiboidextractor.core.reader.WeiboRawData;

import java.util.List;

public interface WeiboDataExtractor {

    List<WeiboData> extract(List<WeiboRawData> weiboRawDataList);
}
