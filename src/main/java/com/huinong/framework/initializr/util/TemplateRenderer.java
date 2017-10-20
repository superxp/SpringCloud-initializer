package com.huinong.framework.initializr.util;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.ConcurrentReferenceHashMap;

import com.google.common.collect.Maps;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Mustache.Compiler;
import com.samskivert.mustache.Mustache.TemplateLoader;
import com.samskivert.mustache.Template;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Dave Syer
 */
@Data
@Slf4j
@Component
public class TemplateRenderer implements ResourceLoaderAware {
  private final ConcurrentMap<String, Template> templateCaches = new ConcurrentReferenceHashMap<>();

  private boolean cache = true;

  private ResourceLoader resourceLoader;

  public String process(String name, Map model) {
    try {
      Template template = getTemplate(name);
      if(model == null){
        model = Maps.newHashMap();
      }
      return template.execute(model);
    } catch (Exception e) {
      log.error("Cannot render: " + name, e);
      throw new IllegalStateException("Cannot render template", e);
    }
  }

  public Template getTemplate(String name) {
    if (cache) {
      return this.templateCaches.computeIfAbsent(name, this::loadTemplate);
    }
    return loadTemplate(name);
  }

  private Template loadTemplate(String name) {
    try {
      Compiler mustache = mustacheCompiler();
      Reader reader = mustache.loader.getTemplate(name);
      return mustache.compile(reader);
    } catch (Exception e) {
      throw new IllegalStateException("Cannot load template " + name, e);
    }
  }

  private Compiler mustacheCompiler() {
    return Mustache.compiler().defaultValue("").withLoader(mustacheTemplateLoader());
  }

  private TemplateLoader mustacheTemplateLoader() {
    String prefix = "classpath:/mustache_templates/";
    Charset charset = Charset.forName("UTF-8");
    return name -> new InputStreamReader(resourceLoader.getResource(prefix + name).getInputStream(),
        charset);
  }

  @Override
  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }
}
