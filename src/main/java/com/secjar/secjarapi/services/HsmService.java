package com.secjar.secjarapi.services;

import CryptoServerAPI.CryptoServerException;
import CryptoServerCXI.CryptoServerCXI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@Service
public class HsmService {

    @Value("${hsm.login}")
    private String hsmLogin;
    @Value("${hsm.password}")
    private String hsmPassword;
    @Value("${hsm.keyStore.location}")
    private String hsmKeyStoreLocation;

    public CryptoServerCXI.Key generateKey(String keyName) {

        CryptoServerCXI serverCXI = initializeServerCXI();

        try {
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

    public byte[] insertKeyToStore(CryptoServerCXI.Key keyToStore) {

        byte[] keyIndex;
        CryptoServerCXI.KeyStore keyStore;

        try {
            keyStore = new CryptoServerCXI.KeyStore(hsmKeyStoreLocation, 16);
            keyIndex = keyToStore.getUName();
            return keyStore.insertKey(CryptoServerCXI.FLAG_OVERWRITE, keyIndex, keyToStore);
        } catch (NoSuchAlgorithmException | CryptoServerException | IOException e) {
            throw new RuntimeException("Problem while storing the key in hsm", e);
        }
    }

    public CryptoServerCXI.Key getKeyFromStore(byte[] keyIndex) {
        try {
            CryptoServerCXI.KeyStore keyStore = new CryptoServerCXI.KeyStore(hsmKeyStoreLocation, 16);
            return keyStore.getKey(keyIndex);
        } catch (IOException | CryptoServerException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] encryptData(byte[] data, CryptoServerCXI.Key keyForEncryption) {
        CryptoServerCXI serverCXI = initializeServerCXI();
        byte[] encryptedData;

        try {
            int mechanisms = CryptoServerCXI.MECH_MODE_ENCRYPT | CryptoServerCXI.MECH_CHAIN_CBC | CryptoServerCXI.MECH_PAD_PKCS5;
            encryptedData = serverCXI.crypt(keyForEncryption, mechanisms, null, data, null);
        } catch (CryptoServerException | IOException e) {
            throw new RuntimeException("Error while encrypting data", e);
        } finally {
            if (serverCXI != null) {
                serverCXI.close();
            }
        }

        return encryptedData;
    }

    private CryptoServerCXI initializeServerCXI() {

        String device = System.getenv("CRYPTOSERVER");
        if (device == null) {
            device = "3001@127.0.0.1";
        }

        CryptoServerCXI serverCXI;

        try {
            serverCXI = new CryptoServerCXI(device, 3000);
            serverCXI.setTimeout(60000);

            serverCXI.logonPassword(hsmLogin, hsmPassword);
        } catch (CryptoServerException | IOException e) {
            throw new RuntimeException("Error while initializing Crypto Server CXI", e);
        }

        return serverCXI;
    }
}
