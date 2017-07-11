/****************************************************************************
 ** Copyright (C) 2004-2017 Mazatech S.r.l. All rights reserved.
 **
 ** This file is part of AmanithVG software, an OpenVG implementation.
 **
 ** Khronos and OpenVG are trademarks of The Khronos Group Inc.
 ** OpenGL is a registered trademark and OpenGL ES is a trademark of
 ** Silicon Graphics, Inc.
 **
 ** This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 ** WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 **
 ** For any information, please contact info@mazatech.com
 **
 ****************************************************************************/
package com.mazatech.amanithvg.tutorial09;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Build;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static com.mazatech.amanithvg.tutorial09.TutorialView.*;

public class TutorialActivity extends AppCompatActivity {

    // keep track of loaded AmanithVG native libraries
    static boolean nativeLibsLoaded = false;
    // view
    private TutorialView view = null;
    // menu
    static final int QUIT_MENU_ITEM = 99;

    private boolean copyFile(InputStream input, OutputStream output) {

        boolean result;

        try {
            byte[] buffer = new byte[4096];
            while (true) {
                int length = input.read(buffer);
                if (length == -1) {
                    break;
                }
                output.write(buffer, 0, length);
            }
            // close streams
            input.close();
            output.close();
            result = true;
        }
        catch (java.io.IOException e) {
            System.err.println("File copy failed.\n" + e);
            result = false;
        }

        return result;
    }

    private boolean loadSharedLibrary(String libPath, String libName) {

        boolean result;
        String tmpPath = System.getProperty("java.io.tmpdir") + "/AmanithVG/";
        String tmpLib = tmpPath + libName;
        // ensure the existence of destination directory
        File tmpFile = new File(tmpLib);
        tmpFile.getParentFile().mkdirs();

        try {
            InputStream input = getAssets().open(libPath + libName);
            FileOutputStream output = new FileOutputStream(tmpFile);
            result = copyFile(input, output);
        }
        catch (java.io.IOException e) {
            System.err.println("Opening file streams failed.\n" + e);
            result = false;
        }

        if (result) {
            try {
                System.load(tmpLib);
            }
            catch (UnsatisfiedLinkError e) {
                System.err.println("Native code library failed to load, the file does not exist.\n" + e);
                result = false;
            }
        }

        return result;
    }

    private boolean loadAmanithVG() {

        boolean result = true;

        String vm = System.getProperty("java.vm.name");

        if (vm != null && vm.contains("Dalvik")) {

            String abi = (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) ? Build.CPU_ABI : Build.SUPPORTED_ABIS[0];
            String vgLibsPath = "amanithvg-natives";
            // check if we have to use AmanithVG SRE (or GLE)
            boolean sreBackend = getResources().getBoolean(R.bool.sreBackend);

            if (abi.equals("arm64-v8a")) {
                if (System.getProperty("os.arch").startsWith("armv7")) {
                    abi = "armeabi-v7a";
                }
            }

            if (abi.equals("armeabi")) {
                vgLibsPath += "/armeabi";
            }
            else
            if (abi.equals("armeabi-v7a")) {
                vgLibsPath += "/armeabi-v7a";
            }
            else
            if (abi.equals("arm64-v8a")) {
                vgLibsPath += "/arm64-v8a";
            }
            else
            if (abi.equals("mips")) {
                vgLibsPath += "/mips";
            }
            else
            if (abi.equals("mips64")) {
                vgLibsPath += "/mips64";
            }
            else
            if (abi.equals("x86")) {
                vgLibsPath += "/x86";
            }
            else
            if (abi.equals("x86_64")) {
                vgLibsPath += "/x86_64";
            }
            else {
                result = false;
            }
            // select the backend engine (SRE / GLE)
            vgLibsPath += (sreBackend) ? "/sre" : "/gle";
            vgLibsPath += "/standalone/";

            if (result) {
                // load AmanithVG library
                result = loadSharedLibrary(vgLibsPath, "libAmanithVG.so");
                if (result) {
                    // load AmanithVG JNI wrapper
                    result = loadSharedLibrary(vgLibsPath, "libAmanithVGJNI.so");
                }
            }
        }

        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // load AmanithVG native libraries, if needed
        if (!nativeLibsLoaded) {
            nativeLibsLoaded = loadAmanithVG();
        }
        if (nativeLibsLoaded) {
            // create the view
            view = new TutorialView(this);
            setContentView(view);
        }
        else {
            setContentView(R.layout.error_view);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // build the menu
        menu.add(0, TUTORIAL_ABOUT_CMD, 0, "About...");
        menu.add(0, QUIT_MENU_ITEM, 0, "Quit");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        boolean result = view.tutorialMenuOption(item.getItemId());

        if (!result) {
            if (item.getItemId() == QUIT_MENU_ITEM) {
                finish();
                result = true;
            }
            else {
                result = super.onOptionsItemSelected(item);
            }
        }
        return result;
    }
}
