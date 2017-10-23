package com.huinong.framework.initializr.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.ZipFileSet;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huinong.framework.initializr.domain.CompileDependency;
import com.huinong.framework.initializr.domain.ProjectRequest;
import com.huinong.framework.initializr.generate.ProjectGenerator;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by Likai on 2017/10/18 0018.
 */

@Controller
@Slf4j
public class InitializrController {
  @Autowired
  private ProjectGenerator projectGenerator;

  @RequestMapping(value = "/index")
  public void index(ModelMap modelMap) throws IOException {
    ObjectMapper om = new ObjectMapper();
    List<CompileDependency> dependencies =
        om.readValue(ResourceUtils.getURL("classpath:static/json/dependencies.json"),
            new TypeReference<List<CompileDependency>>() {});
    modelMap.put("dependencies", dependencies);
    modelMap.put("version", "0.5.0-SNAPSHOT");
  }

  @RequestMapping(value = "/starter.zip")
  @ResponseBody
  public ResponseEntity<byte[]> springZip(/* @Validated */ ProjectRequest projectRequest)
      throws IOException {
    if (CollectionUtils.isEmpty(projectRequest.getCompileDependencies())) {
      List<CompileDependency> compileDependencies = Lists.newArrayList();
      compileDependencies.add(new CompileDependency().setGroupId("com.huinong.truffle")
          .setArtifactId("hn-framework-starter-web"));
      projectRequest.setCompileDependencies(compileDependencies);
    }
    File dir = projectGenerator.generateProjectStructure(projectRequest);

    File download = projectGenerator.createDistributionFile(dir, ".zip");

    Zip zip = new Zip();
    zip.setProject(new Project());
    zip.setDefaultexcludes(false);
    ZipFileSet set = new ZipFileSet();
    set.setDir(dir);
    set.setIncludes("**,");
    set.setDefaultexcludes(false);
    zip.addFileset(set);
    zip.setDestFile(download.getCanonicalFile());
    zip.execute();
    return upload(download, dir, projectRequest.getArtifactId() + ".zip");
  }

  private ResponseEntity<byte[]> upload(File download, File dir, String fileName)
      throws IOException {
    ResponseEntity<byte[]> result;
    try (FileInputStream fileInputStream = new FileInputStream(download)) {
      byte[] bytes = StreamUtils.copyToByteArray(fileInputStream);
      log.info("Uploading: {} ({} bytes)", download, bytes.length);
      result = createResponseEntity(bytes, fileName);
    }
    projectGenerator.cleanTempFiles(dir);
    return result;
  }

  private ResponseEntity<byte[]> createResponseEntity(byte[] content, String fileName) {
    String contentDispositionValue = "attachment; filename=\"" + fileName + "\"";
    return ResponseEntity.ok().header("Content-Type", "application/zip")
        .header("Content-Disposition", contentDispositionValue).body(content);
  }
}
