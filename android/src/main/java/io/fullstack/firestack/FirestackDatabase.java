package io.fullstack.firestack;

import android.content.Context;
import android.util.Log;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReactContext;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

class FirestackDatabaseModule extends ReactContextBaseJavaModule {

  private static final String TAG = "FirestackDatabase";

  private Context context;
  private ReactContext mReactContext;
  private Map<String, Integer> mDBListeners = new HashMap<String, Integer>();

  public FirestackDatabaseModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.context = reactContext;
    mReactContext = reactContext;

    Log.d(TAG, "New instance");
  }

  @Override
  public String getName() {
    return TAG;
  }

  @ReactMethod
  public void set(final String path, final ReadableMap props, final Callback callback) {
    // TODO
    FirestackUtils.todoNote(TAG, "set", callback);
  }

  @ReactMethod
  public void update(final String path, final ReadableMap props, final Callback callback) {
    // TODO
    FirestackUtils.todoNote(TAG, "update", callback);
  }

  @ReactMethod
  public void remove(final String path, final Callback callback) {
    // TODO
    FirestackUtils.todoNote(TAG, "remove", callback);
  }

  @ReactMethod
  public void on(final String path, final String name, final Callback callback) {
    // TODO
    // FirestackUtils.todoNote(TAG, "on", callback);
    Log.d(TAG, "Setting a listener on event: " + name + " for path " + path);
    DatabaseReference ref = this.getDatabaseReferenceAtPath(path);
    final FirestackDatabaseModule self = this;

    if (name.equals("value")) {
      ValueEventListener listener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            WritableMap data = self.dataSnapshotToMap(name, dataSnapshot);
            FirestackUtils.sendEvent(mReactContext, name, data);
        }

        @Override
        public void onCancelled(DatabaseError error) {
            // Failed to read value
            Log.w(TAG, "Failed to read value.", error.toException());
        }
      };
      ref.addValueEventListener(listener);
    } else {
      ChildEventListener listener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
          if (name.equals("child_added")) {
            WritableMap data = self.dataSnapshotToMap(name, dataSnapshot);
            FirestackUtils.sendEvent(mReactContext, name, data);
          }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
          if (name.equals("child_changed")) {
            WritableMap data = self.dataSnapshotToMap(name, dataSnapshot);
            FirestackUtils.sendEvent(mReactContext, name, data);
          }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
          if (name.equals("child_removed")) {
            WritableMap data = self.dataSnapshotToMap(name, dataSnapshot);
            FirestackUtils.sendEvent(mReactContext, name, data);
          }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
          if (name.equals("child_moved")) {
            WritableMap data = self.dataSnapshotToMap(name, dataSnapshot);
            FirestackUtils.sendEvent(mReactContext, name, data);
          }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
          Log.w(TAG, "onCancelled", databaseError.toException());
        }
      };
      ref.addChildEventListener(listener);
    }

    // TODO: Store handles in the mDBListeners hashmap
    // Store the key of the listener... somehow
    String key = "listener_" + path + "_" + name;
    // Integer code = listener.hashCode();
    // String key = code.toString();

    // mDBListeners.put(key, code);

    Log.d(TAG, "Added listener " + key);
    WritableMap resp = Arguments.createMap();
    resp.putString("handle", key);
    callback.invoke(null, resp);
  }

  @ReactMethod
  public void onOnce(final String path, final String name, final Callback callback) {
    // TODO
    FirestackUtils.todoNote(TAG, "onOnce", callback);
  }

  @ReactMethod
  public void off(final String path, final String name, final Callback callback) {
    // TODO
    FirestackUtils.todoNote(TAG, "on", callback);
  }

  @ReactMethod
  public void removeListeners(final String path, final String name, final Callback callback) {
    // TODO
    FirestackUtils.todoNote(TAG, "on", callback);
  }

  private DatabaseReference getDatabaseReferenceAtPath(final String path) {
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference(path);
    return mDatabase;
  }

  private WritableMap dataSnapshotToMap(String name, DataSnapshot dataSnapshot) {
    WritableMap data = Arguments.createMap();

    data.putString("key", dataSnapshot.getKey());
    data.putBoolean("exists", dataSnapshot.exists());
    data.putBoolean("hasChildren", dataSnapshot.hasChildren());

    data.putDouble("childrenCount", dataSnapshot.getChildrenCount());

    WritableMap valueMap = this.castSnapshotValue(dataSnapshot);
    data.putMap("value", valueMap);

    Object priority = dataSnapshot.getPriority();
    if (priority == null) {
      data.putString("priority", "null");
    } else {
      data.putString("priority", priority.toString());
    }

    WritableMap eventMap = Arguments.createMap();
    eventMap.putString("eventName", name);
    eventMap.putMap("snapshot", data);
    return eventMap;
  }

  private <Any> Any castSnapshotValue(DataSnapshot snapshot) {
      if (snapshot.hasChildren()) {
        WritableMap data = Arguments.createMap();
        for (DataSnapshot child : snapshot.getChildren()) {
            Any castedChild = castSnapshotValue(child);
            switch (castedChild.getClass().getName()) {
                case "java.lang.Boolean":
                    data.putBoolean(child.getKey(), (Boolean) castedChild);
                    break;
                case "java.lang.Integer":
                    data.putInt(child.getKey(), (Integer) castedChild);
                    break;
                case "java.lang.Double":
                    data.putDouble(child.getKey(), (Double) castedChild);
                    break;
                case "java.lang.String":
                    data.putString(child.getKey(), (String) castedChild);
                    break;
                case "com.facebook.react.bridge.WritableNativeMap":
                    data.putMap(child.getKey(), (WritableMap) castedChild);
                    break;
            }
        }
        return (Any) data;
    } else {
        String type = snapshot.getValue().getClass().getName();
        switch (type) {
            case "java.lang.Boolean":
                return (Any)((Boolean) snapshot.getValue());
            case "java.lang.Long":
                // TODO check range errors
                return (Any)((Integer)(((Long) snapshot.getValue()).intValue()));
            case "java.lang.Double":
                return (Any)((Double) snapshot.getValue());
            case "java.lang.String":
                return (Any)((String) snapshot.getValue());
            default:
                return (Any) null;
        }
    }
  }
}