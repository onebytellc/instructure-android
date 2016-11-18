/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.instructure.androidfoosball.ktmodels

import io.realm.RealmObject

/**
 * There is currently a bug in the Realm plugin (possibly related to kapt?) which may cause
 * it to generate a mediator class with unbuildable code. The current workaround is to
 * ensure at least one class extends [RealmObject]. Because the 'phone' app flavor does not use
 * Realm, and as far as I can tell there is no way to disable the Realm plugin for a specific
 * flavor, we'll use this dummy class as the workaround.
 *
 * If it still won't build, try adding `kotlin.incremental=false` to gradle.properties.
 */
@Suppress("unused")
open class RealmWorkaround(open var isDumb: Boolean = true) : RealmObject() { }
