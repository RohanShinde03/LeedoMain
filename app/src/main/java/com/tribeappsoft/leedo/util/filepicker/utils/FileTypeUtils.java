package com.tribeappsoft.leedo.util.filepicker.utils;

import android.webkit.MimeTypeMap;

import com.tribeappsoft.leedo.R;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nickolay on 25.10.15.
 */

public class FileTypeUtils {
    public enum FileType {
        DIRECTORY(R.drawable.fp_ic_folder_48dp, R.string.type_directory),
        DOCUMENT(R.drawable.fp_ic_document_box, R.string.type_document),
        EXCEL(R.drawable.fp_ic_excel_box, R.string.type_excel, "xls", "xlk", "xlsb", "xlsm", "xlsx", "xlr", "xltm", "xlw", "numbers", "ods", "ots"),
        PDF(R.drawable.fp_ic_pdf_box, R.string.type_pdf, "pdf"),
        WORD(R.drawable.fp_ic_word_box, R.string.type_word, "doc", "docm", "docx", "dot", "mcw", "rtf", "pages", "odt", "ott");

        private int icon;
        private int description;
        private String[] extensions;

        FileType(int icon, int description, String... extensions) {
            this.icon = icon;
            this.description = description;
            this.extensions = extensions;
        }

        public String[] getExtensions() {
            return extensions;
        }

        public int getIcon() {
            return icon;
        }

        public int getDescription() {
            return description;
        }
    }

    private static Map<String, FileType> fileTypeExtensions = new HashMap<>();

    static {
        for (FileType fileType : FileType.values()) {
            for (String extension : fileType.getExtensions()) {
                fileTypeExtensions.put(extension, fileType);
            }
        }
    }

    public static FileType getFileType(File file) {
        if (file.isDirectory()) {
            return FileType.DIRECTORY;
        }

        FileType fileType = fileTypeExtensions.get(getExtension(file.getName()));
        if (fileType != null) {
            return fileType;
        }

        return FileType.DOCUMENT;
    }

    public static String getExtension(String fileName) {
        String encoded;
        try {
            encoded = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            encoded = fileName;
        }
        return MimeTypeMap.getFileExtensionFromUrl(encoded).toLowerCase();
    }
}
