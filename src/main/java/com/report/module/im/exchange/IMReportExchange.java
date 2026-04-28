package com.report.module.im.exchange;

import com.report.module.im.constants.ImCommonConstants.S2Report;
import com.report.module.im.pojo.vo.S2ReportResultVo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

/**
 * 级联上报 - 上级MC接口定义（对应 V1S2SubMcController 服务端）
 *
 * @HttpExchange url 用于动态指定上级MC地址
 */
@HttpExchange
public interface IMReportExchange {

    /**
     * 上报告警文件（级联转发）
     *
     * @param baseUrl            上级MC地址，如 http://192.168.1.100:8181
     * @param userAgent          设备标识
     * @param cookie             SESSION cookie
     * @param category           告警分类（路径变量）
     * @param module             告警模块
     * @param alarmFileDescList  告警文件描述 JSON
     * @param alarmFileCountJsonObj 告警文件数量统计 JSON（可选）
     * @param files              告警文件列表
     * @return 响应结果
     */
    @PostExchange(url = "{baseUrl}/S2/V1/forward/file/alarm/{category}")
    ResponseEntity<S2ReportResultVo> alarmFileForward(
            @PathVariable("baseUrl") String baseUrl,
            @RequestHeader(S2Report.HEADER_USER_AGENT) String userAgent,
            @RequestHeader(S2Report.HEADER_COOKIE_SESSION) String cookie,
            @PathVariable("category") String category,
            @RequestParam(S2Report.PARAM_MODULE) String module,
            @RequestHeader(S2Report.HEADER_CONTENT_FILEDESC) String alarmFileDescList,
            @RequestHeader(value = S2Report.HEADER_ALARM_FILE_COUNT, required = false) String alarmFileCountJsonObj,
            @RequestParam(value = S2Report.PARAM_FILE, required = false) List<MultipartFile> files);

    /**
     * 上报告警消息（级联转发）
     *
     * @param baseUrl            上级MC地址
     * @param userAgent          设备标识
     * @param cookie             SESSION cookie
     * @param category           告警分类（路径变量）
     * @param alarmCountJsonObj  告警消息数量统计 JSON（可选）
     * @param files              告警消息文件列表
     * @return 响应结果
     */
    @PostExchange(url = "{baseUrl}/S2/V1/forward/alarm/{category}")
    ResponseEntity<S2ReportResultVo> alarmForward(
            @PathVariable("baseUrl") String baseUrl,
            @RequestHeader(S2Report.HEADER_USER_AGENT) String userAgent,
            @RequestHeader(S2Report.HEADER_COOKIE_SESSION) String cookie,
            @PathVariable("category") String category,
            @RequestHeader(value = S2Report.HEADER_ALARM_COUNT, required = false) String alarmCountJsonObj,
            @RequestParam(value = S2Report.PARAM_FILE, required = false) List<MultipartFile> files);
}