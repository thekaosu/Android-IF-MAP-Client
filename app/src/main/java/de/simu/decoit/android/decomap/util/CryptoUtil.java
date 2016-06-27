/* 
 * CryptoUtil..java          0.3 2015-03-08
 * 
 * Licensed to the Apache Software Foundation (ASF) under one 
 * or more contributor license agreements.  See the NOTICE file 
 * distributed with this work for additional information 
 * regarding copyright ownership.  The ASF licenses this file 
 * to you under the Apache License, Version 3.0 (the
 * "License"); you may not use this file except in compliance 
 * with the License.  You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 * d
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY 
 * KIND, either express or implied.  See the License for the 
 * specific language governing permissions and limitations 
 * under the License. 
 */

package de.simu.decoit.android.decomap.util;

import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

/**
 * Utility class that provides cryptographic algorithms.
 *
 * @author Ingo Bente, Tobias Ruhe (FHH)
 * @version 0.3
 */
public class CryptoUtil {

    /**
     * Calculate a hash value for the given {@link String} s with the specified
     * algorithm dig.
     *
     * @param s value to be hashed
     * @param mdId specified algorithm dig
     * @return the hashed value. if problems do occur, an empty {@link String} is returned
     */
    private static String hash(String s, MessageDigestId mdId) {
        StringBuffer buffer = new StringBuffer();
        MessageDigest md;
        byte[] hash;

        // check input string
        if (s == null) {
            Log.w(CryptoUtil.class.getName(), "Empty String given.");
            return buffer.toString();
        }

        // calculate digest
        try {
            md = MessageDigest.getInstance(mdId.toString());
            buffer = new StringBuffer();
            hash = md.digest(s.getBytes());
            for (Byte b : hash) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1)
                    buffer.append(0);
                buffer.append(hex);
            }
        } catch (NoSuchAlgorithmException e) {
            Toolbox.logTxt(CryptoUtil.class.getClass().getName(),
                    "Algorithm not found. This really should not happen!" + Arrays.toString(e.getStackTrace()));
        }

        return buffer.toString();
    }

    /**
     * Get the SHA-1 hash of the given {@link String}
     *
     * @param s String which should be a SHA-1 hash
     * @return SHA-1 hash of given string
     *
     * @deprecated maybe useful in the future
     */
    @Deprecated
    public static String sha1(String s) {
        return hash(s, MessageDigestId.SHA1);
    }

    /**
     * Get the SHA256 hash of the given {@link String}
     *
     * @param s String which should be a SHA256 hash
     * @return SHA256 hash of given string
     */
    public static String sha256(String s) {
        return hash(s, MessageDigestId.SHA256);
    }

    /**
     * Get the MD5 hash of the given {@link String}
     *
     * @param s String which should be a md5 hash
     * @return md5 hash of given string
     *
     * @deprecated maybe useful in the future
     */
    @Deprecated
    public static String md5(String s) {
        return hash(s, MessageDigestId.MD5);
    }


    /**
     * Get a random UUID of the given length
     *
     * @param length max length of UUID
     * @return UUID and null if lenght <= 0
     *
     * @deprecated maybe useful in the future
     */
    @Deprecated
    public static String randomUUID(int length) {
        if (length > 0) {
            String uuid = UUID.randomUUID().toString();
            if (uuid.length() <= length)
                return uuid;
            else
                return uuid.substring(0, length - 1);
        } else
            return null;
    }

    /**
     * Identifiers for message digest algorithms.
     *
     * @author Ingo Bente (FHH)
     */
    private enum MessageDigestId {
        SHA256("SHA-256"), MD5("MD5"), SHA1("SHA-1");

        private final String name;

        MessageDigestId(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
