package com.report.module.im.util.shell;

import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import com.report.common.exception.ExecuteShellException;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Linux 专用 Shell 执行工具类 (基于 Hutool RuntimeUtil)
 * 使用方式：
 * 单命令
 * ShellUtil.execSimple("systemctl restart redis", null);
 *
 * 多命令（&& 连接）
 * ShellUtil.execComplex("systemctl stop redis && systemctl start redis", null);
 *
 * 指定工作目录
 * ShellUtil.execSimple("ls -la", "/home");
 */
public class ImShellUtil {

    // 超时时间：60 秒
    private static final long DEFAULT_TIMEOUT_SECONDS = 60;

    /**
     * 执行简单 Shell 命令
     * <p>
     * 适用于单个命令或不含特殊符号的命令
     * </p>
     *
     * <h3>使用示例：</h3>
     * <pre>{@code
     * // 1. 基本命令
     * ShellUtil.execSimple("systemctl restart redis", null);
     *
     * // 2. 指定工作目录
     * ShellUtil.execSimple("ls -la", "/home");
     *
     * // 3. 获取命令输出
     * String output = ShellUtil.execSimple("pwd", null);
     * }</pre>
     *
     * @param command 命令，例如："systemctl restart redis"
     * @param workDir 工作目录，null 表示当前目录
     * @return 命令执行输出结果
     * @throws ExecuteShellException 命令执行失败时抛出
     */
    public static String execSimple(String command, String workDir) {
        return execute(command, workDir, false);
    }

    /**
     * 执行复杂 Shell 命令
     * <p>
     * 适用于包含 &&、||、管道符等特殊符号的复合命令
     * </p>
     *
     * <h3>使用示例：</h3>
     * <pre>{@code
     * // 1. 多命令顺序执行
     * ShellUtil.execComplex("systemctl stop redis && systemctl start redis", null);
     *
     * // 2. 条件执行
     * ShellUtil.execComplex("test -f /tmp/a.txt || echo '文件不存在'", null);
     *
     * // 3. 管道命令
     * ShellUtil.execComplex("ps aux | grep java | wc -l", null);
     *
     * // 4. 指定工作目录
     * ShellUtil.execComplex("git pull && npm install", "/home/project");
     * }</pre>
     *
     * @param command 复合命令，例如："cmd1 && cmd2"
     * @param workDir 工作目录，null 表示当前目录
     * @return 命令执行输出结果
     * @throws ExecuteShellException 命令执行失败时抛出
     */
    public static String execComplex(String command, String workDir) {
        return execute(command, workDir, true);
    }

    private static String execute(String command, String workDir, boolean isComplex) {
        if (StrUtil.isBlank(command)) {
            throw new ExecuteShellException("命令不能为空");
        }

        String finalWorkDir = normalizeWorkDir(workDir);

        // 构建 bash 命令：先 cd 到指定目录，再执行命令
        String bashCmd;
        if (isComplex) {
            // 复杂命令（含 && || 等）用括号包起来
            bashCmd = "cd '" + finalWorkDir + "' && (" + command + ")";
        } else {
            bashCmd = "cd '" + finalWorkDir + "' && " + command;
        }

        Process process = null;
        try {
            // 使用 /bin/bash -c 执行，支持 &&、|| 等 Shell 语法
            process = RuntimeUtil.exec("/bin/bash", "-c", bashCmd);

            // 等待命令执行完成，带超时
            if (!process.waitFor(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroy();
                throw new ExecuteShellException("命令执行超时，已超过 " + DEFAULT_TIMEOUT_SECONDS + " 秒");
            }

            // 获取命令执行结果
            String result = RuntimeUtil.getResult(process);

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                throw new ExecuteShellException("命令执行失败，退出码：" + exitCode + "，输出：" + result);
            }

            return result;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExecuteShellException("命令执行被中断", e);
        } catch (Exception e) {
            if (e instanceof ExecuteShellException) {
                throw e;
            }
            throw new ExecuteShellException("Shell 执行失败，工作目录：" + finalWorkDir + "，原因：" + e.getMessage(), e);
        } finally {
            if (process != null) {
                RuntimeUtil.destroy(process);
            }
        }
    }

    private static String normalizeWorkDir(String workDir) {
        if (StrUtil.isBlank(workDir)) {
            return System.getProperty("user.dir");
        }
        File dir = new File(workDir);
        if (!dir.exists()) {
            throw new ExecuteShellException("工作目录不存在：" + workDir);
        }
        if (!dir.isDirectory()) {
            throw new ExecuteShellException("路径不是目录：" + workDir);
        }
        return dir.getAbsolutePath();
    }
}
