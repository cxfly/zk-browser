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

    public BeetlTemplateEngine setDirectoryForTemplateLoading(String file) {
        this.resourceLoader = new FileResourceLoader(file);
        return this;
    }

    @Override
    public String render(ModelAndView modelAndView) {
        try {
            Configuration cfg = Configuration.defaultConfiguration();
            cfg.setPlaceholderStart("<%=");
            cfg.setPlaceholderEnd("%>");
            GroupTemplate gt = new GroupTemplate(resourceLoader, cfg);

            Template t = gt.getTemplate(modelAndView.getViewName());
            t.binding((Map) modelAndView.getModel());

            return t.render();
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

}
