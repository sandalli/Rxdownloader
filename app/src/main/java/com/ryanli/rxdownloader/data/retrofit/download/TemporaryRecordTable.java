package com.ryanli.rxdownloader.data.retrofit.download;

import com.ryanli.rxdownloader.data.retrofit.httpapis.DownloadApi;
import com.ryanli.rxdownloader.data.utils.LogUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Response;

import static com.ryanli.rxdownloader.data.retrofit.download.DownloadUtils.contentLength;
import static com.ryanli.rxdownloader.data.retrofit.download.DownloadUtils.empty;
import static com.ryanli.rxdownloader.data.retrofit.download.DownloadUtils.fileName;
import static com.ryanli.rxdownloader.data.retrofit.download.DownloadUtils.lastModify;
import static com.ryanli.rxdownloader.data.retrofit.download.DownloadUtils.notSupportRange;

/**
 * Auther: RyanLi
 * Data: 2018-06-18 23:47
 * Description:
 */
public class TemporaryRecordTable {
    private Map<String, TemporaryRecord> map;

    public TemporaryRecordTable() {
        this.map = new HashMap<>();
    }

    public void add(String url, TemporaryRecord record) {
        map.put(url, record);
    }

    public boolean contain(String url) {
        return map.get(url) != null;
    }

    public void delete(String url) {
        map.remove(url);
    }

    /**
     * Save file info
     *
     * @param url      key
     * @param response response
     */
    public void saveFileInfo(String url, Response<?> response) {
        TemporaryRecord record = map.get(url);
        if (empty(record.getSaveName())) {
            record.setSaveName(fileName(url, response));
        }
        record.setContentLength(contentLength(response));
        record.setLastModify(lastModify(response));
    }

    /**
     * Save range info
     *
     * @param url      key
     * @param response response
     */
    public void saveRangeInfo(String url, Response<?> response) {
        map.get(url).setRangeSupport(!notSupportRange(response));
    }

    /**
     * Init necessary info
     *
     * @param url             url
     * @param maxThreads      max threads
     * @param maxRetryCount   retry count
     * @param defaultSavePath default save path
     * @param downloadApi     api
     */
    public void init(String url, int maxThreads, int maxRetryCount, String defaultSavePath,
                     DownloadApi downloadApi) {
        map.get(url).init(maxThreads, maxRetryCount, defaultSavePath, downloadApi);
    }

    /**
     * Save file state, change or not change.
     *
     * @param url      key
     * @param response response
     */
    public void saveFileState(String url, Response<Void> response) {
        if (response.code() == 304) {
            map.get(url).setFileChanged(false);
        } else if (response.code() == 200) {
            map.get(url).setFileChanged(true);
        }
    }

    /**
     * return file not exists download type.
     *
     * @param url key
     * @return download type
     */
    public DownloadType generateNonExistsType(String url) {
        return getNormalType(url);
    }

    /**
     * return file exists download type
     *
     * @param url key
     * @return download type
     */
    public DownloadType generateFileExistsType(String url) {
        DownloadType type;
        if (fileChanged(url)) {
            type = getNormalType(url);
        } else {
            type = getServerFileChangeType(url);
        }
        return type;
    }

    /**
     * read last modify string
     *
     * @param url key
     * @return last modify
     */
    public String readLastModify(String url) {
        try {
            return map.get(url).readLastModify();
        } catch (IOException e) {
            //TODO log
            //If read failed,return an empty string.
            //If we send empty last-modify,server will response 200.
            //That means file changed.
            return "";
        }
    }

    public boolean fileExists(String url) {
        return map.get(url).file().exists();
    }

    public File[] getFiles(String url) {
        return map.get(url).getFiles();
    }

    private boolean supportRange(String url) {
        return map.get(url).isSupportRange();
    }

    private boolean fileChanged(String url) {
        return map.get(url).isFileChanged();
    }

    private DownloadType getNormalType(String url) {
        DownloadType type;
        if (supportRange(url)) {
            type = new DownloadType.MultiThreadDownload(map.get(url));
        } else {
            type = new DownloadType.NormalDownload(map.get(url));
        }
        return type;
    }

    private DownloadType getServerFileChangeType(String url) {
        if (supportRange(url)) {
            return supportRangeType(url);
        } else {
            return notSupportRangeType(url);
        }
    }

    private DownloadType supportRangeType(String url) {
        if (needReDownload(url)) {
            return new DownloadType.MultiThreadDownload(map.get(url));
        }
        try {
            if (multiDownloadNotComplete(url)) {
                return new DownloadType.ContinueDownload(map.get(url));
            }
        } catch (IOException e) {
            return new DownloadType.MultiThreadDownload(map.get(url));
        }
        return new DownloadType.FinishedDownload(map.get(url));
    }

    private DownloadType notSupportRangeType(String url) {
        if (normalDownloadNotComplete(url)) {
            return new DownloadType.NormalDownload(map.get(url));
        } else {
            return new DownloadType.FinishedDownload(map.get(url));
        }
    }

    private boolean multiDownloadNotComplete(String url) throws IOException {
        return map.get(url).fileNotComplete();
    }

    private boolean normalDownloadNotComplete(String url) {
        return !map.get(url).fileComplete();
    }

    private boolean needReDownload(String url) {
        return tempFileNotExists(url) || tempFileDamaged(url);
    }

    private boolean tempFileDamaged(String url) {
        try {
            return map.get(url).tempFileDamaged();
        } catch (IOException e) {
            LogUtils.i("Record file may be damaged, so we will re-download");
            return true;
        }
    }

    private boolean tempFileNotExists(String url) {
        return !map.get(url).tempFile().exists();
    }
}
