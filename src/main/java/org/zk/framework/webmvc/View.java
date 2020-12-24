package org.zk.framework.webmvc;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NoArgsConstructor
@AllArgsConstructor
public class View {
    public final String DEFAULT_CONTENT_TYPE = "text/html;charset=utf-8";

    private final Pattern pattern = Pattern.compile("\\$\\{[^\\}]+\\}", Pattern.CASE_INSENSITIVE);

    private File viewFile;

    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> mergedModel = this.createMergedOutputModel(model, request, response);

        StringBuilder sb = new StringBuilder();

        RandomAccessFile ra = new RandomAccessFile(this.viewFile, "r");

        String line;
        while (null != (line = ra.readLine())) {
            line = new String(line.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()) {
                String paramName = matcher.group();
                paramName = paramName.replaceAll("\\$\\{|\\}", "");
                Object paramValue = mergedModel.get(paramName);
                if (null == paramValue) {
                    continue;
                }

                line = matcher.replaceFirst(makeStringForRegExp(paramValue.toString()));
                matcher = pattern.matcher(line);
            }
            sb.append(line);
        }

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(DEFAULT_CONTENT_TYPE);
        response.getWriter().write(sb.toString());
    }

    private String makeStringForRegExp(String string) {
        return string.replace("\\", "\\\\").replace("*", "\\*")
                .replace("+", "\\+").replace("|", "\\|")
                .replace("{", "\\{").replace("}", "\\}")
                .replace("(", "\\(").replace(")", "\\)")
                .replace("^", "\\^").replace("$", "\\$")
                .replace("[", "\\[").replace("]", "\\]")
                .replace("?", "\\?").replace(",", "\\,")
                .replace(".", "\\.").replace("&", "\\&");
    }

    /**
     * 合并参数到输出中
     */
    private Map<String, Object> createMergedOutputModel(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> mergedModel = new LinkedHashMap<>();
        if (model != null) {
            mergedModel.putAll(model);
        }
        return mergedModel;
    }
}
