/*
 * Copyright (c) 2013, 2014, Oracle and/or its affiliates. All rights reserved.
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

/*
 * @test
 * @bug      8011288
 * @summary  Erratic/inconsistent indentation of signatures
 * @library  ../lib/
 * @build    JavadocTester
 * @run main TestIndentation
 */

public class TestIndentation extends JavadocTester {

    //Test information.
    private static final String BUG_ID = "8011288";

    //Javadoc arguments.
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-sourcepath", SRC_DIR, "p"
    };

    //Input for string search tests.
    private static final String[][] TEST = {
        { BUG_ID + "/p/Indent.html",
          "<pre>public&nbsp;&lt;T&gt;&nbsp;void&nbsp;m(T&nbsp;t1," },
        { BUG_ID + "/p/Indent.html",
          "\n" +
          "                  T&nbsp;t2)" },
        { BUG_ID + "/p/Indent.html",
          "\n" +
          "           throws java.lang.Exception" }
    };

    /**
     * The entry point of the test.
     * @param args the array of command line arguments.
     */
    public static void main(String[] args) {
        TestIndentation tester = new TestIndentation();
        tester.run(ARGS, TEST, NO_TEST);
        tester.printSummary();
    }

    /**
     * {@inheritDoc}
     */
    public String getBugId() {
        return BUG_ID;
    }

    /**
     * {@inheritDoc}
     */
    public String getBugName() {
        return getClass().getName();
    }
}
