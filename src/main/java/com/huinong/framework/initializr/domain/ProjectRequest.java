package com.huinong.framework.initializr.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by Likai on 2017/10/13 0013.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequest {
  private String applicationName = "DemoApplication";
  private String packageName = "com.example.demo";
  private String language;
  private String groupId;
  private String artifactId;
  private String childArtifactId;
  private String version = "0.0.1-SNAPSHOT";
  private List<CompileDependency> compileDependencies;
  private String versionToken;
}
