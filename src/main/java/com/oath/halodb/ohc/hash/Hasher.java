/*
 * Copyright 2018, Oath Inc
 * Licensed under the terms of the Apache License 2.0. Please refer to accompanying LICENSE file for terms.
 */

// This code is a derivative work heavily modified from the OHC project. See NOTICE file for copyright and license.

package com.oath.halodb.ohc.hash;

import com.oath.halodb.ohc.Uns;
import net.jpountz.xxhash.XXHashFactory;

import java.util.zip.CRC32;

public abstract class Hasher {

    static Hasher create(HashAlgorithm hashAlgorithm) {
        String cls = forAlg(hashAlgorithm);
        try {
            return (Hasher) Class.forName(cls).newInstance();
        } catch (ClassNotFoundException e) {
            if (hashAlgorithm == HashAlgorithm.XX) {
                cls = forAlg(HashAlgorithm.CRC32);
                try {
                    return (Hasher) Class.forName(cls).newInstance();
                } catch (InstantiationException | ClassNotFoundException | IllegalAccessException e1) {
                    throw new RuntimeException(e1);
                }
            }
            throw new RuntimeException(e);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static String forAlg(HashAlgorithm hashAlgorithm) {
        return Hasher.class.getName()
               + '$'
               + hashAlgorithm.name().substring(0, 1)
               + hashAlgorithm.name().substring(1).toLowerCase()
               + "Hash";
    }

    abstract long hash(byte[] array);

    abstract long hash(long address, long offset, int length);


    static final class Crc32Hash extends Hasher {

        long hash(byte[] array) {
            CRC32 crc = new CRC32();
            crc.update(array);
            long h = crc.getValue();
            h |= h << 32;
            return h;
        }

        long hash(long address, long offset, int length) {
            return Uns.crc32(address, offset, length);
        }
    }

    static final class Murmur3Hash extends Hasher {

        long hash(byte[] array) {
            int o = 0;
            int r = array.length;

            long h1 = 0L;
            long h2 = 0L;
            long k1, k2;

            for (; r >= 16; r -= 16) {
                k1 = getLong(array, o);
                o += 8;
                k2 = getLong(array, o);
                o += 8;

                // bmix64()

                h1 ^= mixK1(k1);

                h1 = Long.rotateLeft(h1, 27);
                h1 += h2;
                h1 = h1 * 5 + 0x52dce729;

                h2 ^= mixK2(k2);

                h2 = Long.rotateLeft(h2, 31);
                h2 += h1;
                h2 = h2 * 5 + 0x38495ab5;
            }

            if (r > 0) {
                k1 = 0;
                k2 = 0;
                switch (r) {
                    case 15:
                        k2 ^= toLong(array[o + 14]) << 48; // fall through
                    case 14:
                        k2 ^= toLong(array[o + 13]) << 40; // fall through
                    case 13:
                        k2 ^= toLong(array[o + 12]) << 32; // fall through
                    case 12:
                        k2 ^= toLong(array[o + 11]) << 24; // fall through
                    case 11:
                        k2 ^= toLong(array[o + 10]) << 16; // fall through
                    case 10:
                        k2 ^= toLong(array[o + 9]) << 8; // fall through
                    case 9:
                        k2 ^= toLong(array[o + 8]); // fall through
                    case 8:
                        k1 ^= getLong(array, o);
                        break;
                    case 7:
                        k1 ^= toLong(array[o + 6]) << 48; // fall through
                    case 6:
                        k1 ^= toLong(array[o + 5]) << 40; // fall through
                    case 5:
                        k1 ^= toLong(array[o + 4]) << 32; // fall through
                    case 4:
                        k1 ^= toLong(array[o + 3]) << 24; // fall through
                    case 3:
                        k1 ^= toLong(array[o + 2]) << 16; // fall through
                    case 2:
                        k1 ^= toLong(array[o + 1]) << 8; // fall through
                    case 1:
                        k1 ^= toLong(array[o]);
                        break;
                    default:
                        throw new AssertionError("Should never get here.");
                }

                h1 ^= mixK1(k1);
                h2 ^= mixK2(k2);
            }

            // makeHash()

            h1 ^= array.length;
            h2 ^= array.length;

            h1 += h2;
            h2 += h1;

            h1 = fmix64(h1);
            h2 = fmix64(h2);

            h1 += h2;
            //h2 += h1;

            // padToLong()
            return h1;
        }

        private static long getLong(byte[] array, int o) {
            long l = toLong(array[o + 7]) << 56;
            l |= toLong(array[o + 6]) << 48;
            l |= toLong(array[o + 5]) << 40;
            l |= toLong(array[o + 4]) << 32;
            l |= toLong(array[o + 3]) << 24;
            l |= toLong(array[o + 2]) << 16;
            l |= toLong(array[o + 1]) << 8;
            l |= toLong(array[o]);
            return l;
        }

        long hash(long adr, long offset, int length) {
            long o = offset;
            long r = length;

            long h1 = 0L;
            long h2 = 0L;
            long k1, k2;

            for (; r >= 16; r -= 16) {
                k1 = getLong(adr, o);
                o += 8;
                k2 = getLong(adr, o);
                o += 8;

                // bmix64()

                h1 ^= mixK1(k1);

                h1 = Long.rotateLeft(h1, 27);
                h1 += h2;
                h1 = h1 * 5 + 0x52dce729;

                h2 ^= mixK2(k2);

                h2 = Long.rotateLeft(h2, 31);
                h2 += h1;
                h2 = h2 * 5 + 0x38495ab5;
            }

            if (r > 0) {
                k1 = 0;
                k2 = 0;
                switch ((int) r) {
                    case 15:
                        k2 ^= toLong(Uns.getByte(adr, o + 14)) << 48; // fall through
                    case 14:
                        k2 ^= toLong(Uns.getByte(adr, o + 13)) << 40; // fall through
                    case 13:
                        k2 ^= toLong(Uns.getByte(adr, o + 12)) << 32; // fall through
                    case 12:
                        k2 ^= toLong(Uns.getByte(adr, o + 11)) << 24; // fall through
                    case 11:
                        k2 ^= toLong(Uns.getByte(adr, o + 10)) << 16; // fall through
                    case 10:
                        k2 ^= toLong(Uns.getByte(adr, o + 9)) << 8; // fall through
                    case 9:
                        k2 ^= toLong(Uns.getByte(adr, o + 8)); // fall through
                    case 8:
                        k1 ^= getLong(adr, o);
                        break;
                    case 7:
                        k1 ^= toLong(Uns.getByte(adr, o + 6)) << 48; // fall through
                    case 6:
                        k1 ^= toLong(Uns.getByte(adr, o + 5)) << 40; // fall through
                    case 5:
                        k1 ^= toLong(Uns.getByte(adr, o + 4)) << 32; // fall through
                    case 4:
                        k1 ^= toLong(Uns.getByte(adr, o + 3)) << 24; // fall through
                    case 3:
                        k1 ^= toLong(Uns.getByte(adr, o + 2)) << 16; // fall through
                    case 2:
                        k1 ^= toLong(Uns.getByte(adr, o + 1)) << 8; // fall through
                    case 1:
                        k1 ^= toLong(Uns.getByte(adr, o));
                        break;
                    default:
                        throw new AssertionError("Should never get here.");
                }

                h1 ^= mixK1(k1);
                h2 ^= mixK2(k2);
            }

            // makeHash()

            h1 ^= length;
            h2 ^= length;

            h1 += h2;
            h2 += h1;

            h1 = fmix64(h1);
            h2 = fmix64(h2);

            h1 += h2;
            //h2 += h1;

            // padToLong()

            return h1;
        }

        private static long getLong(long adr, long o) {
            long l = toLong(Uns.getByte(adr, o + 7)) << 56;
            l |= toLong(Uns.getByte(adr, o + 6)) << 48;
            l |= toLong(Uns.getByte(adr, o + 5)) << 40;
            l |= toLong(Uns.getByte(adr, o + 4)) << 32;
            l |= toLong(Uns.getByte(adr, o + 3)) << 24;
            l |= toLong(Uns.getByte(adr, o + 2)) << 16;
            l |= toLong(Uns.getByte(adr, o + 1)) << 8;
            l |= toLong(Uns.getByte(adr, o));
            return l;
        }

        static final long C1 = 0x87c37b91114253d5L;
        static final long C2 = 0x4cf5ad432745937fL;

        static long fmix64(long k) {
            k ^= k >>> 33;
            k *= 0xff51afd7ed558ccdL;
            k ^= k >>> 33;
            k *= 0xc4ceb9fe1a85ec53L;
            k ^= k >>> 33;
            return k;
        }

        static long mixK1(long k1) {
            k1 *= C1;
            k1 = Long.rotateLeft(k1, 31);
            k1 *= C2;
            return k1;
        }

        static long mixK2(long k2) {
            k2 *= C2;
            k2 = Long.rotateLeft(k2, 33);
            k2 *= C1;
            return k2;
        }

        static long toLong(byte value) {
            return value & 0xff;
        }
    }

    static final class XxHash extends Hasher {

        private static final XXHashFactory xx = XXHashFactory.fastestInstance();

        long hash(long address, long offset, int length) {
            return xx.hash64().hash(Uns.directBufferFor(address, offset, length, true), 0);
        }

        long hash(byte[] array) {
            return xx.hash64().hash(array, 0, array.length, 0);
        }
    }
}
