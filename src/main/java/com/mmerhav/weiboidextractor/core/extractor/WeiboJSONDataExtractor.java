package com.mmerhav.weiboidextractor.core.extractor;

import com.mmerhav.weiboidextractor.core.reader.WeiboRawData;
//import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


//@Component
@Slf4j
public class WeiboJSONDataExtractor implements WeiboDataExtractor {

    @Value("${scraping.interval}")
    private int scarpingInterval;

    @Override
    public List<WeiboData> extract(List<WeiboRawData> weiboRawDataList) {
        List<WeiboData> weiboDataResultList = new ArrayList<>();

        String google = "http://www.google.com/search?q=";
        String charset = "UTF-8";
        String userAgent = "ExampleBot 1.0 (+http://example.com/bot)"; // Change this to your company's name and bot homepage!

        Elements links;

        for (WeiboRawData weiboRawData : weiboRawDataList) {
            String nickName = weiboRawData.getNickName();
            String search = "weibo uid= \\\"" + nickName + "\\\"";

            boolean cont = true;
            try {
                links = Jsoup.connect(google + URLEncoder.encode(search, charset)).userAgent(userAgent).get().select(".g>.r>a");
                for (int i = 0; i < links.size() && cont ; i++) {

                    Element link = links.get(i);

                    String title = link.text();
                    String url = link.absUrl("href");

                    url = URLDecoder.decode(url.substring(url.indexOf('=') + 1, url.indexOf('&')), "UTF-8");

                    if (!url.startsWith("http")) {
                        continue; // Ads/news/etc.
                    } else {
                        String sId = url.replaceAll("^\\D*(\\d+).*", "$1");
                        if(StringUtils.isNotEmpty(sId) && StringUtils.isNumeric(sId)) {
                            long uid = Long.valueOf(sId);
                            WeiboData weiboData = new WeiboData();
                            weiboData.setUid(uid);
                            weiboDataResultList.add(weiboData);
                            cont = false;
                        } else {
                            log.error("No id was found in url:{}", url);
                        }
                    }

                    log.info("Title: " + title);
                    log.info("URL: " + url);
                }
            } catch(HttpStatusException e) {
                e.printStackTrace();
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            sleep(scarpingInterval);
        }
        return weiboDataResultList;
    }

    private void sleep(int milliSeconds) {
        try {
            Thread.sleep(milliSeconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

