/*
 * Copyright 2019 ACINQ SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.acinq.eclair.wallet.utils;

import com.tozny.crypto.android.AesCbcWithIntegrity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

import fr.acinq.bitcoin.ByteVector32;
import fr.acinq.bitcoin.DeterministicWallet;
import fr.acinq.eclair.wallet.BuildConfig;

public class EncryptedBackup extends EncryptedData {

  /**
   * Version 1 uses the same derivation path as BIP49 for the encryption key.
   *
   * @see #generateBackupKey_v1(DeterministicWallet.ExtendedPrivateKey)
   * @deprecated should only be used to decrypt older files, not to encrypt new files.
   */
  public final static byte BACKUP_VERSION_1 = 1;

  /**
   * Version 2 uses either m/42'/0' (mainnet) or m/42'/1' (testnet) as derivation path for the encryption key.
   * This is the only difference with version 1.
   *
   * @see #generateBackupKey_v2(DeterministicWallet.ExtendedPrivateKey)
   */
  public final static byte BACKUP_VERSION_2 = 2;

  /**
   * Version 3 compresses the data content before encrypting (uses ZLIB, see {@link Deflater}).
   */
  public final static byte BACKUP_VERSION_3 = 3;

  private static final int IV_LENGTH_V1 = 16;
  private static final int MAC_LENGTH_V1 = 32;

  private EncryptedBackup(int version, AesCbcWithIntegrity.CipherTextIvMac civ) {
    super(version, null, civ);
  }

  /**
   * Encrypt data with AES CBC and return an EncryptedBackup object containing the encrypted data.
   *
   * @param data    data to encrypt
   * @param key     the secret key encrypting the data
   * @param version the version describing the serialization to use for the EncryptedBackup object
   * @return a encrypted backup object ready to be serialized
   * @throws GeneralSecurityException
   */
  public static EncryptedBackup encrypt(final byte[] data, final AesCbcWithIntegrity.SecretKeys key, final int version) throws GeneralSecurityException, IOException {
    final byte[] finalData = version >= BACKUP_VERSION_3 ? compressByteArray(data) : data;
    final AesCbcWithIntegrity.CipherTextIvMac civ = AesCbcWithIntegrity.encrypt(finalData, key);
    return new EncryptedBackup(version, civ);
  }

  /**
   * Decrypt an encrypted backup object with a password and returns a byte array. If version >= 3, this
   * method also unzips the decrypted data.
   *
   * @param key key encrypting the data
   * @return a byte array containing the decrypted data
   * @throws GeneralSecurityException if the password is not correct
   */
  public byte[] decrypt(final AesCbcWithIntegrity.SecretKeys key) throws GeneralSecurityException, IOException {
    final byte[] decryptedData = AesCbcWithIntegrity.decrypt(civ, key);
    return getVersion() >= BACKUP_VERSION_3 ? decompressByteArray(decryptedData) : decryptedData;
  }

  private static byte[] compressByteArray(final byte[] data) throws IOException {
    byte[] result = data;
    final ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length);
    final Deflater deflater = new Deflater();
    final DeflaterOutputStream dos = new DeflaterOutputStream(baos, deflater);
    try {
      dos.write(data);
      dos.finish();
      dos.close();
      result = baos.toByteArray();
    } finally {
      deflater.end();
      closeSilent(dos);
    }
    return result;
  }

  private static byte[] decompressByteArray(final byte[] data) throws IOException {
    byte[] result = data;
    final ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length);
    final Inflater inflater = new Inflater();
    final InflaterOutputStream ios = new InflaterOutputStream(baos, inflater);
    try {
      ios.write(data);
      ios.finish();
      ios.close();
      result = baos.toByteArray();
    } finally {
      inflater.end();
      closeSilent(ios);
    }
    return result;
  }

  private static void closeSilent(final Closeable closeable) {
    try {
      if (closeable != null) {
        closeable.close();
      }
    } catch (IOException ignored) {
    }
  }

  /**
   * Read an array of byte and deserializes it as an EncryptedBackup object.
   *
   * @param serialized array to deserialize
   * @return
   */
  public static EncryptedBackup read(final byte[] serialized) {
    final ByteArrayInputStream stream = new ByteArrayInputStream(serialized);
    final int version = stream.read();
    if (version == BACKUP_VERSION_1 || version == BACKUP_VERSION_2 || version == BACKUP_VERSION_3) {
      final byte[] iv = new byte[IV_LENGTH_V1];
      stream.read(iv, 0, IV_LENGTH_V1);
      final byte[] mac = new byte[MAC_LENGTH_V1];
      stream.read(mac, 0, MAC_LENGTH_V1);
      final byte[] cipher = new byte[stream.available()];
      stream.read(cipher, 0, stream.available());
      return new EncryptedBackup(version, new AesCbcWithIntegrity.CipherTextIvMac(cipher, iv, mac));
    } else {
      throw new RuntimeException("unhandled encrypted backup version");
    }
  }

  /**
   * Derives a hardened key from the extended key. This is used to encrypt/decrypt the channels backup files.
   * Path is the same as BIP49.
   */
  public static ByteVector32 generateBackupKey_v1(final DeterministicWallet.ExtendedPrivateKey pk) {
    final DeterministicWallet.ExtendedPrivateKey dpriv = DeterministicWallet.derivePrivateKey(pk,
      DeterministicWallet.KeyPath$.MODULE$.apply("m/49'"));
    return dpriv.secretkeybytes();
  }

  /**
   * Derives a hardened key from the extended key. This is used to encrypt/decrypt the channels backup files.
   * Path depends on the chain used by the wallet, mainnet or testnet.
   */
  public static ByteVector32 generateBackupKey_v2(final DeterministicWallet.ExtendedPrivateKey pk) {
    final DeterministicWallet.ExtendedPrivateKey dpriv = DeterministicWallet.derivePrivateKey(pk,
      DeterministicWallet.KeyPath$.MODULE$.apply("mainnet".equals(BuildConfig.CHAIN) ? "m/42'/0'" : "m/42'/1'"));
    return dpriv.secretkeybytes();
  }

  /**
   * Serializes an encrypted backup as a byte array, with the result depending on the object version.
   */
  @Override
  public byte[] write() throws IOException {
    if (version == BACKUP_VERSION_1 || version == BACKUP_VERSION_2 || version == BACKUP_VERSION_3) {
      if (civ.getIv().length != IV_LENGTH_V1 || civ.getMac().length != MAC_LENGTH_V1) {
        throw new RuntimeException("could not serialize backup because fields are not of the correct length");
      }
      final ByteArrayOutputStream array = new ByteArrayOutputStream();
      array.write(version);
      array.write(civ.getIv());
      array.write(civ.getMac());
      array.write(civ.getCipherText());
      return array.toByteArray();
    } else {
      throw new RuntimeException("unhandled version");
    }
  }
}
