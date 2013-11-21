/*
 * Copyright (c) 2009, 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/**
 * @test
 * @bug 4167874
 * @library ../../../../com/sun/net/httpserver
 * @library /lib/testlibrary
 * @build FileServerHandler jdk.testlibrary.FileUtils
 * @run shell build.sh
 * @run main/othervm CloseTest
 * @summary URL-downloaded jar files can consume all available file descriptors
 */

import java.io.*;
import java.net.*;
import java.lang.reflect.*;
import com.sun.net.httpserver.*;

public class CloseTest extends Common {

//
// needs two jar files test1.jar and test2.jar with following structure
//
// com/foo/TestClass
// com/foo/TestClass1
// com/foo/Resource1
// com/foo/Resource2
//
// and a directory hierarchy with the same structure/contents

    public static void main (String args[]) throws Exception {

        String workdir = System.getProperty("test.classes");
        if (workdir == null) {
            workdir = args[0];
        }
        if (!workdir.endsWith("/")) {
            workdir = workdir+"/";
        }

        startHttpServer (workdir+"serverRoot/");

        String testjar = workdir + "test.jar";
        copyFile (workdir+"test1.jar", testjar);
        test (testjar, 1);

        // repeat test with different implementation
        // of test.jar (whose TestClass.getValue() returns 2

        copyFile (workdir+"test2.jar", testjar);
        test (testjar, 2);

        // repeat test using a directory of files
        String testdir=workdir+"testdir/";
        rm_minus_rf (new File(testdir));
        copyDir (workdir+"test1/", testdir);
        test (testdir, 1);

        testdir=workdir+"testdir/";
        rm_minus_rf (new File(testdir));
        copyDir (workdir+"test2/", testdir);
        test (testdir, 2);
        getHttpServer().stop (3);
    }

    // create a loader on jarfile (or directory), plus a http loader
    // load a class , then look for a resource
    // also load a class from http loader
    // then close the loader
    // check further new classes/resources cannot be loaded
    // check jar (or dir) can be deleted
    // check existing classes can be loaded
    // check boot classes can be loaded

    static void test (String name, int expectedValue) throws Exception {
        URL url = new URL ("file", null, name);
        URL url2 = getServerURL();
        System.out.println ("Doing tests with URL: " + url + " and " + url2);
        URL[] urls = new URL[2];
        urls[0] =  url;
        urls[1] =  url2;
        URLClassLoader loader = new URLClassLoader (urls);
        Class testclass = loadClass ("com.foo.TestClass", loader, true);
        Class class2 = loadClass ("Test", loader, true); // from http
        class2.newInstance();
        Object test = testclass.newInstance();
        Method method = testclass.getDeclaredMethods()[0]; // int getValue();
        int res = (Integer) method.invoke (test);

        if (res != expectedValue) {
            throw new RuntimeException ("wrong value from getValue() ["+res+
                        "/"+expectedValue+"]");
        }

        // should find /resource1
        URL u1 = loader.findResource ("com/foo/Resource1");
        if (u1 == null) {
            throw new RuntimeException ("can't find com/foo/Resource1 in test1.jar");
        }
        loader.close ();

        // should NOT find /resource2 even though it is in jar
        URL u2 = loader.findResource ("com/foo/Resource2");
        if (u2 != null) {
            throw new RuntimeException ("com/foo/Resource2 unexpected in test1.jar");
        }

        // load tests
        loadClass ("com.foo.TestClass1", loader, false);
        loadClass ("com.foo.TestClass", loader, true);
        loadClass ("java.sql.Array", loader, true);

        // now check we can delete the path
        rm_minus_rf (new File(name));
        System.out.println (" ... OK");
    }

    static HttpServer httpServer;

    static HttpServer getHttpServer() {
        return httpServer;
    }

    static URL getServerURL () throws Exception {
        int port = httpServer.getAddress().getPort();
        String s = "http://127.0.0.1:"+port+"/";
        return new URL(s);
    }

    static void startHttpServer (String docroot) throws Exception {
        httpServer = HttpServer.create (new InetSocketAddress(0), 10);
        HttpContext ctx = httpServer.createContext (
                "/", new FileServerHandler(docroot)
        );
        httpServer.start();
    }
}
