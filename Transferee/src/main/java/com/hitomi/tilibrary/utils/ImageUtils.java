package com.hitomi.tilibrary.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public final class ImageUtils {
    public static final int TYPE_STILL_IMAGE = 0;
    public static final int TYPE_GIF = 1;
    public static final int TYPE_ANIMATED_WEBP = 2;
    public static final int TYPE_STILL_WEBP = 3;

    private static final int ANIMATED_WEBP_MASK = 0x02;

    private ImageUtils() {
    }

    /**
     * For GIF, we only need 3 bytes, 'GIF',
     * For WebP, we need 12 bytes, 'RIFF' + size + 'WEBP',
     * to determine still/animated WebP, we need 5 extra bytes, 4 bytes chunk header to check
     * for extended WebP format, 1 byte to check for animated bit.
     *
     * reference: https://developers.google.com/speed/webp/docs/riff_container
     */
    public static int getImageType(File file) {
        int type = TYPE_STILL_IMAGE;
        try {
            FileInputStream inputStream = new FileInputStream(file);

            byte[] header = new byte[20];
            int read = inputStream.read(header);
            if (read >= 3 && isGifHeader(header)) {
                type = TYPE_GIF;
            } else if (read >= 12 && isWebpHeader(header)) {
                if (read >= 17 && isExtendedWebp(header)
                    && (header[16] & ANIMATED_WEBP_MASK) != 0) {
                    type = TYPE_ANIMATED_WEBP;
                } else {
                    type = TYPE_STILL_WEBP;
                }
            }

            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return type;
    }

    public static String typeName(int type) {
        switch (type) {
            case TYPE_GIF:
                return "GIF";
            case TYPE_STILL_WEBP:
                return "STILL_WEBP";
            case TYPE_ANIMATED_WEBP:
                return "ANIMATED_WEBP";
            case TYPE_STILL_IMAGE:
            default:
                return "STILL_IMAGE";
        }
    }

    private static boolean isGifHeader(byte[] header) {
        return header[0] == 'G' && header[1] == 'I' && header[2] == 'F';
    }

    private static boolean isWebpHeader(byte[] header) {
        return header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
               && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P';
    }

    private static boolean isExtendedWebp(byte[] header) {
        return header[12] == 'V' && header[13] == 'P' && header[14] == '8' && header[15] == 'X';
    }
}
