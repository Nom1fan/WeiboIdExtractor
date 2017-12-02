package com.mmerhav.weiboidextractor.exec.runner;

import com.mmerhav.weiboidextractor.core.extractor.WeiboData;
import com.mmerhav.weiboidextractor.core.extractor.WeiboDataExtractor;
import com.mmerhav.weiboidextractor.core.reader.WeiboRawData;
import com.mmerhav.weiboidextractor.core.reader.WeiboRawDataReader;
import com.mmerhav.weiboidextractor.core.writer.WeiboDataWriter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

//import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WeiboIdExtractorRunnerImpl implements WeiboIdExtractorRunner {

    @Value("${path.to.json}")
    public String pathToJson;

    @Autowired
    private WeiboRawDataReader reader;

    @Autowired
    private WeiboDataExtractor extractor;

    @Autowired
    private WeiboDataWriter writer;

    @Override
    public void run() throws IOException {

        Resource resource = new ClassPathResource(pathToJson);
        InputStream resourceInputStream = resource.getInputStream();
        List<WeiboRawData> weiboRawDataList = reader.read(resourceInputStream);
        log.info("Read raw data:{}", Arrays.toString(weiboRawDataList.toArray()));

        List<WeiboData> weiboDataList = extractor.extract(weiboRawDataList);
        log.info("Extracted weibo data:" + Arrays.toString(weiboDataList.toArray()));

        writer.write(weiboDataList);
    }
}
