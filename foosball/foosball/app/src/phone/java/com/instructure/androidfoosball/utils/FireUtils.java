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

package com.instructure.androidfoosball.utils;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.instructure.androidfoosball.models.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class FireUtils {

    public interface OnIntValue {
        void onValueFound(int value);
    }

    public interface OnStringValue {
        void onValueFound(String value);
    }

    public interface OnTablesValue {
        void onValueFound(List<Table> values);
    }

    public static void getTables(final DatabaseReference database, final OnTablesValue callback) {
        database.keepSynced(false);
        database.child("tables").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Table> tables = new ArrayList<>();
                Map<String, Object> data = (Map<String, Object>) dataSnapshot.getValue();

                for (Object obj : data.values()) {
                    if (obj instanceof Map) {
                        Map<String, Object> mapObj = (Map<String, Object>) obj;
                        Table table = new Table();
                        //Will not have an id set
                        table.setCurrentGame((String)mapObj.get("currentGame"));
                        table.setName((String)mapObj.get("name"));
                        table.setSideOneColor((String)mapObj.get("sideOneColor"));
                        table.setSideTwoColor((String)mapObj.get("sideTwoColor"));
                        table.setSideOneName((String)mapObj.get("sideOneName"));
                        table.setSideTwoName((String)mapObj.get("sideTwoName"));
                        tables.add(table);
                    }
                }
                callback.onValueFound(tables);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onValueFound(new ArrayList<Table>());
            }
        });
    }

    public static void setStartupPhrase(final String id, final DatabaseReference database, final String phrase) {
        database.child("users").child(id).child("customAssignmentPhrase").setValue(phrase);
    }

    public static void setVictoryPhrase(final String id, final DatabaseReference database, final String phrase) {
        database.child("users").child(id).child("customVictoryPhrase").setValue(phrase);
    }

    public static void getWinCount(final String id, final DatabaseReference database, final OnIntValue callback) {
        database.child("users").child(id).child("wins").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer value = dataSnapshot.getValue(Integer.class);
                if(value == null) {
                    callback.onValueFound(0);
                } else {
                    callback.onValueFound(value);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onValueFound(0);
            }
        });
    }

    public static void getLossCount(final String id, final DatabaseReference database, final OnIntValue callback) {
        database.child("users").child(id).child("losses").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer value = dataSnapshot.getValue(Integer.class);
                if(value == null) {
                    callback.onValueFound(0);
                } else {
                    callback.onValueFound(value);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onValueFound(0);
            }
        });
    }

    public static void getStartupPhrase(final String id, final DatabaseReference database, final OnStringValue callback) {
        database.child("users").child(id).child("customAssignmentPhrase").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                if(value == null) {
                    callback.onValueFound("");
                } else {
                    callback.onValueFound(value);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onValueFound("");
            }
        });
    }

    public static void getVictoryPhrase(final String id, final DatabaseReference database, final OnStringValue callback) {
        database.child("users").child(id).child("customVictoryPhrase").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                if(value == null) {
                    callback.onValueFound("");
                } else {
                    callback.onValueFound(value);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onValueFound("");
            }
        });
    }
}
