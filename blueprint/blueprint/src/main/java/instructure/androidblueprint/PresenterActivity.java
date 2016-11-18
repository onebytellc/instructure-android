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

package instructure.androidblueprint;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

/**
 * The Presenter will decide what to do for every incoming event,
 * retrieving or updating data from the Model and preparing the data so the View can display it.
 */
public abstract class PresenterActivity<PRESENTER extends Presenter<VIEW>, VIEW> extends AppCompatActivity {

    /**
     * This point in time we have a presenter prepared and the view interface has been initialized
     * We are ready to go go go
     * The same as setting views in onStart() but also provides a Presenter
     * @param presenter An implementation of the Presenter interface
     */
    protected abstract void onReadySetGo(PRESENTER presenter);
    protected abstract PresenterFactory<PRESENTER> getPresenterFactory();
    protected abstract void onPresenterPrepared(PRESENTER presenter);

    private static final int LOADER_ID = 1001;

    private Presenter<VIEW> mPresenter;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportLoaderManager().initLoader(LOADER_ID, null, new LoaderManager.LoaderCallbacks<PRESENTER>() {
            @Override
            public final Loader<PRESENTER> onCreateLoader(int id, Bundle args) {
                return new PresenterLoader<>(PresenterActivity.this, getPresenterFactory());
            }

            @Override
            public final void onLoadFinished(Loader<PRESENTER> loader, PRESENTER presenter) {
                PresenterActivity.this.mPresenter = presenter;
                if(getIntent().getExtras() != null) {
                    unBundle(getIntent().getExtras());
                }
                onPresenterPrepared(presenter);
            }

            @Override
            public final void onLoaderReset(Loader<PRESENTER> loader) {
                PresenterActivity.this.mPresenter = null;
                onPresenterDestroyed();
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onStart() {
        super.onStart();
        onReadySetGo((PRESENTER)mPresenter.onViewAttached(getPresenterView()));
    }

    @Override
    protected void onStop() {
        mPresenter.onViewDetached();
        super.onStop();
    }

    protected void onPresenterDestroyed() {
        // hook for subclasses
    }

    // Override in case of Activity not implementing Presenter<View> interface
    @SuppressWarnings("unchecked")
    protected VIEW getPresenterView() {
        return (VIEW) this;
    }

    @SuppressWarnings("unchecked")
    protected PRESENTER getPresenter() {
        return (PRESENTER)mPresenter;
    }

    /**
     * Only gets called if extras is not null
     * @param extras A non-null group of extras in Bundle form
     */
    protected void unBundle(@NonNull Bundle extras){}
}

