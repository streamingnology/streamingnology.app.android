package com.streamingnology.snymediaplayer.fragment.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.streamingnology.snymediaplayer.R;
/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class FileContent {

  public List<FileItem> ITEMS = new ArrayList<FileItem>();

  public void addFile(FileItem item) {
    ITEMS.add(item);
  }

  public void clear() {
    ITEMS.clear();
  }

  public static class FileItem {
    public final int     image_id;
    public final String  name;
    public final String  path;
    public final boolean isFolder;

    public FileItem(String file_name, String file_path, boolean isFolder) {
      this.name = file_name;
      this.path = file_path;
      this.isFolder = isFolder;
      if (isFolder) {
        image_id = R.drawable.folder_mac
        ;
      } else {
        image_id = R.drawable.play;
      }
    }

    @Override
    public String toString() {
      return "";
    }
  }
}
