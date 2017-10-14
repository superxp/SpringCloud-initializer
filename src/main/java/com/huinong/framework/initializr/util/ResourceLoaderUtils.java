package com.huinong.framework.initializr.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;

import org.springframework.util.ResourceUtils;
import org.springframework.util.StreamUtils;

/**
 * Created by Likai on 2017/10/13 0013.
 */
public class ResourceLoaderUtils {
  private static final Charset UTF_8 = Charset.forName("UTF-8");

  public static byte[] getBinaryResource(String location) {
    try (InputStream stream = getInputStream(location)) {
      return StreamUtils.copyToByteArray(stream);
    }
    catch (IOException ex) {
      throw new IllegalStateException("Cannot get resource", ex);
    }
  }

  public static String getTextResource(String location) {
    try (InputStream stream = getInputStream(location)) {
      return StreamUtils.copyToString(stream, UTF_8);
    }
    catch (IOException ex) {
      throw new IllegalStateException("Cannot get resource", ex);
    }
  }

  private static InputStream getInputStream(String location) throws IOException {
    URL url = ResourceUtils.getURL(location);
    return url.openStream();
  }
}
