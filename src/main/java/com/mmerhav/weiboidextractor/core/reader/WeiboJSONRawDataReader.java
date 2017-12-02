package com.mmerhav.weiboidextractor.core.reader;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Component
public class WeiboJSONRawDataReader implements WeiboRawDataReader {

    @Autowired
    private Gson gson;

    @Override
    public List<WeiboRawData> read(InputStream inputStream) {
        List<WeiboRawData> weiboRawDataList = new ArrayList<>();
        Type listType = new TypeToken<List<String>>(){}.getType();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        List<String> nicknames = gson.fromJson(inputStreamReader, listType);

        nicknames.forEach(nickName -> weiboRawDataList.add(new WeiboRawData(nickName)));
        return weiboRawDataList;
    }
}
