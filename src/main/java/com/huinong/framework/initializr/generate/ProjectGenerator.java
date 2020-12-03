package com.huinong.framework.initializr.generate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StreamUtils;

import com.google.common.collect.Maps;
import com.huinong.framework.initializr.domain.CompileDependency;
import com.huinong.framework.initializr.domain.ProjectRequest;
import com.huinong.framework.initializr.util.ResourceLoaderUtils;
import com.huinong.framework.initializr.util.TemplateRenderer;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by Xiaopang on 2017/10/12 0012.
 */
@Slf4j
@Component
public class ProjectGenerator {
  private static final String XP_FRAMEWORK_STARTER_MYBATIS = "otter-framework-mybatis";
  private static final String XP_FRAMEWORK_STARTER_REDIS = "otter-framework-starter-redis";
  private static final String XP_FRAMEWORK_STARTER_KAFKA = "otter-framework-kafka";
  private static final String XP_FRAMEWORK_STARTER_ZOOKEEPER = "otter-framework-starter-zookeeper";
  @Autowired
  private TemplateRenderer templateRenderer;

  private File temporaryDirectory;

  @Value("${TMPDIR:.}/initializr")
  private String tmpdir;

  public File generateProjectStructure(@Valid ProjectRequest request) {
    File rootDir = createRootDir();
    File dir = new File(rootDir, request.getArtifactId());
    dir.mkdir();
    Map model = resolveModel(request);
    // 生成pom文件
    String pom = doGenerateMavenPom("parent-pom.xml", model);
    writeText(new File(dir, "pom.xml"), pom);
    // 生成jenkens配置文件
    String hnci = doGenerateMavenPom("xpci", model);
    writeText(new File(dir, ".xpci"), hnci);
    // writeMavenWrapper(dir);
    // 生成gitignore文件
    generateGitIgnore(dir);
    //生成ediotrconfig文件
    generateEditorconfig(dir);
    //生成gitlab文件
    generateGitlabCi(dir);
    // 生成service子工程
    generateServiceProjectStructure(dir, request, model);
    // 生成api子工程
    generateApiProjectStructure(dir, request, model);
    return rootDir;
  }

  public File createDistributionFile(File dir, String extension) {
    File download = new File(dir, dir.getName() + extension);
    return download;
  }

  /**
   * 生成service子模块
   * 
   * @param dir
   * @param request
   * @param model
   */
  private void generateServiceProjectStructure(File dir, ProjectRequest request, Map model) {
    String language = request.getLanguage();
    File serviceDir = new File(dir, request.getArtifactId().concat("-service"));
    serviceDir.mkdir();
    String pom = doGenerateMavenPom("service-pom.xml", model);
    writeText(new File(serviceDir, "pom.xml"), pom);

    List<CompileDependency> compileDependencies = request.getCompileDependencies();
    Optional.ofNullable(compileDependencies).ifPresent(dependencies -> dependencies.forEach(compileDependency -> {
      if(XP_FRAMEWORK_STARTER_MYBATIS.equals(compileDependency.getArtifactId())){
        model.put("useMybatis", true);
      }
      if(XP_FRAMEWORK_STARTER_REDIS.equals(compileDependency.getArtifactId())){
        model.put("useRedis", true);
      }
      if(XP_FRAMEWORK_STARTER_KAFKA.equals(compileDependency.getArtifactId())){
        model.put("useKafka", true);
      }
      if(XP_FRAMEWORK_STARTER_MYBATIS.equals(compileDependency.getArtifactId())
              || XP_FRAMEWORK_STARTER_REDIS.equals(compileDependency.getArtifactId())){
        model.put("customTag", true);
      }
          if (XP_FRAMEWORK_STARTER_ZOOKEEPER.equals(compileDependency.getArtifactId())) {
            model.put("useZooKeeper", true);
          }
    }));
    File src = new File(new File(serviceDir, "src/main/" + language),
        request.getPackageName().replace(".", "/"));
    src.mkdirs();
    File controllerPackage = new File(src, "web/controller");
    controllerPackage.mkdirs();
    String extension = ("kotlin".equals(language) ? "kt" : language);
    write(new File(src, request.getBootstrapApplicationName() + "." + extension),
        "Application." + extension, model);

    File test = new File(new File(serviceDir, "src/test/" + language),
        request.getPackageName().replace(".", "/"));
    test.mkdirs();
    write(new File(test, request.getBootstrapApplicationName() + "Tests." + extension),
        "ApplicationTests." + extension, model);

    File resources = new File(serviceDir, "src/main/resources");
    resources.mkdirs();
    write(new File(resources, "application.yml"), "application.yml", model);
    write(new File(resources, "application-local.yml"), "application-local.yml", model);
    //生成Mybatis映射目录及包路径
    if(Objects.equals(model.get("useMybatis"), true)) {
      File mapper = new File(serviceDir, "src/main/resources/mapper");
      mapper.mkdirs();
      write(new File(resources, "mybatis-config.xml"), "mybatis-config.xml", null);
      File readDir = new File(serviceDir, "src/main/resources/mapper/read");
      readDir.mkdirs();
      File writeDir = new File(serviceDir, "src/main/resources/mapper/write");
      writeDir.mkdirs();
      File readPackage = new File(src, "dao/read");
      readPackage.mkdirs();
      File writePackage = new File(src, "dao/write");
      writePackage.mkdirs();
      File entityPackage = new File(src, "entity");
      entityPackage.mkdirs();
    }
  }

  /**
   * 生成api子模块
   * 
   * @param dir
   * @param request
   */
  private void generateApiProjectStructure(File dir, ProjectRequest request, Map model) {
    File apiDir = new File(dir, request.getArtifactId().concat("-api"));
    apiDir.mkdir();
    String pom = doGenerateMavenPom("api-pom.xml", model);
    writeText(new File(apiDir, "pom.xml"), pom);

  }

  private String doGenerateMavenPom(String templateName, Map model) {
    return templateRenderer.process(templateName, model);
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

  protected Map resolveModel(ProjectRequest request) {
    return Maps.newHashMap(BeanMap.create(request));
  }

  protected void generateGitIgnore(File dir) {
    String body = templateRenderer.process("gitignore.tmpl", null);
    writeText(new File(dir, ".gitignore"), body);
  }

  protected void generateEditorconfig(File dir) {
        String body = templateRenderer.process("editorconfig.tmpl", null);
        writeText(new File(dir, ".editorconfig"), body);
  }

  protected void generateGitlabCi(File dir){
      String body = templateRenderer.process("gitlab-ci.tmpl", null);
      writeText(new File(dir, ".gitlab-ci.yml"), body);
  }

  public void write(File target, String templateName, Map model) {
    String body = templateRenderer.process(templateName, model);
    writeText(target, body);
  }

  private File createRootDir() {
    try {
      File rootDir = File.createTempFile("tmp", "", getTemporaryDirectory());
      rootDir.delete();
      rootDir.mkdirs();
      return rootDir;
    } catch (IOException e) {
      throw new IllegalStateException("Cannot create temp dir", e);
    }
  }

  private File getTemporaryDirectory() {
    if (temporaryDirectory == null) {
      temporaryDirectory = new File(tmpdir);
      temporaryDirectory.mkdirs();
    }
    return temporaryDirectory;
  }

  public void cleanTempFiles(File dir) {
    FileSystemUtils.deleteRecursively(dir);
  }
}
