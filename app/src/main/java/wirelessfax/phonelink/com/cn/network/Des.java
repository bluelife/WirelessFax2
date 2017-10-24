package wirelessfax.phonelink.com.cn.network;
import org.slf4j.LoggerFactory;

import javax.crypto.spec.IvParameterSpec;

import javax.crypto.spec.IvParameterSpec;


import java.security.Key;
import java.security.SecureRandom;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Des
{
	private static  org.slf4j.Logger log = LoggerFactory.getLogger(Des.class);

	/**
	 * 加密
	 * @param datasource byte[]
	 * @return byte[]
	 */
	public static byte[] encrypt(byte[] datasource) {
		try{
			byte[] key = { 48, (byte)138, 86, 108, (byte)178, (byte)255,(byte) 228, 118 };
			byte[] iv1 = { 108, 118, 68, 84, (byte)138, (byte)225, 98, (byte)178 };
			IvParameterSpec iv = new IvParameterSpec(iv1);
			DESKeySpec desKey = new DESKeySpec(key);
			//创建一个密匙工厂，然后用它把DESKeySpec转换成
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey securekey = keyFactory.generateSecret(desKey);
			//Cipher对象实际完成加密操作
			Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			//用密匙初始化Cipher对象
			cipher.init(Cipher.ENCRYPT_MODE, securekey, iv);
			//现在，获取数据并加密
			//正式执行加密操作
			return cipher.doFinal(datasource);
		}catch(Throwable e){
			e.printStackTrace();
		}
		return null;
	}
    public static byte[] toByte(String hexString) {
        int len = hexString.length()/2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2*i, 2*i+2), 16).byteValue();
        return result;
    }

    public static String toHex(byte[] buf) {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2*buf.length);
        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }
        return result.toString();
    }
    private final static String HEX = "0123456789ABCDEF";
    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b>>4)&0x0f)).append(HEX.charAt(b&0x0f));
    }

    private final static String TRANSFORMATION = "DES/CBC/PKCS5Padding";
	/**
	 * 解密
	 * @param src byte[]
	 * @return byte[]
	 * @throws Exception
	 */
	public static byte[] decrypt(byte[] src) throws Exception {
		//建立加密对象的密钥和偏移量，此值重要，不能修改
		byte[] key = { 48, (byte)138, 86, 108, (byte)178, (byte)255, (byte)228, 118 };
		byte[] iv1 = { 108, 118, 68, 84, (byte)138, (byte)225, 98, (byte)178 };


		IvParameterSpec iv = new IvParameterSpec(iv1);
		log.debug("DES ENCRYPT　0");
        SecretKeySpec securekey = new SecretKeySpec(key, "DES");
		log.debug("DES ENCRYPT　3");
// Cipher对象实际完成解密操作
		Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		log.debug("DES ENCRYPT　4");
// 用密匙初始化Cipher对象
		cipher.init(Cipher.DECRYPT_MODE, securekey, iv);
		log.debug("DES ENCRYPT　5   "+src.length+"  "+new String(src));

        byte[] bytes = cipher.doFinal(toByte(new String(src)));

        byte[] bytes2 = new byte[bytes.length];

        for (int i=0; i<bytes.length; i++) {
            bytes2[i] = bytes[i];
        }

        log.debug("DES ENCRYPT　5   "+"  "+new String(bytes2));
// 真正开始解密操作
		return bytes2;
	}
}
