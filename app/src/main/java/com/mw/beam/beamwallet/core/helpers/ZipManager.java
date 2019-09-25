package com.mw.beam.beamwallet.core.helpers;

import java.util.ArrayList;
import java.util.List;
import java.io.FileOutputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;

public class ZipManager {

    private static final int BUFFER = 80000;

    public static void zip(String folder, String zipFileName) {
        File zipFile = new File(zipFileName);

        if(zipFile.exists()) {
            zipFile.delete();
        }

        File directory = new File(folder);

        List<String> _files = getListFiles(directory,new ArrayList<String>());


        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(zipFileName);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            byte data[] = new byte[BUFFER];

            for (int i = 0; i < _files.size(); i++) {
                FileInputStream fi = new FileInputStream(_files.get(i));
                origin = new BufferedInputStream(fi, BUFFER);

                ZipEntry entry = new ZipEntry(_files.get(i).substring(_files.get(i).lastIndexOf(File.separator) + 1));
                out.putNextEntry(entry);
                int count;

                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static List<String> getListFiles(File file, List<String> files) {
        File[] dirs = file.listFiles();
        String name = "";
        if (dirs != null) {
            for (File dir : dirs) {
                if (dir.isFile()) {
                    name = dir.getName().toLowerCase();
                    files.add(dir.getAbsolutePath());
                } else files = getListFiles(dir, files);
            }
        }
        return files;
    }

}
