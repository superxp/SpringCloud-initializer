package com.huinong.framework.initializr.domain;

import lombok.Builder;
import lombok.Data;

/**
 * Created by Likai on 2017/10/13 0013.
 */
@Data
@Builder
public class CompileDependency {
  private String groupId;
  private String artifactId;
  private String version;
  private String type;
}
