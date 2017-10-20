package com.huinong.framework.initializr;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.huinong.framework.initializr.domain.CompileDependency;
import com.huinong.framework.initializr.domain.ProjectRequest;
import com.huinong.framework.initializr.generate.ProjectGenerator;

/**
 * Created by Likai on 2017/10/12 0012.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class InitializrApplicationTest {
  @Autowired
  private ProjectGenerator projectGenerator;

  @Value("${TMPDIR:.}/initializr")
  private String tmpdir;

  @Test
  public void testPom(){
    List<CompileDependency> compileDependencies = Lists.newArrayList();
    compileDependencies.add(new CompileDependency().setGroupId("org.springframework.boot")
        .setArtifactId("spring-boot-configuration-processor"));
    compileDependencies.add(new CompileDependency().setGroupId("com.huinong.truffle")
        .setArtifactId("hn-framework-starter-web"));
    compileDependencies.add(new CompileDependency().setGroupId("com.huinong.truffle")
        .setArtifactId("hn-framework-starter-mybatis"));
    compileDependencies.add(new CompileDependency().setGroupId("com.huinong.truffle")
        .setArtifactId("hn-framework-starter-redis"));
    ProjectRequest projectRequest = new ProjectRequest().setGroupId("com.initializr")
        .setArtifactId("test").setVersion("0.0.1-SNAPSHOT")
        .setCompileDependencies(compileDependencies).setVersionToken("0.4.0-SNAPSHOT")
        .setMavenPluginVersion("1.5.6.RELEASE").setPackageName("com.example.demo")
        .setBootstrapApplicationName("DemoApplication").setLanguage("java");
    File dir = projectGenerator.generateProjectStructure(projectRequest);
    System.out.println(dir.getName());
    projectGenerator.cleanTempFiles(dir);
  }

  @Test
  public void testSystemProperties() throws IOException {
    File temporaryDirectory = new File(tmpdir);
    temporaryDirectory.delete();
    temporaryDirectory.mkdirs();
    File tmpdir = File.createTempFile("tmp", "", temporaryDirectory);
    System.out.println(tmpdir.getName());
  }
}
