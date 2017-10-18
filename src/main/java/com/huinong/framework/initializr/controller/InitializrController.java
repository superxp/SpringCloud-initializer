package com.huinong.framework.initializr.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by Likai on 2017/10/18 0018.
 */

@Controller
@Slf4j
public class InitializrController {

  @RequestMapping (value = "index")
  public String index(){
    return "index";
  }
}
