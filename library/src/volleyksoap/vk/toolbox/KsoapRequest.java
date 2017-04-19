package volleyksoap.vk.toolbox;

import volleyksoap.base.ksoap2.SoapFault;
import volleyksoap.base.ksoap2.serialization.SoapSerializationEnvelope;
import volleyksoap.j2se.ksoap2.transport.HttpResponseException;
import volleyksoap.vk.AuthFailureError;
import volleyksoap.vk.NetworkError;
import volleyksoap.vk.NetworkResponse;
import volleyksoap.vk.ParseError;
import volleyksoap.vk.Request;
import volleyksoap.vk.Response;


import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;


/**
 * Created by Administrator on 2017/4/19/019.
 */public class KsoapRequest extends Request<SoapSerializationEnvelope> {

    private final Response.Listener<SoapSerializationEnvelope> mListener;

    SoapSerializationEnvelope mEnv;

    String mSoapAction;

    VolleyKsoapTransport mVolleyKsoapTransport;

    public KsoapRequest(String soapAction, SoapSerializationEnvelope soap_env, String url, Response.Listener<SoapSerializationEnvelope> listener,
                        Response.ErrorListener errorListener) {
        super(Method.POST, url, errorListener);

        mListener = listener;

        mEnv = soap_env;

        mSoapAction = soapAction;

        if (mSoapAction == null) {
            mSoapAction = "\"\"";
        }

        mVolleyKsoapTransport = new VolleyKsoapTransport();
    }

    @Override
    protected void deliverResponse(SoapSerializationEnvelope response) {
        mListener.onResponse(response);
    }

    @Override
    public String getPostBodyContentType() {
        return getBodyContentType();
    }

    /**
     * @deprecated Use {@link #getBody()}.
     */
    @Override
    public byte[] getPostBody() {
        return getBody();
    }

    @Override
    public String getBodyContentType() {
        if (mEnv.version == SoapSerializationEnvelope.VER12) {
            return VolleyKsoapTransport.CONTENT_TYPE_SOAP_XML_CHARSET_UTF_8;
        } else {
            return VolleyKsoapTransport.CONTENT_TYPE_XML_CHARSET_UTF_8;
        }
    }

    @Override
    public byte[] getBody() {

        byte[] requestData;

        try {
            requestData = mVolleyKsoapTransport.createRequestData(mEnv, "UTF-8");

            mVolleyKsoapTransport.requestDump = mVolleyKsoapTransport.debug ? new String(requestData) : null;
        }
        catch (IOException e) {
            requestData = null;
            mVolleyKsoapTransport.requestDump = null;
        }

        return requestData;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError
    {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("User-Agent", VolleyKsoapTransport.USER_AGENT);

        if (mEnv.version != SoapSerializationEnvelope.VER12) {
            headers.put("SOAPAction", mSoapAction);
        }

        /*if (mEnv.version == SoapSerializationEnvelope.VER12) {
            headers.put(("Content-Type", VolleyKsoapTransport.CONTENT_TYPE_SOAP_XML_CHARSET_UTF_8);
        } else {
            headers.put(("Content-Type", VolleyKsoapTransport.CONTENT_TYPE_XML_CHARSET_UTF_8);
        }*/

        headers.put("Accept-Encoding", "gzip");

        return headers;
    }

    private InputStream getUnZippedInputStream(InputStream inputStream) throws IOException {
        /* workaround for Android 2.3
           (see http://stackoverflow.com/questions/5131016/)
        */
        try {
            return (GZIPInputStream) inputStream;
        } catch (ClassCastException e) {
            return new GZIPInputStream(inputStream);
        }
    }

    @Override
    protected Response<SoapSerializationEnvelope> parseNetworkResponse(NetworkResponse response) {

        InputStream is1 = null;

        try {
            if( response.statusCode != 200 && response.statusCode != 202) {
                //202 is a correct status returned by WCF OneWay operation

                String strTmp = "HTTP request failed, HTTP status: " + (response.statusCode);

                throw new HttpResponseException(strTmp, response.statusCode);
            }

            String myStr;
            int contentLength = 8192;
            boolean xmlContent = false;
            boolean gZippedContent = false;

            myStr = response.headers.get("Content-Length");
            if ( myStr != null ) {
                try {
                    contentLength = Integer.parseInt(myStr);
                } catch ( NumberFormatException nfe ) {
                    contentLength = 8192;
                }
            }

            myStr = response.headers.get("Content-Type");
            if ( myStr != null )
            {
                if(myStr.contains("xml"))
                {
                    xmlContent = true;
                }
            }

            myStr = response.headers.get("Content-Encoding");
            if ( myStr != null )
            {
                if(myStr.equalsIgnoreCase("gzip"))
                {
                    gZippedContent = true;
                }
            }

            InputStream is_new = new ByteArrayInputStream(response.data);

            if (contentLength > 0) {
                if (gZippedContent) {
                    is1 = getUnZippedInputStream(is_new);
                } else {
                    is1 = new BufferedInputStream(is_new,contentLength);
                }
            }
        }

        catch ( HttpResponseException e1)
        {
            return Response.error(new NetworkError(e1));
        }

        catch (IOException e2) {
            return Response.error(new ParseError(e2));
        }

        if( is1 != null)
        {
            /*Winfred Young add it fr soapfualt*/

            mEnv.bIsParsedSoapFault = false;

            try {
                mVolleyKsoapTransport.parseResponse(mEnv, is1);
            }

            catch (IOException e3) {
                return Response.error(new ParseError(e3));
            }

            catch (XmlPullParserException e4)
            {
                return Response.error(new ParseError(e4));
            }

            if(mEnv.bIsParsedSoapFault)
            {
                SoapFault fault = (SoapFault)(mEnv.bodyIn);

                return Response.error(new ParseError(fault));
            }
        }

        return Response.success(mEnv,HttpHeaderParser.parseCacheHeaders(response));
    }
}