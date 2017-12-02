package com.mmerhav.weiboidextractor.core.writer;

import com.google.gson.Gson;
import com.mmerhav.weiboidextractor.core.extractor.WeiboData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Component
public class WeiboDataWriterImpl implements WeiboDataWriter {

    @Value("${output.path}")
    private String outputPath;

    @Autowired
    private Gson gson;

    @Override
    public void write(List<WeiboData> weiboDataList) throws IOException {
        FileWriter fileWriter = new FileWriter(outputPath + "/output.txt");
        fileWriter.write(gson.toJson(weiboDataList));
    }
}
