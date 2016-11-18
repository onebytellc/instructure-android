package com.instructure.pandautils.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.pandautils.R;
import com.instructure.pandautils.models.FileSubmitObject;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUploadUtils {
    public static final String FILE_SCHEME = "file";
    public static final String CONTENT_SCHEME = "content";
    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     * <p/>
     * http://stackoverflow.com/questions/20067508/get-real-path-from-uri-android-kitkat-new-storage-access-framework
     *
     * @param activity The activity context.
     * @param uri      The Uri to query.
     * @author paulburke
     */
    @SuppressLint("NewApi")
    public static String getPath(final Activity activity, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(activity, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {

                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(activity.getContentResolver(), contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {

                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(activity.getContentResolver(), contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if (CONTENT_SCHEME.equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            return getDataColumn(activity.getContentResolver(), uri, null, null);
        }
        // File
        else if (FILE_SCHEME.equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     * @param resolver ContentResolver to query our URI
     * @param uri      The Uri to query.
     * @return The value of the _data column, which is typically a file path.
     *         Empty string if column does not exist.
     */
    public static String getDataColumn(ContentResolver resolver, Uri uri){
        return getDataColumn(resolver, uri, null, null);
    }

    /**
     *
     * @param resolver
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return
     */
    public static String getDataColumn(ContentResolver resolver,  Uri uri, String selection,
                                       String[] selectionArgs) {
        Log.v(Const.PANDA_UTILS_FILE_UPLOAD_UTILS_LOG, "getDataColumn uri: " + uri + " selection: " + selection + " args: " + selectionArgs);
        String filePath = "";
        Cursor cursor = null;
        final String column = MediaStore.MediaColumns.DATA;
        final String[] projection = {
                column
        };

        try {
            cursor = resolver.query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                filePath = cursor.getString(column_index);
            }
        }catch (Exception e){
            // An exception will be raised if the _data column does not exist.
            // This is mostly likely caused by new fileProvider permissions in kitkat+, in those cases, we fall back to using openFileDescriptor
            // to get access to the shared file.
            Log.e(Const.PANDA_UTILS_FILE_UPLOAD_UTILS_LOG, "cursor " + e.toString());
            return "";

        } finally {
            if (cursor != null){
                cursor.close();
            }
        };

        if(filePath == null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(resolver, uri);

                // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
                Uri tempUri = getImageUri(resolver, bitmap);

                // CALL THIS METHOD TO GET THE ACTUAL PATH
                filePath = getRealPathFromURI(resolver, tempUri);
            } catch(Exception e) {
                Log.e(Const.PANDA_UTILS_FILE_UPLOAD_UTILS_LOG, "filePath==null:  " + e.toString());
            }
        }
        return filePath;
    }

    public static Uri getImageUri(ContentResolver resolver, Bitmap inImage) {
        String path = MediaStore.Images.Media.insertImage(resolver, inImage, "profilePic", null);
        return Uri.parse(path);
    }

    public static String getRealPathFromURI(ContentResolver resolver, Uri uri) {
        Cursor cursor = resolver.query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
        String index = cursor.getString(idx);
        cursor.close();
        return index;
    }

    /**
     * Traditionally we were able to check for the MediaColumns.Data type to get a file's absolute path. However with new
     * fileProvider and permissions added in KitKat, _data may not be passed in through a URI. In those cases, we use openFileDescriptor
     * in order to access another apps file
     * @param context
     * @param uri
     * @return
     */
    public static FileSubmitObject getFileSubmitObjectFromInputStream(Context context, Uri uri, String fileName, final String mimeType) {
        if (uri == null) return null;
        File file;
        String errorMessage = "";
        // copy file from uri into new temporary file and pass back that new file's path
        InputStream input = null;
        FileOutputStream output = null;
        try {
            ContentResolver cr = context.getContentResolver();

            input = cr.openInputStream(uri);
            // add extension to filename if needed
            int lastDot = fileName.lastIndexOf(".");
            if (lastDot == -1) {
                fileName = fileName +"." +getFileExtensionFromMimeType(cr.getType(uri));
            }

            // create a temp file to copy the uri contents into
            String tempFilePath = getTempFilePath(fileName, context);

            output = new FileOutputStream(tempFilePath);
            int read = 0;
            byte[] bytes = new byte[4096];
            while ((read = input.read(bytes)) != -1) {
                output.write(bytes, 0, read);
            }
            // return the filepath of our copied file.
            file =  new File(tempFilePath);

        } catch (FileNotFoundException e) {
            file = null;
            errorMessage = context.getString(R.string.errorOccurred);
            Log.e(Const.PANDA_UTILS_FILE_UPLOAD_UTILS_LOG, e.toString());
        } catch (Exception exception) {
            // if querying the datacolumn and the FileDescriptor both fail We can't handle the shared file.
            file = null;
            Log.e(Const.PANDA_UTILS_FILE_UPLOAD_UTILS_LOG, exception.toString());
            errorMessage = context.getString(R.string.errorLoadingFiles);
        } finally {
            if (input != null) try {
                input.close();
            } catch (Exception ignored) {}
            if (output != null) try {
                output.close();
            } catch (Exception ignored) {}
        }

        if(file != null){
            return new FileSubmitObject(fileName, file.length(), mimeType, file.getAbsolutePath(), errorMessage);
        }
        return new FileSubmitObject(fileName, 0, mimeType, "", errorMessage);
    }

    public static String getFileNameColumn(ContentResolver resolver, Uri uri){
        String fileName = "";
        final String[] proj = { MediaStore.MediaColumns.DISPLAY_NAME };

        // get file name
        Cursor metaCursor = resolver.query(uri, proj, null, null, null);
        if (metaCursor != null) {
            try {
                if (metaCursor.moveToFirst()) {
                    fileName = metaCursor.getString(0);
                }
            }catch(Exception e){

            } finally {
                metaCursor.close();
            }
        }

        return fileName;
    }

    public static String getFileNameWithDefault(ContentResolver resolver, Uri uri, String mimeType){
        String fileName = "";
        String scheme = uri.getScheme();
        if (FILE_SCHEME.equalsIgnoreCase(scheme)) {
            fileName = uri.getLastPathSegment();
        } else if (CONTENT_SCHEME.equalsIgnoreCase(scheme)) {
            final String[] proj = {MediaStore.MediaColumns.DISPLAY_NAME};

            // get file name
            Cursor metaCursor = resolver.query(uri, proj, null, null, null);
            if (metaCursor != null) {
                try {
                    if (metaCursor.moveToFirst()) {
                        fileName = metaCursor.getString(0);
                    }
                } catch (Exception e) {

                } finally {
                    metaCursor.close();
                }
            }
        }

        return getTempFilename(fileName);
    }

    public static long getFileSizeColumn(ContentResolver resolver, Uri uri){
        long fileSize = 0;
        final String[] proj = { MediaStore.MediaColumns.SIZE };

        // get file name
        Cursor metaCursor = resolver.query(uri, proj, null, null, null);
        if (metaCursor != null) {
            try {
                if (metaCursor.moveToFirst()) {
                    fileSize = metaCursor.getLong(0);
                }
            }catch(Exception e){

            } finally {
                metaCursor.close();
            }
        }

        return fileSize;
    }

    public static String getFileMimeType(ContentResolver resolver, Uri uri){
        String scheme = uri.getScheme();
        String mimeType = null;
        if (FILE_SCHEME.equalsIgnoreCase(scheme)) {
            if (uri.getLastPathSegment() != null) {
                mimeType = getMimeTypeFromFileNameWithExtension(uri.getLastPathSegment());
            }
        } else if (CONTENT_SCHEME.equalsIgnoreCase(scheme)) {
             mimeType = resolver.getType(uri);
        }
        if(mimeType == null){
            return "*/*";
        }
        return mimeType;
    }

    public static String getMimeTypeFromFileNameWithExtension(String fileNameWithExtension) {
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        int index = fileNameWithExtension.indexOf(".");
        String ext = "";
        if (index != -1) {
            ext = fileNameWithExtension.substring(index + 1).toLowerCase(); // Add one so the dot isn't included
        }
        return mime.getMimeTypeFromExtension(ext);
    }

    public static String getFileExtensionFromMimeType(String mimeType){
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String extension =  mime.getExtensionFromMimeType(mimeType);
        if(extension == null){
            return "";
        }
        return extension;
    }

    public static boolean hasExtension(String fileName){
        int lastDot = fileName.lastIndexOf(".");
        if (lastDot != -1) {
            return true;
        }
        return false;
    }

    /**
     * Generates a filename for files which are either unnamed or are given a default name.
     * Some apps (google photos) will return 'image.jpg' for unnamed images. This will also append the
     * extension to the end of the filename, for cases where that is excluded from the URI.
     * @param fileName
     * @return
     */
    public static String getTempFilename(String fileName){
        if(fileName == null || "".equals(fileName)){
            fileName = "File_Upload";
        }
        else if(fileName.equals("image.jpg")){
            fileName = "Image_Upload";
        }
        else if(fileName.equals("video.mpg") || fileName.equals("video.mpeg")){
            // image doesn't have a name.
            fileName = "Video_Upload";
        }

        return fileName;
    }

    /**
     * Creates a new file from the context of our activity and passes back the Path to that new file.

     * @param fileName
     * @return
     * @throws IOException
     */
    private static String getTempFilePath(String fileName, Context context) throws IOException {
        return getTempFolder(context).getPath() + "/" + fileName;
    }

/*
* Maintaining for history
* *//**
     * Users can view the files they're uploading to canvas before uploading. In order to share copied files from the cloud,
     * we create an external directory for these files, then delete them after uploads finish.
     * @return Directory for the CanvasFolder
     *//*
    public static File getExternalCanvasFolder(){
        File canvasFolder = new File(Environment.getExternalStorageDirectory(), "Canvas");
        if (!canvasFolder.exists()) {
            canvasFolder.mkdirs();
        }
        return canvasFolder;
    }

    public static boolean deleteCanvasDirectory(){
        return deleteDirectory(getExternalCanvasFolder());
    }

    */

    public static File getTempFolder(Context context) {
        File tempFolder = new File(context.getCacheDir(), "temp");
        tempFolder.mkdirs();
        return tempFolder;
    }

    public static boolean deleteDirectory(File fileFolder){
        if (fileFolder.isDirectory()){
            String[] children = fileFolder.list();
            for (int i = 0; i < children.length; i++){
                boolean success = deleteDirectory(new File(fileFolder, children[i]));
                if (!success){
                    return false;
                }
            }
        }
        return fileFolder.delete();
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static Bundle createTaskLoaderBundle(CanvasContext canvasContext, String url, String title, boolean authenticate) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Const.CANVAS_CONTEXT, canvasContext);
        bundle.putString(Const.INTERNAL_URL, url);
        bundle.putBoolean(Const.AUTHENTICATE, authenticate);
        bundle.putString(Const.ACTION_BAR_TITLE, title);
        return bundle;
    }
}
