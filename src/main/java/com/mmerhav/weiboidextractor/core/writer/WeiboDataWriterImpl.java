package com.mmerhav.weiboidextractor.core.writer;

import ch.qos.logback.core.util.FileUtil;
import com.google.gson.Gson;
import com.mmerhav.weiboidextractor.core.extractor.WeiboData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class WeiboDataWriterImpl implements WeiboDataWriter {

    @Value("${output.path}")
    private String outputPath;

    @Autowired
    private Gson gson;

    @Override
    public void write(List<WeiboData> weiboDataList) throws IOException {
        File outputFile = new File(outputPath + "/output.txt");
        FileUtil.createMissingParentDirectories(outputFile);
        boolean isOK = outputFile.createNewFile();
        if(isOK) {
            FileWriter fileWriter = new FileWriter(outputFile);
            fileWriter.write(gson.toJson(weiboDataList));
        } else {
            log.info("Failed to create output file:{}", outputFile.getAbsolutePath());
        }
    }
}
