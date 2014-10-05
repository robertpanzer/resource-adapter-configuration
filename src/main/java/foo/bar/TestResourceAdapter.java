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

import javax.resource.ResourceException;
import javax.resource.spi.*;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;
import javax.transaction.xa.XAResource;
import java.lang.reflect.Method;

@Connector
public class TestResourceAdapter implements ResourceAdapter {

    private BootstrapContext ctx;

    private int iterations = 1000;

    private Method listenerMethod;


    @Override
    public void start(BootstrapContext ctx) throws ResourceAdapterInternalException {

        this.ctx = ctx;
        try {
            listenerMethod = ITestListener.class.getMethod("process", Integer.TYPE);
        } catch (NoSuchMethodException e) {
            throw new ResourceAdapterInternalException(e);
        }

    }

    @Override
    public void stop() {

    }

    @Override
    public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) throws ResourceException {
        WorkManager workManager = ctx.getWorkManager();

        for (int i = 0; i < iterations; i++) {

            try {
                workManager.scheduleWork(new TestWork(i, endpointFactory), 100, null, null);
            } catch (WorkException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) {
    }

    @Override
    public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException {
        return new XAResource[0];
    }

    @SuppressWarnings("unused")
    public int getIterations() {
        return iterations;
    }


    @SuppressWarnings("unused")
    public void setIterations(int iterations) {
        System.out.println("iterations = " + iterations);
        this.iterations = iterations;
    }


    private class TestWork implements Work {

        private final int iteration;
        private MessageEndpointFactory endpointFactory;

        TestWork(int iteration, MessageEndpointFactory endpointFactory) {
            this.iteration = iteration;
            this.endpointFactory = endpointFactory;
        }

        @Override
        public void release() {
        }

        @Override
        public void run() {
            System.out.println("TestWork.run on thread " + Thread.currentThread());
            try {
                MessageEndpoint endpoint = endpointFactory.createEndpoint(null);
                endpoint.beforeDelivery(listenerMethod);
                listenerMethod.invoke(endpoint, iteration);
                endpoint.afterDelivery();
                endpoint.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
