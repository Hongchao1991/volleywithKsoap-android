package volleyksoap.j2se.ksoap2.transport;

import volleyksoap.base.ksoap2.transport.ServiceConnection;

import java.io.IOException;
import java.net.Proxy;

/**
 * HttpsTransportSE is a simple transport for https protocal based connections. It creates a #HttpsServiceConnectionSE
 * with the provided parameters.
 *
 * @author Manfred Moser <manfred@simpligility.com>
 */
public class HttpsTransportSE extends HttpTransportSE{

    static final String PROTOCOL = "https";
    private static final String PROTOCOL_FULL = PROTOCOL + "://";
    
    //connection instance, used for setting the SSLSocketFactory
    private HttpsServiceConnectionSE connection;

    protected final String host;
    protected final int port;
    protected final String file;

    public HttpsTransportSE (String host, int port, String file, int timeout) {
        super(HttpsTransportSE.PROTOCOL_FULL + host + ":" + port + file, timeout);
        this.host = host;
        this.port = port;
        this.file = file;
    }

    /**
     * Creates instance of HttpTransportSE with set url and defines a
     * proxy server to use to access it
     *
     * @param proxy
     * Proxy information or <code>null</code> for direct access
     */
    public HttpsTransportSE(Proxy proxy, String host, int port, String file, int timeout) {
        super(proxy, HttpsTransportSE.PROTOCOL_FULL + host + ":" + port + file);
        this.host = host;
        this.port = port;
        this.file = file;
        this.timeout = timeout;
    }

    /**
     * Returns the HttpsServiceConnectionSE and creates it if necessary
     * @see org.ksoap2.transport.HttpsTransportSE#getServiceConnection()
     */
    public ServiceConnection getServiceConnection() throws IOException
    {
        if(connection != null) {
            return connection;
        } else {
            connection = new HttpsServiceConnectionSE(proxy, host, port, file, timeout);
            return connection;
        }
    }
}
