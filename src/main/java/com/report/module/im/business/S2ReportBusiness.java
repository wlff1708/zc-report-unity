package com.report.module.im.business;

import com.report.common.util.cache.CacheDef;
import com.report.common.util.cache.Caches;
import com.report.module.im.exchange.IMReportExchange;
import com.report.module.im.pojo.vo.S2ReportResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.List;

/**
 * 级联上报业务类
 */
@Slf4j
@Component
public class S2ReportBusiness {

    /** 上级MC地址缓存定义 */
    private static final CacheDef<String> IM_S2_BASE_URL = CacheDef.local("im:s2:base:url");

    private final IMReportExchange exchange;

    public S2ReportBusiness(HttpServiceProxyFactory httpServiceProxyFactory) {
        this.exchange = httpServiceProxyFactory.createClient(IMReportExchange.class);
    }

    /**
     * 上报告警文件（级联转发）
     *
     * @param userAgent          设备标识
     * @param cookie             SESSION cookie
     * @param category           告警分类
     * @param module             告警模块
     * @param alarmFileDescList  告警文件描述 JSON
     * @param alarmFileCountJsonObj 告警文件数量统计 JSON
     * @param files              告警文件列表
     * @return 响应结果
     */
    public S2ReportResultVo forwardAlarmFile(String userAgent, String cookie,
                                              String category, String module, String alarmFileDescList,
                                              String alarmFileCountJsonObj, List<MultipartFile> files) {
        String baseUrl = Caches.get(IM_S2_BASE_URL);
        log.info("级联上报告警文件, baseUrl={}", baseUrl);
        return exchange.alarmFileForward(baseUrl, userAgent, cookie, category, module,
                alarmFileDescList, alarmFileCountJsonObj, files).getBody();
    }

    /**
     * 上报告警消息（级联转发）
     *
     * @param userAgent          设备标识
     * @param cookie             SESSION cookie
     * @param category           告警分类
     * @param alarmCountJsonObj  告警消息数量统计 JSON
     * @param files              告警消息文件列表
     * @return 响应结果
     */
    public S2ReportResultVo forwardAlarm(String userAgent, String cookie,
                                         String category, String alarmCountJsonObj, List<MultipartFile> files) {
        String baseUrl = Caches.get(IM_S2_BASE_URL);
        log.info("级联上报告警消息, baseUrl={}", baseUrl);
        return exchange.alarmForward(baseUrl, userAgent, cookie, category, alarmCountJsonObj, files).getBody();
    }
}