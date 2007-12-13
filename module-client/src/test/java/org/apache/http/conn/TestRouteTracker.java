/*
 * $HeadURL$
 * $Revision$
 * $Date$
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.http.conn;


import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.http.HttpHost;


/**
 * Tests for <code>RouteTracker</code>.
 */
public class TestRouteTracker extends TestCase {

    // a selection of constants for generating routes
    public final static
        HttpHost TARGET1 = new HttpHost("target1.test.invalid");
    public final static
        HttpHost TARGET2 = new HttpHost("target2.test.invalid", 8080);
    // It is not necessary to have extra targets for https.
    // The 'layered' and 'secure' flags are specified explicitly
    // for routes, they will not be determined from the scheme.

    public final static
        HttpHost PROXY1 = new HttpHost("proxy1.test.invalid");
    public final static
        HttpHost PROXY2 = new HttpHost("proxy2.test.invalid", 1080);
    public final static
        HttpHost PROXY3 = new HttpHost("proxy3.test.invalid", 88);

    public final static InetAddress LOCAL41;
    public final static InetAddress LOCAL42;
    public final static InetAddress LOCAL61;
    public final static InetAddress LOCAL62;

    // need static initializer to deal with exceptions
    static {
        try {
            LOCAL41 = InetAddress.getByAddress(new byte[]{ 127, 0, 0, 1 });
            LOCAL42 = InetAddress.getByAddress(new byte[]{ 127, 0, 0, 2 });

            LOCAL61 = InetAddress.getByAddress(new byte[]{
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1
            });
            LOCAL62 = InetAddress.getByAddress(new byte[]{
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2
            });

        } catch (Exception x) {
            throw new ExceptionInInitializerError(x);
        }
    }


