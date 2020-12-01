package com.huinong.framework.initializr.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Created by Xiaopang on 2017/10/13 0013.
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class CompileDependency {
  private String groupId;
  private String artifactId;
  private String version;
  private String type;
  private String name;
}
