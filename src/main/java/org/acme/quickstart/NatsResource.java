package org.acme.quickstart;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.nats.client.Connection;
import io.nats.client.Nats;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

/**
 * NatsResource
 */
@ApplicationScoped
public class NatsResource {

    private static final Logger LOG = LoggerFactory.getLogger(NatsResource.class);
    private Connection nc;

    @ConfigProperty(name = "nats.url", defaultValue = "nats://localhost:4222")
    String natsUrl;

    void onStart(@Observes StartupEvent evt) {
        try {
            nc = Nats.connect(natsUrl);
            LOG.info("connected to NATS: {}", natsUrl);
        } catch (IOException | InterruptedException e) {
            LOG.error("connecting to nats failed", e);
        }
    }

    void onShutdown(@Observes ShutdownEvent evt) {
        try {
            if (nc != null) {
                LOG.info("closing nats connection ...");
                nc.close();
            }
        } catch (InterruptedException e) {
            LOG.debug("closing nats connection failed", e);
        }
    }

    public void publish(String topic, byte[] data) {
        nc.publish(topic, data);
    }
}