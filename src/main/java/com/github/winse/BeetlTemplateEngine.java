package com.github.winse;

import com.google.common.base.Throwables;
import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.ResourceLoader;
import org.beetl.core.Template;
import org.beetl.core.resource.ClasspathResourceLoader;
import org.beetl.core.resource.FileResourceLoader;
import spark.ModelAndView;
import spark.TemplateEngine;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class BeetlTemplateEngine extends TemplateEngine {

    private ResourceLoader resourceLoader = new ClasspathResourceLoader();
    private GroupTemplate gt;

    public BeetlTemplateEngine() {
        try {
            Configuration cfg = Configuration.defaultConfiguration();
            cfg.setPlaceholderStart("<%=");
            cfg.setPlaceholderEnd("%>");
            this.gt = new GroupTemplate(resourceLoader, cfg);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public BeetlTemplateEngine setDirectoryForTemplateLoading(String file) {
        this.resourceLoader = new FileResourceLoader(file);
        return this;
    }

    @Override
    public String render(ModelAndView modelAndView) {
        Template t = gt.getTemplate(modelAndView.getViewName());
        t.binding((Map) modelAndView.getModel());

        return t.render();
    }

}
