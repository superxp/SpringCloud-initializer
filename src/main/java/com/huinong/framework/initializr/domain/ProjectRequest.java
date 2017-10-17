package com.huinong.framework.initializr.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by Likai on 2017/10/13 0013.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequest {
  private String bootstrapApplicationName = "DemoApplication";
  private String packageName = "com.example.demo";
  private String language = "java";
  @NotNull(message = "groupId 不能为空!")
  private String groupId;
  @NotNull(message = "artifactId 不能为空!")
  private String artifactId;
  private String version = "0.0.1-SNAPSHOT";
  private List<CompileDependency> compileDependencies;
  private String versionToken;
  private String mavenPluginVersion = "1.5.6.RELEASE";
}
