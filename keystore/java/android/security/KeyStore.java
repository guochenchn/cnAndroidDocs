/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.security;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

/**
 * @hide This should not be made public in its present form because it
 * assumes that private and secret key bytes are available and would
 * preclude the use of hardware crypto.
 */
public class KeyStore {
    private static final String TAG = "KeyStore";

    // ResponseCodes
    public static final int NO_ERROR = 1;
    public static final int LOCKED = 2;
    public static final int UNINITIALIZED = 3;
    public static final int SYSTEM_ERROR = 4;
    public static final int PROTOCOL_ERROR = 5;
    public static final int PERMISSION_DENIED = 6;
    public static final int KEY_NOT_FOUND = 7;
    public static final int VALUE_CORRUPTED = 8;
    public static final int UNDEFINED_ACTION = 9;
    public static final int WRONG_PASSWORD = 10;

    // States
    public enum State { UNLOCKED, LOCKED, UNINITIALIZED };

    private int mError = NO_ERROR;

    private final IKeystoreService mBinder;

    private KeyStore(IKeystoreService binder) {
        mBinder = binder;
    }

    public static KeyStore getInstance() {
        IKeystoreService keystore = IKeystoreService.Stub.asInterface(ServiceManager
                .getService("android.security.keystore"));
        return new KeyStore(keystore);
    }

    public State state() {
        final int ret;
        try {
            ret = mBinder.test();
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            throw new AssertionError(e);
        }

        switch (ret) {
            case NO_ERROR: return State.UNLOCKED;
            case LOCKED: return State.LOCKED;
            case UNINITIALIZED: return State.UNINITIALIZED;
            default: throw new AssertionError(mError);
        }
    }

    public boolean isUnlocked() {
        return state() == State.UNLOCKED;
    }

    public byte[] get(String key) {
        try {
            return mBinder.get(key);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return null;
        }
    }

    public boolean put(String key, byte[] value, int uid) {
        try {
            return mBinder.insert(key, value, uid) == NO_ERROR;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean put(String key, byte[] value) {
        return put(key, value, -1);
    }

    public boolean delete(String key, int uid) {
        try {
            return mBinder.del(key, uid) == NO_ERROR;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean delete(String key) {
        return delete(key, -1);
    }

    public boolean contains(String key, int uid) {
        try {
            return mBinder.exist(key, uid) == NO_ERROR;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean contains(String key) {
        return contains(key, -1);
    }

    public String[] saw(String prefix, int uid) {
        try {
            return mBinder.saw(prefix, uid);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return null;
        }
    }

    public String[] saw(String prefix) {
        return saw(prefix, -1);
    }

    public boolean reset() {
        try {
            return mBinder.reset() == NO_ERROR;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean password(String password) {
        try {
            return mBinder.password(password) == NO_ERROR;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean lock() {
        try {
            return mBinder.lock() == NO_ERROR;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean unlock(String password) {
        try {
            mError = mBinder.unlock(password);
            return mError == NO_ERROR;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean isEmpty() {
        try {
            return mBinder.zero() == KEY_NOT_FOUND;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean generate(String key, int uid) {
        try {
            return mBinder.generate(key, uid) == NO_ERROR;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean generate(String key) {
        return generate(key, -1);
    }

    public boolean importKey(String keyName, byte[] key, int uid) {
        try {
            return mBinder.import_key(keyName, key, uid) == NO_ERROR;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean importKey(String keyName, byte[] key) {
        return importKey(keyName, key, -1);
    }

    public byte[] getPubkey(String key) {
        try {
            return mBinder.get_pubkey(key);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return null;
        }
    }

    public boolean delKey(String key, int uid) {
        try {
            return mBinder.del_key(key, uid) == NO_ERROR;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean delKey(String key) {
        return delKey(key, -1);
    }

    public byte[] sign(String key, byte[] data) {
        try {
            return mBinder.sign(key, data);
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return null;
        }
    }

    public boolean verify(String key, byte[] data, byte[] signature) {
        try {
            return mBinder.verify(key, data, signature) == NO_ERROR;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean grant(String key, int uid) {
        try {
            return mBinder.grant(key, uid) == NO_ERROR;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public boolean ungrant(String key, int uid) {
        try {
            return mBinder.ungrant(key, uid) == NO_ERROR;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    /**
     * Returns the last modification time of the key in milliseconds since the
     * epoch. Will return -1L if the key could not be found or other error.
     */
    public long getmtime(String key) {
        try {
            final long millis = mBinder.getmtime(key);
            if (millis == -1L) {
                return -1L;
            }

            return millis * 1000L;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return -1L;
        }
    }

    public boolean duplicate(String srcKey, int srcUid, String destKey, int destUid) {
        try {
            return mBinder.duplicate(srcKey, srcUid, destKey, destUid) == NO_ERROR;
        } catch (RemoteException e) {
            Log.w(TAG, "Cannot connect to keystore", e);
            return false;
        }
    }

    public int getLastError() {
        return mError;
    }
}
