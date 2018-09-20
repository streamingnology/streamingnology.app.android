package com.streamingnology.snymediaplayer.fragment.mediaserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class MediaServerContent {

  /**
   * An array of sample (mediaserver) items.
   */
  public List<MediaServerItem> ITEMS = new ArrayList<MediaServerItem>();

  public void addItem(MediaServerItem item) {
    ITEMS.add(item);
  }

  /**
   * A mediaserver item representing a piece of content.
   */
  public static class MediaServerItem {
    public final int image_id;
    public final String addr;

    public MediaServerItem(int image_id, String addr) {
      this.image_id = image_id;
      this.addr = addr;
    }

    @Override
    public String toString() {
      return addr;
    }
  }
}
