/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.tyrus.server;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.websocket.ClientEndpointConfiguration;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfiguration;
import javax.websocket.Extension;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.websocket.server.ServerApplicationConfiguration;
import javax.websocket.server.ServerEndpointConfiguration;

import org.glassfish.tyrus.AnnotatedEndpoint;
import org.glassfish.tyrus.ComponentProviderService;
import org.glassfish.tyrus.EndpointWrapper;
import org.glassfish.tyrus.ErrorCollector;
import org.glassfish.tyrus.ReflectionHelper;
import org.glassfish.tyrus.WithProperties;
import org.glassfish.tyrus.spi.SPIRegisteredEndpoint;
import org.glassfish.tyrus.spi.TyrusServer;

/**
 * Server Container Implementation.
 *
 * @author Martin Matula (martin.matula at oracle.com)
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 * @author Stepan Kopriva (stepan.kopriva at oracle.com)
 */
public class TyrusServerContainer extends WithProperties implements WebSocketContainer {
    private final TyrusServer server;
    private final String contextPath;
    private final ServerApplicationConfiguration configuration;
    private final Set<SPIRegisteredEndpoint> endpoints = new HashSet<SPIRegisteredEndpoint>();
    private final ErrorCollector collector;
    private final ComponentProviderService componentProvider;

    private long maxSessionIdleTimeout = 0;
    private long maxTextMessageBufferSize = 0;
    private long maxBinaryMessageBufferSize = 0;
    private long defaultAsyncSendTimeout = 0;

    public TyrusServerContainer(final TyrusServer server, final String contextPath,
                                final Set<Class<?>> classes) {
        this.collector = new ErrorCollector();
        this.server = server;
        this.contextPath = contextPath;
        this.configuration = new TyrusServerConfiguration(classes);
        componentProvider = ComponentProviderService.create(collector);
    }

    public void start() throws IOException, DeploymentException {
        // start the underlying server
        server.start();
        try {
            // deploy all the annotated endpoints
            for (Class<?> endpointClass : configuration.getAnnotatedEndpointClasses(null)) {
                AnnotatedEndpoint endpoint = AnnotatedEndpoint.fromClass(endpointClass, componentProvider, true, collector);
                EndpointConfiguration config = endpoint.getEndpointConfiguration();
                EndpointWrapper ew = new EndpointWrapper(endpoint, config, componentProvider, this, contextPath, collector);
                deploy(ew);
            }

            // deploy all the programmatic endpoints
            for (Class<? extends ServerEndpointConfiguration> endpointClass : configuration.getEndpointConfigurationClasses(null)) {
                ServerEndpointConfiguration seConfig = ReflectionHelper.getInstance(endpointClass, collector);
                EndpointWrapper ew = new EndpointWrapper(seConfig.getEndpointClass(), seConfig, componentProvider, this, contextPath, collector);
                deploy(ew);
            }
        } catch (DeploymentException de) {
            collector.addException(de);
        }

        if (!collector.isEmpty()) {
            this.stop();
            throw collector.composeComprehensiveException();
        }
    }

    private void deploy(EndpointWrapper wrapper) {
        SPIRegisteredEndpoint ge = server.register(wrapper);
        endpoints.add(ge);
    }

    public void stop() {
        for (SPIRegisteredEndpoint wsa : this.endpoints) {
            wsa.remove();
            this.server.unregister(wsa);
            Logger.getLogger(getClass().getName()).info("Closing down : " + wsa);
        }
        server.stop();
    }

    @Override
    public Session connectToServer(Class annotatedEndpointClass, URI path) throws DeploymentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Session connectToServer(Class<? extends Endpoint> endpointClass, ClientEndpointConfiguration cec, URI path) throws DeploymentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Session> getOpenSessions() {
        Set<Session> result = new HashSet<Session>();

        for (SPIRegisteredEndpoint endpoint : endpoints) {
            result.addAll(endpoint.getOpenSessions());
        }

        return Collections.unmodifiableSet(result);
    }

    @Override
    public long getMaxSessionIdleTimeout() {
        return maxSessionIdleTimeout;
    }

    @Override
    public void setMaxSessionIdleTimeout(long timeout) {
        this.maxSessionIdleTimeout = timeout;
    }

    @Override
    public long getMaxBinaryMessageBufferSize() {
        return maxBinaryMessageBufferSize;
    }

    @Override
    public void setMaxBinaryMessageBufferSize(long max) {
        this.maxBinaryMessageBufferSize = max;
    }

    @Override
    public long getMaxTextMessageBufferSize() {
        return maxTextMessageBufferSize;
    }

    @Override
    public void setMaxTextMessageBufferSize(long max) {
        this.maxTextMessageBufferSize = max;
    }

    @Override
    public Set<Extension> getInstalledExtensions() {
        // TODO
        // return Collections.unmodifiableSet(new HashSet<String>(configuration.parseExtensionsHeader()));

        return Collections.emptySet();
    }

    @Override
    public long getDefaultAsyncSendTimeout() {
        return defaultAsyncSendTimeout;
    }

    @Override
    public void setAsyncSendTimeout(long timeoutmillis) {
        defaultAsyncSendTimeout = timeoutmillis;
    }
}
