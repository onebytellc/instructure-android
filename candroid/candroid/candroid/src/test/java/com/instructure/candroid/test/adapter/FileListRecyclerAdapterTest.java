/*
 * Copyright (C) 2016 - present  Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.candroid.test.adapter;

import android.content.Context;
import android.test.InstrumentationTestCase;

import com.instructure.candroid.adapter.FileListRecyclerAdapter;
import com.instructure.canvasapi.model.FileFolder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@Config(sdk = 17)
@RunWith(RobolectricTestRunner.class)
public class FileListRecyclerAdapterTest extends InstrumentationTestCase {
    private FileListRecyclerAdapter mAdapter;

    public static class FileListRecyclerAdapterWrapper extends FileListRecyclerAdapter {
        protected FileListRecyclerAdapterWrapper(Context context) { super(context, null, 0, "", null, false); }
    }

    @Before
    public void setup(){
        mAdapter = new FileListRecyclerAdapterWrapper(RuntimeEnvironment.application);
    }

    @Test
    public void testAreContentsTheSame_SameObjects(){
        FileFolder fileFolder = new FileFolder();
        fileFolder.setDisplayName("fileFolder");
        fileFolder.setSize(0);
        assertTrue(mAdapter.getItemCallback().areContentsTheSame(fileFolder, fileFolder));
    }

    @Test
    public void testAreContentsTheSame_DifferentObjectNames(){
        FileFolder fileFolder1 = new FileFolder();
        fileFolder1.setDisplayName("fileFolder1");
        fileFolder1.setSize(100);

        FileFolder fileFolder2 = new FileFolder();
        fileFolder2.setDisplayName("fileFolder2");
        fileFolder2.setSize(100);

        assertFalse(mAdapter.getItemCallback().areContentsTheSame(fileFolder1, fileFolder2));
    }

    @Test
    public void testAreContentsTheSame_DifferentObjectSizes(){
        FileFolder fileFolder1 = new FileFolder();
        fileFolder1.setDisplayName("fileFolder");
        fileFolder1.setSize(10);

        FileFolder fileFolder2 = new FileFolder();
        fileFolder2.setDisplayName("fileFolder");
        fileFolder2.setSize(100);

        assertFalse(mAdapter.getItemCallback().areContentsTheSame(fileFolder1, fileFolder2));
    }

    @Test
    public void testAreContentsTheSame_SameFolders(){
        FileFolder fileFolder = new FileFolder();
        fileFolder.setName("fileFolder");
        fileFolder.setSize(0);
        assertTrue(mAdapter.getItemCallback().areContentsTheSame(fileFolder, fileFolder));
    }

    @Test
    public void testAreContentsTheSame_DifferentFolderNames(){
        FileFolder fileFolder1 = new FileFolder();
        fileFolder1.setName("fileFolder1");
        fileFolder1.setSize(100);

        FileFolder fileFolder2 = new FileFolder();
        fileFolder2.setName("fileFolder2");
        fileFolder2.setSize(100);

        assertFalse(mAdapter.getItemCallback().areContentsTheSame(fileFolder1, fileFolder2));
    }

    @Test
    public void testAreContentsTheSame_DifferentFolderSizes(){
        FileFolder fileFolder1 = new FileFolder();
        fileFolder1.setName("fileFolder");
        fileFolder1.setSize(10);

        FileFolder fileFolder2 = new FileFolder();
        fileFolder2.setName("fileFolder");
        fileFolder2.setSize(100);

        assertFalse(mAdapter.getItemCallback().areContentsTheSame(fileFolder1, fileFolder2));
    }

}
