package com.huinong.framework.initializr.generate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

import com.huinong.framework.initializr.domain.ProjectRequest;
import com.huinong.framework.initializr.util.ResourceLoaderUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.huinong.framework.initializr.util.TemplateRenderer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;

/**
 * Created by Likai on 2017/10/12 0012.
 */
@Slf4j
public class ProjectGenerator {
  @Autowired
  private TemplateRenderer templateRenderer;

  public byte[] generateMavenPom(Object request) {
    try {
      Map<String, Object> model = resolveModel(request);
      byte[] content = doGenerateMavenPom(model).getBytes();
      return content;
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
    }
    return null;
  }

  public File generateProjectStructure(ProjectRequest request, Map<String, Object> model) {
    File dir = new File("temp");
    String pom = doGenerateMavenPom(model);
    writeText(new File(dir, "pom.xml"), pom);
    writeMavenWrapper(dir);
    generateGitIgnore(dir);

    String language = request.getLanguage();
    File src = new File(new File(dir, "src/main/" + language),
            request.getPackageName().replace(".", "/"));
    src.mkdirs();
    String extension = ("kotlin".equals(language) ? "kt" : language);
    write(new File(src, request.getApplicationName() + "." + extension),
            "Application." + extension, model);

    File test = new File(new File(dir, "src/test/" + language),
            request.getPackageName().replace(".", "/"));
    test.mkdirs();
    write(new File(test, request.getApplicationName() + "Tests." + extension),
            "ApplicationTests." + extension, model);

    File resources = new File(dir, "src/main/resources");
    resources.mkdirs();
    writeText(new File(resources, "application.yml"), "");
    return null;
  }

  private String doGenerateMavenPom(Map<String, Object> model) {
    return templateRenderer.process("starter-pom.xml", model);
  }

  private void writeMavenWrapper(File dir) {
    writeTextResource(dir, "mvnw.cmd", "maven/mvnw.cmd");
    writeTextResource(dir, "mvnw", "maven/mvnw");

    File wrapperDir = new File(dir, ".mvn/wrapper");
    wrapperDir.mkdirs();
    writeTextResource(wrapperDir, "maven-wrapper.properties",
        "maven/wrapper/maven-wrapper.properties");
    writeBinaryResource(wrapperDir, "maven-wrapper.jar", "maven/wrapper/maven-wrapper.jar");
  }

  private File writeBinaryResource(File dir, String name, String location) {
    return doWriteProjectResource(dir, name, location, true);
  }

  private File writeTextResource(File dir, String name, String location) {
    return doWriteProjectResource(dir, name, location, false);
  }

  private File doWriteProjectResource(File dir, String name, String location, boolean binary) {
    File target = new File(dir, name);
    if (binary) {
      writeBinary(target, ResourceLoaderUtils.getBinaryResource("classpath:project/" + location));
    } else {
      writeText(target, ResourceLoaderUtils.getTextResource("classpath:project/" + location));
    }
    return target;
  }

  private void writeText(File target, String body) {
    try (OutputStream stream = new FileOutputStream(target)) {
      StreamUtils.copy(body, Charset.forName("UTF-8"), stream);
    } catch (Exception e) {
      throw new IllegalStateException("Cannot write file " + target, e);
    }
  }

  private void writeBinary(File target, byte[] body) {
    try (OutputStream stream = new FileOutputStream(target)) {
      StreamUtils.copy(body, stream);
    } catch (Exception e) {
      throw new IllegalStateException("Cannot write file " + target, e);
    }
  }

  protected Map<String, Object> resolveModel(Object originalRequest) {
    Map<String, Object> model = new LinkedHashMap<>();
    return model;
  }

  protected void generateGitIgnore(File dir) {
    String body = templateRenderer.process("gitignore.tmpl", null);
    writeText(new File(dir, ".gitignore"), body);
  }

  public void write(File target, String templateName, Map<String, Object> model) {
    String body = templateRenderer.process(templateName, model);
    writeText(target, body);
  }
}