    public TestRouteTracker(String testName) {
        super(testName);
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestRouteTracker.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    public static Test suite() {
        return new TestSuite(TestRouteTracker.class);
    }


    public void testCstrTargetLocal() {

        RouteTracker rt = new RouteTracker(TARGET1, null);
        assertEquals("wrong target (target,null)",
                     TARGET1, rt.getTargetHost());
        assertEquals("wrong local address (target,null)",
                     null, rt.getLocalAddress());
        assertEquals("wrong hop count (target,null)",
                     0, rt.getHopCount());
        assertEquals("wrong proxy (target,null)",
                     null, rt.getProxyHost());
        assertEquals("wrong route (target,null)",
                     null, rt.toRoute());
        checkCTLS(rt, false, false, false, false);


        rt = new RouteTracker(TARGET2, LOCAL61);
        assertEquals("wrong target (target,local)",
                     TARGET2, rt.getTargetHost());
        assertEquals("wrong local address (target,local)",
                     LOCAL61, rt.getLocalAddress());
        assertEquals("wrong hop count (target,local)",
                     0, rt.getHopCount());
        assertEquals("wrong proxy (target,local)",
                     null, rt.getProxyHost());
        assertEquals("wrong route (target,local)",
                     null, rt.toRoute());
        checkCTLS(rt, false, false, false, false);


        rt = null;
        try {
            rt = new RouteTracker(null, LOCAL41);
            fail("null target not detected");
        } catch (IllegalArgumentException iax) {
            // expected
        }
    }


    public void testCstrRoute() {

        HttpRoute    r  = new HttpRoute(TARGET1);
        RouteTracker rt = new RouteTracker(r);
        assertEquals("wrong target (r1)",
                     TARGET1, rt.getTargetHost());
        assertEquals("wrong local address (r1)",
                     null, rt.getLocalAddress());
        assertEquals("wrong hop count (r1)",
                     0, rt.getHopCount());
        assertEquals("wrong proxy (r1)",
                     null, rt.getProxyHost());
        assertEquals("wrong route (r1)",
                     null, rt.toRoute());
        checkCTLS(rt, false, false, false, false);

        r  = new HttpRoute(TARGET2, LOCAL61, true);
        rt = new RouteTracker(r);
        assertEquals("wrong target (r2)",
                     TARGET2, rt.getTargetHost());
        assertEquals("wrong local address (r2)",
                     LOCAL61, rt.getLocalAddress());
        assertEquals("wrong hop count (r2)",
                     0, rt.getHopCount());
        assertEquals("wrong proxy (r2)",
                     null, rt.getProxyHost());
        assertEquals("wrong route (r2)",
                     null, rt.toRoute());
        checkCTLS(rt, false, false, false, false);


        r  = new HttpRoute(TARGET1, LOCAL42, PROXY3, true);
        rt = new RouteTracker(r);
        assertEquals("wrong target (r3)",
                     TARGET1, rt.getTargetHost());
        assertEquals("wrong local address (r3)",
                     LOCAL42, rt.getLocalAddress());
        assertEquals("wrong hop count (r3)",
                     0, rt.getHopCount());
        assertEquals("wrong proxy (r3)",
                     null, rt.getProxyHost());
        assertEquals("wrong route (r3)",
                     null, rt.toRoute());
        checkCTLS(rt, false, false, false, false);


        rt = null;
        try {
            rt = new RouteTracker(null);
            fail("null route not detected");
        } catch (NullPointerException npx) {
            // expected
        }
    }


    public void testIllegalArgs() {

        RouteTracker rt = new RouteTracker(TARGET2, null);

        try {
            rt.connectProxy(null, true);
            fail("missing proxy argument not detected (connect/false)");
        } catch (IllegalArgumentException iax) {
            // expected
        }

        try {
            rt.connectProxy(null, false);
            fail("missing proxy argument not detected (connect/true)");
        } catch (IllegalArgumentException iax) {
            // expected
        }

        rt.connectProxy(PROXY1, false);

        try {
            rt.tunnelProxy(null, false);
            fail("missing proxy argument not detected (tunnel/false)");
        } catch (IllegalArgumentException iax) {
            // expected
        }

        try {
            rt.tunnelProxy(null, true);
            fail("missing proxy argument not detected (tunnel/true)");
        } catch (IllegalArgumentException iax) {
            // expected
        }

        try {
            rt.getHopTarget(-1);
            fail("negative hop index not detected");
        } catch (IllegalArgumentException iax) {
            // expected
        }

        try {
            rt.getHopTarget(2);
            fail("excessive hop index not detected");
        } catch (IllegalArgumentException iax) {
            // expected
        }
    }


    public void testIllegalStates() {

        RouteTracker rt = new RouteTracker(TARGET1, null);

        try {
            rt.tunnelTarget(false);
            fail("unconnectedness not detected (tunnelTarget)");
        } catch (IllegalStateException isx) {
            // expected
        }

        try {
            rt.tunnelProxy(PROXY1, false);
            fail("unconnectedness not detected (tunnelProxy)");
        } catch (IllegalStateException isx) {
            // expected
        }

        try {
            rt.layerProtocol(true);
            fail("unconnectedness not detected (layerProtocol)");
        } catch (IllegalStateException isx) {
            // expected
        }


        // connect directly
        rt.connectTarget(false);

        try {
            rt.connectTarget(false);
            fail("connectedness not detected (connectTarget)");
        } catch (IllegalStateException isx) {
            // expected
        }

        try {
            rt.connectProxy(PROXY2, false);
            fail("connectedness not detected (connectProxy)");
        } catch (IllegalStateException isx) {
            // expected
        }

        try {
            rt.tunnelTarget(false);
            fail("unproxiedness not detected (tunnelTarget)");
        } catch (IllegalStateException isx) {
            // expected
        }

        try {
            rt.tunnelProxy(PROXY1, false);
            fail("unproxiedness not detected (tunnelProxy)");
        } catch (IllegalStateException isx) {
            // expected
        }
    }


    public void testDirectRoutes() {

        final HttpRouteDirector rd = new BasicRouteDirector();
        HttpRoute r = new HttpRoute(TARGET1, LOCAL41, false);
        RouteTracker rt = new RouteTracker(r);
        boolean complete = checkVia(rt, r, rd, 2);
        assertTrue("incomplete route 1", complete);

        r = new HttpRoute(TARGET2, LOCAL62, true);
        rt = new RouteTracker(r);
        complete = checkVia(rt, r, rd, 2);
        assertTrue("incomplete route 2", complete);
    }


    public void testProxyRoutes() {

        final HttpRouteDirector rd = new BasicRouteDirector();
        HttpRoute r = new HttpRoute(TARGET2, null, PROXY1, false);
        RouteTracker rt = new RouteTracker(r);
        boolean complete = checkVia(rt, r, rd, 2);
        assertTrue("incomplete route 1", complete);

        // tunnelled, but neither secure nor layered
        r = new HttpRoute(TARGET1, LOCAL61, PROXY3, false, true, false);
        rt = new RouteTracker(r);
        complete = checkVia(rt, r, rd, 3);
        assertTrue("incomplete route 2", complete);

        // tunnelled, layered, but not secure
        r = new HttpRoute(TARGET1, LOCAL61, PROXY3, false, true, true);
        rt = new RouteTracker(r);
        complete = checkVia(rt, r, rd, 4);
        assertTrue("incomplete route 3", complete);

        // tunnelled, layered, secure
        r = new HttpRoute(TARGET1, LOCAL61, PROXY3, true);
        rt = new RouteTracker(r);
        complete = checkVia(rt, r, rd, 4);
        assertTrue("incomplete route 4", complete);
    }


    public void testProxyChainRoutes() {

        final HttpRouteDirector rd = new BasicRouteDirector();
        HttpHost[] proxies = { PROXY1, PROXY2 };
        HttpRoute r = new HttpRoute(TARGET2, LOCAL42, proxies,
                                    false, false, false);
        RouteTracker rt = new RouteTracker(r);
        boolean complete = checkVia(rt, r, rd, 3);
        assertTrue("incomplete route 1", complete);

        // tunnelled, but neither secure nor layered
        proxies = new HttpHost[]{ PROXY3, PROXY2 };
        r = new HttpRoute(TARGET1, null, proxies, false, true, false);
        rt = new RouteTracker(r);
        complete = checkVia(rt, r, rd, 4);
        assertTrue("incomplete route 2", complete);

        // tunnelled, layered, but not secure
        proxies = new HttpHost[]{ PROXY3, PROXY2, PROXY1 };
        r = new HttpRoute(TARGET2, LOCAL61, proxies, false, true, true);
        rt = new RouteTracker(r);
        complete = checkVia(rt, r, rd, 6);
        assertTrue("incomplete route 3", complete);

        // tunnelled, layered, secure
        proxies = new HttpHost[]{ PROXY1, PROXY3 };
        r = new HttpRoute(TARGET1, LOCAL61, proxies, true, true, true);
        rt = new RouteTracker(r);
        complete = checkVia(rt, r, rd, 5);
        assertTrue("incomplete route 4", complete);
    }


    public void testEqualsHashcodeCloneToString()
        throws CloneNotSupportedException {

        RouteTracker rt0 = new RouteTracker(TARGET1, null);
        RouteTracker rt4 = new RouteTracker(TARGET1, LOCAL41);
        RouteTracker rt6 = new RouteTracker(TARGET1, LOCAL62);

        assertFalse("rt0", rt0.equals(null));
        assertTrue("rt0", rt0.equals(rt0));
        assertFalse("rt0", rt0.equals(rt0.toString()));

        assertFalse("rt0 == rt4", rt0.equals(rt4));
        assertFalse("rt4 == rt0", rt4.equals(rt0));
        assertFalse("rt0 == rt6", rt0.equals(rt6));
        assertFalse("rt6 == rt0", rt6.equals(rt0));
        assertFalse("rt4 == rt6", rt4.equals(rt6));
        assertFalse("rt6 == rt4", rt6.equals(rt4));

        // it is likely but not guaranteed that the hashcodes are different
        assertFalse("rt0 == rt4 (hashcode)", rt0.hashCode() == rt4.hashCode());
        assertFalse("rt0 == rt6 (hashcode)", rt0.hashCode() == rt6.hashCode());
        assertFalse("rt6 == rt4 (hashcode)", rt6.hashCode() == rt4.hashCode());

        assertEquals("rt0 (clone)", rt0, rt0.clone());
        assertEquals("rt4 (clone)", rt4, rt4.clone());
        assertEquals("rt6 (clone)", rt6, rt6.clone());


        // we collect (clones of) the different tracked routes along the way
        // rt0 -> direct connection
        // rt1 -> via single proxy
        // rt2 -> via proxy chain
        Set<RouteTracker> hs = new HashSet<RouteTracker>();

        // we also collect hashcodes for the different paths
        // since we can't guarantee what influence the HttpHost hashcodes have,
        // we keep separate sets here
        Set<Integer> hc0 = new HashSet<Integer>();
        Set<Integer> hc4 = new HashSet<Integer>();
        Set<Integer> hc6 = new HashSet<Integer>();

        RouteTracker rt = null;

        assertTrue(hs.add(rt0));
        assertTrue(hs.add(rt4));
        assertTrue(hs.add(rt6));

        assertTrue(hc0.add(new Integer(rt0.hashCode())));
        assertTrue(hc4.add(new Integer(rt4.hashCode())));
        assertTrue(hc6.add(new Integer(rt6.hashCode())));

        rt = (RouteTracker) rt0.clone();
        rt.connectTarget(false);
        assertTrue(hs.add(rt));
        assertTrue(hc0.add(new Integer(rt.hashCode())));

        rt = (RouteTracker) rt0.clone();
        rt.connectTarget(true);
        assertTrue(hs.add(rt));
        assertTrue(hc0.add(new Integer(rt.hashCode())));


        // proxy (insecure) -> tunnel (insecure) -> layer (secure)
        rt = (RouteTracker) rt4.clone();
        rt.connectProxy(PROXY1, false);
        assertTrue(hs.add((RouteTracker) rt.clone()));
        // this is not guaranteed to be unique...
        assertTrue(hc4.add(new Integer(rt.hashCode())));

        rt.tunnelTarget(false);
        assertTrue(hs.add((RouteTracker) rt.clone()));
        assertTrue(hc4.add(new Integer(rt.hashCode())));

        rt.layerProtocol(true);
        assertTrue(hs.add((RouteTracker) rt.clone()));
        assertTrue(hc4.add(new Integer(rt.hashCode())));


        // proxy (secure) -> tunnel (secure) -> layer (insecure)
        rt = (RouteTracker) rt4.clone();
        rt.connectProxy(PROXY1, true);
        assertTrue(hs.add((RouteTracker) rt.clone()));
        // this is not guaranteed to be unique...
        assertTrue(hc4.add(new Integer(rt.hashCode())));

        rt.tunnelTarget(true);
        assertTrue(hs.add((RouteTracker) rt.clone()));
        assertTrue(hc4.add(new Integer(rt.hashCode())));

        rt.layerProtocol(false);
        assertTrue(hs.add((RouteTracker) rt.clone()));
        assertTrue(hc4.add(new Integer(rt.hashCode())));


        // PROXY1/i -> PROXY2/i -> tunnel/i -> layer/s
        rt = (RouteTracker) rt6.clone();
        rt.connectProxy(PROXY1, false);
        assertTrue(hs.add((RouteTracker) rt.clone()));
        // this is not guaranteed to be unique...
        assertTrue(hc6.add(new Integer(rt.hashCode())));

        rt.tunnelProxy(PROXY2, false);
        assertTrue(hs.add((RouteTracker) rt.clone()));
        // this is not guaranteed to be unique...
        assertTrue(hc6.add(new Integer(rt.hashCode())));

        rt.tunnelTarget(false);
        assertTrue(hs.add((RouteTracker) rt.clone()));
        assertTrue(hc6.add(new Integer(rt.hashCode())));

        rt.layerProtocol(true);
        assertTrue(hs.add((RouteTracker) rt.clone()));
        assertTrue(hc6.add(new Integer(rt.hashCode())));


        // PROXY1/s -> PROXY2/s -> tunnel/s -> layer/i
        rt = (RouteTracker) rt6.clone();
        rt.connectProxy(PROXY1, true);
        assertTrue(hs.add((RouteTracker) rt.clone()));
        // this is not guaranteed to be unique...
        assertTrue(hc6.add(new Integer(rt.hashCode())));

        rt.tunnelProxy(PROXY2, true);
        assertTrue(hs.add((RouteTracker) rt.clone()));
        // this is not guaranteed to be unique...
        assertTrue(hc6.add(new Integer(rt.hashCode())));

        rt.tunnelTarget(true);
        assertTrue(hs.add((RouteTracker) rt.clone()));
        assertTrue(hc6.add(new Integer(rt.hashCode())));

        rt.layerProtocol(false);
        assertTrue(hs.add((RouteTracker) rt.clone()));
        assertTrue(hc6.add(new Integer(rt.hashCode())));


        // PROXY2/i -> PROXY1/i -> tunnel/i -> layer/s
        rt = (RouteTracker) rt6.clone();
        rt.connectProxy(PROXY2, false);
        assertTrue(hs.add((RouteTracker) rt.clone()));
        // this is not guaranteed to be unique...
        assertTrue(hc6.add(new Integer(rt.hashCode())));

        rt.tunnelProxy(PROXY1, false);
        assertTrue(hs.add((RouteTracker) rt.clone()));
        // proxy chain sequence does not affect hashcode, so duplicate:
        // assertTrue(hc6.add(new Integer(rt.hashCode())));

        rt.tunnelTarget(false);
        assertTrue(hs.add((RouteTracker) rt.clone()));
        // proxy chain sequence does not affect hashcode, so duplicate:
        // assertTrue(hc6.add(new Integer(rt.hashCode())));

        rt.layerProtocol(true);
        assertTrue(hs.add((RouteTracker) rt.clone()));
        // proxy chain sequence does not affect hashcode, so duplicate:
        // assertTrue(hc6.add(new Integer(rt.hashCode())));


        // check that all toString are OK and different
        Set<String> rtstrings = new HashSet<String>();
        for (RouteTracker current: hs) {
            final String rts = checkToString(current);
            assertTrue("duplicate toString: " + rts, rtstrings.add(rts));
        }
    }


    /** Helper to check the status of the four flags. */
    public final static void checkCTLS(RouteTracker rt,
                                       boolean c, boolean t,
                                       boolean l, boolean s) {
        String rts = rt.toString();
        assertEquals("wrong flag connected: " + rts, c, rt.isConnected());
        assertEquals("wrong flag tunnelled: " + rts, t, rt.isTunnelled());
        assertEquals("wrong flag layered: "   + rts, l, rt.isLayered());
        assertEquals("wrong flag secure: "    + rts, s, rt.isSecure());
    }


    /**
     * Helper to check tracking of a route.
     * This uses a {@link HttpRouteDirector} to fake establishing the route,
     * checking the intermediate steps.
     *
     * @param rt        the tracker to check with
     * @param r         the route to establish
     * @param rd        the director to check with
     * @param steps     the step count for this invocation
     *
     * @return  <code>true</code> iff the route is complete
     */
    public final static boolean checkVia(RouteTracker rt, HttpRoute r,
                                         HttpRouteDirector rd, int steps) {

        final String msg = r.toString() + " @ " + rt.toString();

        boolean complete = false;
        while (!complete && (steps > 0)) {

            int action = rd.nextStep(r, rt.toRoute());
            switch (action) {

            case HttpRouteDirector.COMPLETE:
                complete = true;
                assertEquals(r, rt.toRoute());
                break;

            case HttpRouteDirector.CONNECT_TARGET: {
                final boolean sec = r.isSecure();
                rt.connectTarget(sec);
                checkCTLS(rt, true, false, false, sec);
                assertEquals("wrong hop count "+msg,
                             1, rt.getHopCount());
                assertEquals("wrong hop0 "+msg,
                             r.getTargetHost(), rt.getHopTarget(0));
            } break;

            case HttpRouteDirector.CONNECT_PROXY: {
                // we assume an insecure proxy connection
                final boolean sec = false;
                rt.connectProxy(r.getProxyHost(), sec);
                checkCTLS(rt, true, false, false, sec);
                assertEquals("wrong hop count "+msg,
                             2, rt.getHopCount());
                assertEquals("wrong hop0 "+msg,
                             r.getProxyHost(), rt.getHopTarget(0));
                assertEquals("wrong hop1 "+msg,
                             r.getTargetHost(), rt.getHopTarget(1));
            } break;

            case HttpRouteDirector.TUNNEL_TARGET: {
                final int hops = rt.getHopCount();
                // we assume an insecure tunnel
                final boolean sec = false;
                rt.tunnelTarget(sec);
                checkCTLS(rt, true, true, false, sec);
                assertEquals("wrong hop count "+msg,
                             hops, rt.getHopCount());
                assertEquals("wrong hop0 "+msg,
                             r.getProxyHost(), rt.getHopTarget(0));
                assertEquals("wrong hopN "+msg,
                             r.getTargetHost(), rt.getHopTarget(hops-1));
            } break;

            case HttpRouteDirector.TUNNEL_PROXY: {
                final int hops = rt.getHopCount(); // before tunnelling
                // we assume an insecure tunnel
                final boolean  sec = false;
                final HttpHost pxy = r.getHopTarget(hops-1);
                rt.tunnelProxy(pxy, sec);
                // Since we're tunnelling to a proxy and not the target,
                // the 'tunelling' flag is false: no end-to-end tunnel.
                checkCTLS(rt, true, false, false, sec);
                assertEquals("wrong hop count "+msg,
                             hops+1, rt.getHopCount());
                assertEquals("wrong hop0 "+msg,
                             r.getProxyHost(), rt.getHopTarget(0));
                assertEquals("wrong hop"+hops+" "+msg,
                             pxy, rt.getHopTarget(hops-1));
                assertEquals("wrong hopN "+msg,
                             r.getTargetHost(), rt.getHopTarget(hops));
            } break;

            case HttpRouteDirector.LAYER_PROTOCOL: {
                final int    hops = rt.getHopCount();
                final boolean tun = rt.isTunnelled();
                final boolean sec = r.isSecure();
                rt.layerProtocol(sec);
                checkCTLS(rt, true, tun, true, sec);
                assertEquals("wrong hop count "+msg,
                             hops, rt.getHopCount());
                assertEquals("wrong proxy "+msg,
                             r.getProxyHost(), rt.getProxyHost());
                assertEquals("wrong target "+msg,
                             r.getTargetHost(), rt.getTargetHost());
            } break;


            // UNREACHABLE
            default:
                fail("unexpected action " + action + " from director, "+msg);
                break;

            } // switch
            steps--;
        }

        return complete;
    } // checkVia


    /**
     * Checks the output of <code>toString</code>.
     *
     * @param rt        the tracker for which to check the output
     *
     * @return  the result of <code>rt.toString()</code>
     */
    public final static String checkToString(RouteTracker rt) {
        if (rt == null)
            return null;

        final String rts = rt.toString();

        if (rt.getLocalAddress() != null) {
            final String las = rt.getLocalAddress().toString();
            assertFalse("no local address in toString(): " + rts,
                        rts.indexOf(las) < 0);
        }

        for (int i=0; i<rt.getHopCount(); i++) {
            final String hts = rt.getHopTarget(i).toString();
            assertFalse("hop "+i+" ("+hts+") missing in toString(): " + rts,
                        rts.indexOf(hts) < 0);
        }

        return rts;
    }


} // class TestRouteTracker
