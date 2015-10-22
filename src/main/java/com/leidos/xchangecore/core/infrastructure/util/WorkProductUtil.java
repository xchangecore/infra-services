package com.leidos.xchangecore.core.infrastructure.util;

import java.util.Date;
import java.util.UUID;
import java.util.zip.CRC32;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leidos.xchangecore.core.infrastructure.service.WorkProductService;

public class WorkProductUtil {

    static Logger log = LoggerFactory.getLogger(InterestGroupInfoUtil.class);

    public static final String getACT() {

        Date now = new Date();
        return WorkProductService.ACTPrefix + "-" + now.toString() + "-" +
               UUID.randomUUID().toString();
    }

    public static final String calculateChecksum(String date, Integer version, Integer size) {

        String crcStr = date.concat(version.toString() + size.toString());
        long crc = calculateCRC(crcStr);
        String checksum = date + ":" + version + ":" + size.toString() + ":" + crc;
        return checksum;
    }

    private static final long calculateCRC(String crcStr) {

        long crc = 0;
        try {
            byte bytes[] = crcStr.getBytes();

            CRC32 crcEngine = new CRC32();
            crcEngine.update(bytes);
            crc = crcEngine.getValue();
        } catch (Throwable e) {
            e.printStackTrace();
            log.error("Error calculating checksum with crcStr=" + crcStr);
        }
        return crc;
    }

}
