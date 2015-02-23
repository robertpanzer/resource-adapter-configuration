/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package foo.bar;


import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;

@RunWith(Arquillian.class)
public class RATest {

    @Deployment
    public static Archive<?> deploy() throws Exception {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "ratest.war");


        JavaArchive resourceAdapterJar = ShrinkWrap.create(JavaArchive.class, "resourceadapter.jar")
                .addClasses(TestActivationSpec.class,
                        TestResourceAdapter.class);

        ResourceAdapterArchive rar = ShrinkWrap.create(ResourceAdapterArchive.class, "test.rar")
                .addAsLibrary(resourceAdapterJar);

        JavaArchive resourceAdapterAPI = ShrinkWrap.create(JavaArchive.class, "rarutil.jar")
                .addClass(ITestListener.class);

        JavaArchive mdbJar = ShrinkWrap.create(JavaArchive.class, "mdb.jar")
                .addClasses(TestMDB.class);

        war.addAsLibraries(rar, mdbJar)
            .addAsLibraries(resourceAdapterAPI);

        war.addClass(RATest.class);

        return war;
    }

    @Test
    public void test() throws Exception {
        int expectedIterations = 10;
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 10000) {
            if (TestMDB.numberOfCalls.get() == expectedIterations) {
                return;
            }
        }
        Assert.fail("Only " + TestMDB.numberOfCalls.get() + " iterations");
    }
}
