package com.mmerhav.weiboidextractor.core.reader;

import java.io.InputStream;
import java.util.List;

public interface WeiboRawDataReader {

    List<WeiboRawData> read(InputStream inputStream);
}
