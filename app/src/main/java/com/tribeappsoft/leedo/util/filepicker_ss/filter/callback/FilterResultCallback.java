package com.tribeappsoft.leedo.util.filepicker_ss.filter.callback;

import com.tribeappsoft.leedo.util.filepicker_ss.filter.entity.BaseFile;
import com.tribeappsoft.leedo.util.filepicker_ss.filter.entity.Directory;

import java.util.List;

/**
 * Created by Vincent Woo
 * Date: 2016/10/11
 * Time: 11:39
 */

public interface FilterResultCallback<T extends BaseFile> {
    void onResult(List<Directory<T>> directories);
}
