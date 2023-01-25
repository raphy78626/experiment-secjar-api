package com.secjar.secjarapi.services;

import CryptoServerAPI.CryptoServerException;
import CryptoServerCXI.CryptoServerCXI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class HsmService {

    @Value("${hsm.login}")
    private String hsmLogin;
    @Value("${hsm.password}")
    private String hsmPassword;

    public CryptoServerCXI.Key generateKey(String keyName) {

        String device = System.getenv("CRYPTOSERVER");
        if (device == null) {
            device = "3001@127.0.0.1";
        }

        CryptoServerCXI serverCXI = null;

        try {
            serverCXI = new CryptoServerCXI(device, 3000);
            serverCXI.setTimeout(60000);

            serverCXI.logonPassword(hsmLogin, hsmPassword);

            CryptoServerCXI.KeyAttributes keyAttributes = new CryptoServerCXI.KeyAttributes();
            keyAttributes.setAlgo(CryptoServerCXI.KEY_ALGO_AES);
            keyAttributes.setSize(256);
            keyAttributes.setName(keyName);

            return serverCXI.generateKey(CryptoServerCXI.FLAG_EXTERNAL, keyAttributes);
        } catch (CryptoServerException | IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (serverCXI != null) {
                serverCXI.close();
            }
        }
    }
}
