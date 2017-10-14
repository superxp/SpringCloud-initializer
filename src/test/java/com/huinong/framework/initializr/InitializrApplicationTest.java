package com.huinong.framework.initializr;

import java.util.List;
import java.util.Map;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.test.context.junit4.SpringRunner;

import com.huinong.framework.initializr.domain.CompileDependency;
import com.huinong.framework.initializr.domain.ProjectRequest;
import com.huinong.framework.initializr.util.TemplateRenderer;

/**
 * Created by Likai on 2017/10/12 0012.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class InitializrApplicationTest {
  @Autowired
  private TemplateRenderer templateRenderer;

  @Test
  public void testPom(){
    List<CompileDependency> compileDependencies = Lists.newArrayList();
    compileDependencies.add(CompileDependency.builder().groupId("org.springframework.boot")
            .artifactId("spring-boot-configuration-processor").build());
    compileDependencies.add(CompileDependency.builder().groupId("com.huinong.truffle")
            .artifactId("hn-framework-starter-web").build());
    ProjectRequest projectRequest = ProjectRequest.builder().groupId("com.initializr").artifactId("test")
            .version("0.0.1-SNAPSHOT").childArtifactId("test-service").compileDependencies(compileDependencies).versionToken("0.4.0-SNAPSHOT").build();
    BeanMap beanMap = BeanMap.create(projectRequest);
    System.out.println(templateRenderer.process("starter-pom.xml", beanMap));
  }
}
