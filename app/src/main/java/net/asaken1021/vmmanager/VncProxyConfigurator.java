package net.asaken1021.vmmanager;

import org.eclipse.jetty.websocket.jakarta.common.ServerEndpointConfigWrapper;

public class VncProxyConfigurator extends ServerEndpointConfigWrapper.Configurator {
    private final String uri;

    public VncProxyConfigurator(String uri) {
        this.uri = uri;
    }

    @SuppressWarnings("unchecked")
	@Override
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        if (VncProxyEndpoint.class.equals(endpointClass)) {
            return (T) new VncProxyEndpoint(this.uri);
        } else {
            return super.getEndpointInstance(endpointClass);
        }
    }
}
