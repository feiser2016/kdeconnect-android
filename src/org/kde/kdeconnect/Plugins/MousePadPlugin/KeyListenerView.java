/*
 * Copyright 2014 Saikrishna Arcot <saiarcot895@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of
 * the License or (at your option) version 3 or any later version
 * accepted by the membership of KDE e.V. (or its successor approved
 * by the membership of KDE e.V.), which shall act as a proxy
 * defined in Section 14 of version 3 of the license.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
*/

package org.kde.kdeconnect.Plugins.MousePadPlugin;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import org.kde.kdeconnect.BackgroundService;
import org.kde.kdeconnect.Device;
import org.kde.kdeconnect.NetworkPackage;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class KeyListenerView extends View  {

    private String deviceId;

    private static HashMap<Integer, Integer> SpecialKeysMap = new HashMap<Integer, Integer>();
    static {
        int i = 0;
        SpecialKeysMap.put(KeyEvent.KEYCODE_DEL, ++i);              // 1
        SpecialKeysMap.put(KeyEvent.KEYCODE_TAB, ++i);              // 2
        SpecialKeysMap.put(KeyEvent.KEYCODE_ENTER, 12); ++i;        // 3 is not used, return is 12 instead
        SpecialKeysMap.put(KeyEvent.KEYCODE_DPAD_LEFT, ++i);        // 4
        SpecialKeysMap.put(KeyEvent.KEYCODE_DPAD_UP, ++i);          // 5
        SpecialKeysMap.put(KeyEvent.KEYCODE_DPAD_RIGHT, ++i);       // 6
        SpecialKeysMap.put(KeyEvent.KEYCODE_DPAD_DOWN, ++i);        // 7
        SpecialKeysMap.put(KeyEvent.KEYCODE_PAGE_UP, ++i);          // 8
        SpecialKeysMap.put(KeyEvent.KEYCODE_PAGE_DOWN, ++i);        // 9
        if (Build.VERSION.SDK_INT >= 11) {
            SpecialKeysMap.put(KeyEvent.KEYCODE_MOVE_HOME, ++i);        // 10
            SpecialKeysMap.put(KeyEvent.KEYCODE_MOVE_END, ++i);         // 11
            SpecialKeysMap.put(KeyEvent.KEYCODE_NUMPAD_ENTER, ++i);     // 12
            SpecialKeysMap.put(KeyEvent.KEYCODE_FORWARD_DEL, ++i);      // 13
            SpecialKeysMap.put(KeyEvent.KEYCODE_ESCAPE, ++i);           // 14
            ++i;           // 15
            ++i;           // 16
            ++i;           // 17
            ++i;           // 18
            ++i;           // 19
            ++i;           // 20
            SpecialKeysMap.put(KeyEvent.KEYCODE_F1, ++i);           // 21
            SpecialKeysMap.put(KeyEvent.KEYCODE_F2, ++i);           // 22
            SpecialKeysMap.put(KeyEvent.KEYCODE_F3, ++i);           // 23
            SpecialKeysMap.put(KeyEvent.KEYCODE_F4, ++i);           // 24
            SpecialKeysMap.put(KeyEvent.KEYCODE_F5, ++i);           // 25
            SpecialKeysMap.put(KeyEvent.KEYCODE_F6, ++i);           // 26
            SpecialKeysMap.put(KeyEvent.KEYCODE_F7, ++i);           // 27
            SpecialKeysMap.put(KeyEvent.KEYCODE_F8, ++i);           // 28
            SpecialKeysMap.put(KeyEvent.KEYCODE_F9, ++i);           // 29
            SpecialKeysMap.put(KeyEvent.KEYCODE_F10, ++i);          // 30
            SpecialKeysMap.put(KeyEvent.KEYCODE_F11, ++i);          // 31
            SpecialKeysMap.put(KeyEvent.KEYCODE_F12, ++i);          // 21
        }
    }

    public void setDeviceId(String id) {
        deviceId = id;
    }

    public KeyListenerView(Context context, AttributeSet set) {
        super(context, set);

        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_FULLSCREEN;
        return null;
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        char utfChar = (char)event.getUnicodeChar();
        if (utfChar == 9 || utfChar == 10) utfChar = 0; //Workaround to send enter and tab as special keys instead of characters
        //Log.e("KeyDown","keycode:"+keyCode);

        final NetworkPackage np = new NetworkPackage(NetworkPackage.PACKAGE_TYPE_MOUSEPAD);

        if (utfChar != 0) {
            final String utfString = new String(new char[]{utfChar});
            //Log.e("KeyDown","utfString:"+utfString);
            np.set("key", utfString);
        } else {
            if (!SpecialKeysMap.containsKey(keyCode)) {
                return false;
            }
            final int specialKey = SpecialKeysMap.get(keyCode);
            np.set("specialKey", specialKey);
        }

        if (event != null) {
            if (event.isAltPressed()) {
                np.set("alt", true);
            }
            if (Build.VERSION.SDK_INT >= 11) {
                if (event.isCtrlPressed()) {
                    np.set("ctrl", true);
                }
            }
        }

        BackgroundService.RunCommand(getContext(), new BackgroundService.InstanceCallback() {
            @Override
            public void onServiceStart(BackgroundService service) {
                Device device = service.getDevice(deviceId);
                MousePadPlugin mousePadPlugin = (MousePadPlugin) device.getPlugin("plugin_mousepad");
                if (mousePadPlugin == null) return;
                mousePadPlugin.sendKeyboardPacket(np);
            }
        });

        return true;
    }

}
