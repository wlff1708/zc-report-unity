package com.report.module.im.pojo.bo;

/**
 * 上报文件消息BO
 *
 * @param s2DealBO  给级联上报用
 * @param js2DealBO 给指调上报用
 */
public record ImFileDealForReportBO(ImFileDealBO s2DealBO, ImFileDealBO js2DealBO) {
}
