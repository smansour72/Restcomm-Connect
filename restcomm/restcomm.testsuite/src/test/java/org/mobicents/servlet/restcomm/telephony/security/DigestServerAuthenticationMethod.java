package org.mobicents.servlet.restcomm.telephony.security;

/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2014, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Random;

import javax.sip.address.URI;
import javax.sip.header.AuthorizationHeader;
import javax.sip.message.Request;

/**
 * Implements the HTTP digest authentication method.
 * 
 * @author M. Ranganathan
 * @author Marc Bednarek
 */
public class DigestServerAuthenticationMethod implements AuthenticationMethod {

    public static final String DEFAULT_SCHEME = "Digest";

    public static final String DEFAULT_DOMAIN = "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + "";

    public static final String DEFAULT_ALGORITHM = "MD5";

    public final static String DEFAULT_REALM = "sip-servlets-realm";

    private MessageDigest messageDigest;

    /** to hex converter */
    private static final char[] toHex = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    /**
     * Default constructor.
     */
    public DigestServerAuthenticationMethod() {
        try {
            messageDigest = MessageDigest.getInstance(DEFAULT_ALGORITHM);
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("Algorithm not found " + ex);
            ex.printStackTrace();
        }
    }

    public static String toHexString(byte b[]) {
        int pos = 0;
        char[] c = new char[b.length * 2];
        for (int i = 0; i < b.length; i++) {
            c[pos++] = toHex[(b[i] >> 4) & 0x0F];
            c[pos++] = toHex[b[i] & 0x0f];
        }
        return new String(c);
    }

    /**
     * Initialize
     */
    public void initialize() {
        System.out.println("DEBUG, DigestAuthenticationMethod, initialize()," + " the realm is:" + DEFAULT_REALM);
    }

    /**
     * Get the authentication scheme
     * 
     * @return the scheme name
     */
    public String getScheme() {
        return DEFAULT_SCHEME;
    }

    /**
     * get the authentication realm
     * 
     * @return the realm name
     */
    public String getRealm(String resource) {
        return DEFAULT_REALM;
    }

    /**
     * get the authentication domain.
     * 
     * @return the domain name
     */
    public String getDomain() {
        return DEFAULT_DOMAIN;
    }

    /**
     * Get the authentication Algorithm
     * 
     * @return the alogirithm name (i.e. Digest).
     */
    public String getAlgorithm() {
        return DEFAULT_ALGORITHM;
    }

    /**
     * Generate the challenge string.
     * 
     * @return a generated nonce.
     */
    public String generateNonce() {
        // Get the time of day and run MD5 over it.
        Date date = new Date();
        long time = date.getTime();
        Random rand = new Random();
        long pad = rand.nextLong();
        String nonceString = (Long.valueOf(time)).toString() + (Long.valueOf(pad)).toString();
        byte mdbytes[] = messageDigest.digest(nonceString.getBytes());
        // Convert the mdbytes array into a hex string.
        return toHexString(mdbytes);
    }

    /**
     * Check the response and answer true if authentication succeeds. We are making simplifying assumptions here and assuming
     * that the password is available to us for computation of the MD5 hash. We also dont cache authentications so that the user
     * has to authenticate on each registration.
     * 
     * @param user is the username
     * @param authHeader is the Authroization header from the SIP request.
     */
    public boolean doAuthenticate(String user, String password, AuthorizationHeader authHeader, Request request) {
        String realm = authHeader.getRealm();
        String username = authHeader.getUsername();

        if (username == null) {
            System.out.println("DEBUG, DigestAuthenticateMethod, doAuthenticate(): "
                    + "WARNING: userName parameter not set in the header received!!!");
            username = user;
        }
        if (realm == null) {
            System.out.println("DEBUG, DigestAuthenticateMethod, doAuthenticate(): "
                    + "WARNING: realm parameter not set in the header received!!! WE use the default one");
            realm = DEFAULT_REALM;
        }

        System.out.println("DEBUG, DigestAuthenticateMethod, doAuthenticate(): " + "Trying to authenticate user: " + username
                + " for " + " the realm: " + realm);

        String nonce = authHeader.getNonce();
        // If there is a URI parameter in the Authorization header,
        // then use it.
        URI uri = authHeader.getURI();
        // There must be a URI parameter in the authorization header.
        if (uri == null) {
            System.out.println("DEBUG, DigestAuthenticateMethod, doAuthenticate(): "
                    + "ERROR: uri paramater not set in the header received!");
            return false;
        }
        System.out.println("DEBUG, DigestAuthenticationMethod, doAuthenticate(), username:" + username);
        System.out.println("DEBUG, DigestAuthenticationMethod, doAuthenticate(), realm:" + realm);
        System.out.println("DEBUG, DigestAuthenticationMethod, doAuthenticate(), password:" + password);
        System.out.println("DEBUG, DigestAuthenticationMethod, doAuthenticate(), uri:" + uri);
        System.out.println("DEBUG, DigestAuthenticationMethod, doAuthenticate(), nonce:" + nonce);
        System.out.println("DEBUG, DigestAuthenticationMethod, doAuthenticate(), method:" + request.getMethod());

        String A1 = username + ":" + realm + ":" + password;
        String A2 = request.getMethod().toUpperCase() + ":" + uri.toString();
        byte mdbytes[] = messageDigest.digest(A1.getBytes());
        String HA1 = toHexString(mdbytes);

        System.out.println("DEBUG, DigestAuthenticationMethod, doAuthenticate(), HA1:" + HA1);
        mdbytes = messageDigest.digest(A2.getBytes());
        String HA2 = toHexString(mdbytes);
        String KD = HA1 + ":" + nonce;
        System.out.println("DEBUG, DigestAuthenticationMethod, doAuthenticate(), HA2:" + HA2);
        String nonceCount = authHeader.getParameter("nc");
        String cnonce = authHeader.getCNonce();
        String qop = authHeader.getQop();
        if (cnonce != null && nonceCount != null && qop != null
                && (qop.equalsIgnoreCase("auth") || qop.equalsIgnoreCase("auth-int"))) {
            System.out.println("DEBUG, DigestAuthenticationMethod, doAuthenticate(), cnonce:" + cnonce);
            System.out.println("DEBUG, DigestAuthenticationMethod, doAuthenticate(), nonceCount:" + nonceCount);
            System.out.println("DEBUG, DigestAuthenticationMethod, doAuthenticate(), qop:" + qop);

            KD += ":" + nonceCount;
            KD += ":" + cnonce;
            KD += ":" + qop;
        }
        KD += ":" + HA2;
        mdbytes = messageDigest.digest(KD.getBytes());
        String mdString = toHexString(mdbytes);
        String response = authHeader.getResponse();
        System.out.println("DEBUG, DigestAuthenticateMethod, doAuthenticate(): " + "we have to compare his response: "
                + response + " with our computed" + " response: " + mdString);

        int res = (mdString.compareTo(response));
        if (res == 0) {
            System.out.println("DEBUG, DigestAuthenticateMethod, doAuthenticate(): " + "User authenticated...");
        } else {
            System.out.println("DEBUG, DigestAuthenticateMethod, doAuthenticate(): " + "User not authenticated...");
        }
        return res == 0;
    }

}
