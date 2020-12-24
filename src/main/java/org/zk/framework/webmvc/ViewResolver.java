package org.zk.framework.webmvc;

import lombok.NoArgsConstructor;

import java.io.File;
import java.util.Locale;

@NoArgsConstructor
public class ViewResolver {

    private File templateRoot;

    public ViewResolver(String templateRoot) {
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        this.templateRoot = new File(templateRootPath);
    }

    public View resolveViewName(String viewName, Locale locale) throws Exception {
        if (viewName == null || "".equals(viewName)) {
            return null;
        }

        String viewNameNew = viewName.endsWith(".html") ? viewName : (viewName + ".html");
        // 不管多少个'/'统一转成一个'/'
        File templateFile = new File((templateRoot.getPath() + "/" + viewNameNew).replaceAll("/+", "/"));
        return new View(templateFile);
    }
}