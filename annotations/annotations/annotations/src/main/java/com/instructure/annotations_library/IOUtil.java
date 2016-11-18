/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.annotations_library;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtil {

    private static final String LOG_TAG = "IOUtil";
    public static void closeStream(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                Log.i(LOG_TAG, "Failed to close InputStream", e);
            }
        }
    }

    public static void closeStream(OutputStream os) {
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                Log.i(LOG_TAG, "Failed to close OutputStream", e);
            }
        }
    }

    public static void copy(File in, OutputStream out) throws IOException {
        copy(new FileInputStream(in), out);
    }

    public static void copy(InputStream in, File out) throws IOException {
        copy(in, new FileOutputStream(out));
    }

    /**
     * Taken from Apache Commons IOUtils. Copies an inputstream to an outputstream
     */
    public static void copy(InputStream input, OutputStream output) throws IOException {
        copy(input, output, false);
    }

    public static File getFileFromInputStream(Context context, InputStream inputStream) {
        // Get the directory for the app's private pictures directory.
        File file = null;
        OutputStream outputStream = null;
        try {
            file = File.createTempFile(LOG_TAG, null, context.getCacheDir());
            outputStream = new FileOutputStream(file);
            IOUtil.copy(inputStream, outputStream);
            outputStream.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            Log.e(LOG_TAG, "IO Exception :" +e.getMessage());
        }finally{
            IOUtil.closeStream(inputStream);
            IOUtil.closeStream(outputStream);
        }
        return file;
    }

    public static void copy(InputStream input, OutputStream output, boolean shouldClose) throws IOException {
        try {
            byte[] buffer = new byte[1024 * 4];
            int n;
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
            }
            output.flush();
        }finally {
            if(shouldClose){
                closeStream(input);
                closeStream(output);
            }
        }
    }
}
