/** 
 * Copyright (c) 2006, James Seigel, Calgary, AB., Canada
 * Copyright (c) 2003,2004, Stefan Haustein, Oberhausen, Rhld., Germany
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The  above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE. 
 */

package volleyksoap.vk.toolbox;

import volleyksoap.base.ksoap2.SoapEnvelope;

import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Abstract class which holds common methods and members that are used by the
 * transport layers. This class encapsulates the serialization and
 * deserialization of the soap messages, leaving the basic communication
 * routines to the subclasses.
 */
public class VolleyKsoapTransport {

    /**
     * Added to enable web service interactions on the emulator to be debugged
     * with Fiddler2 (Windows) but provides utility for other proxy
     * requirements.
     */

    public static final int DEFAULT_BUFFER_SIZE = 256*1024; // 256 Kb

    /** Set to true if debugging */
    public boolean debug  =  false;
    /** String dump of request for debugging. */
    public String requestDump;
    /** String dump of response for debugging */
    public String responseDump;
    private String xmlVersionTag = "";

    public static final String CONTENT_TYPE_XML_CHARSET_UTF_8 = "text/xml;charset=utf-8";
    public static final String CONTENT_TYPE_SOAP_XML_CHARSET_UTF_8 = "application/soap+xml;charset=utf-8";
    public static final String USER_AGENT = "ksoap2-android/2.6.0+";

    private int bufferLength = DEFAULT_BUFFER_SIZE;

    private HashMap prefixes = new HashMap();

    public HashMap getPrefixes() {
        return prefixes;
    }

    public VolleyKsoapTransport() {
    }

    public VolleyKsoapTransport(int bufferLength) {
        this.bufferLength = bufferLength;
    }

    /**
     * Sets up the parsing to hand over to the envelope to deserialize.
     */
    protected void parseResponse(SoapEnvelope envelope, InputStream is)
            throws XmlPullParserException, IOException {
        XmlPullParser xp = new KXmlParser();
        xp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        xp.setInput(is, null);
        envelope.parse(xp);
        /*
         * Fix memory leak when running on android in strict mode. Issue 133
         */
        is.close();
    }

    /**
     * Serializes the request.
     */
    protected byte[] createRequestData(SoapEnvelope envelope, String encoding)
            throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(bufferLength);
        byte result[] = null;
        bos.write(xmlVersionTag.getBytes());
        XmlSerializer xw = new KXmlSerializer();

        final Iterator keysIter = prefixes.keySet().iterator();

        xw.setOutput(bos, encoding);
        while (keysIter.hasNext()) {
            String key = (String) keysIter.next();
            xw.setPrefix(key, (String) prefixes.get(key));
        }
        envelope.write(xw);
        xw.flush();
        bos.write('\r');
        bos.write('\n');
        bos.flush();
        result = bos.toByteArray();
        xw = null;
        bos = null;
        return result;
    }

    /**
     * Serializes the request.
     */
    protected byte[] createRequestData(SoapEnvelope envelope)
            throws IOException {
        return createRequestData(envelope, null);
    }


    /**
     * Sets the version tag for the outgoing soap call. Example <?xml
     * version=\"1.0\" encoding=\"UTF-8\"?>
     *
     * @param tag
     *            the xml string to set at the top of the soap message.
     */
    public void setXmlVersionTag(String tag) {
        xmlVersionTag = tag;
    }

}